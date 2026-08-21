# ME 样板自动化接口（API v1）

Mek Energistics 提供的稳定 SPI。第三方 Mekanism 机器实现它之后，可以**直接使用 ME 样板供应器 / 被动合成 / 输出接口升级**，不需要再做一套 ME 方块变体，也不需要在接口签名里引用 AE2 类型。

公开类型：

| 类型 | 包 |
| --- | --- |
| `IMePatternAutomationHost` | `com.beipuo.mekenergistics.api.upgrade` |
| `MePatternAutomation` | `com.beipuo.mekenergistics.api.upgrade` |

当前 `API_VERSION = 1`。只在破坏性改签名时才会加版本；兼容修订可以新增 `default` 方法。

运行时请调用 `mePatternAutomationApiVersion()`，不要依赖编译期常量做协商（Java 可能内联 `API_VERSION`）。

## 你会得到什么

实现接口并注册方块后，Mek Energistics 负责：

- 给机器加上 ME 样板供应器、被动合成、输出接口三张卡
- 在 `TileEntityMekanism` 构造时给支持的机器挂上样板槽（子类覆盖 `getInitialInventory` 仍然生效）
- 通过 `AECapabilities.IN_WORLD_GRID_NODE_HOST` 让 AE 线缆发现主机（NeoForge AE2 不会靠 `instanceof IInWorldGridNodeHost`）
- 按你声明的物品 / 流体 / 化学品槽编码样板、推入原料、产物回网
- 把持久槽和手动槽从样板 I/O 里剔除，即使你误把它们也列进了 pattern 列表

## 你不需要做什么

- 不要引用 `appeng.*`
- 不要自己注册 AE 网格节点或样板供应器
- 不要给每台机器再做一块 ME 方块
- 不要让普通 Mekanism 方块实体去实现 Mek Energistics 内部的 `MeUpgradeableMachine`

Mekanism Magic 已经通过它自己的 `IMekanismMagicAutomation` 接入，不需要再实现本 SPI。次元采矿机（`mekanism_magic:dimension_miner`）会声明不支持样板自动化，因此被排除。

## 接入步骤

1. 把 `mekenergistics` 标成可选依赖（或按你的发布策略设为必需）。
2. 在 **`TileEntityMekanism` 子类** 上实现 `IMePatternAutomationHost`。
3. 在模组构造阶段用 **方块 ID** 调用 `MePatternAutomation.registerBlock(...)`，必须早于 Mek Energistics 的 capability 注册。
4. 方块最好是 Mekanism 的 `ITypeBlock`，否则升级卡加不进去。
5. 按下面的规则声明槽位。返回值里不要放 `null` 元素。

## 接口方法

### 资格

| 方法 | 必须实现 | 说明 |
| --- | --- | --- |
| `meSupportsPatternAutomation()` | 是 | 返回 `false` 时：不加卡、不做 AE 主机、不编码样板。可按机器种类或配置关闭。 |
| `mePatternAutomationApiVersion()` | 否 | 默认返回 `API_VERSION`。 |

### 样板 I/O（会进入编码样板，也可被 AE 插入 / 抽回）

| 方法 | 默认 | 说明 |
| --- | --- | --- |
| `mePatternItemInputs()` | 必须实现 | 物品输入 |
| `mePatternItemOutputs()` | 必须实现 | 物品输出 |
| `mePatternFluidInputs()` | 空列表 | 流体输入 |
| `mePatternFluidOutputs()` | 空列表 | 流体输出 |
| `mePatternChemicalInputs()` | 空列表 | 化学品输入 |
| `mePatternChemicalOutputs()` | 空列表 | 化学品输出 |

### 持久输入（配方需要，但不写进每张样板）

例如催化剂、灵魂源、已安装工具。AE 不会把它们当成样板原料。

| 方法 | 默认 |
| --- | --- |
| `mePersistentItemInputs()` | 空列表 |
| `mePersistentFluidInputs()` | 空列表 |
| `mePersistentChemicalInputs()` | 空列表 |

同一对象如果同时出现在 pattern 列表和 persistent 列表里，**会被当成 persistent，从样板端口剔除**。

### 仅手动槽

选择器、粉笔、手册等：既不自动插入，也不编码。

| 方法 | 默认 |
| --- | --- |
| `meManualOnlyItemSlots()` | 空列表 |

同样按对象身份从 pattern 端口剔除。

### 其它

| 方法 | 必须实现 | 说明 |
| --- | --- | --- |
| `meEnergyContainer()` | 是 | 返回机器已经在用的能量容器。 |
| `meIsBusy()` | 否，默认 `false` | 机器暂时不能再接自动化任务时返回 `true`。请如实实现。 |
| `meGroupParallelItemInputs()` | 否，默认 `false` | `true` 时，多个相同物品输入槽会合成一个工厂式分组端口，避免多数量配方被拆到各条处理线。并行工厂应返回 `true`。 |

## 注册方块

```java
MePatternAutomation.registerBlock(
        ResourceLocation.fromNamespaceAndPath("mymod", "my_machine"));
```

也接受 `Block` 重载，但 **只有该方块已经出现在 `BuiltInRegistries.BLOCK` 时才会登记**。构造阶段的 `DeferredRegister` 持有者通常还不在注册表里，调用会被静默忽略。

推荐在 `@Mod` 构造函数里用 `ResourceLocation` 登记：

```java
public MyMod(IEventBus modBus) {
    if (ModList.get().isLoaded("mekenergistics")) {
        MePatternAutomation.registerBlock(
                ResourceLocation.fromNamespaceAndPath("mymod", "my_machine"));
    }
}
```

`FMLCommonSetupEvent` **太晚**：那时 `RegisterCapabilitiesEvent` 已经结束，AE 线缆发现不到主机。

## 可选依赖（很重要）

Tile 类如果直接 `implements IMePatternAutomationHost`，没有 Mek Energistics 时这个类会加载失败。

可选接入请三选一：

1. 把接口实现放到 **仅在 `mekenergistics` 存在时加载** 的类（独立 compat 类、或带条件的 Mixin）。
2. 将 `mekenergistics` 设为该机器功能的必需依赖。
3. 像 Mekanism Magic 那样发布自己的自动化 API，由 Mek Energistics 做桥接。

`mods.toml` 示例：

```toml
[[dependencies.mymod]]
    modId="mekenergistics"
    type="optional"
    versionRange="[3.0.6,)"
    ordering="NONE"
    side="BOTH"
```

Gradle 使用 `compileOnly` / `localRuntime`（按你的发布方式），不要让接口签名泄漏 AE2 类型。

## 最小示例

```java
public class MyMachineBlockEntity extends TileEntityMekanism
        implements IMePatternAutomationHost {
    private final IInventorySlot inputSlot;
    private final IInventorySlot outputSlot;
    private final IInventorySlot catalystSlot;
    private final IInventorySlot selectorSlot;

    @Override
    public boolean meSupportsPatternAutomation() {
        return true;
    }

    @Override
    public List<IInventorySlot> mePatternItemInputs() {
        return List.of(this.inputSlot);
    }

    @Override
    public List<IInventorySlot> mePatternItemOutputs() {
        return List.of(this.outputSlot);
    }

    @Override
    public List<IInventorySlot> mePersistentItemInputs() {
        return List.of(this.catalystSlot);
    }

    @Override
    public List<IInventorySlot> meManualOnlyItemSlots() {
        return List.of(this.selectorSlot);
    }

    @Override
    public IEnergyContainer meEnergyContainer() {
        return getEnergyContainer();
    }
}
```

流体 / 化学品机器覆盖对应的 `mePatternFluid*` / `mePatternChemical*` 方法即可。

## 约束与常见坑

- 主机必须是 `TileEntityMekanism`（或兼容的 Mekanism tile）。接口注入只作用在真正的 SPI 实现者上，能量立方等普通 Mek 方块不会变成 ME 主机。
- 只返回这台机器自己的槽 / 罐对象，不要每次调用 `new` 一个包装器，否则 persistent / manual 剔除会失效。
- 列表不要包含 `null`。
- 工厂若已有专用 Mixin 挂上样板槽，构造期包装是幂等的，不会套两层。
- 线缆连不上：几乎都是没调用 `registerBlock`，或调用晚于 capability 事件，或 `registerBlock(Block)` 时方块还不在注册表中。

## 版本策略

- v1：本文描述的完整表面。
- 新增 `default` 方法：兼容修订，不升 `API_VERSION`。
- 改方法签名、收紧语义、删除方法：升 `API_VERSION`。
