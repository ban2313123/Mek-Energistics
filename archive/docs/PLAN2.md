# Mek-Energistics 后续重构计划 v2

## Summary

当前重构列车已经完成 AE support API、recipe 默认委托、UI/Menu helper、AE-backed energy container、构造顺序崩溃修复、recipe AE lifecycle helper 等低风险收敛。下一步不继续扩大 UI/Menu 或 registry 改动，也不抽 `MeAeSupportBase`，优先做运行时硬化和旧通用机器的小步清理。

总体原则：不改方块 ID、菜单 ID、注册名、NBT tag、recipe JSON、blockstate JSON、世界兼容格式；optional compat 边界保持原类和加载条件。

## Key Changes

- **Phase 1: Pattern decode hardening**
  - 新建分支 `refactor/pattern-decode-hardening`，基线为 `a72685a Refactor recipe AE lifecycle helpers`。
  - 在 support 边界新增内部 helper，例如 `MePatternDecodeHelper.safeDecode(...)`，统一包装 `PatternDetailsHelper.decodePattern(...)`。
  - helper 捕获第三方坏 pattern / 缺失 decoder / 异常 NBT 导致的 decode 崩溃，记录机器位置与物品信息后跳过该 pattern。
  - 替换 `MeRecipeMachineAeSupport`、`MeFactoryAeSupport`、`MeMekanismMachineBlockEntity` 中的 pattern decode 调用；客户端 overlay 和 memory card 只在确认不会吞掉用户可见错误后再接入。
  - 不删除 pattern slot，不改 pattern NBT，不改 terminal 显示语义；坏 pattern 只是不进入 provider 列表。

- **Phase 2: Legacy generic machine cleanup**
  - 新建分支 `refactor/legacy-machine-cleanup`，从 hardening 通过后的提交创建。
  - 暂不拆 `MeMekanismMachineBlockEntity`，只提取窄 helper：pattern decode/update、AE node save/load、smart multiplication tick 唤醒等与现有 support 已一致的逻辑。
  - 保留 `MeMekanismMachineBlockEntity` 类名、block entity type、slot index、energy/container 行为和所有配置数据格式。
  - 不把旧通用机器强行迁移到 `MeRecipeMachineAeSupport`，除非前一小步验证证明没有构造顺序和 superclass 初始化风险。

- **Phase 3: Factory compat lifecycle bridge cleanup**
  - 新建分支 `refactor/factory-compat-helpers`。
  - 只清理 `MeExternalFactorySupport` / `MeExtraFactoryBridge` 附近的重复委托，保持 compat MenuTypes、ClientScreens、注册入口和 optional mod 条件不变。
  - 不合并 EME / MEKE / MekMM 的注册类，不折叠 compat package。
  - 目标是减少重复 lifecycle/save-load/terminal bridge 代码，不改变 factory `AeOutputMode`、terminal pattern inventory 或 output insertion 行为。

- **Phase 4: UI widget cleanup, only after backend stabilizes**
  - 只处理仍然完全重复的 screen widget 组装，例如标准 energy/progress widget helper。
  - 保留 factory、chemical、特殊布局 screen 的自定义 `drawForegroundText` 和偏移。
  - 不再改 `ModMenuTypes#getMachineContainer()`，除非出现新的重复或编译风险；当前 map 清理已完成。

- **Deferred**
  - 暂不抽 `MeAeSupportBase`。
  - 暂不 Builder 化 `MeMekanismMachine`。
  - 暂不完整拆分 `MeMekanismMachineBlockEntity`。
  - 暂不合并 compat MenuTypes / ClientScreens。
  - 暂不处理 `ClientSetup` event bus deprecation。

## Public/Internal API Impact

- 新增 internal-only pattern decode helper，建议放在 `blockentity.support` 边界内。
- helper 返回 `@Nullable IPatternDetails` 或等价安全结果；调用点遇到 `null` 时跳过 pattern。
- 不新增对外配置项，不改变玩家可见命令、菜单、NBT、注册名或数据包格式。
- 日志使用现有 mod logger；避免每 tick 刷屏，同一个 slot 的坏 pattern 应尽量只在 updatePatterns 触发时记录。

## Test Plan

- 每个 phase 后运行：
  ```powershell
  $env:JAVA_HOME='C:\GRAALVM\graalvm-jdk-21.0.6+8.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat compileJava --no-daemon --no-problems-report --no-configuration-cache
  ```

- Phase 1 和最终完成后运行：
  ```powershell
  $env:JAVA_HOME='C:\GRAALVM\graalvm-jdk-21.0.6+8.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat build --no-daemon --no-problems-report --no-configuration-cache
  $env:JAVA_HOME='C:\GRAALVM\graalvm-jdk-21.0.6+8.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat runClient --no-daemon --no-problems-report --no-configuration-cache
  ```

- 静态验收：
  - `rg -n "PatternDetailsHelper\.decodePattern" src\main\java\com\beipuo\mekenergistics\blockentity` 只剩 safe decode helper 内部或明确例外。
  - `git diff --stat` 不扩散到 registry、JSON、菜单 ID、方块 ID、枚举构造。
  - 搜索确认 `PatternPriority`、`PatternTerminalName`、`AeOutputMode`、pattern slot tag 名不变。

- 游戏内抽测：
  - 普通 ME 机器、化学机器、factory、MekMM/EME 普通机器能正常打开 GUI 并显示 pattern terminal。
  - 插入有效 encoded pattern 后 provider 刷新，pattern priority、custom terminal name、smart multiplication 保存读档正常。
  - 插入异常或缺 decoder 的 encoded pattern 不再导致启动或打开世界崩溃；有效 pattern 仍照常工作。
  - AE 能量不足暂停，恢复后继续；输出网络满时产物留在机器输出槽，网络可插入后继续导出。
  - 旧世界加载不丢 pattern slot、AE output mode、custom terminal name 或 pending smart pattern 状态。

## Assumptions

- 当前 `refactor/ae-lifecycle-helpers` 是新的基线，未跟踪的 `extendedae_plus.mixins.json` 和 `libs/` 继续保留本地未提交。
- 下一步目标是稳定性优先，其次才是减少重复代码。
- 旧 `docs/plan.md` 和 `docs/PLAN-Codex.md` 中已经完成或过于激进的阶段不再作为下一步依据。
- 所有大结构重构必须在 pattern decode hardening 和 runtime 验证通过后重新评估。
