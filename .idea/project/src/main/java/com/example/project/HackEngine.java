package com.example.project;

import java.util.Random;

public class HackEngine {
    public enum GameState { MAIN_MENU, INTRO, PLAYING, PAUSED, SHOP, GAMEOVER }
    public GameState currentState = GameState.MAIN_MENU;

    public boolean isHacking = false;
    public double progress = 0.0;
    public int currentSegment = 0;
    public final int TOTAL_SEGMENTS = 4;

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

    public String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 1500; i++) sb.append((char)(random.nextInt(94) + 33));
        return sb.toString();
    }
}