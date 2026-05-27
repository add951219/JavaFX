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
    private AudioClip gunshotSound, pictureHitSound; // 新增：打靶槍聲與按錯音效

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
        } catch (Exception e) { System.out.println("音訊快取生成失敗"); }

        try {
            errorSound1 = new AudioClip(getClass().getResource("/error1.mp3").toExternalForm());
            errorSound2 = new AudioClip(getClass().getResource("/error2.mp3").toExternalForm());
            loseSound = new AudioClip(getClass().getResource("/lose.mp3").toExternalForm());
        } catch (Exception e) { System.out.println("音效載入失敗"); }

        // === 新增：載入自訂的打靶音效 ===
        try {
            gunshotSound = new AudioClip(getClass().getResource("/gun.mp3").toExternalForm());
            pictureHitSound = new AudioClip(getClass().getResource("/pic_hit.mp3").toExternalForm());
        } catch (Exception e) { System.out.println("自訂打靶音效載入失敗，請確認 resources 資料夾下有 gun.mp3 與 pic_hit.mp3"); }

        try {
            Media bgmMedia = new Media(getClass().getResource("/bgm.mp3").toExternalForm());
            bgmPlayer = new MediaPlayer(bgmMedia); bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); bgmPlayer.setVolume(0.5); bgmPlayer.play();
        } catch (Exception e) { System.out.println("背景音樂載入失敗"); }
    }

    public void setBgmVolume(double vol) { if (bgmPlayer != null) bgmPlayer.setVolume(vol); }

    public double sfxVolume = 0.5;
    public void playErrorSound(int type) {
        if (type == 1 && errorSound1 != null) {
            if (errorSound1.isPlaying()) errorSound1.stop();
            errorSound1.setVolume(sfxVolume); // 加入音量控制
            errorSound1.play();
        }
        else if (type == 2 && errorSound2 != null) {
            if (errorSound2.isPlaying()) errorSound2.stop();
            errorSound2.setVolume(sfxVolume); // 加入音量控制
            errorSound2.play();
        }
    }

    public void playSuccessSound() {
        if (audioPool == null || cachedSuccessBuf == null || cachedAudioFormat == null) return;
        audioPool.submit(() -> {
            try {
                SourceDataLine sdl = AudioSystem.getSourceDataLine(cachedAudioFormat); sdl.open(cachedAudioFormat); sdl.start(); sdl.write(cachedSuccessBuf, 0, cachedSuccessBuf.length); sdl.drain(); sdl.close();
            } catch (Exception e) {}
        });
    }

    // === 新增：播放專屬音效的方法 ===
    public void playGunshotSound() {
        if (gunshotSound != null) {
            if (gunshotSound.isPlaying()) gunshotSound.stop();
            gunshotSound.setVolume(sfxVolume);
            gunshotSound.play();
        }
    }

    public void playPictureHitSound() {
        if (pictureHitSound != null) {
            if (pictureHitSound.isPlaying()) pictureHitSound.stop();
            pictureHitSound.setVolume(sfxVolume);
            pictureHitSound.play();
        }
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnMousePressed(e -> {
            // 新增：蟲子關卡中，不可增加一般 hack 進度
            if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight) {
                engine.isHacking = true;
                ui.playComboHitEffect(engine.comboMultiplier);
            }
        });
        scene.setOnMouseReleased(e -> { if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY) engine.isHacking = false; });
        scene.setOnKeyPressed(e -> {
            if (!ui.root.isFocused()) ui.root.requestFocus();
            if (engine.currentState == HackEngine.GameState.PLAYING) {
                boolean isActionKey = e.getCode().isLetterKey() || e.getCode().isDigitKey() || e.getCode() == KeyCode.SPACE;
                if (isActionKey) engine.runTotalKeystrokes++;

                if (e.getCode() == KeyCode.DIGIT1 || e.getCode() == KeyCode.NUMPAD1) {
                    if (engine.activeGlitch == HackEngine.GlitchType.CORE_OVERLOAD) ui.typeWriterUpdate("⚠ BLOCKED: CORE OVERLOAD ACTIVE ⚠");
                    else if (engine.useEMP(p)) { ui.typeWriterUpdate(">>> EMP DEPLOYED!"); ui.updateShopUI(); ui.updateFirewallUI(); ui.playPulseEffect(); ui.playSweepTransition(Color.CYAN); }
                }
                if (e.getCode() == KeyCode.DIGIT2 || e.getCode() == KeyCode.NUMPAD2) {
                    if (engine.useSlow(p)) { ui.typeWriterUpdate(">>> TIME DILATION ACTIVE! COOLDOWN EXTENDED."); ui.updateShopUI(); }
                }
                if (e.getCode() == KeyCode.SPACE && engine.isFirewallFight) {
                    // === 修改：導入過熱機制，防止玩家依賴高屬性無腦連點 ===
                    if (engine.isOverheated) {
                        playErrorSound(1); // 鎖定狀態下按空白鍵會發出警告音
                    } else {
                        engine.runCorrectKeystrokes++;
                        engine.firewallProgress += 0.05 + (p.upgClick * 0.015);

                        // 再度下修發熱量：基礎值降為 0.04，可以連續狂敲超過 25 下才過熱
                        double heatIncrease = Math.max(0.015, 0.04 - (p.upgCoolant * 0.003));
                        engine.coreHeat += heatIncrease;

                        if (engine.coreHeat >= 1.0) {
                            engine.isOverheated = true;
                            engine.overheatEndTime = System.nanoTime() + 2_000_000_000L; // 鎖定 2 秒
                            playErrorSound(1);
                            ui.shakeScreen();
                        }
                        ui.updateFirewallUI(); ui.playFirewallSpacePopEffect(); ui.playComboHitEffect(engine.comboMultiplier); ui.playFlashEffect(Color.rgb(0, 255, 204, 0.15), 50);
                    }
                }
                if (engine.isInterceptFight) {
                    String input = e.getText().toUpperCase();
                    if (!input.isEmpty() && engine.sequenceIndex < engine.targetSequence.length()) {
                        if (input.equals(engine.targetSequence.substring(engine.sequenceIndex, engine.sequenceIndex + 1))) {
                            engine.sequenceIndex++; engine.runCorrectKeystrokes++; playSuccessSound(); ui.updateInterceptUI(); ui.playComboHitEffect(engine.comboMultiplier);
                            if (engine.sequenceIndex >= engine.targetSequence.length()) { engine.isInterceptFight = false; ui.interceptLayer.setVisible(false); ui.typeWriterUpdate(">>> PACKET SECURED."); if(!engine.isFirewallFight) engine.currentSegment++; }
                        } else { ui.triggerErrorEffect(ui.errorImage1, 1); engine.interceptDeadline -= 1_000_000_000L; }
                    }
                }
                if (engine.isDecryptFight) {
                    String inputChar = ""; KeyCode code = e.getCode();
                    if (code.isLetterKey()) inputChar = code.toString(); else if (code.isDigitKey()) inputChar = code.toString().replace("DIGIT", ""); else if (code.isKeypadKey() && code.toString().startsWith("NUMPAD")) inputChar = code.toString().replace("NUMPAD", "");
                    if (!inputChar.isEmpty()) {
                        engine.decryptInput += inputChar; engine.runCorrectKeystrokes++; playSuccessSound(); ui.updateDecryptUI(); ui.playComboHitEffect(engine.comboMultiplier);
                        if (engine.decryptInput.length() >= engine.decryptTarget.length()) {
                            if (engine.decryptInput.equals(engine.decryptTarget)) { engine.isDecryptFight = false; ui.decryptLayer.setVisible(false); ui.typeWriterUpdate(">>> ENCRYPTION BROKEN."); engine.currentSegment++; } else { ui.triggerErrorEffect(ui.errorImage2, 2); engine.runCorrectKeystrokes -= engine.decryptInput.length(); engine.decryptInput = ""; engine.decryptDeadline -= 1_000_000_000L; ui.updateDecryptUI(); }
                        }
                    } else if (code == KeyCode.BACK_SPACE && engine.decryptInput.length() > 0) { engine.decryptInput = engine.decryptInput.substring(0, engine.decryptInput.length() - 1); ui.updateDecryptUI(); }
                }
            }
            if (e.getCode() == KeyCode.ESCAPE && engine.currentState == HackEngine.GameState.PLAYING) { engine.currentState = HackEngine.GameState.PAUSED; ui.pauseLayer.setVisible(true); }
        });
    }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (engine.currentState != HackEngine.GameState.PLAYING) return;
            long now = System.nanoTime();

            if (engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION) { ui.root.setTranslateX((engine.random.nextDouble() - 0.5) * 6.5); ui.root.setTranslateY((engine.random.nextDouble() - 0.5) * 6.5); if (engine.random.nextInt(15) == 0) ui.statusLabel.setText("⚠ CRITICAL_ERROR: LINE_FRACTURE_DETECTION ⚠"); } else { ui.root.setTranslateX(0); ui.root.setTranslateY(0); }
            if (engine.random.nextInt(10) == 0) { ui.matrixBg.setText(engine.generateRandomCode()); ui.matrixBg.setTextFill(engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION ? Color.rgb(255, 0, 50, 0.35) : Color.rgb(0, 255, 204, 0.15)); }

            if (engine.isHacking && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight) {
                engine.comboFrames++; engine.comboMultiplier = Math.min(3.0, 1.0 + (engine.comboFrames / 180.0)); engine.updateMaxCombo(); if (engine.comboMultiplier >= 2.0 && engine.random.nextInt(4) == 0) ui.playComboHitEffect(engine.comboMultiplier);
            } else { engine.comboFrames = 0; engine.comboMultiplier = 1.0; }
            ui.updateComboDisplay(engine.comboMultiplier);

            // === 修改：防火牆過熱自然冷卻機制 ===
            if (engine.isFirewallFight) {
                if (engine.isOverheated) {
                    if (now > engine.overheatEndTime) {
                        engine.isOverheated = false;
                        engine.coreHeat = 0.0;
                    }
                } else {
                    // 平滑化散熱速度：每一幀稍微散熱，停下不按時能更快冷卻
                    engine.coreHeat = Math.max(0, engine.coreHeat - 0.005);
                }

                double drainRate = (0.003 + (p.currentLevel * p.routeDiffMult * 0.0008)); if (engine.isBossLevel(p.currentLevel)) drainRate *= 0.35; engine.firewallProgress -= drainRate; ui.updateFirewallUI();
                if (engine.firewallProgress <= 0) triggerGameOver(">>> BLOCKED <<<");
                else if (engine.firewallProgress >= 1.0) { engine.isFirewallFight = false; ui.firewallLayer.setVisible(false); ui.typeWriterUpdate(">>> FIREWALL SHATTERED."); if(!engine.isInterceptFight) engine.currentSegment++; ui.playPulseEffect(); ui.playSweepTransition(Color.CYAN); }
            }

            if (engine.isInterceptFight) {
                double timeLeft = (engine.interceptDeadline - now) / 1_000_000_000.0; ui.interceptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft))); if (now > engine.interceptDeadline) handleEventFailure();
            }
            if (engine.isDecryptFight) {
                if (!engine.isDecryptFlashed && now > engine.decryptFlashEndTime) { engine.isDecryptFlashed = true; ui.decryptTargetDisplay.setText("? ? ? ? ?"); }
                double timeLeft = (engine.decryptDeadline - now) / 1_000_000_000.0; ui.decryptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft))); if (now > engine.decryptDeadline) handleEventFailure();
            }

            // === 新增：打蟲子關卡主迴圈 ===
            if (engine.isBugCatchFight) {
                double timeLeft = (engine.bugCatchDeadline - now) / 1_000_000_000.0;
                ui.bugTimeLabel.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft)));

                // 難度提升：每隔一段時間刷新畫面一次蟲子佈局，刷新時間隨等級縮短 (最快 0.55 秒)
                long refreshInterval = Math.max(550_000_000L, 1_100_000_000L - (p.currentLevel * 35_000_000L));
                if (now - engine.lastBugSpawnTime > refreshInterval) {
                    ui.spawnBugsForEvent();
                    engine.lastBugSpawnTime = now;
                }

                if (now > engine.bugCatchDeadline) handleEventFailure();
            }

            // === 修改：一般 Hack 注入時加入反追蹤機制 ===
            if (!engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight) {

                // 等級 5 以後開始有反追蹤機制，防止無腦死壓滑鼠
                if (p.currentLevel > 5 && engine.random.nextInt(350) == 0 && !engine.isBeingTraced) {
                    engine.isBeingTraced = true;
                    engine.traceLevel = 0.0;
                    playErrorSound(2);
                }

                if (engine.isBeingTraced) {
                    if (engine.isHacking) {
                        // 修改：依據隱蔽路由等級減緩追蹤速度 (下限保證最少會增加 0.005)
                        double traceSpeed = Math.max(0.005, 0.015 - (p.upgStealth * 0.002));
                        engine.traceLevel += traceSpeed;

                        if (engine.traceLevel >= 1.0) {
                            engine.progress = Math.max(0, engine.progress - 0.3); // 追蹤滿了，進度大扣
                            engine.isBeingTraced = false;
                            engine.traceLevel = 0.0;
                            playErrorSound(1);
                            ui.shakeScreen();
                            ui.typeWriterUpdate(">>> ⚠ LOCATION COMPROMISED. PROGRESS LOST ⚠");
                        }
                    } else {
                        engine.traceLevel -= 0.02; // 放開滑鼠則追蹤下降
                        if (engine.traceLevel <= 0) {
                            engine.isBeingTraced = false;
                            engine.traceLevel = 0.0;
                        }
                    }
                    ui.updateTraceUI(engine.traceLevel); // 更新追蹤 UI
                } else {
                    ui.updateTraceUI(0); // 隱藏追蹤 UI
                }

                double checkpointSize = 1.0 / engine.totalSegments; double securedProgress = engine.currentSegment * checkpointSize; double targetCheckpoint = (engine.currentSegment + 1) * checkpointSize;
                if (engine.isHacking) { engine.progress += 0.0022 + (p.upgSpeed * 0.0006); if (engine.progress >= targetCheckpoint) { engine.progress = targetCheckpoint; engine.isHacking = false; if (engine.currentSegment < engine.totalSegments - 1) triggerCheckpointEvent(); else playLevelClearExplosion(); } } else { engine.progress -= 0.0010 + (p.currentLevel * 0.0004); if (engine.progress < securedProgress) engine.progress = securedProgress; } ui.updateASCIIProgress();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); timeline.play();
    }

    public void checkBossLevel() { engine.rollGlitch(p.currentLevel); ui.updateGlitchDisplay(); if (engine.isBossLevel(p.currentLevel)) { engine.totalSegments = 8; ui.uiBorder.setTextFill(Color.RED); ui.statusLabel.setTextFill(Color.RED); ui.typeWriterUpdate("⚠ WARNING: CORE OVERRIDE DETECTED. PREPARE FOR ASSAULT ⚠"); } else { engine.totalSegments = 4; ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); ui.statusLabel.setTextFill(Color.CYAN); } }

    public void triggerCheckpointEvent() {
        ui.playSweepTransition(Color.WHITE); ui.playPulseEffect();
        long MathNow = System.nanoTime();
        if (engine.isBossLevel(p.currentLevel)) {
            engine.isFirewallFight = true; engine.firewallProgress = 0.5 + (p.talentWeakFW * 0.05); ui.firewallLayer.setVisible(true); ui.updateFirewallUI();
            engine.isInterceptFight = true; engine.sequenceIndex = 0; String[] directions = {"W", "A", "S", "D"}; StringBuilder sb = new StringBuilder(); for (int i = 0; i < 4; i++) sb.append(directions[engine.random.nextInt(4)]); engine.targetSequence = sb.toString(); ui.updateInterceptUI(); engine.interceptDeadline = MathNow + (long)(14.0 * 1_000_000_000L); ui.interceptLayer.setVisible(true);
        } else {
            // === 改動：將打靶關卡加入隨機事件池 ===
            int rand = engine.random.nextInt(4); // 從 3 種擴展成 4 種
            if (rand == 0) { engine.isFirewallFight = true; engine.firewallProgress = 0.5 + (p.talentWeakFW * 0.05); ui.firewallLayer.setVisible(true); ui.updateFirewallUI(); }
            else if (rand == 1) { engine.startInterceptEvent(p, MathNow); ui.updateInterceptUI(); ui.interceptLayer.setVisible(true); }
            else if (rand == 2) { engine.startDecryptEvent(p, MathNow); ui.decryptTargetDisplay.setText(engine.decryptTarget); ui.updateDecryptUI(); ui.decryptLayer.setVisible(true); }
            else {
                engine.startBugCatchEvent(p, MathNow);
                ui.updateBugScoreUI();
                ui.spawnBugsForEvent();
                ui.bugCatchLayer.setVisible(true);
            }
        }
    }

    public void handleHoneypotTrap() { }

    public void handleEventFailure() {
        // 重置所有小關卡的變數與圖層
        engine.isInterceptFight = false; engine.isDecryptFight = false; engine.isBugCatchFight = false;
        ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false); ui.bugCatchLayer.setVisible(false);
        engine.progress = engine.currentSegment * (1.0 / engine.totalSegments);
        ui.shakeScreen(); ui.playFlashEffect(Color.rgb(255, 0, 0, 0.3), 300); ui.typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED.");
    }

    public void playLevelClearExplosion() { engine.currentState = HackEngine.GameState.PAUSED; engine.isHacking = false; ui.updateTraceUI(0); Timeline explosion = new Timeline(new KeyFrame(Duration.millis(50), e -> { ui.statusLabel.setText(engine.generateRandomCode().substring(0, 40)); ui.uiBorder.setTextFill(Color.color(engine.random.nextDouble(), engine.random.nextDouble(), engine.random.nextDouble())); })); explosion.setCycleCount(15); explosion.setOnFinished(e -> triggerLevelClear()); explosion.play(); }

    public void triggerLevelClear() {
        ui.playPulseEffect(); ui.playSweepTransition(Color.LIME);
        int baseReward = engine.isBossLevel(p.currentLevel) ? 500 : 100; int earned = (int)((p.currentLevel * baseReward) * engine.comboMultiplier * p.routeRewardMult); p.darkCoins += earned; p.currentLevel++; engine.progress = 0.0; engine.currentSegment = 0; if (p.currentLevel > p.highScore) p.highScore = p.currentLevel; ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); ui.gameLayer.setVisible(false); engine.currentState = HackEngine.GameState.ROUTE_SELECT; ui.routeLayer.setVisible(true);
        engine.coreHeat = 0.0; engine.isOverheated = false; engine.isBeingTraced = false; engine.traceLevel = 0.0;
    }

    public void triggerGameOver(String reason) {
        if (loseSound != null) { if (loseSound.isPlaying()) loseSound.stop(); loseSound.play(); }
        engine.currentState = HackEngine.GameState.GAMEOVER;
        ui.shakeScreen(); ui.playSweepTransition(Color.RED); ui.playFlashEffect(Color.rgb(255, 0, 0, 0.4), 800);
        ui.updateTraceUI(0);
        int earnedLegacy = p.darkCoins / 10; p.legacyCoins += earnedLegacy; if (engine.runMaxCombo > p.highestCombo) p.highestCombo = engine.runMaxCombo;
        try { p.saveData(); } catch (Exception ex) { System.out.println("⚠ CRITICAL IO ERROR"); }
        ui.updateTalentUI();
        String title = "SCRIPT KIDDIE (腳本小子)"; int apm = engine.getRunAPM(); double acc = engine.getRunAccuracy();
        if (p.currentLevel >= 20) title = "CYBER DEMIGOD (網神)"; else if (engine.runMaxCombo >= 3.0 && acc >= 95.0) title = "FLAWLESS GHOST (無瑕駭客)"; else if (apm >= 350) title = "KEYBOARD WARRIOR (鍵盤武神)"; else if (engine.runMaxCombo >= 2.5) title = "COMBO MASTER (連擊大師)"; else if (p.currentLevel > 5) title = "NET RUNNER (邊緣行者)";
        ui.showGameOverStats(reason, p.currentLevel, earnedLegacy, engine.runMaxCombo, apm, acc, title);
    }

    public void selectTalentNode(int branchId, int level) { this.selectedBranch = branchId; this.selectedLevel = level; int currentLevelInBranch = (branchId == 1) ? p.talentStartEMP : (branchId == 2 ? p.talentWeakFW : p.talentFlashTime); int cost = (branchId == 1) ? 50 : (branchId == 2 ? 75 : 100); String branchName = (branchId == 1) ? "控制組件優化 [EMP 強化]" : (branchId == 2 ? "防火牆漏洞利用 [FW 弱化]" : "緩衝記憶體擴充 [FLASH 記憶]"); ui.talentNameLabel.setText(String.format(">>> 解密節點：%s (等級 %d) <<<", branchName, level)); ui.talentEffectLabel.setText(branchId == 1 ? String.format("加成效果：自帶 %d 顆 EMP 脈衝彈。", level) : (branchId == 2 ? String.format("加成效果：FW 弱化 +%d%%。", level * 5) : String.format("加成效果：閃現記憶時間 +%.2fs。", level * 0.15))); if (level <= currentLevelInBranch) { ui.talentCostLabel.setText("狀態：[ 數據已完美同步寫入 ]"); ui.talentCostLabel.setTextFill(Color.LIME); ui.btnUpgradeTalent.setVisible(false); } else if (level == currentLevelInBranch + 1) { ui.talentCostLabel.setText(String.format("升級消耗：%d ¢", cost)); ui.talentCostLabel.setTextFill(Color.GOLD); ui.btnUpgradeTalent.setText(">>> 執行數據寫入核心 <<<"); ui.btnUpgradeTalent.setVisible(true); ui.btnUpgradeTalent.setOnAction(e -> executeSelectedUpgrade(branchId, cost)); } else { ui.talentCostLabel.setText("狀態：[ 核心未串接 ]"); ui.talentCostLabel.setTextFill(Color.RED); ui.btnUpgradeTalent.setVisible(false); } ui.playDescFadeIn(); }
    private void executeSelectedUpgrade(int branchId, int cost) { if (p.buyLegacy(cost)) { if (branchId == 1) p.talentStartEMP++; else if (branchId == 2) p.talentWeakFW++; else if (branchId == 3) p.talentFlashTime++; try { p.saveData(); } catch(Exception ex) {} ui.updateTalentUI(); selectTalentNode(branchId, selectedLevel); } else { ui.triggerErrorEffect(ui.errorImage2, 2); ui.talentCostLabel.setText("錯誤：[ Legacy Coins 不足 ]"); ui.talentCostLabel.setTextFill(Color.RED); } }

    public void openTalentTree() { ui.playSweepTransition(Color.web("#FF007F")); engine.currentState = HackEngine.GameState.TALENT_TREE; ui.menuLayer.setVisible(false); ui.updateTalentUI(); ui.talentLayer.setVisible(true); }
    public void closeTalentTree() { ui.playSweepTransition(Color.CYAN); engine.currentState = HackEngine.GameState.MAIN_MENU; ui.talentLayer.setVisible(false); ui.menuLayer.setVisible(true); }

    public void startIntroSequence() { engine.currentState = HackEngine.GameState.INTRO; ui.menuLayer.setVisible(false); ui.introLayer.setVisible(true); Label text = (Label) ui.introLayer.getChildren().get(0); String[] lines = {"WAKING UP SYSTEM...", "ACCESS GRANTED."}; Timeline introTimeline = new Timeline(); for (int i=0; i<lines.length; i++) { final int index = i; introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5 * (i+1)), e -> text.setText(lines[index]))); } introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(lines.length * 0.5 + 0.5), e -> { ui.introLayer.setVisible(false); ui.gameLayer.setVisible(true); engine.currentState = HackEngine.GameState.PLAYING; engine.startNewRun(); checkBossLevel(); })); introTimeline.play(); }
    public void resetGame() { p.reset(); engine.resetEvents(); ui.gameOverLayer.setVisible(false); ui.gameLayer.setVisible(true); ui.updateTraceUI(0); engine.currentState = HackEngine.GameState.PLAYING; engine.startNewRun(); checkBossLevel(); }

    public void returnToMenu() { ui.playSweepTransition(Color.WHITE); ui.pauseLayer.setVisible(false); ui.gameLayer.setVisible(false); ui.shopLayer.setVisible(false); ui.gameOverLayer.setVisible(false); ui.routeLayer.setVisible(false); ui.menuLayer.setVisible(true); resetGame(); engine.currentState = HackEngine.GameState.MAIN_MENU; }
    public void enterShop() { ui.playSweepTransition(Color.LIME); ui.routeLayer.setVisible(false); ui.updateShopUI(); ui.shopLayer.setVisible(true); engine.currentState = HackEngine.GameState.SHOP; }

    public static void main(String[] args) { launch(); }
}