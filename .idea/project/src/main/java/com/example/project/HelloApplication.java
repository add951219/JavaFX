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
import javafx.scene.effect.DropShadow;
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
    public BossManager bossManager;

    private int selectedBranch = 0;
    private int selectedLevel = 0;

    private AudioClip errorSound1, errorSound2, loseSound;
    private AudioClip gunshotSound, pictureHitSound;
    public AudioClip bossIntroSound, bossPhaseSound;

    private MediaPlayer bgmPlayer;
    private AudioClip hackTickSound;
    private long lastHackSoundTime = 0;
    private int hackSoundInterval = 100;
    private AudioFormat cachedAudioFormat;
    private ExecutorService audioPool;
    private byte[] cachedSuccessBuf;
    private AudioClip hoverSound;
    private AudioClip clickSound;
    public AudioClip playerHitSound;
    public AudioClip bossFailSound;
    public AudioClip noMoneySound;

    @Override
    public void start(Stage stage) {
        initAudio(); ui = new UIManager(p, engine, this); bossManager = new BossManager(this, engine, ui, p);
        Scene scene = new Scene(ui.root, 800, 600); setupInputHandlers(scene); startGameLoop();
        stage.setScene(scene); stage.setTitle("Neon Breach - Override Edition"); stage.show(); ui.updateTalentUI(); ui.root.requestFocus();
    }

    @Override
    public void stop() {
        if (bgmPlayer != null) bgmPlayer.stop();
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
        } catch (Exception e) { }

        try { errorSound1 = new AudioClip(getClass().getResource("/error1.mp3").toExternalForm()); errorSound2 = new AudioClip(getClass().getResource("/error2.mp3").toExternalForm()); loseSound = new AudioClip(getClass().getResource("/lose.mp3").toExternalForm()); gunshotSound = new AudioClip(getClass().getResource("/gun.mp3").toExternalForm()); pictureHitSound = new AudioClip(getClass().getResource("/pic_hit.mp3").toExternalForm()); } catch (Exception e) {}
        try { bossIntroSound = new AudioClip(getClass().getResource("/boss_intro.mp3").toExternalForm()); } catch (Exception e) {}
        try { bossPhaseSound = new AudioClip(getClass().getResource("/boss_phase.mp3").toExternalForm()); } catch (Exception e) {}
        try { hackTickSound = new AudioClip(getClass().getResource("/hack_tick.mp3").toExternalForm()); } catch (Exception e) {}
        try { hoverSound = new AudioClip(getClass().getResource("/hover.mp3").toExternalForm()); } catch (Exception e) {}
        try { clickSound = new AudioClip(getClass().getResource("/click.mp3").toExternalForm()); } catch (Exception e) {}

        // --- 新增的受傷與 Boss 失敗音效 ---
        try { playerHitSound = new AudioClip(getClass().getResource("/hit.mp3").toExternalForm()); } catch (Exception e) {}
        try { bossFailSound = new AudioClip(getClass().getResource("/boss_fail.mp3").toExternalForm()); } catch (Exception e) {}

        try { Media bgmMedia = new Media(getClass().getResource("/bgm.mp3").toExternalForm()); bgmPlayer = new MediaPlayer(bgmMedia); bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); bgmPlayer.setVolume(0.25); bgmPlayer.play(); } catch (Exception e) {}
        try { noMoneySound = new AudioClip(getClass().getResource("/no_money.mp3").toExternalForm()); } catch (Exception e) {}
    }
    public void playHoverSound() {
        if (hoverSound != null) {
            hoverSound.setVolume(getActualSfxVolume() * 0.5);
            hoverSound.play();
        }
    }
    public void playClickSound() {
        if (clickSound != null) {
            clickSound.setVolume(getActualSfxVolume());
            clickSound.play();
        }
    }
    //專門用來播放按住左鍵時的定時微小音效，受 sfxVolume 影響
    public void playHackTickSound() {
        if (hackTickSound != null) {
            // 如果覺得按鍵音太大會吵到 BGM，可以在這裡乘上一個小於 1 的倍率，例如 * 0.7
            hackTickSound.setVolume(getActualSfxVolume() * 0.7);
            hackTickSound.play();
        }
    }

    public void setBgmVolume(double vol) {
        if (bgmPlayer != null) bgmPlayer.setVolume(vol * vol);
    }
    public double sfxVolume = 0.5;
    public void playErrorSound(int type) {
        if (type == 1 && errorSound1 != null) { errorSound1.setVolume(getActualSfxVolume()); errorSound1.play(); }
        else if (type == 2 && errorSound2 != null) { errorSound2.setVolume(getActualSfxVolume()); errorSound2.play(); }
    }
    public void setSfxVolume(double vol) {
        this.sfxVolume = vol;
        double actualVol = getActualSfxVolume();
        if (bossIntroSound != null) bossIntroSound.setVolume(actualVol);
        if (bossPhaseSound != null) bossPhaseSound.setVolume(actualVol);
        if (loseSound != null) loseSound.setVolume(actualVol);
        if (playerHitSound != null) playerHitSound.setVolume(actualVol);
        if (bossFailSound != null) bossFailSound.setVolume(actualVol);
        if (noMoneySound != null) noMoneySound.setVolume(actualVol);
    }

    public void playSuccessSound() {
        // 確保 audioPool 已經成功初始化才執行
        if (audioPool == null || audioPool.isShutdown()) return;

        audioPool.submit(() -> {
            try {
                SourceDataLine sdl = AudioSystem.getSourceDataLine(cachedAudioFormat);
                sdl.open(cachedAudioFormat);

                // 動態調整音量
                if (sdl.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
                    javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl) sdl.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(getActualSfxVolume() > 0.0001 ? getActualSfxVolume() : 0.0001) * 20.0);
                    gainControl.setValue(dB);
                }

                sdl.start();
                sdl.write(cachedSuccessBuf, 0, cachedSuccessBuf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception e) {}
        });
    }
    public void playGunshotSound() {
        if (gunshotSound != null) { gunshotSound.setVolume(getActualSfxVolume()); gunshotSound.play(); }
    }
    public void playPictureHitSound() {
        if (pictureHitSound != null) { pictureHitSound.setVolume(getActualSfxVolume()); pictureHitSound.play(); }
    }
    public void playPlayerHitSound() {
        if (playerHitSound != null) { playerHitSound.setVolume(getActualSfxVolume()); playerHitSound.play(); }
    }

    public void playBossFailSound() {
        if (bossFailSound != null) { bossFailSound.setVolume(getActualSfxVolume()); bossFailSound.play(); }
    }
    public void playNoMoneySound() {
        if (noMoneySound != null) {
            noMoneySound.setVolume(getActualSfxVolume());
            noMoneySound.play();
        }
    }
    private void setupInputHandlers(Scene scene) {
        scene.setOnMousePressed(e -> {
            if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight) {
                engine.isHacking = true;
                ui.playComboHitEffect(engine.comboMultiplier);

                // 按下的瞬間立刻先播放第一次音效，提升操作即時回饋感
                playHackTickSound();
                lastHackSoundTime = System.currentTimeMillis();
            }
        });
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // 放開左鍵時，不論目前遊戲狀態是什麼，都強制中斷音效避免殘音
                if (hackTickSound != null && hackTickSound.isPlaying()) {
                    hackTickSound.stop();
                }

                if (engine.currentState == HackEngine.GameState.PLAYING) {
                    engine.isHacking = false;
                }
            }
        });
        scene.setOnKeyPressed(e -> {
            if (!ui.root.isFocused()) ui.root.requestFocus();

            // === 錄影與導播專用神之手作弊鍵 ===
            if (e.getCode() == KeyCode.F1) { if (engine.currentState == HackEngine.GameState.PLAYING) { engine.resetEvents(); ui.firewallLayer.setVisible(false); ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false); ui.bugCatchLayer.setVisible(false); ui.surgeLayer.setVisible(false); ui.typeWriterUpdate("[DEBUG_MODE] INSTANT WIN ACTIVATED."); playLevelClearExplosion(); } }
            if (e.getCode() == KeyCode.F2) { p.darkCoins += 50000; p.legacyCoins += 5000; ui.updateShopUI(); ui.updateTalentUI(); if (engine.currentState == HackEngine.GameState.PLAYING) ui.typeWriterUpdate("[DEBUG_MODE] +50000 DarkCoins / +5000 Legacy Coins."); }
            if (e.getCode() == KeyCode.F3) { p.currentLevel = ((p.currentLevel / 5) + 1) * 5 - 1; ui.updateASCIIProgress(); if (engine.currentState == HackEngine.GameState.PLAYING) ui.typeWriterUpdate("[DEBUG_MODE] WARPED TO BOSS ENTRANCE."); }
            if (e.getCode() == KeyCode.F4) { engine.coreHeat = 0; engine.isOverheated = false; engine.overheatEndTime = 0; engine.traceLevel = 0; engine.isBeingTraced = false; p.empCharges = 99; p.slowCharges = 99; ui.updateFirewallUI(); ui.updateTraceUI(0); ui.updateShopUI(); }

            // 新增：F5 一鍵神裝 (滿級天賦與商店，方便錄製全亮特效)
            if (e.getCode() == KeyCode.F5) {
                p.upgClick = 5; p.upgSpeed = 5; p.upgCoolant = 5; p.upgStealth = 5; p.empCharges = 99; p.slowCharges = 99;
                p.talentStartEMP = 3; p.talentWeakFW = 5; p.talentFlashTime = 3; p.talentSignalShield = 3;
                ui.updateShopUI(); ui.updateTalentUI();
                if (engine.currentState == HackEngine.GameState.PLAYING) ui.typeWriterUpdate("[DEMO_MODE] ALL UPGRADES MAXED OUT.");
            }

            // 新增：F6 手動切換視覺干擾 (方便錄製畫面錯位的 Cyberpunk 特效)
            if (e.getCode() == KeyCode.F6) {
                if (engine.currentState == HackEngine.GameState.PLAYING) {
                    engine.activeGlitch = (engine.activeGlitch == HackEngine.GlitchType.NONE) ? HackEngine.GlitchType.VISUAL_DISTORTION : HackEngine.GlitchType.NONE;
                    ui.updateGlitchDisplay();
                    ui.typeWriterUpdate("[DEMO_MODE] VISUAL_DISTORTION TOGGLED.");
                }
            }

            if (e.getCode() == KeyCode.TAB && engine.isBossFight && engine.currentBossType == HackEngine.BossType.HYDRA && engine.bossPhase == 1) { engine.activeHydraHead = (engine.activeHydraHead + 1) % 3; ui.updateFirewallUI(); e.consume(); return; }

            if (engine.currentState == HackEngine.GameState.PLAYING) {
                // === SURGE A/D 走位判定 ===
                if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.SURGE && engine.isSurgeFight) {
                    if (e.getCode() == KeyCode.A) {
                        engine.surgePlayerPos = Math.max(0, engine.surgePlayerPos - 1);
                        ui.updateSurgeUI(engine.getSurgeElapsed()); e.consume(); return;
                    } else if (e.getCode() == KeyCode.D) {
                        engine.surgePlayerPos = Math.min(4, engine.surgePlayerPos + 1);
                        ui.updateSurgeUI(engine.getSurgeElapsed()); e.consume(); return;
                    }
                }

                boolean isActionKey = e.getCode().isLetterKey() || e.getCode().isDigitKey() || e.getCode() == KeyCode.SPACE;
                if (isActionKey) engine.runTotalKeystrokes++;

                if (e.getCode() == KeyCode.DIGIT1 || e.getCode() == KeyCode.NUMPAD1) { if (engine.activeGlitch == HackEngine.GlitchType.CORE_OVERLOAD) ui.typeWriterUpdate("⚠ BLOCKED: CORE OVERLOAD ACTIVE ⚠"); else if (engine.useEMP(p)) { ui.typeWriterUpdate(">>> EMP DEPLOYED!"); ui.updateShopUI(); ui.updateFirewallUI(); ui.playPulseEffect(); ui.playSweepTransition(Color.CYAN); } }
                if (e.getCode() == KeyCode.DIGIT2 || e.getCode() == KeyCode.NUMPAD2) { if (engine.useSlow(p)) { ui.typeWriterUpdate(">>> TIME DILATION ACTIVE!"); ui.updateShopUI(); } }
                if (e.getCode() == KeyCode.DIGIT3 || e.getCode() == KeyCode.NUMPAD3) {
                    if (p.autoSolveCharges > 0 && (engine.isDecryptFight || engine.isInterceptFight)) {
                        p.autoSolveCharges--; playSuccessSound(); ui.updateShopUI();
                        if (engine.isDecryptFight) { engine.decryptInput = engine.decryptTarget; ui.updateDecryptUI(); }
                        else if (engine.isInterceptFight) { engine.sequenceIndex = engine.targetSequence.length(); ui.updateInterceptUI(); }
                    }
                }
                if (e.getCode() == KeyCode.DIGIT4 || e.getCode() == KeyCode.NUMPAD4) {
                    if (p.overloadCharges > 0 && engine.isFirewallFight) {
                        p.overloadCharges--; ui.updateShopUI();
                        ui.typeWriterUpdate(">>> ⚠ CORE OVERLOAD VIRUS INJECTED ⚠"); ui.playFlashEffect(Color.RED, 500); playErrorSound(2); ui.shakeScreen();
                        if(engine.isBossFight && engine.currentBossType == HackEngine.BossType.HYDRA) { for(int i=0; i<3; i++) engine.hydraWalls[i] = Math.min(1.0, engine.hydraWalls[i] + 0.8); }
                        else { engine.firewallProgress = Math.min(1.0, engine.firewallProgress + 0.8); }
                        engine.coreHeat = 1.0; engine.isOverheated = true; engine.overheatEndTime = System.nanoTime() + 2_500_000_000L; // 強制過熱 2.5 秒
                        ui.updateFirewallUI();
                    }
                }
                if (e.getCode() == KeyCode.SPACE && engine.isFirewallFight) {
                    if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.PULSE && engine.isPulseFight) {
                        int result = engine.judgePulseHit();
                        if (result == 1) { playSuccessSound(); ui.playPulseHitEffect(); }
                        else if (result == -1) { playErrorSound(1); ui.shakeScreen(); }
                        ui.updatePulseScanUI();
                    }
                    else if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.MIMIC && !engine.isMimicWindow) {
                        engine.firewallProgress -= 0.08; playErrorSound(1); ui.shakeScreen(); ui.updateFirewallUI();
                    }
                    else if (engine.isOverheated) { playErrorSound(1); }
                    else {
                        engine.runCorrectKeystrokes++;
                        if(engine.isBossFight && engine.currentBossType == HackEngine.BossType.HYDRA) engine.hydraWalls[engine.activeHydraHead] += 0.05 + (p.upgClick * 0.015);
                        else engine.firewallProgress += 0.05 + (p.upgClick * 0.015);
                        engine.coreHeat += Math.max(0.01, 0.025 - (p.upgCoolant * 0.002));
                        if (engine.coreHeat >= 1.0) { engine.isOverheated = true; engine.overheatEndTime = System.nanoTime() + 800_000_000L; playErrorSound(1); ui.shakeScreen(); ui.playFlashEffect(Color.RED, 250); }
                        ui.updateFirewallUI(); ui.playFirewallSpacePopEffect(); ui.playComboHitEffect(engine.comboMultiplier);
                    }
                }

                if (engine.isInterceptFight) {
                    String input = e.getText().toUpperCase();
                    if (!input.isEmpty() && engine.sequenceIndex < engine.targetSequence.length()) {
                        String targetChar = engine.targetSequence.substring(engine.sequenceIndex, engine.sequenceIndex + 1);
                        if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.PULSE) {
                            if (input.equals(targetChar)) {
                                long now = System.nanoTime(); long targetTime = engine.pulseLetterDeadlines[engine.sequenceIndex];
                                if (Math.abs(now - targetTime) <= engine.pulseLetterWindow) {
                                    engine.sequenceIndex++; engine.runCorrectKeystrokes++; playSuccessSound(); ui.playComboHitEffect(engine.comboMultiplier);
                                    if (engine.sequenceIndex >= engine.targetSequence.length()) {
                                        engine.isInterceptFight = false; ui.interceptLayer.setVisible(false); ui.typeWriterUpdate(">>> RHYTHM SYNC COMPLETE.");
                                        if (bossPhaseSound != null) bossPhaseSound.play(); ui.playFlashEffect(Color.RED, 500); engine.bossPhase++; bossManager.startBossPhase();
                                    }
                                } else { ui.triggerErrorEffect(ui.errorImage1, 1); engine.interceptDeadline -= 1_000_000_000L; }
                            } else { ui.triggerErrorEffect(ui.errorImage1, 1); engine.interceptDeadline -= 1_000_000_000L; }
                        }
                        else {
                            if (input.equals(targetChar)) {
                                engine.sequenceIndex++; engine.runCorrectKeystrokes++; playSuccessSound(); ui.updateInterceptUI(); ui.playComboHitEffect(engine.comboMultiplier);
                                if (engine.sequenceIndex >= engine.targetSequence.length()) {
                                    engine.isInterceptFight = false; ui.interceptLayer.setVisible(false);
                                    if (engine.isEscapeSequence) { engine.isEscapeSequence = false; ui.interceptTimeDisplay.setTextFill(Color.WHITE); ui.typeWriterUpdate(">>> OVERRIDE SUCCESSFUL. CONNECTION SEVERED SAFELY."); p.currentLevel++; triggerLevelClear(); }
                                    else if (!engine.isBossFight) { ui.typeWriterUpdate(">>> PACKET SECURED."); engine.currentSegment++; }
                                }
                            } else { ui.triggerErrorEffect(ui.errorImage1, 1); engine.interceptDeadline -= 1_000_000_000L; }
                        }
                    }
                }

                if (engine.isDecryptFight) {
                    if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.PULSE && !engine.pulseAllRevealed) { e.consume(); return; }
                    String inputChar = ""; KeyCode code = e.getCode();
                    if (code.isLetterKey()) inputChar = code.toString(); else if (code.isDigitKey()) inputChar = code.toString().replace("DIGIT", "");
                    if (!inputChar.isEmpty()) {
                        engine.decryptInput += inputChar; engine.runCorrectKeystrokes++; playSuccessSound(); ui.updateDecryptUI(); ui.playComboHitEffect(engine.comboMultiplier);
                        if (engine.decryptInput.length() >= engine.decryptTarget.length()) {
                            if (engine.decryptInput.equals(engine.decryptTarget)) {
                                if (!engine.isBossFight) { engine.isDecryptFight = false; ui.decryptLayer.setVisible(false); ui.typeWriterUpdate(">>> ENCRYPTION BROKEN."); engine.currentSegment++; }
                            } else {
                                ui.triggerErrorEffect(ui.errorImage2, 2); engine.runCorrectKeystrokes = Math.max(0, engine.runCorrectKeystrokes - engine.decryptInput.length());
                                engine.decryptInput = ""; engine.decryptDeadline -= 1_000_000_000L; ui.updateDecryptUI();
                            }
                        }
                    } else if (code == KeyCode.BACK_SPACE && engine.decryptInput.length() > 0) { engine.decryptInput = engine.decryptInput.substring(0, engine.decryptInput.length() - 1); ui.updateDecryptUI(); }
                }
            }
            if (e.getCode() == KeyCode.ESCAPE && engine.currentState == HackEngine.GameState.PLAYING) { engine.currentState = HackEngine.GameState.PAUSED; ui.pauseLayer.setVisible(true); }
        });
    }
    private double getActualSfxVolume() {
        return sfxVolume * sfxVolume;
    }
    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            ui.drawMatrixRain();
            if (engine.currentState != HackEngine.GameState.PLAYING) return;
            long now = System.nanoTime();

            if (engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION) { ui.root.setTranslateX((engine.random.nextDouble() - 0.5) * 6.5); ui.root.setTranslateY((engine.random.nextDouble() - 0.5) * 6.5); if (engine.random.nextInt(15) == 0) { if (!"⚠ CRITICAL_ERROR: LINE_FRACTURE_DETECTION ⚠".equals(ui.statusLabel.getText())) ui.statusLabel.setText("⚠ CRITICAL_ERROR: LINE_FRACTURE_DETECTION ⚠"); } } else { ui.root.setTranslateX(0); ui.root.setTranslateY(0); }

            if (engine.isBossFight && !engine.isEscapeSequence) {
                bossManager.updateBossLoop(now);
                ui.updateASCIIProgress();
                return;
            }

            if (engine.isFirewallFight) {
                if (engine.isOverheated) { if (now > engine.overheatEndTime) { engine.isOverheated = false; engine.coreHeat = 0.0; } }
                else { engine.coreHeat = Math.max(0, engine.coreHeat - 0.005); }
                double drainRate = (0.003 + (p.currentLevel * p.routeDiffMult * 0.0008)); if (engine.isOverheated) drainRate *= 0.15;
                engine.firewallProgress -= drainRate; ui.updateFirewallUI();
                if (engine.firewallProgress <= 0) triggerGameOver(">>> BLOCKED <<<");
                else if (engine.firewallProgress >= 1.0) { engine.isFirewallFight = false; ui.firewallLayer.setVisible(false); ui.typeWriterUpdate(">>> FIREWALL SHATTERED."); engine.currentSegment++; ui.playPulseEffect(); ui.playSweepTransition(Color.CYAN); }
            }

            if (engine.isInterceptFight) {
                double timeLeft = (engine.interceptDeadline - now) / 1_000_000_000.0;
                String timeStr = String.format("Time left: %.1fs", Math.max(0, timeLeft));
                if (!timeStr.equals(ui.interceptTimeDisplay.getText())) ui.interceptTimeDisplay.setText(timeStr);
                if (now > engine.interceptDeadline) { if (engine.isEscapeSequence) { engine.isEscapeSequence = false; engine.isInterceptFight = false; ui.interceptLayer.setVisible(false); ui.interceptTimeDisplay.setTextFill(Color.WHITE); triggerGameOver("FATAL ERROR: NEURAL OVERRIDE FAILED"); } else { handleEventFailure(); } }
            }

            if (engine.isDecryptFight) {
                if (!engine.isDecryptFlashed && now > engine.decryptFlashEndTime) { engine.isDecryptFlashed = true; ui.decryptTargetDisplay.setText("? ? ? ? ?"); }
                double timeLeft = (engine.decryptDeadline - now) / 1_000_000_000.0;
                String timeStr = String.format("Time left: %.1fs", Math.max(0, timeLeft));
                if (!timeStr.equals(ui.decryptTimeDisplay.getText())) ui.decryptTimeDisplay.setText(timeStr);
                if (now > engine.decryptDeadline) handleEventFailure();
            }

            if (engine.isBugCatchFight) {
                double timeLeft = (engine.bugCatchDeadline - now) / 1_000_000_000.0;
                String timeStr = String.format("Time left: %.1fs", Math.max(0, timeLeft));
                if (!timeStr.equals(ui.bugTimeLabel.getText())) ui.bugTimeLabel.setText(timeStr);
                long refreshInterval = Math.max(550_000_000L, 1_100_000_000L - (p.currentLevel * 35_000_000L));
                if (now - engine.lastBugSpawnTime > refreshInterval) { ui.spawnBugsForEvent(); engine.lastBugSpawnTime = now; }
                if (now > engine.bugCatchDeadline) handleEventFailure();
            }

            if (engine.isHacking && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight && !engine.isSurgeFight) { engine.comboFrames++; engine.comboMultiplier = Math.min(3.0, 1.0 + (engine.comboFrames / 180.0)); engine.updateMaxCombo(); if (engine.comboMultiplier >= 2.0 && engine.random.nextInt(4) == 0) ui.playComboHitEffect(engine.comboMultiplier); } else { engine.comboFrames = 0; engine.comboMultiplier = 1.0; }
            ui.updateComboDisplay(engine.comboMultiplier);

            if (!engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight && !engine.isSurgeFight) {
                if (p.currentLevel > 5 && engine.random.nextInt(350) == 0 && !engine.isBeingTraced && engine.isHacking) {
                    engine.isBeingTraced = true; engine.traceLevel = 0.1; playErrorSound(2);
                }
                if (engine.isBeingTraced) {
                    if (engine.isHacking) {
                        // 套用木馬挖礦副作用：提升被追蹤速度
                        double traceSpeed = Math.max(0.002, 0.015 - (p.upgStealth * 0.002) - (p.talentSignalShield * 0.002) + (p.upgMiner * 0.003));
                        engine.traceLevel += traceSpeed;

                        if (engine.traceLevel >= 1.0) {
                            if (p.hasTraceShield) {
                                // 護盾抵擋
                                p.hasTraceShield = false; engine.traceLevel = 0.0;
                                playSuccessSound(); ui.playFlashEffect(Color.CYAN, 400);
                                ui.typeWriterUpdate(">>> TRACE BLOCKED BY BACKUP SHIELD <<<");
                                ui.updateShopUI(); // 更新技能列的狀態
                            } else {
                                // 正常受傷
                                engine.progress = Math.max(0, engine.progress - 0.3); engine.isBeingTraced = false; engine.traceLevel = 0.0; playErrorSound(1); ui.shakeScreen(); ui.typeWriterUpdate(">>> ⚠ LOCATION COMPROMISED. PROGRESS LOST ⚠");
                            }
                        }
                    } else { engine.traceLevel -= 0.02; if (engine.traceLevel <= 0) { engine.isBeingTraced = false; engine.traceLevel = 0.0; } }
                    ui.updateTraceUI(engine.traceLevel);
                } else { ui.updateTraceUI(0); }

                double checkpointSize = 1.0 / engine.totalSegments; double securedProgress = engine.currentSegment * checkpointSize; double targetCheckpoint = (engine.currentSegment + 1) * checkpointSize;
                if (engine.isHacking) {
                    engine.progress += 0.0022 + (p.upgSpeed * 0.0006);

                    long nowMs = System.currentTimeMillis();
                    if (nowMs - lastHackSoundTime >= hackSoundInterval) {
                        playHackTickSound();
                        lastHackSoundTime = nowMs;
                        hackSoundInterval = 70 + engine.random.nextInt(70);
                    }

                    if (engine.progress >= targetCheckpoint) {
                        engine.progress = targetCheckpoint;
                        engine.isHacking = false;

                        if (hackTickSound != null && hackTickSound.isPlaying()) {
                            hackTickSound.stop();
                        }

                        ui.updateASCIIProgress();
                        if (engine.currentSegment < engine.totalSegments - 1) triggerCheckpointEvent();
                        else playLevelClearExplosion();
                    }
                }
                else { engine.progress -= 0.0010 + (p.currentLevel * 0.0004); if (engine.progress < securedProgress) engine.progress = securedProgress; }
                ui.updateASCIIProgress();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); timeline.play();
    }

    public void triggerCheckpointEvent() {
        ui.playSweepTransition(Color.WHITE); ui.playPulseEffect();
        long MathNow = System.nanoTime(); int rand = engine.random.nextInt(4);
        if (rand == 0) { engine.isFirewallFight = true; engine.firewallProgress = 0.5 + (p.talentWeakFW * 0.05); ui.firewallLayer.setVisible(true); ui.updateFirewallUI(); }
        else if (rand == 1) { engine.startInterceptEvent(p, MathNow); ui.updateInterceptUI(); ui.interceptLayer.setVisible(true); }
        else if (rand == 2) { engine.startDecryptEvent(p, MathNow); ui.decryptTargetDisplay.setText(engine.decryptTarget); ui.updateDecryptUI(); ui.decryptLayer.setVisible(true); }
        else { engine.startBugCatchEvent(p, MathNow); ui.updateBugScoreUI(); ui.spawnBugsForEvent(); ui.bugCatchLayer.setVisible(true); }
    }

    public void handleEventFailure() { engine.isInterceptFight = false; engine.isDecryptFight = false; engine.isBugCatchFight = false; ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false); ui.bugCatchLayer.setVisible(false); engine.progress = engine.currentSegment * (1.0 / engine.totalSegments); ui.shakeScreen(); ui.playFlashEffect(Color.rgb(255, 0, 0, 0.3), 300); ui.typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED."); }
    public void playLevelClearExplosion() { engine.currentState = HackEngine.GameState.PAUSED; engine.isHacking = false; ui.updateTraceUI(0); Timeline explosion = new Timeline(new KeyFrame(Duration.millis(50), e -> { Color randomColor = Color.color(engine.random.nextDouble(), engine.random.nextDouble(), engine.random.nextDouble()); ui.uiBorder.setTextFill(randomColor); ui.uiBorder.setEffect(new DropShadow(25, randomColor)); ui.root.setTranslateX((engine.random.nextDouble() - 0.5) * 12); ui.root.setTranslateY((engine.random.nextDouble() - 0.5) * 12); })); explosion.setCycleCount(15); explosion.setOnFinished(e -> { ui.root.setTranslateX(0); ui.root.setTranslateY(0); ui.uiBorder.setEffect(null); triggerLevelClear(); }); explosion.play(); }
    public void triggerLevelClear() {
        ui.playPulseEffect(); ui.playSweepTransition(Color.LIME);
        int baseReward = engine.isBossLevel(p.currentLevel) ? 1000 : 100;
        // 套用木馬挖礦程式加成 (每級 +15%)
        int earned = (int)((p.currentLevel * baseReward) * engine.comboMultiplier * p.routeRewardMult * (1.0 + p.upgMiner * 0.15));
        if (!engine.isEscapeSequence) p.darkCoins += earned;
        if (engine.isBossFight || engine.isBossLevel(p.currentLevel)) p.legacyCoins += 15;
        p.currentLevel++; engine.progress = 0.0; engine.currentSegment = 0;
        if (p.currentLevel > p.highScore) p.highScore = p.currentLevel;
        ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); ui.gameLayer.setVisible(false);
        engine.currentState = HackEngine.GameState.ROUTE_SELECT; ui.routeLayer.setVisible(true);
        engine.coreHeat = 0.0; engine.isOverheated = false; engine.isBeingTraced = false; engine.traceLevel = 0.0;
    }
    public void startIntroSequence() { engine.currentState = HackEngine.GameState.INTRO; ui.menuLayer.setVisible(false); ui.introLayer.setVisible(true); Label text = (Label) ui.introLayer.getChildren().get(0); String[] lines = {"WAKING UP SYSTEM...", "ACCESS GRANTED."}; Timeline introTimeline = new Timeline(); for (int i=0; i<lines.length; i++) { final int index = i; introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5 * (i+1)), e -> text.setText(lines[index]))); } introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(lines.length * 0.5 + 0.5), e -> { ui.introLayer.setVisible(false); ui.gameLayer.setVisible(true); engine.currentState = HackEngine.GameState.PLAYING; engine.startNewRun(); bossManager.checkBossLevel(); })); introTimeline.play(); }
    public void triggerGameOver(String reason) { engine.isEscapeSequence = false; ui.interceptTimeDisplay.setTextFill(Color.WHITE); if (loseSound != null) { if (loseSound.isPlaying()) loseSound.stop(); loseSound.play(); } engine.currentState = HackEngine.GameState.GAMEOVER; ui.shakeScreen(); ui.playSweepTransition(Color.RED); ui.playFlashEffect(Color.rgb(255, 0, 0, 0.4), 800); ui.updateTraceUI(0); int earnedLegacy = p.darkCoins / 10; p.legacyCoins += earnedLegacy; if (engine.runMaxCombo > p.highestCombo) p.highestCombo = engine.runMaxCombo; try { p.saveData(); } catch (Exception ex) {} ui.updateTalentUI(); String title = "SCRIPT KIDDIE"; int apm = engine.getRunAPM(); double acc = engine.getRunAccuracy(); if (p.currentLevel >= 20) title = "CYBER DEMIGOD"; else if (engine.runMaxCombo >= 3.0 && acc >= 95.0) title = "FLAWLESS GHOST"; else if (apm >= 350) title = "KEYBOARD WARRIOR"; else if (engine.runMaxCombo >= 2.5) title = "COMBO MASTER"; else if (p.currentLevel > 5) title = "NET RUNNER"; ui.showGameOverStats(reason, p.currentLevel, earnedLegacy, engine.runMaxCombo, apm, acc, title); }
    public void selectTalentNode(int branchId, int level) { this.selectedBranch = branchId; this.selectedLevel = level; int currentLevelInBranch = (branchId == 1) ? p.talentStartEMP : (branchId == 2 ? p.talentWeakFW : (branchId == 3 ? p.talentFlashTime : p.talentSignalShield)); int cost = (branchId == 1) ? 50 : (branchId == 2 ? 75 : (branchId == 3 ? 100 : 120)); String branchName = (branchId == 1) ? "控制組件優化 [EMP 強化]" : (branchId == 2 ? "防火牆漏洞利用 [FW 弱化]" : (branchId == 3 ? "緩衝記憶體擴充 [FLASH 記憶]" : "訊號屏蔽防護 [防追蹤與干擾]")); ui.talentNameLabel.setText(String.format(">>> 解密節點：%s (等級 %d) <<<", branchName, level)); ui.talentEffectLabel.setText(branchId == 1 ? String.format("加成效果：自帶 %d 顆 EMP 脈衝彈。", level) : (branchId == 2 ? String.format("加成效果：FW 弱化 +%d%%。", level * 5) : (branchId == 3 ? String.format("加成效果：閃現記憶時間 +%.2fs。", level * 0.15) : String.format("加成效果：被追蹤速度降低 %d%%，環境干擾機率下降。", level * 15)))); if (level <= currentLevelInBranch) { ui.talentCostLabel.setText("狀態：[ 數據已完美同步寫入 ]"); ui.talentCostLabel.setTextFill(Color.LIME); ui.btnUpgradeTalent.setVisible(false); } else if (level == currentLevelInBranch + 1) { ui.talentCostLabel.setText(String.format("升級消耗：%d ¢", cost)); ui.talentCostLabel.setTextFill(Color.GOLD); ui.btnUpgradeTalent.setText(">>> 執行數據寫入核心 <<<"); ui.btnUpgradeTalent.setVisible(true); ui.btnUpgradeTalent.setOnAction(e -> executeSelectedUpgrade(branchId, cost)); } else { ui.talentCostLabel.setText("狀態：[ 核心未串接 ]"); ui.talentCostLabel.setTextFill(Color.RED); ui.btnUpgradeTalent.setVisible(false); } ui.playDescFadeIn(); }
    private void executeSelectedUpgrade(int branchId, int cost) {
        if (p.buyLegacy(cost)) {
            if (branchId == 1) p.talentStartEMP++;
            else if (branchId == 2) p.talentWeakFW++;
            else if (branchId == 3) p.talentFlashTime++;
            else if (branchId == 4) p.talentSignalShield++;
            try { p.saveData(); } catch(Exception ex) {}
            ui.shakeScreen();
            ui.playFlashEffect(Color.rgb(0, 255, 204, 0.4), 400);
            playSuccessSound();
            ui.updateTalentUI();
            selectTalentNode(branchId, selectedLevel);
        } else {
            playNoMoneySound(); // 播放餘額不足專屬音效
            ui.triggerErrorEffect(ui.errorImage2, 0); // 傳入 0 避免重複播放普通的 errorSound2
            ui.talentCostLabel.setText("錯誤：[ Legacy Coins 不足 ]");
            ui.talentCostLabel.setTextFill(Color.RED);
        }
    }
    public void openTalentTree() { ui.playSweepTransition(Color.web("#FF007F")); engine.currentState = HackEngine.GameState.TALENT_TREE; ui.menuLayer.setVisible(false); ui.updateTalentUI(); ui.talentLayer.setVisible(true); }
    public void closeTalentTree() { ui.playSweepTransition(Color.CYAN); engine.currentState = HackEngine.GameState.MAIN_MENU; ui.talentLayer.setVisible(false); ui.menuLayer.setVisible(true); }
    public void handleHoneypotTrap() { }
    public void returnToMenu() { ui.playSweepTransition(Color.WHITE); ui.pauseLayer.setVisible(false); ui.gameLayer.setVisible(false); ui.shopLayer.setVisible(false); ui.gameOverLayer.setVisible(false); ui.routeLayer.setVisible(false); ui.menuLayer.setVisible(true); resetGame(); engine.currentState = HackEngine.GameState.MAIN_MENU; }
    public void enterShop() { ui.playSweepTransition(Color.LIME); ui.routeLayer.setVisible(false); ui.updateShopUI(); ui.shopLayer.setVisible(true); engine.currentState = HackEngine.GameState.SHOP; }
    public void resetGame() { engine.isEscapeSequence = false; p.reset(); engine.resetEvents(); ui.gameOverLayer.setVisible(false); ui.gameLayer.setVisible(true); ui.updateTraceUI(0); engine.currentState = HackEngine.GameState.PLAYING; engine.startNewRun(); bossManager.checkBossLevel(); }
    public static void main(String[] args) { launch(); }
}