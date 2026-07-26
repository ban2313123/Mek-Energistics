package com.beipuo.mekenergistics.compat.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * {@code MekanismMachineProvider.registerMachine} ends in a fallback that builds the generic
 * {@code MeMekanismMachineBlockEntity}. No machine reaches it: every Mekanism machine either has an
 * explicit case or is caught by one of the two guards above it.
 *
 * <p>That is worth pinning rather than leaving as a comment. The fallback is 845 lines of code that
 * has never run, so it has never been exercised in game -- a change to {@code slotLayout()} or
 * {@code hasAdvancedChemicalInput()} that quietly makes it reachable would put an untested code path
 * into production. If this test fails, either restore the invariant or deliberately decide the
 * fallback is now live and test it properly.
 */
class MekanismMachineFallbackTest {

    private static final Path PROVIDER =
            Path.of("src/main/java/com/beipuo/mekenergistics/compat/provider/MekanismMachineProvider.java");

    @Test
    void noMekanismMachineReachesTheGenericFallback() throws IOException {
        Set<String> explicit = explicitCases("registerMachine", "registerFactory");
        assertFalse(explicit.isEmpty(), "Failed to parse the registerMachine cases");

        List<String> unhandled = new ArrayList<>();
        for (MeMekanismMachine machine : mekanismMachines()) {
            if (explicit.contains(machine.name()) || caughtByAGuard(machine)) {
                continue;
            }
            unhandled.add(machine.name());
        }
        assertEquals(List.of(), unhandled,
                "These machines now fall through to MeMekanismMachineBlockEntity, which has never run");
    }

    /** The two guards standing in front of the fallback, mirrored from the provider. */
    private static boolean caughtByAGuard(MeMekanismMachine machine) {
        return (machine.slotLayout() == MeMekanismMachine.SlotLayout.SINGLE_ITEM && machine.hasRecipeLogic())
                || machine.hasAdvancedChemicalInput();
    }

    /**
     * Guards against the test going vacuous: if every machine gained an explicit case, the guards
     * above would stop being exercised and this test would pass without proving anything.
     */
    @Test
    void theGuardsAreWhatKeepSomeMachinesOffTheFallback() throws IOException {
        Set<String> explicit = explicitCases("registerMachine", "registerFactory");
        List<String> guardedOnly = mekanismMachines().stream()
                .filter(machine -> !explicit.contains(machine.name()))
                .map(MeMekanismMachine::name)
                .toList();
        assertFalse(guardedOnly.isEmpty(),
                "No machine relies on the guards any more -- the fallback analysis needs revisiting");
        for (String name : guardedOnly) {
            assertTrue(caughtByAGuard(MeMekanismMachine.valueOf(name)), name);
        }
    }

    private static List<MeMekanismMachine> mekanismMachines() {
        // Intrinsic metadata only: availability filtering needs a running game.
        return java.util.Arrays.stream(MeMekanismMachine.values())
                .filter(machine -> machine.provider() == CompatMod.MEKANISM)
                .filter(machine -> machine.machineKind() == CompatMachineKind.MACHINE)
                .collect(Collectors.toList());
    }

    /**
     * Case labels between the two method declarations. Anchored on the declarations rather than the
     * bare names, which also appear as call sites earlier in the file.
     */
    private static Set<String> explicitCases(String from, String until) throws IOException {
        String source = Files.readString(PROVIDER);
        int start = source.indexOf(declarationOf(from));
        int end = source.indexOf(declarationOf(until));
        assertTrue(start >= 0 && end > start, "Could not locate " + from + " in the provider");

        Set<String> cases = new java.util.LinkedHashSet<>();
        Matcher matcher = Pattern.compile("case ([A-Z][A-Z0-9_]*) ->").matcher(source.substring(start, end));
        while (matcher.find()) {
            cases.add(matcher.group(1));
        }
        return cases;
    }

    private static String declarationOf(String methodName) {
        return "TileEntityMekanism> " + methodName + "(";
    }
}
