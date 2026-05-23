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

    public final Random random = new Random();

    public void resetEvents() {
        isHacking = false; progress = 0.0; currentSegment = 0; comboFrames = 0; comboMultiplier = 1.0;
        isFirewallFight = false; firewallProgress = 0.5; isInterceptFight = false; isDecryptFight = false;
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

    public boolean useEMP(PlayerStats p) {
        if (p.empCharges > 0 && isFirewallFight) { p.empCharges--; firewallProgress += 0.4; return true; }
        return false;
    }

    public boolean useSlow(PlayerStats p) {
        if (p.slowCharges > 0 && (isInterceptFight || isDecryptFight)) {
            p.slowCharges--; interceptDeadline += 2_500_000_000L; decryptDeadline += 2_500_000_000L; return true;
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