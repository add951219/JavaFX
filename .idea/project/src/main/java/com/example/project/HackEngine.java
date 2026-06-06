package com.example.project;

import java.util.Random;

public class HackEngine {
    public enum GameState { MAIN_MENU, ROUTE_SELECT, SHOP, TALENT_TREE, PLAYING, PAUSED, GAMEOVER, INTRO,
        TUTORIAL,TESTING, DEMO_MENU }
    public enum GlitchType { NONE, NETWORK_LAG, VISUAL_DISTORTION, CORE_OVERLOAD }
    public enum BossType { NONE, PULSE, SURGE, PHANTOM, CERBERUS, ARCHITECT, MIMIC, HYDRA, SPECTER, NULL_GOD }

    public boolean isBossFight = false;
    public BossType currentBossType = BossType.NONE;
    public int bossPhase = 1;
    public int maxBossPhase = 3;
    public int bossRage = 0;
    public boolean isEscapeSequence = false;

    // === 天賦專用變數 ===
    public boolean heatDumpAvailable = true; // 緊急散熱每局1次
    public int errorCorrectCharges = 0;      // 防呆協議次數
    public int eventMistakes = 0;            // 記錄事件失誤次數 (極限駭客用)

    public boolean isSurgeFight = false;
    public int surgePlayerPos = 2;
    public int surgeHP = 3;
    public long surgeStartTime = 0;
    public long surgeNextAttackTime = 0;
    public int surgeHealsDone = 0;
    public boolean[] surgeWarnings = new boolean[5];
    public boolean[] surgeExplosions = new boolean[5];
    public long surgeWarnEndTime = 0;
    public long surgeExplodeEndTime = 0;
    public boolean isSurgeFakeOut = false;

    public boolean isPulseFight = false;
    public double pulseScanPos = 0.0;
    public double pulseScanDir = 1.0;
    public double pulseScanSpeed = 0.012;
    public double pulseZoneMin = 0.35;
    public double pulseZoneMax = 0.65;
    public int pulseHitsRequired = 5;
    public int pulseHitsCount = 0;
    public boolean pulseJustHit = false;
    public long pulseHitFlashEnd = 0;
    public long pulsePhase1Deadline = 0;
    public long[] pulseLetterDeadlines;
    public long pulseLetterWindow = 400_000_000L;
    public long pulseBeatInterval = 1_200_000_000L;
    public String[] pulseRevealChars;
    public int pulseRevealIndex = 0;
    public long pulseNextRevealTime = 0;
    public long pulseRevealInterval = 600_000_000L;
    public boolean pulseAllRevealed = false;

    public long cerberusGlobalDeadline = 0;
    public long lastArchitectShiftTime = 0;
    public boolean isMimicWindow = true;
    public long mimicToggleTime = 0;
    public String displaySequence = "";
    public double[] hydraWalls = {1.0, 1.0, 1.0};
    public int activeHydraHead = 0;
    public boolean isSpecterHidden = false;
    public long specterHideTime = 0;

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
    public void startFirewallEvent() { isFirewallFight = true; firewallProgress = 0.5; }
    public void startInterceptEvent() { startInterceptEvent(new PlayerStats(), System.nanoTime()); }
    public void startDecryptEvent() { startDecryptEvent(new PlayerStats(), System.nanoTime()); }
    public void startBugCatchEvent() { startBugCatchEvent(new PlayerStats(), System.nanoTime()); }
    public void startBossPulse() { currentBossType = BossType.PULSE; bossPhase = 1; }
    public void startBossSurge() { currentBossType = BossType.SURGE; bossPhase = 1; }

    public void startNewRun() {
        progress = 0.0; currentSegment = 0; isHacking = false; isBeingTraced = false; traceLevel = 0.0;
        coreHeat = 0.0; isOverheated = false; heatDumpAvailable = true;
        runTotalKeystrokes = 0; runCorrectKeystrokes = 0; runMaxCombo = 1.0; comboMultiplier = 1.0; comboFrames = 0;
        runStartTime = System.nanoTime();
        resetEvents();
    }

    public void resetEvents() {
        isFirewallFight = false; isInterceptFight = false; isDecryptFight = false; isBugCatchFight = false;
        isBossFight = false; bossPhase = 1; bossRage = 0; currentBossType = BossType.NONE; isEscapeSequence = false;
        isPulseFight = false; pulseHitsCount = 0; pulseRevealIndex = 0; pulseAllRevealed = false;
        isSurgeFight = false; errorCorrectCharges = 0; eventMistakes = 0;
    }

    // === 天賦：防毒核心 (Glitch Immunity) 機率觸發 ===
    public void rollGlitch(int level, PlayerStats p) {
        activeGlitch = GlitchType.NONE;
        int chance = (15 + level) - (p.talentSignalShield * 8);
        if (level > 3 && random.nextInt(100) < Math.max(5, chance)) {
            if (p.talentGlitchImmune && random.nextInt(100) < 30) return; // 30% 完全免疫

            int g = random.nextInt(3);
            if (g == 0) activeGlitch = GlitchType.NETWORK_LAG;
            else if (g == 1) activeGlitch = GlitchType.VISUAL_DISTORTION;
            else if (g == 2) activeGlitch = GlitchType.CORE_OVERLOAD;
        }
    }

    // === 天賦：連擊保險 (Combo Guard) ===
    public void dropCombo(PlayerStats p) {
        if (p.talentComboGuard && comboMultiplier > 1.0) {
            comboMultiplier = Math.max(1.0, comboMultiplier - 1.0);
            comboFrames = (int)((comboMultiplier - 1.0) * 180.0);
        } else {
            comboFrames = 0;
            comboMultiplier = 1.0;
        }
    }

    public BossType determineBossType(int level) {
        if (level % 5 != 0) return BossType.NONE;
        if (level == 5) return BossType.PULSE;
        if (level == 10) return BossType.SURGE;
        if (level == 15) return BossType.MIMIC;
        if (level == 20) return BossType.ARCHITECT;
        if (level == 25) return BossType.CERBERUS;
        if (level == 30) return BossType.HYDRA;
        if (level == 35) return BossType.SPECTER;
        return BossType.NULL_GOD;
    }

    public boolean isBossLevel(int level) { return level % 5 == 0; }

    public String generateBossAlphaNum(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    public void shiftArchitectTarget() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<decryptTarget.length(); i++) sb.append((char)(random.nextInt(26) + 'A'));
        decryptTarget = sb.toString(); decryptInput = "";
    }

    public double getSurgeElapsed() {
        if (!isSurgeFight) return 0.0;
        return (System.nanoTime() - surgeStartTime) / 1_000_000_000.0;
    }

    public void generateSurgeAttack(double elapsed, long now) {
        for(int i=0; i<5; i++) { surgeWarnings[i] = false; }
        double warningTime = 0.8;
        long nextInterval = 1_500_000_000L;
        int targetsCount = 1;
        isSurgeFakeOut = false;

        if (elapsed < 20) {
            targetsCount = 1; warningTime = 0.8; nextInterval = 1_500_000_000L;
        } else if (elapsed < 45) {
            targetsCount = 1; warningTime = 0.6;
            nextInterval = random.nextBoolean() ? 400_000_000L : 1_200_000_000L;
        } else if (elapsed < 70) {
            targetsCount = 2; warningTime = 0.6; nextInterval = 1_200_000_000L;
            if (random.nextDouble() < 0.25) isSurgeFakeOut = true;
        } else {
            targetsCount = 3; warningTime = 0.5; nextInterval = 900_000_000L;
        }

        if (targetsCount == 3) {
            int furthest = 2;
            if (surgePlayerPos < 2) furthest = 4;
            else if (surgePlayerPos > 2) furthest = 0;
            else furthest = random.nextBoolean() ? 0 : 4;
            surgeWarnings[furthest] = true;
            targetsCount--;
        }

        while (targetsCount > 0) {
            int r = random.nextInt(5);
            if (!surgeWarnings[r]) { surgeWarnings[r] = true; targetsCount--; }
        }

        surgeWarnEndTime = now + (long)(warningTime * 1_000_000_000L);
        surgeNextAttackTime = surgeWarnEndTime + nextInterval;
    }

    public void updateSurgeHeal(double elapsed) {
        if (elapsed >= 30.0 && surgeHealsDone == 0) { surgeHP = Math.min(3, surgeHP + 1); surgeHealsDone++; }
        if (elapsed >= 60.0 && surgeHealsDone == 1) { surgeHP = Math.min(3, surgeHP + 1); surgeHealsDone++; }
    }

    public void startPulseFirewall(long now) {
        isPulseFight = true; pulseScanPos = 0.0; pulseScanDir = 1.0;
        pulseHitsCount = 0; pulseJustHit = false; pulseHitsRequired = 5;
        isFirewallFight = true; firewallProgress = 0.0; pulsePhase1Deadline = now + 15_000_000_000L;
    }
    public void startPulseBeat(PlayerStats p, long now) {
        isInterceptFight = true; sequenceIndex = 0; eventMistakes = 0; int len = 4 + (p.currentLevel / 4);
        String[] dirs = {"W","A","S","D"}; StringBuilder sb = new StringBuilder();
        for (int i=0; i<len; i++) sb.append(dirs[random.nextInt(4)]);
        targetSequence = sb.toString(); pulseLetterDeadlines = new long[len];
        for (int i=0; i<len; i++) pulseLetterDeadlines[i] = now + pulseBeatInterval * (i + 1);
        interceptDeadline = now + pulseBeatInterval * (len + 2);
    }
    public void startPulseReveal(PlayerStats p, long now) {
        isDecryptFight = true; decryptInput = ""; eventMistakes = 0; int len = 4 + (p.currentLevel / 5);
        StringBuilder sb = new StringBuilder(); for(int i=0; i<len; i++) sb.append((char)(random.nextInt(26) + 'A'));
        decryptTarget = sb.toString(); pulseRevealChars = decryptTarget.split(""); pulseRevealIndex = 0; pulseAllRevealed = false;
        pulseNextRevealTime = now + 1_500_000_000L; decryptDeadline = now + (long)(15.0 * 1_000_000_000L); isDecryptFlashed = false; decryptFlashEndTime = 0;
    }
    public void updatePulseScan(long now) {
        pulseScanPos += pulseScanSpeed * pulseScanDir;
        if (pulseScanPos >= 1.0) { pulseScanPos = 1.0; pulseScanDir = -1.0; }
        if (pulseScanPos <= 0.0) { pulseScanPos = 0.0; pulseScanDir = 1.0; }
        if (pulseJustHit && now > pulseHitFlashEnd) pulseJustHit = false;
    }
    public int judgePulseHit() {
        if (pulseScanPos >= pulseZoneMin && pulseScanPos <= pulseZoneMax) {
            pulseHitsCount++; firewallProgress = (double) pulseHitsCount / pulseHitsRequired; pulseJustHit = true; pulseHitFlashEnd = System.nanoTime() + 200_000_000L; return 1;
        } else if (pulseScanPos <= pulseZoneMax + 0.15 && pulseScanPos >= pulseZoneMin - 0.15) { return 0; } else {
            firewallProgress = Math.max(0, firewallProgress - 0.2); pulseHitsCount = (int)(firewallProgress * pulseHitsRequired); return -1;
        }
    }

    public void startInterceptEvent(PlayerStats p, long now) {
        isInterceptFight = true; sequenceIndex = 0; eventMistakes = 0;
        if(p.talentErrorCorrect) errorCorrectCharges = 2;
        int len = 4 + (p.currentLevel / 3);
        String[] dirs = {"W", "A", "S", "D"}; StringBuilder sb = new StringBuilder();
        for (int i=0; i<len; i++) sb.append(dirs[random.nextInt(4)]);
        targetSequence = sb.toString();
        displaySequence = targetSequence;
        // 如果目前是 MIMIC 戰，故意把玩家畫面上顯示的提示字串反轉，達到欺敵效果
        if (currentBossType == BossType.MIMIC || p.currentLevel == 999) {
            displaySequence = new StringBuilder(targetSequence).reverse().toString();
        }
        double time = 4.0 - (p.currentLevel * 0.05); if(time < 1.5) time = 1.5;
        if (activeGlitch == GlitchType.NETWORK_LAG) time *= 0.6;
        interceptDeadline = now + (long)(time * 1_000_000_000L);
    }
    public void startDecryptEvent(PlayerStats p, long now) {
        isDecryptFight = true; decryptInput = ""; isDecryptFlashed = false; eventMistakes = 0;
        if(p.talentErrorCorrect) errorCorrectCharges = 2;
        int len = 4 + (p.currentLevel / 4);
        StringBuilder sb = new StringBuilder(); for(int i=0; i<len; i++) sb.append((char)(random.nextInt(26) + 'A'));
        decryptTarget = sb.toString();
        // 如果是 MIMIC 戰，玩家必須反過來輸入才算對，所以我們把底層的比對目標直接反轉
        if (currentBossType == BossType.MIMIC || p.currentLevel == 999) {
            decryptTarget = new StringBuilder(decryptTarget).reverse().toString();
        }
        double flashTime = 1.2 + (p.talentFlashTime * 0.15) - (p.currentLevel * 0.03); if(flashTime < 0.4) flashTime = 0.4;
        decryptFlashEndTime = now + (long)(flashTime * 1_000_000_000L);
        double totalTime = 5.0 - (p.currentLevel * 0.05); if(totalTime < 2.5) totalTime = 2.5;
        if (activeGlitch == GlitchType.NETWORK_LAG) totalTime *= 0.6;
        decryptDeadline = now + (long)(totalTime * 1_000_000_000L);
    }
    public void startBugCatchEvent(PlayerStats p, long now) {
        isBugCatchFight = true; bugsCaught = 0; double baseTime = 15.0 - (p.currentLevel * 0.2); if(baseTime < 7.0) baseTime = 7.0;
        bugCatchDeadline = now + (long)(baseTime * 1_000_000_000L); lastBugSpawnTime = now;
    }

    // === 天賦：木馬分裂 (Trojan Split) ===
    public int useEMP(PlayerStats p) {
        if (p.empCharges > 0 && isFirewallFight && currentBossType != BossType.PULSE) {
            p.empCharges--;
            if(currentBossType == BossType.HYDRA && isBossFight) { for(int i=0; i<3; i++) hydraWalls[i] = Math.min(1.0, hydraWalls[i] + 0.4); }
            else { firewallProgress = Math.min(1.0, firewallProgress + 0.4); }

            if (p.talentTrojanSplit && random.nextDouble() < 0.25) {
                if (random.nextBoolean()) p.empCharges++; else p.slowCharges++;
                return 2; // 回傳 2 代表觸發了天賦退款
            }
            return 1;
        } return 0;
    }
    public boolean useSlow(PlayerStats p) {
        if (p.slowCharges > 0) {
            if (isInterceptFight && currentBossType != BossType.PULSE) { p.slowCharges--; interceptDeadline += 3_000_000_000L; return true; }
            if (isDecryptFight) { p.slowCharges--; decryptDeadline += 3_000_000_000L; return true; }
            if (isBugCatchFight) { p.slowCharges--; bugCatchDeadline += 3_000_000_000L; return true; }
        } return false;
    }

    public void updateMaxCombo() { if (comboMultiplier > runMaxCombo) runMaxCombo = comboMultiplier; }
    public int getRunAPM() { long duration = System.nanoTime() - runStartTime; double mins = duration / 60_000_000_000.0; return mins > 0 ? (int)(runTotalKeystrokes / mins) : 0; }
    public double getRunAccuracy() { return runTotalKeystrokes > 0 ? ((double)runCorrectKeystrokes / runTotalKeystrokes) * 100 : 100.0; }
}