# SI schema 与代码生成指南

本文描述当前 `zero-codegen` 支持的 `.si` schema、生成物和命令行参数。内容面向当前独立项目，不引用历史 benchmark 报告。

## 1. schema 文件结构

一个 `.si` 文件可以包含以下顶层块：

- `enum`
- `struct`
- `client_to_server:`
- `server_to_client:`

最小示例：

```si
enum PresenceMode {
  UNKNOWN,
  ONLINE,
  BUSY,
  AWAY
}

struct FriendBrief {
  long uid;
  String nick;
}

@hot
struct PresencePayload {
  long uid;
  PresenceMode mode;
  String nick;
  List<Integer> achievements;
  Optional<String> title;
  Map<Integer,FriendBrief> friends;
}

client_to_server:
  updatePresence(PresencePayload payload, int seq);

server_to_client:
  updatePresenceAck(int seq, boolean ok, String reason);
```

## 2. 注释与预处理

支持：

- 行内 `// comment`
- 以 `#` 开头的整行注释
- 空行
- 全角冒号会被归一化为普通冒号

当前不支持块注释、`import/include`、namespace 声明和宏系统。

## 3. 类型

基础类型：

- `int`
- `long`
- `byte`
- `short`
- `boolean`
- `char`
- `float`
- `double`
- `String` 或 `string`

复合类型：

- 自定义 `struct`
- 自定义 `enum`
- `Optional<T>`
- `T[]`
- `List<T>` / `ArrayList<T>` / `LinkedList<T>` / `Collection<T>`
- `Set<T>` / `HashSet<T>` / `LinkedHashSet<T>`
- `Queue<T>` / `Deque<T>`
- `Map<K,V>` / `HashMap<K,V>` / `LinkedHashMap<K,V>`

集合可以嵌套，也可以作为数组元素，例如：

```si
struct CollectionArrayPayload {
  List<Integer>[] shards;
  Set<String>[] tagBuckets;
  Map<Integer,RoomBucket>[] roomBuckets;
}
```

## 4. 注解

### 4.1 struct 注解

`@hot`

为热点结构生成更展开的容器读写路径，减少部分通用 helper 和 lambda 分发。它不改变 wire 语义。

`@fixed`

尝试生成固定布局 codec。结构内字段必须是 fixed-compatible 类型，否则会回退为普通 codec 并产生 warning。

`@inline`

结构作为嵌套字段时，生成器会尽量内联读写逻辑，减少嵌套方法调用。

### 4.2 field 注解

`@borrow`

用于减少读取阶段的物化复制。支持：

- `@borrow byte[]`
- `@borrow String`
- `@borrow int[]`
- `@borrow long[]`
- `@borrow float[]`
- `@borrow double[]`

Java 侧会映射到 `BorrowedBytes`、`BorrowedString` 或 primitive array view。默认是安全复制语义；如需 zero-copy view，运行时必须显式调用 `ByteIO.setUnsafeBorrowEnabled(true)`。

`@fixed(n)`

用于固定长度字段。支持：

- `@fixed(n) String`
- `@fixed(n) byte[]`
- `@fixed(n) int[]`
- `@fixed(n) long[]`
- `@fixed(n) float[]`
- `@fixed(n) double[]`

`@packed`

用于更紧凑的 fixed-width 容器编码。常见场景：

- `@packed List<Integer>`
- `@packed List<Long>`
- `@packed Map<Integer,Integer>`
- `@packed Map<Integer,Long>`
- `@packed Map<Integer,Struct>`

`@packed` 会改变 wire 编码，通信两端必须使用同一个 schema。

## 5. wire 语义概览

当前协议是顺序布局，不是 tagged field：

- 字段按 schema 声明顺序读写。
- 调整字段顺序通常属于不兼容协议变更。
- 普通 struct 会先写 presence bits，再写本次存在的字段内容。
- `@fixed` struct 不写 presence bits，按固定布局顺序读写。

presence-tracked 字段在默认值时可被省略。常见默认值：

- 数字：`0`
- `boolean`：`false`
- `char`：`'\0'`
- `String`：空字符串
- 数组和集合：空容器
- `Optional<T>`：`Optional.empty()`
- `enum`：ordinal `0`

如果业务必须区分“未传”和“传了默认值”，应显式建模，例如使用 `Optional<T>` 或额外状态字段。

## 6. 生成物

Java 输出通常包括：

- `<Struct>.java`
- `<Enum>.java`
- `ProtoIds.java`
- `bo/I<SchemaBase>BO.java`
- `bo/ProtoDispatchManager.java`
- 可选 `bo/impl/<SchemaBase>BOImp.java`
- 可选 `bo/config/GeneratedProtoAutoConfig.java`
- `<pkg>.runtime.*` Java runtime 源码

C# 输出通常包括：

- `<Struct>.cs`
- `<Enum>.cs`
- `ProtoIds.cs`
- `BufUtil.cs`

Java runtime 会生成到 `<pkg>.runtime` 包下。它是当前项目的模板复制结果，不依赖外部 `zero-common` 或 `zero-protocol`。

## 7. protoId 文件

`--protoId` 指向协议号配置文件。格式：

```text
Player 1000 2000
PresenceShowcase 13000 14000
```

每行含义：

- 第一列：schema 基名
- 第二列：client-to-server 起始 ID
- 第三列：server-to-client 起始 ID

如果没有配置，生成器会使用默认区间并输出 warning。生产协议建议显式配置，避免 ID 冲突。

## 8. 命令行参数

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| `--input` | 输入 `.si` 文件列表，逗号分隔 | 必填 |
| `--out` | Java 协议输出目录 | `generated-src` |
| `--pkg` | Java 协议包名 | `com.zero.protocol` |
| `--protoId` | 协议号配置文件 | 空 |
| `--genJava` | 是否生成 Java | `true` |
| `--genCs` | 是否生成 C# | `false` |
| `--outCs` | C# 输出目录 | `cs-src` |
| `--javaCommonOut` | Java runtime 输出根目录 | 同 `--out` |
| `--csCommonOut` | C# runtime 输出根目录 | 同 `--outCs` |
| `--boOut` | Java BO 输出根目录 | 同 `--out` |
| `--boPkg` | Java BO 包名 | `<pkg>.bo` |
| `--csNs` | C# namespace | 同 `--pkg` |
| `--genBoImpl` | 是否生成 BO stub 实现 | `false` |
| `--implWithComponent` | BO stub 是否带 Spring `@Component` | `false` |
| `--genAutoConfig` | 是否生成 Spring auto config | `false` |
| `--scanImplPackage` | auto config 是否扫描 `boPkg.impl` | `false` |
| `--scanImpl` | `--scanImplPackage` 的兼容别名 | `false` |
| `--simd` | 是否生成 SIMD 相关路径 | `false` |
| `--cleanGeneratedOutputs` | 生成前清理目标生成目录中的旧 `.java` / `.cs` | `false` |

## 9. 常用命令

生成 Java：

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --out "generated-src" `
  --pkg "com.zero.protocol" `
  --cleanGeneratedOutputs true
```

生成 Java runtime 到独立目录：

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --out "generated-src/protocol" `
  --javaCommonOut "generated-src/runtime" `
  --pkg "com.zero.protocol"
```

生成 BO 实现和 auto config：

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --out "generated-src" `
  --pkg "com.zero.protocol" `
  --boOut "generated-src" `
  --boPkg "com.zero.protocol.bo" `
  --genBoImpl true `
  --genAutoConfig true `
  --scanImplPackage true
```

生成 C#：

```powershell
java -jar target/zero-codegen-0.1.0-SNAPSHOT.jar `
  --input "src/main/resources/example/Player.si" `
  --genCs true `
  --outCs "generated-cs" `
  --csNs "Zero.Protocol"
```

## 10. 兼容性注意事项

- Java / C# 双端必须使用同一份 `.si` schema。
- 字段顺序、注解和类型变化可能改变 wire。
- `HashMap` / `HashSet` 迭代顺序不稳定。需要稳定字节输出时，优先使用 `LinkedHashMap`、`LinkedHashSet` 或稳定排序的列表。
- 建议生产输出目录保持独立，使用 `--cleanGeneratedOutputs true` 清理旧生成物。
- 不要把 `target/`、`generated-src/`、`generated-cs/` 当作源码事实来源。
