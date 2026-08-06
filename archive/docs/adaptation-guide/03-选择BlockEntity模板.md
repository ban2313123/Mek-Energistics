# 3. 选择 BlockEntity 模板

公共注册入口由 `CompatMachineCatalog.available()` 驱动：

```text
CompatMachineCatalog
-> CompatMachineProviders
-> CompatMachineFamilyAdapter
-> BlockEntity / BlockType / Grid host / menu
```

新增机器时不在 `ModBlockEntities` 或 `ModBlockTypes` 中添加附属模组分支。将第三方类型和构造器放进对应 provider/family adapter，并先由 catalog requirement 过滤不可用机器。

普通机器根据真实 Mek tile 选择最小 BlockEntity 模板；有 AE 能力的机器实现 `MeAeMachine`，无明确样板输入输出语义的工具机器不加入 crafting provider。普通机器通常使用 `MeRecipeMachineAeSupport`。

工厂机器实现 `MeFactoryAeMachine`，若需要声明物理槽位/储罐则实现 `MeFactoryIoOwner`，统一使用 `MeFactoryAeSupport`。Mek 原版、MekMM、Mekanism Extras、Evolved Mekanism 和组合工厂都遵循这一层。

推荐参考：

- 单 item：`MeElectricMachineBlockEntity`；
- item + chemical：`MeMetallurgicInfuserBlockEntity` 或 `MeAdvancedElectricMachineBlockEntity`；
- chemical + chemical：`MeChemicalInfuserBlockEntity`；
- 多输出/tank：Washer、PRC、Electrolytic Separator；
- 工厂 item -> item：`MeItemStackToItemStackFactoryBlockEntity`；
- 工厂 item + chemical -> item：`MeItemStackChemicalToItemStackFactoryBlockEntity`。

具体 BlockEntity 只保留上游 recipe/cache、能量和必要生命周期覆写；AE pattern、输入事务、输出 ticker、NBT 和终端委托给共同 support。
