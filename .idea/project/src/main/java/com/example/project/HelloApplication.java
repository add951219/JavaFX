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
import javafx.stage.Stage;

import java.util.Random;

public class HelloApplication extends Application {

    private boolean isHacking = false;
    private double progress = 0.0;
    private boolean isGameOver = false;
    private final Random random = new Random();

    // 改用 Label 來做 ASCII 介面與亂碼背景
    private Label progressDisplay;
    private Label matrixBg;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        // 亂碼背景層
        matrixBg = new Label();
        matrixBg.setTextFill(Color.rgb(0, 100, 0, 0.3)); // 半透明綠色
        matrixBg.setFont(Font.font("Consolas", 12));
        matrixBg.setAlignment(Pos.TOP_LEFT);
        matrixBg.setMaxWidth(Double.MAX_VALUE);

        // UI 佈局
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        statusLabel = new Label(">>> 終端機就緒。長按滑鼠左鍵開始下載機密資料 <<<");
        statusLabel.setTextFill(Color.LIME);
        statusLabel.setFont(Font.font("Consolas", 20));

        // ASCII 進度條
        progressDisplay = new Label("[....................] 0%");
        progressDisplay.setTextFill(Color.LIME);
        progressDisplay.setFont(Font.font("Consolas", 30));

        vbox.getChildren().addAll(statusLabel, progressDisplay);
        root.getChildren().addAll(matrixBg, vbox); // 把背景和 UI 疊起來

        Scene scene = new Scene(root, 800, 600);

        // 攔截器：滑鼠按下與放開
        scene.setOnMousePressed(e -> {
            if(e.getButton() == MouseButton.PRIMARY && !isGameOver) isHacking = true;
        });
        scene.setOnMouseReleased(e -> {
            if(e.getButton() == MouseButton.PRIMARY && !isGameOver) {
                isHacking = false;
                statusLabel.setText(">>> 下載暫停。請繼續長按左鍵 <<<");
            }
        });

        // 主迴圈引擎
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                // 1. 每 0.1 秒更新一次亂碼背景
                if (now - lastUpdate > 100_000_000) {
                    matrixBg.setText(generateRandomCode());
                    lastUpdate = now;
                }

                // 2. 更新下載進度邏輯
                if (isHacking && progress < 1.0 && !isGameOver) {
                    progress += 0.003; // 下載速度
                    updateASCIIProgress();
                }
            }
        };
        gameLoop.start();

        stage.setScene(scene);
        stage.setTitle("駭客入侵：防火牆攻防戰 (System Hack)");
        stage.show();
    }

    // 專門處理 ASCII 進度條動畫的方法
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
        }
    }

    // 產生隨機亂碼的方法
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 1500; i++) {
            sb.append((char)(random.nextInt(94) + 33)); // 隨機產生可見的 ASCII 字符
        }
        return sb.toString();
    }

    public static void main(String[] args) { launch(); }
}