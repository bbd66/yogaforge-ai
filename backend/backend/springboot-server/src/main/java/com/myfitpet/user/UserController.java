package com.myfitpet.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.myfitpet.dto.UpdateProfileRequest;
import com.myfitpet.yoga.UserMetricsRepository;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
        private final UserMetricsRepository userMetricsRepository;

        public UserController(UserRepository userRepository, UserMetricsRepository userMetricsRepository) {
            this.userRepository = userRepository;
            this.userMetricsRepository = userMetricsRepository;
        }

    // 已移除通用用户列表/创建/按ID查询接口，聚焦自服务型 /me 操作

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) authentication.getPrincipal();
        var opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("UNAUTHORIZED");
        }
        var u = opt.get();
        return ResponseEntity.ok(new MeResponse(u.getId(), u.getEmail(), u.getUsername(), u.getAvatarUrl(), u.getCreatedAt()));
    }

    static class MeResponse {
        private Long id;
        private String email;
        private String username;
        private String avatarUrl;
        private java.time.OffsetDateTime createdAt;

        public MeResponse(Long id, String email, String username, String avatarUrl, java.time.OffsetDateTime createdAt) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getAvatarUrl() { return avatarUrl; }
        public java.time.OffsetDateTime getCreatedAt() { return createdAt; }
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(Authentication authentication,
                                      @Valid @RequestBody UpdateProfileRequest req) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        if (req.getUsername() == null && req.getAvatarUrl() == null
            && req.getEmail() == null /*&& req.getPhone() == null*/) {
            return ResponseEntity.badRequest().body("INVALID_INPUT");
        }
        Long userId = (Long) authentication.getPrincipal();
        var opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("UNAUTHORIZED");
        }
        var u = opt.get();
        if (req.getUsername() != null) {
            String trimmed = req.getUsername().trim();
            if (trimmed.length() < 2 || trimmed.length() > 64) {
                return ResponseEntity.badRequest().body("INVALID_INPUT");
            }
            u.setUsername(trimmed);
        }
        if (req.getEmail() != null) {
            String em = req.getEmail().trim().toLowerCase();
            if (em.isEmpty() || em.length() > 128 || !em.contains("@")) {
                return ResponseEntity.badRequest().body("INVALID_INPUT");
            }
            // 简单防止邮箱冲突
            var existing = userRepository.findByEmail(em);
            if (existing.isPresent() && !existing.get().getId().equals(u.getId())) {
                return ResponseEntity.status(409).body("EMAIL_EXISTS");
            }
            u.setEmail(em);
        }
        if (req.getAvatarUrl() != null) {
            String url = req.getAvatarUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return ResponseEntity.badRequest().body("INVALID_INPUT");
            }
            u.setAvatarUrl(url);
        }
        // if (req.getPhone() != null) {
        //     String ph = req.getPhone().trim();
        //     if (ph.length() > 32) {
        //         return ResponseEntity.badRequest().body("INVALID_INPUT");
        //     }
        //     u.setPhone(ph.isEmpty() ? null : ph);
        // }
        User saved = userRepository.save(u);
        return ResponseEntity.ok(new ProfileResponse(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getAvatarUrl(), /*saved.getPhone(),*/ saved.getUpdatedAt()));
    }

    static class ProfileResponse {
        private Long id;
        private String email;
        private String username;
        private String avatarUrl;
        // private String phone;
        private java.time.OffsetDateTime updatedAt;

        public ProfileResponse(Long id, String email, String username, String avatarUrl, /*String phone,*/ java.time.OffsetDateTime updatedAt) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.updatedAt = updatedAt;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getAvatarUrl() { return avatarUrl; }
        // public String getPhone() { return phone; }
        public java.time.OffsetDateTime getUpdatedAt() { return updatedAt; }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) authentication.getPrincipal();
        var opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("UNAUTHORIZED");
        }
        userRepository.deleteById(userId);
        // 删除后令牌仍可被客户端持有，但后续访问将因找不到用户被视为未授权
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/me/metrics")
    public ResponseEntity<?> getMyMetrics(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) authentication.getPrincipal();
        var opt = userMetricsRepository.findById(userId);
        long totalCalories = 0L;
        int totalSessions = 0;
        long totalSeconds = 0L;
        if (opt.isPresent()) {
            var m = opt.get();
            if (m.getTotalCalories() != null) totalCalories = m.getTotalCalories();
            if (m.getTotalSessions() != null) totalSessions = m.getTotalSessions();
            if (m.getTotalYogaSeconds() != null) totalSeconds = m.getTotalYogaSeconds();
        }
        double totalHours = totalSeconds / 3600.0;
        return ResponseEntity.ok(new MetricsResponse(totalSessions, totalCalories, totalHours));
    }

    static class MetricsResponse {
        private final int totalSessions;
        private final long totalCalories;
        private final double totalHours;

        MetricsResponse(int totalSessions, long totalCalories, double totalHours) {
            this.totalSessions = totalSessions;
            this.totalCalories = totalCalories;
            this.totalHours = totalHours;
        }

        public int getTotalSessions() { return totalSessions; }
        public long getTotalCalories() { return totalCalories; }
        public double getTotalHours() { return totalHours; }
    }
}
