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

    // === 新增：反制機制專屬升級 ===
    public int upgCoolant = 0; // 散熱組件：降低過熱累積速度
    public int upgStealth = 0; // 隱蔽路由：減緩被追蹤的速度

    // 主動技能 (消耗品)
    public int empCharges = 0;
    public int slowCharges = 0;

    public int highScore = 0;
    public double highestCombo = 1.0; // 新增：生涯最高 Combo 紀錄

    // 路線選擇倍率
    public double routeRewardMult = 1.0;
    public double routeDiffMult = 1.0;

    // === 局外永久天賦系統變數 (新增) ===
    public int legacyCoins = 0;       // 永久貨幣
    public int talentStartEMP = 0;     // 開局自帶 EMP (Max 3)
    public int talentWeakFW = 0;       // 弱化防火牆初始血量 (Max 5)
    public int talentFlashTime = 0;    // 延長解密閃現記憶時間 (Max 3)
    public int talentSignalShield = 0; // 新增：訊號屏蔽防護天賦 (Max 3)

    private final Preferences prefs;

    public PlayerStats() {
        // 初始化並載入系統註冊表中的存檔數據
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

    // 新增：購買永久天賦用的方法
    public boolean buyLegacy(int cost) {
        if (legacyCoins >= cost) {
            legacyCoins -= cost;
            saveData(); // 扣錢後立刻存檔
            return true;
        }
        return false;
    }

    public void reset() {
        currentLevel = 1;
        darkCoins = 0;
        upgClick = 0; upgSpeed = 0; upgShield = 0; upgBot = 0; upgMine = 0;
        upgCoolant = 0; upgStealth = 0; // 新增：重置反制升級

        // 變更：每次開局重置時，根據天賦賦予初始 EMP 數量
        empCharges = talentStartEMP;
        slowCharges = 0;
        routeRewardMult = 1.0; routeDiffMult = 1.0;
    }

    // 新增：從硬碟讀取數據
    public void loadData() {
        highScore = prefs.getInt("highScore", 0);
        highestCombo = prefs.getDouble("highestCombo", 1.0);
        legacyCoins = prefs.getInt("legacyCoins", 0);
        talentStartEMP = prefs.getInt("talentStartEMP", 0);
        talentWeakFW = prefs.getInt("talentWeakFW", 0);
        talentFlashTime = prefs.getInt("talentFlashTime", 0);
        talentSignalShield = prefs.getInt("talentSignalShield", 0);
    }

    // 新增：將數據寫入硬碟
    public void saveData() {
        prefs.putInt("highScore", highScore);
        prefs.putDouble("highestCombo", highestCombo);
        prefs.putInt("legacyCoins", legacyCoins);
        talentStartEMP = prefs.getInt("talentStartEMP", 0);
        talentWeakFW = prefs.getInt("talentWeakFW", 0);
        talentFlashTime = prefs.getInt("talentFlashTime", 0);
        prefs.putInt("talentSignalShield", talentSignalShield);
    }
}