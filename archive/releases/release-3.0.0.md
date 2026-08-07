### English

#### Change

- Added the ME Pattern Provider Upgrade, allowing supported existing Mekanism and addon machines and factories to join an AE2 network and expose pattern-provider controls without being converted into separate ME blocks. Installed upgrades, patterns, output routing, terminal names, and visibility settings persist across reloads.
- Added the ME Passive Crafting Upgrade with configurable interval and batch multiplier controls. It periodically extracts complete pattern inputs from connected AE storage, submits them atomically to the machine, restores rejected inputs, and is discoverable through JEI.
- Expanded installable-upgrade coverage across Mekanism, Mekanism: MoreMachine, Mekanism Extras, Evolved Mekanism, and Mekanism Empowered machines and factories. Added dedicated upgrade-card artwork, switched the project license to MIT, and updated the Data Energistics compatibility runtime to 2.4.2 through CurseMaven.

#### Fix

- Fixed ME upgrade state, patterns, factory recipes, AE nodes, and adjacent capabilities not being restored or refreshed reliably after world and recipe reloads.
- Fixed pattern-window slot paging, phantom windows on unsupported addon machines, and Jade AE status appearing when no ME upgrade was active.
- Fixed ME-connected machines exposing network energy only to recipe checks instead of refilling every local energy container before work, which could break GUIs, parallel limits, and machine-specific logic.
- Fixed compatibility crashes involving Mekanism Extras factory input layouts, MekBee machines, and unsupported addon machines scanned by AE2 pattern terminals.

### 中文

#### 变更

- 新增 ME 样板供应器升级，使受支持的现有 Mekanism 及其附属机器与工厂无需转换成独立 ME 方块，即可接入 AE2 网络并使用样板供应器控制。已安装升级、样板、输出路由、终端名称与可见性设置均可在重载后保留。
- 新增 ME 被动合成升级，提供可配置的执行间隔与批次数量。它会定期从已连接的 AE 存储提取完整样板输入，原子地提交给机器，在拒绝时返还材料，并可通过 JEI 查询。
- 将可安装升级覆盖扩展至 Mekanism、Mekanism: MoreMachine、Mekanism Extras、Evolved Mekanism 与 Mekanism Empowered 的机器和工厂；新增专用升级卡贴图，将项目许可证切换为 MIT，并通过 CurseMaven 将 Data Energistics 兼容测试版本更新至 2.4.2。

#### 修复

- 修复世界或配方重载后，ME 升级状态、样板、工厂配方、AE 节点及相邻能力未能可靠恢复或刷新的问题。
- 修复样板窗口翻页槽位错误、不受支持的附属机器显示虚假窗口，以及未启用 ME 升级时 Jade 仍显示 AE 状态的问题。
- 修复接入 ME 的机器仅在配方检查中读取网络能量，却未在工作前补充全部本地能源容器，进而影响 GUI、并行限制与机器专用逻辑的问题。
- 修复 Mekanism Extras 工厂输入布局、MekBee 机器，以及 AE2 样板终端扫描不受支持附属机器时的兼容性崩溃。
