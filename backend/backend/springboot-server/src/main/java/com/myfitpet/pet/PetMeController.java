package com.myfitpet.pet;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
public class PetMeController {

    private final PetRepository petRepository;
    private final PetAccountService petAccountService;

    public PetMeController(PetRepository petRepository, PetAccountService petAccountService) {
        this.petRepository = petRepository;
        this.petAccountService = petAccountService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> myPet(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        var petOpt = petRepository.findByOwnerId(userId);
        if (petOpt.isEmpty()) {
            return ResponseEntity.status(404).body("PET_NOT_FOUND");
        }
        PetEntity p = petOpt.get();
        Map<String,Object> out = new HashMap<>();
        out.put("id", p.getId());
        out.put("name", p.getName());
        out.put("level", p.getLevel());
        out.put("exp", p.getExp());
        out.put("gold", p.getGold());
        return ResponseEntity.ok(out);
    }

    /**
     * 统一为当前登录用户的宠物应用奖励或扣除：可增加/减少经验和金币
     * expDelta, goldDelta 可以为正(奖励)或负(扣除)
     */
    static class RewardRequest { public int expDelta; public int goldDelta; }

    @PostMapping("/me/reward")
    public ResponseEntity<?> reward(Authentication auth, @RequestBody RewardRequest req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        var petOpt = petRepository.findByOwnerId(userId);
        if (petOpt.isEmpty()) {
            return ResponseEntity.status(404).body("PET_NOT_FOUND");
        }
        // 允许金币为负（扣费），经验小于 0 则按照 0 处理
        int expDelta = Math.max(0, req.expDelta);
        int goldDelta = req.goldDelta;
        return ResponseEntity.ok(petAccountService.applyRewardForUser(userId, expDelta, goldDelta));
    }

    static class PurchaseRequest { public int productId; public int price; }

    /**
     * 使用金币购买商品：根据 price 扣减当前登录用户宠物的金币
     */
    @PostMapping("/me/purchase")
    public ResponseEntity<?> purchase(Authentication auth, @RequestBody PurchaseRequest req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        var petOpt = petRepository.findByOwnerId(userId);
        if (petOpt.isEmpty()) {
            return ResponseEntity.status(404).body("PET_NOT_FOUND");
        }

        PetEntity pet = petOpt.get();
        int currentGold = pet.getGold();
        int price = req.price;

        if (price <= 0) {
            return ResponseEntity.badRequest().body("INVALID_PRICE");
        }
        if (currentGold < price) {
            return ResponseEntity.badRequest().body("INSUFFICIENT_GOLD");
        }

        // 使用已有的奖励服务统一扣费（经验不变，金币减少）
        return ResponseEntity.ok(petAccountService.applyRewardForUser(userId, 0, -price));
    }

    /**
     * 测试用：给当前登录用户的宠物增加一定数量的金币
     * 前端可用于验证交易整体逻辑和动画效果
     */
    static class GainGoldRequest { public int amount; public String reason; }

    @PostMapping("/me/gain-gold")
    public ResponseEntity<?> gainGold(Authentication auth, @RequestBody GainGoldRequest req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        var petOpt = petRepository.findByOwnerId(userId);
        if (petOpt.isEmpty()) {
            return ResponseEntity.status(404).body("PET_NOT_FOUND");
        }

        int amount = req.amount;
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("INVALID_AMOUNT");
        }

        // 使用已有奖励服务统一处理金币增加（经验不变，仅金币增加）
        return ResponseEntity.ok(petAccountService.applyRewardForUser(userId, 0, amount));
    }

    static class EarnRequest { public int expDelta; }

    @PostMapping("/me/experience/earn")
    public ResponseEntity<?> earn(Authentication auth, @RequestBody EarnRequest req) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        Long userId = (Long) auth.getPrincipal();
        var petOpt = petRepository.findByOwnerId(userId);
        if (petOpt.isEmpty()) {
            return ResponseEntity.status(404).body("PET_NOT_FOUND");
        }
        return ResponseEntity.ok(petAccountService.addExperience(petOpt.get().getId(), req.expDelta));
    }
}
