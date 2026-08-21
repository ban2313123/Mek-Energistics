# ME Pattern Automation SPI (API v1)

Stable first-party SPI from Mek Energistics. Third-party Mekanism machines implement it to receive **ME pattern-provider, passive-crafting, and output-interface upgrades** on the original block. You do not ship a separate ME block variant, and you must not put AE2 types in the SPI signatures.

Public types:

| Type | Package |
| --- | --- |
| `IMePatternAutomationHost` | `com.beipuo.mekenergistics.api.upgrade` |
| `MePatternAutomation` | `com.beipuo.mekenergistics.api.upgrade` |

`API_VERSION = 1`. Bump it only for breaking signature changes. Compatible revisions may add `default` methods.

Call `mePatternAutomationApiVersion()` at runtime. Do not negotiate against the compile-time `API_VERSION` constant; Java may inline it.

## What Mek Energistics provides

After you implement the interface and register the block, Mek Energistics:

- Adds the ME pattern-provider, passive-crafting, and output-interface cards
- Attaches pattern slots during `TileEntityMekanism` construction (subclass `getInitialInventory` overrides still receive them)
- Registers `AECapabilities.IN_WORLD_GRID_NODE_HOST` so AE cables discover the host (NeoForge AE2 does not use `instanceof IInWorldGridNodeHost`)
- Encodes patterns from your declared item / fluid / chemical surfaces, inserts ingredients, and returns outputs to the ME network
- Strips persistent and manual-only surfaces from pattern I/O even if they were also listed as pattern slots

## What you must not do

- Do not reference `appeng.*`
- Do not register AE grid nodes or crafting providers yourself
- Do not create a parallel ME block for each machine
- Do not implement Mek Energistics internals such as `MeUpgradeableMachine` on ordinary Mekanism tiles

Mekanism Magic is already bridged through its own `IMekanismMagicAutomation` and does not need this SPI. The dimensional miner (`mekanism_magic:dimension_miner`) reports that it does not support pattern automation and is excluded.

## Integration steps

1. Depend on `mekenergistics` as optional (or required, if that matches your release).
2. Implement `IMePatternAutomationHost` on a **`TileEntityMekanism` subclass**.
3. Call `MePatternAutomation.registerBlock(...)` with the **block id** during mod construction, before Mek Energistics registers capabilities.
4. Prefer a Mekanism `ITypeBlock` so upgrade cards can be added.
5. Declare slots as below. Do not return `null` elements.

## Interface methods

### Eligibility

| Method | Required | Meaning |
| --- | --- | --- |
| `meSupportsPatternAutomation()` | yes | When `false`: no cards, no AE host, no pattern encoding. Use this to disable a machine kind or a config flag. |
| `mePatternAutomationApiVersion()` | no | Defaults to `API_VERSION`. |

### Pattern I/O (encoded into patterns; AE may insert / extract)

| Method | Default | Meaning |
| --- | --- | --- |
| `mePatternItemInputs()` | required | Item inputs |
| `mePatternItemOutputs()` | required | Item outputs |
| `mePatternFluidInputs()` | empty | Fluid inputs |
| `mePatternFluidOutputs()` | empty | Fluid outputs |
| `mePatternChemicalInputs()` | empty | Chemical inputs |
| `mePatternChemicalOutputs()` | empty | Chemical outputs |

### Persistent inputs (needed to run, not encoded on every pattern)

Catalysts, spirit sources, installed tools. AE does not treat them as pattern ingredients.

| Method | Default |
| --- | --- |
| `mePersistentItemInputs()` | empty |
| `mePersistentFluidInputs()` | empty |
| `mePersistentChemicalInputs()` | empty |

If the same object appears in both a pattern list and a persistent list, **it is treated as persistent and removed from pattern ports**.

### Manual-only slots

Selectors, chalk, manuals: never auto-inserted, never encoded.

| Method | Default |
| --- | --- |
| `meManualOnlyItemSlots()` | empty |

These are also stripped from pattern ports by object identity.

### Other

| Method | Required | Meaning |
| --- | --- | --- |
| `meEnergyContainer()` | yes | Return the energy container the tile already uses. |
| `meIsBusy()` | no, default `false` | Return `true` when the machine cannot accept more automated work. Keep it truthful. |
| `meGroupParallelItemInputs()` | no, default `false` | When `true`, several identical item-input slots become one factory-style grouped port so multi-count recipes are not split across process lanes. Parallel factories should return `true`. |

## Registering the block

```java
MePatternAutomation.registerBlock(
        ResourceLocation.fromNamespaceAndPath("mymod", "my_machine"));
```

The `Block` overload only records the block **after it exists in `BuiltInRegistries.BLOCK`**. A constructor-time `DeferredRegister` holder is usually unbound; that call is ignored.

Register the `ResourceLocation` from your `@Mod` constructor:

```java
public MyMod(IEventBus modBus) {
    if (ModList.get().isLoaded("mekenergistics")) {
        MePatternAutomation.registerBlock(
                ResourceLocation.fromNamespaceAndPath("mymod", "my_machine"));
    }
}
```

`FMLCommonSetupEvent` is **too late**: `RegisterCapabilitiesEvent` has already run, so AE cables will not discover the host.

## Optional dependency (important)

A tile class that `implements IMePatternAutomationHost` will fail to load if Mek Energistics is absent.

For optional integration, pick one:

1. Put the interface on a class that loads **only when `mekenergistics` is present** (a dedicated compat class or a conditional Mixin).
2. Make `mekenergistics` required for that machine feature.
3. Publish your own automation API and let Mek Energistics bridge it, as Mekanism Magic does.

`mods.toml` example:

```toml
[[dependencies.mymod]]
    modId="mekenergistics"
    type="optional"
    versionRange="[3.0.6,)"
    ordering="NONE"
    side="BOTH"
```

Use `compileOnly` / `localRuntime` in Gradle as appropriate. Do not leak AE2 types through your SPI signatures.

## Minimal example

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

Override `mePatternFluid*` / `mePatternChemical*` for fluid or chemical machines.

## Constraints and pitfalls

- The host must be a `TileEntityMekanism` (or a compatible Mekanism tile). The ME-upgrade adapter is mixed only onto real SPI implementors; energy cubes and other ordinary Mek tiles stay ordinary.
- Return the tile's own slot / tank instances. Do not allocate a new wrapper on every call, or persistent / manual exclusion will miss them.
- Do not put `null` in the lists.
- If a factory mixin already wrapped the inventory, constructor wrapping is idempotent and will not add a second pattern-slot layer.
- Cables not connecting almost always means `registerBlock` was skipped, ran after capability registration, or used `registerBlock(Block)` before the block was in the registry.

## Versioning

- v1: the surface described here.
- New `default` methods: compatible revision, `API_VERSION` unchanged.
- Signature changes, tighter semantics, or removed methods: bump `API_VERSION`.
