// src/main/java/com/example/practice/dto/schedule/TimeRuleResponse.java
package com.example.practice.dto.schedule;

import com.example.practice.entity.schedule.TimeScheduleRule;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class TimeRuleResponse {
    private LocalTime executeTime;
    private List<DayOfWeek> daysOfWeek;
    private Integer durationMinutes;

    public static TimeRuleResponse from(TimeScheduleRule rule) {
        if (rule == null) {
            return null;
        }

        return TimeRuleResponse.builder()
                .executeTime(rule.getExecuteTime())
                .daysOfWeek(rule.getDayOfWeekValues())
                .durationMinutes(rule.getDurationMinutes())
                .build();
    }
}
