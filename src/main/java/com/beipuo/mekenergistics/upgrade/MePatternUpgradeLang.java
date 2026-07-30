package com.beipuo.mekenergistics.upgrade;

import mekanism.api.text.ILangEntry;

public enum MePatternUpgradeLang implements ILangEntry {
    NAME("upgrade.mekenergistics.me_pattern_provider"),
    DESCRIPTION("upgrade.mekenergistics.me_pattern_provider.description");

    private final String translationKey;

    MePatternUpgradeLang(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}
