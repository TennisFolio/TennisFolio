package com.tennisfolio.Tennisfolio.common;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RoundType {
    FINAL("Final", "final"),
    SEMIFINALS("Semifinals", "semifinals"),
    QUARTERFINALS("Quarterfinals", "quarterfinals"),
    ROUND16("Round of 16", "round-of-16"),
    ROUND32("Round of 32", "round-of-32"),
    Round64("Round of 64", "round-of-64"),
    ROUND128("Round of 128", "round-of-128"),
    R128("R128", "r128"),
    UNKNOWN("UNKNOWN", "UNKNOWN");

    private final String name;
    private final String slug;

    RoundType(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public static RoundType fromName(String name) {
        if(name == null || name.isBlank()) return UNKNOWN;
        return Arrays.stream(values())
                .filter(type -> type.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("RoundType not found for name: " + name));
    }
}
