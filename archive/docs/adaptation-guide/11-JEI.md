# 11. JEI

主入口是 `client/jei/MekEnergisticsJeiPlugin.java`。Mek 原生 recipe type 可在主插件注册 catalyst；optional recipe viewer 类型必须留在独立 JEI compat 类。

主插件只调用 `OptionalJeiCompat.registerCatalysts(...)`。loader 同时检查 `OptionalCompatClasses` 和 `CompatMachineCatalog.hasAvailableFamily(...)`，再延迟加载对应 JEI compat；主插件不得直接 import optional 实现或调用 `ModList`。

工厂变体通常由 catalog 机器集合生成隐藏列表，只保留代表性基础 ME 机器作为 catalyst。新增 family 后验证：目标 family 不可用时不会加载 compat，可用时 catalyst 完整，且隐藏逻辑不会移除唯一入口。
