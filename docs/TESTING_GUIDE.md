# 测试与验证说明

本文说明当前项目的测试层次和验证方式。

## 1. 运行测试

```powershell
mvn test
```

测试需要 JDK 21，因为 `StandaloneGenerationRoundTripTest` 会在测试进程内调用 JDK compiler 编译生成源码。

## 2. 测试覆盖

当前测试集中包含两类验证。

生成器结构验证：

- `CommonOutOptionTest`
- `DispatcherGenerationTest`
- `EstimatedSizeGenerationTest`
- `FieldAnnotationParseTest`
- `FieldMetadataGenerationTest`
- `FixedInlineGenerationTest`
- `ByteBufHotGenerationTest`
- `DominantMaskGenerationTest`
- `HotTailSkipGenerationTest`
- `PackedIntObjectMapGenerationTest`
- `CsOptimizationGenerationTest`

独立闭环验证：

- `StandaloneGenerationRoundTripTest`

闭环测试会执行：

1. 读取 `src/test/resources/comprehensive/ComprehensiveProtocol.si`。
2. 调用 `SiCompiler.compileBatch` 生成 Java 协议代码和 runtime。
3. 复制综合 fixture / driver 到生成源码目录。
4. 使用 `javax.tools.JavaCompiler` 编译生成源码。
5. 反射运行 `ComprehensiveCorrectnessDriver.runChecks()`。
6. 验证 ByteCursor / ByteBuf 编码字节一致、round-trip 正确、`readInto` 能正确重置内容。

这条链路用于保证项目从 `.si` 到生成代码编译运行是自洽的。

## 3. 打包验证

```powershell
mvn package
```

成功后生成：

- `target/zero-codegen-0.1.0-SNAPSHOT.jar`
- `target/zero-codegen-0.1.0-SNAPSHOT-gui.jar`
- `target/dist/run-codegen-gui.bat`

`mvn package` 会先运行测试。

## 4. 手工生成验证

```powershell
mvn package

java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --out "generated-src" `
  --pkg "com.zero.protocol" `
  --cleanGeneratedOutputs true
```

检查输出：

```powershell
Get-ChildItem -Recurse generated-src
```

`generated-src/` 是临时生成物，已被 `.gitignore` 忽略。

## 5. 清理构建产物

```powershell
Remove-Item -Recurse -Force target, generated-src, generated-cs, temp -ErrorAction SilentlyContinue
```

仅在当前项目根目录执行上面的命令。若将 `--out` 指向其他工程源码目录，不要用通配清理。

## 6. 新增测试建议

新增生成器能力时，至少补一类测试：

- 字符串级生成断言：适合保护代码形状、import、方法签名。
- 编译级闭环：适合保护生成代码是否可编译。
- 运行级 round-trip：适合保护 wire 兼容性和默认值行为。

涉及 runtime 或 wire 改动时，应优先增加运行级 round-trip。
