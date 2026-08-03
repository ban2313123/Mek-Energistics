package com.beipuo.mekenergistics.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Guards invariants that are only observable once the game is running, so they are asserted against
 * the sources instead. Each corresponds to a defect that was fixed rather than a style preference.
 */
class MekEnergisticsHardeningTest {

    private static final Path CONFIG =
            Path.of("src/main/java/com/beipuo/mekenergistics/config/MekEnergisticsConfig.java");
    private static final Path MIXIN_PLUGIN =
            Path.of("src/main/java/com/beipuo/mekenergistics/mixin/MekEnergisticsMixinPlugin.java");
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/mekenergistics.mixins.json");
    private static final Path MIXIN_DIR = Path.of("src/main/java/com/beipuo/mekenergistics/mixin");
    private static final Path JADE_PLUGIN =
            Path.of("src/main/java/com/beipuo/mekenergistics/compat/jade/MekEnergisticsJadePlugin.java");

    /**
     * {@code patternPages} sizes the machine inventory, so a client that disagrees with the server
     * mis-indexes every slot interaction. COMMON configs are loaded per-side and never synced;
     * only SERVER configs reach the client.
     */
    @Test
    void structuralConfigValuesLiveOnASyncedSpec() throws IOException {
        String source = Files.readString(CONFIG);
        assertFalse(source.contains("ModConfig.Type.COMMON"),
                "Container-sizing config must not sit on an unsynced COMMON spec");
        assertTrue(source.contains("ModConfig.Type.SERVER"), "Expected a SERVER spec");
        assertTrue(source.contains("ModConfig.Type.CLIENT"), "Expected a CLIENT spec");

        int serverSpec = source.indexOf("private static ModConfigSpec buildServerSpec");
        int clientSpec = source.indexOf("private static ModConfigSpec buildClientSpec");
        assertTrue(serverSpec >= 0 && clientSpec > serverSpec, "Expected separate spec builders");
        for (String synced : new String[] {"patternPages", "preferLocalFe", "preferAppliedFluxNetworkFe"}) {
            int defined = source.indexOf("\"" + synced + "\"");
            assertTrue(defined > serverSpec && defined < clientSpec,
                    synced + " must be defined on the server spec");
        }
        assertTrue(source.indexOf("\"hideJeiMachineVariants\"") > clientSpec,
                "hideJeiMachineVariants is a display-only option and belongs on the client spec");
    }

    /**
     * Some optional mods ship mixin targets in a conditional sub-module, so "the mod is loaded" does
     * not imply "the target class exists". Any mixin aimed at one of those packages must be gated on
     * the target class itself.
     */
    @Test
    void mixinsTargetingConditionalSubmodulesAreGatedOnTheTargetClass() throws IOException {
        String plugin = Files.readString(MIXIN_PLUGIN);
        List<String> ungated = new ArrayList<>();
        List<String> examined = new ArrayList<>();
        try (Stream<Path> files = Files.list(MIXIN_DIR)) {
            for (Path mixin : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String simpleName = mixin.getFileName().toString().replace(".java", "");
                if (simpleName.equals(MIXIN_PLUGIN.getFileName().toString().replace(".java", ""))) {
                    continue; // the plugin names these packages precisely because it gates on them
                }
                if (!targetsConditionalSubmodule(Files.readString(mixin))) {
                    continue;
                }
                examined.add(simpleName);
                Matcher gate = Pattern.compile(
                                Pattern.quote("." + simpleName) + "\",\\s*Gate\\.(mod|target)\\(")
                        .matcher(plugin);
                if (!gate.find() || !"target".equals(gate.group(1))) {
                    ungated.add(simpleName);
                }
            }
        }
        assertFalse(examined.isEmpty(),
                "Found no mixins targeting a conditional sub-module — this check has gone vacuous");
        assertEquals(List.of(), ungated,
                "These mixins target a conditional sub-module but are gated only on a mod id");
    }

    private static boolean targetsConditionalSubmodule(String source) {
        return source.contains("com.jerry.meklm.") || source.contains(".integration.mekaf.");
    }

    @Test
    void mixinConfigListsEachMixinOnce() throws IOException {
        List<String> entries = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([A-Za-z0-9_.]+(?:Accessor|Mixin))\"")
                .matcher(Files.readString(MIXIN_CONFIG));
        while (matcher.find()) {
            entries.add(matcher.group(1));
        }
        assertFalse(entries.isEmpty(), "Expected the mixin config to list mixins");

        Set<String> seen = new HashSet<>();
        List<String> duplicates = entries.stream().filter(entry -> !seen.add(entry)).toList();
        assertEquals(List.of(), duplicates, "Duplicate mixin entries");
    }

    /**
     * Injections into an optional mod must not be mandatory. These target third-party internals --
     * ExtendedAE's {@code ContainerRenamer.setter} is a private static helper -- so an upstream
     * rename is a normal event. With the default {@code require = 1} that becomes a startup crash;
     * with {@code require = 0} the feature quietly stops working, which is the right trade for an
     * optional enhancement.
     */
    @Test
    void injectionsIntoOptionalModsTolerateAMissingTarget() throws IOException {
        List<String> mandatory = new ArrayList<>();
        for (String subPackage : new String[] {"extendedae", "dataenergistics", "omnisequence"}) {
            Path dir = MIXIN_DIR.resolve(subPackage);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> files = Files.list(dir)) {
                for (Path mixin : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                    String source = Files.readString(mixin);
                    Matcher injection = Pattern.compile("@(Inject|ModifyArg|ModifyVariable|Redirect)\\(")
                            .matcher(source);
                    while (injection.find()) {
                        String body = annotationBody(source, injection.end() - 1);
                        if (!body.contains("require = 0")) {
                            mandatory.add(mixin.getFileName() + " @" + injection.group(1));
                        }
                    }
                }
            }
        }
        assertEquals(List.of(), mandatory,
                "Injections into optional mods must pass require = 0 so a moved target degrades instead of crashing");
    }

    /**
     * Annotation body from its opening parenthesis to the matching close. Parentheses inside string
     * literals are skipped -- mixin method descriptors are full of them, e.g.
     * {@code "canRename(Ljava/lang/Object;)Z"}.
     */
    private static String annotationBody(String source, int openParen) {
        int depth = 0;
        boolean inString = false;
        for (int i = openParen; i < source.length(); i++) {
            char c = source.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            switch (c) {
                case '"' -> inString = true;
                case '(' -> depth++;
                case ')' -> {
                    if (--depth == 0) {
                        return source.substring(openParen + 1, i);
                    }
                }
                default -> { }
            }
        }
        throw new AssertionError("Unbalanced annotation starting at offset " + openParen);
    }

    /**
     * Registering against BlockEntity/Block makes Jade run these providers for every block in the
     * game just to fail an instanceof check.
     */
    @Test
    void jadeProvidersAreRegisteredAgainstOwnTypes() throws IOException {
        String source = Files.readString(JADE_PLUGIN);
        assertFalse(source.contains("BlockEntity.class"),
                "Jade data provider must not be registered against every block entity");
        assertFalse(source.contains(", Block.class"),
                "Jade component provider must not be registered against every block");
    }
}
