# 24. 按机器类型的落地 playbook

所有类型都遵循同一原则：BlockEntity 只保留 Mek recipe/cache、能量和上游生命周期；AE 输入由 `MeInputLayout`/router 处理，输出由稳定 `MeOutputPort` 回网。

## 单 item 输入机器

代表：Enrichment Chamber、Crusher、Energized Smelter。

- 普通机器参考 `MeElectricMachineBlockEntity`，工厂参考 `MeItemStackToItemStackFactoryBlockEntity`；
- 声明一个 item input port 和 item output port；
- 不在具体机器检查 `inputHolder.length` 或 AE key 类型。

## item + chemical 输入机器

代表：Osmium Compressor、Purification Chamber、Chemical Injection Chamber、Metallurgic Infuser。

- item slot 和 chemical tank 作为同一事务的端口；
- 槽位确实有主/副语义时使用固定 lane，否则使用 unordered layout；
- 不绕过上游 `useStatisticalMechanics()`、容量和过滤器。

## 双 item 输入机器

代表：Combiner、CNC Stamper。

- 主输入和副输入声明为两个固定 lane；
- router 统一模拟、执行和回滚；
- extra/catalyst 槽不能与主槽交换。

## item 输入 + 副产物输出机器

代表：Precision Sawmill。

- 声明主输出和 secondary output 两个 item port；
- 副产物为空是合法结果；
- 输出不与样板预期结果比较，实际存在什么就回收什么。

## item + fluid + chemical 输入机器

代表：Pressurized Reaction Chamber。

- 用三个位置敏感 lane 声明 item、fluid、chemical；
- 所有输入先模拟，任一失败时整体返回 `false`；
- item、chemical 和 fluid 输出端口全部声明。

## chemical + chemical 输入机器

代表：Chemical Infuser、Pigment Mixer。

- 左右 tank 可互换时作为同一候选端口集合；
- router 负责回溯分配和完整回滚；
- 输出 chemical tank 直接作为 `MeOutputPort` 声明。

## fluid/chemical 模式切换机器

代表：Rotary Condensentrator、Antiprotonic Nucleosynthesizer。

- 当前 mode 决定 input/output adapter 的端口类型；
- GUI 改 mode 后同步更新 layout/ports；
- 样板只在当前物理端口能容纳时执行。

## 多输出或环境依赖机器

代表：Electrolytic Separator、Chemical Washer、Solar Neutron Activator、Nutritional Liquifier。

- 所有实际 output slot/tank 都加入输出端口列表；
- daylight、biome、water source 等环境条件仍由 Mek recipe/机器逻辑处理；
- `AeOutputMode` 分别控制 item、chemical、fluid，网络满时保留机器内背压。

## 无 AE 样板的工具机器

Digital Miner、Teleporter、Oredictionificator、Modification Station、Logistical Sorter 等没有明确 crafting provider 语义的机器，不实现 `MeAeMachine`，也不进入 catalog 的 ME variant；公共 registry 只处理 `available()` 且 `hasMeVariant` 的条目。
