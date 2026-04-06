package com.myfitpet.yoga;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/yoga/session")
public class ManualMetricsController {

    private final UserMetricsRepository userMetricsRepository;

    public ManualMetricsController(UserMetricsRepository userMetricsRepository) {
        this.userMetricsRepository = userMetricsRepository;
    }

    static class ManualMetricsRequest {
        public int durationSeconds;
        public int calories;
    }

    @PostMapping("/metrics-manual")
    public ResponseEntity<?> addManualMetrics(Authentication auth, @RequestBody ManualMetricsRequest req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();

        int duration = Math.max(0, req.durationSeconds);
        int calories = Math.max(0, req.calories);

        var metricsOpt = userMetricsRepository.findById(userId);
        UserMetrics m = metricsOpt.orElseGet(() -> {
            UserMetrics nm = new UserMetrics();
            nm.setUserId(userId);
            nm.setTotalCalories(0L);
            nm.setTotalSessions(0);
            nm.setTotalYogaSeconds(0L);
            return nm;
        });

        m.setTotalCalories(m.getTotalCalories() + calories);
        m.setTotalSessions(m.getTotalSessions() + 1);
        m.setTotalYogaSeconds(m.getTotalYogaSeconds() + duration);
        userMetricsRepository.save(m);

        return ResponseEntity.ok(Map.of(
                "userId", m.getUserId(),
                "totalSessions", m.getTotalSessions(),
                "totalCalories", m.getTotalCalories(),
                "totalYogaSeconds", m.getTotalYogaSeconds()
        ));
    }
}
