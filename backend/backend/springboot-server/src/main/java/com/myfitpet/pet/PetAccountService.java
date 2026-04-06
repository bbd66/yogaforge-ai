package com.myfitpet.pet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class PetAccountService {

    private final PetRepository petRepository;

    public PetAccountService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public Map<String, Object> getAccount(Long petId) {
        PetEntity pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("pet not found"));
        int level = pet.getLevel();
        int exp = pet.getExp();
        int req = requiredExp(level);
        Map<String, Object> out = new HashMap<>();
        out.put("level", level);
        out.put("exp", exp);
        out.put("expRequired", req);
        return out;
    }

    @Transactional
    public Map<String, Object> addExperience(Long petId, int expDelta) {
        if (expDelta <= 0) throw new IllegalArgumentException("expDelta must be > 0");
        PetEntity pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("pet not found"));
        int level = pet.getLevel();
        int exp = pet.getExp() + expDelta;

        int levelUps = 0;
        while (exp >= requiredExp(level)) {
            exp -= requiredExp(level);
            level += 1;
            levelUps += 1;
            if (level >= 100) { // safety cap
                exp = 0;
                break;
            }
        }

        pet.setLevel(level);
        pet.setExp(exp);
        petRepository.save(pet);

        Map<String, Object> out = new HashMap<>();
        out.put("level", level);
        out.put("exp", exp);
        out.put("expRequired", requiredExp(level));
        out.put("levelUps", levelUps);
        return out;
    }

    @Transactional
    public Map<String, Object> applyRewardForUser(Long ownerId, int expGain, int goldGain) {
        // allow negative goldGain for spending; expGain must be non-negative
        if (expGain < 0) throw new IllegalArgumentException("expGain must be >= 0");
        PetEntity pet = petRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("pet not found"));
        int level = pet.getLevel();
        int exp = pet.getExp() + expGain;
        int gold = pet.getGold() + goldGain;
        int levelUps = 0;
        while (exp >= requiredExp(level)) {
            exp -= requiredExp(level);
            level += 1;
            levelUps += 1;
            if (level >= 100) { exp = 0; break; }
        }
        pet.setLevel(level);
        pet.setExp(exp);
        pet.setGold(gold);
        petRepository.save(pet);
        Map<String,Object> out = new HashMap<>();
        out.put("level", level);
        out.put("exp", exp);
        out.put("gold", gold);
        out.put("expRequired", requiredExp(level));
        out.put("levelUps", levelUps);
        out.put("expGain", expGain);
        out.put("goldGain", goldGain);
        return out;
    }

    private int requiredExp(int level) {
        // simple curve: floor(100 * level^1.3), min 50
        double req = Math.floor(100 * Math.pow(Math.max(level, 1), 1.3));
        return (int) Math.max(50, req);
    }
}
