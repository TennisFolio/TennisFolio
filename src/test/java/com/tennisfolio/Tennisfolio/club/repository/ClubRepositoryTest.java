package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.config.QuerydslConfig;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class ClubRepositoryTest {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Test
    void saveClub_persistsPublicIdNameDescriptionAndCreator() {
        Club club = new Club(
                "Seocho Tennis Crew",
                "Weeknight doubles club",
                10L
        );

        Club saved = clubRepository.saveAndFlush(club);

        Club found = clubRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPublicId()).isNotBlank();
        assertThat(found.getName()).isEqualTo("Seocho Tennis Crew");
        assertThat(found.getDescription()).isEqualTo("Weeknight doubles club");
        assertThat(found.getCreatedByUserId()).isEqualTo(10L);
        assertThat(found.getDeletedAt()).isNull();
    }

    @Test
    void saveClubMember_persistsAdminMemberLinkedToUser() {
        Club club = clubRepository.saveAndFlush(new Club(
                "Seocho Tennis Crew",
                "Weeknight doubles club",
                10L
        ));
        ClubMember member = new ClubMember(
                club,
                10L,
                "Kim Admin",
                Gender.MALE,
                ClubMemberRole.ADMIN,
                "advanced",
                "open chat",
                "creator"
        );

        ClubMember saved = clubMemberRepository.saveAndFlush(member);

        ClubMember found = clubMemberRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getClub().getId()).isEqualTo(club.getId());
        assertThat(found.getUserId()).isEqualTo(10L);
        assertThat(found.getName()).isEqualTo("Kim Admin");
        assertThat(found.getGender()).isEqualTo(Gender.MALE);
        assertThat(found.getRole()).isEqualTo(ClubMemberRole.ADMIN);
        assertThat(found.getSkillNote()).isEqualTo("advanced");
        assertThat(found.getContactMemo()).isEqualTo("open chat");
        assertThat(found.getMemo()).isEqualTo("creator");
        assertThat(found.isActive()).isTrue();
    }

    @Test
    void findActiveClubMemberByName_excludesInactiveMembers() {
        Club club = clubRepository.saveAndFlush(new Club(
                "Seocho Tennis Crew",
                "Weeknight doubles club",
                10L
        ));
        ClubMember activeMember = clubMemberRepository.saveAndFlush(new ClubMember(
                club,
                null,
                "Park Member",
                Gender.FEMALE,
                ClubMemberRole.MEMBER,
                "intermediate",
                null,
                null
        ));
        ClubMember inactiveMember = clubMemberRepository.saveAndFlush(new ClubMember(
                club,
                null,
                "Lee Former",
                Gender.MALE,
                ClubMemberRole.MEMBER,
                null,
                null,
                null
        ));
        inactiveMember.deactivate();
        clubMemberRepository.saveAndFlush(inactiveMember);

        List<ClubMember> activeMembers =
                clubMemberRepository.findByClubAndActiveTrueOrderByIdAsc(club);

        assertThat(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Park Member")).isTrue();
        assertThat(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Lee Former")).isFalse();
        assertThat(activeMembers)
                .extracting(ClubMember::getId)
                .containsExactly(activeMember.getId());
    }
}
