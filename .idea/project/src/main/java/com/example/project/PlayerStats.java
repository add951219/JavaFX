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

    public int upgCoolant = 0;
    public int upgStealth = 0;

    // 主動技能 (消耗品)
    public int empCharges = 0;
    public int slowCharges = 0;

    public int highScore = 0;
    public double highestCombo = 1.0;

    public int upgMiner = 0;
    public boolean hasTraceShield = false;
    public int autoSolveCharges = 0;
    public int overloadCharges = 0;

    // 路線選擇倍率
    public double routeRewardMult = 1.0;
    public double routeDiffMult = 1.0;

    // === 局外永久天賦系統變數 ===
    public int legacyCoins = 0;
    // 原有基礎天賦 (Level 1)
    public int talentStartEMP = 0;
    public int talentWeakFW = 0;
    public int talentFlashTime = 0;
    public int talentSignalShield = 0;

    // 全新四大流派天賦 (Level 2~4)
    public boolean talentErrorCorrect = false; // 防呆協議
    public boolean talentComboGuard = false;   // 連擊保險
    public boolean talentGlitchImmune = false; // 防毒核心
    public boolean talentOverdrive = false;    // 熱能超頻
    public boolean talentEdgeRunner = false;   // 極限駭客
    public boolean talentTrojanSplit = false;  // 木馬分裂
    public boolean talentHeatDump = false;     // 緊急散熱
    public boolean talentBugZapper = false;    // 滅蟲波段
    public boolean talentIntuition = false;    // 直覺駭入

    private final Preferences prefs;

    public PlayerStats() {
        prefs = Preferences.userNodeForPackage(PlayerStats.class);
        loadData();
    }

    public boolean buy(int cost) {
        if (darkCoins >= cost) { darkCoins -= cost; return true; }
        return false;
    }

    public boolean buyLegacy(int cost) {
        if (legacyCoins >= cost) { legacyCoins -= cost; saveData(); return true; }
        return false;
    }

    public void reset() {
        currentLevel = 1;
        darkCoins = 0;
        upgClick = 0; upgSpeed = 0; upgShield = 0; upgBot = 0; upgMine = 0;
        upgCoolant = 0; upgStealth = 0;
        upgMiner = 0; hasTraceShield = false;
        empCharges = talentStartEMP;
        slowCharges = 0;
        autoSolveCharges = 0; overloadCharges = 0;
        routeRewardMult = 1.0; routeDiffMult = 1.0;
    }

    public void loadData() {
        highScore = prefs.getInt("highScore", 0);
        highestCombo = prefs.getDouble("highestCombo", 1.0);
        legacyCoins = prefs.getInt("legacyCoins", 0);
        talentStartEMP = prefs.getInt("talentStartEMP", 0);
        talentWeakFW = prefs.getInt("talentWeakFW", 0);
        talentFlashTime = prefs.getInt("talentFlashTime", 0);
        talentSignalShield = prefs.getInt("talentSignalShield", 0);

        talentErrorCorrect = prefs.getBoolean("talentErrorCorrect", false);
        talentComboGuard = prefs.getBoolean("talentComboGuard", false);
        talentGlitchImmune = prefs.getBoolean("talentGlitchImmune", false);
        talentOverdrive = prefs.getBoolean("talentOverdrive", false);
        talentEdgeRunner = prefs.getBoolean("talentEdgeRunner", false);
        talentTrojanSplit = prefs.getBoolean("talentTrojanSplit", false);
        talentHeatDump = prefs.getBoolean("talentHeatDump", false);
        talentBugZapper = prefs.getBoolean("talentBugZapper", false);
        talentIntuition = prefs.getBoolean("talentIntuition", false);
    }

    public void saveData() {
        prefs.putInt("highScore", highScore);
        prefs.putDouble("highestCombo", highestCombo);
        prefs.putInt("legacyCoins", legacyCoins);
        prefs.putInt("talentStartEMP", talentStartEMP);
        prefs.putInt("talentWeakFW", talentWeakFW);
        prefs.putInt("talentFlashTime", talentFlashTime);
        prefs.putInt("talentSignalShield", talentSignalShield);

        prefs.putBoolean("talentErrorCorrect", talentErrorCorrect);
        prefs.putBoolean("talentComboGuard", talentComboGuard);
        prefs.putBoolean("talentGlitchImmune", talentGlitchImmune);
        prefs.putBoolean("talentOverdrive", talentOverdrive);
        prefs.putBoolean("talentEdgeRunner", talentEdgeRunner);
        prefs.putBoolean("talentTrojanSplit", talentTrojanSplit);
        prefs.putBoolean("talentHeatDump", talentHeatDump);
        prefs.putBoolean("talentBugZapper", talentBugZapper);
        prefs.putBoolean("talentIntuition", talentIntuition);
    }
}