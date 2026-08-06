# 9. BlockType、Side Config 和默认物品配置

公共 `ModBlockTypes` 只遍历 `CompatMachineCatalog.available()` 并调用对应 `CompatMachineProvider`。BlockType 的第三方类、shape、tier、attribute 和升级支持放在 provider/family adapter 内，不在公共 registry 添加附属分支。

`CompatMachineSpec.sideConfigProfile()` 是默认物品配置的集中事实，`MeBlockDeferredRegister` 用它选择 Mekanism 的 attached side config。新增机器时确认 profile 能覆盖：

- 普通 item/energy；
- advanced/extra input；
- chemical/fluid 输出；
- reaction、washer、rotary、separator 等专用结构。

provider 的 BlockType builder 仍要声明真实 `TransmissionType`、GUI、能量配置、`AttributeFactoryType`、`AttributeTier`、shape 和 upgrade target。不要在 `ModBlockTypes`、物品默认配置和 provider 中分别维护三套机器字符串判断。

新增 profile 或 provider family 后补 catalog/provider boundary 测试，并进游戏验证新放置机器的默认输入输出方向。
