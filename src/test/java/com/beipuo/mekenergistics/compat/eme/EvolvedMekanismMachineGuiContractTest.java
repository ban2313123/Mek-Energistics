package com.beipuo.mekenergistics.compat.eme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EvolvedMekanismMachineGuiContractTest {
    private static final Path MENU_TYPES = Path.of(
            "src/main/java/com/beipuo/mekenergistics/compat/eme/EvolvedMekanismMachineMenuTypes.java");
    private static final Path PROVIDER = Path.of(
            "src/main/java/com/beipuo/mekenergistics/compat/provider/EmekMachineProvider.java");
    private static final Path SCREENS = Path.of(
            "src/main/java/com/beipuo/mekenergistics/client/compat/eme/EvolvedMekanismMachineClientScreens.java");

    private static final List<GuiBinding> BINDINGS = List.of(
            new GuiBinding("ALLOYER", "ME_ALLOYER", "me_alloyer", "MeAlloyerBlockEntity", "GuiAlloyer"),
            new GuiBinding("CHEMIXER", "ME_CHEMIXER", "me_chemixer", "MeChemixerBlockEntity", "GuiChemixer"),
            new GuiBinding("SOLIDIFICATION_CHAMBER", "ME_SOLIDIFIER", "me_solidifier",
                    "MeSolidifierBlockEntity", "GuiSolidifier"),
            new GuiBinding("THERMALIZER", "ME_THERMALIZER", "me_thermalizer",
                    "MeThermalizerBlockEntity", "GuiMelter"));

    @Test
    void everyEvolvedMachineHasItsOwnCompatibleMenuAndScreen() throws IOException {
        Set<String> evolvedIdentities = java.util.Arrays.stream(MeMekanismMachine.values())
                .filter(machine -> machine.family() == CompatMachineFamily.EMEK_MACHINE)
                .map(machine -> machine.identity().name())
                .collect(Collectors.toSet());
        assertEquals(evolvedIdentities, BINDINGS.stream().map(GuiBinding::identity).collect(Collectors.toSet()),
                "Update the dedicated GUI bindings whenever the EMEK machine catalog changes");

        String menuTypes = Files.readString(MENU_TYPES);
        String provider = Files.readString(PROVIDER);
        String screens = Files.readString(SCREENS).replace("\r\n", "\n");
        for (GuiBinding binding : BINDINGS) {
            assertTrue(menuTypes.contains("MekanismTileContainer<" + binding.tileClass() + ">> "
                            + binding.menuField()),
                    binding.identity() + " menu holder is not typed to its actual ME tile");
            assertTrue(menuTypes.contains("register.registerMenu(\"" + binding.menuName()
                            + "\", () -> MekanismContainerType.tile(" + binding.tileClass() + ".class"),
                    binding.identity() + " does not register a dedicated tile-compatible menu");
            assertTrue(provider.contains("case " + binding.identity()
                            + " -> EvolvedMekanismMachineMenuTypes." + binding.menuField() + ";"),
                    binding.identity() + " does not resolve to its dedicated menu");
            assertTrue(screens.contains("EvolvedMekanismMachineMenuTypes." + binding.menuField() + ".get(),\n"
                            + "                (MenuScreens.ScreenConstructor) (menu, inv, title) -> new "
                            + binding.guiClass() + "("),
                    binding.identity() + " menu is not bound to the source mod's matching GUI");
        }
    }

    private record GuiBinding(String identity, String menuField, String menuName, String tileClass,
            String guiClass) {
    }
}
