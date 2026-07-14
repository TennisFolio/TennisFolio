package com.tennisfolio.Tennisfolio.club.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_club_member")
@Getter
@NoArgsConstructor
public class ClubMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUB_MEMBER_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLUB_ID", nullable = false)
    private Club club;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false)
    private ClubMemberRole role;

    @Column(name = "SKILL_NOTE")
    private String skillNote;

    @Column(name = "CONTACT_MEMO")
    private String contactMemo;

    @Column(name = "MEMO")
    private String memo;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;

    public ClubMember(
            Club club,
            Long userId,
            String name,
            Gender gender,
            ClubMemberRole role,
            String skillNote,
            String contactMemo,
            String memo
    ) {
        this.club = club;
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.role = role;
        this.skillNote = skillNote;
        this.contactMemo = contactMemo;
        this.memo = memo;
    }

    public void update(
            String name,
            Gender gender,
            ClubMemberRole role,
            String skillNote,
            String contactMemo,
            String memo
    ) {
        this.name = name;
        this.gender = gender;
        this.role = role;
        this.skillNote = skillNote;
        this.contactMemo = contactMemo;
        this.memo = memo;
    }

    public void deactivate() {
        this.active = false;
    }
}
