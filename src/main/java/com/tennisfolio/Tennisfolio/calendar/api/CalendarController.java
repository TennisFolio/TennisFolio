package com.tennisfolio.Tennisfolio.calendar.api;

import com.tennisfolio.Tennisfolio.calendar.application.CalendarService;
import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<TournamentCalendarResponse>>> getTournamentCalendar(
            @RequestParam(value = "month", required = false)
            @NotBlank(message="month 파라미터는 필수입니다.")
            @Pattern(regexp= "\\d{6}", message="month 파라미터는 yyyyMM 형식이어야 합니다.") String month){

        ResponseDTO<List<TournamentCalendarResponse>> res =
                ResponseDTO.success(calendarService.getTournamentCalendar(month));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/detail")
    public ResponseEntity<ResponseDTO<List<MatchScheduleResponse>>> getMatchSchedule(
            @RequestParam(value="date")
            @NotBlank(message = "date 파라미터는 필수입니다.")
            @Pattern(regexp= "\\d{8}", message="date 파라미터는 yyyyMMDD 형식이어야 합니다.")
            String date,
            @RequestParam(value="seasonId", required = false)
            Long seasonId){


        ResponseDTO<List<MatchScheduleResponse>> res =
                ResponseDTO.success(calendarService.getMatchSchedule(date, seasonId));

        return ResponseEntity.ok(res);
    }

}
