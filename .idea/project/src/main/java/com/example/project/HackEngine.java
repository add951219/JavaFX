package com.example.project;

import java.util.Random;

public class HackEngine {
    // 新增 ROUTE_SELECT 狀態
    public enum GameState { MAIN_MENU, INTRO, PLAYING, PAUSED, ROUTE_SELECT, SHOP, GAMEOVER }
    public GameState currentState = GameState.MAIN_MENU;

    public boolean isHacking = false;
    public double progress = 0.0;
    public int currentSegment = 0;
    public int totalSegments = 4; // Boss 戰可以改變這個長度

    public double comboMultiplier = 1.0;
    public int comboFrames = 0;

    public boolean isFirewallFight = false;
    public double firewallProgress = 0.5;

    public boolean isInterceptFight = false;
    public String targetSequence = "";
    public int sequenceIndex = 0;
    public long interceptDeadline = 0;

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
        isHoneypotActive = false;
    }

    public boolean isBossLevel(int level) {
        return level % 5 == 0;
    }

    // 主動技能 1：EMP (削弱防火牆 40% 血量)
    public boolean useEMP(PlayerStats p) {
        if (p.empCharges > 0 && isFirewallFight) {
            p.empCharges--;
            firewallProgress += 0.4;
            return true;
        }
        return false;
    }

    // 主動技能 2：超頻沙漏 (延長 WASD 攔截時間 2.5 秒)
    public boolean useSlow(PlayerStats p) {
        if (p.slowCharges > 0 && isInterceptFight) {
            p.slowCharges--;
            interceptDeadline += 2_500_000_000L;
            return true;
        }
        return false;
    }

    public String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 1500; i++) sb.append((char)(random.nextInt(94) + 33));
        return sb.toString();
    }
}