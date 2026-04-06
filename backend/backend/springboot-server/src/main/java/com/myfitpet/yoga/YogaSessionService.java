package com.myfitpet.yoga;

import com.myfitpet.pose.dto.PoseEvaluateResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class YogaSessionService {
    static class SessionState {
        String sessionId;
        Long userId;
        String expectedPose;
        long startEpoch;
        int poseCount;
        double qualitySum;
        int frameCount;
    }

    private final Map<String, SessionState> sessions = new HashMap<>();
    private final UserMetricsRepository userMetricsRepository;

    public YogaSessionService(UserMetricsRepository userMetricsRepository) {
        this.userMetricsRepository = userMetricsRepository;
    }

    public Map<String, Object> startSession(Long userId, String expectedPose) {
        String sid = UUID.randomUUID().toString();
        SessionState st = new SessionState();
        st.sessionId = sid;
        st.userId = userId;
        st.expectedPose = expectedPose;
        st.startEpoch = Instant.now().getEpochSecond();
        sessions.put(sid, st);
        return Map.of("sessionId", sid, "expectedPose", expectedPose);
    }

    public Map<String, Object> onFrame(String sessionId, PoseEvaluateResponse eval) {
        SessionState st = sessions.get(sessionId);
        if (st == null) {
            return Map.of("error", "SESSION_NOT_FOUND");
        }
        st.frameCount++;
        if (eval != null && eval.isRecognized()) {
            st.poseCount++;
            st.qualitySum += eval.getQualityScore();
        }
        double avgQuality = st.frameCount > 0 ? st.qualitySum / st.frameCount : 0.0;
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("recognized", eval != null && eval.isRecognized());
        resp.put("templatePose", eval != null ? eval.getTemplatePose() : null);
        resp.put("qualityScore", eval != null ? eval.getQualityScore() : 0.0);
        resp.put("poseCount", st.poseCount);
        resp.put("avgQuality", avgQuality);
        return resp;
    }

    public Map<String, Object> stopSession(String sessionId) {
        SessionState st = sessions.remove(sessionId);
        if (st == null) {
            return Map.of("error", "SESSION_NOT_FOUND");
        }
        long duration = Instant.now().getEpochSecond() - st.startEpoch;
        double avgQuality = st.frameCount > 0 ? st.qualitySum / st.frameCount : 0.0;

        // Reward policy (simple): coins and exp based on quality & duration
        int coins = (int) Math.min(200, Math.floor((duration / 60.0) * (st.poseCount + avgQuality) * 0.5));
        int exp = (int) Math.min(500, Math.floor((duration) * 0.1 + st.poseCount * 1.0 + avgQuality));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("durationSeconds", duration);
        summary.put("poseCount", st.poseCount);
        summary.put("avgQuality", avgQuality);
        summary.put("coins", coins);
        summary.put("exp", exp);

        // 累加到 user_metrics（这里暂时只统计次数和总时长，卡路里通过手动上报接口累加）
        userMetricsRepository.findById(st.userId)
                .ifPresentOrElse(m -> {
                    m.setTotalSessions(m.getTotalSessions() + 1);
                    m.setTotalYogaSeconds(m.getTotalYogaSeconds() + duration);
                    userMetricsRepository.save(m);
                }, () -> {
                    UserMetrics m = new UserMetrics();
                    m.setUserId(st.userId);
                    m.setTotalSessions(1);
                    m.setTotalYogaSeconds(duration);
                    userMetricsRepository.save(m);
                });

        return summary;
    }

    public Map<String, Object> getUserMetrics(Long userId) {
        return userMetricsRepository.findById(userId)
            .map(m -> Map.<String, Object>of(
                "userId", m.getUserId(),
                "totalSessions", m.getTotalSessions(),
                "totalCalories", m.getTotalCalories(),
                "totalYogaSeconds", m.getTotalYogaSeconds()
            ))
            .orElseGet(() -> Map.of(
                "userId", userId,
                "totalSessions", 0,
                "totalCalories", 0,
                "totalYogaSeconds", 0
            ));
    }
}