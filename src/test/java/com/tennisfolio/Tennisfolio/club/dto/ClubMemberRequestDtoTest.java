package com.tennisfolio.Tennisfolio.club.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ClubMemberRequestDtoTest {

    @Test
    void createRequest_exposesJoinedAtWithoutBirthYear() {
        assertThat(fieldNames(ClubMemberCreateRequest.class))
                .contains("joinedAt")
                .doesNotContain("birthYear");
    }

    @Test
    void updateRequest_exposesJoinedAtWithoutBirthYear() {
        assertThat(fieldNames(ClubMemberUpdateRequest.class))
                .contains("joinedAt")
                .doesNotContain("birthYear");
    }

    @Test
    void statusChangeRequest_acceptsOptionalStatusStartDate() {
        assertThat(fieldNames(ClubMemberStatusChangeRequest.class))
                .contains("statusDate");
    }

    private String[] fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .toArray(String[]::new);
    }
}
