package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.api.upgrade.IMePatternAutomationHost;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.support.MePatternSlotInventoryHolder;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.compat.magic.MekanismMagicAutomationAccess;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.tile.base.TileEntityMekanism;
import com.example.mekanismmagic.api.IMekanismMagicAutomation;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Drives the shipped SPI mapper: eligibility, pattern I/O, and exclusion of persistent/manual
 * surfaces. Dummy Mekanism slots/tanks are JDK proxies so the real {@code MeMachineIoAdapter}
 * entry points are used.
 */
class MePatternAutomationProfilesTest {
    @AfterEach
    void resetMagicBridge() {
        MekanismMagicAutomationAccess.resetForTest();
    }

    @Test
    void disabledHostsAreNotUpgradeTargetsAndProduceNoPorts() {
        RecordingHost host = RecordingHost.enabled(false);
        host.itemInputs = List.of(proxy(IInventorySlot.class));

        assertFalse(MePatternAutomationProfiles.isUpgradeTarget(host));
        assertTrue(MePatternAutomationProfiles.inputLayout(host).isEmpty());
        assertTrue(MePatternAutomationProfiles.outputPorts(host).isEmpty());
    }

    @Test
    void enabledHostMapsDeclaredPatternItemFluidAndChemicalPorts() {
        IInventorySlot in = proxy(IInventorySlot.class);
        IInventorySlot out = proxy(IInventorySlot.class);
        IExtendedFluidTank fluidIn = proxy(IExtendedFluidTank.class);
        IExtendedFluidTank fluidOut = proxy(IExtendedFluidTank.class);
        IChemicalTank chemIn = proxy(IChemicalTank.class);
        IChemicalTank chemOut = proxy(IChemicalTank.class);
        RecordingHost host = RecordingHost.enabled(true);
        host.itemInputs = List.of(in);
        host.itemOutputs = List.of(out);
        host.fluidInputs = List.of(fluidIn);
        host.fluidOutputs = List.of(fluidOut);
        host.chemicalInputs = List.of(chemIn);
        host.chemicalOutputs = List.of(chemOut);

        assertTrue(MePatternAutomationProfiles.isUpgradeTarget(host));
        MeInputLayout inputs = MePatternAutomationProfiles.inputLayout(host);
        assertEquals(3, inputs.ports().size(), "item + fluid + chemical pattern inputs");
        assertEquals(3, MePatternAutomationProfiles.outputPorts(host).size(),
                "item + fluid + chemical pattern outputs");
    }

    @Test
    void persistentAndManualSurfacesAreStrippedEvenIfAlsoListedAsPatternIo() {
        IInventorySlot pattern = proxy(IInventorySlot.class);
        IInventorySlot spirit = proxy(IInventorySlot.class);
        IInventorySlot chalk = proxy(IInventorySlot.class);
        IExtendedFluidTank setupFluid = proxy(IExtendedFluidTank.class);
        RecordingHost host = RecordingHost.enabled(true);
        host.itemInputs = List.of(pattern, spirit, chalk);
        host.itemOutputs = List.of(pattern, chalk);
        host.fluidInputs = List.of(setupFluid);
        host.persistentItems = List.of(spirit);
        host.persistentFluids = List.of(setupFluid);
        host.manualItems = List.of(chalk);

        MeInputLayout inputs = MePatternAutomationProfiles.inputLayout(host);
        assertEquals(1, inputs.ports().size(), "only the true pattern item input remains");
        assertEquals(1, MePatternAutomationProfiles.outputPorts(host).size(),
                "manual slots are not pattern outputs");
        assertNotEquals(MePatternAutomationProfiles.inputLayout(host).ports().size(),
                host.itemInputs.size());
    }

    @Test
    void groupedParallelInputsCollapseToOnePort() {
        RecordingHost host = RecordingHost.enabled(true);
        host.itemInputs = List.of(proxy(IInventorySlot.class), proxy(IInventorySlot.class),
                proxy(IInventorySlot.class));
        host.groupParallel = true;

        MeInputLayout grouped = MePatternAutomationProfiles.inputLayout(host);
        assertEquals(1, grouped.ports().size());

        host.groupParallel = false;
        assertEquals(3, MePatternAutomationProfiles.inputLayout(host).ports().size());
    }

    @Test
    void publicSpiHasVersionedSurfaceWithoutAe2Types() throws IOException {
        Path spi = Path.of("src/main/java/com/beipuo/mekenergistics/api/upgrade/IMePatternAutomationHost.java");
        String source = Files.readString(spi);
        assertTrue(source.contains("int API_VERSION = 1"));
        assertTrue(source.contains("meSupportsPatternAutomation"));
        assertTrue(source.contains("mePatternItemInputs"));
        assertTrue(source.contains("mePatternItemOutputs"));
        assertTrue(source.contains("mePatternFluidInputs"));
        assertTrue(source.contains("mePatternFluidOutputs"));
        assertTrue(source.contains("mePatternChemicalInputs"));
        assertTrue(source.contains("mePatternChemicalOutputs"));
        assertTrue(source.contains("mePersistentItemInputs"));
        assertTrue(source.contains("meManualOnlyItemSlots"));
        assertTrue(source.contains("meEnergyContainer"));
        assertTrue(source.contains("meIsBusy"));
        assertFalse(source.contains("appeng."));
        String docsEn = Files.readString(Path.of("docs/me-pattern-automation.md"));
        String docsZh = Files.readString(Path.of("docs/me-pattern-automation.zh.md"));
        assertTrue(docsEn.contains("IMePatternAutomationHost"));
        assertTrue(docsEn.contains("MePatternAutomation.registerBlock"));
        assertTrue(docsEn.contains("API_VERSION = 1"));
        assertTrue(docsZh.contains("IMePatternAutomationHost"));
        assertTrue(docsZh.contains("MePatternAutomation.registerBlock"));
        assertTrue(docsZh.contains("API_VERSION = 1"));
        for (Method method : IMePatternAutomationHost.class.getMethods()) {
            assertFalse(method.getReturnType().getName().startsWith("appeng."), method.getName());
            for (Class<?> parameter : method.getParameterTypes()) {
                assertFalse(parameter.getName().startsWith("appeng."), method.getName());
            }
        }
    }

    @Test
    void constructorRedirectWrapsVirtualGetInitialInventoryAndDoesNotMarkEveryMekTileAsMeHost()
            throws IOException {
        String externalMixin = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/mixin/TileEntityExternalPatternAutomationMeUpgradeMixin.java"));
        String hostMixin = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/mixin/IMePatternAutomationHostUpgradeMixin.java"));
        assertTrue(externalMixin.contains("method = \"<init>\""));
        assertTrue(externalMixin.contains("getInitialInventory(Lmekanism/api/IContentsListener;)"));
        assertTrue(externalMixin.contains("MePatternAutomationRuntimes.wrapInventory"));
        assertFalse(externalMixin.contains("implements MeUpgradeRecipeMachineAdapter"),
                "every TileEntityMekanism must not become MeUpgradeableMachine");
        assertTrue(hostMixin.contains("IMePatternAutomationHost.class"));
        assertTrue(hostMixin.contains("MeUpgradeRecipeMachineAdapter"));
        assertFalse(MeUpgradeableMachine.class.isAssignableFrom(TileEntityMekanism.class));
    }

    @Test
    void wrapInventoryIsIdentityUnlessTheTileIsAnUpgradeAdapterHost() {
        IInventorySlotHolder holder = proxy(IInventorySlotHolder.class);
        RecordingHost enabled = RecordingHost.enabled(true);
        enabled.itemInputs = List.of(proxy(IInventorySlot.class));
        assertTrue(MePatternAutomationProfiles.isUpgradeTarget(enabled));
        assertSame(holder, MePatternAutomationRuntimes.wrapInventory(enabled, holder, null),
                "unit-test hosts are not MeUpgradeRecipeMachineAdapter; wrap must not invent slots");
        RecordingHost disabled = RecordingHost.enabled(false);
        assertSame(holder, MePatternAutomationRuntimes.wrapInventory(disabled, holder, null));
        assertSame(holder, MePatternAutomationRuntimes.wrapInventory("not-a-host", holder, null));
    }

    @Test
    void wrapInventoryDoesNotReattachSlotsWhenASpecificMixinAlreadyWrappedTheHolder() {
        IInventorySlotHolder alreadyWrapped = (IInventorySlotHolder) Proxy.newProxyInstance(
                MePatternSlotInventoryHolder.class.getClassLoader(),
                new Class<?>[] {IInventorySlotHolder.class, MePatternSlotInventoryHolder.class},
                (proxy, method, args) -> defaultProxyResult(method, args, proxy));
        java.util.concurrent.atomic.AtomicInteger addCalls = new java.util.concurrent.atomic.AtomicInteger();
        Object host = Proxy.newProxyInstance(
                IMePatternAutomationHost.class.getClassLoader(),
                new Class<?>[] {IMePatternAutomationHost.class, MeUpgradeRecipeMachineAdapter.class},
                (proxy, method, args) -> {
                    if ("meSupportsPatternAutomation".equals(method.getName())) {
                        return true;
                    }
                    if ("addMePatternSlots".equals(method.getName())) {
                        addCalls.incrementAndGet();
                        return args[0];
                    }
                    if (method.getReturnType() == List.class) {
                        return List.of();
                    }
                    return defaultProxyResult(method, args, proxy);
                });
        assertTrue(MePatternAutomationProfiles.isUpgradeTarget(host));
        assertSame(alreadyWrapped, MePatternAutomationRuntimes.wrapInventory(host, alreadyWrapped, null),
                "factory mixins wrap first; constructor wrap must not add a second pattern-slot layer");
        assertEquals(0, addCalls.get(), "addMePatternSlots must not run on an already-wrapped holder");
    }

    @Test
    void upgradeAndCapabilityRegistrationIncludeExternalSpiHosts() throws IOException {
        String registrar = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/upgrade/MeUpgradeSupportRegistrar.java"));
        String blockEntities = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/registry/ModBlockEntities.java"));
        String mixins = Files.readString(Path.of("src/main/resources/mekenergistics.mixins.json"));
        assertTrue(registrar.contains("MePatternAutomation.resolveRegisteredBlocks()"));
        assertTrue(registrar.contains("externalPatternUpgradeBlocks()"));
        assertTrue(registrar.contains("dimension_miner"));
        assertTrue(blockEntities.contains("externalPatternUpgradeBlocks()"));
        assertTrue(blockEntities.contains("AECapabilities.IN_WORLD_GRID_NODE_HOST"));
        assertTrue(mixins.contains("TileEntityExternalPatternAutomationMeUpgradeMixin"));
        assertTrue(mixins.contains("IMePatternAutomationHostUpgradeMixin"));
    }

    @Test
    void magicBridgeFeedsTheSameMapperAndHonorsDisabledAutomation() {
        MekanismMagicAutomationAccess.resetForTest();
        MekanismMagicAutomationAccess.installApiForTest(IMekanismMagicAutomation.class);
        IInventorySlot input = proxy(IInventorySlot.class);
        IInventorySlot output = proxy(IInventorySlot.class);
        IInventorySlot spirit = proxy(IInventorySlot.class);
        MagicHost enabled = new MagicHost(true, List.of(input, spirit), List.of(output), List.of(spirit),
                List.of());
        MagicHost disabled = new MagicHost(false, List.of(input), List.of(output), List.of(), List.of());

        assertTrue(MePatternAutomationProfiles.isUpgradeTarget(enabled));
        assertEquals(1, MePatternAutomationProfiles.inputLayout(MePatternAutomationProfiles.resolveHost(enabled))
                .ports()
                .size(), "persistent spirit is not a pattern input");
        assertFalse(MePatternAutomationProfiles.isUpgradeTarget(disabled));
        assertTrue(MePatternAutomationProfiles.inputLayout(MePatternAutomationProfiles.resolveHost(disabled))
                .isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, args) -> defaultProxyResult(method, args, proxy));
    }

    private static Object defaultProxyResult(Method method, Object[] args, Object proxy) {
        if ("equals".equals(method.getName())) {
            return proxy == args[0];
        }
        if ("hashCode".equals(method.getName())) {
            return System.identityHashCode(proxy);
        }
        Class<?> returned = method.getReturnType();
        if (returned == boolean.class) {
            return false;
        }
        if (returned == int.class || returned == long.class || returned == float.class || returned == double.class) {
            return 0;
        }
        return null;
    }

    private static final class RecordingHost implements IMePatternAutomationHost {
        private List<IInventorySlot> itemInputs = List.of();
        private List<IInventorySlot> itemOutputs = List.of();
        private List<IExtendedFluidTank> fluidInputs = List.of();
        private List<IExtendedFluidTank> fluidOutputs = List.of();
        private List<IChemicalTank> chemicalInputs = List.of();
        private List<IChemicalTank> chemicalOutputs = List.of();
        private List<IInventorySlot> persistentItems = List.of();
        private List<IExtendedFluidTank> persistentFluids = List.of();
        private List<IInventorySlot> manualItems = List.of();
        private boolean groupParallel;
        private final boolean enabled;

        private RecordingHost(boolean enabled) {
            this.enabled = enabled;
        }

        static RecordingHost enabled(boolean enabled) {
            return new RecordingHost(enabled);
        }

        @Override
        public boolean meSupportsPatternAutomation() {
            return this.enabled;
        }

        @Override
        public List<IInventorySlot> mePatternItemInputs() {
            return this.itemInputs;
        }

        @Override
        public List<IInventorySlot> mePatternItemOutputs() {
            return this.itemOutputs;
        }

        @Override
        public List<IExtendedFluidTank> mePatternFluidInputs() {
            return this.fluidInputs;
        }

        @Override
        public List<IExtendedFluidTank> mePatternFluidOutputs() {
            return this.fluidOutputs;
        }

        @Override
        public List<IChemicalTank> mePatternChemicalInputs() {
            return this.chemicalInputs;
        }

        @Override
        public List<IChemicalTank> mePatternChemicalOutputs() {
            return this.chemicalOutputs;
        }

        @Override
        public List<IInventorySlot> mePersistentItemInputs() {
            return this.persistentItems;
        }

        @Override
        public List<IExtendedFluidTank> mePersistentFluidInputs() {
            return this.persistentFluids;
        }

        @Override
        public List<IInventorySlot> meManualOnlyItemSlots() {
            return this.manualItems;
        }

        @Override
        public IEnergyContainer meEnergyContainer() {
            return null;
        }

        @Override
        public boolean meGroupParallelItemInputs() {
            return this.groupParallel;
        }
    }

    private static final class MagicHost implements IMekanismMagicAutomation {
        private final boolean supports;
        private final List<IInventorySlot> inputs;
        private final List<IInventorySlot> outputs;
        private final List<IInventorySlot> persistent;
        private final List<IInventorySlot> manual;

        private MagicHost(boolean supports, List<IInventorySlot> inputs, List<IInventorySlot> outputs,
                List<IInventorySlot> persistent, List<IInventorySlot> manual) {
            this.supports = supports;
            this.inputs = inputs;
            this.outputs = outputs;
            this.persistent = persistent;
            this.manual = manual;
        }

        @Override
        public ResourceLocation mekanismMagicMachineId() {
            return ResourceLocation.parse("mekanism_magic:test");
        }

        @Override
        public List<IInventorySlot> mekanismMagicPatternInputs() {
            return this.inputs;
        }

        @Override
        public List<IInventorySlot> mekanismMagicPatternOutputs() {
            return this.outputs;
        }

        @Override
        public List<IInventorySlot> mekanismMagicPersistentInputs() {
            return this.persistent;
        }

        @Override
        public List<IInventorySlot> mekanismMagicManualOnlySlots() {
            return this.manual;
        }

        @Override
        public IEnergyContainer mekanismMagicEnergyContainer() {
            return null;
        }

        @Override
        public boolean mekanismMagicSupportsPatternAutomation() {
            return this.supports;
        }
    }
}
