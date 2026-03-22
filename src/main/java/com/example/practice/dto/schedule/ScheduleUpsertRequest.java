// src/main/java/com/example/practice/dto/schedule/ScheduleUpsertRequest.java
package com.example.practice.dto.schedule;

import com.example.practice.entity.schedule.ControlSystemType;
import com.example.practice.entity.schedule.ScheduleType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleUpsertRequest {
    private Long farmId;
    private String name;
    private ControlSystemType controlSystemType;
    private ScheduleType scheduleType;
    private TimeRuleRequest timeRule;
    private ConditionRuleRequest conditionRule;
}
