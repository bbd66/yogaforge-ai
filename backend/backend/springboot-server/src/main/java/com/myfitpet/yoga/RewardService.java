package com.myfitpet.yoga;

import org.springframework.stereotype.Service;

@Service
public class RewardService {
    public Reward compute(int calories, int poseCount, double qualityScore, int durationSeconds) {
        if (durationSeconds < 30) return new Reward(0,0); // 过滤极短会话
        int coins = (int)Math.floor(calories * 0.2);
        int exp = (int)Math.floor(calories * 0.5 + poseCount * 0.1 + qualityScore * 10);
        coins = Math.min(coins, 200);
        exp = Math.min(exp, 500);
        return new Reward(coins, exp);
    }

    public static class Reward {
        private final int coinsGain;
        private final int expGain;
        public Reward(int coinsGain, int expGain) { this.coinsGain = coinsGain; this.expGain = expGain; }
        public int getCoinsGain() { return coinsGain; }
        public int getExpGain() { return expGain; }
    }
}