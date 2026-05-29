package com.example.project;

import java.util.Random;

public class HackEngine {
    public enum GameState { MAIN_MENU, ROUTE_SELECT, SHOP, TALENT_TREE, PLAYING, PAUSED, GAMEOVER, INTRO }
    public enum GlitchType { NONE, NETWORK_LAG, VISUAL_DISTORTION, CORE_OVERLOAD }

    // === 新增：Boss 系統專屬型態與狀態 ===
    public enum BossType { NONE, SENTINEL, PHANTOM, CERBERUS, ARCHITECT, NULL_GOD }
    public boolean isBossFight = false;
    public BossType currentBossType = BossType.NONE;
    public int bossPhase = 1; // 1: 突破外層, 2: 滲透核心, 3: 最終解密
    public int bossRage = 0; // 失敗疊加的憤怒值 (最大 3)
    public long cerberusGlobalDeadline = 0; // 三頭犬專屬全域倒數
    public long lastArchitectShiftTime = 0; // 建築師專屬干擾計時器

    public GameState currentState = GameState.MAIN_MENU;
    public double progress = 0.0;
    public int currentSegment = 0;
    public int totalSegments = 4;
    public boolean isHacking = false;

    public GlitchType activeGlitch = GlitchType.NONE;

    public boolean isFirewallFight = false;
    public double firewallProgress = 1.0;
    public double coreHeat = 0.0;
    public boolean isOverheated = false;
    public long overheatEndTime = 0;

    public boolean isBeingTraced = false;
    public double traceLevel = 0.0;

    public boolean isInterceptFight = false;
    public String targetSequence = "";
    public int sequenceIndex = 0;
    public long interceptDeadline = 0;

    public boolean isDecryptFight = false;
    public String decryptTarget = "";
    public String decryptInput = "";
    public long decryptFlashEndTime = 0;
    public boolean isDecryptFlashed = false;
    public long decryptDeadline = 0;

    public boolean isBugCatchFight = false;
    public int bugsCaught = 0;
    public long bugCatchDeadline = 0;
    public long lastBugSpawnTime = 0;

    public Random random = new Random();

    public int runTotalKeystrokes = 0;
    public int runCorrectKeystrokes = 0;
    public double runMaxCombo = 1.0;
    public double comboMultiplier = 1.0;
    public int comboFrames = 0;
    private long runStartTime = 0;

    public void startNewRun() {
        progress = 0.0; currentSegment = 0; isHacking = false; isBeingTraced = false; traceLevel = 0.0;
        coreHeat = 0.0; isOverheated = false;
        runTotalKeystrokes = 0; runCorrectKeystrokes = 0; runMaxCombo = 1.0; comboMultiplier = 1.0; comboFrames = 0;
        runStartTime = System.nanoTime();
        resetEvents();
    }

    public void resetEvents() {
        isFirewallFight = false; isInterceptFight = false; isDecryptFight = false; isBugCatchFight = false;
        isBossFight = false; bossPhase = 1; bossRage = 0; currentBossType = BossType.NONE;
    }

    public void rollGlitch(int level) {
        activeGlitch = GlitchType.NONE;
        if (level > 3 && random.nextInt(100) < (15 + level)) {
            int g = random.nextInt(3);
            if (g == 0) activeGlitch = GlitchType.NETWORK_LAG;
            else if (g == 1) activeGlitch = GlitchType.VISUAL_DISTORTION;
            else if (g == 2) activeGlitch = GlitchType.CORE_OVERLOAD;
        }
    }

    // 判斷 Boss 型態 (每 5 關一個)
    public BossType determineBossType(int level) {
        if (level % 5 != 0) return BossType.NONE;
        if (level == 5) return BossType.SENTINEL;
        if (level == 10) return BossType.PHANTOM;
        if (level == 15) return BossType.CERBERUS;
        if (level == 20) return BossType.ARCHITECT;
        return BossType.NULL_GOD;
    }

    public boolean isBossLevel(int level) {
        return level % 5 == 0;
    }

    public String generateBossAlphaNum(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    public void shiftArchitectTarget() {
        if (currentBossType == BossType.SENTINEL || (currentBossType == BossType.NULL_GOD && random.nextBoolean())) {
            decryptTarget = generateBossAlphaNum(decryptTarget.length());
        } else {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<decryptTarget.length(); i++) sb.append((char)(random.nextInt(26) + 'A'));
            decryptTarget = sb.toString();
        }
        decryptInput = "";
    }

    public void startInterceptEvent(PlayerStats p, long now) {
        isInterceptFight = true; sequenceIndex = 0;
        int len = 4 + (p.currentLevel / 3);
        String[] dirs = {"W", "A", "S", "D"}; StringBuilder sb = new StringBuilder();
        for (int i=0; i<len; i++) sb.append(dirs[random.nextInt(4)]);
        targetSequence = sb.toString();
        double time = 4.0 - (p.currentLevel * 0.05); if(time < 1.5) time = 1.5;
        if (activeGlitch == GlitchType.NETWORK_LAG) time *= 0.6;
        interceptDeadline = now + (long)(time * 1_000_000_000L);
    }

    public void startDecryptEvent(PlayerStats p, long now) {
        isDecryptFight = true; decryptInput = ""; isDecryptFlashed = false;
        int len = 4 + (p.currentLevel / 4);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<len; i++) sb.append((char)(random.nextInt(26) + 'A'));
        decryptTarget = sb.toString();
        double flashTime = 1.2 + (p.talentFlashTime * 0.15) - (p.currentLevel * 0.03); if(flashTime < 0.4) flashTime = 0.4;
        decryptFlashEndTime = now + (long)(flashTime * 1_000_000_000L);
        double totalTime = 5.0 - (p.currentLevel * 0.05); if(totalTime < 2.5) totalTime = 2.5;
        if (activeGlitch == GlitchType.NETWORK_LAG) totalTime *= 0.6;
        decryptDeadline = now + (long)(totalTime * 1_000_000_000L);
    }

    public void startBugCatchEvent(PlayerStats p, long now) {
        isBugCatchFight = true; bugsCaught = 0;
        double baseTime = 15.0 - (p.currentLevel * 0.2); if(baseTime < 7.0) baseTime = 7.0;
        bugCatchDeadline = now + (long)(baseTime * 1_000_000_000L);
        lastBugSpawnTime = now;
    }

    public boolean useEMP(PlayerStats p) {
        if (p.empCharges > 0 && isFirewallFight) { p.empCharges--; firewallProgress = Math.min(1.0, firewallProgress + 0.4); return true; } return false;
    }

    public boolean useSlow(PlayerStats p) {
        if (p.slowCharges > 0) {
            if (isInterceptFight) { p.slowCharges--; interceptDeadline += 3_000_000_000L; return true; }
            if (isDecryptFight) { p.slowCharges--; decryptDeadline += 3_000_000_000L; return true; }
            if (isBugCatchFight) { p.slowCharges--; bugCatchDeadline += 3_000_000_000L; return true; }
        } return false;
    }

    public void updateMaxCombo() { if (comboMultiplier > runMaxCombo) runMaxCombo = comboMultiplier; }
    public int getRunAPM() {
        long duration = System.nanoTime() - runStartTime; double mins = duration / 60_000_000_000.0;
        return mins > 0 ? (int)(runTotalKeystrokes / mins) : 0;
    }
    public double getRunAccuracy() { return runTotalKeystrokes > 0 ? ((double)runCorrectKeystrokes / runTotalKeystrokes) * 100 : 100.0; }
}