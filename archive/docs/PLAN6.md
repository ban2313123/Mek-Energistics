# Mek-Energistics 重构状态与验收

> **状态：SUPERSEDED（已被当前实现取代）**
>
> 本文件早期的“final hardening / legacy cleanup”计划不再是执行计划。旧分支基线、旧阶段编号和已删除 helper 的保留/合并建议均不适用；当前实现和测试是唯一事实来源。

## 当前架构

- `MeMekanismMachine` 声明稳定的机器 ID、`CompatMachineFamily`、tier 和 machine type。
- `CompatMachineCatalog` 为每台机器生成唯一 `CompatMachineSpec`，集中保存 provider、来源方块 ID、ME 方块 ID、类别、要求和侧面配置 profile。
- 公共注册入口只遍历 `CompatMachineCatalog.available()`，通过 `CompatMachineProviders` 加载 provider；provider 再用 `CompatMachineFamilyAdapter` 绑定 BlockEntity、BlockType、Grid host 和菜单行为。
- 可选客户端 screen 由 `CompatMachineClientProviders` 负责；运行时模组/类探测集中在 `OptionalCompatClasses`。公共 common 入口不得直接链接 optional 实现。
- 普通机器使用 `MeRecipeMachineAeSupport`，工厂使用 `MeFactoryAeSupport`。机器实现 `MePatternIoOwner` 或 `MeFactoryIoOwner`，只声明 `MeInputLayout`、输出端口和机器特有 busy/energy 适配。
- `MeAeMachine` / `MeFactoryAeMachine` 的默认方法统一处理 pattern、节点、smart pattern、终端、优先级和输出模式；具体 BlockEntity 不再实现 recipe-specific `pushPattern` 或 AE key 解析。
- `CompatFactoryTierGraph` 是工厂升级、跨附属等级和反向索引的唯一图索引；不得重新建立字符串 tier 链或 path-only 索引。
- `MekEnergisticsDataGenerator` 以 catalog/profile 生成重复 blockstate、模型、配方和 loot；特殊模型和显示变换保留在 `src/main/resources`。

## 不再使用的路径

以下类和职责已经删除，禁止重新引入：

- `MeLegacyMachineAeHelper`、`MeMekanismMachinePatternInput`、`MeFactoryPatternInput`；
- `MeExternalFactorySupport`、`MeExtraFactoryBridge`、`MeAdvancedFactorySupport`；
- 具体机器按 Mekanism recipe 类型解析 `KeyCounter[]`、自行 simulate/execute/rollback；
- 公共注册类按附属模组类名或 `switch (spec.route())` 分派；
- 具体机器运行时隐式登记 output slot/tank；
- 直接在 provider/common 业务代码调用 `ModList.get()`；
- 为重复资源手工复制整组 tier JSON。

## 兼容边界

- 不改变现有 block ID、menu ID、注册名、NBT key、recipe ID 或旧世界格式。
- `AePatternSchema=2` 优先读取 `MePatternSlot*`；旧 `Inventory` pattern 槽只在缺少新槽位标签时迁移一次。
- `PatternPriority`、`PatternTerminalName`、`AeOutputMode`、smart-pattern pending 数据保持兼容。
- optional compat 只在 catalog requirement/family 可用时加载；客户端和 JEI 同样经过 client/provider family gate。

## 验收顺序

1. 执行 `runData`，确认第二次生成内容不变，且 main/generated 没有重复资源路径。
2. 执行 `compileJava`、`test`、`build`，重点覆盖 catalog/family/provider 边界、tier graph、输入事务、NBT/lifecycle 和 datagen 等测试。
3. 启动 `runClient`，分别抽测原生机器、原生工厂、MekMM、Mekanism Extras、Evolved Mekanism 和组合工厂。
4. 游戏内验证：放置、接入 AE、打开 GUI、安装样板、单次和批量下单、物品/chemical/fluid 输出、网络满后恢复、区块卸载/读档重连、拆除和升级。
5. 静态验收：具体 BlockEntity 无 AE key parsing/recipe-specific `pushPattern`；safe decode 之外无 `PatternDetailsHelper.decodePattern`；公共入口无 optional 实现引用；已删除 helper 无调用或文件残留。

后续若需扩展，只增加 catalog/family/provider/I/O adapter 的真实差异，并同步适配指南；不要恢复本文件中已标记为 superseded 的阶段计划。
