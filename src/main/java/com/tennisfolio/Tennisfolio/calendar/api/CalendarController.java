package com.tennisfolio.Tennisfolio.calendar.api;

import com.tennisfolio.Tennisfolio.calendar.application.CalendarService;
import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<TournamentCalendarResponse>>> getTournamentCalendar(@RequestParam(value = "month", required = false) String month){

        if(month == null || month.isBlank()){
            return ResponseEntity.badRequest().body(ResponseDTO.error("MISSING_MONTH_PARAM", "month 파라미터는 필수입니다."));
        }

        if(!month.matches("\\d{6}")){
            return ResponseEntity.badRequest().body(ResponseDTO.error("INVALID_MONTH_FORMAT", "month 파라미터는 yyyyMM 형식이어야 합니다."));
        }

        ResponseDTO<List<TournamentCalendarResponse>> res =
                ResponseDTO.success(calendarService.getTournamentCalendar(month));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/detail")
    public ResponseEntity<ResponseDTO<List<MatchScheduleResponse>>> getMatchSchedule(
            @RequestParam(value="date", required =false) String date,
            @RequestParam(value="seasonId") Long seasonId){

        if(seasonId == null || seasonId == 0L){
            return ResponseEntity.badRequest().body(ResponseDTO.error("MISSING_SEASON_ID", "seasonId 파라미터는 필수입니다."));
        }
        if(date == null || date.isBlank()){
            return ResponseEntity.badRequest().body(ResponseDTO.error("MISSING_DATE_PARAM", "date 파라미터는 필수입니다."));
        }

        if(!date.matches("\\d{8}")){
            return ResponseEntity.badRequest().body(ResponseDTO.error("INVALID_DATE_FORMAT", "date 파라미터는 yyyyMMDD 형식이어야 합니다."));
        }

        ResponseDTO<List<MatchScheduleResponse>> res =
                ResponseDTO.success(calendarService.getMatchSchedule(date, seasonId));

        return ResponseEntity.ok(res);
    }

}
