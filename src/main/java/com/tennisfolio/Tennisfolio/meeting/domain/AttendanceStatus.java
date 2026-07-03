package com.tennisfolio.Tennisfolio.meeting.domain;

public enum AttendanceStatus {
    ATTENDING, NOT_ATTENDING, WAITING;

    public static AttendanceStatus fromValue(String value) {
        if ("MAYBE".equals(value)) {
            return WAITING;
        }
        return AttendanceStatus.valueOf(value);
    }
}
