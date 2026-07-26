package com.beipuo.mekenergistics.registry;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MachineTranslationTest {
    @Test
    void everyMachineVariantHasEnglishAndChineseNames() {
        for (String locale : new String[] {"en_us", "zh_cn"}) {
            JsonObject translations = loadLanguage(locale);
            for (MeMekanismMachine machine : MeMekanismMachine.values()) {
                if (!machine.hasMeVariant()) {
                    continue;
                }
                String id = machine.registryName();
                assertTrue(translations.has("block.mekenergistics." + id),
                        () -> locale + " is missing block name for " + id);
                assertTrue(translations.has("container.mekenergistics." + id),
                        () -> locale + " is missing container name for " + id);
            }
        }
    }

    private static JsonObject loadLanguage(String locale) {
        String path = "/assets/mekenergistics/lang/" + locale + ".json";
        var stream = MachineTranslationTest.class.getResourceAsStream(path);
        assertNotNull(stream, "Missing language resource " + path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
