# zero-codegen 用户指南 / User Guide

本文是一份面向使用者的双语指南，说明如何安装、编写 `.si` schema、运行 CLI/GUI、理解生成物、执行测试和运行性能基准。

This bilingual guide explains how to install the project, write `.si` schemas, run the CLI/GUI, understand generated outputs, run tests, and execute performance benchmarks.

## 1. 项目定位 / What This Project Does

`zero-codegen` 将 SI 协议 schema 编译为可直接集成到业务工程的协议代码。它适合需要紧凑二进制协议、Java/C# 双端代码生成、协议号管理和服务端分发器的项目。

`zero-codegen` compiles SI protocol schemas into protocol code that can be integrated into application projects. It is designed for compact binary protocols, Java/C# generation, protocol id management, and server-side dispatch helpers.

主要输出 / Main outputs:

- Java protocol classes with Cursor and Netty `ByteBuf` codecs.
- Java runtime helper sources under the target package.
- Java BO interfaces and dispatch manager.
- Java and C# `ProtoIds`.
- Optional C# protocol classes and runtime helper.

## 2. 环境准备 / Setup

要求 / Requirements:

- JDK 21
- Maven 3.9+

检查环境 / Check your environment:

```powershell
java -version
mvn -version
```

构建项目 / Build:

```powershell
mvn test
mvn package
```

构建成功后会生成 / After a successful build:

- `target/zero-codegen-0.1.0-SNAPSHOT.jar`
- `target/zero-codegen-0.1.0-SNAPSHOT-gui.jar`
- `target/dist/run-codegen-gui.bat`

## 3. 编写 SI Schema / Writing SI Schemas

一个 `.si` 文件可以包含 enum、struct、`client_to_server` 和 `server_to_client`。

A `.si` file may contain enums, structs, `client_to_server`, and `server_to_client` sections.

最小示例 / Minimal example:

```si
enum PlayerRole {
  UNKNOWN,
  WARRIOR,
  MAGE,
  SUPPORT
}

struct PlayerInfo {
  long uid;
  String name;
  int level;
  PlayerRole role;
}

struct Player {
  PlayerInfo base;
  List<Integer> itemIds;
  Map<String,Integer> counters;
}

client_to_server:
  queryPlayer(long uid, String traceId);

server_to_client:
  queryPlayerResult(Player player);
```

常用类型 / Common types:

- Scalars: `int`, `long`, `byte`, `short`, `boolean`, `char`, `float`, `double`, `String`.
- Containers: `T[]`, `List<T>`, `Set<T>`, `Map<K,V>`, `Optional<T>`.
- User types: custom `enum` and `struct`.

常用注解 / Common annotations:

- `@hot`: generate optimized normal protocol codec paths.
- `@fixed`: mark a fixed-layout struct.
- `@inline`: inline a fixed struct inside another fixed struct.
- `@packed`: packed encoding for supported numeric lists/maps.
- `@borrow`: borrowed array/string projection support for selected fields.

完整 schema 说明见 / Full schema reference: [SI_CODEGEN_GUIDE.md](SI_CODEGEN_GUIDE.md).

## 4. 使用 CLI / Using The CLI

基础 Java 生成 / Basic Java generation:

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --out "generated-src" `
  --pkg "com.zero.protocol" `
  --cleanGeneratedOutputs true
```

多个输入文件 / Multiple input files:

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si,src/main/resources/example/PresenceShowcase.si" `
  --out "generated-src" `
  --pkg "com.zero.protocol"
```

生成 Java + C# / Generate Java and C#:

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si,src/main/resources/example/PresenceShowcase.si" `
  --protoId "src/main/resources/example/protoId.txt" `
  --out "generated-src" `
  --pkg "com.zero.protocol" `
  --genCs true `
  --outCs "generated-cs" `
  --csNs "Zero.Protocol" `
  --cleanGeneratedOutputs true
```

常用参数 / Common options:

| 参数 / Option | 默认值 / Default | 说明 / Description |
| --- | --- | --- |
| `--input` | required | `.si` file list, comma-separated when multiple files are used |
| `--out` | `generated-src` | Java protocol output directory |
| `--pkg` | `com.zero.protocol` | Java protocol package |
| `--protoId` | empty | Optional protocol id mapping file |
| `--genJava` | `true` | Enable Java generation |
| `--genCs` | `false` | Enable C# generation |
| `--outCs` | `cs-src` | C# output directory |
| `--csNs` | empty | C# namespace |
| `--javaCommonOut` | same as `--out` | Java runtime/common output directory |
| `--csCommonOut` | same as `--outCs` | C# runtime/common output directory |
| `--cleanGeneratedOutputs` | `false` | Clean generated outputs before writing |

注意 / Note:

`--cleanGeneratedOutputs true` 会清理生成器管理的输出文件。请只把 `--out` / `--outCs` 指向明确的生成目录，避免误删业务手写代码。

`--cleanGeneratedOutputs true` cleans generator-managed output files. Point `--out` / `--outCs` to dedicated generated-source directories.

## 5. 使用 GUI / Using The GUI

打包后启动 GUI / Launch after packaging:

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT-gui.jar
```

GUI 支持 / The GUI supports:

- 添加单个 `.si` 文件或目录。
- 配置 Java 输出目录和 Java package。
- 配置 C# 输出目录和 namespace。
- 配置 `protoId.txt`。
- 先 Validate，再 Generate。
- 记忆上次使用的部分路径和参数。

## 6. 理解生成物 / Understanding Outputs

Java 生成物通常包括 / Java outputs usually include:

- One Java protocol class per `struct`.
- Java enum classes for SI enums.
- Runtime helper sources under `runtime.bytes`, `runtime.serialize`, `runtime.proto`, and `runtime.netty`.
- BO interfaces under the configured BO package.
- `ProtoIds` and `ProtoDispatchManager` when protocol methods are present.

生成类通常支持 / Generated classes usually support:

- `writeTo(ByteCursor)` and `readFrom(ByteCursor)`.
- `writeTo(ByteBuf)` and `readFrom(ByteBuf)`.
- `readInto(...)` for object reuse.
- `estimatedSize()` for buffer planning.

C# 生成物包括 / C# outputs include:

- C# DTO/protocol classes.
- `BufUtil.cs` runtime helper.
- C# `ProtoIds` when protocol ids are configured.

## 7. 协议号文件 / Protocol Id File

`protoId.txt` 用于给 `client_to_server` 和 `server_to_client` 方法分配稳定 id。示例文件位于：

`src/main/resources/example/protoId.txt`

`protoId.txt` assigns stable ids to protocol methods. See the example file above.

建议 / Recommendations:

- 将 `protoId.txt` 纳入版本控制。
- 不要复用已经发布过的协议号。
- 删除协议时保留历史 id 记录，避免未来误用。

## 8. 测试 / Testing

运行测试 / Run tests:

```powershell
mvn test
```

测试会验证 / Tests verify:

- schema parsing and generation details.
- generated Java sources compile successfully.
- ByteCursor and ByteBuf encoded bytes are consistent.
- round-trip decode works.
- `readInto` resets reused objects correctly.

打包验证 / Package verification:

```powershell
mvn package
```

## 9. 性能基准 / Performance Benchmarks

快速基准 / Quick benchmark:

```powershell
mvn "-DskipTests" test-compile `
  "-Dzero.benchmark.profile=quick" `
  "-Dzero.benchmark.operationsPerIteration=80" `
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java `
  "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.zero.codegen.benchmark.CodegenBenchmarkLauncher"
```

标准基准 / Standard benchmark:

```powershell
mvn "-DskipTests" test-compile `
  "-Dzero.benchmark.profile=standard" `
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java `
  "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.zero.codegen.benchmark.CodegenBenchmarkLauncher"
```

输出 / Outputs:

- `target/benchmark-results/benchmark-report.md`
- `target/benchmark-results/benchmark-results.csv`

当前 benchmark 覆盖 / Current benchmark coverage:

- Zero Cursor / ByteBuf / `readInto` / `estimatedSize`.
- Protobuf official generated Java classes.
- Protobuf CodedStream low-level codec.
- FlatBuffers runtime and randomRead.
- SBE-style Agrona fixed layout.
- Cap'n Proto runtime randomRead and randomWrite.
- Jackson JSON, Jackson Smile, Kryo, and JDK serialization auxiliary comparisons.

详细报告 / Detailed report: [CROSS_FRAMEWORK_PERFORMANCE_REPORT_2026-04-29.md](CROSS_FRAMEWORK_PERFORMANCE_REPORT_2026-04-29.md).

## 10. 清理 / Cleanup

PowerShell:

```powershell
Remove-Item -Recurse -Force target, generated-src, generated-cs, cs-src, temp -ErrorAction SilentlyContinue
```

只在项目根目录执行清理命令。不要对业务工程源码目录执行通配清理。

Run cleanup only at the repository root. Do not run wildcard cleanup against application source directories.

## 11. 集成建议 / Integration Recommendations

- 把生成代码输出到单独目录，例如 `generated-src` 或业务工程的 generated source root。
- 把 `.si` schema 和 `protoId.txt` 作为协议契约纳入版本控制。
- 对外发布协议前固定协议号，不要重排 enum 或复用旧 id。
- 在 CI 中至少运行 `mvn test`，重要 wire-format 变更时运行 benchmark。
- 将生成代码视为构建产物还是提交产物，由业务仓库策略决定；本项目默认忽略本地生成目录。

English:

- Use a dedicated generated source directory.
- Version control `.si` schemas and `protoId.txt`.
- Freeze protocol ids before release; do not reuse old ids.
- Run `mvn test` in CI, and run benchmarks for important wire-format changes.
- Decide per application repository whether generated code is committed or produced during build.
