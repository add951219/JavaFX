package com.example.project;
import javafx.scene.input.KeyCode;

public class TestModeManager {
    private final HelloApplication app;
    private final HackEngine engine;
    private final UIManager ui;

    public TestModeManager(HelloApplication app, HackEngine engine, UIManager ui) {
        this.app = app; this.engine = engine; this.ui = ui;
    }

    public void jumpToLevel(KeyCode code) {
        if (code == KeyCode.ESCAPE) {
            app.isDemoMode = false;
            app.p.currentLevel = 1; // 強制重置等級
            engine.currentState = HackEngine.GameState.MAIN_MENU;
            ui.hideDemoMenu();
            ui.menuLayer.setVisible(true);
            return;
        }

        long now = System.nanoTime();
        engine.resetEvents();

        boolean valid = true;
        switch (code) {
            // === 基礎事件測試 ===
            case DIGIT1, NUMPAD1 -> {
                app.p.currentLevel = 5;
                engine.isFirewallFight = true;
                engine.firewallProgress = 0.5; // 調整為 0.5 確保具備可玩性與展示容錯率
                ui.firewallLayer.setVisible(true);
                ui.updateFirewallUI();
            }
            case DIGIT2, NUMPAD2 -> {
                app.p.currentLevel = 5;
                engine.startInterceptEvent(app.p, now);
                ui.interceptLayer.setVisible(true);
                ui.updateInterceptUI();
            }
            case DIGIT3, NUMPAD3 -> {
                app.p.currentLevel = 5;
                engine.startDecryptEvent(app.p, now);
                ui.decryptTargetDisplay.setText(engine.decryptTarget);
                ui.decryptLayer.setVisible(true);
                ui.updateDecryptUI();
            }
            case DIGIT4, NUMPAD4 -> {
                app.p.currentLevel = 5;
                engine.startBugCatchEvent(app.p, now);
                ui.bugCatchLayer.setVisible(true);
                ui.spawnBugsForEvent();
                ui.updateBugScoreUI();
            }

            // === 自選 Boss 關卡快捷鍵 ===
            case DIGIT5, NUMPAD5 -> {
                app.p.currentLevel = 5; // 設定對應等級，防止公式長度爆表
                engine.currentBossType = HackEngine.BossType.PULSE;
                engine.bossPhase = 1;
                engine.maxBossPhase = 3;
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            case DIGIT6, NUMPAD6 -> {
                app.p.currentLevel = 10;
                engine.currentBossType = HackEngine.BossType.SURGE;
                engine.bossPhase = 1;
                engine.maxBossPhase = 1; // SURGE 為單一生存階段
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            case DIGIT7, NUMPAD7 -> {
                app.p.currentLevel = 15;
                engine.currentBossType = HackEngine.BossType.CERBERUS;
                engine.bossPhase = 1;
                engine.maxBossPhase = 3;
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            case DIGIT8, NUMPAD8 -> {
                app.p.currentLevel = 20;
                engine.currentBossType = HackEngine.BossType.ARCHITECT;
                engine.bossPhase = 1;
                engine.maxBossPhase = 3;
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            case DIGIT9, NUMPAD9 -> {
                app.p.currentLevel = 25;
                engine.currentBossType = HackEngine.BossType.MIMIC;
                engine.bossPhase = 1;
                engine.maxBossPhase = 3;
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            case DIGIT0, NUMPAD0 -> {
                app.p.currentLevel = 30;
                engine.currentBossType = HackEngine.BossType.HYDRA;
                engine.bossPhase = 1;
                engine.maxBossPhase = 3;
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            case MINUS -> {
                app.p.currentLevel = 35;
                engine.currentBossType = HackEngine.BossType.SPECTER;
                engine.bossPhase = 1;
                engine.maxBossPhase = 3;
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            case EQUALS -> {
                app.p.currentLevel = 40;
                engine.currentBossType = HackEngine.BossType.NULL_GOD;
                engine.bossPhase = 1;
                engine.maxBossPhase = 4;
                engine.isBossFight = true;
                app.bossManager.startBossPhase();
            }
            default -> valid = false;
        }

        if (valid) {
            app.isDemoMode = true; // 開啟 Demo 鎖定
            engine.currentState = HackEngine.GameState.PLAYING;
            ui.hideDemoMenu();
            ui.menuLayer.setVisible(false);
            ui.gameLayer.setVisible(true);
            ui.updateASCIIProgress();
        }
    }
}