# 22. compat 分派检查单

新增一个模组 compat 后，逐项检查：

- `CompatMachineFamily`、catalog spec、sourceBlockId、tier/type 和 requirement 正确；
- 未安装目标模组时 `CompatMachineCatalog.available()` 不包含对应 spec；
- 公共静态初始化不加载目标模组类，运行时 `ModList` 只出现在 `OptionalCompatClasses`；
- 每个 catalog family 在对应 server provider 中有 `CompatMachineFamilyAdapter`；
- `ModBlocks`、`ModBlockEntities`、`ModBlockTypes` 只遍历 `available()`；
- provider 能为每个 spec 选择正确 BlockEntity、BlockType、Grid host 和 menu；
- `MePatternIoOwner`/`MeFactoryIoOwner` 声明完整输入布局和输出端口，具体机器不解析 AE key；
- client provider、JEI compat 和 optional mixin 都有独立 gate；
- installer 先按完整 sourceBlockId，再使用 provider BlockState resolver；
- `CompatFactoryTierGraph` 覆盖 base/basic/next/跨附属等级；
- `runData` 生成所有 `hasMeVariant` catalog 资源，recipe 条件覆盖 optional requirement；
- 静态 boundary test、`compileJava`、`test`、`build` 和客户端抽测均通过。
