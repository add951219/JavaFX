package com.example.project;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    // 核心狀態變數
    private boolean isHacking = false;
    private double progress = 0.0;
    private boolean isGameOver = false;

    // UI 元件
    private ProgressBar progressBar;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        // 最底層容器：使用 StackPane 方便未來疊加警告圖層
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0a0a0a;"); // 駭客終端機深色背景

        // UI 佈局：將文字與進度條垂直置中
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        // 終端機狀態文字
        statusLabel = new Label(">>> 終端機就緒。長按滑鼠左鍵開始下載機密資料 <<<");
        statusLabel.setTextFill(Color.LIME);
        statusLabel.setFont(Font.font("Consolas", 18)); // 使用類似寫程式的等寬字體

        // 下載進度條
        progressBar = new ProgressBar(0);
        progressBar.setPrefSize(400, 30);
        // 使用 CSS 將進度條變成科技感的螢光綠
        progressBar.setStyle("-fx-accent: lime; -fx-control-inner-background: #111111; -fx-box-border: lime;");

        vbox.getChildren().addAll(statusLabel, progressBar);
        root.getChildren().add(vbox);

        Scene scene = new Scene(root, 800, 600);

        // 攔截器：滑鼠按下 (長按開始下載)
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !isGameOver) {
                isHacking = true;
                statusLabel.setText(">>> 系統入侵中... 資料下載中 [Hold] <<<");
            }
        });

        // 攔截器：滑鼠放開 (暫停下載)
        scene.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !isGameOver) {
                isHacking = false;
                statusLabel.setText(">>> 下載暫停。請繼續長按左鍵 <<<");
            }
        });

        // 主迴圈引擎：每秒執行約 60 次
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isHacking && progress < 1.0 && !isGameOver) {
                    progress += 0.002; // 控制下載速度，數字越大越快
                    progressBar.setProgress(progress);

                    if (progress >= 1.0) {
                        isGameOver = true;
                        isHacking = false;
                        statusLabel.setText(">>> 任務完成！機密資料已全數下載 <<<");
                        statusLabel.setTextFill(Color.CYAN); // 完成後變色
                    }
                }
            }
        };
        gameLoop.start();

        stage.setTitle("駭客入侵：防火牆攻防戰 (System Hack)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}