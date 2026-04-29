# zero-codegen 项目指南

本文说明当前独立项目的结构、构建方式和清理边界。这里的内容以当前仓库为准，不再引用原 `D:\workspace\JavaSpace\zeroServer` 中的父工程或历史报告。

## 1. 项目定位

`zero-codegen` 是 SI 协议生成器，核心输入是 `.si` schema，核心输出包括：

- Java 协议类
- Java runtime 源码模板
- Java BO 接口
- Java `ProtoIds`
- Java `ProtoDispatchManager`
- 可选 Java BO stub / Spring auto config
- C# 协议类
- C# runtime helper `BufUtil.cs`
- C# `ProtoIds`

当前项目可独立运行：

- Maven 构建不依赖外部 parent POM。
- 生成代码所需的 Java runtime 模板位于本项目 `src/main/resources/runtime/java`。
- 测试不依赖外部 `zero-protocol` 源码。

## 2. 目录结构

```text
zero-codegen/
  pom.xml
  README.md
  docs/
    PROJECT_GUIDE.md
    SI_CODEGEN_GUIDE.md
    TESTING_GUIDE.md
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
      resources/
```

关键文件：

- `SiCompiler.java`：命令行入口、schema 解析和 Java/C# 代码生成逻辑。
- `SiGui.java`：桌面 GUI 入口。
- `JavaRuntimeSupport.java`：把 `runtime/java` 模板复制并重写到目标包。
- `CSharpRuntimeSupport.java`：生成 C# runtime helper。
- `src/main/resources/example`：可用于手工试跑的 schema。
- `src/test/java/com/zero/codegen/StandaloneGenerationRoundTripTest.java`：独立闭环测试。

当前保留的示例 schema：

- `Player.si`：最小可运行协议示例。
- `PresenceShowcase.si`：presence bits、默认值省略、`Optional` 和容器混合。
- `BorrowArrayProjection.si`：primitive array borrow view。
- `InventoryShowcase.si`：嵌套结构、`Set`、`Map`、`Optional`。
- `CollectionArrayShowcase.si`：集合数组。
- `protoId.txt`：上述示例的协议号范围。

## 3. 环境要求

- JDK 21
- Maven 3.9+
- Windows PowerShell 可直接使用本文命令。Linux/macOS 只需替换路径分隔符。

检查环境：

```powershell
java -version
mvn -version
```

## 4. 构建

运行测试：

```powershell
mvn test
```

打包：

```powershell
mvn package
```

主要产物：

- `target/zero-codegen-0.1.0-SNAPSHOT.jar`
- `target/zero-codegen-0.1.0-SNAPSHOT-gui.jar`
- `target/dist/run-codegen-gui.bat`

`target/` 是构建产物，不应提交。

## 5. 命令行使用

最小 Java 生成：

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --out "generated-src" `
  --pkg "com.zero.protocol"
```

批量生成：

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si,src/main/resources/example/PresenceShowcase.si" `
  --protoId "src/main/resources/example/protoId.txt" `
  --out "generated-src" `
  --pkg "com.zero.protocol" `
  --boOut "generated-src" `
  --boPkg "com.zero.protocol.bo" `
  --cleanGeneratedOutputs true
```

Java + C#：

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

## 6. 生成输出建议

建议把生成输出放在可清理目录中，例如：

- `generated-src/`
- `generated-cs/`
- `temp/`

这些目录已写入 `.gitignore`。如果要把生成代码作为另一个项目的源码依赖，可以把 `--out` 指向目标项目的源码目录，但应配合 `--cleanGeneratedOutputs true` 使用独立输出目录，避免误删非生成代码。

## 7. 清理边界

本轮清理移除了以下类型内容：

- Maven 构建产物：`target/`
- 历史生成物：`generated-src/`、`generated-cs/`
- 旧提取残留：`zero-protocol/`、`temp/`
- IDE 本地配置：`.idea/`、`.settings/`、`.classpath`、`.project`
- 过时报告和乱码说明：根目录历史报告 Markdown、旧 performance guide
- 历史测试/压测数据：跨框架 benchmark、旧 SIMD 试验 schema、无效的旧 `Player2`/`equipment` 示例

保留内容：

- `src/main/java`
- `src/main/resources`
- `src/test/java`
- `src/test/resources`
- `pom.xml`
- `README.md`
- `docs/`

## 8. 后续整理建议

下一阶段可以继续做：

- 把 `SiCompiler.java` 中解析、模型、Java 生成、C# 生成拆成独立包。
- 为 generated Java runtime 建立更小的模板模块边界。
- 增加 CLI help 输出和错误码约定。
- 把 C# 编译/运行验证纳入独立测试链路。
