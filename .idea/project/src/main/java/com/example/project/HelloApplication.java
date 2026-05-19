package com.example.project;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Random;

public class HelloApplication extends Application {

    private boolean isHacking = false;
    private double progress = 0.0;
    private boolean isGameOver = false;

    // 事件狀態變數
    private boolean isAdminTracking = false;
    private boolean isFirewallFight = false;
    private long lastEventTime = 0;

    // 防火牆拔河進度 (0.0 被阻斷, 1.0 成功癱瘓)
    private double firewallProgress = 0.5;

    private final Random random = new Random();

    // UI 元件
    private Label progressDisplay;
    private Label matrixBg;
    private Label statusLabel;

    // 警報圖層
    private StackPane adminLayer;
    private StackPane firewallLayer;
    private Label firewallBarDisplay;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        // 1. 亂碼背景層
        matrixBg = new Label();
        matrixBg.setTextFill(Color.rgb(0, 100, 0, 0.3));
        matrixBg.setFont(Font.font("Consolas", 12));
        matrixBg.setAlignment(Pos.TOP_LEFT);
        matrixBg.setMaxWidth(Double.MAX_VALUE);

        // 2. 主 UI 佈局
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        statusLabel = new Label(">>> 終端機就緒。長按滑鼠左鍵開始下載 <<<");
        statusLabel.setTextFill(Color.LIME);
        statusLabel.setFont(Font.font("Consolas", 20));

        progressDisplay = new Label("[....................] 0%");
        progressDisplay.setTextFill(Color.LIME);
        progressDisplay.setFont(Font.font("Consolas", 30));

        vbox.getChildren().addAll(statusLabel, progressDisplay);

        // 3. 管理員巡邏圖層 (紅光)
        adminLayer = new StackPane();
        adminLayer.setStyle("-fx-background-color: rgba(255, 0, 0, 0.4);");
        Label adminText = new Label("⚠ ADMIN TRACKING ⚠\n立刻停止下載！");
        adminText.setTextFill(Color.WHITE);
        adminText.setFont(Font.font("Consolas", FontWeight.BOLD, 50));
        adminText.setAlignment(Pos.CENTER);
        adminLayer.getChildren().add(adminText);
        adminLayer.setVisible(false);

        // 4. 防火牆圖層 (藍光)
        firewallLayer = new StackPane();
        firewallLayer.setStyle("-fx-background-color: rgba(0, 50, 150, 0.6);"); // 半透明藍
        VBox firewallBox = new VBox(20);
        firewallBox.setAlignment(Pos.CENTER);
        Label firewallTitle = new Label("--- FIREWALL DETECTED ---\n狂點右鍵發送垃圾封包癱瘓它！");
        firewallTitle.setTextFill(Color.CYAN);
        firewallTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        firewallTitle.setAlignment(Pos.CENTER);

        firewallBarDisplay = new Label("[||||||||||..........]");
        firewallBarDisplay.setTextFill(Color.WHITE);
        firewallBarDisplay.setFont(Font.font("Consolas", 40));

        firewallBox.getChildren().addAll(firewallTitle, firewallBarDisplay);
        firewallLayer.getChildren().add(firewallBox);
        firewallLayer.setVisible(false);

        // 將所有圖層疊起來
        root.getChildren().addAll(matrixBg, vbox, adminLayer, firewallLayer);

        Scene scene = new Scene(root, 800, 600);

        // 攔截器：滑鼠點擊
        scene.setOnMousePressed(e -> {
            if (isGameOver) return;

            // 左鍵：正常下載
            if (e.getButton() == MouseButton.PRIMARY) {
                isHacking = true;
            }
            // 右鍵：對抗防火牆
            else if (e.getButton() == MouseButton.SECONDARY && isFirewallFight) {
                firewallProgress += 0.05; // 每次點擊增加 5% 進度
                updateFirewallUI();
            }
        });

        // 攔截器：滑鼠放開
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !isGameOver) {
                isHacking = false;
                if (!isAdminTracking && !isFirewallFight) {
                    statusLabel.setText(">>> 下載暫停。請繼續長按左鍵 <<<");
                }
            }
        });

        // 主迴圈引擎
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastBgUpdate = 0;

            @Override
            public void handle(long now) {
                if (isGameOver) return;
                if (lastEventTime == 0) lastEventTime = now;

                // 更新背景亂碼
                if (now - lastBgUpdate > 100_000_000) {
                    matrixBg.setText(generateRandomCode());
                    lastBgUpdate = now;
                }

                // 隨機觸發突發事件
                if (!isAdminTracking && !isFirewallFight) {
                    if (now - lastEventTime > 3_000_000_000L + random.nextInt(2_000_000_000)) {
                        double eventRoll = random.nextDouble();
                        if (eventRoll < 0.25) {
                            // 25% 機率觸發巡邏 (紅光)
                            isAdminTracking = true;
                            adminLayer.setVisible(true);
                            lastEventTime = now;
                        } else if (eventRoll < 0.5) {
                            // 25% 機率觸發防火牆 (藍光)
                            isFirewallFight = true;
                            firewallProgress = 0.5; // 重置拔河進度到中間
                            firewallLayer.setVisible(true);
                            updateFirewallUI();
                            lastEventTime = now;
                        } else {
                            lastEventTime = now;
                        }
                    }
                }
                // 處理紅光巡邏
                else if (isAdminTracking) {
                    if (isHacking) {
                        triggerGameOver(">>> FAILED <<<\nIP 已被鎖定，系統斷線。");
                        return;
                    }
                    if (now - lastEventTime > 2_000_000_000L) {
                        isAdminTracking = false;
                        adminLayer.setVisible(false);
                        lastEventTime = now;
                        statusLabel.setText(">>> 巡邏結束，安全。請繼續下載。 <<<");
                    }
                }
                // 處理藍光防火牆拔河
                else if (isFirewallFight) {
                    firewallProgress -= 0.003; // 防火牆自動扣除你的進度 (系統阻力)
                    updateFirewallUI();

                    if (firewallProgress <= 0) {
                        triggerGameOver(">>> FAILED <<<\n防火牆已將您的連線阻斷。");
                    } else if (firewallProgress >= 1.0) {
                        isFirewallFight = false;
                        firewallLayer.setVisible(false);
                        lastEventTime = now;
                        statusLabel.setText(">>> 防火牆已癱瘓！請繼續下載。 <<<");
                    }
                }

                // 更新正常下載進度
                if (isHacking && progress < 1.0 && !isAdminTracking && !isFirewallFight) {
                    progress += 0.002;
                    updateASCIIProgress();
                }
            }
        };
        gameLoop.start();

        stage.setScene(scene);
        stage.setTitle("駭客入侵：防火牆攻防戰 (System Hack)");
        stage.show();
    }

    private void updateASCIIProgress() {
        int bars = (int) (progress * 20);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) sb.append(i < bars ? "|" : ".");
        sb.append("] ").append((int)(progress * 100)).append("%");

        progressDisplay.setText(sb.toString());
        statusLabel.setText(">>> 系統入侵中... 資料下載中 [Hold] <<<");

        if (progress >= 1.0) {
            isGameOver = true;
            isHacking = false;
            statusLabel.setText(">>> 任務完成！機密資料已全數下載 <<<");
            statusLabel.setTextFill(Color.CYAN);
            adminLayer.setVisible(false);
            firewallLayer.setVisible(false);
        }
    }

    // 更新防火牆拔河的 ASCII 介面
    private void updateFirewallUI() {
        int bars = (int) (firewallProgress * 20);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) sb.append(i < bars ? "|" : ".");
        sb.append("]");
        firewallBarDisplay.setText(sb.toString());
    }

    private void triggerGameOver(String reason) {
        isGameOver = true;
        isHacking = false;

        adminLayer.setStyle("-fx-background-color: rgba(139, 0, 0, 0.9);");
        ((Label)adminLayer.getChildren().get(0)).setText(reason);
        ((Label)adminLayer.getChildren().get(0)).setTextFill(Color.RED);

        firewallLayer.setVisible(false);
        adminLayer.setVisible(true);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 1500; i++) sb.append((char)(random.nextInt(94) + 33));
        return sb.toString();
    }

    public static void main(String[] args) { launch(); }
}