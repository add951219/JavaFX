package com.example.project;

import java.util.Random;

public class HackEngine {
    public enum GameState { MAIN_MENU, INTRO, PLAYING, PAUSED, ROUTE_SELECT, SHOP, GAMEOVER }
    public GameState currentState = GameState.MAIN_MENU;

    public boolean isHacking = false;
    public double progress = 0.0;
    public int currentSegment = 0;
    public int totalSegments = 4;

    public double comboMultiplier = 1.0;
    public int comboFrames = 0;

    // 事件 1：防火牆
    public boolean isFirewallFight = false;
    public double firewallProgress = 0.5;

    // 事件 2：攔截
    public boolean isInterceptFight = false;
    public String targetSequence = "";
    public int sequenceIndex = 0;
    public long interceptDeadline = 0;

    // 事件 3：解密矩陣 (新增)
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

    // 產生英數混合密碼
    public String generateAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}