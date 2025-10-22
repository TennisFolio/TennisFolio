package com.tennisfolio.Tennisfolio.common;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RoundType {
    FINAL("Final", "final", "결승"),
    SEMIFINALS("Semifinals", "semifinals", "준결승"),
    QUARTERFINALS("Quarterfinals", "quarterfinals", "8강"),
    ROUND16("Round of 16", "round-of-16", "16강"),
    ROUND32("Round of 32", "round-of-32", "32강"),
    Round64("Round of 64", "round-of-64", "64강"),
    ROUND128("Round of 128", "round-of-128", "128강"),
    R128("R128", "r128", "128강"),
    UNKNOWN("UNKNOWN", "UNKNOWN", "미정");

    private final String name;
    private final String slug;
    private final String krName;

    RoundType(String name, String slug, String krName) {
        this.name = name;
        this.slug = slug;
        this.krName = krName;
    }

    public static RoundType fromName(String name) {
        if(name == null || name.isBlank()) return UNKNOWN;
        return Arrays.stream(values())
                .filter(type -> type.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("RoundType not found for name: " + name));
    }

    public static RoundType fromSlug(String slug){
        if(slug == null || slug.isBlank()) return UNKNOWN;
        return Arrays.stream(values())
                .filter(type -> type.slug.equalsIgnoreCase(slug))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("RoundType not found for slug: " + slug));
    }
}
