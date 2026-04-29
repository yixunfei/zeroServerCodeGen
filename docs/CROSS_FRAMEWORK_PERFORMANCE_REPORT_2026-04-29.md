# zero-codegen Real Reference Codec Performance Report

Date: 2026-04-29  
Project: `zero-codegen`  
Benchmark profile: `standard`  
Raw outputs: `target/benchmark-results/benchmark-report.md`, `target/benchmark-results/benchmark-results.csv`

## 中文报告

### 1. 执行摘要

本轮在上一版横向评测基础上引入了更真实的参照路径：

- `protobuf-generated`：由 Maven 在 `test-compile` 阶段执行 `protoc 3.25.9`，从 `src/test/proto/benchmark_cross_frame.proto` 生成官方 Java 类。
- `protobuf-codedstream`：保留低层 `CodedInputStream` / `CodedOutputStream` 手写 codec，作为 wire-format 和低层 API 参照。
- `flatbuffers-runtime randomRead`：按字段随机访问 FlatBuffers buffer，不完整物化 DTO。
- `capnproto-runtime randomRead`：按字段、pointer、list 随机读取 Cap'n Proto message，不完整物化 DTO。
- `capnproto-runtime randomWrite`：按非声明顺序写入字段和列表元素，然后序列化。

核心结论：

- 编码尺寸：Zero 仍是三档 cross-frame 负载中最小线格式。官方 Protobuf 生成类比手写 CodedStream 更紧凑，约为 Zero 的 1.16 至 1.17 倍。
- 完整 encode/decode：SBE/Agrona 仍是吞吐上限参照；Zero Cursor/ByteBuf 在尺寸和分配上更紧凑；官方 Protobuf 生成类明显优于旧 CodedStream encode。
- 随机读取：FlatBuffers 与 Cap'n Proto 的按需随机读取优势非常明显，large 负载下分别约 104 ns/op 与 113 ns/op，远低于完整 DTO decode。
- Cap'n Proto randomWrite：随机顺序写入与常规 encode 量级接近，large 负载约 26.3 us/op。
- 工程口径：本轮 Protobuf 已是官方生成类；Cap'n Proto 当前仍使用 Java runtime + 等价 struct factory，因为本机没有 `capnp`/`capnpc-java` 命令链。随机访问语义已纳入测试，后续如需“官方 Cap'n Proto 生成类”可继续接入外部编译器。

### 2. 运行方式

从干净目录运行时必须先执行 test 编译，以生成 Protobuf 官方 Java 类：

```powershell
mvn "-DskipTests" test-compile `
  "-Dzero.benchmark.profile=standard" `
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java `
  "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.zero.codegen.benchmark.CodegenBenchmarkLauncher"
```

运行环境：

| 项目 | 值 |
| --- | --- |
| OS | Windows 11 10.0 amd64 |
| JDK | Oracle Java 21.0.10 |
| JVM | Java HotSpot 64-Bit Server VM 21.0.10 |
| Warmup | 3 iterations |
| Measurement | 6 iterations |
| Operations / iteration | 1000 |
| Protobuf compiler | `protoc 3.25.9` via `protobuf-maven-plugin` |

### 3. 数据模型

横向对比使用 `CrossFrameworkFrame` 公共子集，覆盖：

- 标量：`long`、`int`、`float`、`boolean`、enum。
- 文本与二进制：`String remark`、`byte[] payload`。
- primitive array：`int[] changedSlots`。
- 对象列表：`List<CfSkill> casts`、`List<CfBuff> buffs`。

| 数据集 | skills | buffs | int slots | payload bytes |
| --- | ---: | ---: | ---: | ---: |
| `cross-frame-small` | 8 | 8 | 16 | 256 |
| `cross-frame-medium` | 64 | 64 | 128 | 2048 |
| `cross-frame-large` | 256 | 256 | 512 | 8192 |

### 4. 编码尺寸

| 数据集 | Zero | Protobuf CodedStream | Protobuf Generated | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| small | 507 | 628 | 592 | 1008 | 686 | 864 |
| medium | 3684 | 4581 | 4294 | 6224 | 4833 | 5816 |
| large | 14844 | 18343 | 17192 | 24080 | 19043 | 22712 |

相对 Zero 的尺寸倍率：

| 数据集 | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: |
| small | 1.17x | 1.24x | 1.99x | 1.35x | 1.70x |
| medium | 1.17x | 1.24x | 1.69x | 1.31x | 1.58x |
| large | 1.16x | 1.24x | 1.62x | 1.28x | 1.53x |

### 5. 完整编码性能

单位：`ns/op mean`。

| 数据集 | Zero Cursor | Zero ByteBuf | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| small | 2930.00 | 4133.53 | 8065.00 | 17394.48 | 11068.78 | 1943.08 | 11479.35 |
| medium | 8009.72 | 6161.88 | 5959.68 | 71855.72 | 10313.48 | 6572.48 | 9688.73 |
| large | 15272.23 | 19191.57 | 22971.90 | 281990.83 | 38425.60 | 4875.47 | 25368.92 |

观察：

- small/large encode 中 SBE/Agrona 最快；medium 本轮官方 Protobuf 生成类略快于 Zero ByteBuf 与 SBE/Agrona。
- Zero Cursor 在 small/large 中保持较好的速度与最小尺寸，分配也低。
- 官方 Protobuf 生成类大幅改善了旧 CodedStream encode 的分配与耗时，large encode 从约 282 us/op 降至约 23 us/op。

### 6. 完整解码性能

单位：`ns/op mean`。

| 数据集 | Zero Cursor | Zero ByteBuf | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| small | 2064.85 | 2632.03 | 7495.40 | 5373.23 | 4351.63 | 2221.47 | 5357.62 |
| medium | 3659.93 | 3073.10 | 10400.63 | 9643.95 | 4793.90 | 1819.67 | 4893.67 |
| large | 9584.22 | 11793.38 | 35437.67 | 37774.43 | 21012.20 | 5041.55 | 13891.82 |

观察：

- small decode 中 Zero Cursor 最快；medium/large decode 中 SBE/Agrona 最快。
- Cap'n Proto 完整 decode 在 large 上优于 FlatBuffers 和 Protobuf，但仍慢于 Zero 与 SBE。
- 官方 Protobuf 生成类 decode 未必优于 CodedStream，因为 generated message 构建会维护不可变 message/list 结构。

### 7. 随机读取与随机写入

单位：`ns/op mean`。

| 数据集 | Protobuf Generated randomRead | FlatBuffers randomRead | Cap'n Proto randomRead | Cap'n Proto randomWrite |
| --- | ---: | ---: | ---: | ---: |
| small | 3615.08 | 534.32 | 1726.57 | 4756.62 |
| medium | 8349.98 | 465.40 | 675.93 | 9173.90 |
| large | 31781.75 | 104.05 | 112.90 | 26275.08 |

观察：

- FlatBuffers 与 Cap'n Proto 的随机读取正是它们的优势场景；随着 payload/list 增大，按需读取相对完整 decode 的收益更明显。
- Protobuf generated randomRead 仍需要 parse 整个 message，因此本质上不是零拷贝随机访问，只是解析后访问 generated object 的部分字段。
- Cap'n Proto randomWrite 与常规 encode 量级相近，说明字段写入顺序对该模型影响有限，主要成本仍在 message 构建、pointer/list 初始化与最终 stream serialization。

### 8. 分配摘要

| 数据集 | codec | operation | alloc B/op |
| --- | --- | --- | ---: |
| small | zero-cursor | encode | 600 |
| small | protobuf-generated | encode | 3456 |
| small | flatbuffers-runtime | randomRead | 176 |
| small | capnproto-runtime | randomRead | 800 |
| medium | protobuf-generated | encode | 16624 |
| medium | capnproto-runtime | randomWrite | 47336 |
| large | zero-cursor | encode | 14936 |
| large | protobuf-generated | encode | 64616 |
| large | flatbuffers-runtime | randomRead | 144 |
| large | capnproto-runtime | randomRead | 688 |

### 9. 局限与后续建议

- 当前 harness 是项目内轻量基准，不是 JMH。它适合内部趋势、回归和工程定位；公开发布建议追加 JMH、GC/p99、async-profiler。
- Protobuf 已接入官方 `protoc` 生成类。Cap'n Proto 暂未接入官方生成器，原因是本机没有 `capnp`/`capnpc-java` 命令链；当前测试覆盖了 runtime struct 的随机读写语义。
- FlatBuffers 当前仍使用 runtime + 等价访问器，未接入 `flatc` 官方生成类；随机读取场景已覆盖其核心优势。
- randomRead 指标只读取少量代表性字段，不等价于完整业务消费。
- 后续可继续接入官方 `flatc`、SBE generator、`capnp compile`，并按“完整物化”和“按需字段访问”双轨输出。

## English Report

### 1. Executive Summary

This round upgrades the previous comparison with more realistic reference paths:

- `protobuf-generated`: official Java classes generated by `protoc 3.25.9` from `src/test/proto/benchmark_cross_frame.proto` during Maven `test-compile`.
- `protobuf-codedstream`: retained as a low-level handwritten `CodedInputStream` / `CodedOutputStream` reference.
- `flatbuffers-runtime randomRead`: field-level random access without full DTO materialization.
- `capnproto-runtime randomRead`: pointer/list random access without full DTO materialization.
- `capnproto-runtime randomWrite`: fields and list elements written in non-declaration order, then serialized.

Key findings:

- Encoded size: Zero remains the most compact wire format. Official Protobuf generated classes are more compact than the previous CodedStream path, about 1.16x to 1.17x Zero.
- Full encode/decode: SBE/Agrona remains the throughput ceiling in most large sequential cases. Zero keeps the smallest bytes and low allocation. Official Protobuf generated encode is dramatically better than the old CodedStream encode path.
- Random access: FlatBuffers and Cap'n Proto show their intended strengths. On the large dataset, random reads are about 104 ns/op and 113 ns/op respectively, far below full DTO decode.
- Cap'n Proto randomWrite is close to regular encode in magnitude, about 26.3 us/op on the large dataset.
- Scope: Protobuf is now an official generated-class comparison. Cap'n Proto still uses Java runtime plus generated-code-equivalent struct factories because the local `capnp` / `capnpc-java` toolchain is not installed. Its random-access behavior is now represented.

### 2. Command

Run `test-compile` first so Maven generates the official Protobuf Java classes:

```powershell
mvn "-DskipTests" test-compile `
  "-Dzero.benchmark.profile=standard" `
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java `
  "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.zero.codegen.benchmark.CodegenBenchmarkLauncher"
```

Environment:

| Item | Value |
| --- | --- |
| OS | Windows 11 10.0 amd64 |
| JDK | Oracle Java 21.0.10 |
| JVM | Java HotSpot 64-Bit Server VM 21.0.10 |
| Warmup | 3 iterations |
| Measurement | 6 iterations |
| Operations / iteration | 1000 |
| Protobuf compiler | `protoc 3.25.9` via `protobuf-maven-plugin` |

### 3. Dataset

| Dataset | skills | buffs | int slots | payload bytes |
| --- | ---: | ---: | ---: | ---: |
| `cross-frame-small` | 8 | 8 | 16 | 256 |
| `cross-frame-medium` | 64 | 64 | 128 | 2048 |
| `cross-frame-large` | 256 | 256 | 512 | 8192 |

### 4. Encoded Size

| Dataset | Zero | Protobuf CodedStream | Protobuf Generated | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| small | 507 | 628 | 592 | 1008 | 686 | 864 |
| medium | 3684 | 4581 | 4294 | 6224 | 4833 | 5816 |
| large | 14844 | 18343 | 17192 | 24080 | 19043 | 22712 |

Size ratio versus Zero:

| Dataset | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: |
| small | 1.17x | 1.24x | 1.99x | 1.35x | 1.70x |
| medium | 1.17x | 1.24x | 1.69x | 1.31x | 1.58x |
| large | 1.16x | 1.24x | 1.62x | 1.28x | 1.53x |

### 5. Full Encode Performance

Mean `ns/op`:

| Dataset | Zero Cursor | Zero ByteBuf | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| small | 2930.00 | 4133.53 | 8065.00 | 17394.48 | 11068.78 | 1943.08 | 11479.35 |
| medium | 8009.72 | 6161.88 | 5959.68 | 71855.72 | 10313.48 | 6572.48 | 9688.73 |
| large | 15272.23 | 19191.57 | 22971.90 | 281990.83 | 38425.60 | 4875.47 | 25368.92 |

### 6. Full Decode Performance

Mean `ns/op`:

| Dataset | Zero Cursor | Zero ByteBuf | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| small | 2064.85 | 2632.03 | 7495.40 | 5373.23 | 4351.63 | 2221.47 | 5357.62 |
| medium | 3659.93 | 3073.10 | 10400.63 | 9643.95 | 4793.90 | 1819.67 | 4893.67 |
| large | 9584.22 | 11793.38 | 35437.67 | 37774.43 | 21012.20 | 5041.55 | 13891.82 |

### 7. Random Access

Mean `ns/op`:

| Dataset | Protobuf Generated randomRead | FlatBuffers randomRead | Cap'n Proto randomRead | Cap'n Proto randomWrite |
| --- | ---: | ---: | ---: | ---: |
| small | 3615.08 | 534.32 | 1726.57 | 4756.62 |
| medium | 8349.98 | 465.40 | 675.93 | 9173.90 |
| large | 31781.75 | 104.05 | 112.90 | 26275.08 |

FlatBuffers and Cap'n Proto random reads avoid full DTO materialization and show the expected random-access advantage. Protobuf generated randomRead still parses the whole message first, so it should not be read as a zero-copy access metric.

### 8. Limitations And Next Steps

- The harness is lightweight and in-project, not JMH.
- Protobuf now uses official generated Java classes.
- Cap'n Proto and FlatBuffers still use runtime APIs plus generated-code-equivalent accessors. Official `capnp` and `flatc` generated classes can be added as the next strict comparison layer.
- randomRead covers representative fields only; it is not equivalent to full business consumption.
- Recommended next steps: add JMH, official `flatc`, official SBE generator output, official `capnp compile`, p99/GC metrics, and CPU/allocation profiles.
