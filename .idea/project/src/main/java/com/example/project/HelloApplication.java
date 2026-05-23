package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloApplication extends Application {

    public final PlayerStats p = new PlayerStats();
    public final HackEngine engine = new HackEngine();
    public UIManager ui;

    private int selectedBranch = 0;
    private int selectedLevel = 0;
    private AudioClip errorSound1, errorSound2, loseSound;

    // 新增 BGM 播放器
    private MediaPlayer bgmPlayer;

    private ExecutorService audioPool;
    private byte[] cachedSuccessBuf;
    private AudioFormat cachedAudioFormat;

    @Override
    public void start(Stage stage) {
        initAudio();
        ui = new UIManager(p, engine, this);
        Scene scene = new Scene(ui.root, 800, 600);
        setupInputHandlers(scene);
        startGameLoop();
        stage.setScene(scene);
        stage.setTitle("Neon Breach - Cyber Glitch Curse Edition");
        stage.show();
        ui.updateTalentUI();
        ui.root.requestFocus();
    }

    private void initAudio() {
        audioPool = Executors.newFixedThreadPool(4);

        try {
            float sampleRate = 44100f;
            cachedSuccessBuf = new byte[2400];
            for (int i = 0; i < cachedSuccessBuf.length; i++) {
                double frequency = 1050.0 + (200.0 * ((double) i / cachedSuccessBuf.length));
                double angle = i / (sampleRate / frequency) * 2.0 * Math.PI;
                double envelope = Math.exp(-6.5 * i / cachedSuccessBuf.length);
                cachedSuccessBuf[i] = (byte) (Math.sin(angle) * 35 * envelope);
            }
            cachedAudioFormat = new AudioFormat(sampleRate, 8, 1, true, false);
        } catch (Exception e) {
            System.out.println("音訊快取生成失敗");
        }

        try {
            errorSound1 = new AudioClip(getClass().getResource("/error1.mp3").toExternalForm());
            errorSound2 = new AudioClip(getClass().getResource("/error2.mp3").toExternalForm());
            loseSound = new AudioClip(getClass().getResource("/lose.mp3").toExternalForm());
        } catch (Exception e) {
            System.out.println("音效載入失敗！請確認 resources 資料夾中存有 error1.mp3、error2.mp3 與 lose.mp3");
        }

        // 載入並播放背景音樂
        try {
            Media bgmMedia = new Media(getClass().getResource("/bgm.mp3").toExternalForm());
            bgmPlayer = new MediaPlayer(bgmMedia);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); // 設定無限循環
            bgmPlayer.setVolume(0.5); // 預設音量 50%
            bgmPlayer.play(); // 遊戲啟動時直接播放
        } catch (Exception e) {
            System.out.println("背景音樂載入失敗！請確認 resources 資料夾中存有 bgm.mp3");
        }
    }

    // 讓 UIManager 可以調整音量
    public void setBgmVolume(double vol) {
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(vol);
        }
    }

    public void playErrorSound(int type) {
        if (type == 1 && errorSound1 != null) {
            if (errorSound1.isPlaying()) errorSound1.stop();
            errorSound1.play();
        } else if (type == 2 && errorSound2 != null) {
            if (errorSound2.isPlaying()) errorSound2.stop();
            errorSound2.play();
        }
    }

    public void playSuccessSound() {
        if (audioPool == null || cachedSuccessBuf == null || cachedAudioFormat == null) return;
        audioPool.submit(() -> {
            try {
                SourceDataLine sdl = AudioSystem.getSourceDataLine(cachedAudioFormat);
                sdl.open(cachedAudioFormat);
                sdl.start();
                sdl.write(cachedSuccessBuf, 0, cachedSuccessBuf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception e) {
                // 靜音降級處理
            }
        });
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnMousePressed(e -> {
            if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight) {
                engine.isHacking = true;
                ui.playComboHitEffect(engine.comboMultiplier);
            }
        });
        scene.setOnMouseReleased(e -> { if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY) engine.isHacking = false; });
        scene.setOnKeyPressed(e -> {
            if (!ui.root.isFocused()) ui.root.requestFocus();
            if (engine.currentState == HackEngine.GameState.PLAYING) {
                if (e.getCode() == KeyCode.DIGIT1 || e.getCode() == KeyCode.NUMPAD1) {
                    if (engine.activeGlitch == HackEngine.GlitchType.CORE_OVERLOAD) ui.typeWriterUpdate("⚠ BLOCKED: CORE OVERLOAD ACTIVE ⚠");
                    else if (engine.useEMP(p)) { ui.typeWriterUpdate(">>> EMP DEPLOYED!"); ui.updateShopUI(); ui.updateFirewallUI(); }
                }
                if (e.getCode() == KeyCode.DIGIT2 || e.getCode() == KeyCode.NUMPAD2) {
                    if (engine.useSlow(p)) {
                        ui.typeWriterUpdate(">>> TIME DILATION ACTIVE! COOLDOWN EXTENDED.");
                        ui.updateShopUI();
                    }
                }
                if (e.getCode() == KeyCode.SPACE && engine.isFirewallFight) {
                    engine.firewallProgress += 0.05 + (p.upgClick * 0.015);
                    ui.updateFirewallUI();
                    ui.playFirewallSpacePopEffect();
                    ui.playComboHitEffect(engine.comboMultiplier);
                }
                if (engine.isInterceptFight) {
                    String input = e.getText().toUpperCase();
                    if (!input.isEmpty() && engine.sequenceIndex < engine.targetSequence.length()) {
                        if (input.equals(engine.targetSequence.substring(engine.sequenceIndex, engine.sequenceIndex + 1))) {
                            engine.sequenceIndex++;
                            playSuccessSound();
                            ui.updateInterceptUI();
                            ui.playComboHitEffect(engine.comboMultiplier);
                            if (engine.sequenceIndex >= engine.targetSequence.length()) {
                                engine.isInterceptFight = false; ui.interceptLayer.setVisible(false);
                                ui.typeWriterUpdate(">>> PACKET SECURED.");
                                if(!engine.isFirewallFight) engine.currentSegment++;
                            }
                        } else {
                            ui.triggerErrorEffect(ui.errorImage1, 1);
                            engine.interceptDeadline -= 1_000_000_000L;
                        }
                    }
                }
                if (engine.isDecryptFight) {
                    String inputChar = ""; KeyCode code = e.getCode();
                    if (code.isLetterKey()) inputChar = code.toString();
                    else if (code.isDigitKey()) inputChar = code.toString().replace("DIGIT", "");
                    else if (code.isKeypadKey() && code.toString().startsWith("NUMPAD")) inputChar = code.toString().replace("NUMPAD", "");
                    if (!inputChar.isEmpty()) {
                        engine.decryptInput += inputChar;
                        playSuccessSound();
                        ui.updateDecryptUI();
                        ui.playComboHitEffect(engine.comboMultiplier);
                        if (engine.decryptInput.length() >= engine.decryptTarget.length()) {
                            if (engine.decryptInput.equals(engine.decryptTarget)) {
                                engine.isDecryptFight = false; ui.decryptLayer.setVisible(false);
                                ui.typeWriterUpdate(">>> ENCRYPTION BROKEN."); engine.currentSegment++;
                            } else {
                                ui.triggerErrorEffect(ui.errorImage2, 2);
                                engine.decryptInput = "";
                                engine.decryptDeadline -= 1_000_000_000L;
                                ui.updateDecryptUI();
                            }
                        }
                    } else if (code == KeyCode.BACK_SPACE && engine.decryptInput.length() > 0) {
                        engine.decryptInput = engine.decryptInput.substring(0, engine.decryptInput.length() - 1); ui.updateDecryptUI();
                    }
                }
            }
            if (e.getCode() == KeyCode.ESCAPE && engine.currentState == HackEngine.GameState.PLAYING) { engine.currentState = HackEngine.GameState.PAUSED; ui.pauseLayer.setVisible(true); }
        });
    }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (engine.currentState != HackEngine.GameState.PLAYING) return;
            long now = System.nanoTime();

            if (engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION) {
                ui.root.setTranslateX((engine.random.nextDouble() - 0.5) * 6.5);
                ui.root.setTranslateY((engine.random.nextDouble() - 0.5) * 6.5);
                if (engine.random.nextInt(15) == 0) {
                    ui.statusLabel.setText("⚠ CRITICAL_ERROR: LINE_FRACTURE_DETECTION ⚠");
                }
            } else {
                ui.root.setTranslateX(0);
                ui.root.setTranslateY(0);
            }

            if (engine.random.nextInt(10) == 0) {
                ui.matrixBg.setText(engine.generateRandomCode());
                ui.matrixBg.setTextFill(engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION ? Color.rgb(255, 0, 50, 0.35) : Color.rgb(0, 255, 204, 0.15));
            }
            if (engine.isHacking && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight) {
                engine.comboFrames++; engine.comboMultiplier = Math.min(3.0, 1.0 + (engine.comboFrames / 180.0));
                if (engine.comboMultiplier >= 2.0 && engine.random.nextInt(4) == 0) ui.playComboHitEffect(engine.comboMultiplier);
            } else { engine.comboFrames = 0; engine.comboMultiplier = 1.0; }
            ui.updateComboDisplay(engine.comboMultiplier);

            if (engine.isFirewallFight) {
                double drainRate = (0.003 + (p.currentLevel * p.routeDiffMult * 0.0008));
                if (engine.isBossLevel(p.currentLevel)) {
                    drainRate *= 0.35;
                }
                engine.firewallProgress -= drainRate;
                ui.updateFirewallUI();
                if (engine.firewallProgress <= 0) triggerGameOver(">>> BLOCKED <<<");
                else if (engine.firewallProgress >= 1.0) { engine.isFirewallFight = false; ui.firewallLayer.setVisible(false); ui.typeWriterUpdate(">>> FIREWALL SHATTERED."); if(!engine.isInterceptFight) engine.currentSegment++; }
            }
            if (engine.isInterceptFight) {
                double timeLeft = (engine.interceptDeadline - now) / 1_000_000_000.0;
                ui.interceptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft)));
                if (now > engine.interceptDeadline) handleEventFailure();
            }
            if (engine.isDecryptFight) {
                if (!engine.isDecryptFlashed && now > engine.decryptFlashEndTime) { engine.isDecryptFlashed = true; ui.decryptTargetDisplay.setText("? ? ? ? ?"); }
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
                        engine.progress = targetCheckpoint; engine.isHacking = false;
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
        timeline.setCycleCount(Timeline.INDEFINITE); timeline.play();
    }
    public void checkBossLevel() { engine.rollGlitch(p.currentLevel); ui.updateGlitchDisplay(); if (engine.isBossLevel(p.currentLevel)) { engine.totalSegments = 8; ui.uiBorder.setTextFill(Color.RED); ui.statusLabel.setTextFill(Color.RED); ui.typeWriterUpdate("⚠ WARNING: CORE OVERRIDE DETECTED. PREPARE FOR ASSAULT ⚠"); } else { engine.totalSegments = 4; ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); ui.statusLabel.setTextFill(Color.CYAN); } }
    public void triggerCheckpointEvent() {
        ui.shakeScreen(); long MathNow = System.nanoTime();
        if (engine.isBossLevel(p.currentLevel)) {
            engine.isFirewallFight = true; engine.firewallProgress = 0.5 + (p.talentWeakFW * 0.05); ui.firewallLayer.setVisible(true); ui.updateFirewallUI();
            engine.isInterceptFight = true; engine.sequenceIndex = 0;

            String[] directions = {"W", "A", "S", "D"};
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(directions[engine.random.nextInt(4)]);
            }
            engine.targetSequence = sb.toString();

            ui.updateInterceptUI();
            engine.interceptDeadline = MathNow + (long)(14.0 * 1_000_000_000L);
            ui.interceptLayer.setVisible(true);
        } else {
            int rand = engine.random.nextInt(3);
            if (rand == 0) { engine.isFirewallFight = true; engine.firewallProgress = 0.5 + (p.talentWeakFW * 0.05); ui.firewallLayer.setVisible(true); ui.updateFirewallUI(); }
            else if (rand == 1) { engine.startInterceptEvent(p, MathNow); ui.updateInterceptUI(); ui.interceptLayer.setVisible(true); }
            else { engine.startDecryptEvent(p, MathNow); ui.decryptTargetDisplay.setText(engine.decryptTarget); ui.updateDecryptUI(); ui.decryptLayer.setVisible(true); }
        }
    }
    public void handleHoneypotTrap() { }
    public void handleEventFailure() { engine.isInterceptFight = false; engine.isDecryptFight = false; ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false); engine.progress = engine.currentSegment * (1.0 / engine.totalSegments); ui.shakeScreen(); ui.typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED."); }
    public void playLevelClearExplosion() { engine.currentState = HackEngine.GameState.PAUSED; engine.isHacking = false; Timeline explosion = new Timeline(new KeyFrame(Duration.millis(50), e -> { ui.statusLabel.setText(engine.generateRandomCode().substring(0, 40)); ui.uiBorder.setTextFill(Color.color(engine.random.nextDouble(), engine.random.nextDouble(), engine.random.nextDouble())); })); explosion.setCycleCount(15); explosion.setOnFinished(e -> triggerLevelClear()); explosion.play(); }
    public void triggerLevelClear() { int baseReward = engine.isBossLevel(p.currentLevel) ? 500 : 100; int earned = (int)((p.currentLevel * baseReward) * engine.comboMultiplier * p.routeRewardMult); p.darkCoins += earned; p.currentLevel++; engine.progress = 0.0; engine.currentSegment = 0; if (p.currentLevel > p.highScore) p.highScore = p.currentLevel; ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); ui.gameLayer.setVisible(false); engine.currentState = HackEngine.GameState.ROUTE_SELECT; ui.routeLayer.setVisible(true); }

    public void triggerGameOver(String reason) {
        if (loseSound != null) { if (loseSound.isPlaying()) loseSound.stop(); loseSound.play(); }
        engine.currentState = HackEngine.GameState.GAMEOVER;
        ui.shakeScreen();
        int earnedLegacy = p.darkCoins / 10;
        p.legacyCoins += earnedLegacy;

        try {
            p.saveData();
        } catch (Exception ex) {
            System.out.println("⚠ CRITICAL IO ERROR: 數據同步寫入失敗，防止寫入中斷死當。");
        }

        ui.updateTalentUI();
        ui.gameOverReasonLabel.setText(reason);
        ui.gameOverStatsLabel.setText("REACHED LAYER: " + p.currentLevel + "\nPERMANENT LEGACY COINS EARNED: +" + earnedLegacy);
        ui.gameOverLayer.setVisible(true); ui.firewallLayer.setVisible(false); ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false);
    }

    public void selectTalentNode(int branchId, int level) { this.selectedBranch = branchId; this.selectedLevel = level; int currentLevelInBranch = (branchId == 1) ? p.talentStartEMP : (branchId == 2 ? p.talentWeakFW : p.talentFlashTime); int cost = (branchId == 1) ? 50 : (branchId == 2 ? 75 : 100); String branchName = (branchId == 1) ? "控制組件優化 [EMP 強化]" : (branchId == 2 ? "防火牆漏洞利用 [FW 弱化]" : "緩衝記憶體擴充 [FLASH 記憶]"); ui.talentNameLabel.setText(String.format(">>> 解密節點：%s (等級 %d) <<<", branchName, level)); ui.talentEffectLabel.setText(branchId == 1 ? String.format("加成效果：自帶 %d 顆 EMP 脈衝彈。", level) : (branchId == 2 ? String.format("加成效果：FW 弱化 +%d%%。", level * 5) : String.format("加成效果：閃現記憶時間 +%.2fs。", level * 0.15))); if (level <= currentLevelInBranch) { ui.talentCostLabel.setText("狀態：[ 數據已完美同步寫入 ]"); ui.talentCostLabel.setTextFill(Color.LIME); ui.btnUpgradeTalent.setVisible(false); } else if (level == currentLevelInBranch + 1) { ui.talentCostLabel.setText(String.format("升級消耗：%d ¢", cost)); ui.talentCostLabel.setTextFill(Color.GOLD); ui.btnUpgradeTalent.setText(">>> 執行數據寫入核心 <<<"); ui.btnUpgradeTalent.setVisible(true); ui.btnUpgradeTalent.setOnAction(e -> executeSelectedUpgrade(branchId, cost)); } else { ui.talentCostLabel.setText("狀態：[ 核心未串接 ]"); ui.talentCostLabel.setTextFill(Color.RED); ui.btnUpgradeTalent.setVisible(false); } ui.playDescFadeIn(); }
    private void executeSelectedUpgrade(int branchId, int cost) {
        if (p.buyLegacy(cost)) {
            if (branchId == 1) p.talentStartEMP++; else if (branchId == 2) p.talentWeakFW++; else if (branchId == 3) p.talentFlashTime++;
            try { p.saveData(); } catch(Exception ex) {}
            ui.updateTalentUI(); selectTalentNode(branchId, selectedLevel);
        } else {
            ui.triggerErrorEffect(ui.errorImage2, 2);
            ui.talentCostLabel.setText("錯誤：[ Legacy Coins 不足 ]"); ui.talentCostLabel.setTextFill(Color.RED);
        }
    }
    public void openTalentTree() { engine.currentState = HackEngine.GameState.TALENT_TREE; ui.menuLayer.setVisible(false); ui.updateTalentUI(); ui.talentLayer.setVisible(true); }
    public void closeTalentTree() { engine.currentState = HackEngine.GameState.MAIN_MENU; ui.talentLayer.setVisible(false); ui.menuLayer.setVisible(true); }
    public void startIntroSequence() { engine.currentState = HackEngine.GameState.INTRO; ui.menuLayer.setVisible(false); ui.introLayer.setVisible(true); Label text = (Label) ui.introLayer.getChildren().get(0); String[] lines = {"WAKING UP SYSTEM...", "ACCESS GRANTED."}; Timeline introTimeline = new Timeline(); for (int i=0; i<lines.length; i++) { final int index = i; introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5 * (i+1)), e -> text.setText(lines[index]))); } introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(lines.length * 0.5 + 0.5), e -> { ui.introLayer.setVisible(false); ui.gameLayer.setVisible(true); engine.currentState = HackEngine.GameState.PLAYING; checkBossLevel(); })); introTimeline.play(); }
    public void resetGame() { p.reset(); engine.resetEvents(); ui.gameOverLayer.setVisible(false); ui.gameLayer.setVisible(true); engine.currentState = HackEngine.GameState.PLAYING; checkBossLevel(); }
    public void returnToMenu() { ui.pauseLayer.setVisible(false); ui.gameLayer.setVisible(false); ui.shopLayer.setVisible(false); ui.gameOverLayer.setVisible(false); ui.routeLayer.setVisible(false); ui.menuLayer.setVisible(true); resetGame(); engine.currentState = HackEngine.GameState.MAIN_MENU; }
    public void enterShop() { ui.routeLayer.setVisible(false); ui.updateShopUI(); ui.shopLayer.setVisible(true); engine.currentState = HackEngine.GameState.SHOP; }
    public static void main(String[] args) { launch(); }
}