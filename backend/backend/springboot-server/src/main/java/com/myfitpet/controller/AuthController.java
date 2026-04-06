package com.myfitpet.controller;

import com.myfitpet.user.User;
import com.myfitpet.user.UserRepository;
import com.myfitpet.dto.RegisterRequest;
import com.myfitpet.dto.RegisterResponse;
import com.myfitpet.dto.LoginRequest;
import com.myfitpet.dto.LoginResponse;
import com.myfitpet.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final com.myfitpet.pet.PetRepository petRepository;
    private final com.myfitpet.yoga.UserMetricsRepository metricsRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder,
                          com.myfitpet.pet.PetRepository petRepository,
                          com.myfitpet.yoga.UserMetricsRepository metricsRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.petRepository = petRepository;
        this.metricsRepository = metricsRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest req) {
        log.info("[REGISTER] Incoming request email='{}' username='{}'", req.getEmail(), req.getUsername());
        String emailLower = req.getEmail().trim().toLowerCase();
        String usernameTrim = req.getUsername().trim();

        if (userRepository.existsByEmail(emailLower)) {
            log.warn("[REGISTER] Email exists: {}", emailLower);
            return ResponseEntity.status(409).body("EMAIL_EXISTS");
        }
        if (userRepository.existsByUsername(usernameTrim)) {
            log.warn("[REGISTER] Username exists: {}", usernameTrim);
            return ResponseEntity.status(409).body("USERNAME_EXISTS");
        }

        String hash = passwordEncoder.encode(req.getPassword());
        User user = new User();
        user.setEmail(emailLower);
        user.setUsername(usernameTrim);
        user.setPasswordHash(hash);
        user.setAvatarUrl(req.getAvatarUrl());
        try {
            user = userRepository.save(user);
            log.info("[REGISTER] User persisted id={}", user.getId());
        } catch (DataIntegrityViolationException e) {
            log.error("[REGISTER] Data integrity violation", e);
            return ResponseEntity.status(409).body("EMAIL_OR_USERNAME_EXISTS");
        }

        // Initialize unique pet and user metrics with zeros (avoid lambda capturing non-final variables)
        final Long newUserId = user.getId();
        if (petRepository.findByOwnerId(newUserId).isEmpty()) {
            com.myfitpet.pet.PetEntity pet = new com.myfitpet.pet.PetEntity();
            pet.setOwnerId(newUserId);
            pet.setName(user.getUsername() + "_pet");
            pet.setLevel(1);
            pet.setExp(0);
            pet.setGold(0);
            petRepository.save(pet);
            log.info("[REGISTER] Pet initialized for userId={} petName={}", newUserId, pet.getName());
        } else {
            log.info("[REGISTER] Pet already existed for userId={}", newUserId);
        }
        if (!metricsRepository.existsById(newUserId)) {
            com.myfitpet.yoga.UserMetrics m = new com.myfitpet.yoga.UserMetrics();
            m.setUserId(newUserId);
            m.setTotalCalories(0L);
            m.setTotalSessions(0);
            m.setTotalYogaSeconds(0L);
            metricsRepository.save(m);
            log.info("[REGISTER] User metrics initialized for userId={}", newUserId);
        } else {
            log.info("[REGISTER] User metrics already existed for userId={}", newUserId);
        }

        String token = jwtService.generateToken(user.getId());
        RegisterResponse resp = new RegisterResponse(user.getId(), user.getEmail(), user.getUsername(), user.getAvatarUrl(), token);
        log.info("[REGISTER] Completed for userId={}", user.getId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest req) {
        String emailLower = req.getEmail().trim().toLowerCase();
        return userRepository.findByEmail(emailLower)
                .map(user -> {
                    if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
                        return ResponseEntity.status(401).body("INVALID_CREDENTIALS");
                    }
                    String token = jwtService.generateToken(user.getId());
                    LoginResponse resp = new LoginResponse(user.getId(), user.getEmail(), user.getUsername(), user.getAvatarUrl(), token);
                    return ResponseEntity.ok(resp);
                })
                .orElseGet(() -> ResponseEntity.status(401).body("INVALID_CREDENTIALS"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        // 无状态 JWT，不存储服务端会话，客户端只需丢弃 token
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        return ResponseEntity.noContent().build(); // 204，客户端在收到后清除本地 token
    }
}
