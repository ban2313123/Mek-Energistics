package com.beipuo.mekenergistics.architecture;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.crafting.PatternDetailsHelper;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * MekMM, Mekanism Extras, Evolved Mekanism and Evolved Mekanism Extras are {@code compileOnly}
 * dependencies, so a player may be running with none of them installed. Classes that load
 * unconditionally must therefore never reference those mods' classes, nor the {@code compat}
 * sub-packages that adapt them — the adapters are reached only through {@code compat.provider},
 * which resolves them reflectively once the mod is confirmed present.
 *
 * <p>Adapters and mod-specific screens do link those classes directly, on purpose; they are only
 * ever loaded behind a provider. The rules below therefore pin down the always-loaded entry points
 * rather than whole packages.
 *
 * <p>These rules read compiled bytecode rather than source text, so they see the references that
 * actually cause {@code NoClassDefFoundError} and are unaffected by renames, formatting, or a
 * package name appearing in a comment.
 */
class CompatBoundaryRulesTest {
    private static final JavaClasses MOD_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.beipuo.mekenergistics");

    /**
     * Classes on a load path that runs even when no optional mod is installed. Registration and
     * catalog code reaches optional machines only through {@code CompatMachineProviders}.
     */
    private static final List<String> ALWAYS_LOADED = List.of(
            "com.beipuo.mekenergistics.registry.ModBlocks",
            "com.beipuo.mekenergistics.registry.ModBlockEntities",
            "com.beipuo.mekenergistics.registry.ModBlockTypes",
            "com.beipuo.mekenergistics.registry.ModMenuTypes",
            "com.beipuo.mekenergistics.block.MeMekanismMachineBlock",
            "com.beipuo.mekenergistics.item.MeInstallerTargetResolver",
            "com.beipuo.mekenergistics.item.MeInstallerUpgradeHandler",
            "com.beipuo.mekenergistics.client.ClientSetup",
            "com.beipuo.mekenergistics.blockentity.MeMekanismMachineBlockEntity",
            "com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog");

    private static final String[] OPTIONAL_ADAPTER_PACKAGES = {
            "..compat.mekmm..", "..compat.meke..", "..compat.eme..", "..compat.omnisequence..",
            "..compat.thunderbolt..",
    };
    private static final String[] OPTIONAL_MOD_PACKAGES = {
            "com.jerry..", "fr.iglee42..", "io.github.masyumero..", "com.atir.molecularmanipulator..",
            "com.moakiee.thunderbolt..",
    };

    /**
     * Renaming or moving one of the entry points would otherwise make the rules below silently stop
     * protecting it, since a name that matches nothing produces no violations.
     */
    @Test
    void everyGuardedEntryPointStillExists() {
        for (String className : ALWAYS_LOADED) {
            assertTrue(MOD_CLASSES.stream().anyMatch(imported -> imported.getName().equals(className)),
                    () -> className + " is guarded by the boundary rules but no longer exists under that name");
        }
    }

    @Test
    void alwaysLoadedEntryPointsNeverTouchOptionalModClasses() {
        noClasses()
                .that().haveNameMatching(guardedNamePattern())
                .should().dependOnClassesThat().resideInAnyPackage(OPTIONAL_MOD_PACKAGES)
                .because("a player without the optional mod installed would hit NoClassDefFoundError")
                .check(MOD_CLASSES);
    }

    @Test
    void alwaysLoadedEntryPointsNeverTouchTheOptionalModAdapters() {
        noClasses()
                .that().haveNameMatching(guardedNamePattern())
                .should().dependOnClassesThat().resideInAnyPackage(OPTIONAL_ADAPTER_PACKAGES)
                .because("the adapters are reachable only through compat.provider, which loads them reflectively")
                .check(MOD_CLASSES);
    }

    @Test
    void theCatalogStaysPureDataAndNeverReachesIntoTheProviders() {
        noClasses()
                .that().resideInAPackage("..compat.catalog..")
                .should().dependOnClassesThat().resideInAnyPackage("..compat.provider..")
                .because("the catalog describes machines; wiring them up is the providers' job")
                .check(MOD_CLASSES);

        noClasses()
                .that().resideInAPackage("..compat.catalog..")
                .should().dependOnClassesThat().resideInAnyPackage(OPTIONAL_MOD_PACKAGES)
                .check(MOD_CLASSES);
    }

    @Test
    void runtimeModPresenceChecksStayInTheTwoProbesThatOwnThem() {
        noClasses()
                .that().haveNameNotMatching(".*OptionalCompatClasses")
                .and().haveNameNotMatching(".*MekEnergisticsMixinPlugin")
                .should().dependOnClassesThat().haveFullyQualifiedName("net.neoforged.fml.ModList")
                .because("one probe for the running game and one for mixin bootstrap is all we want to maintain")
                .check(MOD_CLASSES);
    }

    @Test
    void encodedPatternsAreDecodedOnlyThroughTheGuardedHelper() {
        noClasses()
                .that().haveNameNotMatching(".*MePatternDecodeHelper")
                .should().callMethodWhere(target(owner(type(PatternDetailsHelper.class)))
                        .and(target(name("decodePattern"))))
                .because("a malformed pattern must fail soft, and the helper is where that is handled")
                .check(MOD_CLASSES);
    }

    @Test
    void concreteMachinesLeaveKeyHandlingToTheRouter() {
        noClasses()
                .that().haveSimpleNameEndingWith("BlockEntity")
                .should().dependOnClassesThat().haveFullyQualifiedName("appeng.api.stacks.AEItemKey")
                .orShould().dependOnClassesThat().haveFullyQualifiedName("appeng.api.stacks.AEFluidKey")
                .orShould().dependOnClassesThat().haveFullyQualifiedName("me.ramidzkh.mekae2.ae2.MekanismKey")
                .because("a machine declares its ports; turning AE keys into inserts is the router's job")
                .check(MOD_CLASSES);
    }

    @Test
    void onlyTheCatalogEnumeratesEveryMachine() {
        noClasses()
                // javac desugars an enum switch into a synthetic class whose initializer calls
                // values() to build a jump table. That is not the enumeration we care about.
                .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
                .and().haveNameNotMatching(".*CompatMachineCatalog")
                .and().haveNameNotMatching(".*MeMekanismMachine")
                .should().callMethod(MeMekanismMachine.class, "values")
                .because("the catalog is the single place that decides which machines exist")
                .check(MOD_CLASSES);
    }

    private static String guardedNamePattern() {
        return String.join("|", ALWAYS_LOADED).replace(".", "\\.");
    }
}
