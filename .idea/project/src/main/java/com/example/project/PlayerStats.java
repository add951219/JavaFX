package com.example.project;

public class PlayerStats {
    public int currentLevel = 1;
    public int darkCoins = 0;

    // 被動升級
    public int upgClick = 0;
    public int upgSpeed = 0;
    public int upgShield = 0;
    public int upgBot = 0;
    public int upgMine = 0;

    // 主動技能 (消耗品)
    public int empCharges = 0;
    public int slowCharges = 0;

    public int highScore = 0;

    // 路線選擇倍率
    public double routeRewardMult = 1.0;
    public double routeDiffMult = 1.0;

    public boolean buy(int cost) {
        if (darkCoins >= cost) {
            darkCoins -= cost;
            return true;
        }
        return false;
    }

    public void reset() {
        currentLevel = 1;
        darkCoins = 0;
        upgClick = 0; upgSpeed = 0; upgShield = 0; upgBot = 0; upgMine = 0;
        empCharges = 0; slowCharges = 0;
        routeRewardMult = 1.0; routeDiffMult = 1.0;
    }
}