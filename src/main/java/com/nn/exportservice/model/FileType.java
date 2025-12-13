package com.nn.exportservice.model;

/**
 * Enum representing different file types with their corresponding prefix patterns
 */
public enum FileType {
    REDEMPTION("redemption"),
    OUTPAY("outpay"),
    OWN_AND_BEN("own_and_ben");

    private final String prefixPattern;

    FileType(String prefixPattern) {
        this.prefixPattern = prefixPattern;
    }

    public String getPrefixPattern() {
        return prefixPattern;
    }
}
