# Changelog

## 2.0.0-beta

### Change

- Rebuilt ME machine integration around a shared AE support and I/O adapter layer, replacing duplicated lifecycle, pattern, input, output, and smart-batch implementations across Mekanism and supported addons.
- Changed pattern delivery to route actual AE item, chemical, and fluid keys transactionally into declared machine ports without selecting a path from the Mekanism recipe type. Failed deliveries now roll back instead of leaving partial inputs.
- Migrated native Mekanism, Mekanism: MoreMachine, Mekanism Extras, Evolved Mekanism, and Evolved Mekanism Extras machines and factories to the shared input routing and actual-output collection model.
- Centralized optional machine compatibility, registration, client setup, generated resources, factory tier graphs, and installer routes in the compatibility catalog.
- Added ME versions of the Mekanism: MoreMachine Large Rotary Condensentrator, Large Solar Neutron Activator, Large Electrolytic Separator, Large Chemical Infuser, and Large Antiprotonic Nucleosynthesizer.
- Matched the five large machines' upstream models, baked transforms, collision and selection bounds, renderers, menus, recipes, loot, and JEI integration without adding Mekanism side-configuration pages.
- Added independent item, chemical, and fluid output-to-AE controls to the shared pattern window, including support for actual fluid and chemical byproducts.
- Updated the machine adaptation guide for the catalog-driven support and I/O adapter architecture.

### Fix

- Fixed ME nodes failing to reconnect after chunk unloads, world reloads, or reuse of the same block entity instance by recreating destroyed managed nodes from retained state.
- Fixed large Mekanism: MoreMachine nodes so cables can connect through the center of any horizontal face without proxying one node into itself, and deferred large-node setup until the block entity is fully constructed.
- Fixed encoded patterns not being republished reliably after loading, while preserving existing pattern slots, priorities, terminal names, output modes, and pending smart-pattern work.
- Fixed pattern-terminal node access and restored optional Data Energistics interaction after network and world reloads.
- Fixed smart-pattern capacity checks for upgraded input slots, guarded extra-slot limit probing, and kept failed multi-input transactions atomic.
- Fixed ME Metallurgic Infuser conversion patterns so convertible items can enter the extra slot, and corrected pattern-slot quick-move behavior.
- Fixed large-machine item models, lighting and occlusion behavior, dedicated menu resolution, and the Large Antiprotonic Nucleosynthesizer renderer path.

## 1.0.7

### Fix

- Fixed a client crash when showing details for ME factory item tooltips if Mekanism reports no known item containers for the factory stack.

## 1.0.5

### Change

- Added EvolvedMekanism machine compatibility for ME Solidification Chamber, ME Thermalizer, and ME Chemical Mixer.
- Added smart pattern multiplication and faster pattern processing for ME machines and factories.
- Added safer shared AE output, pattern insertion, and network-energy helpers across Mekanism, Mekanism Extras, Mekanism: MoreMachine, EvolvedMekanism, and EvolvedMekanismExtras machines.
- Added a CurseForge publish workflow for GitHub release based publishing.
- Improved ME pattern-machine menus, quick move behavior, and configurable tile screens.

### Fix

- Fixed recipe AE support initialization order for regular recipe machines, chemical machines, Mekanism: MoreMachine machines, and compat factories.
- Fixed malformed encoded pattern handling so invalid third-party patterns are skipped safely instead of crashing the machine integration path.
- Fixed AE-backed energy container construction order and recipe energy usage wrappers.
- Fixed optional EvolvedMekanism, EvolvedMekanismExtras, Mekanism Extras, and Mekanism: MoreMachine factory support delegates.
- Fixed client setup registration on the mod event bus.

## 1.0.0

### Change

- Added smart pattern multiplication for ME pattern slots, so large autocrafting requests can place repeated patterns more conveniently when the machine has room.
- Improved ME machine items to look and read more like their Mekanism counterparts, including colored machine names and clearer item tooltips.
- Improved ME machine registration and item behavior consistency across Mekanism, Mekanism: MoreMachine, Mekanism Extras, and Evolved Mekanism Extras integrations.
- Reorganized the machine adaptation guide for pack makers and future ME machine support.

### Fix

- Fixed ME machine wrench dismantling so machine data, upgrades, side configuration, energy, inventory contents, and installed AE patterns are preserved correctly.
- Fixed ME Factory Installer upgrades across Mekanism Extras and Evolved Mekanism Extras factory chains.
- Fixed ME machine item tooltips and stored-data display by backing item behavior with the expected capabilities.
- Fixed ME Isotopic Centrifuge and ME Centrifuging Factory item icons being too large in inventories.
- Fixed ME centrifuging factory item lighting so their inventory icons match the original factory style more closely.
- Fixed ExtendedAE renaming support so quartz cutting knives can open the renaming screen on ME machines when ExtendedAE is installed.
- Fixed renamed ME machine drops so they restore the machine name through the normal item name instead of saving a separate pattern-terminal name.

## 0.0.14-beta

- Fixed Mekanism: MoreMachine CNC Stamper pattern insertion so non-consumed mold items can stay preloaded in the extra slot while AE patterns only provide consumed input items.
- Applied the same non-consumed mold handling to MekMM and Mekanism Extras stamping factories.
- Improved shared AE pattern insertion helpers for single-item inputs with required extra slots.
- Removed unsupported ME variants for Mekanism utility/configuration machines that do not need AE pattern support.
- Verified the update with Gradle build.

## 0.0.13-beta

- Adapted Mekanism Extras and EvolvedMekanismExtras factory installers so terminal VME ME factories can enter the matching extra factory chains.
- Kept cross-chain installer upgrades gated to the first extra tier, avoiding skips from lower EvolvedMekanism factory tiers.
- Verified the update with Gradle compile.

## 0.0.12-beta

- Added EvolvedMekanism and EvolvedMekanismExtras factory compatibility, including recipes, models, loot tables, lang entries, and EME Extra factory GUI support.
- Added the EvolvedMekanism Iglee Library dependency and updated runtime dependencies for the new compatibility chain.
- Fixed ME Factory Installer behavior so reusing it on existing ME machines does not downgrade them to ME Basic Factories.
- Matched Mekanism's installer interaction behavior by removing the client-side right-click use animation from ME factory installers.
- Matched EvolvedMekanism factory upgrade support for ME Evolved factories while keeping stack upgrades limited to factories whose source mod supports them.
- Added the machine adaptation guide docs.
- Verified the update with Gradle build.

## 0.0.11-beta

- Fixed transparent/missing adjacent block faces next to ME machines, including the ME Metallurgic Infuser case reported with AE2 Additions wireless transceivers.
- Restored Mekanism's original custom block shapes for ME machines and factories so adjacent blocks cull faces correctly.
- Applied the same shape fix across Mekanism, Mekanism Extras factories, and Mekanism: MoreMachine shape-sensitive machines.
- Verified the update with Gradle build.

## 0.0.10-beta

- Fixed ME machine dismantling so wrench removal keeps Mekanism machine data and drops installed AE patterns instead of deleting them.
- Fixed ME Factory Installer upgrades so installed upgrades, machine data, side configuration, energy, and AE patterns are preserved during conversion.
- Fixed Isotopic Centrifuge upgrade paths, including ME Isotopic Centrifuge to ME Basic Centrifuging Factory and Mekanism: MoreMachine centrifuging factories.
- Updated ME centrifuging factory models to use this mod's redesigned local factory indicator bars.
- Fixed machine light occlusion so ME machines, factories, and tall machines match Mekanism's original lighting behavior.
- Verified the update with Gradle build.

## 0.0.9-beta

- Fixed AE-network energy usage for Mekanism machines so recipe execution can consume AE power without displaying network power as stored FE.
- Added AE-aware recipe energy handling for Mekanism: MoreMachine base machines, regular factories, and advanced factories.
- Added AE-aware recipe energy handling for Mekanism Extras regular factories, MoreMachine-derived factories, and advanced factories.
- Kept original Mekanism/compat-mod recipe logic intact while wrapping only the cached recipe energy view.
- Verified the update with `compileJava`.

## 0.0.8-beta

- Added ME Planting Station and ME Replicator base machines for Mekanism: MoreMachine, so the ME Factory Installer maps the original base machines to their matching ME base versions instead of a basic factory.
- Improved ME Factory Installer target resolution and blocked it from remapping machines that are already ME machines.
- Added bounding-block handling for the ME Factory Installer when converting or interacting with tall machines.
- Fixed MekMM planting/replicating base machine AE support initialization during block entity construction.
- Refactored machine and factory registration so tile registration and AE grid-node capability registration share centralized descriptors.
- Matched the ME Planting Station item display transform with the ME planting factory item models.
