package com.tennisfolio.Tennisfolio.club.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClubAccountTest {

    @Test
    void 클럽별_표시이름과_역할을_변경한다() {
        ClubAccount account = new ClubAccount(null, 10L, "테니스 김", ClubAccountRole.ADMIN);

        account.updateDisplayName("김테니스");
        account.changeRole(ClubAccountRole.OWNER);

        assertThat(account.getDisplayName()).isEqualTo("김테니스");
        assertThat(account.getRole()).isEqualTo(ClubAccountRole.OWNER);
    }
}
