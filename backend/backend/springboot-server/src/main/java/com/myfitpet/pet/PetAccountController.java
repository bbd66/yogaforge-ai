package com.myfitpet.pet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pets")
public class PetAccountController {

    private final PetAccountService petAccountService;

    public PetAccountController(PetAccountService petAccountService) {
        this.petAccountService = petAccountService;
    }

    @GetMapping("/{petId}/account")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable("petId") Long petId) {
        return ResponseEntity.ok(petAccountService.getAccount(petId));
    }

    public static class EarnRequest {
        public int expDelta;
        public int getExpDelta() { return expDelta; }
        public void setExpDelta(int expDelta) { this.expDelta = expDelta; }
    }

    @PostMapping("/{petId}/experience/earn")
    public ResponseEntity<Map<String, Object>> earn(@PathVariable("petId") Long petId, @RequestBody EarnRequest req) {
        return ResponseEntity.ok(petAccountService.addExperience(petId, req.expDelta));
    }
}
