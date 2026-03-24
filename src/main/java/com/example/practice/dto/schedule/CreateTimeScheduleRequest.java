// src/main/java/com/example/practice/dto/schedule/CreateTimeScheduleRequest.java
package com.example.practice.dto.schedule;

import com.example.practice.entity.schedule.ControlSystemType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateTimeScheduleRequest {
    private Long farmId;
    private String name;
    private ControlSystemType controlSystemType;
    private LocalTime executeTime;
    private List<DayOfWeek> daysOfWeek;
    private Integer durationMinutes;
}
