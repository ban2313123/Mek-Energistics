package com.beipuo.mekenergistics.blockentity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

/**
 * Productive Bees Genesis injects {@code IAe2OutputHost} into tiles that also receive our
 * {@link MeSmartCableConnection} contract (its apiary extends {@code TileEntityElectricMachine},
 * which our upgrade mixin makes implement {@link MeUpgradeableMachine}). Both interfaces used to
 * default {@code getCableConnectionType(Direction)} to SMART, so the concrete class inherited two
 * conflicting defaults and failed to load with {@code IncompatibleClassChangeError}. The contract
 * is therefore abstract, and every concrete implementor resolves it with its own class-level
 * method, which wins over any integration default.
 */
class MeSmartCableConnectionContractTest {
    private static final JavaClasses MOD_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.beipuo.mekenergistics");

    private static final Path LIFECYCLE_MIXIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/TileEntityMekanismMeUpgradeLifecycleMixin.java");

    @Test
    void interfaceDeclaresAnAbstractContractInsteadOfADefault() throws NoSuchMethodException {
        Method contract = MeSmartCableConnection.class.getMethod("getCableConnectionType", Direction.class);
        assertEquals(MeSmartCableConnection.class, contract.getDeclaringClass());
        assertFalse(contract.isDefault());
        assertTrue(Modifier.isAbstract(contract.getModifiers()));
    }

    @Test
    void everyConcreteImplementorDeclaresItsOwnOverride() {
        List<JavaClass> implementors = MOD_CLASSES.stream()
                .filter(clazz -> clazz.isAssignableTo(MeSmartCableConnection.class))
                .filter(clazz -> !clazz.isInterface() && !clazz.getModifiers().contains(JavaModifier.ABSTRACT))
                .toList();
        assertFalse(implementors.isEmpty(), "the scan found no concrete implementors to guard");
        for (JavaClass implementor : implementors) {
            boolean explicit = implementor.getAllMethods().stream().anyMatch(method ->
                    method.getName().equals("getCableConnectionType")
                            && method.getOwner().equals(implementor)
                            && method.getRawParameterTypes().size() == 1
                            && method.getRawParameterTypes().get(0).getName().equals(Direction.class.getName())
                            && !method.getModifiers().contains(JavaModifier.ABSTRACT));
            assertTrue(explicit, () -> implementor.getName()
                    + " must declare getCableConnectionType(Direction) explicitly so it never inherits a conflicting interface default");
        }
    }

    @Test
    void thirdPartyTilesGetTheOverrideFromTheMekanismLifecycleMixin() throws IOException {
        String source = Files.readString(LIFECYCLE_MIXIN);
        assertTrue(source.contains("public AECableType getCableConnectionType(Direction side)"));
        assertTrue(source.contains("return AECableType.SMART;"));
    }
}