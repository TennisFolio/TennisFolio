package com.tennisfolio.Tennisfolio.club.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubMemberStatusTest {

    @Test
    void from_returnsMatchingStatus() {
        assertThat(ClubMemberStatus.from("SUSPENDED")).isEqualTo(ClubMemberStatus.SUSPENDED);
    }

    @Test
    void from_rejectsUnknownStatus() {
        assertThatThrownBy(() -> ClubMemberStatus.from("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 상태 값이 올바르지 않습니다.");
    }
}
