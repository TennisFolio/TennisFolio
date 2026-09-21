package com.tennisfolio.Tennisfolio.club.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardData;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardGenderCount;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberActivity;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberFilter;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberPage;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMonthlyData;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMonthlyMeeting;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardRecentMeeting;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardSkillTierCount;
import com.tennisfolio.Tennisfolio.club.entity.QClubMember;
import com.tennisfolio.Tennisfolio.club.entity.QClubSkillTier;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingParticipantType;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.entity.QMeeting;
import com.tennisfolio.Tennisfolio.meeting.entity.QMeetingAttendance;
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

    public ClubDashboardData findDashboardData(Long clubId, LocalDateTime from, LocalDateTime to) {
        long activeMemberCount = countActiveMembers(clubId);
        List<ClubDashboardGenderCount> genderCounts = findGenderCounts(clubId);
        List<ClubDashboardSkillTierCount> skillTierCounts = findSkillTierCounts(clubId);
        long unclassifiedSkillMemberCount = countUnclassifiedSkillMembers(clubId);
        long meetingCount = countMeetings(clubId, from, to, false);
        long cancelledMeetingCount = countMeetings(clubId, from, to, true);
        long memberAttendanceCount = countMemberAttendances(clubId, from, to);
        long guestAttendanceCount = countGuestAttendances(clubId, from, to);
        long participantCount = countParticipatingMembers(clubId, from, to);
        List<ClubDashboardRecentMeeting> recentMeetings = findRecentMeetings(clubId, from, to);

        return new ClubDashboardData(
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

    public ClubDashboardMonthlyData findMonthlyDashboardData(
            Long clubId,
            LocalDateTime from,
            LocalDateTime to,
            ClubDashboardMemberFilter memberFilter,
            int page,
            int size
    ) {
        return new ClubDashboardMonthlyData(
                countActiveMembers(clubId),
                countMeetings(clubId, from, to, false),
                countMemberAttendances(clubId, from, to),
                countGuestAttendances(clubId, from, to),
                countParticipatingMembers(clubId, from, to),
                findMemberActivities(clubId, from, to, memberFilter, page, size),
                findMonthlyMeetings(clubId, from, to)
        );
    }

    private ClubDashboardMemberPage findMemberActivities(
            Long clubId,
            LocalDateTime from,
            LocalDateTime to,
            ClubDashboardMemberFilter memberFilter,
            int page,
            int size
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        QClubMember clubMember = QClubMember.clubMember;
        QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
        QMeeting meeting = QMeeting.meeting;
        List<Tuple> results = queryFactory
                .select(clubMember.id, clubMember.name, attendance.count())
                .from(clubMember)
                .leftJoin(attendance).on(
                        attendance.clubMemberId.eq(clubMember.id),
                        attendingCondition(attendance),
                        memberAttendanceCondition(attendance),
                        attendance.meeting.id.in(
                                JPAExpressions.select(meeting.id)
                                        .from(meeting)
                                        .where(eligibleNonCancelledMeetingCondition(meeting, clubId, from, to))
                        )
                )
                .where(clubMember.club.id.eq(clubId), clubMember.active.isTrue())
                .groupBy(clubMember.id, clubMember.name)
                .orderBy(attendance.count().desc(), clubMember.name.asc(), clubMember.id.asc())
                .fetch();

        List<ClubDashboardMemberActivity> filteredMembers = results.stream()
                .map(result -> new ClubDashboardMemberActivity(
                        result.get(clubMember.id),
                        result.get(clubMember.name),
                        zeroIfNull(result.get(attendance.count()))
                ))
                .filter(member -> matchesMemberFilter(member, memberFilter))
                .toList();
        long totalElements = filteredMembers.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / normalizedSize);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filteredMembers.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filteredMembers.size());

        return new ClubDashboardMemberPage(
                filteredMembers.subList(fromIndex, toIndex),
                normalizedPage,
                normalizedSize,
                totalElements,
                totalPages
        );
    }

    private boolean matchesMemberFilter(ClubDashboardMemberActivity member, ClubDashboardMemberFilter memberFilter) {
        return switch (memberFilter) {
            case ALL -> true;
            case PARTICIPATED -> member.getAttendanceCount() > 0;
            case NOT_PARTICIPATED -> member.getAttendanceCount() == 0;
        };
    }

    private List<ClubDashboardMonthlyMeeting> findMonthlyMeetings(Long clubId, LocalDateTime from, LocalDateTime to) {
        QMeeting meeting = QMeeting.meeting;
        QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
        BooleanExpression memberAttendance = attendingCondition(attendance).and(memberAttendanceCondition(attendance));
        BooleanExpression guestAttendance = attendingCondition(attendance).and(guestAttendanceCondition(attendance));
        NumberExpression<Long> memberAttendanceCount = new CaseBuilder().when(memberAttendance).then(1L).otherwise(0L).sum();
        NumberExpression<Long> guestAttendanceCount = new CaseBuilder().when(guestAttendance).then(1L).otherwise(0L).sum();

        return queryFactory
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
                .where(eligibleMeetingCondition(meeting, clubId, from, to))
                .groupBy(meeting.id, meeting.publicId, meeting.startAt, meeting.title, meeting.status)
                .orderBy(meeting.startAt.asc(), meeting.id.asc())
                .fetch()
                .stream()
                .map(result -> new ClubDashboardMonthlyMeeting(
                        result.get(meeting.publicId),
                        result.get(meeting.startAt),
                        result.get(meeting.title),
                        result.get(meeting.status),
                        zeroIfNull(result.get(memberAttendanceCount)),
                        zeroIfNull(result.get(guestAttendanceCount))
                ))
                .toList();
    }

    private long countActiveMembers(Long clubId) {
        QClubMember clubMember = QClubMember.clubMember;
        return zeroIfNull(queryFactory
                .select(clubMember.count())
                .from(clubMember)
                .where(clubMember.club.id.eq(clubId), clubMember.active.isTrue())
                .fetchOne());
    }

    private List<ClubDashboardGenderCount> findGenderCounts(Long clubId) {
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
                new ClubDashboardGenderCount(Gender.MALE, countsByGender.getOrDefault(Gender.MALE, 0L)),
                new ClubDashboardGenderCount(Gender.FEMALE, countsByGender.getOrDefault(Gender.FEMALE, 0L))
        );
    }

    private List<ClubDashboardSkillTierCount> findSkillTierCounts(Long clubId) {
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
                .map(result -> new ClubDashboardSkillTierCount(
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

    private List<ClubDashboardRecentMeeting> findRecentMeetings(Long clubId, LocalDateTime from, LocalDateTime to) {
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

        List<ClubDashboardRecentMeeting> recentMeetings = new ArrayList<>();
        for (Tuple result : results) {
            recentMeetings.add(new ClubDashboardRecentMeeting(
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

}
