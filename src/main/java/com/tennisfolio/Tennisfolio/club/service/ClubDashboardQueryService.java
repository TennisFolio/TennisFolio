package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardData;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardPeriod;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberComposition;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberParticipation;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.repository.ClubDashboardQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

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
    public ClubDashboardResponse getDashboard(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        ClubDashboardPeriod period = calculateDashboardPeriod();
        ClubDashboardData dashboardData = dashboardQueryRepository.findDashboardData(
                club.getId(),
                period.getFromDateTime(),
                period.getToDateTime()
        );
        return toResponse(period, dashboardData);
    }

    private ClubDashboardPeriod calculateDashboardPeriod() {
        LocalDate to = LocalDate.now(clock);
        LocalDate from = to.minusDays(29);
        return new ClubDashboardPeriod(from, to);
    }

    private ClubDashboardResponse toResponse(
            ClubDashboardPeriod period,
            ClubDashboardData dashboardData
    ) {
        return new ClubDashboardResponse(
                period,
                dashboardData.getActiveMemberCount(),
                dashboardData.getMeetingCount(),
                dashboardData.getCancelledMeetingCount(),
                new ClubDashboardMemberParticipation(
                        dashboardData.getParticipantCount(),
                        calculateParticipationRate(dashboardData),
                        dashboardData.getMemberAttendanceCount(),
                        calculateInactiveParticipantCount(dashboardData)
                ),
                dashboardData.getGuestAttendanceCount(),
                calculateAverageAttendancePerMeeting(dashboardData),
                new ClubDashboardMemberComposition(
                        dashboardData.getGenderCounts(),
                        dashboardData.getSkillTierCounts(),
                        dashboardData.getUnclassifiedSkillMemberCount()
                ),
                dashboardData.getRecentMeetings()
        );
    }

    private int calculateParticipationRate(ClubDashboardData dashboardData) {
        if (dashboardData.getActiveMemberCount() == 0) {
            return 0;
        }
        return (int) Math.round(
                (double) dashboardData.getParticipantCount() / dashboardData.getActiveMemberCount() * 100
        );
    }

    private long calculateInactiveParticipantCount(ClubDashboardData dashboardData) {
        return Math.max(0, dashboardData.getActiveMemberCount() - dashboardData.getParticipantCount());
    }

    private double calculateAverageAttendancePerMeeting(ClubDashboardData dashboardData) {
        if (dashboardData.getMeetingCount() == 0) {
            return 0;
        }
        double average = (double) (dashboardData.getMemberAttendanceCount() + dashboardData.getGuestAttendanceCount())
                / dashboardData.getMeetingCount();
        return Math.round(average * 10) / 10.0;
    }

}
