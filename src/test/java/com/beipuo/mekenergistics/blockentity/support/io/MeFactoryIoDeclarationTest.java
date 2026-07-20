package com.beipuo.mekenergistics.blockentity.support.io;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class MeFactoryIoDeclarationTest {
    private static final Path FACTORIES = Path.of("src/main/java/com/beipuo/mekenergistics/blockentity");

    @Test
    void factoryOwnersDoNotExposeTheSameChemicalTankAsInputAndOutput() throws IOException {
        try (Stream<Path> paths = Files.walk(FACTORIES)) {
            for (Path path : paths.filter(file -> file.toString().endsWith("FactoryBlockEntity.java")).toList()) {
                String source = Files.readString(path);
                String input = returnedExpression(source, "Input");
                String output = returnedExpression(source, "Output");
                if (input != null && output != null && input.toLowerCase(java.util.Locale.ROOT).contains("tank")) {
                    assertNotEquals(input, output,
                            () -> path + " declares the same chemical tank as an AE input and output");
                }
            }
        }
    }

    private static String returnedExpression(String source, String role) {
        Pattern method = Pattern.compile(
                "meChemical" + role + "Tanks\\(\\)\\s*\\{\\s*return\\s+([^;]+);\\s*\\}",
                Pattern.DOTALL);
        Matcher matcher = method.matcher(source);
        return matcher.find() ? matcher.group(1).replaceAll("\\s+", "") : null;
    }
}
