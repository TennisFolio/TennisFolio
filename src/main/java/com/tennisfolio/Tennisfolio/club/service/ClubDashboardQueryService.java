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
            YearMonth yearMonth,
            ClubDashboardMemberFilter memberFilter,
            int page
    ) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        YearMonth requestedMonth = requireSelectableMonth(yearMonth);
        ClubDashboardPeriod period = new ClubDashboardPeriod(requestedMonth);
        ClubDashboardMonthlyData dashboardData = dashboardQueryRepository.findMonthlyDashboardData(
                club.getId(),
                period.getFromDateTime(),
                period.getToDateTime(),
                memberFilter,
                page,
                10
        );
        return toResponse(period, dashboardData);
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
            ClubDashboardMonthlyData dashboardData
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
                dashboardData.getMeetings()
        );
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
