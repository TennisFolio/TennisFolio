package com.tennisfolio.Tennisfolio.meeting.entity;

import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AttendanceStatusConverter implements AttributeConverter<AttendanceStatus, String> {

    @Override
    public String convertToDatabaseColumn(AttendanceStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AttendanceStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AttendanceStatus.fromValue(dbData);
    }
}
