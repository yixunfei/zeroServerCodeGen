# 性能基线 2026-04-29

本报告由当前项目内置 benchmark runner 生成。完整原始结果见：

- `target/benchmark-results/benchmark-report.md`
- `target/benchmark-results/benchmark-results.csv`
- `docs/CROSS_FRAMEWORK_PERFORMANCE_REPORT_2026-04-29.md`

## 运行配置

- 日期：2026-04-29
- OS：Windows 11 amd64
- JVM：Java HotSpot 21.0.10
- profile：`standard`
- warmup iterations：3
- measurement iterations：6
- operations per iteration：1000
- Protobuf：`protoc 3.25.9` 生成 Java 类

## 关键结论

- Protobuf 已新增官方生成类路径 `protobuf-generated`，不再只依赖手写 CodedStream 参照。
- Zero 的编码尺寸仍是 cross-frame 三档数据中最小；官方 Protobuf 生成类约为 Zero 的 1.16 至 1.17 倍。
- SBE-style Agrona fixed layout 在 large encode/decode 上仍最快；medium encode 本轮由 `protobuf-generated` 略胜。
- FlatBuffers 和 Cap'n Proto 的 randomRead 已纳入测试，large randomRead 分别约 104 ns/op 与 113 ns/op，显著体现按需字段读取优势。
- Cap'n Proto randomWrite 已纳入测试，large randomWrite 约 26.28 us/op。

## Cross-Framework 编码尺寸

| dataset | Zero | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| cross-frame-small | 507 | 592 | 628 | 1008 | 686 | 864 |
| cross-frame-medium | 3684 | 4294 | 4581 | 6224 | 4833 | 5816 |
| cross-frame-large | 14844 | 17192 | 18343 | 24080 | 19043 | 22712 |

## Cross-Framework 完整 Encode/Decode

单位：`ns/op mean`。

| dataset | operation | Zero Cursor | Zero ByteBuf | Protobuf Generated | Protobuf CodedStream | FlatBuffers | SBE/Agrona | Cap'n Proto |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| cross-frame-small | encode | 2930.00 | 4133.53 | 8065.00 | 17394.48 | 11068.78 | 1943.08 | 11479.35 |
| cross-frame-small | decode | 2064.85 | 2632.03 | 7495.40 | 5373.23 | 4351.63 | 2221.47 | 5357.62 |
| cross-frame-medium | encode | 8009.72 | 6161.88 | 5959.68 | 71855.72 | 10313.48 | 6572.48 | 9688.73 |
| cross-frame-medium | decode | 3659.93 | 3073.10 | 10400.63 | 9643.95 | 4793.90 | 1819.67 | 4893.67 |
| cross-frame-large | encode | 15272.23 | 19191.57 | 22971.90 | 281990.83 | 38425.60 | 4875.47 | 25368.92 |
| cross-frame-large | decode | 9584.22 | 11793.38 | 35437.67 | 37774.43 | 21012.20 | 5041.55 | 13891.82 |

## 随机访问摘要

单位：`ns/op mean`。

| dataset | Protobuf Generated randomRead | FlatBuffers randomRead | Cap'n Proto randomRead | Cap'n Proto randomWrite |
| --- | ---: | ---: | ---: | ---: |
| cross-frame-small | 3615.08 | 534.32 | 1726.57 | 4756.62 |
| cross-frame-medium | 8349.98 | 465.40 | 675.93 | 9173.90 |
| cross-frame-large | 31781.75 | 104.05 | 112.90 | 26275.08 |

## Zero 模式摘要

| dataset | mode | ns/op mean | alloc B/op |
| --- | --- | ---: | ---: |
| fixed-telemetry | cursor encode | 1082.30 | 136 |
| fixed-telemetry | cursor decode | 373.42 | 104.66 |
| fixed-telemetry | cursor readInto | 225.28 | 24 |
| packed-collections-128 | cursor encode | 17499.02 | 4256.13 |
| packed-collections-128 | cursor decode | 11120.85 | 7488.03 |
| packed-collections-128 | cursor readInto | 10298.30 | 4456 |
| cross-frame-large | cursor encode | 15272.23 | 14936 |
| cross-frame-large | cursor decode | 9584.22 | 31016 |
| cross-frame-large | cursor readInto | 16469.50 | 104 |
