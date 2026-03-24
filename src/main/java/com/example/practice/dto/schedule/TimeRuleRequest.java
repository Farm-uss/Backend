// src/main/java/com/example/practice/dto/schedule/TimeRuleRequest.java
package com.example.practice.dto.schedule;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class TimeRuleRequest {
    private LocalTime executeTime;
    private List<DayOfWeek> daysOfWeek;
    private Integer durationMinutes;
}
