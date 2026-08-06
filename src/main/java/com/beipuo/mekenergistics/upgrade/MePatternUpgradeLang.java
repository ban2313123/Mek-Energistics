package com.beipuo.mekenergistics.upgrade;

import mekanism.api.text.ILangEntry;

public enum MePatternUpgradeLang implements ILangEntry {
    NAME("upgrade.mekenergistics.me_pattern_provider"),
    DESCRIPTION("upgrade.mekenergistics.me_pattern_provider.description"),
    PASSIVE_NAME("upgrade.mekenergistics.me_passive_crafting"),
    PASSIVE_DESCRIPTION("upgrade.mekenergistics.me_passive_crafting.description");

    private final String translationKey;

    MePatternUpgradeLang(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}
