package com.tennisfolio.Tennisfolio.club.entity;

import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClubMemberTest {

    @Test
    void 휴면으로_변경하면_휴면일을_기록한다() {
        ClubMember member = createMember();
        LocalDate suspendedAt = LocalDate.of(2026, 9, 23);

        member.changeStatus(ClubMemberStatus.SUSPENDED, suspendedAt);

        assertThat(member.getMemberStatus()).isEqualTo(ClubMemberStatus.SUSPENDED);
        assertThat(member.getSuspendedAt()).isEqualTo(suspendedAt);
        assertThat(member.getLeftAt()).isNull();
    }

    @Test
    void 탈퇴에서_활성으로_복구하면_탈퇴일을_지운다() {
        ClubMember member = createMember();
        member.changeStatus(ClubMemberStatus.WITHDRAWN, LocalDate.of(2026, 9, 22));

        member.changeStatus(ClubMemberStatus.ACTIVE, LocalDate.of(2026, 9, 23));

        assertThat(member.getMemberStatus()).isEqualTo(ClubMemberStatus.ACTIVE);
        assertThat(member.getLeftAt()).isNull();
        assertThat(member.getSuspendedAt()).isNull();
    }

    private ClubMember createMember() {
        return new ClubMember(
                null,
                null,
                "테니스 회원",
                Gender.MALE,
                ClubMemberRole.MEMBER,
                null,
                null,
                null
        );
    }
}
