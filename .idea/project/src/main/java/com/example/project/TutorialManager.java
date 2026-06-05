package com.example.project;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

public class TutorialManager {
    private final HelloApplication app;
    private final HackEngine engine;
    private final UIManager ui;

    public int tutorialStep = 0;
    public double tutorialProgress = 0.0;
    public double tutorialHeat = 0.0;
    public String targetSeq = "WASD";
    public int seqIndex = 0;

    // 解密課程變數
    public String decryptTarget = "NEON";
    public String decryptInput = "";
    public boolean isDecryptFlashed = false;
    public long decryptFlashTime = 0;

    public TutorialManager(HelloApplication app, HackEngine engine, UIManager ui) {
        this.app = app; this.engine = engine; this.ui = ui;
    }

    public void startTutorial() {
        engine.currentState = HackEngine.GameState.TUTORIAL;
        ui.menuLayer.setVisible(false);
        ui.tutorialLayer.setVisible(true);
        tutorialStep = 0;
        tutorialProgress = 0;
        tutorialHeat = 0;
        seqIndex = 0;
        decryptInput = "";
        isDecryptFlashed = false;
        ui.updateTutorialUI(">>> 系統初始化...\n歡迎來到 Neon Breach 新手模擬訓練。\n\n[點擊滑鼠左鍵] 開始訓練", "");
    }

    public void handleMousePress(MouseButton button) {
        if (button != MouseButton.PRIMARY) return;

        if (tutorialStep == 0) {
            tutorialStep = 1;
            ui.updateTutorialUI(">>> 課程 1：基礎駭入\n按住 [滑鼠左鍵] 不放，來推進一般節點的駭入進度。", "進度: 0%");
        } else if (tutorialStep == 1) {
            engine.isHacking = true;
            app.playHackTickSound();
        } else if (tutorialStep == 5) {
            endTutorial();
        }
    }

    public void handleMouseRelease(MouseButton button) {
        if (button == MouseButton.PRIMARY && tutorialStep == 1) {
            engine.isHacking = false;
        }
    }

    public void handleKeyPress(KeyCode code) {
        if (tutorialStep == 2 && code == KeyCode.SPACE) {
            tutorialProgress += 0.08;
            tutorialHeat += 0.15; // 故意讓熱量累積很快，逼玩家停下來

            app.playSuccessSound();
            ui.playFirewallSpacePopEffect();

            if (tutorialHeat >= 1.0) {
                app.playErrorSound(1);
                ui.shakeScreen();
                ui.updateTutorialUI(">>> ⚠ 警告：核心過熱！\n狂按空白鍵會累積熱量。若達 100% 系統會被鎖定扣血！\n(請等待散熱降溫後繼續敲擊)", getFirewallStr());
            } else if (tutorialProgress >= 1.0) {
                tutorialStep = 3;

                app.playSuccessSound();
                ui.shakeScreen();
                ui.playFlashEffect(Color.LIME, 500);

                ui.updateTutorialUI(">>> 課程 3：攔截封包\n遊戲中會出現限時攔截事件。請依照順序輸入以下按鍵：\n", "請輸入: " + targetSeq);
            } else {
                ui.updateTutorialUI(">>> 課程 2：突破防火牆\n遇到防火牆時，連打 [空白鍵] 來擊破防禦！\n注意下方的熱量警告，快滿時必須停手。", getFirewallStr());
            }
        } else if (tutorialStep == 3) {
            String input = code.toString().toUpperCase();
            if (seqIndex < targetSeq.length() && input.equals(String.valueOf(targetSeq.charAt(seqIndex)))) {
                seqIndex++;
                app.playSuccessSound();

                if (seqIndex >= targetSeq.length()) {
                    tutorialStep = 4;
                    decryptFlashTime = System.nanoTime() + 2_000_000_000L; // 2秒後閃現結束

                    app.playSuccessSound();
                    ui.shakeScreen();
                    ui.playFlashEffect(Color.LIME, 500);

                    ui.updateTutorialUI(">>> 課程 4：記憶解密\n密碼只會短暫顯示一次，請先【記憶】下來。\n文字消失後，憑記憶輸入破解！", "目標密碼: " + decryptTarget);
                } else {
                    StringBuilder display = new StringBuilder();
                    for(int i = 0; i < targetSeq.length(); i++) {
                        if(i < seqIndex) display.append("[✓] ");
                        else display.append(targetSeq.charAt(i)).append(" ");
                    }
                    ui.updateTutorialUI(">>> 課程 3：攔截封包\n遊戲中會出現限時攔截事件。請依照順序輸入以下按鍵：\n", display.toString());
                }
            } else if (code.isLetterKey() || code.isDigitKey()) {
                app.playErrorSound(1);
                ui.shakeScreen();
            }
        } else if (tutorialStep == 4) {
            if (!isDecryptFlashed) return; // 還沒閃現完不能輸入

            String inputChar = "";
            if (code.isLetterKey()) inputChar = code.toString();
            else if (code.isDigitKey()) inputChar = code.toString().replace("DIGIT", "");

            if (!inputChar.isEmpty()) {
                decryptInput += inputChar;
                app.playSuccessSound();

                if (decryptInput.length() >= decryptTarget.length()) {
                    if (decryptInput.equals(decryptTarget)) {
                        tutorialStep = 5;
                        app.playSuccessSound();
                        ui.shakeScreen();
                        ui.playFlashEffect(Color.LIME, 500);
                        ui.updateTutorialUI(">>> 訓練完成！\n四大基礎防禦你都已經掌握。實戰中還會遇到抓蟲與防追蹤機制。\n\n[點擊滑鼠左鍵] 領取 500 ¢ 獎金並返回主畫面", "");
                    } else {
                        app.playErrorSound(2);
                        ui.shakeScreen();
                        decryptInput = ""; // 答錯清空重來
                        ui.updateTutorialUI(">>> 課程 4：記憶解密 (密碼錯誤！)\n憑記憶輸入破解！(答錯會清空輸入)", "輸入: " + decryptInput + "_");
                    }
                } else {
                    ui.updateTutorialUI(">>> 課程 4：記憶解密\n憑記憶輸入破解！", "輸入: " + decryptInput + "_");
                }
            } else if (code == KeyCode.BACK_SPACE && decryptInput.length() > 0) {
                decryptInput = decryptInput.substring(0, decryptInput.length() - 1);
                ui.updateTutorialUI(">>> 課程 4：記憶解密\n憑記憶輸入破解！", "輸入: " + decryptInput + "_");
            }
        }
    }

    private String getFirewallStr() {
        int pBar = (int) (Math.min(1.0, tutorialProgress) * 10);
        int hBar = (int) (Math.min(1.0, tutorialHeat) * 10);
        return "防禦: [" + "|".repeat(pBar) + ".".repeat(10 - pBar) + "]\n熱量: [" + "█".repeat(hBar) + ".".repeat(10 - hBar) + "]";
    }

    public void updateLoop(long now) {
        if (tutorialStep == 1 && engine.isHacking) {
            tutorialProgress += 0.004;
            ui.updateTutorialUI(">>> 課程 1：基礎駭入\n按住 [滑鼠左鍵] 不放，來推進一般節點的駭入進度。", "進度: " + (int)(tutorialProgress * 100) + "%");
            if (tutorialProgress >= 1.0) {
                engine.isHacking = false;
                tutorialStep = 2;
                tutorialProgress = 0;
                tutorialHeat = 0;

                app.playSuccessSound();
                ui.shakeScreen();
                ui.playFlashEffect(Color.LIME, 500);

                ui.updateTutorialUI(">>> 課程 2：突破防火牆\n遇到防火牆時，連打 [空白鍵] 來擊破防禦！\n注意下方的熱量警告，快滿時必須停手。", getFirewallStr());
            }
        }

        if (tutorialStep == 2) {
            if (tutorialHeat > 0) {
                tutorialHeat = Math.max(0, tutorialHeat - 0.003); // 自動散熱
                ui.updateTutorialUI(ui.tutorialTextLabel.getText(), getFirewallStr());
            }
        }

        if (tutorialStep == 4) {
            if (!isDecryptFlashed && now > decryptFlashTime) {
                isDecryptFlashed = true;
                ui.updateTutorialUI(">>> 課程 4：記憶解密\n文字已隱藏，現在請憑記憶輸入破解！", "輸入: " + decryptInput + "_");
            }
        }
    }

    private void endTutorial() {
        app.p.darkCoins += 500;
        ui.updateShopUI();
        ui.tutorialLayer.setVisible(false);
        app.returnToMenu();
    }
}