# zero-codegen

独立的 SI 协议代码生成器，用于从 `.si` schema 生成 Java / C# 协议代码、Java 运行时模板、协议号常量、BO 接口和协议分发器。当前项目已从原 `zeroServer` 工程中抽离，能够独立构建、测试、运行和做编解码性能基准。

English: `zero-codegen` is a standalone SI protocol code generator. It generates Java / C# protocol classes, Java runtime sources, protocol id constants, BO interfaces, and dispatch helpers from `.si` schema files. This repository is self-contained and no longer depends on the original `zeroServer` parent project.

## 核心能力 / Highlights

- Java 协议类生成：支持 Cursor 与 Netty `ByteBuf` 编解码路径。
- C# 协议类生成：可选生成 C# DTO 和 runtime helper。
- 自带 Java runtime 模板：生成物不依赖外部 `zero-protocol` 模块。
- 协议号生成：可从 `protoId.txt` 生成 Java / C# `ProtoIds`。
- 分发器生成：生成 `ProtoDispatchManager` 和 BO 接口。
- 独立闭环测试：测试会动态生成源码、编译并执行 round-trip。
- 性能基准：内置 Zero、Protobuf 官方生成类、FlatBuffers、SBE-style Agrona、Cap'n Proto 等横向 benchmark。

English highlights:

- Generates Java protocol classes with Cursor and Netty `ByteBuf` codecs.
- Optionally generates C# protocol classes and runtime helpers.
- Emits Java runtime sources into the target package, avoiding external runtime modules.
- Generates protocol ids, dispatch managers, and BO interfaces.
- Includes self-contained generation, compilation, and codec round-trip tests.
- Includes cross-framework codec benchmarks against Protobuf generated classes, FlatBuffers, SBE-style Agrona, and Cap'n Proto.

## 项目结构 / Project Map

```text
zero-codegen/
  pom.xml
  README.md
  docs/
    USER_GUIDE.md
    PROJECT_GUIDE.md
    SI_CODEGEN_GUIDE.md
    TESTING_GUIDE.md
    PERFORMANCE_BENCHMARK_GUIDE.md
    CROSS_FRAMEWORK_PERFORMANCE_REPORT_2026-04-29.md
  src/
    main/
      java/com/zero/codegen/
        SiCompiler.java
        SiGui.java
        JavaRuntimeSupport.java
        CSharpRuntimeSupport.java
      resources/
        example/
        runtime/java/
      scripts/
    test/
      java/com/zero/codegen/
      proto/
      resources/
```

关键文件 / Key files:

- `src/main/java/com/zero/codegen/SiCompiler.java`：CLI 入口、schema 解析和 Java/C# 代码生成核心。
- `src/main/java/com/zero/codegen/SiGui.java`：Swing GUI 入口。
- `src/main/resources/runtime/java`：生成 Java 协议代码所需的 runtime 模板。
- `src/main/resources/example`：可直接试跑的 `.si` 示例。
- `src/test/resources/benchmark`：性能基准 schema 与 benchmark driver。
- `src/test/proto/benchmark_cross_frame.proto`：Protobuf 官方生成类 benchmark schema。

## 环境要求 / Requirements

- JDK 21
- Maven 3.9+
- Windows PowerShell 示例命令可直接运行；Linux/macOS 可替换路径分隔符。

English:

- JDK 21
- Maven 3.9+
- Commands below use Windows PowerShell syntax; adapt paths for Linux/macOS.

## 快速开始 / Quick Start

构建与测试 / Build and test:

```powershell
mvn test
mvn package
```

生成 Java 协议代码 / Generate Java protocol code:

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --out "generated-src" `
  --pkg "com.zero.protocol" `
  --cleanGeneratedOutputs true
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

启动 GUI / Launch GUI:

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT-gui.jar
```

## 性能基准 / Benchmarks

运行标准横向 benchmark / Run the standard cross-framework benchmark:

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

## 文档 / Documentation

- [用户指南 / User Guide](docs/USER_GUIDE.md)
- [项目结构与独立构建](docs/PROJECT_GUIDE.md)
- [SI schema 与生成器说明](docs/SI_CODEGEN_GUIDE.md)
- [测试与验证说明](docs/TESTING_GUIDE.md)
- [编解码性能基准说明](docs/PERFORMANCE_BENCHMARK_GUIDE.md)
- [横向性能评测报告 / Cross-Framework Performance Report](docs/CROSS_FRAMEWORK_PERFORMANCE_REPORT_2026-04-29.md)

## 当前状态 / Current Status

- `pom.xml` 不再继承外部父 POM。
- 生成的 Java runtime 不再引用 `com.zero.common`。
- 测试会在临时目录中生成协议代码、编译生成源码，并执行 ByteCursor / ByteBuf 编解码 round-trip。
- benchmark 已包含 Protobuf 官方生成类、FlatBuffers randomRead、Cap'n Proto randomRead/randomWrite 等真实参照场景。
- 构建产物、临时生成物和 IDE 本地配置由 `.gitignore` 排除。

English:

- The Maven project is independent and does not inherit an external parent POM.
- Generated Java runtime sources do not reference `com.zero.common`.
- Tests generate protocol sources, compile them, and run ByteCursor / ByteBuf round-trip checks.
- Benchmarks include Protobuf generated classes, FlatBuffers random reads, and Cap'n Proto random read/write scenarios.
- Build outputs, generated scratch directories, and local IDE files are ignored.
