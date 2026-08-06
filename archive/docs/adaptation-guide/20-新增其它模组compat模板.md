# 20. 新增其它模组 compat 的模板

新增附属模组时，先在 `CompatMod`、`CompatMachineFamily` 和 `MeMekanismMachine` 声明稳定元数据，再让 catalog 生成 spec。不要把可选模组类散落到公共注册类，也不要新增每台机器一个 facade 方法。

推荐结构：

```text
compat/catalog/CompatMachineFamily.java
compat/provider/SomeModMachineProvider.java
compat/<modid>/...                 # 只引用目标模组
client/compat/provider/...          # optional screen
client/jei/compat/...               # optional JEI
```

服务端 provider 应继承 `AbstractCompatMachineProvider`，为每个可用 `CompatMachineFamily` 提供一个 `CompatMachineFamilyAdapter.of(...)`。adapter 绑定：

- `registerBlockEntity`；
- `createBlockType`；
- `registerGridNodeHost`；
- `menuType`。

provider 本身可实现真实的 `resolveOriginalMachine`、`resolveInstallerUpgrade`、installer 检测和 optional menu 注册，但这些逻辑必须按 family/availability gate 执行。

接入检查：

- `OptionalCompatClasses` 增加 mod-id/class-resource 能力探测；
- catalog requirement 覆盖真实依赖组合；
- `CompatMachineCatalogTest` 覆盖 source ID、family、tier/type 唯一性；
- client provider 和 `OptionalJeiCompat` 只在可用 family 后加载；
- `MeFactoryIoOwner`/`MePatternIoOwner` 只声明物理 I/O；
- `MekEnergisticsDataGenerator` 生成重复资源；
- 静态测试确认公共入口没有 optional import、ModList 或逐机枚举。
