package com.beipuo.mekenergistics.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Every literal translation key this mod asks for must be one this mod ships.
 *
 * <p>Written after a Jade tooltip was found rendering three of its four states through AE2's The One
 * Probe keys, while this mod's own equivalents sat translated and unused -- so the tooltip read
 * "Device Online" above "AE Network: Booting", and the wording depended on another mod continuing to
 * ship keys for an unrelated integration. Nothing failed, because the borrowed keys happened to
 * resolve.
 */
class OwnTranslationKeyTest {

    private static final Path SOURCE_ROOT = Path.of("src/main/java/com/beipuo/mekenergistics");
    private static final Path EN_US =
            Path.of("src/main/resources/assets/mekenergistics/lang/en_us.json");

    /** Key prefixes this mod owns and must therefore define. */
    private static final Pattern OWN_KEY = Pattern.compile(
            "\"((?:tooltip|gui|config\\.jade\\.plugin_mekenergistics|key|itemGroup|block|item)"
                    + "[A-Za-z0-9_.]*mekenergistics[A-Za-z0-9_.]*)\"");

    @Test
    void everyOwnKeyUsedInCodeIsShipped() throws IOException {
        JsonObject lang = JsonParser.parseString(Files.readString(EN_US)).getAsJsonObject();
        Set<String> used = new LinkedHashSet<>();

        try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
            for (Path source : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String text = Files.readString(source);
                Matcher translatable = Pattern.compile("Component\\.translatable\\(\\s*(\"[^\"]+\")")
                        .matcher(text);
                while (translatable.find()) {
                    Matcher own = OWN_KEY.matcher(translatable.group(1));
                    if (own.matches()) {
                        used.add(own.group(1));
                    }
                }
            }
        }

        assertFalse(used.isEmpty(), "Found no own translation keys -- this check has gone vacuous");
        List<String> missing = used.stream().filter(key -> !lang.has(key)).sorted().toList();
        assertEquals(List.of(), missing, "Translation keys used in code but absent from en_us.json");
    }

    /**
     * Keys belonging to The One Probe have no business in a Jade plugin: this mod does not depend on
     * that mod, and AE2 only ships them for its own TOP integration.
     */
    @Test
    void jadeTooltipsDoNotBorrowTheOneProbeKeys() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(SOURCE_ROOT.resolve("compat/jade"))) {
            for (Path source : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (Files.readString(source).contains("theoneprobe.")) {
                    offenders.add(source.getFileName().toString());
                }
            }
        }
        assertEquals(List.of(), offenders, "Jade providers must use this mod's own translation keys");
    }

    /** A locale that ships a key set must not be missing any of the AE status strings. */
    @Test
    void aeStatusStringsExistInEveryShippedLocale() throws IOException {
        List<String> missing = new ArrayList<>();
        for (String locale : new String[] {"en_us", "zh_cn", "ru_ru"}) {
            Path file = EN_US.resolveSibling(locale + ".json");
            JsonObject lang = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            for (String state : new String[] {"online", "offline", "booting", "missing_channel"}) {
                String key = "tooltip.mekenergistics.ae_status." + state;
                if (!lang.has(key)) {
                    missing.add(locale + ": " + key);
                }
            }
        }
        assertEquals(List.of(), missing, "AE status tooltip strings are incomplete");
    }
}
