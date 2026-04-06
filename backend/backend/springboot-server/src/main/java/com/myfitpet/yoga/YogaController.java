package com.myfitpet.yoga;

import com.myfitpet.pose.PoseModelClient;
import com.myfitpet.pose.dto.PoseEvaluateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/yoga/session")
public class YogaController {
    private final PoseModelClient poseClient;
    private final YogaSessionService yogaService;
    private final RewardService rewardService;
    private final com.myfitpet.pet.PetAccountService petAccountService;

    public YogaController(PoseModelClient poseClient,
                          YogaSessionService yogaService,
                          RewardService rewardService,
                          com.myfitpet.pet.PetAccountService petAccountService) {
        this.poseClient = poseClient;
        this.yogaService = yogaService;
        this.rewardService = rewardService;
        this.petAccountService = petAccountService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(Authentication auth, @RequestBody Map<String, Object> req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        String expectedPose = (String) req.getOrDefault("expectedPose", "");
        Map<String, Object> resp = yogaService.startSession(userId, expectedPose);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/frame")
    public ResponseEntity<?> frame(Authentication auth, @RequestBody Map<String, Object> req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String sessionId = (String) req.get("sessionId");
        String imageBase64 = (String) req.get("imageBase64");
        if (sessionId == null || imageBase64 == null) {
            return ResponseEntity.badRequest().body("MISSING_SESSION_OR_IMAGE");
        }
        PoseEvaluateResponse eval = poseClient.evaluateBase64(imageBase64, false);
        Map<String, Object> resp = yogaService.onFrame(sessionId, eval);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop(Authentication auth, @RequestBody Map<String, Object> req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        String sessionId = (String) req.get("sessionId");
        if (sessionId == null) {
            return ResponseEntity.badRequest().body("MISSING_SESSION_ID");
        }
        Map<String, Object> sessionSummary = yogaService.stopSession(sessionId);
        int calories = ((Number)sessionSummary.getOrDefault("calories", 0)).intValue();
        int poseCount = ((Number)sessionSummary.getOrDefault("poseCount", 0)).intValue();
        double quality = ((Number)sessionSummary.getOrDefault("avgQuality", 0.0)).doubleValue();
        int durationSeconds = ((Number)sessionSummary.getOrDefault("durationSeconds", 0)).intValue();
        RewardService.Reward reward = rewardService.compute(calories, poseCount, quality, durationSeconds);
        Map<String,Object> petAccount = petAccountService.applyRewardForUser(userId, reward.getExpGain(), reward.getCoinsGain());
        Map<String,Object> out = new HashMap<>();
        out.put("session", sessionSummary);
        out.put("reward", Map.of("coins", reward.getCoinsGain(), "exp", reward.getExpGain()));
        out.put("petAccount", petAccount);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> metrics(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        Map<String, Object> m = yogaService.getUserMetrics(userId);
        return ResponseEntity.ok(m);
    }
}