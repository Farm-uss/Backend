// src/main/java/com/example/practice/entity/schedule/TimeScheduleRule.java
package com.example.practice.entity.schedule;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "time_schedule_rule")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeScheduleRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false, unique = true)
    private AutomationSchedule schedule;

    @Column(name = "execute_time", nullable = false)
    private LocalTime executeTime;

    @Column(name = "days_of_week", nullable = false, length = 100)
    private String daysOfWeek;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    public static TimeScheduleRule create(LocalTime executeTime,
                                          List<DayOfWeek> daysOfWeek,
                                          Integer durationMinutes) {
        return TimeScheduleRule.builder()
                .executeTime(executeTime)
                .daysOfWeek(toStorage(daysOfWeek))
                .durationMinutes(durationMinutes)
                .build();
    }
    public void update(LocalTime executeTime,
                       List<DayOfWeek> daysOfWeek,
                       Integer durationMinutes) {
        this.executeTime = executeTime;
        this.daysOfWeek = toStorage(daysOfWeek);
        this.durationMinutes = durationMinutes;
    }


    void assignSchedule(AutomationSchedule schedule) {
        this.schedule = schedule;
    }

    public List<DayOfWeek> getDayOfWeekValues() {
        if (daysOfWeek == null || daysOfWeek.isBlank()) {
            return List.of();
        }
        return Arrays.stream(daysOfWeek.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }

    private static String toStorage(List<DayOfWeek> daysOfWeek) {
        return daysOfWeek.stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(","));
    }
}
