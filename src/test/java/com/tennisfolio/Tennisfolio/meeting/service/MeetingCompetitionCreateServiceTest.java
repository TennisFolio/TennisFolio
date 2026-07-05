package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCreationResult;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingCompetitionCreateServiceTest {

    @Mock
    MeetingRepository meetingRepository;

    @Mock
    MeetingAttendanceRepository attendanceRepository;

    @Mock
    CompetitionCommandService competitionCommandService;

    @Mock
    CompetitionRepository competitionRepository;

    MeetingCompetitionCreateService service;

    @BeforeEach
    void setUp() {
        service = new MeetingCompetitionCreateService(
                meetingRepository,
                attendanceRepository,
                competitionCommandService,
                competitionRepository
        );
    }

    @Test
    void createCompetition_createsFixedScheduleFromAttendingParticipantsAndConnectsCompetition() {
        Meeting meeting = meeting(null);
        Competition competition = competition(200L, "competition-public-id", 10L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(List.of(
                attendance(meeting, "Alex Kim", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Ben Park", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Chris Lee", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Dana Choi", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Eun Seo", Gender.FEMALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Fay Shin", Gender.FEMALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Gina Han", Gender.FEMALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Hana Jung", Gender.FEMALE, AttendanceStatus.ATTENDING)
        ));
        when(competitionCommandService.createCompetitionResult(any(CompetitionCreateRequest.class), eq(10L)))
                .thenReturn(new CompetitionCreationResult(competition, "competition-token"));

        MeetingCompetitionCreateResponse response = service.createCompetition("meeting-public-id", 10L);

        ArgumentCaptor<CompetitionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(CompetitionCreateRequest.class);
        verify(competitionCommandService).createCompetitionResult(requestCaptor.capture(), eq(10L));
        CompetitionCreateRequest request = requestCaptor.getValue();
        assertThat(request.getMode()).isEqualTo("FIXED_SCHEDULE");
        assertThat(request.getCompetitionName()).isEqualTo("Saturday doubles");
        assertThat(request.getMaleCount()).isEqualTo(4);
        assertThat(request.getFemaleCount()).isEqualTo(4);
        assertThat(request.getCourtCount()).isEqualTo(2);
        assertThat(request.getTotalGames()).isEqualTo(6);
        assertThat(request.getMalePlayerNames()).containsExactly("Alex Kim", "Ben Park", "Chris Lee", "Dana Choi");
        assertThat(request.getFemalePlayerNames()).containsExactly("Eun Seo", "Fay Shin", "Gina Han", "Hana Jung");
        assertThat(response.getPublicId()).isEqualTo("competition-public-id");
        assertThat(meeting.getCompetitionId()).isEqualTo(200L);
        verify(competitionRepository, never()).findByPublicIdAndDeletedAtIsNull("competition-public-id");
    }

    @Test
    void createCompetition_passesSameGenderDoublesOnlyChoiceToCompetitionRequest() {
        Meeting meeting = meeting(null);
        Competition competition = competition(200L, "competition-public-id", 10L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(List.of(
                attendance(meeting, "Alex Kim", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Ben Park", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Chris Lee", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Dana Choi", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Eun Seo", Gender.FEMALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Fay Shin", Gender.FEMALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Gina Han", Gender.FEMALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Hana Jung", Gender.FEMALE, AttendanceStatus.ATTENDING)
        ));
        when(competitionCommandService.createCompetitionResult(any(CompetitionCreateRequest.class), eq(10L)))
                .thenReturn(new CompetitionCreationResult(competition, "competition-token"));

        service.createCompetition(
                "meeting-public-id",
                10L,
                new MeetingCompetitionCreateRequest(true)
        );

        ArgumentCaptor<CompetitionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(CompetitionCreateRequest.class);
        verify(competitionCommandService).createCompetitionResult(requestCaptor.capture(), eq(10L));
        assertThat(requestCaptor.getValue().isSameGenderDoublesOnly()).isTrue();
    }

    @Test
    void createCompetition_rejectsNonOwner() {
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCompetition("meeting-public-id", 20L))
                .isInstanceOf(NotFoundException.class);
        verify(competitionCommandService, never()).createCompetition(
                any(CompetitionCreateRequest.class),
                anyLong()
        );
        verify(competitionCommandService, never()).createCompetitionResult(
                any(CompetitionCreateRequest.class),
                anyLong()
        );
    }

    @Test
    void createCompetition_rejectsAlreadyConnectedMeeting() {
        Meeting meeting = meeting(200L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.createCompetition("meeting-public-id", 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
        verify(competitionCommandService, never()).createCompetition(
                any(CompetitionCreateRequest.class),
                anyLong()
        );
        verify(competitionCommandService, never()).createCompetitionResult(
                any(CompetitionCreateRequest.class),
                anyLong()
        );
    }

    @Test
    void createCompetition_rejectsWhenAttendingParticipantsAreLessThanCourtCapacity() {
        Meeting meeting = meeting(null);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(List.of(
                attendance(meeting, "Alex Kim", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Ben Park", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Chris Lee", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Dana Choi", Gender.MALE, AttendanceStatus.ATTENDING),
                attendance(meeting, "Eun Seo", Gender.FEMALE, AttendanceStatus.ATTENDING)
        ));

        assertThatThrownBy(() -> service.createCompetition("meeting-public-id", 10L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(responseStatusException.getReason())
                            .isEqualTo("코트 수에 비해 참석자가 적습니다. 참석자를 늘리거나 코트 수를 줄여주세요.");
                });
        verify(competitionCommandService, never()).createCompetitionResult(
                any(CompetitionCreateRequest.class),
                anyLong()
        );
    }

    @Test
    void deleteCompetition_softDeletesConnectedCompetitionAndClearsMeetingConnection() {
        Meeting meeting = meeting(200L);
        Competition competition = competition(200L, "competition-public-id", 10L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        when(competitionRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(200L, 10L))
                .thenReturn(Optional.of(competition));

        service.deleteCompetition("meeting-public-id", 10L);

        assertThat(competition.isDeleted()).isTrue();
        assertThat(meeting.getCompetitionId()).isNull();
    }

    @Test
    void deleteCompetition_rejectsMeetingWithoutCompetition() {
        Meeting meeting = meeting(null);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.deleteCompetition("meeting-public-id", 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private static Meeting meeting(Long competitionId) {
        Meeting meeting = new Meeting(
                10L,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6
        );
        ReflectionTestUtils.setField(meeting, "publicId", "meeting-public-id");
        if (competitionId != null) {
            meeting.connectCompetition(competitionId);
        }
        return meeting;
    }

    private static MeetingAttendance attendance(
            Meeting meeting,
            String participantName,
            Gender gender,
            AttendanceStatus status
    ) {
        return new MeetingAttendance(meeting, participantName, gender, status);
    }

    private static Competition competition(Long id, String publicId, Long ownerUserId) {
        Competition competition = new Competition(
                "Saturday doubles",
                4,
                4,
                2,
                3,
                136L,
                Competition.CompetitionMode.FIXED_SCHEDULE,
                ownerUserId
        );
        ReflectionTestUtils.setField(competition, "id", id);
        ReflectionTestUtils.setField(competition, "publicId", publicId);
        return competition;
    }
}
