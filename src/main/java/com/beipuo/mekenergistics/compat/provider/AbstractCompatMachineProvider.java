package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import java.util.EnumMap;
import java.util.Map;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public abstract class AbstractCompatMachineProvider implements CompatMachineProvider {
    private final CompatMod provider;
    private final Map<CompatMachineFamily, CompatMachineFamilyAdapter> familyAdapters;

    protected AbstractCompatMachineProvider(
            CompatMod provider, Map<CompatMachineFamily, CompatMachineFamilyAdapter> familyAdapters) {
        this.provider = provider;
        EnumMap<CompatMachineFamily, CompatMachineFamilyAdapter> copy = new EnumMap<>(CompatMachineFamily.class);
        copy.putAll(familyAdapters);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException(provider + " provider must declare at least one machine family");
        }
        this.familyAdapters = Map.copyOf(copy);
    }

    @Override
    public final ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(CompatMachineSpec spec) {
        return adapterFor(spec).menuType(spec);
    }

    @Override
    public final TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
            CompatMachineSpec spec, MachineFactoryRegistrar registrar) {
        return adapterFor(spec).registerBlockEntity(spec, registrar);
    }

    @Override
    public final BlockTypeTile<? extends TileEntityMekanism> createBlockType(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType) {
        return adapterFor(spec).createBlockType(spec, tileType);
    }

    @Override
    public final void registerGridNodeHost(
            CompatMachineSpec spec,
            RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        adapterFor(spec).registerGridNodeHost(spec, event, holder);
    }

    private CompatMachineFamilyAdapter adapterFor(CompatMachineSpec spec) {
        if (spec.provider() != provider) {
            throw new IllegalArgumentException(provider + " provider cannot handle " + spec.provider());
        }
        CompatMachineFamilyAdapter adapter = familyAdapters.get(spec.family());
        if (adapter == null) {
            throw new IllegalArgumentException(provider + " provider cannot handle family " + spec.family());
        }
        return adapter;
    }
}
