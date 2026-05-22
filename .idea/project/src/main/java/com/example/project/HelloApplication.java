package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HelloApplication extends Application {

    // 匯入剛剛拆分的兩大功能模組
    private final PlayerStats p = new PlayerStats();
    private final HackEngine engine = new HackEngine();

    // UI 元件
    private StackPane root;
    private Label progressDisplay, statusLabel, uiBorder, matrixBg;
    private StackPane menuLayer, introLayer, gameLayer, pauseLayer, firewallLayer, interceptLayer, shopLayer, gameOverLayer;
    private Label coinDisplay, comboDisplay, highScoreDisplay, firewallBarDisplay, interceptTargetDisplay, interceptTimeDisplay;
    private Label gameOverReasonLabel, gameOverStatsLabel;
    private Button honeypotBtn;
    private String currentTargetText = "";
    private double currentVolume = 0.5;

    @Override
    public void start(Stage stage) {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0b0c10;");

        buildVisuals();

        Scene scene = new Scene(root, 800, 600);
        setupInputHandlers(scene);
        startGameLoop();

        stage.setScene(scene);
        stage.setTitle("Neon Breach - Modularized Edition");
        stage.show();
        root.requestFocus();
    }

    private void buildVisuals() {
        matrixBg = new Label();
        matrixBg.setTextFill(Color.rgb(0, 255, 204, 0.15));
        matrixBg.setFont(Font.font("Consolas", 12));
        matrixBg.setAlignment(Pos.TOP_LEFT);

        Label crtOverlay = new Label();
        crtOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        crtOverlay.setStyle("-fx-background-color: repeating-linear-gradient(0deg, rgba(0,0,0,0) 0px, rgba(0,0,0,0) 1px, rgba(0,255,0,0.03) 2px, rgba(0,255,0,0.03) 3px);");
        crtOverlay.setMouseTransparent(true);

        menuLayer = new StackPane();
        menuLayer.setStyle("-fx-background-color: rgba(11, 12, 16, 0.95);");
        VBox menuBox = new VBox(30);
        menuBox.setAlignment(Pos.CENTER);
        Label title = new Label("NEON BREACH");
        title.setTextFill(Color.CYAN);
        title.setFont(Font.font("Impact", 70));

        highScoreDisplay = new Label("HIGHEST LAYER: " + p.highScore);
        highScoreDisplay.setTextFill(Color.LIME);
        highScoreDisplay.setFont(Font.font("Consolas", 20));

        Button btnStart = createStyledButton(">>> INITIATE HACK <<<");
        btnStart.setOnAction(e -> startIntroSequence());

        menuBox.getChildren().addAll(title, highScoreDisplay, btnStart);
        menuLayer.getChildren().add(menuBox);

        introLayer = new StackPane();
        introLayer.setStyle("-fx-background-color: black;");
        Label introText = new Label();
        introText.setTextFill(Color.LIME);
        introText.setFont(Font.font("Consolas", 24));
        introLayer.getChildren().add(introText);
        introLayer.setVisible(false);

        uiBorder = new Label("╔════════════════════════════════════════════╗\n║                                            ║\n║                                            ║\n║                                            ║\n╚════════════════════════════════════════════╝");
        uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5));
        uiBorder.setFont(Font.font("Consolas", 20));
        uiBorder.setAlignment(Pos.CENTER);

        VBox gameBox = new VBox(10);
        gameBox.setAlignment(Pos.CENTER);
        statusLabel = new Label("");
        statusLabel.setTextFill(Color.CYAN);
        statusLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        progressDisplay = new Label("LEVEL 1 [....☼....☼....☼....] 0%");
        progressDisplay.setTextFill(Color.LIME);
        progressDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 28));

        comboDisplay = new Label("COMBO: x1.0");
        comboDisplay.setTextFill(Color.YELLOW);
        comboDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 20));

        gameBox.getChildren().addAll(statusLabel, comboDisplay, progressDisplay);
        gameLayer = new StackPane(uiBorder, gameBox);
        gameLayer.setVisible(false);

        honeypotBtn = new Button("⚠ [NODE VULNERABILITY] CLICK FOR 300 ¢");
        honeypotBtn.setStyle("-fx-background-color: #330000; -fx-text-fill: #FF3333; -fx-border-color: red; -fx-cursor: hand;");
        StackPane.setAlignment(honeypotBtn, Pos.TOP_RIGHT);
        honeypotBtn.setVisible(false);
        honeypotBtn.setOnAction(e -> handleHoneypotTrap());
        gameLayer.getChildren().add(honeypotBtn);

        buildEventLayers();
        buildShopLayer();

        pauseLayer = new StackPane();
        pauseLayer.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        VBox pauseBox = new VBox(20);
        pauseBox.setAlignment(Pos.CENTER);
        Label pauseTitle = new Label("SYSTEM PAUSED");
        pauseTitle.setTextFill(Color.WHITE);
        pauseTitle.setFont(Font.font("Consolas", 40));
        Button btnResume = createStyledButton("RESUME");
        btnResume.setOnAction(e -> { engine.currentState = HackEngine.GameState.PLAYING; pauseLayer.setVisible(false); });
        Button btnMenuPause = createStyledButton("ABORT TO MENU");
        btnMenuPause.setOnAction(e -> returnToMenu());
        pauseBox.getChildren().addAll(pauseTitle, btnResume, btnMenuPause);
        pauseLayer.getChildren().add(pauseBox);
        pauseLayer.setVisible(false);

        gameOverLayer = new StackPane();
        gameOverLayer.setStyle("-fx-background-color: rgba(139, 0, 0, 0.95);");
        VBox overBox = new VBox(20);
        overBox.setAlignment(Pos.CENTER);

        gameOverReasonLabel = new Label("");
        gameOverReasonLabel.setTextFill(Color.RED);
        gameOverReasonLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 45));

        gameOverStatsLabel = new Label("");
        gameOverStatsLabel.setTextFill(Color.WHITE);
        gameOverStatsLabel.setFont(Font.font("Consolas", 20));

        Button btnRestart = createStyledButton("SYSTEM REBOOT");
        btnRestart.setOnAction(e -> resetGame());

        Button btnMenuDead = createStyledButton("RETURN TO MENU");
        btnMenuDead.setOnAction(e -> returnToMenu());

        overBox.getChildren().addAll(gameOverReasonLabel, gameOverStatsLabel, btnRestart, btnMenuDead);
        gameOverLayer.getChildren().add(overBox);
        gameOverLayer.setVisible(false);

        root.getChildren().addAll(matrixBg, gameLayer, crtOverlay, firewallLayer, interceptLayer, shopLayer, introLayer, gameOverLayer, pauseLayer, menuLayer);
    }

    private void buildEventLayers() {
        firewallLayer = new StackPane();
        firewallLayer.setStyle("-fx-background-color: rgba(0, 80, 255, 0.8);");
        VBox fwBox = new VBox(20);
        fwBox.setAlignment(Pos.CENTER);
        Label fwTitle = new Label("--- FIREWALL INTERCEPT ---");
        fwTitle.setTextFill(Color.CYAN);
        fwTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 35));
        firewallBarDisplay = new Label("[||||||||||..........]");
        firewallBarDisplay.setTextFill(Color.WHITE);
        firewallBarDisplay.setFont(Font.font("Consolas", 45));
        fwBox.getChildren().addAll(fwTitle, new Label("Mash [SPACEBAR]!"), firewallBarDisplay);
        firewallLayer.getChildren().add(fwBox);
        firewallLayer.setVisible(false);

        interceptLayer = new StackPane();
        interceptLayer.setStyle("-fx-background-color: rgba(100, 0, 150, 0.8);");
        VBox intBox = new VBox(20);
        intBox.setAlignment(Pos.CENTER);
        Label intTitle = new Label("!!! PACKET INTERCEPTED !!!");
        intTitle.setTextFill(Color.ORANGE);
        intTitle.setFont(Font.font("Consolas", 35));
        interceptTargetDisplay = new Label("W A S D");
        interceptTargetDisplay.setTextFill(Color.YELLOW);
        interceptTargetDisplay.setFont(Font.font("Consolas", 45));
        interceptTimeDisplay = new Label("Time left: 3.0s");
        interceptTimeDisplay.setTextFill(Color.WHITE);
        intBox.getChildren().addAll(intTitle, interceptTargetDisplay, interceptTimeDisplay);
        interceptLayer.getChildren().add(intBox);
        interceptLayer.setVisible(false);
    }

    private void buildShopLayer() {
        shopLayer = new StackPane();
        shopLayer.setStyle("-fx-background-color: #050505;");
        VBox shopBox = new VBox(15);
        shopBox.setAlignment(Pos.CENTER);
        Label shopTitle = new Label("--- BLACK MARKET ---");
        shopTitle.setTextFill(Color.LIME);
        shopTitle.setFont(Font.font("Consolas", 40));

        coinDisplay = new Label("DarkCoins: 0 ¢");
        coinDisplay.setTextFill(Color.GOLD);
        coinDisplay.setFont(Font.font("Consolas", 25));

        Button btn1 = createShopButton("重磅封包 (空白鍵威力)", 100);
        btn1.setOnAction(e -> { if(p.buy(100)) p.upgClick++; btn1.setText("重磅封包 (Lv."+p.upgClick+")"); coinDisplay.setText("DarkCoins: "+p.darkCoins+" ¢"); });

        Button btn2 = createShopButton("注入加速 (抵抗退速)", 150);
        btn2.setOnAction(e -> { if(p.buy(150)) p.upgSpeed++; btn2.setText("注入加速 (Lv."+p.upgSpeed+")"); coinDisplay.setText("DarkCoins: "+p.darkCoins+" ¢"); });

        Button btn3 = createShopButton("ICE 護盾 (抵擋一次失誤)", 300);
        btn3.setOnAction(e -> { if(p.buy(300)) p.upgShield++; btn3.setText("ICE 護盾 (剩餘:"+p.upgShield+")"); coinDisplay.setText("DarkCoins: "+p.darkCoins+" ¢"); });

        Button btn4 = createShopButton("幽靈 Bot (自動推進)", 500);
        btn4.setOnAction(e -> { if(p.buy(500)) p.upgBot++; btn4.setText("幽靈 Bot (Lv."+p.upgBot+")"); coinDisplay.setText("DarkCoins: "+p.darkCoins+" ¢"); });

        Button btn5 = createShopButton("背景挖礦機 (被動收入)", 400);
        btn5.setOnAction(e -> { if(p.buy(400)) p.upgMine++; btn5.setText("背景挖礦機 (Lv."+p.upgMine+")"); coinDisplay.setText("DarkCoins: "+p.darkCoins+" ¢"); });

        Button btnNext = createStyledButton(">>> NEXT LAYER <<<");
        btnNext.setOnAction(e -> {
            engine.currentState = HackEngine.GameState.PLAYING;
            shopLayer.setVisible(false);
            gameLayer.setVisible(true);
        });

        shopBox.getChildren().addAll(shopTitle, coinDisplay, btn1, btn2, btn3, btn4, btn5, btnNext);
        shopLayer.getChildren().add(shopBox);
        shopLayer.setVisible(false);
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: black; -fx-text-fill: cyan; -fx-border-color: cyan; -fx-font-family: 'Consolas'; -fx-font-size: 18px; -fx-cursor: hand;");
        return btn;
    }

    private Button createShopButton(String name, int cost) {
        Button btn = new Button(name + " [Cost: " + cost + "¢]");
        btn.setStyle("-fx-background-color: #111; -fx-text-fill: lime; -fx-border-color: lime; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-cursor: hand;");
        return btn;
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnMousePressed(e -> {
            if (engine.currentState == HackEngine.GameState.PLAYING && e.getButton() == MouseButton.PRIMARY && !engine.isFirewallFight && !engine.isInterceptFight) {
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
                    pauseLayer.setVisible(true);
                } else if (engine.currentState == HackEngine.GameState.PAUSED) {
                    engine.currentState = HackEngine.GameState.PLAYING;
                    pauseLayer.setVisible(false);
                }
            }

            if (engine.currentState != HackEngine.GameState.PLAYING) return;

            if (e.getCode() == KeyCode.SPACE && engine.isFirewallFight) {
                engine.firewallProgress += 0.05 + (p.upgClick * 0.015);
                updateFirewallUI();
                e.consume();
            }
            if (engine.isInterceptFight) {
                String inputKey = e.getCode().toString();
                String expectedKey = engine.targetSequence.substring(engine.sequenceIndex, engine.sequenceIndex + 1);
                if (inputKey.equals(expectedKey)) {
                    engine.sequenceIndex++;
                    updateInterceptUI();
                    if (engine.sequenceIndex >= engine.targetSequence.length()) {
                        engine.isInterceptFight = false;
                        interceptLayer.setVisible(false);
                        typeWriterUpdate(">>> PACKET REROUTED. NODE SECURED.");
                        engine.currentSegment++;
                    }
                }
                e.consume();
            }
        });
    }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (engine.currentState != HackEngine.GameState.PLAYING) return;

            long now = System.nanoTime();
            if (engine.random.nextInt(10) == 0) matrixBg.setText(engine.generateRandomCode());

            if (p.upgMine > 0 && engine.random.nextDouble() < 0.01) p.darkCoins += p.upgMine;

            if (!engine.isFirewallFight && !engine.isInterceptFight && engine.isHacking && !engine.isHoneypotActive && engine.random.nextDouble() < 0.002) {
                engine.isHoneypotActive = true;
                honeypotBtn.setVisible(true);
                engine.honeypotExpireTime = now + 1_500_000_000L + engine.random.nextInt(1_000_000_000);
            }
            if (engine.isHoneypotActive && now > engine.honeypotExpireTime) {
                engine.isHoneypotActive = false;
                honeypotBtn.setVisible(false);
            }

            if (engine.isHacking && !engine.isFirewallFight && !engine.isInterceptFight) {
                engine.comboFrames++;
                engine.comboMultiplier = Math.min(3.0, 1.0 + (engine.comboFrames / 180.0));
            } else {
                engine.comboFrames = 0;
                engine.comboMultiplier = 1.0;
            }
            comboDisplay.setText(String.format("COMBO: x%.1f", engine.comboMultiplier));

            if (engine.isFirewallFight) {
                engine.firewallProgress -= (0.003 + (p.currentLevel * 0.0008));
                updateFirewallUI();
                if (engine.firewallProgress <= 0) triggerGameOver(">>> CONNECTION BLOCKED <<<");
                else if (engine.firewallProgress >= 1.0) {
                    engine.isFirewallFight = false;
                    firewallLayer.setVisible(false);
                    typeWriterUpdate(">>> FIREWALL NEUTRALIZED.");
                    engine.currentSegment++;
                }
            } else if (engine.isInterceptFight) {
                double timeLeft = (engine.interceptDeadline - now) / 1_000_000_000.0;
                interceptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft)));
                if (now > engine.interceptDeadline) handleInterceptFailure();
            } else {
                double securedProgress = engine.currentSegment * 0.25;
                double targetCheckpoint = (engine.currentSegment + 1) * 0.25;

                engine.progress += (p.upgBot * 0.0005);

                if (engine.isHacking) {
                    engine.progress += 0.0022 + (p.upgSpeed * 0.0006);
                    if (engine.progress >= targetCheckpoint) {
                        engine.progress = targetCheckpoint;
                        engine.isHacking = false;
                        if (engine.currentSegment < engine.TOTAL_SEGMENTS - 1) triggerCheckpointEvent();
                        else playLevelClearExplosion();
                    }
                } else {
                    double bounceDecay = 0.0010 + (p.currentLevel * 0.0004);
                    engine.progress -= bounceDecay;
                    if (engine.progress < securedProgress) engine.progress = securedProgress;
                }
                updateASCIIProgress();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void triggerCheckpointEvent() {
        shakeScreen();
        if (engine.random.nextBoolean()) {
            engine.isFirewallFight = true;
            engine.firewallProgress = 0.5;
            firewallLayer.setVisible(true);
            updateFirewallUI();
        } else {
            engine.isInterceptFight = true;
            engine.sequenceIndex = 0;
            int len = 4 + (p.currentLevel / 2);
            String[] pool = {"W", "A", "S", "D"};
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<len; i++) sb.append(pool[engine.random.nextInt(pool.length)]);
            engine.targetSequence = sb.toString();
            updateInterceptUI();
            engine.interceptDeadline = System.nanoTime() + (long)(Math.max(1.5, 4.5 - p.currentLevel*0.3) * 1_000_000_000L);
            interceptLayer.setVisible(true);
        }
    }

    private void handleHoneypotTrap() {
        engine.isHoneypotActive = false;
        honeypotBtn.setVisible(false);
        if (p.upgShield > 0) {
            p.upgShield--;
            typeWriterUpdate(">>> HONEYPOT TRIGGERED! ICE SHIELD DEPLOYED. (Remaining: "+p.upgShield+")");
        } else {
            p.darkCoins = Math.max(0, p.darkCoins - 60);
            engine.progress = Math.max(engine.currentSegment * 0.25, engine.progress - 0.08);
            shakeScreen();
            typeWriterUpdate(">>> HONEYPOT TRIGGERED! RESOURCES DRAINED!");
        }
    }

    private void handleInterceptFailure() {
        engine.isInterceptFight = false;
        interceptLayer.setVisible(false);
        if (p.upgShield > 0) {
            p.upgShield--;
            typeWriterUpdate(">>> PACKET LOST! ICE SHIELD ABSORBED IMPACT.");
        } else {
            engine.progress = engine.currentSegment * 0.25;
            shakeScreen();
            typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED.");
        }
    }

    private void playLevelClearExplosion() {
        engine.currentState = HackEngine.GameState.PAUSED;
        engine.isHacking = false;
        Timeline explosion = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            statusLabel.setText(engine.generateRandomCode().substring(0, 40));
            uiBorder.setTextFill(Color.color(engine.random.nextDouble(), engine.random.nextDouble(), engine.random.nextDouble()));
        }));
        explosion.setCycleCount(15);
        explosion.setOnFinished(e -> triggerLevelClear());
        explosion.play();
    }

    private void triggerLevelClear() {
        int earned = (int)((p.currentLevel * 100) * engine.comboMultiplier);
        p.darkCoins += earned;
        p.currentLevel++;
        engine.progress = 0.0;
        engine.currentSegment = 0;

        if (p.currentLevel > p.highScore) p.highScore = p.currentLevel;

        coinDisplay.setText("DarkCoins: " + p.darkCoins + " ¢");
        uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5));
        engine.currentState = HackEngine.GameState.SHOP;
        gameLayer.setVisible(false);
        shopLayer.setVisible(true);
    }

    private void triggerGameOver(String reason) {
        engine.currentState = HackEngine.GameState.GAMEOVER;
        shakeScreen();
        gameOverReasonLabel.setText(reason);
        gameOverStatsLabel.setText("REACHED LAYER: " + p.currentLevel);
        gameOverLayer.setVisible(true);
        firewallLayer.setVisible(false);
        interceptLayer.setVisible(false);
        shopLayer.setVisible(false);
    }

    private void shakeScreen() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), root);
        tt.setFromX(0f); tt.setByX(10f); tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.playFromStart();
    }

    private void startIntroSequence() {
        engine.currentState = HackEngine.GameState.INTRO;
        menuLayer.setVisible(false);
        introLayer.setVisible(true);
        Label text = (Label) introLayer.getChildren().get(0);
        String[] lines = {"WAKING UP SYSTEM...", "ESTABLISHING SECURE CONNECTION...", "BYPASSING MAINFRAME FIREWALLS...", "ACCESS GRANTED."};
        Timeline introTimeline = new Timeline();
        for (int i=0; i<lines.length; i++) {
            final int index = i;
            introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1 * (i+1)), e -> text.setText(lines[index])));
        }
        introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(lines.length + 1), e -> {
            introLayer.setVisible(false);
            gameLayer.setVisible(true);
            engine.currentState = HackEngine.GameState.PLAYING;
        }));
        introTimeline.play();
    }

    private void resetGame() {
        p.reset();
        engine.resetEvents();
        firewallLayer.setVisible(false);
        interceptLayer.setVisible(false);
        honeypotBtn.setVisible(false);
        gameOverLayer.setVisible(false);
        gameLayer.setVisible(true);
        engine.currentState = HackEngine.GameState.PLAYING;
    }

    private void returnToMenu() {
        pauseLayer.setVisible(false);
        gameLayer.setVisible(false);
        shopLayer.setVisible(false);
        gameOverLayer.setVisible(false);
        menuLayer.setVisible(true);
        highScoreDisplay.setText("HIGHEST LAYER: " + p.highScore);
        resetGame();
        engine.currentState = HackEngine.GameState.MAIN_MENU;
    }

    private void updateFirewallUI() { firewallBarDisplay.setText("[" + "|".repeat((int)(engine.firewallProgress*20)) + ".".repeat(20-(int)(engine.firewallProgress*20)) + "]"); }
    private void updateInterceptUI() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<engine.targetSequence.length(); i++) sb.append(i<engine.sequenceIndex ? "- " : engine.targetSequence.charAt(i)+" ");
        interceptTargetDisplay.setText(sb.toString().trim());
    }
    private void updateASCIIProgress() {
        StringBuilder sb = new StringBuilder("[");
        for (int i=1; i<=20; i++) {
            if (i <= (engine.progress*20)) sb.append("|");
            else if (i%5==0 && i!=20) sb.append("☼");
            else sb.append(".");
        }
        sb.append("] ").append((int)(engine.progress*100)).append("%");
        progressDisplay.setText("LEVEL " + p.currentLevel + " " + sb.toString());
        if (!engine.isHacking && !engine.isFirewallFight) statusLabel.setText(">>> WARNING: LOSING PROGRESS... [RELEASED]");
        else if (engine.isHacking) statusLabel.setText(">>> INJECTING... BREACHING LAYER " + (engine.currentSegment+1));
    }
    private void typeWriterUpdate(String t) { currentTargetText = t; statusLabel.setText(t); }
    public static void main(String[] args) { launch(); }
}