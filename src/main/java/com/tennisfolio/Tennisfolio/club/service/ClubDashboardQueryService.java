package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberFilter;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberGuestRatio;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMonthlyData;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardPeriod;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberParticipation;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.repository.ClubDashboardQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClubDashboardQueryService {

    private final ClubAccessService clubAccessService;
    private final ClubDashboardQueryRepository dashboardQueryRepository;
    private final Clock clock;

    public ClubDashboardQueryService(
            ClubAccessService clubAccessService,
            ClubDashboardQueryRepository dashboardQueryRepository,
            Clock clock
    ) {
        this.clubAccessService = clubAccessService;
        this.dashboardQueryRepository = dashboardQueryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ClubDashboardResponse getDashboard(
            String clubPublicId,
            Long currentUserId,
            YearMonth requestedMonth,
            ClubDashboardMemberFilter memberFilter,
            int page
    ) {
        Club club = clubAccessService.requireOperator(clubPublicId, currentUserId);
        DashboardMonthSelection selection = selectDashboardMonth(club, requestedMonth, memberFilter, page);
        return toResponse(selection.period(), selection.dashboardData(), selection.hasMeetingHistory());
    }

    private DashboardMonthSelection selectDashboardMonth(
            Club club,
            YearMonth requestedMonth,
            ClubDashboardMemberFilter memberFilter,
            int page
    ) {
        if (requestedMonth != null) {
            return dashboardMonthSelection(club.getId(), requireSelectableMonth(requestedMonth), memberFilter, page, true);
        }

        YearMonth currentMonth = YearMonth.from(LocalDate.now(clock));
        DashboardMonthSelection currentSelection = dashboardMonthSelection(
                club.getId(), currentMonth, memberFilter, page, true
        );
        if (currentSelection.dashboardData().getMeetingCount() > 0) {
            return currentSelection;
        }

        LocalDateTime latestMeetingStartAt = dashboardQueryRepository.findLatestActiveMeetingStartAt(
                club.getId(),
                currentSelection.period().getToDateTime()
        );
        if (latestMeetingStartAt == null) {
            return new DashboardMonthSelection(currentSelection.period(), currentSelection.dashboardData(), false);
        }

        YearMonth latestMeetingMonth = YearMonth.from(latestMeetingStartAt);
        return latestMeetingMonth.equals(currentMonth)
                ? currentSelection
                : dashboardMonthSelection(club.getId(), latestMeetingMonth, memberFilter, page, true);
    }

    private DashboardMonthSelection dashboardMonthSelection(
            Long clubId,
            YearMonth yearMonth,
            ClubDashboardMemberFilter memberFilter,
            int page,
            boolean hasMeetingHistory
    ) {
        ClubDashboardPeriod period = new ClubDashboardPeriod(yearMonth);
        ClubDashboardMonthlyData dashboardData = dashboardQueryRepository.findMonthlyDashboardData(
                clubId,
                period.getFromDateTime(),
                period.getToDateTime(),
                memberFilter,
                page,
                10
        );
        return new DashboardMonthSelection(period, dashboardData, hasMeetingHistory);
    }

    private YearMonth requireSelectableMonth(YearMonth yearMonth) {
        YearMonth currentMonth = YearMonth.from(LocalDate.now(clock));
        if (yearMonth.isAfter(currentMonth)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "미래 월은 조회할 수 없습니다.");
        }
        return yearMonth;
    }

    private ClubDashboardResponse toResponse(
            ClubDashboardPeriod period,
            ClubDashboardMonthlyData dashboardData,
            boolean hasMeetingHistory
    ) {
        return new ClubDashboardResponse(
                period,
                dashboardData.getActiveMemberCount(),
                dashboardData.getMeetingCount(),
                new ClubDashboardMemberParticipation(
                        dashboardData.getParticipantCount(),
                        calculateParticipationRate(dashboardData),
                        dashboardData.getMembers()
                ),
                calculateMemberGuestRatio(dashboardData),
                dashboardData.getMeetings(),
                hasMeetingHistory
        );
    }

    private record DashboardMonthSelection(
            ClubDashboardPeriod period,
            ClubDashboardMonthlyData dashboardData,
            boolean hasMeetingHistory
    ) {
    }

    private int calculateParticipationRate(ClubDashboardMonthlyData dashboardData) {
        if (dashboardData.getActiveMemberCount() == 0) {
            return 0;
        }
        return (int) Math.round(
                (double) dashboardData.getParticipantCount() / dashboardData.getActiveMemberCount() * 100
        );
    }

    private ClubDashboardMemberGuestRatio calculateMemberGuestRatio(ClubDashboardMonthlyData dashboardData) {
        long totalAttendanceCount = dashboardData.getMemberAttendanceCount() + dashboardData.getGuestAttendanceCount();
        int memberRate = totalAttendanceCount == 0 ? 0
                : (int) Math.round((double) dashboardData.getMemberAttendanceCount() / totalAttendanceCount * 100);
        return new ClubDashboardMemberGuestRatio(
                dashboardData.getMemberAttendanceCount(),
                dashboardData.getGuestAttendanceCount(),
                memberRate,
                totalAttendanceCount == 0 ? 0 : 100 - memberRate
        );
    }

}
