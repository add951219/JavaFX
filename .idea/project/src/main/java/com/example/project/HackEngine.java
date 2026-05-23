package com.example.project;

import java.util.Random;

public class HackEngine {
    public enum GameState { MAIN_MENU, INTRO, PLAYING, PAUSED, ROUTE_SELECT, SHOP, GAMEOVER, TALENT_TREE }
    public GameState currentState = GameState.MAIN_MENU;

    // === 環境詛咒類型 ===
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

    public boolean isHoneypotActive = false;
    public long honeypotExpireTime = 0;

    public final Random random = new Random();

    public void resetEvents() {
        isHacking = false;
        progress = 0.0;
        currentSegment = 0;
        comboFrames = 0;
        comboMultiplier = 1.0;
        isFirewallFight = false;
        firewallProgress = 0.5;
        isInterceptFight = false;
        isDecryptFight = false;
        isHoneypotActive = false;
    }

    public boolean isBossLevel(int level) {
        return level % 5 == 0;
    }

    public void rollGlitch(int level) {
        if (isBossLevel(level)) {
            activeGlitch = GlitchType.NONE;
            return;
        }
        int r = random.nextInt(4); // 0: 無, 1: 延遲, 2: 視覺, 3: 超載
        if (r == 1) activeGlitch = GlitchType.NETWORK_LAG;
        else if (r == 2) activeGlitch = GlitchType.VISUAL_DISTORTION;
        else if (r == 3) activeGlitch = GlitchType.CORE_OVERLOAD;
        else activeGlitch = GlitchType.NONE;
    }

    // === 新增：將 WASD 攔截事件的數學計算徹底封裝 ===
    public void startInterceptEvent(PlayerStats p, long now) {
        isInterceptFight = true;
        sequenceIndex = 0;

        // 計算密碼長度
        int len = (int)(4 + (p.currentLevel * p.routeDiffMult / 2));
        String[] pool = {"W", "A", "S", "D"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(pool[random.nextInt(pool.length)]);
        targetSequence = sb.toString();

        // 套用環境延遲詛咒係數
        double lagModifier = (activeGlitch == GlitchType.NETWORK_LAG) ? 0.6 : 1.0;
        double baseTime = Math.max(1.5, 5.0 - p.currentLevel * 0.2);
        interceptDeadline = now + (long)(baseTime * 1_000_000_000L * lagModifier);
    }

    // === 新增：將解密矩陣事件的數學計算徹底封裝 ===
    public void startDecryptEvent(PlayerStats p, long now) {
        isDecryptFight = true;
        isDecryptFlashed = false;
        decryptInput = "";

        int len = Math.min(6, 3 + (p.currentLevel / 3));
        decryptTarget = generateAlphanumeric(len);

        // 套用天賦加成與延遲詛咒係數
        double lagModifier = (activeGlitch == GlitchType.NETWORK_LAG) ? 0.6 : 1.0;
        decryptFlashEndTime = now + 500_000_000L + (p.talentFlashTime * 150_000_000L);
        decryptDeadline = now + (long)(5.0 * 1_000_000_000L * lagModifier);
    }

    public boolean useEMP(PlayerStats p) {
        if (p.empCharges > 0 && isFirewallFight) {
            p.empCharges--;
            firewallProgress += 0.4;
            return true;
        }
        return false;
    }

    public boolean useSlow(PlayerStats p) {
        if (p.slowCharges > 0 && (isInterceptFight || isDecryptFight)) {
            p.slowCharges--;
            interceptDeadline += 2_500_000_000L;
            decryptDeadline += 2_500_000_000L;
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