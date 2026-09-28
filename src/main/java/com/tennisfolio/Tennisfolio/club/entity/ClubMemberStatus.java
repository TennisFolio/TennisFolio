package com.tennisfolio.Tennisfolio.club.entity;

public enum ClubMemberStatus {
    ACTIVE,
    SUSPENDED,
    WITHDRAWN;

    public static ClubMemberStatus from(String value) {
        try {
            return valueOf(value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("회원 상태 값이 올바르지 않습니다.");
        }
    }
}
