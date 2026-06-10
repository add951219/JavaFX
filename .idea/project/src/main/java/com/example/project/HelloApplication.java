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
    public TutorialManager tutorialManager;

    private int selectedBranch = 0;
    private int selectedLevel = 0;

    private AudioClip errorSound1, errorSound2, loseSound;
    private AudioClip gunshotSound, pictureHitSound;
    public AudioClip bossIntroSound, bossPhaseSound, passSound; // 新增 passSound

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
    public TestModeManager testModeManager;
    public boolean isDemoMode = false;

    @Override
    public void start(Stage stage) {
        initAudio();
        ui = new UIManager(p, engine, this);
        bossManager = new BossManager(this, engine, ui, p);
        tutorialManager = new TutorialManager(this, engine, ui);
        testModeManager = new TestModeManager(this, engine, ui);
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
        try { playerHitSound = new AudioClip(getClass().getResource("/hit.mp3").toExternalForm()); } catch (Exception e) {}
        try { bossFailSound = new AudioClip(getClass().getResource("/boss_fail.mp3").toExternalForm()); } catch (Exception e) {}
        try { Media bgmMedia = new Media(getClass().getResource("/bgm.mp3").toExternalForm()); bgmPlayer = new MediaPlayer(bgmMedia); bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); bgmPlayer.setVolume(0.25); bgmPlayer.play(); } catch (Exception e) {}
        try { noMoneySound = new AudioClip(getClass().getResource("/no_money.mp3").toExternalForm()); } catch (Exception e) {}
        try { passSound = new AudioClip(getClass().getResource("/pass.mp3").toExternalForm()); } catch (Exception e) {} // 載入過關音效
    }

    public void playHoverSound() { if (hoverSound != null) { hoverSound.setVolume(getActualSfxVolume() * 0.5); hoverSound.play(); } }
    public void playClickSound() { if (clickSound != null) { clickSound.setVolume(getActualSfxVolume()); clickSound.play(); } }
    public void playPassSound() { if (passSound != null) { passSound.setVolume(getActualSfxVolume()); passSound.play(); } } // 播放過關音效方法
    public void playHackTickSound() {
        if (hackTickSound != null) {
            hackTickSound.setVolume(getActualSfxVolume() * 0.7); hackTickSound.play();
        }
    }
    public void setBgmVolume(double vol) { if (bgmPlayer != null) bgmPlayer.setVolume(vol * vol); }
    public double sfxVolume = 0.5;
    public void playErrorSound(int type) { if (type == 1 && errorSound1 != null) { errorSound1.setVolume(getActualSfxVolume()); errorSound1.play(); } else if (type == 2 && errorSound2 != null) { errorSound2.setVolume(getActualSfxVolume()); errorSound2.play(); } }
    public void setSfxVolume(double vol) {
        this.sfxVolume = vol; double actualVol = getActualSfxVolume();
        if (bossIntroSound != null) bossIntroSound.setVolume(actualVol); if (bossPhaseSound != null) bossPhaseSound.setVolume(actualVol);
        if (loseSound != null) loseSound.setVolume(actualVol); if (playerHitSound != null) playerHitSound.setVolume(actualVol);
        if (bossFailSound != null) bossFailSound.setVolume(actualVol); if (noMoneySound != null) noMoneySound.setVolume(actualVol);
        if (passSound != null) passSound.setVolume(actualVol); // 同步音量設定
    }

    public void playSuccessSound() {
        if (audioPool == null || audioPool.isShutdown()) return;
        audioPool.submit(() -> {
            try {
                SourceDataLine sdl = AudioSystem.getSourceDataLine(cachedAudioFormat); sdl.open(cachedAudioFormat);
                if (sdl.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
                    javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl) sdl.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(getActualSfxVolume() > 0.0001 ? getActualSfxVolume() : 0.0001) * 20.0); gainControl.setValue(dB);
                }
                sdl.start(); sdl.write(cachedSuccessBuf, 0, cachedSuccessBuf.length); sdl.drain(); sdl.close();
            } catch (Exception e) {}
        });
    }

    public void playTalentUnlockSynthSound() {
        if (audioPool == null || audioPool.isShutdown()) return;
        audioPool.submit(() -> {
            try {
                float sampleRate = 44100f; byte[] talentBuf = new byte[8000];
                for (int i = 0; i < talentBuf.length; i++) {
                    double frequency = 700.0;
                    if (i > talentBuf.length * 0.65) { frequency = 1400.0; } else if (i > talentBuf.length * 0.3) { frequency = 1050.0; }
                    double angle = i / (sampleRate / frequency) * 2.0 * Math.PI;
                    double envelope = Math.exp(-3.8 * i / talentBuf.length);
                    talentBuf[i] = (byte) (Math.sin(angle) * 45 * envelope);
                }
                SourceDataLine sdl = AudioSystem.getSourceDataLine(cachedAudioFormat); sdl.open(cachedAudioFormat);
                if (sdl.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
                    javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl) sdl.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(getActualSfxVolume() > 0.0001 ? getActualSfxVolume() : 0.0001) * 20.0); gainControl.setValue(dB);
                }
                sdl.start(); sdl.write(talentBuf, 0, talentBuf.length); sdl.drain(); sdl.close();
            } catch (Exception e) {}
        });
    }

    public void playGunshotSound() { if (gunshotSound != null) { gunshotSound.setVolume(getActualSfxVolume()); gunshotSound.play(); } }
    public void playPictureHitSound() { if (pictureHitSound != null) { pictureHitSound.setVolume(getActualSfxVolume()); pictureHitSound.play(); } }
    public void playPlayerHitSound() { if (playerHitSound != null) { playerHitSound.setVolume(getActualSfxVolume()); playerHitSound.play(); } }
    public void playBossFailSound() { if (bossFailSound != null) { bossFailSound.setVolume(getActualSfxVolume()); bossFailSound.play(); } }
    public void playNoMoneySound() { if (noMoneySound != null) { noMoneySound.setVolume(getActualSfxVolume()); noMoneySound.play(); } }

    private void setupInputHandlers(Scene scene) {
        scene.setOnMousePressed(e -> {
            if (engine.currentState == HackEngine.GameState.TUTORIAL) { tutorialManager.handleMousePress(e.getButton()); return; }
            if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight) {
                engine.isHacking = true; ui.playComboHitEffect(engine.comboMultiplier);
                playHackTickSound();
                lastHackSoundTime = System.currentTimeMillis();
            }
        });
        scene.setOnMouseReleased(e -> {
            if (engine.currentState == HackEngine.GameState.TUTORIAL) { tutorialManager.handleMouseRelease(e.getButton()); return; }
            if (e.getButton() == MouseButton.PRIMARY) {
                if (hackTickSound != null && hackTickSound.isPlaying()) { hackTickSound.stop(); }
                if (engine.currentState == HackEngine.GameState.PLAYING) {
                    if(engine.isHacking) { engine.isHacking = false; engine.dropCombo(p); }
                }
            }
        });
        scene.setOnKeyPressed(e -> {
            if (!ui.root.isFocused()) ui.root.requestFocus();

            // === Demo Zone 邏輯 ===
            if (engine.currentState == HackEngine.GameState.DEMO_MENU) {
                testModeManager.jumpToLevel(e.getCode());
                return;
            }
            if (e.getCode() == KeyCode.F9) {
                engine.currentState = HackEngine.GameState.DEMO_MENU;
                ui.showDemoMenu();
                return;
            }
            if (engine.currentState == HackEngine.GameState.TESTING) {
                testModeManager.jumpToLevel(e.getCode());
                return;
            }
            if (!ui.root.isFocused()) ui.root.requestFocus();
            if (engine.currentState == HackEngine.GameState.TUTORIAL) { tutorialManager.handleKeyPress(e.getCode()); return; }

            if (e.getCode() == KeyCode.F1) { if (engine.currentState == HackEngine.GameState.PLAYING) { engine.resetEvents(); ui.firewallLayer.setVisible(false); ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false); ui.bugCatchLayer.setVisible(false); ui.surgeLayer.setVisible(false); ui.typeWriterUpdate("[DEBUG_MODE] INSTANT WIN ACTIVATED."); playLevelClearExplosion(); } }
            if (e.getCode() == KeyCode.F2) { p.darkCoins += 50000; p.legacyCoins += 5000; ui.updateShopUI(); ui.updateTalentUI(); if (engine.currentState == HackEngine.GameState.PLAYING) ui.typeWriterUpdate("[DEBUG_MODE] +50000 DarkCoins / +5000 Legacy Coins."); }
            if (e.getCode() == KeyCode.F3) { p.currentLevel = ((p.currentLevel / 5) + 1) * 5 - 1; ui.updateASCIIProgress(); if (engine.currentState == HackEngine.GameState.PLAYING) ui.typeWriterUpdate("[DEBUG_MODE] WARPED TO BOSS ENTRANCE."); }
            if (e.getCode() == KeyCode.F4) { engine.coreHeat = 0; engine.isOverheated = false; engine.overheatEndTime = 0; engine.traceLevel = 0; engine.isBeingTraced = false; p.empCharges = 99; p.slowCharges = 99; ui.updateFirewallUI(); ui.updateTraceUI(0); ui.updateShopUI(); }
            if (e.getCode() == KeyCode.F5) {
                p.upgClick = 5; p.upgSpeed = 5; p.upgCoolant = 5; p.upgStealth = 5; p.empCharges = 99; p.slowCharges = 99;
                p.talentStartEMP = 3; p.talentWeakFW = 5; p.talentFlashTime = 3; p.talentSignalShield = 3;
                p.talentErrorCorrect = true; p.talentComboGuard = true; p.talentGlitchImmune = true; p.talentOverdrive = true; p.talentEdgeRunner = true; p.talentTrojanSplit = true; p.talentHeatDump = true; p.talentBugZapper = true; p.talentIntuition = true;
                ui.updateShopUI(); ui.updateTalentUI();
                if (engine.currentState == HackEngine.GameState.PLAYING) ui.typeWriterUpdate("[DEMO_MODE] ALL UPGRADES MAXED OUT.");
            }
            if (e.getCode() == KeyCode.F6) {
                if (engine.currentState == HackEngine.GameState.PLAYING) {
                    engine.activeGlitch = (engine.activeGlitch == HackEngine.GlitchType.NONE) ? HackEngine.GlitchType.VISUAL_DISTORTION : HackEngine.GlitchType.NONE;
                    ui.updateGlitchDisplay(); ui.typeWriterUpdate("[DEMO_MODE] VISUAL_DISTORTION TOGGLED.");
                }
            }

            if (e.getCode() == KeyCode.F7) {
                // F7：完全初始所有東西，包括局外天賦、金幣、最高紀錄
                p.legacyCoins = 0;
                p.highScore = 0;
                p.highestCombo = 1.0;

                // 重置所有天賦
                p.talentStartEMP = 0; p.talentWeakFW = 0; p.talentFlashTime = 0; p.talentSignalShield = 0;
                p.talentErrorCorrect = false; p.talentComboGuard = false; p.talentGlitchImmune = false;
                p.talentOverdrive = false; p.talentEdgeRunner = false; p.talentTrojanSplit = false;
                p.talentHeatDump = false; p.talentBugZapper = false; p.talentIntuition = false;

                // 重置局內資源與設定儲存檔
                p.reset();
                try { p.saveData(); } catch(Exception ex) {}

                // 更新畫面顯示
                ui.updateShopUI();
                ui.updateTalentUI();
                ui.updateFirewallUI();
                if (engine.currentState == HackEngine.GameState.PLAYING) {
                    ui.typeWriterUpdate("[DEBUG_MODE] COMPLETE WIPE SUCCESSFUL.");
                }
            }

            if (e.getCode() == KeyCode.F8) {
                // F8：一鍵獲得極大量金幣與永久貨幣 (突破上限)
                p.darkCoins += 9999999;
                p.legacyCoins += 999999;

                ui.updateShopUI();
                ui.updateTalentUI();
                if (engine.currentState == HackEngine.GameState.PLAYING) {
                    ui.typeWriterUpdate("[DEBUG_MODE] UNLIMITED WEALTH ACTIVATED.");
                }
            }

            if (e.getCode() == KeyCode.TAB && engine.isBossFight && engine.currentBossType == HackEngine.BossType.HYDRA && engine.bossPhase == 1) { engine.activeHydraHead = (engine.activeHydraHead + 1) % 3; ui.updateFirewallUI(); e.consume(); return; }

            if (engine.currentState == HackEngine.GameState.PLAYING) {
                if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.SURGE && engine.isSurgeFight) {
                    if (e.getCode() == KeyCode.A) { engine.surgePlayerPos = Math.max(0, engine.surgePlayerPos - 1); ui.updateSurgeUI(engine.getSurgeElapsed()); e.consume(); return; }
                    else if (e.getCode() == KeyCode.D) { engine.surgePlayerPos = Math.min(4, engine.surgePlayerPos + 1); ui.updateSurgeUI(engine.getSurgeElapsed()); e.consume(); return; }
                }

                boolean isActionKey = e.getCode().isLetterKey() || e.getCode().isDigitKey() || e.getCode() == KeyCode.SPACE;
                if (isActionKey) engine.runTotalKeystrokes++;

                if (e.getCode() == KeyCode.DIGIT1 || e.getCode() == KeyCode.NUMPAD1) {
                    if (engine.activeGlitch == HackEngine.GlitchType.CORE_OVERLOAD) ui.typeWriterUpdate("⚠ BLOCKED: CORE OVERLOAD ACTIVE ⚠");
                    else {
                        int empResult = engine.useEMP(p);
                        if (empResult > 0) {
                            if (empResult == 2) ui.typeWriterUpdate(">>> EMP DEPLOYED! TROJAN SPLIT REFUNDED RESOURCE!");
                            else ui.typeWriterUpdate(">>> EMP DEPLOYED!");
                            ui.updateShopUI(); ui.updateFirewallUI(); ui.playPulseEffect(); ui.playSweepTransition(Color.CYAN);
                        }
                    }
                }
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
                        p.overloadCharges--; ui.updateShopUI(); ui.typeWriterUpdate(">>> ⚠ CORE OVERLOAD VIRUS INJECTED ⚠"); ui.playFlashEffect(Color.RED, 500); playErrorSound(2); ui.shakeScreen();
                        if(engine.isBossFight && engine.currentBossType == HackEngine.BossType.HYDRA) { for(int i=0; i<3; i++) engine.hydraWalls[i] = Math.min(1.0, engine.hydraWalls[i] + 0.8); }
                        else { engine.firewallProgress = Math.min(1.0, engine.firewallProgress + 0.8); }
                        engine.coreHeat = 1.0; engine.isOverheated = true; engine.overheatEndTime = System.nanoTime() + 2_500_000_000L; ui.updateFirewallUI();
                    }
                }

                if (e.getCode() == KeyCode.DIGIT5 || e.getCode() == KeyCode.NUMPAD5) {
                    if (p.talentHeatDump && engine.heatDumpAvailable && engine.isFirewallFight) {
                        engine.heatDumpAvailable = false; engine.coreHeat = 0.0; engine.isOverheated = false;
                        playSuccessSound(); ui.playFlashEffect(Color.CYAN, 400); ui.typeWriterUpdate(">>> EMERGENCY HEAT DUMP EXECUTED. <<<");
                        ui.updateShopUI(); ui.updateFirewallUI();
                    }
                }

                if (e.getCode() == KeyCode.SPACE && engine.isFirewallFight) {
                    if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.PULSE && engine.isPulseFight) {
                        int result = engine.judgePulseHit();
                        if (result == 1) { playSuccessSound(); ui.playPulseHitEffect(); } else if (result == -1) { playErrorSound(1); ui.shakeScreen(); engine.dropCombo(p); }
                        ui.updatePulseScanUI();
                    }
                    else if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.MIMIC && !engine.isMimicWindow) {
                        engine.firewallProgress -= 0.08; playErrorSound(1); ui.shakeScreen(); ui.updateFirewallUI(); engine.dropCombo(p);
                    }
                    else if (engine.isOverheated) { playErrorSound(1); }
                    else {
                        engine.runCorrectKeystrokes++;
                        double dmgBase = 0.05 + (p.upgClick * 0.015);
                        if (p.talentOverdrive && engine.coreHeat >= 0.80 && !engine.isOverheated) {
                            dmgBase *= 1.5; ui.playFlashEffect(Color.rgb(255, 100, 0, 0.1), 30);
                        }

                        if(engine.isBossFight && engine.currentBossType == HackEngine.BossType.HYDRA) engine.hydraWalls[engine.activeHydraHead] += dmgBase;
                        else engine.firewallProgress += dmgBase;

                        // ====== 【修改 1】： 大幅增加每次按空白鍵時提升的熱量 ======
                        // 將原本的 0.025 提升至 0.08 (基礎值更高，過熱風險增加)
                        engine.coreHeat += Math.max(0.02, 0.08 - (p.upgCoolant * 0.005));

                        if (engine.coreHeat >= 1.0) { engine.isOverheated = true; engine.overheatEndTime = System.nanoTime() + 800_000_000L; playErrorSound(1); ui.shakeScreen(); ui.playFlashEffect(Color.RED, 250); engine.dropCombo(p); }
                        ui.updateFirewallUI(); ui.playFirewallSpacePopEffect(); ui.playComboHitEffect(engine.comboMultiplier);
                    }
                }

                if (engine.isInterceptFight) {
                    String input = e.getText().toUpperCase();
                    if (!input.isEmpty() && engine.sequenceIndex < engine.targetSequence.length()) {

                        // === 【MIMIC 欺敵機制加入】 ===
                        String targetChar = "";
                        if (engine.currentBossType == HackEngine.BossType.MIMIC || p.currentLevel == 999) {
                            targetChar = engine.targetSequence.substring(
                                    engine.targetSequence.length() - 1 - engine.sequenceIndex,
                                    engine.targetSequence.length() - engine.sequenceIndex
                            );
                        } else {
                            targetChar = engine.targetSequence.substring(engine.sequenceIndex, engine.sequenceIndex + 1);
                        }
                        // ===========================

                        if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.PULSE) {
                            if (input.equals(targetChar)) {
                                long now = System.nanoTime(); long targetTime = engine.pulseLetterDeadlines[engine.sequenceIndex];
                                if (Math.abs(now - targetTime) <= engine.pulseLetterWindow) {
                                    engine.sequenceIndex++; engine.runCorrectKeystrokes++; playSuccessSound(); ui.playComboHitEffect(engine.comboMultiplier);
                                    if (engine.sequenceIndex >= engine.targetSequence.length()) {
                                        engine.isInterceptFight = false; ui.interceptLayer.setVisible(false); ui.typeWriterUpdate(">>> RHYTHM SYNC COMPLETE.");
                                        if (bossPhaseSound != null) bossPhaseSound.play(); ui.playFlashEffect(Color.RED, 500); engine.bossPhase++; bossManager.startBossPhase();
                                    }
                                } else { ui.triggerErrorEffect(ui.errorImage1, 1); engine.interceptDeadline -= 1_000_000_000L; engine.dropCombo(p); }
                            } else { ui.triggerErrorEffect(ui.errorImage1, 1); engine.interceptDeadline -= 1_000_000_000L; engine.dropCombo(p); }
                        }
                        else {
                            if (input.equals(targetChar)) {
                                engine.sequenceIndex++; engine.runCorrectKeystrokes++; playSuccessSound(); ui.updateInterceptUI(); ui.playComboHitEffect(engine.comboMultiplier);
                                if (engine.sequenceIndex >= engine.targetSequence.length()) {
                                    engine.isInterceptFight = false; ui.interceptLayer.setVisible(false);
                                    if (engine.eventMistakes == 0 && p.talentEdgeRunner) { p.legacyCoins++; ui.typeWriterUpdate(">>> PERFECT. EDGE RUNNER BONUS: +1 ¢"); }
                                    else if (engine.isEscapeSequence) { ui.interceptTimeDisplay.setTextFill(Color.WHITE); ui.typeWriterUpdate(">>> OVERRIDE SUCCESSFUL. CONNECTION SEVERED SAFELY."); p.currentLevel++; triggerLevelClear(); engine.isEscapeSequence = false; }
                                    else if (!engine.isBossFight) { ui.typeWriterUpdate(">>> PACKET SECURED."); engine.currentSegment++; }
                                }
                            } else {
                                engine.eventMistakes++;
                                if (p.talentErrorCorrect && engine.errorCorrectCharges > 0) {
                                    engine.errorCorrectCharges--; playErrorSound(1); ui.typeWriterUpdate(">>> ERROR CORRECTED. NO PENALTY.");
                                } else {
                                    ui.triggerErrorEffect(ui.errorImage1, 1); engine.interceptDeadline -= 1_000_000_000L; engine.dropCombo(p);
                                }
                            }
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
                                if (!engine.isBossFight) {
                                    engine.isDecryptFight = false; ui.decryptLayer.setVisible(false);
                                    if (engine.eventMistakes == 0 && p.talentEdgeRunner) { p.legacyCoins++; ui.typeWriterUpdate(">>> PERFECT ENCRYPTION BROKEN. EDGE RUNNER BONUS: +1 ¢"); }
                                    else { ui.typeWriterUpdate(">>> ENCRYPTION BROKEN."); }
                                    engine.currentSegment++;
                                }
                            } else {
                                engine.eventMistakes++;
                                if (p.talentErrorCorrect && engine.errorCorrectCharges > 0) {
                                    engine.errorCorrectCharges--; playErrorSound(1); ui.typeWriterUpdate(">>> ERROR CORRECTED. NO PENALTY.");
                                    engine.decryptInput = ""; ui.updateDecryptUI();
                                } else {
                                    ui.triggerErrorEffect(ui.errorImage2, 2); engine.runCorrectKeystrokes = Math.max(0, engine.runCorrectKeystrokes - engine.decryptInput.length());
                                    engine.decryptInput = ""; engine.decryptDeadline -= 1_000_000_000L; ui.updateDecryptUI(); engine.dropCombo(p);
                                }
                            }
                        }
                    } else if (code == KeyCode.BACK_SPACE && engine.decryptInput.length() > 0) { engine.decryptInput = engine.decryptInput.substring(0, engine.decryptInput.length() - 1); ui.updateDecryptUI(); }
                }
            }
            if (e.getCode() == KeyCode.ESCAPE && engine.currentState == HackEngine.GameState.PLAYING) { engine.currentState = HackEngine.GameState.PAUSED; ui.showPauseMenu(); }
        });
    }
    private double getActualSfxVolume() { return sfxVolume * sfxVolume; }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            ui.drawMatrixRain();

            if (engine.currentState == HackEngine.GameState.TUTORIAL) { tutorialManager.updateLoop(System.nanoTime());
                return; }
            if (engine.currentState != HackEngine.GameState.PLAYING) return;
            long now = System.nanoTime();
            if (this.isDemoMode) {
                boolean eventActive = engine.isFirewallFight || engine.isInterceptFight ||
                        engine.isDecryptFight || engine.isBugCatchFight ||
                        engine.isBossFight;
                if (!eventActive) {
                    triggerLevelClear();
                    return;
                }
            }

            if (engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION) { ui.root.setTranslateX((engine.random.nextDouble() - 0.5) * 6.5); ui.root.setTranslateY((engine.random.nextDouble() - 0.5) * 6.5); if (engine.random.nextInt(15) == 0) { if (!"⚠ CRITICAL_ERROR: LINE_FRACTURE_DETECTION ⚠".equals(ui.statusLabel.getText())) ui.statusLabel.setText("⚠ CRITICAL_ERROR: LINE_FRACTURE_DETECTION ⚠"); } } else { ui.root.setTranslateX(0); ui.root.setTranslateY(0); }

            if (engine.isBossFight && !engine.isEscapeSequence) { bossManager.updateBossLoop(now); ui.updateASCIIProgress(); return; }

            if (engine.isFirewallFight) {
                // ====== 【修改 2】： 加快自然散熱的速度 ======
                if (engine.isOverheated) { if (now > engine.overheatEndTime) { engine.isOverheated = false; engine.coreHeat = 0.0; } }
                else { engine.coreHeat = Math.max(0, engine.coreHeat - 0.01); } // 從 0.001 提升到 0.006 (降得也快)

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

            if (engine.isHacking && !engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight && !engine.isSurgeFight) {
                engine.comboFrames++;
                engine.comboMultiplier = Math.min(3.0, 1.0 + (engine.comboFrames / 180.0));
                engine.updateMaxCombo();
                if (engine.comboMultiplier >= 2.0 && engine.random.nextInt(4) == 0) ui.playComboHitEffect(engine.comboMultiplier);
            }
            ui.updateComboDisplay(engine.comboMultiplier);

            if (!engine.isFirewallFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isBugCatchFight && !engine.isSurgeFight) {
                if (p.currentLevel > 5 && engine.random.nextInt(350) == 0 && !engine.isBeingTraced && engine.isHacking) {
                    engine.isBeingTraced = true; engine.traceLevel = 0.1; playErrorSound(2);
                }
                if (engine.isBeingTraced) {
                    if (engine.isHacking) {
                        double traceSpeed = Math.max(0.002, 0.015 - (p.upgStealth * 0.002) - (p.talentSignalShield * 0.002) + (p.upgMiner * 0.003));
                        engine.traceLevel += traceSpeed;
                        if (engine.traceLevel >= 1.0) {
                            if (p.hasTraceShield) {
                                p.hasTraceShield = false; engine.traceLevel = 0.0; playSuccessSound(); ui.playFlashEffect(Color.CYAN, 400); ui.typeWriterUpdate(">>> TRACE BLOCKED BY BACKUP SHIELD <<<"); ui.updateShopUI();
                            } else {
                                engine.progress = Math.max(0, engine.progress - 0.3); engine.isBeingTraced = false; engine.traceLevel = 0.0; playErrorSound(1); ui.shakeScreen(); ui.typeWriterUpdate(">>> ⚠ LOCATION COMPROMISED. PROGRESS LOST ⚠"); engine.dropCombo(p);
                            }
                        }
                    } else { engine.traceLevel -= 0.02; if (engine.traceLevel <= 0) { engine.isBeingTraced = false; engine.traceLevel = 0.0; } }
                    ui.updateTraceUI(engine.traceLevel);
                } else { ui.updateTraceUI(0); }

                double checkpointSize = 1.0 / engine.totalSegments; double securedProgress = engine.currentSegment * checkpointSize; double targetCheckpoint = (engine.currentSegment + 1) * checkpointSize;
                if (engine.isHacking) {
                    double hackSpd = 0.0022 + (p.upgSpeed * 0.0006);
                    if (p.talentOverdrive && engine.coreHeat >= 0.80 && !engine.isOverheated) hackSpd *= 1.5;

                    engine.progress += hackSpd;
                    long nowMs = System.currentTimeMillis();
                    if (nowMs - lastHackSoundTime >= hackSoundInterval) { playHackTickSound(); lastHackSoundTime = nowMs; hackSoundInterval = 70 + engine.random.nextInt(70); }

                    if (engine.progress >= targetCheckpoint) {
                        engine.progress = targetCheckpoint; engine.isHacking = false;
                        if (hackTickSound != null && hackTickSound.isPlaying()) { hackTickSound.stop(); }
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

    public void handleEventFailure() { engine.isInterceptFight = false; engine.isDecryptFight = false; engine.isBugCatchFight = false; ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false); ui.bugCatchLayer.setVisible(false); engine.progress = engine.currentSegment * (1.0 / engine.totalSegments); ui.shakeScreen(); ui.playFlashEffect(Color.rgb(255, 0, 0, 0.3), 300); ui.typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED."); engine.dropCombo(p); }
    public void playLevelClearExplosion() {
        playPassSound(); // ===== [修改重點] 移到這裡：綠色條一滿，準備開始閃爍特效前就立刻播放音效 =====

        engine.currentState = HackEngine.GameState.PAUSED;
        engine.isHacking = false;
        ui.updateTraceUI(0);
        Timeline explosion = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            Color randomColor = Color.color(engine.random.nextDouble(), engine.random.nextDouble(), engine.random.nextDouble());
            ui.uiBorder.setTextFill(randomColor);
            ui.uiBorder.setEffect(new DropShadow(25, randomColor));
            ui.root.setTranslateX((engine.random.nextDouble() - 0.5) * 12);
            ui.root.setTranslateY((engine.random.nextDouble() - 0.5) * 12);
        }));
        explosion.setCycleCount(15);
        explosion.setOnFinished(e -> {
            ui.root.setTranslateX(0); ui.root.setTranslateY(0); ui.uiBorder.setEffect(null);
            triggerLevelClear();
        });
        explosion.play();
    }

    public void triggerLevelClear() {
        // ===== [修改重點] 已經將 playPassSound(); 從這裡移除，避免重複播放兩次 =====

        if (this.isDemoMode || p.currentLevel == 999) {
            this.isDemoMode = false;
            p.currentLevel = 1;// 恢復等級
            ui.gameLayer.setVisible(false);
            engine.currentState = HackEngine.GameState.DEMO_MENU;
            ui.showDemoMenu();                 // 顯示選單
            return;                            // 【關鍵】這裡直接 return，不執行下面的過關邏輯
        }
        ui.playPulseEffect(); ui.playSweepTransition(Color.LIME);
        int baseReward = engine.isBossLevel(p.currentLevel) ? 800 : (100 + p.currentLevel * 10);
        int earned = (int)(baseReward * engine.comboMultiplier * p.routeRewardMult * (1.0 + p.upgMiner * 0.15));

        if (!engine.isEscapeSequence) p.darkCoins += earned;
        if (engine.isBossFight || engine.isBossLevel(p.currentLevel)) p.legacyCoins += 15;
        p.currentLevel++; engine.progress = 0.0; engine.currentSegment = 0;
        if (p.currentLevel > p.highScore) p.highScore = p.currentLevel;
        ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); ui.gameLayer.setVisible(false);
        engine.currentState = HackEngine.GameState.ROUTE_SELECT; ui.routeLayer.setVisible(true);
        engine.coreHeat = 0.0; engine.isOverheated = false; engine.isBeingTraced = false; engine.traceLevel = 0.0;
    }

    public void startIntroSequence() { engine.currentState = HackEngine.GameState.INTRO; ui.menuLayer.setVisible(false); ui.introLayer.setVisible(true); Label text = (Label) ui.introLayer.getChildren().get(0); String[] lines = {"WAKING UP SYSTEM...", "ACCESS GRANTED."}; Timeline introTimeline = new Timeline(); for (int i=0; i<lines.length; i++) { final int index = i; introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5 * (i+1)), e -> text.setText(lines[index]))); } introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(lines.length * 0.5 + 0.5), e -> { ui.introLayer.setVisible(false); ui.gameLayer.setVisible(true); engine.currentState = HackEngine.GameState.PLAYING; engine.startNewRun(); bossManager.checkBossLevel(); })); introTimeline.play(); }
    public void triggerGameOver(String reason) {
        if (this.isDemoMode) {
            this.isDemoMode = false;// 關閉 Demo 模式
            p.currentLevel = 1;
            ui.gameLayer.setVisible(false);
            engine.currentState = HackEngine.GameState.DEMO_MENU; // 強制回到 Demo 選單狀態
            ui.showDemoMenu(); // 呼叫介面顯示 Demo 選單畫面
            return; // 直接結束方法，阻止原本的死亡結算畫面
        }
        engine.isEscapeSequence = false;
        ui.interceptTimeDisplay.setTextFill(Color.WHITE);
        if (loseSound != null) {
            if (loseSound.isPlaying()) loseSound.stop(); loseSound.play();
        }
        engine.currentState = HackEngine.GameState.GAMEOVER;
        ui.shakeScreen();
        ui.playSweepTransition(Color.RED);
        ui.playFlashEffect(Color.rgb(255, 0, 0, 0.4), 800);
        ui.updateTraceUI(0);
        int earnedLegacy = p.darkCoins / 10;
        p.legacyCoins += earnedLegacy;
        if (engine.runMaxCombo > p.highestCombo) p.highestCombo = engine.runMaxCombo;
        try { p.saveData(); } catch (Exception ex) {} ui.updateTalentUI();
        String title = "SCRIPT KIDDIE";
        int apm = engine.getRunAPM();
        double acc = engine.getRunAccuracy();
        if (p.currentLevel >= 20) title = "CYBER DEMIGOD";
        else if (engine.runMaxCombo >= 3.0 && acc >= 95.0) title = "FLAWLESS GHOST";
        else if (apm >= 350) title = "KEYBOARD WARRIOR";
        else if (engine.runMaxCombo >= 2.5) title = "COMBO MASTER";
        else if (p.currentLevel > 5) title = "NET RUNNER";
        ui.showGameOverStats(reason, p.currentLevel, earnedLegacy, engine.runMaxCombo, apm, acc, title);
    }

    public void selectTalentNode(int branchId, int level) {
        this.selectedBranch = branchId; this.selectedLevel = level;
        int cost = 0; String branchName = ""; String desc = "";

        if (branchId == 1) {
            branchName = "控制組件優化 [EMP 強化]"; desc = String.format("自帶 %d 顆 EMP 脈衝彈。", level); cost = 50 * level;
        } else if (branchId == 2) {
            branchName = "防火牆漏洞利用 [FW 弱化]"; desc = String.format("FW 弱化 +%d%%。", level * 5); cost = 75 * level;
        } else if (branchId == 3) {
            branchName = "緩衝記憶體擴充 [FLASH 記憶]"; desc = String.format("閃現記憶時間 +%.2fs。", level * 0.15); cost = 100 * level;
        } else if (branchId == 4) {
            branchName = "訊號屏蔽防護 [防追蹤與干擾]"; desc = String.format("被追蹤速度降低，環境干擾機率下降 (Lv.%d)。", level); cost = 120 * level;
        } else if (branchId == 5) {
            if (level == 1) { branchName = "防呆協議 [Error Correct]"; desc = "攔截/解密前2次打錯不扣秒數與連擊。"; }
            if (level == 2) { branchName = "連擊保險 [Combo Guard]"; desc = "失誤時 Combo 不歸零，僅下降一階。"; }
            if (level == 3) { branchName = "防毒核心 [Glitch Immune]"; desc = "新節點有 30% 機率直接免疫環境詛咒。"; }
            cost = 150 * level;
        } else if (branchId == 6) {
            if (level == 1) { branchName = "熱能超頻 [Overdrive]"; desc = "熱量 80% 以上，破壞力與速度 +50%。"; }
            if (level == 2) { branchName = "極限駭客 [Edge Runner]"; desc = "事件零失誤，額外掉落 1 ¢ 永久貨幣。"; }
            cost = 200 * level;
        } else if (branchId == 7) {
            if (level == 1) { branchName = "木馬分裂 [Trojan Split]"; desc = "EMP 炸毀防禦有 25% 生成 1 顆消耗品。"; }
            if (level == 2) { branchName = "緊急散熱 [Heat Dump]"; desc = "解鎖技能 [5]，每局 1 次瞬間清空熱量。"; }
            cost = 200 * level;
        } else if (branchId == 8) {
            if (level == 1) { branchName = "滅蟲波段 [Bug Zapper]"; desc = "抓蟲事件中目標變大，干擾障礙蟲減速。"; }
            if (level == 2) { branchName = "直覺駭入 [Intuition]"; desc = "攔截事件預先高亮顯示接下來的 3 個按鍵。"; }
            cost = 200 * level;
        }

        ui.talentNameLabel.setText(String.format(">>> 解密節點：%s (等級 %d) <<<", branchName, level));
        ui.talentEffectLabel.setText("加成效果：" + desc);

        boolean isUnlocked = ui.unlocked(branchId, level);
        boolean isAvailable = ui.isNextAvailable(branchId, level);

        if (isUnlocked) {
            ui.talentCostLabel.setText("狀態：[ 數據已完美同步寫入 ]"); ui.talentCostLabel.setTextFill(Color.LIME); ui.btnUpgradeTalent.setVisible(false);
        } else if (isAvailable) {
            ui.talentCostLabel.setText(String.format("升級消耗：%d ¢", cost)); ui.talentCostLabel.setTextFill(Color.GOLD);
            ui.btnUpgradeTalent.setText(">>> 執行數據寫入核心 <<<"); ui.btnUpgradeTalent.setVisible(true);

            final int finalCost = cost;
            final String finalBranchName = branchName;

            ui.btnUpgradeTalent.setOnAction(e -> executeSelectedUpgrade(branchId, level, finalCost, finalBranchName));
        } else {
            ui.talentCostLabel.setText("狀態：[ 核心前置條件未滿足 ]"); ui.talentCostLabel.setTextFill(Color.RED); ui.btnUpgradeTalent.setVisible(false);
        }
        ui.playDescFadeIn();
    }

    private void executeSelectedUpgrade(int branchId, int level, int cost, String branchName) {
        if (p.buyLegacy(cost)) {
            if (branchId == 1) p.talentStartEMP++;
            else if (branchId == 2) p.talentWeakFW++;
            else if (branchId == 3) p.talentFlashTime++;
            else if (branchId == 4) p.talentSignalShield++;
            else if (branchId == 5) {
                if (level == 1) p.talentErrorCorrect = true;
                if (level == 2) p.talentComboGuard = true;
                if (level == 3) p.talentGlitchImmune = true;
            } else if (branchId == 6) {
                if (level == 1) p.talentOverdrive = true;
                if (level == 2) p.talentEdgeRunner = true;
            } else if (branchId == 7) {
                if (level == 1) p.talentTrojanSplit = true;
                if (level == 2) p.talentHeatDump = true;
            } else if (branchId == 8) {
                if (level == 1) p.talentBugZapper = true;
                if (level == 2) p.talentIntuition = true;
            }

            try { p.saveData(); } catch(Exception ex) {}

            ui.playTalentSuccessCinematic(branchName);
            playTalentUnlockSynthSound();

            ui.updateTalentUI();
            selectTalentNode(branchId, level);
        } else {
            playNoMoneySound();
            ui.triggerErrorEffect(ui.errorImage2, 0);
            ui.talentCostLabel.setText("錯誤：[ Legacy Coins 不足 ]");
            ui.talentCostLabel.setTextFill(Color.RED);
        }
    }

    public void openTalentTree() { ui.playSweepTransition(Color.web("#FF007F")); engine.currentState = HackEngine.GameState.TALENT_TREE; ui.menuLayer.setVisible(false); ui.updateTalentUI(); ui.talentLayer.setVisible(true); }
    public void closeTalentTree() { ui.playSweepTransition(Color.CYAN); engine.currentState = HackEngine.GameState.MAIN_MENU; ui.talentLayer.setVisible(false); ui.menuLayer.setVisible(true); }
    public void handleHoneypotTrap() { }

    public void returnToMenu() {
        ui.playSweepTransition(Color.WHITE);
        ui.pauseLayer.setVisible(false);
        ui.gameLayer.setVisible(false);
        ui.shopLayer.setVisible(false);
        ui.gameOverLayer.setVisible(false);
        ui.routeLayer.setVisible(false);
        if (ui.tutorialLayer != null) { ui.tutorialLayer.setVisible(false); }
        ui.menuLayer.setVisible(true);

        engine.isEscapeSequence = false;
        p.reset(); engine.resetEvents(); ui.updateTraceUI(0);
        engine.currentState = HackEngine.GameState.MAIN_MENU;
    }

    public void enterShop() { ui.playSweepTransition(Color.LIME); ui.routeLayer.setVisible(false); ui.updateShopUI(); ui.shopLayer.setVisible(true); engine.currentState = HackEngine.GameState.SHOP; }
    public void resetGame() { engine.isEscapeSequence = false; p.reset(); engine.resetEvents(); ui.gameOverLayer.setVisible(false); ui.gameLayer.setVisible(true); ui.updateTraceUI(0); engine.currentState = HackEngine.GameState.PLAYING; engine.startNewRun(); bossManager.checkBossLevel(); }
    public static void main(String[] args) { launch(); }
}