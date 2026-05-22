package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HelloApplication extends Application {

    private final PlayerStats p = new PlayerStats();
    private final HackEngine engine = new HackEngine();

    private StackPane root;
    private Label progressDisplay, statusLabel, uiBorder, matrixBg;
    private StackPane menuLayer, introLayer, gameLayer, pauseLayer, firewallLayer, interceptLayer, shopLayer, gameOverLayer, routeLayer;
    private Label coinDisplay, comboDisplay, highScoreDisplay, firewallBarDisplay, interceptTargetDisplay, interceptTimeDisplay;
    private Label gameOverReasonLabel, gameOverStatsLabel, skillDisplay;
    private Button honeypotBtn;
    private String currentTargetText = "";

    @Override
    public void start(Stage stage) {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0b0c10;");

        buildVisuals();

        Scene scene = new Scene(root, 800, 600);
        setupInputHandlers(scene);
        startGameLoop();

        stage.setScene(scene);
        stage.setTitle("Neon Breach - Roguelite Boss Edition");
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

        // 主選單
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

        // 遊戲主 UI
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

        // 技能列顯示 (左下角)
        skillDisplay = new Label("[1] EMP: 0   [2] SLOW: 0");
        skillDisplay.setTextFill(Color.WHITE);
        skillDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        StackPane.setAlignment(skillDisplay, Pos.BOTTOM_LEFT);
        gameLayer.getChildren().add(skillDisplay);

        honeypotBtn = new Button("⚠ [NODE VULNERABILITY] CLICK FOR 300 ¢");
        honeypotBtn.setStyle("-fx-background-color: #330000; -fx-text-fill: #FF3333; -fx-border-color: red; -fx-cursor: hand;");
        StackPane.setAlignment(honeypotBtn, Pos.TOP_RIGHT);
        honeypotBtn.setVisible(false);
        honeypotBtn.setOnAction(e -> handleHoneypotTrap());
        gameLayer.getChildren().add(honeypotBtn);

        buildEventLayers();
        buildRouteLayer(); // 新增的路線層
        buildShopLayer();

        pauseLayer = new StackPane();
        pauseLayer.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        VBox pauseBox = new VBox(20);
        pauseBox.setAlignment(Pos.CENTER);
        Label pauseTitle = new Label("SYSTEM PAUSED");
        pauseTitle.setTextFill(Color.WHITE);
        pauseTitle.setFont(Font.font("Consolas", 40));
        Button btnResume = createStyledButton("RESUME");
        btnResume.setOnAction(e -> {
            engine.currentState = HackEngine.GameState.PLAYING;
            pauseLayer.setVisible(false);
        });
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

        root.getChildren().addAll(matrixBg, gameLayer, crtOverlay, firewallLayer, interceptLayer, routeLayer, shopLayer, introLayer, gameOverLayer, pauseLayer, menuLayer);
    }

    private void buildEventLayers() {
        // 為了容納 Boss 戰同時出現，把防火牆偏上、攔截偏下
        firewallLayer = new StackPane();
        firewallLayer.setStyle("-fx-background-color: rgba(0, 80, 255, 0.6);"); // 透明度調低一點
        VBox fwBox = new VBox(10);
        fwBox.setAlignment(Pos.CENTER);
        Label fwTitle = new Label("- FIREWALL -");
        fwTitle.setTextFill(Color.CYAN);
        fwTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        firewallBarDisplay = new Label("[||||||||||..........]");
        firewallBarDisplay.setTextFill(Color.WHITE);
        firewallBarDisplay.setFont(Font.font("Consolas", 40));
        fwBox.getChildren().addAll(fwTitle, new Label("SPACEBAR!"), firewallBarDisplay);
        firewallLayer.getChildren().add(fwBox);
        StackPane.setAlignment(fwBox, Pos.TOP_CENTER);
        fwBox.setTranslateY(100);
        firewallLayer.setVisible(false);

        interceptLayer = new StackPane();
        interceptLayer.setStyle("-fx-background-color: rgba(100, 0, 150, 0.6);");
        VBox intBox = new VBox(10);
        intBox.setAlignment(Pos.CENTER);
        Label intTitle = new Label("! INTERCEPT !");
        intTitle.setTextFill(Color.ORANGE);
        intTitle.setFont(Font.font("Consolas", 30));
        interceptTargetDisplay = new Label("W A S D");
        interceptTargetDisplay.setTextFill(Color.YELLOW);
        interceptTargetDisplay.setFont(Font.font("Consolas", 40));
        interceptTimeDisplay = new Label("Time left: 3.0s");
        interceptTimeDisplay.setTextFill(Color.WHITE);
        intBox.getChildren().addAll(intTitle, interceptTargetDisplay, interceptTimeDisplay);
        interceptLayer.getChildren().add(intBox);
        StackPane.setAlignment(intBox, Pos.BOTTOM_CENTER);
        intBox.setTranslateY(-100);
        interceptLayer.setVisible(false);
    }

    private void buildRouteLayer() {
        routeLayer = new StackPane();
        routeLayer.setStyle("-fx-background-color: rgba(10, 30, 10, 0.9);");
        VBox routeBox = new VBox(20);
        routeBox.setAlignment(Pos.CENTER);
        Label title = new Label(">>> SELECT NEXT NODE <<<");
        title.setTextFill(Color.LIME);
        title.setFont(Font.font("Consolas", 35));

        HBox btnBox = new HBox(30);
        btnBox.setAlignment(Pos.CENTER);

        Button btnNormal = createStyledButton("廢棄學術伺服器\n(難度: 0.8x | 獎勵: 0.7x)");
        btnNormal.setOnAction(e -> {
            p.routeDiffMult = 0.8;
            p.routeRewardMult = 0.7;
            enterShop();
        });

        Button btnHard = createStyledButton("高風險金融節點\n(難度: 1.5x | 獎勵: 2.0x)");
        btnHard.setStyle("-fx-background-color: #300; -fx-text-fill: #f55; -fx-border-color: red; -fx-font-family: 'Consolas'; -fx-font-size: 16px;");
        btnHard.setOnAction(e -> {
            p.routeDiffMult = 1.5;
            p.routeRewardMult = 2.0;
            enterShop();
        });

        btnBox.getChildren().addAll(btnNormal, btnHard);
        routeBox.getChildren().addAll(title, btnBox);
        routeLayer.getChildren().add(routeBox);
        routeLayer.setVisible(false);
    }

    private void buildShopLayer() {
        shopLayer = new StackPane();
        shopLayer.setStyle("-fx-background-color: #050505;");
        VBox shopBox = new VBox(10);
        shopBox.setAlignment(Pos.CENTER);
        Label shopTitle = new Label("--- BLACK MARKET ---");
        shopTitle.setTextFill(Color.LIME);
        shopTitle.setFont(Font.font("Consolas", 40));

        coinDisplay = new Label("DarkCoins: 0 ¢");
        coinDisplay.setTextFill(Color.GOLD);
        coinDisplay.setFont(Font.font("Consolas", 25));

        Button btn1 = createShopButton("重磅封包 (Lv." + p.upgClick + ")", 100);
        btn1.setOnAction(e -> {
            if (p.buy(100)) p.upgClick++;
            btn1.setText("重磅封包 (Lv." + p.upgClick + ")");
            updateShopUI();
        });

        Button btn2 = createShopButton("注入加速 (Lv." + p.upgSpeed + ")", 150);
        btn2.setOnAction(e -> {
            if (p.buy(150)) p.upgSpeed++;
            btn2.setText("注入加速 (Lv." + p.upgSpeed + ")");
            updateShopUI();
        });

        Button btnEmp = createShopButton("EMP 脈衝彈 (炸 firewall)", 200);
        btnEmp.setOnAction(e -> {
            if (p.buy(200)) p.empCharges++;
            updateShopUI();
        });

        Button btnSlow = createShopButton("超頻沙漏 (緩速 WASD)", 250);
        btnSlow.setOnAction(e -> {
            if (p.buy(250)) p.slowCharges++;
            updateShopUI();
        });

        Button btnNext = createStyledButton(">>> INJECT PAYLOAD <<<");
        btnNext.setOnAction(e -> {
            engine.currentState = HackEngine.GameState.PLAYING;
            shopLayer.setVisible(false);
            gameLayer.setVisible(true);
            engine.resetEvents();
            checkBossLevel();
        });

        shopBox.getChildren().addAll(shopTitle, coinDisplay, btn1, btn2, btnEmp, btnSlow, btnNext);
        shopLayer.getChildren().add(shopBox);
        shopLayer.setVisible(false);
    }

    private void enterShop() {
        routeLayer.setVisible(false);
        updateShopUI();
        shopLayer.setVisible(true);
        engine.currentState = HackEngine.GameState.SHOP;
    }

    private void updateShopUI() {
        coinDisplay.setText("DarkCoins: " + p.darkCoins + " ¢");
        skillDisplay.setText("[1] EMP: " + p.empCharges + "   [2] SLOW: " + p.slowCharges);
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: black; -fx-text-fill: cyan; -fx-border-color: cyan; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;");
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

            // 技能施放
            if (e.getCode() == KeyCode.DIGIT1 && engine.useEMP(p)) {
                typeWriterUpdate(">>> EMP DEPLOYED! FIREWALL DAMAGED.");
                updateShopUI();
                updateFirewallUI();
            }
            if (e.getCode() == KeyCode.DIGIT2 && engine.useSlow(p)) {
                typeWriterUpdate(">>> OVERCLOCK ENGAGED! TIME SLOWED.");
                updateShopUI();
            }

            // 事件對抗
            if (e.getCode() == KeyCode.SPACE && engine.isFirewallFight) {
                engine.firewallProgress += 0.05 + (p.upgClick * 0.015);
                updateFirewallUI();
                e.consume();
            }
            if (engine.isInterceptFight) {
                String inputKey = e.getCode().toString();
                if (engine.sequenceIndex < engine.targetSequence.length()) {
                    String expectedKey = engine.targetSequence.substring(engine.sequenceIndex, engine.sequenceIndex + 1);
                    if (inputKey.equals(expectedKey)) {
                        engine.sequenceIndex++;
                        updateInterceptUI();
                        if (engine.sequenceIndex >= engine.targetSequence.length()) {
                            engine.isInterceptFight = false;
                            interceptLayer.setVisible(false);
                            typeWriterUpdate(">>> PACKET SECURED.");
                            if (!engine.isFirewallFight) engine.currentSegment++; // 若非Boss雙重戰，解除即推進
                        }
                    }
                }
            }
        });
    }

    private void checkBossLevel() {
        if (engine.isBossLevel(p.currentLevel)) {
            engine.totalSegments = 8; // Boss 關卡特別長
            uiBorder.setTextFill(Color.RED);
            statusLabel.setTextFill(Color.RED);
            typeWriterUpdate("⚠ WARNING: CORE OVERRIDE DETECTED. PREPARE FOR ASSAULT ⚠");
        } else {
            engine.totalSegments = 4;
            uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5));
            statusLabel.setTextFill(Color.CYAN);
        }
    }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (engine.currentState != HackEngine.GameState.PLAYING) return;

            long now = System.nanoTime();
            if (engine.random.nextInt(10) == 0) matrixBg.setText(engine.generateRandomCode());

            if (engine.isHacking && !engine.isFirewallFight && !engine.isInterceptFight) {
                engine.comboFrames++;
                engine.comboMultiplier = Math.min(3.0, 1.0 + (engine.comboFrames / 180.0));
            } else {
                engine.comboFrames = 0;
                engine.comboMultiplier = 1.0;
            }
            comboDisplay.setText(String.format("COMBO: x%.1f", engine.comboMultiplier));

            // 防火牆邏輯
            if (engine.isFirewallFight) {
                engine.firewallProgress -= (0.003 + (p.currentLevel * p.routeDiffMult * 0.0008));
                updateFirewallUI();
                if (engine.firewallProgress <= 0) triggerGameOver(">>> BLOCKED <<<");
                else if (engine.firewallProgress >= 1.0) {
                    engine.isFirewallFight = false;
                    firewallLayer.setVisible(false);
                    typeWriterUpdate(">>> FIREWALL SHATTERED.");
                    if (!engine.isInterceptFight) engine.currentSegment++;
                }
            }

            // 攔截邏輯 (兩者可同時存在)
            if (engine.isInterceptFight) {
                double timeLeft = (engine.interceptDeadline - now) / 1_000_000_000.0;
                interceptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft)));
                if (now > engine.interceptDeadline) handleInterceptFailure();
            }

            // 進度推進邏輯
            if (!engine.isFirewallFight && !engine.isInterceptFight) {
                double checkpointSize = 1.0 / engine.totalSegments;
                double securedProgress = engine.currentSegment * checkpointSize;
                double targetCheckpoint = (engine.currentSegment + 1) * checkpointSize;

                if (engine.isHacking) {
                    engine.progress += 0.0022 + (p.upgSpeed * 0.0006);
                    if (engine.progress >= targetCheckpoint) {
                        engine.progress = targetCheckpoint;
                        engine.isHacking = false;
                        if (engine.currentSegment < engine.totalSegments - 1) triggerCheckpointEvent();
                        else playLevelClearExplosion();
                    }
                } else {
                    engine.progress -= 0.0010 + (p.currentLevel * 0.0004);
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
        boolean isBoss = engine.isBossLevel(p.currentLevel);

        // Boss 戰：兩者同時觸發！
        if (isBoss || engine.random.nextBoolean()) {
            engine.isFirewallFight = true;
            engine.firewallProgress = 0.5;
            firewallLayer.setVisible(true);
            updateFirewallUI();
        }
        if (isBoss || !engine.isFirewallFight) {
            engine.isInterceptFight = true;
            engine.sequenceIndex = 0;
            int len = (int) (4 + (p.currentLevel * p.routeDiffMult / 2));
            String[] pool = {"W", "A", "S", "D"};
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) sb.append(pool[engine.random.nextInt(pool.length)]);
            engine.targetSequence = sb.toString();
            updateInterceptUI();
            double baseTime = Math.max(1.5, 5.0 - p.currentLevel * p.routeDiffMult * 0.3);
            if (isBoss) baseTime += 2.0; // Boss 戰稍微多給一點點時間
            engine.interceptDeadline = System.nanoTime() + (long) (baseTime * 1_000_000_000L);
            interceptLayer.setVisible(true);
        }
    }

    private void handleHoneypotTrap() { /* 簡化省略 */ }

    private void handleInterceptFailure() {
        engine.isInterceptFight = false;
        interceptLayer.setVisible(false);
        engine.progress = engine.currentSegment * (1.0 / engine.totalSegments);
        shakeScreen();
        typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED.");
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
        int baseReward = engine.isBossLevel(p.currentLevel) ? 500 : 100;
        int earned = (int) ((p.currentLevel * baseReward) * engine.comboMultiplier * p.routeRewardMult);
        p.darkCoins += earned;
        p.currentLevel++;
        engine.progress = 0.0;
        engine.currentSegment = 0;
        if (p.currentLevel > p.highScore) p.highScore = p.currentLevel;

        uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5));
        gameLayer.setVisible(false);

        // 切換到路線選擇
        engine.currentState = HackEngine.GameState.ROUTE_SELECT;
        routeLayer.setVisible(true);
    }

    private void triggerGameOver(String reason) {
        engine.currentState = HackEngine.GameState.GAMEOVER;
        shakeScreen();
        gameOverReasonLabel.setText(reason);
        gameOverStatsLabel.setText("REACHED LAYER: " + p.currentLevel);
        gameOverLayer.setVisible(true);
        firewallLayer.setVisible(false);
        interceptLayer.setVisible(false);
    }

    private void shakeScreen() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), root);
        tt.setFromX(0f);
        tt.setByX(10f);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.playFromStart();
    }

    private void startIntroSequence() {
        engine.currentState = HackEngine.GameState.INTRO;
        menuLayer.setVisible(false);
        introLayer.setVisible(true);
        Label text = (Label) introLayer.getChildren().get(0);
        String[] lines = {"WAKING UP SYSTEM...", "ACCESS GRANTED."};
        Timeline introTimeline = new Timeline();
        for (int i = 0; i < lines.length; i++) {
            final int index = i;
            introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5 * (i + 1)), e -> text.setText(lines[index])));
        }
        introTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(lines.length * 0.5 + 0.5), e -> {
            introLayer.setVisible(false);
            gameLayer.setVisible(true);
            engine.currentState = HackEngine.GameState.PLAYING;
            checkBossLevel();
        }));
        introTimeline.play();
    }

    private void resetGame() {
        p.reset();
        engine.resetEvents();
        gameOverLayer.setVisible(false);
        gameLayer.setVisible(true);
        engine.currentState = HackEngine.GameState.PLAYING;
        checkBossLevel();
    }

    private void returnToMenu() {
        pauseLayer.setVisible(false);
        gameLayer.setVisible(false);
        shopLayer.setVisible(false);
        gameOverLayer.setVisible(false);
        routeLayer.setVisible(false);
        menuLayer.setVisible(true);
        highScoreDisplay.setText("HIGHEST LAYER: " + p.highScore);
        resetGame();
        engine.currentState = HackEngine.GameState.MAIN_MENU;
    }

    private void updateFirewallUI() {
        firewallBarDisplay.setText("[" + "|".repeat((int) (engine.firewallProgress * 20)) + ".".repeat(20 - (int) (engine.firewallProgress * 20)) + "]");
    }

    private void updateInterceptUI() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < engine.targetSequence.length(); i++)
            sb.append(i < engine.sequenceIndex ? "- " : engine.targetSequence.charAt(i) + " ");
        interceptTargetDisplay.setText(sb.toString().trim());
    }

    private void updateASCIIProgress() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i <= 20; i++) {
            if (i <= (engine.progress * 20)) sb.append("|");
            else sb.append(".");
        }
        sb.append("] ").append((int) (engine.progress * 100)).append("%");
        progressDisplay.setText("LEVEL " + p.currentLevel + " " + sb.toString());
        if (!engine.isHacking && !engine.isFirewallFight)
            statusLabel.setText(">>> WARNING: LOSING PROGRESS... [RELEASED]");
        else if (engine.isHacking)
            statusLabel.setText(">>> INJECTING... BREACHING LAYER " + (engine.currentSegment + 1));
    }

    private void typeWriterUpdate(String t) {
        currentTargetText = t;
        statusLabel.setText(t);
    }

    public static void main(String[] args) {
        launch();
    }
}