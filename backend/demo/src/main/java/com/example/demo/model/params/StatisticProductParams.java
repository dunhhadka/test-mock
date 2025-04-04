package com.example.demo.model.params;

import com.example.demo.utils.DateFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class StatisticProductParams extends BaseParams {
    private String code;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public StatisticProductParams(Map<String, String> map) {
        super(map);
        this.code = map.get("code");
        this.name = map.get("name");

        LocalDate currentDate = LocalDate.now();

        startTime = LocalDateTime.of(currentDate, LocalTime.MIDNIGHT).minusMonths(1);
        endTime = LocalDateTime.of(currentDate.plusDays(1), LocalTime.MIDNIGHT);

        if (map.get("startTime") != null && map.get("endTime") != null) {
            startTime = DateFormat.toLocalDateTime(map.get("startTime"), true).withHour(0).withMinute(0).minusDays(1)
                    .minusMonths(1);
            endTime = DateFormat.toLocalDateTime(map.get("endTime"), true).withHour(0).withMinute(0);
        }

    }
}
