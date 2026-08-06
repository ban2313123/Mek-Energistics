### English

#### Change

- Added counted batch-dispatch compatibility for AE2 Lightning Tech and Thunderbolt Core. With smart pattern multiplication enabled, Thunderbolt crafting CPUs can now send ME machines and factories as many complete crafts as their current physical input capacity accepts in one atomic transfer. Disabling smart multiplication keeps the optimized ordinary single-craft path, while direct batch routing prevents duplicate multiplication and returns every unaccepted craft to the CPU.
- Added AE2 Lightning Tech 2.0.4 and Thunderbolt Core 1.0.2 to the compatibility test runtime and compiled directly against Thunderbolt Core's published batch-provider API.

### 中文

#### 变更

- 新增对 AE2 Lightning Tech 与 Thunderbolt Core 计数批量派发的兼容。开启智能样板倍增后，Thunderbolt 合成 CPU 可根据 ME 机器或工厂当前的物理输入容量，在一次原子传输中派发多份完整配方；关闭智能倍增时仍使用优化后的普通单份派发路径。直接批量路由可避免与 Mek-E 自身倍增逻辑重复计算，并将所有未接收的配方份数交还 CPU。
- 将 AE2 Lightning Tech 2.0.4 与 Thunderbolt Core 1.0.2 加入兼容性测试运行环境，并直接使用 Thunderbolt Core 已发布的批量供应器 API 进行编译。
