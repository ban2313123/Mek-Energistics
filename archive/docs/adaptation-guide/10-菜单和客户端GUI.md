# 10. 菜单和客户端 GUI

`ModMenuTypes` 注册公共 menu 定义，但机器到 menu 的选择统一走：

```text
CompatMachineCatalog.get(machine)
-> CompatMachineProviders.get(spec.provider())
-> family adapter menuType(spec)
```

可选模组 provider 在 `registerMenus(...)` 中按可用 family 注册菜单。公共 `ModMenuTypes` 不直接引用 optional BlockEntity/container。

客户端 screen 由 `CompatMachineClientProviders.available()` 加载对应 client provider。第三方 GUI 或 BlockEntity class literal 只能出现在该附属的 client provider/compat package 中。

新增 GUI 时检查样板按钮、AE 输出开关、tracker、JEI exclusion area 和玩家背包偏移。只有确实需要新 container/screen 时才新增定义；同 family 的机器优先复用 provider 已绑定的 menu。
