package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HelloApplication extends Application {

    // 引入三大模組：數據(Model)、邏輯(Engine)、畫面(View)
    public final PlayerStats p = new PlayerStats();
    public final HackEngine engine = new HackEngine();
    public UIManager ui;

    // 音效播放器
    private AudioClip errorSound;

    @Override
    public void start(Stage stage) {
        initAudio(); // 載入音效

        // 將核心交給 UIManager 進行畫面渲染
        ui = new UIManager(p, engine, this);
        Scene scene = new Scene(ui.root, 800, 600);

        setupInputHandlers(scene);
        startGameLoop();

        stage.setScene(scene);
        stage.setTitle("Neon Breach - Meta Progression Edition");
        stage.show();

        // 初始同步天賦樹資訊
        ui.updateTalentUI();
        ui.root.requestFocus();
    }

    // 初始化音效的方法
    private void initAudio() {
        try {
            // 讀取 resources 資料夾下的 error.mp3
            String soundPath = getClass().getResource("/error.mp3").toExternalForm();
            errorSound = new AudioClip(soundPath);
        } catch (Exception e) {
            System.out.println("找不到音效檔案，請確認 src/main/resources/error.mp3 是否存在！");
        }
    }

    // 播放錯誤音效的捷徑
    private void playErrorSound() {
        if (errorSound != null) {
            errorSound.play();
        }
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnMousePressed(e -> {
            if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight) {
                engine.isHacking = true;
            }
        });
        scene.setOnMouseReleased(e -> {
            if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY) {
                engine.isHacking = false;
            }
        });

        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (engine.currentState == HackEngine.GameState.PLAYING) {
                    engine.currentState = HackEngine.GameState.PAUSED;
                    ui.pauseLayer.setVisible(true);
                } else if (engine.currentState == HackEngine.GameState.PAUSED) {
                    engine.currentState = HackEngine.GameState.PLAYING;
                    ui.pauseLayer.setVisible(false);
                }
            }

            if (engine.currentState != HackEngine.GameState.PLAYING) return;

            // 施放技能
            if (e.getCode() == KeyCode.DIGIT1 && engine.useEMP(p)) {
                ui.typeWriterUpdate(">>> EMP DEPLOYED! FIREWALL DAMAGED.");
                ui.updateShopUI(); ui.updateFirewallUI();
            }
            if (e.getCode() == KeyCode.DIGIT2 && engine.useSlow(p)) {
                ui.typeWriterUpdate(">>> OVERCLOCK ENGAGED! TIME SLOWED.");
                ui.updateShopUI();
            }

            // 事件對抗邏輯
            if (e.getCode() == KeyCode.SPACE && engine.isFirewallFight) {
                engine.firewallProgress += 0.05 + (p.upgClick * 0.015);
                ui.updateFirewallUI();
                e.consume();
            }

            // WASD 攔截邏輯
            if (engine.isInterceptFight) {
                String inputKey = e.getCode().toString();
                if (engine.sequenceIndex < engine.targetSequence.length()) {
                    String expectedKey = engine.targetSequence.substring(engine.sequenceIndex, engine.sequenceIndex + 1);
                    if (inputKey.equals(expectedKey)) {
                        engine.sequenceIndex++;
                        ui.updateInterceptUI();
                        if (engine.sequenceIndex >= engine.targetSequence.length()) {
                            engine.isInterceptFight = false;
                            ui.interceptLayer.setVisible(false);
                            ui.typeWriterUpdate(">>> PACKET SECURED.");
                            if(!engine.isFirewallFight) engine.currentSegment++;
                        }
                    } else if (inputKey.matches("[A-Z]")) {
                        // 打錯字母時播放音效！
                        playErrorSound();
                    }
                }
                e.consume();
            }

            // 解密矩陣邏輯
            if (engine.isDecryptFight) {
                if (e.getCode().isLetterKey() || e.getCode().isDigitKey()) {
                    engine.decryptInput += e.getCode().toString();
                    ui.updateDecryptUI();

                    if (engine.decryptInput.length() >= engine.decryptTarget.length()) {
                        if (engine.decryptInput.equals(engine.decryptTarget)) {
                            engine.isDecryptFight = false;
                            ui.decryptLayer.setVisible(false);
                            ui.typeWriterUpdate(">>> ENCRYPTION BROKEN.");
                            engine.currentSegment++;
                        } else {
                            ui.shakeScreen();
                            playErrorSound(); // 密碼錯誤時播放音效！
                            engine.decryptInput = "";
                            engine.decryptDeadline -= 1_000_000_000L;
                            ui.updateDecryptUI();
                        }
                    }
                } else if (e.getCode() == KeyCode.BACK_SPACE && engine.decryptInput.length() > 0) {
                    engine.decryptInput = engine.decryptInput.substring(0, engine.decryptInput.length() - 1);
                    ui.updateDecryptUI();
                }
                e.consume();
            }
        });
    }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (engine.currentState != HackEngine.GameState.PLAYING) return;

            long now = System.nanoTime();
            if (engine.random.nextInt(10) == 0) ui.matrixBg.setText(engine.generateRandomCode());

            if (engine.isHacking && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight) {
                engine.comboFrames++;
                engine.comboMultiplier = Math.min(3.0, 1.0 + (engine.comboFrames / 180.0));
            } else {
                engine.comboFrames = 0;
                engine.comboMultiplier = 1.0;
            }
            ui.comboDisplay.setText(String.format("COMBO: x%.1f", engine.comboMultiplier));

            if (engine.isFirewallFight) {
                engine.firewallProgress -= (0.003 + (p.currentLevel * p.routeDiffMult * 0.0008));
                ui.updateFirewallUI();
                if (engine.firewallProgress <= 0) triggerGameOver(">>> BLOCKED <<<");
                else if (engine.firewallProgress >= 1.0) {
                    engine.isFirewallFight = false;
                    ui.firewallLayer.setVisible(false);
                    ui.typeWriterUpdate(">>> FIREWALL SHATTERED.");
                    if(!engine.isInterceptFight) engine.currentSegment++;
                }
            }

            if (engine.isInterceptFight) {
                double timeLeft = (engine.interceptDeadline - now) / 1_000_000_000.0;
                ui.interceptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft)));
                if (now > engine.interceptDeadline) handleEventFailure();
            }

            if (engine.isDecryptFight) {
                if (!engine.isDecryptFlashed && now > engine.decryptFlashEndTime) {
                    engine.isDecryptFlashed = true;
                    ui.decryptTargetDisplay.setText("? ? ? ? ?");
                }
                double timeLeft = (engine.decryptDeadline - now) / 1_000_000_000.0;
                ui.decryptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft)));
                if (now > engine.decryptDeadline) handleEventFailure();
            }

            if (!engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight) {
                double checkpointSize = 1.0 / engine.totalSegments;
                double securedProgress = engine.currentSegment * checkpointSize;
                double targetCheckpoint = (engine.currentSegment + 1) * checkpointSize;

                if (engine.isHacking) {
                    engine.progress += 0.0022 + (p.upgSpeed * 0.0006);
                    if (engine.progress >= targetCheckpoint) {
                        engine.progress = targetCheckpoint;
                        engine.isHacking = false;
                        if (engine.currentSegment < engine.totalSegments - 1) triggerCheckpointEvent();
                        else playLevelClearExplosion();
                    }
                } else {
                    engine.progress -= 0.0010 + (p.currentLevel * 0.0004);
                    if (engine.progress < securedProgress) engine.progress = securedProgress;
                }
                ui.updateASCIIProgress();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // --- 遊戲邏輯與流程控制 ---

    public void checkBossLevel() {
        if (engine.isBossLevel(p.currentLevel)) {
            engine.totalSegments = 8;
            ui.uiBorder.setTextFill(Color.RED);
            ui.statusLabel.setTextFill(Color.RED);
            ui.typeWriterUpdate("⚠ WARNING: CORE OVERRIDE DETECTED. PREPARE FOR ASSAULT ⚠");
        } else {
            engine.totalSegments = 4;
            ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5));
            ui.statusLabel.setTextFill(Color.CYAN);
        }
    }

    public void triggerCheckpointEvent() {
        ui.shakeScreen();
        boolean isBoss = engine.isBossLevel(p.currentLevel);

        if (isBoss) {
            // 套用天賦2：弱化防火牆，初始血量降低 (進度增加代表防線變弱)
            engine.isFirewallFight = true;
            engine.firewallProgress = 0.5 + (p.talentWeakFW * 0.05);
            ui.firewallLayer.setVisible(true); ui.updateFirewallUI();

            engine.isInterceptFight = true; engine.sequenceIndex = 0;
            engine.targetSequence = "WASD";
            ui.updateInterceptUI();
            engine.interceptDeadline = System.nanoTime() + (long)(5.0 * 1_000_000_000L);
            ui.interceptLayer.setVisible(true);
        } else {
            int rand = engine.random.nextInt(3);
            if (rand == 0) {
                // 套用天賦2：弱化防火牆
                engine.isFirewallFight = true;
                engine.firewallProgress = 0.5 + (p.talentWeakFW * 0.05);
                ui.firewallLayer.setVisible(true); ui.updateFirewallUI();
            } else if (rand == 1) {
                engine.isInterceptFight = true; engine.sequenceIndex = 0;
                int len = (int)(4 + (p.currentLevel * p.routeDiffMult / 2));
                String[] pool = {"W", "A", "S", "D"}; StringBuilder sb = new StringBuilder();
                for (int i=0; i<len; i++) sb.append(pool[engine.random.nextInt(pool.length)]);
                engine.targetSequence = sb.toString(); ui.updateInterceptUI();
                engine.interceptDeadline = System.nanoTime() + (long)((Math.max(1.5, 5.0 - p.currentLevel * 0.2)) * 1_000_000_000L);
                ui.interceptLayer.setVisible(true);
            } else {
                engine.isDecryptFight = true; engine.isDecryptFlashed = false; engine.decryptInput = "";
                int len = Math.min(6, 3 + (p.currentLevel / 3));
                engine.decryptTarget = engine.generateAlphanumeric(len);
                ui.decryptTargetDisplay.setText(engine.decryptTarget);
                ui.updateDecryptUI();
                long now = System.nanoTime();

                // 套用天賦3：加長閃現記憶時間
                engine.decryptFlashEndTime = now + 500_000_000L + (p.talentFlashTime * 150_000_000L);
                engine.decryptDeadline = now + (long)(5.0 * 1_000_000_000L);
                ui.decryptLayer.setVisible(true);
            }
        }
    }

    public void handleHoneypotTrap() {
        engine.isHoneypotActive = false;
        ui.honeypotBtn.setVisible(false);
        p.darkCoins = Math.max(0, p.darkCoins - 50);
        ui.shakeScreen();
        ui.typeWriterUpdate(">>> HONEYPOT TRIGGERED! COINS DRAINED.");
    }

    public void handleEventFailure() {
        engine.isInterceptFight = false; engine.isDecryptFight = false;
        ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false);
        engine.progress = engine.currentSegment * (1.0 / engine.totalSegments);
        ui.shakeScreen();
        ui.typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED.");
    }

    public void playLevelClearExplosion() {
        engine.currentState = HackEngine.GameState.PAUSED;
        engine.isHacking = false;
        Timeline explosion = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            ui.statusLabel.setText(engine.generateRandomCode().substring(0, 40));
            ui.uiBorder.setTextFill(Color.color(engine.random.nextDouble(), engine.random.nextDouble(), engine.random.nextDouble()));
        }));
        explosion.setCycleCount(15);
        explosion.setOnFinished(e -> triggerLevelClear());
        explosion.play();
    }

    public void triggerLevelClear() {
        int baseReward = engine.isBossLevel(p.currentLevel) ? 500 : 100;
        int earned = (int)((p.currentLevel * baseReward) * engine.comboMultiplier * p.routeRewardMult);
        p.darkCoins += earned;
        p.currentLevel++;
        engine.progress = 0.0; engine.currentSegment = 0;
        if (p.currentLevel > p.highScore) {
            p.highScore = p.currentLevel;
            p.saveData(); // 超越最高紀錄立刻永久存檔
        }

        ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5));
        ui.gameLayer.setVisible(false);
        engine.currentState = HackEngine.GameState.ROUTE_SELECT;
        ui.routeLayer.setVisible(true);
    }

    public void triggerGameOver(String reason) {
        engine.currentState = HackEngine.GameState.GAMEOVER;
        ui.shakeScreen();

        // === 局外永久結算 (將當局剩餘 DarkCoins 的 10% 存入 LegacyCoins) ===
        int earnedLegacy = p.darkCoins / 10;
        p.legacyCoins += earnedLegacy;
        p.saveData(); // 死亡時必定自動存檔
        ui.updateTalentUI(); // 同步主選單數據

        ui.gameOverReasonLabel.setText(reason);
        ui.gameOverStatsLabel.setText("REACHED LAYER: " + p.currentLevel + "\nPERMANENT LEGACY COINS EARNED: +" + earnedLegacy);
        ui.gameOverLayer.setVisible(true);
        ui.firewallLayer.setVisible(false);
        ui.interceptLayer.setVisible(false);
        ui.decryptLayer.setVisible(false);
    }

    // 新增：處理天賦購買邏輯
    public void upgradeTalent(int id, int cost) {
        if (id == 1 && p.talentStartEMP < 3 && p.buyLegacy(cost)) {
            p.talentStartEMP++;
        } else if (id == 2 && p.talentWeakFW < 5 && p.buyLegacy(cost)) {
            p.talentWeakFW++;
        } else if (id == 3 && p.talentFlashTime < 3 && p.buyLegacy(cost)) {
            p.talentFlashTime++;
        } else {
            playErrorSound(); // 錢不夠或滿級就嗶嗶叫
            return;
        }
        p.saveData();
        ui.updateTalentUI();
    }

    // 新增：天賦選單切換
    public void openTalentTree() {
        engine.currentState = HackEngine.GameState.TALENT_TREE;
        ui.menuLayer.setVisible(false);
        ui.updateTalentUI();
        ui.talentLayer.setVisible(true);
    }

    public void closeTalentTree() {
        engine.currentState = HackEngine.GameState.MAIN_MENU;
        ui.talentLayer.setVisible(false);
        ui.menuLayer.setVisible(true);
    }

    public void startIntroSequence() {
        engine.currentState = HackEngine.GameState.INTRO;
        ui.menuLayer.setVisible(false);
        ui.introLayer.setVisible(true);
        Label text = (Label) ui.introLayer.getChildren().get(0);
        String[] lines = {"WAKING UP SYSTEM...", "ACCESS GRANTED."};
        Timeline introTimeline = new Timeline();
        for (int i=0; i<lines.length; i++) {
            final int index = i;
            introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5 * (i+1)), e -> text.setText(lines[index])));
        }
        introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(lines.length * 0.5 + 0.5), e -> {
            ui.introLayer.setVisible(false);
            ui.gameLayer.setVisible(true);
            engine.currentState = HackEngine.GameState.PLAYING;
            checkBossLevel();
        }));
        introTimeline.play();
    }

    public void resetGame() {
        p.reset(); engine.resetEvents();
        ui.gameOverLayer.setVisible(false); ui.gameLayer.setVisible(true);
        engine.currentState = HackEngine.GameState.PLAYING; checkBossLevel();
    }

    public void returnToMenu() {
        ui.pauseLayer.setVisible(false); ui.gameLayer.setVisible(false); ui.shopLayer.setVisible(false);
        ui.gameOverLayer.setVisible(false); ui.routeLayer.setVisible(false); ui.menuLayer.setVisible(true);
        resetGame(); engine.currentState = HackEngine.GameState.MAIN_MENU;
    }

    public void enterShop() {
        ui.routeLayer.setVisible(false);
        ui.updateShopUI();
        ui.shopLayer.setVisible(true);
        engine.currentState = HackEngine.GameState.SHOP;
    }

    public static void main(String[] args) { launch(); }
}