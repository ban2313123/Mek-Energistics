# 15. 新增 Mek 机器的推荐步骤

1. 在 `MeMekanismMachine` 声明稳定 base name、machine type、tier 和 `CompatMachineFamily`。
2. 确认 `CompatMachineCatalog` 生成的 `sourceBlockId`、ME ID、requirement 和 side-config profile 正确。
3. 选择最小上游 BlockEntity 模板；只有访问 Mek 私有字段时才新增 mixin accessor，并配置 optional gate。
4. 为普通机器实现 `MeAeMachine` + `MePatternIoOwner`，为工厂实现 `MeFactoryAeMachine` + `MeFactoryIoOwner`。
5. 声明 `MeInputLayout`、输出端口、模式/催化剂约束；不要实现 recipe-specific `pushPattern` 或 AE key parsing。
6. 在对应 provider/family adapter 绑定 BlockEntity、BlockType、Grid host 和 menu；不要修改公共 registry 的附属分支。
7. 需要 optional screen/JEI 时只在 client provider/JEI compat 中增加实现。
8. 确认 `CompatFactoryTierGraph` 和 installer provider 能识别升级目标。
9. 运行 `runData`，让 catalog/profile 生成重复 blockstate、model、recipe、loot；特殊模型才保留手写资源。
10. 运行 `compileJava`、`test`、`build`，再启动客户端验证样板下单、加工、输出回网、重载和升级。
