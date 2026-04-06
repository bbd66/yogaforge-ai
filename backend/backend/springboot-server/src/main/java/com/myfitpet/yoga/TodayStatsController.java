package com.myfitpet.yoga;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class TodayStatsController {

    private final ExerciseSessionRepository exerciseSessionRepository;

    public TodayStatsController(ExerciseSessionRepository exerciseSessionRepository) {
        this.exerciseSessionRepository = exerciseSessionRepository;
    }

    @GetMapping("/today-data")
    public ResponseEntity<?> getTodayData(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "UNAUTHORIZED"));
        }
        Long userId = (Long) auth.getPrincipal();

        LocalDate today = LocalDate.now();
        OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        OffsetDateTime endOfDay = startOfDay.plusDays(1);

        var sessions = exerciseSessionRepository.findAll();

        int totalMinutes = 0;
        int totalCalories = 0;

        for (ExerciseSession s : sessions) {
            if (!userId.equals(s.getUserId())) continue;
            OffsetDateTime st = s.getStartTime();
            if (st == null) continue;
            if (st.isBefore(startOfDay) || !st.isBefore(endOfDay)) continue;
            if (s.getDurationSeconds() != null) {
                totalMinutes += s.getDurationSeconds() / 60;
            }
            if (s.getCalories() != null) {
                totalCalories += s.getCalories();
            }
        }

        int steps = 0; // 暂时没有设备步数，先返回 0

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "steps", steps,
                        "calories", totalCalories,
                        "minutes", totalMinutes
                )
        ));
    }
}
