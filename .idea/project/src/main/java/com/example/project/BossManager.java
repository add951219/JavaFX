package com.example.project;

import javafx.scene.paint.Color;

public class BossManager {
    private final HelloApplication app;
    private final HackEngine engine;
    private final UIManager ui;
    private final PlayerStats p;

    public BossManager(HelloApplication app, HackEngine engine, UIManager ui, PlayerStats p) {
        this.app = app; this.engine = engine; this.ui = ui; this.p = p;
    }

    public void checkBossLevel() {
        engine.rollGlitch(p.currentLevel, p); // === 修改這裡 ===
        ui.updateGlitchDisplay();
        engine.currentBossType = engine.determineBossType(p.currentLevel);

        if (engine.currentBossType != HackEngine.BossType.NONE) {
            engine.isBossFight = true; engine.bossPhase = 1; engine.bossRage = 0;
            engine.isEscapeSequence = false;

            engine.maxBossPhase = (engine.currentBossType == HackEngine.BossType.NULL_GOD) ? 4 : 3;
            if (engine.currentBossType == HackEngine.BossType.SURGE) engine.maxBossPhase = 1; // SURGE 只有一個 90s 階段

            engine.totalSegments = engine.maxBossPhase;
            ui.uiBorder.setTextFill(Color.RED);

            String title = "⚠ THREAT DETECTED ⚠";
            String codeName = engine.currentBossType.name();
            Color c = Color.RED;

            if(engine.currentBossType == HackEngine.BossType.PULSE) c = Color.web("#FF007F");
            if(engine.currentBossType == HackEngine.BossType.SURGE) c = Color.YELLOW;
            if(engine.currentBossType == HackEngine.BossType.CERBERUS) c = Color.ORANGE;
            if(engine.currentBossType == HackEngine.BossType.MIMIC) c = Color.LIGHTPINK;
            if(engine.currentBossType == HackEngine.BossType.HYDRA) c = Color.GREENYELLOW;
            if(engine.currentBossType == HackEngine.BossType.SPECTER) c = Color.DARKGRAY;
            if(engine.currentBossType == HackEngine.BossType.NULL_GOD) c = Color.WHITE;

            if (app.bossIntroSound != null) app.bossIntroSound.play();

            engine.currentState = HackEngine.GameState.INTRO;
            ui.playBossIntroAnimation(title, codeName, c, () -> {
                engine.currentState = HackEngine.GameState.PLAYING;
                startBossPhase();
            });
        } else {
            engine.totalSegments = 4; ui.uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); ui.statusLabel.setTextFill(Color.CYAN);
        }
    }

    public void startBossPhase() {
        long now = System.nanoTime();
        double rageMult = 1.0 - (engine.bossRage * 0.15);

        if(engine.bossPhase > 1) {
            engine.dropCombo(p); // Boss 換階段稍微掉連擊
        }

        // === SURGE Boss (獨立生存階段) ===
        if (engine.currentBossType == HackEngine.BossType.SURGE) {
            engine.isSurgeFight = true;
            engine.surgeStartTime = now;
            engine.surgePlayerPos = 2; // 起始在中間
            engine.surgeHP = 3;
            engine.surgeHealsDone = 0;
            engine.surgeNextAttackTime = now + 2_000_000_000L; // 2 秒後開始第一波攻擊
            for(int i=0; i<5; i++) { engine.surgeWarnings[i] = false; engine.surgeExplosions[i] = false; }
            ui.surgeLayer.setVisible(true);
            ui.updateSurgeUI(0.0);
            ui.typeWriterUpdate(">>> SURGE DETECTED. SURVIVE FOR 90 SECONDS.");
            return;
        }

        // === PULSE Boss ===
        if (engine.currentBossType == HackEngine.BossType.PULSE) {
            if (engine.bossPhase == 1) {
                engine.pulseScanSpeed = 0.012 * (1.0 + engine.bossRage * 0.1);
                engine.startPulseFirewall(now); ui.firewallLayer.setVisible(true); ui.updatePulseScanUI();
                ui.typeWriterUpdate(">>> PULSE DETECTED. SYNCHRONIZE YOUR INPUT.");
            }
            else if (engine.bossPhase == 2) {
                engine.startPulseBeat(p, now); ui.interceptLayer.setVisible(true); ui.updatePulseInterceptUI(now);
                ui.typeWriterUpdate(">>> MATCH THE BEAT. INPUT ON THE MARK.");
            }
            else if (engine.bossPhase == 3) {
                engine.startPulseReveal(p, now); ui.decryptLayer.setVisible(true); ui.decryptLayer.toFront();
                ui.decryptTargetDisplay.setText("[ GET READY ]"); ui.decryptTargetDisplay.setTextFill(Color.YELLOW); ui.updateDecryptUI();
                ui.typeWriterUpdate(">>> MEMORIZE THE SEQUENCE. IT ONLY SHOWS ONCE.");
            }
            return;
        }

        // === 一般 Boss ===
        if (engine.bossPhase == 1) {
            engine.isFirewallFight = true; ui.firewallLayer.setVisible(true); engine.firewallProgress = 0.2 + (p.talentWeakFW * 0.05);
            if (engine.currentBossType == HackEngine.BossType.CERBERUS) engine.cerberusGlobalDeadline = now + (long)(35.0 * 1_000_000_000L);
            if (engine.currentBossType == HackEngine.BossType.MIMIC) { engine.isMimicWindow = true; engine.mimicToggleTime = now + 1_500_000_000L; }
            if (engine.currentBossType == HackEngine.BossType.HYDRA) { engine.hydraWalls = new double[]{0.3, 0.3, 0.3}; engine.activeHydraHead = 0; }
            if (engine.currentBossType == HackEngine.BossType.NULL_GOD) { engine.isBeingTraced = true; engine.traceLevel = 0.0; }
        }
        else if (engine.bossPhase == 2) {
            engine.isInterceptFight = true; engine.sequenceIndex = 0; engine.eventMistakes = 0; int len = 8 + (p.currentLevel / 3);
            if (engine.currentBossType == HackEngine.BossType.NULL_GOD) { engine.targetSequence = engine.generateBossAlphaNum(len); engine.displaySequence = engine.targetSequence; }
            else { String[] dirs = {"W", "A", "S", "D"}; StringBuilder sb = new StringBuilder(); for (int i=0; i<len; i++) sb.append(dirs[engine.random.nextInt(4)]); engine.targetSequence = sb.toString(); engine.displaySequence = engine.targetSequence; }
            if (engine.currentBossType == HackEngine.BossType.MIMIC) { engine.displaySequence = engine.targetSequence; engine.targetSequence = new StringBuilder(engine.targetSequence).reverse().toString(); }
            if (engine.currentBossType == HackEngine.BossType.SPECTER) { engine.isSpecterHidden = false; engine.specterHideTime = now + 1_000_000_000L; }
            double baseTime = Math.max(5.0, 10.0 - (p.currentLevel * 0.15));
            engine.interceptDeadline = now + (long)(baseTime * rageMult * 1_000_000_000L);
            if(p.talentErrorCorrect) engine.errorCorrectCharges = 2;
            ui.interceptLayer.setVisible(true); ui.updateInterceptUI();
        }
        else if (engine.bossPhase == 3) {
            engine.isDecryptFight = true; engine.decryptInput = ""; engine.isDecryptFlashed = false; engine.eventMistakes = 0; int len = 6 + (p.currentLevel / 4);
            StringBuilder sb = new StringBuilder(); for(int i=0; i<len; i++) sb.append((char)(engine.random.nextInt(26) + 'A')); engine.decryptTarget = sb.toString();
            if (engine.currentBossType == HackEngine.BossType.MIMIC) { engine.isDecryptFlashed = true; ui.decryptTargetDisplay.setText("? ? ? ? ?"); engine.decryptFlashEndTime = now + 500_000_000L; }
            else { double flashTime = Math.max(0.3, 1.2 + (p.talentFlashTime * 0.15) - (p.currentLevel * 0.02)) * rageMult; engine.decryptFlashEndTime = now + (long)(flashTime * 1_000_000_000L); ui.decryptTargetDisplay.setText(engine.decryptTarget); }
            double baseDecTime = Math.max(4.0, 8.0 - (p.currentLevel * 0.15));
            engine.decryptDeadline = now + (long)(baseDecTime * rageMult * 1_000_000_000L);
            if(p.talentErrorCorrect) engine.errorCorrectCharges = 2;
            ui.decryptLayer.setVisible(true); ui.updateDecryptUI();
        }
        else if (engine.bossPhase == 4) {
            engine.isDecryptFight = true; engine.decryptInput = ""; engine.decryptTarget = engine.generateBossAlphaNum(8);
            engine.lastArchitectShiftTime = now; engine.decryptDeadline = now + (long)(12.0 * rageMult * 1_000_000_000L); ui.decryptLayer.setVisible(true); ui.updateDecryptUI();
        }
    }

    public void handleBossFailure(String reason) {
        engine.currentState = HackEngine.GameState.PAUSED;
        ui.firewallLayer.setVisible(false); ui.interceptLayer.setVisible(false); ui.decryptLayer.setVisible(false);
        ui.surgeLayer.setVisible(false); // 確保 SURGE 圖層被隱藏
        engine.isFirewallFight = false; engine.isInterceptFight = false; engine.isDecryptFight = false; engine.isSurgeFight = false;

        ui.shakeScreen(); ui.playFlashEffect(Color.RED, 600); app.playBossFailSound();

        if (engine.bossRage >= 3) { app.triggerGameOver("FATAL ERROR: BOSS ENRAGE LIMIT REACHED"); return; }
        engine.bossRage++;
        ui.bossFailReason.setText("ERROR: " + reason);
        ui.btnBossRetry.setText(">>> REBOOT PHASE [Rage: " + engine.bossRage + "/3] <<<");
        ui.bossFailLayer.setVisible(true);
    }

    public void retryBossPhase() {
        engine.isEscapeSequence = false; ui.bossFailLayer.setVisible(false);
        engine.currentState = HackEngine.GameState.PLAYING; startBossPhase();
    }

    public void escapeBoss() {
        ui.bossFailLayer.setVisible(false); engine.isBossFight = false; engine.isEscapeSequence = true; engine.isInterceptFight = true; engine.sequenceIndex = 0;
        engine.targetSequence = engine.generateBossAlphaNum(6); engine.displaySequence = engine.targetSequence;
        engine.interceptDeadline = System.nanoTime() + (long)(3.0 * 1_000_000_000L);
        ui.interceptLayer.setVisible(true); ui.updateInterceptUI(); ui.interceptTimeDisplay.setTextFill(Color.RED); ui.typeWriterUpdate("⚠ FATAL OVERRIDE INITIATED - TYPE TO SURVIVE ⚠");
        engine.currentState = HackEngine.GameState.PLAYING;
    }

    public void updateBossLoop(long now) {
        if (engine.currentBossType == HackEngine.BossType.CERBERUS) {
            double tLeft = (engine.cerberusGlobalDeadline - now) / 1_000_000_000.0;
            ui.statusLabel.setText(String.format("⚠ GLOBAL TIMER: %.1fs ⚠", Math.max(0, tLeft)));
            if (now > engine.cerberusGlobalDeadline) { handleBossFailure("CERBERUS TIMEOUT - FULL RESET"); return; }
        }

        if (engine.currentBossType == HackEngine.BossType.SURGE && engine.isSurgeFight) {
            handleSurgeLoop(now); return;
        }

        if (engine.isFirewallFight) handleFirewallLoop(now);
        else if (engine.isInterceptFight) handleInterceptLoop(now);
        else if (engine.isDecryptFight) handleDecryptLoop(now);
    }

    // === SURGE 專屬執行迴圈 ===
    private void handleSurgeLoop(long now) {
        double elapsed = (now - engine.surgeStartTime) / 1_000_000_000.0;
        engine.updateSurgeHeal(elapsed);

        if (elapsed >= 90.0) {
            // 撐過 90 秒，直接過關！
            engine.isSurgeFight = false; engine.isBossFight = false; ui.surgeLayer.setVisible(false);
            app.playLevelClearExplosion();
            return;
        }

        // 生成新攻擊
        if (now > engine.surgeNextAttackTime) {
            engine.generateSurgeAttack(elapsed, now);
        }

        // 預警結束，準備引爆
        if (engine.surgeWarnEndTime != 0 && now > engine.surgeWarnEndTime) {
            if (!engine.isSurgeFakeOut) {
                // 真爆炸
                for (int i=0; i<5; i++) engine.surgeExplosions[i] = engine.surgeWarnings[i];
                if (engine.surgeExplosions[engine.surgePlayerPos]) {
                    engine.surgeHP--; app.playPlayerHitSound(); ui.shakeScreen(); ui.playFlashEffect(Color.RED, 300);
                }
            }
            for(int i=0; i<5; i++) engine.surgeWarnings[i] = false;
            engine.surgeWarnEndTime = 0;
            engine.surgeExplodeEndTime = now + 150_000_000L; // 爆炸視覺殘留 0.15 秒
        }

        // 引爆結束，清空畫面
        if (engine.surgeExplodeEndTime != 0 && now > engine.surgeExplodeEndTime) {
            for(int i=0; i<5; i++) engine.surgeExplosions[i] = false;
            engine.surgeExplodeEndTime = 0;
        }

        ui.updateSurgeUI(elapsed);

        if (engine.surgeHP <= 0) {
            handleBossFailure("SURGE OVERWHELM: CRITICAL DAMAGE");
        }
    }

    private void handleFirewallLoop(long now) {
        if (engine.currentBossType == HackEngine.BossType.PULSE) {
            engine.updatePulseScan(now); ui.updatePulseScanUI();
            if (now > engine.pulsePhase1Deadline) { handleBossFailure("SYNC TIMEOUT"); }
            else if (engine.pulseHitsCount >= engine.pulseHitsRequired) {
                engine.isPulseFight = false; engine.isFirewallFight = false; ui.firewallLayer.setVisible(false);
                ui.typeWriterUpdate(">>> SYNCHRONIZATION ACHIEVED.");
                if (app.bossPhaseSound != null) app.bossPhaseSound.play();
                ui.playSweepTransition(Color.CYAN); engine.bossPhase = 2; startBossPhase();
            }
            return;
        }

        if (engine.isOverheated) { if (now > engine.overheatEndTime) { engine.isOverheated = false; engine.coreHeat = 0.0; } }
        else { engine.coreHeat = Math.max(0, engine.coreHeat - 0.005); }

        double drainRate = 0.0015; if (engine.isOverheated) drainRate *= 0.15;
        if (engine.currentBossType == HackEngine.BossType.MIMIC) {
            if (now > engine.mimicToggleTime) { engine.isMimicWindow = !engine.isMimicWindow; engine.mimicToggleTime = now + (engine.isMimicWindow ? 1_500_000_000L : 1_000_000_000L); }
        }

        boolean hydraDead = false;
        if (engine.currentBossType == HackEngine.BossType.HYDRA) {
            engine.hydraWalls[0] -= drainRate; engine.hydraWalls[1] -= drainRate; engine.hydraWalls[2] -= drainRate;
            if (engine.hydraWalls[0] <= 0 || engine.hydraWalls[1] <= 0 || engine.hydraWalls[2] <= 0) hydraDead = true;
        } else { engine.firewallProgress -= drainRate; }
        ui.updateFirewallUI();

        if (engine.firewallProgress <= 0 || hydraDead) handleBossFailure("FIREWALL PURGE");
        else {
            boolean pass = (engine.currentBossType == HackEngine.BossType.HYDRA) ?
                    (engine.hydraWalls[0] >= 1.0 && engine.hydraWalls[1] >= 1.0 && engine.hydraWalls[2] >= 1.0) : (engine.firewallProgress >= 1.0);
            if (pass) {
                engine.isFirewallFight = false; ui.firewallLayer.setVisible(false); ui.typeWriterUpdate(">>> OUTER SHELL BROKEN. ADVANCING TO PHASE 2...");
                if (app.bossPhaseSound != null) app.bossPhaseSound.play();
                ui.playSweepTransition(Color.WHITE); engine.bossPhase = 2; startBossPhase();
            }
        }
    }

    private void handleInterceptLoop(long now) {
        if (engine.currentBossType == HackEngine.BossType.PULSE) {
            ui.updatePulseInterceptUI(now);
            if (engine.sequenceIndex < engine.pulseLetterDeadlines.length) {
                if (now > engine.pulseLetterDeadlines[engine.sequenceIndex] + engine.pulseLetterWindow) { handleBossFailure("MISSED HEARTBEAT"); return; }
            }
            return;
        }
        if (engine.currentBossType == HackEngine.BossType.SPECTER && !engine.isSpecterHidden && now > engine.specterHideTime) { engine.isSpecterHidden = true; ui.updateInterceptUI(); }

        double timeLeft = (engine.interceptDeadline - now) / 1_000_000_000.0;
        if (engine.currentBossType != HackEngine.BossType.CERBERUS) ui.interceptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft))); else ui.interceptTimeDisplay.setText("HURRY!");

        if (now > engine.interceptDeadline && engine.currentBossType != HackEngine.BossType.CERBERUS) handleBossFailure("INTERCEPT TIMEOUT");
        else if (engine.sequenceIndex >= engine.targetSequence.length()) {
            engine.isInterceptFight = false; ui.interceptLayer.setVisible(false);

            // 天賦：極限駭客 結算
            if (engine.eventMistakes == 0 && p.talentEdgeRunner) {
                p.legacyCoins++;
                ui.typeWriterUpdate(">>> CORE SEQUENCE INJECTED. EDGE RUNNER BONUS: +1 ¢");
            } else {
                ui.typeWriterUpdate(">>> CORE SEQUENCE INJECTED. RAGE MODE ACTIVATED!");
            }

            if (app.bossPhaseSound != null) app.bossPhaseSound.play();
            ui.playFlashEffect(Color.RED, 500); engine.bossPhase++; startBossPhase();
        }
    }

    private void handleDecryptLoop(long now) {
        if (engine.currentBossType == HackEngine.BossType.PULSE) {
            if (!engine.pulseAllRevealed) {
                if (now >= engine.pulseNextRevealTime) {
                    if (engine.pulseRevealIndex <= engine.pulseRevealChars.length) {
                        StringBuilder display = new StringBuilder();
                        for (int i = 0; i < engine.decryptTarget.length(); i++) {
                            display.append(i < engine.pulseRevealIndex ? engine.decryptTarget.charAt(i) : '?');
                            if (i < engine.decryptTarget.length()-1) display.append(" ");
                        }
                        ui.decryptTargetDisplay.setText(display.toString()); ui.decryptTargetDisplay.setTextFill(Color.WHITE);
                        if (app.bossPhaseSound != null) app.bossPhaseSound.play();
                        engine.pulseRevealIndex++; engine.pulseNextRevealTime = now + engine.pulseRevealInterval;
                    } else {
                        engine.pulseAllRevealed = true; engine.decryptDeadline = now + 5_000_000_000L;
                        StringBuilder q = new StringBuilder();
                        for(int i=0; i<engine.decryptTarget.length(); i++) { q.append("?"); if(i < engine.decryptTarget.length()-1) q.append(" "); }
                        ui.decryptTargetDisplay.setText(q.toString()); ui.typeWriterUpdate(">>> NOW. TYPE IT FROM MEMORY.");
                    }
                }
                return;
            }
        }

        if (engine.currentBossType == HackEngine.BossType.MIMIC) { if (engine.isDecryptFlashed && now > engine.decryptFlashEndTime) { engine.isDecryptFlashed = false; ui.decryptTargetDisplay.setText(engine.decryptTarget); } }
        else if (engine.currentBossType != HackEngine.BossType.PULSE) { if (!engine.isDecryptFlashed && now > engine.decryptFlashEndTime) { engine.isDecryptFlashed = true; ui.decryptTargetDisplay.setText("? ? ? ? ?"); } }

        if ((engine.currentBossType == HackEngine.BossType.ARCHITECT || engine.currentBossType == HackEngine.BossType.NULL_GOD) && (now - engine.lastArchitectShiftTime > 2_000_000_000L)) {
            engine.shiftArchitectTarget(); ui.decryptTargetDisplay.setText(engine.decryptTarget); ui.triggerErrorEffect(ui.errorImage1, 1); engine.lastArchitectShiftTime = now;
            if(engine.currentBossType == HackEngine.BossType.NULL_GOD) engine.decryptInput = ""; ui.updateDecryptUI();
        }

        double timeLeft = (engine.decryptDeadline - now) / 1_000_000_000.0;
        if (engine.currentBossType != HackEngine.BossType.CERBERUS) ui.decryptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft))); else ui.decryptTimeDisplay.setText("FINAL RUSH!");

        if (now > engine.decryptDeadline && engine.currentBossType != HackEngine.BossType.CERBERUS) handleBossFailure("DECRYPT TIMEOUT");
        else if (engine.decryptInput.equals(engine.decryptTarget)) {
            engine.isDecryptFight = false; ui.decryptLayer.setVisible(false);

            // 天賦：極限駭客 結算
            if (engine.eventMistakes == 0 && p.talentEdgeRunner) {
                p.legacyCoins++;
                ui.typeWriterUpdate(">>> ENCRYPTION BROKEN. EDGE RUNNER BONUS: +1 ¢");
            }

            if(engine.bossPhase >= engine.maxBossPhase) { engine.isBossFight = false; app.playLevelClearExplosion(); }
            else { engine.bossPhase++; startBossPhase(); }
        }
    }
}