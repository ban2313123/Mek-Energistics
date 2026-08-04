# Changelog

## 2.0.5

### English

#### Fix

- Fixed highly parallel ME machines and factories reporting insufficient energy after an autocrafting order even when the AE network contained enough FE. Network-backed recipe checks now see the full available local and network energy instead of being capped by the machine's local energy buffer.

### 中文

#### 修复

- 修复高并行 ME 机器与工厂在自动合成下单后，即使 AE 网络中有足够 FE 仍显示能量不足的问题。网络供能配方检查现在会读取本地与网络中的完整可用能量，不再受机器本地能量缓存上限限制。

## 2.0.3

### English

#### Change

- Expanded AE2 memory card support to copy patterns together with item, chemical, and fluid output-to-AE settings, the pattern assembly name, and Pattern Access Terminal visibility.
- Preserved stored block-entity components and machine configuration when dismantling ME machines and factories.
- Made the optional OmniSequence and Data Energistics API contracts self-contained for reproducible builds, and updated the GitHub Actions upload/download steps to v5.
- Set the Minecraft 1.21.1 NeoForge development and build baseline to 21.1.220.

#### Fix

- Refreshed AE nodes, published patterns, and neighboring capabilities immediately after an installer converts a machine, so the converted ME machine can accept autocrafting jobs without first being broken and placed again.

### 中文

#### 变更

- 扩展 AE2 内存卡支持：除样板外，现在还会复制物品、化学品和流体的输出至 AE 设置、样板装配名称，以及是否在样板访问终端中显示。
- 拆除 ME 机器与工厂时保留其方块实体组件和机器配置。
- 将 OmniSequence 与 Data Energistics 的可选 API 契约改为项目内自包含，使构建可复现，并将 GitHub Actions 的上传与下载步骤更新至 v5。
- 将 Minecraft 1.21.1 的 NeoForge 开发与构建基线设为 21.1.220。

#### 修复

- 安装器转换机器后会立即刷新 AE 节点、已发布样板和相邻能力，使转换后的 ME 机器无需先拆除重放即可正常接收自动合成订单。

## 2.0.2

### English

#### Change

- Added counted batch-provider integration for Data Energistics, allowing its crafting CPU to negotiate the number of complete crafts that an ME machine or factory can currently accept and transfer the batch atomically.
- Added Omni Batch Provider API v1 integration for OmniSequence: Transfinite 1.3.9, including ordinary ME machines and factory tiers, while preventing duplicate multiplication when an Omni-managed crafting CPU controls the batch.
- Replaced the fixed 1,048,576-copy smart-pattern ceiling with capacity-aware `long` sizing, so large machines can use their actual available input capacity while remaining bounded by the crafting job, input amounts, energy, and atomic routing checks.
- Updated the NeoForge development and build baseline from 21.1.220 to 21.1.238 for Minecraft 1.21.1.

#### Fix

- Preserved pattern input-slot identities and substituted keys during Omni batch admission and used the authoritative delivered totals at commit time, avoiding incorrect assumptions when AE2 substitutions change keys or ratios.
- Kept returned-container, reusable-input, malformed, incomplete, or capacity-changed batches on safe rejection or normal one-craft dispatch paths without partially consuming inputs.

### 中文

#### 变更

- 新增 Data Energistics 计数批处理供应器适配，使其合成 CPU 能按 ME 机器或工厂当前可接收的完整配方数量进行协商，并原子地移交整批材料。
- 新增 OmniSequence: Transfinite 1.3.9 的 Omni Batch Provider API v1 适配，覆盖普通 ME 机器与各等级 ME 工厂；当批处理由 Omni 管理的合成 CPU 执行时，会避免与本模组的倍增逻辑重复计算。
- 移除智能样板固定 1,048,576 份的上限，改为基于机器真实输入容量的 `long` 范围计算，同时仍受合成任务余量、单份输入数量、AE 能量与原子路由检查约束。
- 将 Minecraft 1.21.1 的 NeoForge 开发与构建基线从 21.1.220 更新至 21.1.238。

#### 修复

- Omni 批处理准入现在会保留样板输入槽位及替代材料键，并在提交时使用 API 交付的权威材料总量，避免 AE2 替代材料改变键或比例时产生错误推算。
- 对带返还容器或可复用输入的配方，以及畸形、不完整或容量已变化的批次，保持安全拒绝或回退到普通单份派发，不会部分吞入材料。

## 2.0.1

### English

#### Fix

- Fixed a game crash when ME autocrafting sent recipes such as nutritional liquid to the Mekanism Extras ME Infinite Dissolving Factory by routing conversion items to the factory's real chemical input slot instead of its nullable inherited extra slot.
- Made shared item, chemical, and fluid input adapters reject unavailable optional-machine ports safely, preventing equivalent null-slot crashes when addon machine layouts are missing or change.

### 中文

#### 修复

- 修复 ME 自动合成向 Mekanism Extras 的 ME 悖论无限溶解工厂下单营养液等配方时发生的游戏崩溃；转换物品现在会送入工厂真实的化学品输入槽，而非继承得到的可空附加槽。
- 共用物品、化学品与流体输入适配器现在会安全拒绝不可用的可选机器端口，避免附属模组机器布局缺失或变化时发生同类空槽崩溃。

## 2.0.0

### English

#### Change

- Moved the item, chemical, and fluid output-to-AE controls into the shared pattern window and added a persisted per-machine toggle for showing patterns in the Pattern Access Terminal.
- Expanded Mekanism: MoreMachine large-machine networking so AE2 cables can connect anywhere on an exposed multiblock surface, with correctly owned nodes for AE2 security.
- Matched ME factory energy usage and storage to the base machine and factory tier it mirrors, including Evolved Mekanism, Mekanism: MoreMachine, Mekanism Extras, and Evolved Mekanism Extras machines and factories.
- Reduced per-tick overhead by caching stable pattern I/O layouts, optional-mod lookups, factory tier reflection, pattern-slot views, and machine block lookups.
- Reorganized machine definitions, energy profiles, I/O profiles, factory GUI geometry, and optional compatibility boundaries, with stronger transaction, output-drain, save-compatibility, and architecture tests.

#### Fix

- Fixed `patternPages` and `preferLocalFe` configuration values not being synchronized from the server, which could make clients address the wrong machine inventory slots.
- Fixed optional large-machine and Evolved Mekanism Extras mixins loading when their target classes were absent, and made missing optional block entities or ExtendedAE rename hooks degrade safely instead of crashing startup or world loading.
- Fixed ME factories using a flat 50 J/t cost and 2,000,000 J buffer instead of the upstream machine's configured energy values and tier process count.
- Fixed Jade AE status tooltips using unrelated AE2 translation keys and added consistent localized text for the missing-channel state.

### 中文

#### 变更

- 将物品、化学品和流体的 AE 输出控制移入共用样板窗口，并新增按机器持久化的“是否在样板访问终端中显示”开关。
- 扩展 Mekanism: MoreMachine 大型机器的网络连接，使 AE2 线缆可连接多方块结构任意外露表面，并为所有节点设置正确的 AE2 安全所有者。
- 使 ME 工厂的能耗与储能匹配其对应基础机器和工厂等级，覆盖 Evolved Mekanism、Mekanism: MoreMachine、Mekanism Extras 与 Evolved Mekanism Extras 的机器和工厂。
- 缓存稳定的样板 I/O 布局、可选模组检测、工厂等级反射结果、样板槽视图与机器方块查询，降低每 tick 的重复开销。
- 重构机器定义、能量配置、I/O 配置、工厂 GUI 布局及可选兼容边界，并加强事务安全、输出提取、存档兼容与架构测试。

#### 修复

- 修复 `patternPages` 与 `preferLocalFe` 配置未从服务端同步，导致客户端可能访问错误机器物品栏槽位的问题。
- 修复大型机器与 Evolved Mekanism Extras 的 Mixin 在目标类不存在时仍会加载的问题；当可选方块实体或 ExtendedAE 重命名钩子缺失时，现在会安全降级而非导致启动或世界加载崩溃。
- 修复 ME 工厂统一使用 50 J/t 与 2,000,000 J 储能，而未采用上游机器配置能耗及对应等级并行数的问题。
- 修复 Jade 的 AE 状态提示错误使用无关 AE2 翻译键的问题，并补充统一的“缺少频道”本地化文本。

## 2.0.0-beta

### Change

- Rebuilt ME machine integration around a shared AE support and I/O adapter layer, replacing duplicated lifecycle, pattern, input, output, and smart-batch implementations across Mekanism and supported addons.
- Changed pattern delivery to route actual AE item, chemical, and fluid keys transactionally into declared machine ports without selecting a path from the Mekanism recipe type. Failed deliveries now roll back instead of leaving partial inputs.
- Migrated native Mekanism, Mekanism: MoreMachine, Mekanism Extras, Evolved Mekanism, and Evolved Mekanism Extras machines and factories to the shared input routing and actual-output collection model.
- Centralized optional machine compatibility, registration, client setup, generated resources, factory tier graphs, and installer routes in the compatibility catalog.
- Added ME versions of the Mekanism: MoreMachine Large Rotary Condensentrator, Large Solar Neutron Activator, Large Electrolytic Separator, Large Chemical Infuser, and Large Antiprotonic Nucleosynthesizer.
- Matched the five large machines' upstream models, baked transforms, collision and selection bounds, renderers, menus, recipes, loot, and JEI integration without adding Mekanism side-configuration pages.
- Added independent item, chemical, and fluid output-to-AE controls to the shared pattern window, including support for actual fluid and chemical byproducts.
- Updated the machine adaptation guide for the catalog-driven support and I/O adapter architecture.

### Fix

- Fixed ME nodes failing to reconnect after chunk unloads, world reloads, or reuse of the same block entity instance by recreating destroyed managed nodes from retained state.
- Fixed large Mekanism: MoreMachine nodes so cables can connect through the center of any horizontal face without proxying one node into itself, and deferred large-node setup until the block entity is fully constructed.
- Fixed encoded patterns not being republished reliably after loading, while preserving existing pattern slots, priorities, terminal names, output modes, and pending smart-pattern work.
- Fixed pattern-terminal node access and restored optional Data Energistics interaction after network and world reloads.
- Fixed smart-pattern capacity checks for upgraded input slots, guarded extra-slot limit probing, and kept failed multi-input transactions atomic.
- Fixed ME Metallurgic Infuser conversion patterns so convertible items can enter the extra slot, and corrected pattern-slot quick-move behavior.
- Fixed large-machine item models, lighting and occlusion behavior, dedicated menu resolution, and the Large Antiprotonic Nucleosynthesizer renderer path.

## 1.0.7

### Fix

- Fixed a client crash when showing details for ME factory item tooltips if Mekanism reports no known item containers for the factory stack.

## 1.0.5

### Change

- Added EvolvedMekanism machine compatibility for ME Solidification Chamber, ME Thermalizer, and ME Chemical Mixer.
- Added smart pattern multiplication and faster pattern processing for ME machines and factories.
- Added safer shared AE output, pattern insertion, and network-energy helpers across Mekanism, Mekanism Extras, Mekanism: MoreMachine, EvolvedMekanism, and EvolvedMekanismExtras machines.
- Added a CurseForge publish workflow for GitHub release based publishing.
- Improved ME pattern-machine menus, quick move behavior, and configurable tile screens.

### Fix

- Fixed recipe AE support initialization order for regular recipe machines, chemical machines, Mekanism: MoreMachine machines, and compat factories.
- Fixed malformed encoded pattern handling so invalid third-party patterns are skipped safely instead of crashing the machine integration path.
- Fixed AE-backed energy container construction order and recipe energy usage wrappers.
- Fixed optional EvolvedMekanism, EvolvedMekanismExtras, Mekanism Extras, and Mekanism: MoreMachine factory support delegates.
- Fixed client setup registration on the mod event bus.

## 1.0.0

### Change

- Added smart pattern multiplication for ME pattern slots, so large autocrafting requests can place repeated patterns more conveniently when the machine has room.
- Improved ME machine items to look and read more like their Mekanism counterparts, including colored machine names and clearer item tooltips.
- Improved ME machine registration and item behavior consistency across Mekanism, Mekanism: MoreMachine, Mekanism Extras, and Evolved Mekanism Extras integrations.
- Reorganized the machine adaptation guide for pack makers and future ME machine support.

### Fix

- Fixed ME machine wrench dismantling so machine data, upgrades, side configuration, energy, inventory contents, and installed AE patterns are preserved correctly.
- Fixed ME Factory Installer upgrades across Mekanism Extras and Evolved Mekanism Extras factory chains.
- Fixed ME machine item tooltips and stored-data display by backing item behavior with the expected capabilities.
- Fixed ME Isotopic Centrifuge and ME Centrifuging Factory item icons being too large in inventories.
- Fixed ME centrifuging factory item lighting so their inventory icons match the original factory style more closely.
- Fixed ExtendedAE renaming support so quartz cutting knives can open the renaming screen on ME machines when ExtendedAE is installed.
- Fixed renamed ME machine drops so they restore the machine name through the normal item name instead of saving a separate pattern-terminal name.

## 0.0.14-beta

- Fixed Mekanism: MoreMachine CNC Stamper pattern insertion so non-consumed mold items can stay preloaded in the extra slot while AE patterns only provide consumed input items.
- Applied the same non-consumed mold handling to MekMM and Mekanism Extras stamping factories.
- Improved shared AE pattern insertion helpers for single-item inputs with required extra slots.
- Removed unsupported ME variants for Mekanism utility/configuration machines that do not need AE pattern support.
- Verified the update with Gradle build.

## 0.0.13-beta

- Adapted Mekanism Extras and EvolvedMekanismExtras factory installers so terminal VME ME factories can enter the matching extra factory chains.
- Kept cross-chain installer upgrades gated to the first extra tier, avoiding skips from lower EvolvedMekanism factory tiers.
- Verified the update with Gradle compile.

## 0.0.12-beta

- Added EvolvedMekanism and EvolvedMekanismExtras factory compatibility, including recipes, models, loot tables, lang entries, and EME Extra factory GUI support.
- Added the EvolvedMekanism Iglee Library dependency and updated runtime dependencies for the new compatibility chain.
- Fixed ME Factory Installer behavior so reusing it on existing ME machines does not downgrade them to ME Basic Factories.
- Matched Mekanism's installer interaction behavior by removing the client-side right-click use animation from ME factory installers.
- Matched EvolvedMekanism factory upgrade support for ME Evolved factories while keeping stack upgrades limited to factories whose source mod supports them.
- Added the machine adaptation guide docs.
- Verified the update with Gradle build.

## 0.0.11-beta

- Fixed transparent/missing adjacent block faces next to ME machines, including the ME Metallurgic Infuser case reported with AE2 Additions wireless transceivers.
- Restored Mekanism's original custom block shapes for ME machines and factories so adjacent blocks cull faces correctly.
- Applied the same shape fix across Mekanism, Mekanism Extras factories, and Mekanism: MoreMachine shape-sensitive machines.
- Verified the update with Gradle build.

## 0.0.10-beta

- Fixed ME machine dismantling so wrench removal keeps Mekanism machine data and drops installed AE patterns instead of deleting them.
- Fixed ME Factory Installer upgrades so installed upgrades, machine data, side configuration, energy, and AE patterns are preserved during conversion.
- Fixed Isotopic Centrifuge upgrade paths, including ME Isotopic Centrifuge to ME Basic Centrifuging Factory and Mekanism: MoreMachine centrifuging factories.
- Updated ME centrifuging factory models to use this mod's redesigned local factory indicator bars.
- Fixed machine light occlusion so ME machines, factories, and tall machines match Mekanism's original lighting behavior.
- Verified the update with Gradle build.

## 0.0.9-beta

- Fixed AE-network energy usage for Mekanism machines so recipe execution can consume AE power without displaying network power as stored FE.
- Added AE-aware recipe energy handling for Mekanism: MoreMachine base machines, regular factories, and advanced factories.
- Added AE-aware recipe energy handling for Mekanism Extras regular factories, MoreMachine-derived factories, and advanced factories.
- Kept original Mekanism/compat-mod recipe logic intact while wrapping only the cached recipe energy view.
- Verified the update with `compileJava`.

## 0.0.8-beta

- Added ME Planting Station and ME Replicator base machines for Mekanism: MoreMachine, so the ME Factory Installer maps the original base machines to their matching ME base versions instead of a basic factory.
- Improved ME Factory Installer target resolution and blocked it from remapping machines that are already ME machines.
- Added bounding-block handling for the ME Factory Installer when converting or interacting with tall machines.
- Fixed MekMM planting/replicating base machine AE support initialization during block entity construction.
- Refactored machine and factory registration so tile registration and AE grid-node capability registration share centralized descriptors.
- Matched the ME Planting Station item display transform with the ME planting factory item models.
