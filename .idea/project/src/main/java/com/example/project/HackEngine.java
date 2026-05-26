package com.example.project;

import java.util.Random;

public class HackEngine {
    public enum GameState { MAIN_MENU, INTRO, PLAYING, PAUSED, ROUTE_SELECT, SHOP, GAMEOVER, TALENT_TREE }
    public GameState currentState = GameState.MAIN_MENU;
    public enum GlitchType { NONE, NETWORK_LAG, VISUAL_DISTORTION, CORE_OVERLOAD }
    public GlitchType activeGlitch = GlitchType.NONE;

    public boolean isHacking = false;
    public double progress = 0.0;
    public int currentSegment = 0;
    public int totalSegments = 4;
    public double comboMultiplier = 1.0;
    public int comboFrames = 0;

    public boolean isFirewallFight = false;
    public double firewallProgress = 0.5;

    public boolean isInterceptFight = false;
    public String targetSequence = "";
    public int sequenceIndex = 0;
    public long interceptDeadline = 0;

    public boolean isDecryptFight = false;
    public String decryptTarget = "";
    public String decryptInput = "";
    public long decryptFlashEndTime = 0;
    public long decryptDeadline = 0;
    public boolean isDecryptFlashed = false;

    // === 新增：打蟲子關卡變數 ===
    public boolean isBugCatchFight = false;
    public int bugsCaught = 0;
    public long bugCatchDeadline = 0;
    public long lastBugSpawnTime = 0;

    public final Random random = new Random();

    // === 結算系統追蹤變數 ===
    public double runMaxCombo = 1.0;
    public int runTotalKeystrokes = 0;
    public int runCorrectKeystrokes = 0;
    public long runStartTime = 0;

    public void resetEvents() {
        isHacking = false; progress = 0.0; currentSegment = 0; comboFrames = 0; comboMultiplier = 1.0;
        isFirewallFight = false; firewallProgress = 0.5; isInterceptFight = false; isDecryptFight = false;
        isBugCatchFight = false; // 新增重置
    }

    public void startNewRun() {
        runMaxCombo = 1.0;
        runTotalKeystrokes = 0;
        runCorrectKeystrokes = 0;
        runStartTime = System.nanoTime();
    }

    public void updateMaxCombo() {
        if (comboMultiplier > runMaxCombo) {
            runMaxCombo = comboMultiplier;
        }
    }

    public int getRunAPM() {
        if (runStartTime == 0) return 0;
        double minutes = (System.nanoTime() - runStartTime) / 60_000_000_000.0;
        if (minutes <= 0.01) return 0;
        return (int) (runTotalKeystrokes / minutes);
    }

    public double getRunAccuracy() {
        if (runTotalKeystrokes == 0) return 100.0;
        return ((double) runCorrectKeystrokes / runTotalKeystrokes) * 100.0;
    }

    public boolean isBossLevel(int level) { return level % 5 == 0; }

    public void rollGlitch(int level) {
        if (isBossLevel(level)) { activeGlitch = GlitchType.NONE; return; }
        int r = random.nextInt(4);
        if (r == 1) activeGlitch = GlitchType.NETWORK_LAG;
        else if (r == 2) activeGlitch = GlitchType.VISUAL_DISTORTION;
        else if (r == 3) activeGlitch = GlitchType.CORE_OVERLOAD;
        else activeGlitch = GlitchType.NONE;
    }

    public void startInterceptEvent(PlayerStats p, long now) {
        isInterceptFight = true; sequenceIndex = 0;
        int len = (int)(4 + (p.currentLevel * p.routeDiffMult / 2));
        String[] pool = {"W", "A", "S", "D"}; StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(pool[random.nextInt(pool.length)]);
        targetSequence = sb.toString();
        double lagModifier = (activeGlitch == GlitchType.NETWORK_LAG) ? 0.6 : 1.0;
        interceptDeadline = now + (long)(Math.max(2.5, 6.5 - p.currentLevel * 0.12) * 1_000_000_000L * lagModifier);
    }

    public void startDecryptEvent(PlayerStats p, long now) {
        isDecryptFight = true; isDecryptFlashed = false; decryptInput = "";
        decryptTarget = generateAlphanumeric(Math.min(6, 3 + (p.currentLevel / 3)));
        double lagModifier = (activeGlitch == GlitchType.NETWORK_LAG) ? 0.6 : 1.0;
        decryptFlashEndTime = now + 500_000_000L + (p.talentFlashTime * 150_000_000L);
        decryptDeadline = now + (long)(Math.max(4.0, 7.5 - p.currentLevel * 0.12) * 1_000_000_000L * lagModifier);
    }

    // === 新增：啟動打蟲子關卡 ===
    public void startBugCatchEvent(PlayerStats p, long now) {
        isBugCatchFight = true;
        bugsCaught = 0;
        double lagModifier = (activeGlitch == GlitchType.NETWORK_LAG) ? 0.8 : 1.0;
        // 限時約為 12~15 秒，隨難度遞減
        bugCatchDeadline = now + (long)(Math.max(8.0, 15.0 - p.currentLevel * 0.2) * 1_000_000_000L * lagModifier);
        lastBugSpawnTime = now;
    }

    public boolean useEMP(PlayerStats p) {
        if (p.empCharges > 0 && isFirewallFight) { p.empCharges--; firewallProgress += 0.4; return true; }
        return false;
    }

    public boolean useSlow(PlayerStats p) {
        // 新增：緩速技能也能延長打蟲子的時間
        if (p.slowCharges > 0 && (isInterceptFight || isDecryptFight || isBugCatchFight)) {
            p.slowCharges--;
            interceptDeadline += 2_500_000_000L;
            decryptDeadline += 2_500_000_000L;
            bugCatchDeadline += 2_500_000_000L;
            return true;
        }
        return false;
    }

    public String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 1500; i++) sb.append((char)(random.nextInt(94) + 33));
        return sb.toString();
    }

    public String generateAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}