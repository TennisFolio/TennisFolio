package com.tennisfolio.Tennisfolio.club.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tennisfolio.Tennisfolio.club.entity.QClubMember;
import com.tennisfolio.Tennisfolio.club.entity.QClubSkillTier;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingParticipantType;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.entity.QMeeting;
import com.tennisfolio.Tennisfolio.meeting.entity.QMeetingAttendance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class ClubDashboardQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ClubDashboardQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public DashboardData findDashboardData(Long clubId, LocalDateTime from, LocalDateTime to) {
        long activeMemberCount = countActiveMembers(clubId);
        List<GenderCount> genderCounts = findGenderCounts(clubId);
        List<SkillTierCount> skillTierCounts = findSkillTierCounts(clubId);
        long unclassifiedSkillMemberCount = countUnclassifiedSkillMembers(clubId);
        long meetingCount = countMeetings(clubId, from, to, false);
        long cancelledMeetingCount = countMeetings(clubId, from, to, true);
        long memberAttendanceCount = countMemberAttendances(clubId, from, to);
        long guestAttendanceCount = countGuestAttendances(clubId, from, to);
        long participantCount = countParticipatingMembers(clubId, from, to);
        List<RecentMeeting> recentMeetings = findRecentMeetings(clubId, from, to);

        return new DashboardData(
                activeMemberCount,
                genderCounts,
                skillTierCounts,
                unclassifiedSkillMemberCount,
                meetingCount,
                cancelledMeetingCount,
                memberAttendanceCount,
                guestAttendanceCount,
                participantCount,
                recentMeetings
        );
    }

    private long countActiveMembers(Long clubId) {
        QClubMember clubMember = QClubMember.clubMember;
        return zeroIfNull(queryFactory
                .select(clubMember.count())
                .from(clubMember)
                .where(clubMember.club.id.eq(clubId), clubMember.active.isTrue())
                .fetchOne());
    }

    private List<GenderCount> findGenderCounts(Long clubId) {
        QClubMember clubMember = QClubMember.clubMember;
        List<Tuple> results = queryFactory
                .select(clubMember.gender, clubMember.count())
                .from(clubMember)
                .where(clubMember.club.id.eq(clubId), clubMember.active.isTrue())
                .groupBy(clubMember.gender)
                .fetch();
        Map<Gender, Long> countsByGender = results.stream()
                .collect(Collectors.toMap(
                        result -> result.get(clubMember.gender),
                        result -> zeroIfNull(result.get(clubMember.count()))
                ));

        return List.of(
                new GenderCount(Gender.MALE, countsByGender.getOrDefault(Gender.MALE, 0L)),
                new GenderCount(Gender.FEMALE, countsByGender.getOrDefault(Gender.FEMALE, 0L))
        );
    }

    private List<SkillTierCount> findSkillTierCounts(Long clubId) {
        QClubSkillTier skillTier = QClubSkillTier.clubSkillTier;
        QClubMember clubMember = QClubMember.clubMember;
        List<Tuple> results = queryFactory
                .select(skillTier.id, skillTier.name, skillTier.level, clubMember.count())
                .from(skillTier)
                .leftJoin(clubMember).on(
                        clubMember.club.id.eq(clubId),
                        clubMember.active.isTrue(),
                        clubMember.skillTier.eq(skillTier)
                )
                .where(skillTier.club.id.eq(clubId))
                .groupBy(skillTier.id, skillTier.name, skillTier.level)
                .orderBy(skillTier.level.asc(), skillTier.id.asc())
                .fetch();

        return results.stream()
                .map(result -> new SkillTierCount(
                        result.get(skillTier.id),
                        result.get(skillTier.name),
                        result.get(skillTier.level),
                        zeroIfNull(result.get(clubMember.count()))
                ))
                .toList();
    }

    private long countUnclassifiedSkillMembers(Long clubId) {
        QClubMember clubMember = QClubMember.clubMember;
        return zeroIfNull(queryFactory
                .select(clubMember.count())
                .from(clubMember)
                .where(clubMember.club.id.eq(clubId), clubMember.active.isTrue(), clubMember.skillTier.isNull())
                .fetchOne());
    }

    private long countMeetings(Long clubId, LocalDateTime from, LocalDateTime to, boolean cancelled) {
        QMeeting meeting = QMeeting.meeting;
        BooleanExpression statusCondition = cancelled
                ? meeting.status.eq(MeetingStatus.CANCELLED)
                : meeting.status.ne(MeetingStatus.CANCELLED);
        return zeroIfNull(queryFactory
                .select(meeting.count())
                .from(meeting)
                .where(eligibleMeetingCondition(meeting, clubId, from, to), statusCondition)
                .fetchOne());
    }

    private long countMemberAttendances(Long clubId, LocalDateTime from, LocalDateTime to) {
        QMeeting meeting = QMeeting.meeting;
        QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
        return countAttendances(clubId, from, to, memberAttendanceCondition(attendance));
    }

    private long countGuestAttendances(Long clubId, LocalDateTime from, LocalDateTime to) {
        QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
        return countAttendances(clubId, from, to, guestAttendanceCondition(attendance));
    }

    private long countAttendances(
            Long clubId,
            LocalDateTime from,
            LocalDateTime to,
            BooleanExpression participantCondition
    ) {
        QMeeting meeting = QMeeting.meeting;
        QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
        return zeroIfNull(queryFactory
                .select(attendance.count())
                .from(attendance)
                .join(attendance.meeting, meeting)
                .where(
                        eligibleNonCancelledMeetingCondition(meeting, clubId, from, to),
                        attendingCondition(attendance),
                        participantCondition
                )
                .fetchOne());
    }

    private long countParticipatingMembers(Long clubId, LocalDateTime from, LocalDateTime to) {
        QMeeting meeting = QMeeting.meeting;
        QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
        return zeroIfNull(queryFactory
                .select(attendance.clubMemberId.countDistinct())
                .from(attendance)
                .join(attendance.meeting, meeting)
                .where(
                        eligibleNonCancelledMeetingCondition(meeting, clubId, from, to),
                        attendingCondition(attendance),
                        memberAttendanceCondition(attendance)
                )
                .fetchOne());
    }

    private List<RecentMeeting> findRecentMeetings(Long clubId, LocalDateTime from, LocalDateTime to) {
        QMeeting meeting = QMeeting.meeting;
        QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
        BooleanExpression memberAttendance = attendingCondition(attendance).and(memberAttendanceCondition(attendance));
        BooleanExpression guestAttendance = attendingCondition(attendance).and(guestAttendanceCondition(attendance));
        NumberExpression<Long> memberAttendanceCount =
                new CaseBuilder().when(memberAttendance).then(1L).otherwise(0L).sum();
        NumberExpression<Long> guestAttendanceCount =
                new CaseBuilder().when(guestAttendance).then(1L).otherwise(0L).sum();

        List<Tuple> results = queryFactory
                .select(
                        meeting.publicId,
                        meeting.startAt,
                        meeting.title,
                        meeting.status,
                        memberAttendanceCount,
                        guestAttendanceCount
                )
                .from(meeting)
                .leftJoin(attendance).on(attendance.meeting.eq(meeting), attendance.deletedAt.isNull())
                .where(eligibleNonCancelledMeetingCondition(meeting, clubId, from, to))
                .groupBy(meeting.id, meeting.publicId, meeting.startAt, meeting.title, meeting.status)
                .orderBy(meeting.startAt.desc(), meeting.id.desc())
                .limit(3)
                .fetch();

        List<RecentMeeting> recentMeetings = new ArrayList<>();
        for (Tuple result : results) {
            recentMeetings.add(new RecentMeeting(
                    result.get(meeting.publicId),
                    result.get(meeting.startAt),
                    result.get(meeting.title),
                    result.get(meeting.status),
                    zeroIfNull(result.get(memberAttendanceCount)),
                    zeroIfNull(result.get(guestAttendanceCount))
            ));
        }
        return recentMeetings;
    }

    private BooleanExpression eligibleMeetingCondition(QMeeting meeting, Long clubId, LocalDateTime from, LocalDateTime to) {
        return meeting.clubId.eq(clubId)
                .and(meeting.deletedAt.isNull())
                .and(meeting.startAt.between(from, to));
    }

    private BooleanExpression eligibleNonCancelledMeetingCondition(
            QMeeting meeting,
            Long clubId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return eligibleMeetingCondition(meeting, clubId, from, to)
                .and(meeting.status.ne(MeetingStatus.CANCELLED));
    }

    private BooleanExpression attendingCondition(QMeetingAttendance attendance) {
        return attendance.deletedAt.isNull().and(attendance.attendanceStatus.eq(AttendanceStatus.ATTENDING));
    }

    private BooleanExpression memberAttendanceCondition(QMeetingAttendance attendance) {
        return attendance.participantType.eq(MeetingParticipantType.CLUB_MEMBER)
                .and(attendance.clubMemberId.isNotNull());
    }

    private BooleanExpression guestAttendanceCondition(QMeetingAttendance attendance) {
        return attendance.participantType.ne(MeetingParticipantType.CLUB_MEMBER)
                .or(attendance.clubMemberId.isNull());
    }

    private long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }

    @Getter
    @RequiredArgsConstructor
    public static class DashboardData {
        private final long activeMemberCount;
        private final List<GenderCount> genderCounts;
        private final List<SkillTierCount> skillTierCounts;
        private final long unclassifiedSkillMemberCount;
        private final long meetingCount;
        private final long cancelledMeetingCount;
        private final long memberAttendanceCount;
        private final long guestAttendanceCount;
        private final long participantCount;
        private final List<RecentMeeting> recentMeetings;
    }

    @Getter
    @RequiredArgsConstructor
    public static class GenderCount {
        private final Gender gender;
        private final long count;
    }

    @Getter
    @RequiredArgsConstructor
    public static class SkillTierCount {
        private final Long skillTierId;
        private final String name;
        private final int level;
        private final long count;
    }

    @Getter
    @RequiredArgsConstructor
    public static class RecentMeeting {
        private final String publicId;
        private final LocalDateTime startAt;
        private final String title;
        private final MeetingStatus status;
        private final long memberAttendanceCount;
        private final long guestAttendanceCount;
    }
}
