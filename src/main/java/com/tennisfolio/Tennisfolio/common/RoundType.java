package com.tennisfolio.Tennisfolio.common;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RoundType {
    FINAL("Final", "final", "결승"),
    SEMIFINALS("Semifinals", "semifinals", "준결승"),
    QUARTERFINALS("Quarterfinals", "quarterfinals", "8강"),
    ROUND_16("Round of 16", "round-of-16", "16강"),
    ROUND_32("Round of 32", "round-of-32", "32강"),
    Round_64("Round of 64", "round-of-64", "64강"),
    ROUND_128("Round of 128", "round-of-128", "128강"),
    R128("R128", "r128", "128강"),
    QUALIFICATION("Qualification", "qualification", "예선"),
    QUALIFICATION_1("Qualification Round 1", "qualification-round-1", "예선 1회전"),
    QUALIFICATION_2("Qualification Round 2", "qualification-round-2", "예선 2회전"),
    QUALIFICATION_FINAL("Qualification Final", "qualification-final", "예선 결승전"),

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
                .orElseGet(() -> UNKNOWN);
    }

    public static RoundType fromSlug(String slug){
        if(slug == null || slug.isBlank()) return UNKNOWN;
        return Arrays.stream(values())
                .filter(type -> type.slug.equalsIgnoreCase(slug))
                .findFirst()
                .orElseGet(() -> UNKNOWN);
    }
}
