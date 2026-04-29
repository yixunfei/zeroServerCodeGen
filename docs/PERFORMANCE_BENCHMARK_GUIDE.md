# 编解码性能基准说明

本文说明当前项目内置的性能基准套件、覆盖范围、运行方式、本机基线摘要，以及与 Protobuf、FlatBuffers、SBE、Cap'n Proto 的横向比较口径。

## 1. 覆盖范围

基准 schema 位于 `src/test/resources/benchmark/BenchmarkProtocol.si`，覆盖：

- fixed layout：`@fixed` / `@inline` 数值结构。
- packed 容器：`@packed List<Integer>`、`@packed List<Long>`、`@packed Map<Integer,Integer>`、`@packed Map<Integer,Long>`。
- hot 普通协议：基础类型、`String`、`Optional<String>`、primitive array、对象数组、`List`、`Set`、`Map`、嵌套 `List<Map<...>>`、大 Map。
- 横向公共协议：`CrossFrameworkFrame`，包含标量、enum、`String`、`byte[]`、`int[]`、对象列表。
- Zero 生成模式：Cursor encode/decode、ByteBuf encode/decode、Cursor `readInto` 复用、`estimatedSize()`。
- 横向参照框架：Protobuf 官方生成类、Protobuf CodedStream、FlatBuffers Java runtime、SBE-style Agrona fixed layout、Cap'n Proto Java runtime。
- 随机访问场景：Protobuf generated randomRead、FlatBuffers randomRead、Cap'n Proto randomRead、Cap'n Proto randomWrite。
- 辅助通用框架：Jackson JSON、Jackson Smile、Kryo、JDK serialization。

参照实现说明：

- `protobuf-generated` 使用 Maven 在 `test-compile` 阶段执行 `protoc 3.25.9` 生成的官方 Java 类，schema 位于 `src/test/proto/benchmark_cross_frame.proto`。
- `protobuf-codedstream` 使用 protobuf-java 的 `CodedInputStream` / `CodedOutputStream` 和等价手写 schema codec，不依赖 `protoc` 生成类。
- `flatbuffers-runtime` 使用 FlatBuffers Java runtime builder/table API 和等价手写访问器。
- `sbe-agrona` 使用 Agrona buffer 上的固定布局 SBE-style codec，不是官方 SBE generator 输出。
- `capnproto-runtime` 使用 Cap'n Proto Java runtime 和等价手写 struct factory。

## 2. 运行命令

基准 runner 不会被 `mvn test` 自动执行，需要显式运行：

```powershell
mvn "-DskipTests" `
  test-compile `
  "-Dzero.benchmark.profile=standard" `
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java `
  "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.zero.codegen.benchmark.CodegenBenchmarkLauncher"
```

快速验证可使用：

```powershell
mvn "-DskipTests" `
  test-compile `
  "-Dzero.benchmark.profile=quick" `
  "-Dzero.benchmark.operationsPerIteration=80" `
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java `
  "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.zero.codegen.benchmark.CodegenBenchmarkLauncher"
```

输出文件：

- `target/benchmark-results/benchmark-report.md`
- `target/benchmark-results/benchmark-results.csv`
- `target/benchmark-work/`：动态生成和编译的 benchmark 源码/类。

JDK 21 下 Agrona 需要访问 `jdk.internal.misc.Unsafe`。项目已在 `.mvn/jvm.config` 中内置所需 JVM module 参数，因此正常使用 Maven 命令即可运行。Protobuf 官方生成类依赖 `test-compile` 阶段生成，运行 benchmark 时不要省略该 lifecycle goal。

## 3. Profile

| profile | warmup | measurement | 默认每轮操作数 |
| --- | ---: | ---: | ---: |
| `quick` | 2 | 4 | 200 |
| `standard` | 3 | 6 | 1000 |
| `full` | 5 | 8 | 3000 |

可用参数覆盖：

```powershell
"-Dzero.benchmark.warmupIterations=5"
"-Dzero.benchmark.measurementIterations=8"
"-Dzero.benchmark.operationsPerIteration=3000"
"-Dzero.benchmark.resultDir=target/benchmark-results"
"-Dzero.benchmark.workDir=target/benchmark-work"
```

## 4. 指标含义

- `bytes/op`：该 codec 的编码后字节数。
- `ns/op mean`：单次操作平均耗时。
- `ns/op p50` / `p95`：按 measurement iteration 统计的中位和尾部耗时。
- `ops/s`：每秒操作数。
- `MiB/s`：按编码字节数估算的数据吞吐。
- `alloc B/op`：当前线程分配字节数，来自 `com.sun.management.ThreadMXBean`。

## 5. 本机标准基线

运行环境：

- Windows 11 amd64
- Java HotSpot 21.0.10
- profile：`standard`
- warmup：3
- measurement：6
- 每轮操作数：1000

完整双语报告见 `docs/CROSS_FRAMEWORK_PERFORMANCE_REPORT_2026-04-29.md`。

### 5.1 Protobuf / FlatBuffers / SBE / Cap'n Proto 横向尺寸

| dataset | Zero | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| `cross-frame-small` | 507 | 592 | 628 | 1008 | 686 | 864 |
| `cross-frame-medium` | 3684 | 4294 | 4581 | 6224 | 4833 | 5816 |
| `cross-frame-large` | 14844 | 17192 | 18343 | 24080 | 19043 | 22712 |

### 5.2 横向 encode/decode 摘要

单位为 `ns/op mean`：

| dataset | operation | fastest | Zero Cursor | Zero ByteBuf | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `cross-frame-small` | encode | SBE/Agrona | 2930.00 | 4133.53 | 8065.00 | 17394.48 | 11068.78 | 1943.08 | 11479.35 |
| `cross-frame-small` | decode | Zero Cursor | 2064.85 | 2632.03 | 7495.40 | 5373.23 | 4351.63 | 2221.47 | 5357.62 |
| `cross-frame-medium` | encode | Protobuf Generated | 8009.72 | 6161.88 | 5959.68 | 71855.72 | 10313.48 | 6572.48 | 9688.73 |
| `cross-frame-medium` | decode | SBE/Agrona | 3659.93 | 3073.10 | 10400.63 | 9643.95 | 4793.90 | 1819.67 | 4893.67 |
| `cross-frame-large` | encode | SBE/Agrona | 15272.23 | 19191.57 | 22971.90 | 281990.83 | 38425.60 | 4875.47 | 25368.92 |
| `cross-frame-large` | decode | SBE/Agrona | 9584.22 | 11793.38 | 35437.67 | 37774.43 | 21012.20 | 5041.55 | 13891.82 |

### 5.3 随机访问摘要

单位为 `ns/op mean`：

| dataset | Protobuf Generated randomRead | FlatBuffers randomRead | Cap'n Proto randomRead | Cap'n Proto randomWrite |
| --- | ---: | ---: | ---: | ---: |
| `cross-frame-small` | 3615.08 | 534.32 | 1726.57 | 4756.62 |
| `cross-frame-medium` | 8349.98 | 465.40 | 675.93 | 9173.90 |
| `cross-frame-large` | 31781.75 | 104.05 | 112.90 | 26275.08 |

### 5.4 Zero 内部模式摘要

| dataset | metric | result |
| --- | --- | ---: |
| fixed-telemetry | zero-cursor encode | 1082.30 ns/op |
| fixed-telemetry | zero-cursor decode | 373.42 ns/op |
| fixed-telemetry | zero-cursor readInto | 225.28 ns/op |
| packed-collections-128 | zero-cursor encode | 17499.02 ns/op |
| packed-collections-128 | zero-cursor decode | 11120.85 ns/op |
| packed-collections-128 | zero-bytebuf encode | 4644.32 ns/op |
| cross-frame-large | zero-cursor encode throughput | 926.93 MiB/s |
| cross-frame-large | zero-cursor decode throughput | 1477.05 MiB/s |
| cross-frame-large | zero-cursor readInto allocation | 104 B/op |

### 5.5 辅助通用框架摘要

JSON、Smile、Kryo、JDK serialization 仍保留在 `payload-sparse`、`payload-dense-32`、`payload-dense-256` 数据集中，用于观察通用序列化框架与 Zero 生成代码的趋势差异。详表见 `target/benchmark-results/benchmark-report.md`。

## 6. 读数注意事项

- 当前 harness 是项目内置轻量基准，不是 JMH；适合持续回归、横向趋势和快速定位，但严肃发布数据建议再接入 JMH 或 async-profiler。
- Protobuf 已包含官方生成类和 CodedStream 两条路径；FlatBuffers、Cap'n Proto 当前为 runtime API 加等价手写访问器；SBE 当前为 SBE-style 固定布局。若用于正式对外口径，建议继续追加 `flatc`、`capnp compile` 和官方 SBE generator 产物。
- Protobuf CodedStream 当前实现的 encode 会为嵌套 message 建立较多临时 byte array，因此分配量偏高。
- FlatBuffers 和 Cap'n Proto 的随机访问/零拷贝优势没有在本轮单独建模；当前 decode 均物化为等价 DTO。
- 横向框架使用等价 DTO，不包含业务层对象映射成本。
- 结果与 CPU、JVM 参数、后台负载和 warmup 长度强相关；提交报告时应附带 `benchmark-report.md` 的 Configuration 区块。
