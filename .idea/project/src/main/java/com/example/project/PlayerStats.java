package com.example.project;

import java.util.prefs.Preferences;

public class PlayerStats {
    public int currentLevel = 1;
    public int darkCoins = 0;

    // 被動升級
    public int upgClick = 0;
    public int upgSpeed = 0;
    public int upgShield = 0;
    public int upgBot = 0;
    public int upgMine = 0;

    // 主動技能
    public int empCharges = 0;
    public int slowCharges = 0;

    // 永久存檔數據
    public int highScore = 0;
    public int legacyCoins = 0; // 局外永久貨幣 (每局結束後結算)

    public double routeRewardMult = 1.0;
    public double routeDiffMult = 1.0;

    // 存檔神器
    private final Preferences prefs;

    public PlayerStats() {
        // 初始化並綁定這個類別的存檔空間
        prefs = Preferences.userNodeForPackage(PlayerStats.class);
        loadData();
    }

    public boolean buy(int cost) {
        if (darkCoins >= cost) {
            darkCoins -= cost;
            return true;
        }
        return false;
    }

    // 每次重新開始遊戲時，只重置局內數據
    public void reset() {
        currentLevel = 1;
        darkCoins = 0;
        upgClick = 0; upgSpeed = 0; upgShield = 0; upgBot = 0; upgMine = 0;
        empCharges = 0; slowCharges = 0;
        routeRewardMult = 1.0; routeDiffMult = 1.0;
    }

    // 從系統讀取存檔
    public void loadData() {
        highScore = prefs.getInt("highScore", 0);
        legacyCoins = prefs.getInt("legacyCoins", 0);
    }

    // 將數據寫入系統存檔
    public void saveData() {
        prefs.putInt("highScore", highScore);
        prefs.putInt("legacyCoins", legacyCoins);
    }
}