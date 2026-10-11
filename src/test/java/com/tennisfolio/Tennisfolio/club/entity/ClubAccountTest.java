package com.tennisfolio.Tennisfolio.club.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClubAccountTest {

    @Test
    void 클럽계정은_역할만_변경한다() {
        ClubAccount account = new ClubAccount(null, 10L, ClubAccountRole.ADMIN);

        account.changeRole(ClubAccountRole.OWNER);

        assertThat(account.getRole()).isEqualTo(ClubAccountRole.OWNER);
    }
}
