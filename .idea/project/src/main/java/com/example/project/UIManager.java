package com.example.project;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class UIManager {
    private final PlayerStats p;
    private final HackEngine engine;
    private final HelloApplication app;
    private final String[] charCache = new String[94];

    public StackPane root, menuLayer, introLayer, gameLayer, pauseLayer, firewallLayer, interceptLayer, decryptLayer, bugCatchLayer, shopLayer, gameOverLayer, routeLayer, talentLayer;
    public StackPane bossIntroLayer, bossFailLayer, surgeLayer; // 新增 SURGE 圖層
    public BossManager bossManager;
    public Label bossIntroTitle, bossIntroName, bossFailReason;
    public Button btnBossRetry, btnBossEscape;

    public Label progressDisplay, statusLabel, uiBorder, coinDisplay, comboDisplay, highScoreDisplay, talentCoinDisplay;
    public Label firewallBarDisplay, interceptTimeDisplay, decryptTargetDisplay, decryptInputDisplay, decryptTimeDisplay, bugScoreLabel, bugTimeLabel;
    public Label gameOverReasonLabel, gameOverStatsLabel, skillDisplay, glitchWarningLabel, talentNameLabel, talentEffectLabel, talentCostLabel;
    public Label traceWarningLabel, shopDescLabel;

    public HBox interceptTargetDisplay, surgeGridBox;
    public Label surgeTimeLabel, surgeHPLabel;
    public AnchorPane bugCatchPane;
    public Label versionLabel, systemStatusLabel, bootWarningLabel;
    public Button btnUpgradeTalent, honeypotBtn;
    public VBox descBox;
    public ImageView errorImage1, errorImage2;
    public Button btnShopClick, btnShopSpeed, btnShopCoolant, btnShopStealth, btnShopEmp, btnShopSlow, btnShopMiner, btnShopShield, btnShopAutoSolve, btnShopOverload;
    public Slider menuVolumeSlider, pauseVolumeSlider;
    public Slider menuSfxSlider, pauseSfxSlider;

    private Group treeGroup;
    public String currentTargetText = "";
    private DropShadow neonGlowCyan, neonGlowPink, neonGlowGreen;
    public Rectangle flashOverlay, scanline;

    private Image targetBugImg;
    private Image[] obstacleBugImgs;

    public Canvas matrixCanvas;
    private GraphicsContext gc;
    private final int matrixCols = 40;
    private final double[] dropY = new double[matrixCols];
    private final double[] dropSpeed = new double[matrixCols];

    public UIManager(PlayerStats p, HackEngine engine, HelloApplication app) {
        this.p = p; this.engine = engine; this.app = app;
        initVisualEffects(); buildVisuals();
    }

    private void initVisualEffects() {
        neonGlowCyan = new DropShadow(); neonGlowCyan.setColor(Color.rgb(0, 255, 204, 0.8)); neonGlowCyan.setRadius(15); neonGlowCyan.setSpread(0.2);
        neonGlowPink = new DropShadow(); neonGlowPink.setColor(Color.rgb(255, 0, 127, 0.8)); neonGlowPink.setRadius(15); neonGlowPink.setSpread(0.2);
        neonGlowGreen = new DropShadow(); neonGlowGreen.setColor(Color.rgb(0, 255, 0, 0.8)); neonGlowGreen.setRadius(15); neonGlowGreen.setSpread(0.4);
        try { targetBugImg = new Image(getClass().getResource("/bug.png").toExternalForm()); obstacleBugImgs = new Image[]{ new Image(getClass().getResource("/chen1.jpg").toExternalForm()), new Image(getClass().getResource("/chen2.jpg").toExternalForm()), new Image(getClass().getResource("/chen3.png").toExternalForm()) }; } catch (Exception e) {}

        // 預先產生好 94 個 ASCII 字元字串，避免在迴圈中每秒產生幾千個新 String 物件
        for(int i = 0; i < 94; i++) {
            charCache[i] = String.valueOf((char)(i + 33));
        }
    }

    private void buildVisuals() {
        root = new StackPane(); root.setStyle("-fx-background-color: #0b0c10;");
        matrixCanvas = new Canvas(800, 600); gc = matrixCanvas.getGraphicsContext2D();
        for (int i = 0; i < matrixCols; i++) { dropY[i] = engine.random.nextDouble() * 600; dropSpeed[i] = 1.5 + engine.random.nextDouble() * 2.5; }
        Label crtOverlay = new Label(); crtOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); crtOverlay.setStyle("-fx-background-color: repeating-linear-gradient(0deg, rgba(0,0,0,0) 0px, rgba(0,0,0,0) 1px, rgba(0,255,0,0.03) 2px, rgba(0,255,0,0.03) 3px);"); crtOverlay.setMouseTransparent(true);

        // === 駭客化修改：把大廳底色的透明度大幅降至 0.25，讓背後的閃電和雨粒清晰透出 ===
        menuLayer = new StackPane(); menuLayer.setStyle("-fx-background-color: rgba(11, 12, 16, 0.25);");

        VBox menuBox = new VBox(20); menuBox.setAlignment(Pos.CENTER);
        versionLabel = new Label("CONNECTION: ACTIVE // PROTOCOL: NEON_CORE_v2.85"); versionLabel.setTextFill(Color.rgb(0, 255, 204, 0.8)); versionLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 13));

        Label title = new Label("NEON BREACH"); title.setTextFill(Color.CYAN); title.setFont(Font.font("Impact", 75)); title.setEffect(neonGlowCyan);

        Timeline titleGlitch = new Timeline(new KeyFrame(Duration.millis(150), e -> {
            if (engine.random.nextDouble() < 0.08) {
                String[] glitches = {"N3ON BR3ACH", "N#ON B!EACH", "N_ON B_EACH", "NE0N 8R3ACH", "N E O N  B R E A C H"};
                title.setText(glitches[engine.random.nextInt(glitches.length)]);
                title.setTranslateX((engine.random.nextDouble() - 0.5) * 10);
                title.setTextFill(engine.random.nextBoolean() ? Color.web("#FF007F") : Color.WHITE);
            } else {
                title.setText("NEON BREACH");
                title.setTranslateX(0);
                title.setTextFill(Color.CYAN);
            }
        }));
        titleGlitch.setCycleCount(Animation.INDEFINITE); titleGlitch.play();

        highScoreDisplay = new Label("HIGHEST LAYER: " + p.highScore + "  |  LEGACY COINS: " + p.legacyCoins + " ¢"); highScoreDisplay.setTextFill(Color.LIME); highScoreDisplay.setFont(Font.font("Consolas", 20)); highScoreDisplay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5;");
        systemStatusLabel = new Label("[ SYSTEM STATUS: IDLE - READY FOR INJECTION ]"); systemStatusLabel.setTextFill(Color.YELLOW); systemStatusLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14)); systemStatusLabel.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5;");
        Button btnStart = createStyledButton(">>> INITIATE HACK <<<"); btnStart.setOnAction(e -> app.startIntroSequence());
        Button btnOpenTalents = createStyledButton(">>> CYBER TALENTS <<<"); setupNeonButtonAnimation(btnOpenTalents, "#FF007F", "rgba(255, 0, 127, 0.2)"); btnOpenTalents.setOnAction(e -> app.openTalentTree());
        Button btnExit = createStyledButton(">>> DISCONNECT SYSTEM <<<"); setupNeonButtonAnimation(btnExit, "#555555", "rgba(85, 85, 85, 0.1)"); btnExit.setOnAction(e -> System.exit(0));
        bootWarningLabel = new Label("⚠ WARNING: LOCAL SUBNET REPORTING QUANTUM FLUCTUATIONS ⚠"); bootWarningLabel.setTextFill(Color.rgb(255, 50, 50, 0.9)); bootWarningLabel.setFont(Font.font("Consolas", 11)); bootWarningLabel.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 2;");

        Label menuVolLabel = new Label("BGM_VOL:"); menuVolLabel.setTextFill(Color.CYAN); menuVolLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14)); menuVolumeSlider = new Slider(0, 1, 0.5); menuVolumeSlider.setMaxWidth(200); menuVolumeSlider.setStyle("-fx-cursor: hand;"); HBox menuVolBox = new HBox(10, menuVolLabel, menuVolumeSlider); menuVolBox.setAlignment(Pos.CENTER); menuVolBox.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5;");
        Label menuSfxLabel = new Label("SFX_VOL:"); menuSfxLabel.setTextFill(Color.CYAN); menuSfxLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14)); menuSfxSlider = new Slider(0, 1, 0.5); menuSfxSlider.setMaxWidth(200); menuSfxSlider.setStyle("-fx-cursor: hand;"); HBox menuSfxBox = new HBox(10, menuSfxLabel, menuSfxSlider); menuSfxBox.setAlignment(Pos.CENTER); menuSfxBox.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5;");

        menuBox.getChildren().addAll(versionLabel, title, highScoreDisplay, systemStatusLabel, btnStart, btnOpenTalents, btnExit, bootWarningLabel, menuVolBox, menuSfxBox);

        Label fakeTerminal = new Label("> INITIALIZING NEON_CORE...\n> WAITING FOR USER INPUT...");
        fakeTerminal.setTextFill(Color.LIME); fakeTerminal.setFont(Font.font("Consolas", 12));
        fakeTerminal.setAlignment(Pos.BOTTOM_LEFT); StackPane.setAlignment(fakeTerminal, Pos.BOTTOM_LEFT);
        fakeTerminal.setTranslateX(15); fakeTerminal.setTranslateY(-15);
        fakeTerminal.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 5;"); // 加深終端機底色防止被閃電蓋掉

        String[] logs = {
                "Decrypting RSA keys...",
                "Bypassing subnet mask...",
                "Sniffing port 443...",
                "Compiling C++ exploit...",
                "Solving Karnaugh map matrices...",
                "Bypassing logic gate arrays...",
                "WARNING: Trace attempt blocked.",
                "Fetching node data..."
        };

        Timeline terminalAnim = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (engine.random.nextDouble() < 0.3) return;
            String current = fakeTerminal.getText();
            String[] lines = current.split("\n");
            StringBuilder newLog = new StringBuilder();
            int start = Math.max(0, lines.length - 4);
            for (int i = start; i < lines.length; i++) newLog.append(lines[i]).append("\n");
            newLog.append("> ").append(logs[engine.random.nextInt(logs.length)]);
            fakeTerminal.setText(newLog.toString());
        }));
        terminalAnim.setCycleCount(Animation.INDEFINITE); terminalAnim.play();

        menuLayer.getChildren().addAll(menuBox, fakeTerminal);

        FadeTransition statusBlink = new FadeTransition(Duration.millis(800), systemStatusLabel); statusBlink.setFromValue(1.0); statusBlink.setToValue(0.3); statusBlink.setCycleCount(Animation.INDEFINITE); statusBlink.setAutoReverse(true); statusBlink.play(); FadeTransition warningBlink = new FadeTransition(Duration.millis(1200), bootWarningLabel); warningBlink.setFromValue(0.6); warningBlink.setToValue(0.2); warningBlink.setCycleCount(Animation.INDEFINITE); warningBlink.setAutoReverse(true); warningBlink.play();
        introLayer = new StackPane(); introLayer.setStyle("-fx-background-color: black;"); Label introText = new Label(); introText.setTextFill(Color.LIME); introText.setFont(Font.font("Consolas", 24)); introLayer.getChildren().add(introText); introLayer.setVisible(false);
        uiBorder = new Label("╔════════════════════════════════════════════╗\n║                                            ║\n║                                            ║\n║                                            ║\n╚════════════════════════════════════════════╝"); uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); uiBorder.setFont(Font.font("Consolas", 20)); uiBorder.setAlignment(Pos.CENTER);
        VBox gameBox = new VBox(10); gameBox.setAlignment(Pos.CENTER); statusLabel = new Label(""); statusLabel.setTextFill(Color.CYAN); statusLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18)); glitchWarningLabel = new Label(" [系統狀態：傳輸環境安全]"); glitchWarningLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 15)); glitchWarningLabel.setTextFill(Color.LIME); traceWarningLabel = new Label(""); traceWarningLabel.setTextFill(Color.RED); traceWarningLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16)); traceWarningLabel.setVisible(false); progressDisplay = new Label("LEVEL 1 [....☼....☼....☼....] 0%"); progressDisplay.setTextFill(Color.LIME); progressDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 28)); comboDisplay = new Label("COMBO: x1.0"); comboDisplay.setTextFill(Color.YELLOW); comboDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 20)); gameBox.getChildren().addAll(statusLabel, glitchWarningLabel, traceWarningLabel, comboDisplay, progressDisplay); gameLayer = new StackPane(uiBorder, gameBox); gameLayer.setVisible(false); skillDisplay = new Label("[1] EMP: 0   [2] SLOW: 0"); skillDisplay.setTextFill(Color.WHITE); skillDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 16)); StackPane.setAlignment(skillDisplay, Pos.BOTTOM_LEFT); gameLayer.getChildren().add(skillDisplay); honeypotBtn = new Button("⚠ [NODE VULNERABILITY] CLICK FOR 300 ¢"); honeypotBtn.setStyle("-fx-background-color: #330000; -fx-text-fill: #FF3333; -fx-border-color: red; -fx-cursor: hand;"); StackPane.setAlignment(honeypotBtn, Pos.TOP_RIGHT); honeypotBtn.setVisible(false); honeypotBtn.setOnAction(e -> app.handleHoneypotTrap()); gameLayer.getChildren().add(honeypotBtn);
        buildEventLayers(); buildRouteLayer(); buildShopLayer(); buildTalentLayerStructure(); buildBossLayers();
        pauseLayer = new StackPane(); pauseLayer.setStyle("-fx-background-color: rgba(0,0,0,0.85);"); VBox pauseBox = new VBox(20); pauseBox.setAlignment(Pos.CENTER); Label pauseTitle = new Label("SYSTEM PAUSED"); pauseTitle.setTextFill(Color.WHITE); pauseTitle.setFont(Font.font("Consolas", 40));

        Label pauseVolLabel = new Label("BGM_VOL:"); pauseVolLabel.setTextFill(Color.CYAN); pauseVolLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14)); pauseVolumeSlider = new Slider(0, 1, 0.5); pauseVolumeSlider.setMaxWidth(200); pauseVolumeSlider.setStyle("-fx-cursor: hand;"); HBox pauseVolBox = new HBox(10, pauseVolLabel, pauseVolumeSlider); pauseVolBox.setAlignment(Pos.CENTER);
        Label pauseSfxLabel = new Label("SFX_VOL:"); pauseSfxLabel.setTextFill(Color.CYAN); pauseSfxLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14)); pauseSfxSlider = new Slider(0, 1, 0.5); pauseSfxSlider.setMaxWidth(200); pauseSfxSlider.setStyle("-fx-cursor: hand;"); HBox pauseSfxBox = new HBox(10, pauseSfxLabel, pauseSfxSlider); pauseSfxBox.setAlignment(Pos.CENTER);

        menuVolumeSlider.valueProperty().bindBidirectional(pauseVolumeSlider.valueProperty()); menuVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> { app.setBgmVolume(newVal.doubleValue()); });
        menuSfxSlider.valueProperty().bindBidirectional(pauseSfxSlider.valueProperty()); menuSfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> { app.setSfxVolume(newVal.doubleValue()); });

        Button btnResume = createStyledButton("RESUME"); btnResume.setOnAction(e -> { engine.currentState = HackEngine.GameState.PLAYING; pauseLayer.setVisible(false); }); Button btnMenuPause = createStyledButton("ABORT TO MENU"); btnMenuPause.setOnAction(e -> app.returnToMenu()); pauseBox.getChildren().addAll(pauseTitle, pauseVolBox, pauseSfxBox, btnResume, btnMenuPause); pauseLayer.getChildren().add(pauseBox); pauseLayer.setVisible(false);
        gameOverLayer = new StackPane(); gameOverLayer.setStyle("-fx-background-color: rgba(139, 0, 0, 0.95);"); VBox overBox = new VBox(20); overBox.setAlignment(Pos.CENTER); gameOverReasonLabel = new Label(""); gameOverReasonLabel.setTextFill(Color.RED); gameOverReasonLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 45)); gameOverStatsLabel = new Label(""); gameOverStatsLabel.setTextFill(Color.WHITE); gameOverStatsLabel.setFont(Font.font("Consolas", 18)); gameOverStatsLabel.setTextAlignment(TextAlignment.CENTER); Button btnRestart = createStyledButton("SYSTEM REBOOT"); btnRestart.setOnAction(e -> app.resetGame()); Button btnMenuDead = createStyledButton("RETURN TO MENU"); btnMenuDead.setOnAction(e -> app.returnToMenu()); overBox.getChildren().addAll(gameOverReasonLabel, gameOverStatsLabel, btnRestart, btnMenuDead); gameOverLayer.getChildren().add(overBox); gameOverLayer.setVisible(false);
        errorImage1 = loadEmergeErrorImage("error1.jpg"); errorImage2 = loadEmergeErrorImage("error2.jpg"); flashOverlay = new Rectangle(800, 600, Color.TRANSPARENT); flashOverlay.setMouseTransparent(true); scanline = new Rectangle(800, 6, Color.CYAN); scanline.setVisible(false); scanline.setMouseTransparent(true);
        Node[] allLayers = { matrixCanvas, gameLayer, crtOverlay, firewallLayer, interceptLayer, decryptLayer, bugCatchLayer, surgeLayer, routeLayer, shopLayer, introLayer, gameOverLayer, bossIntroLayer, bossFailLayer, talentLayer, pauseLayer, menuLayer, errorImage1, errorImage2, flashOverlay, scanline }; for (Node layer : allLayers) if (layer != null) root.getChildren().add(layer);
    }

    private void buildBossLayers() {
        bossIntroLayer = new StackPane(); bossIntroLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.95);"); VBox introBox = new VBox(20); introBox.setAlignment(Pos.CENTER); bossIntroTitle = new Label("⚠ CRITICAL THREAT DETECTED ⚠"); bossIntroTitle.setTextFill(Color.RED); bossIntroTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 24)); bossIntroName = new Label(""); bossIntroName.setTextFill(Color.WHITE); bossIntroName.setFont(Font.font("Impact", 60)); introBox.getChildren().addAll(bossIntroTitle, bossIntroName); bossIntroLayer.getChildren().add(introBox); bossIntroLayer.setVisible(false);
        bossFailLayer = new StackPane(); bossFailLayer.setStyle("-fx-background-color: rgba(60, 0, 0, 0.95);"); VBox failBox = new VBox(20); failBox.setAlignment(Pos.CENTER); Label fTitle = new Label(">>> SYNCHRONIZATION LOST <<<"); fTitle.setTextFill(Color.RED); fTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 40)); bossFailReason = new Label("PHASE FAILED"); bossFailReason.setTextFill(Color.ORANGE); bossFailReason.setFont(Font.font("Consolas", 20)); btnBossRetry = createStyledButton(">>> REBOOT PHASE (Rage +1) <<<"); btnBossRetry.setOnAction(e -> app.bossManager.retryBossPhase()); btnBossEscape = createStyledButton(">>> 強制斷線生死賭局 (WARNING: 失敗即死) <<<"); setupNeonButtonAnimation(btnBossEscape, "#FF3333", "rgba(255, 51, 51, 0.2)"); btnBossEscape.setOnAction(e -> app.bossManager.escapeBoss()); failBox.getChildren().addAll(fTitle, bossFailReason, btnBossRetry, btnBossEscape); bossFailLayer.getChildren().add(failBox); bossFailLayer.setVisible(false);
    }

    public void playBossIntroAnimation(String title, String codeName, Color themeColor, Runnable onFinished) { bossIntroLayer.setVisible(true); bossIntroTitle.setText(title); bossIntroName.setText(""); bossIntroName.setEffect(new DropShadow(20, themeColor)); Timeline timeline = new Timeline(); for (int i = 0; i <= codeName.length(); i++) { final String text = codeName.substring(0, i); timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 100), e -> { bossIntroName.setText(text); })); } timeline.getKeyFrames().add(new KeyFrame(Duration.millis(codeName.length() * 100 + 1500), e -> { bossIntroLayer.setVisible(false); if (onFinished != null) onFinished.run(); })); timeline.play(); }
    private void drawLightningBoltOnCanvas(GraphicsContext gc) {
        double currentX = 100 + engine.random.nextDouble() * 600; // 閃電起始點 (偏中間)
        double currentY = 0;

        gc.setStroke(Color.WHITE); // 閃電核心為純白
        gc.setEffect(neonGlowCyan); // 外圍帶有強烈的青藍色光暈

        for (int step = 0; step < 15; step++) {
            // 隨機決定下一段閃電的轉折點
            double nextX = currentX + (engine.random.nextDouble() - 0.5) * 150;
            double nextY = currentY + 30 + engine.random.nextDouble() * 50;

            // 畫出主幹 (較粗)
            gc.setLineWidth(3 + engine.random.nextDouble() * 4);
            gc.strokeLine(currentX, currentY, nextX, nextY);

            // 30% 機率產生細小的分支閃電
            if (engine.random.nextDouble() < 0.3) {
                double branchX = currentX;
                double branchY = currentY;
                for (int b = 0; b < 4; b++) {
                    double nX = branchX + (engine.random.nextDouble() - 0.5) * 120;
                    double nY = branchY + 20 + engine.random.nextDouble() * 40;
                    gc.setLineWidth(1 + engine.random.nextDouble() * 2);
                    gc.strokeLine(branchX, branchY, nX, nY);
                    branchX = nX;
                    branchY = nY;
                }
            }
            currentX = nextX; currentY = nextY;
            if (currentY > 600) break;
        }
        gc.setEffect(null); // 重置發光效果以免影響代碼雨

        // 伴隨閃電的強烈青白全螢幕閃光
        playFlashEffect(Color.rgb(180, 255, 255, 0.45), 200);

        // 如果想加入雷聲，可以把這行註解打開 (需確保有 thunder.mp3)
        // app.playGunshotSound();
    }
    public void drawMatrixRain() {
        if (gc == null) return; gc.setFill(Color.rgb(11, 12, 16, 0.18)); gc.fillRect(0, 0, 800, 600); gc.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        boolean isBoss = engine.isBossFight; boolean isOverheated = engine.isOverheated; boolean isTraced = engine.isBeingTraced;

        boolean isMenu = (engine.currentState != HackEngine.GameState.PLAYING && engine.currentState != HackEngine.GameState.PAUSED && engine.currentState != HackEngine.GameState.INTRO);

        // === 駭客化修改：大廳隨機觸發白虎風格的真實閃電 (每幀約 1.2% 機率) ===
        if (isMenu && engine.random.nextDouble() < 0.012) {
            drawLightningBoltOnCanvas(gc);
        }

        for (int i = 0; i < matrixCols; i++) {
            String text;
            Color charColor;

            if (isMenu) {
                // 選單狀態：改成隨機跳動的 ASCII 亂碼
                text = String.valueOf((char)(engine.random.nextInt(94) + 33));
                // 提高不透明度到 1.0，使用更刺眼、更明顯的電光藍色
                charColor = Color.rgb(0, 210, 255, 1.0);
            } else {
                // 遊戲狀態：原本明亮的青綠色 ASCII 亂碼
                text = String.valueOf((char)(engine.random.nextInt(94) + 33));
                charColor = Color.rgb(0, 255, 204, 0.8);
            }

            if (isBoss) {
                if (engine.currentBossType == HackEngine.BossType.PULSE) charColor = Color.web("#FF007F");
                else if (engine.currentBossType == HackEngine.BossType.SURGE) charColor = Color.YELLOW;
                else if (engine.currentBossType == HackEngine.BossType.PHANTOM) charColor = Color.rgb(200, 0, 255, 0.9);
                else if (engine.currentBossType == HackEngine.BossType.CERBERUS) charColor = Color.rgb(255, 80, 0, 0.9);
                else if (engine.currentBossType == HackEngine.BossType.ARCHITECT) charColor = Color.rgb(255, 255, 100, 0.9);
                else if (engine.currentBossType == HackEngine.BossType.MIMIC) charColor = Color.LIGHTPINK;
                else if (engine.currentBossType == HackEngine.BossType.HYDRA) charColor = Color.GREENYELLOW;
                else if (engine.currentBossType == HackEngine.BossType.SPECTER) charColor = Color.DARKGRAY;
                else charColor = Color.rgb(255, 50, 50, 0.9);
            }
            if (isTraced && engine.random.nextInt(8) == 0) { text = "⚠"; charColor = Color.RED; }
            double x = i * 20; double y = dropY[i];
            if (isOverheated) { charColor = Color.rgb(255, 150, 0, 0.9); x += (engine.random.nextDouble() - 0.5) * 6.0; } else if (engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION) { x += (engine.random.nextDouble() - 0.5) * 4.0; }

            gc.setFill(charColor); gc.fillText(text, x, y);

            double currentSpeed = dropSpeed[i];
            // 把大廳的代碼雨速度調快，讓雨粒感更明顯更強烈
            if (isMenu) currentSpeed *= 0.95;

            if (isBoss) { currentSpeed *= 1.4; if (engine.bossPhase == 3) currentSpeed *= 1.5; } if (isOverheated) currentSpeed *= 0.5;
            dropY[i] += currentSpeed;

            if (dropY[i] > 600 && engine.random.nextDouble() > 0.92) {
                dropY[i] = 0;
                dropSpeed[i] = (isMenu ? 1.0 : 1.5) + engine.random.nextDouble() * 2.5;
            }
        }
    }
    //安全更新字串的方法，阻斷多餘的 JavaFX 渲染
    private void setLabelTextIfChanged(Label label, String newText) {
        if (!newText.equals(label.getText())) {
            label.setText(newText);
        }
    }

    public void playSweepTransition(Color color) { scanline.setFill(color); if(color.equals(Color.CYAN)) scanline.setEffect(neonGlowCyan); else if (color.equals(Color.web("#FF007F"))) scanline.setEffect(neonGlowPink); else scanline.setEffect(neonGlowGreen); scanline.setVisible(true); scanline.setTranslateY(-300); TranslateTransition sweep = new TranslateTransition(Duration.millis(350), scanline); sweep.setToY(300); FadeTransition ft = new FadeTransition(Duration.millis(350), scanline); ft.setFromValue(1.0); ft.setToValue(0.0); ParallelTransition pt = new ParallelTransition(sweep, ft); pt.setOnFinished(e -> scanline.setVisible(false)); pt.play(); }
    public void playPulseEffect() { ScaleTransition st = new ScaleTransition(Duration.millis(120), root); st.setFromX(1.0); st.setFromY(1.0); st.setToX(1.015); st.setToY(1.015); st.setAutoReverse(true); st.setCycleCount(2); st.play(); }
    public void shakeScreen() { TranslateTransition tt = new TranslateTransition(Duration.millis(40), root); tt.setFromX(0f); tt.setByX(5f); tt.setCycleCount(4); tt.setAutoReverse(true); tt.playFromStart(); }
    public void playFlashEffect(Color c, double durationMillis) { flashOverlay.setFill(c); FadeTransition ft = new FadeTransition(Duration.millis(durationMillis), flashOverlay); ft.setFromValue(0.25); ft.setToValue(0.0); ft.play(); }

    private void setupNeonButtonAnimation(Button btn, String primaryColor, String glowBgColor) {
        btn.setStyle("-fx-background-color: black; -fx-text-fill: " + primaryColor + "; -fx-border-color: " + primaryColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;");
        DropShadow btnGlow = new DropShadow(8, Color.web(primaryColor));
        btn.setOnMouseEntered(e -> {
            app.playHoverSound(); // 滑鼠移入時播放音效
            btn.setEffect(btnGlow);
            btn.setStyle("-fx-background-color: " + glowBgColor + "; -fx-text-fill: white; -fx-border-color: " + primaryColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setEffect(null);
            btn.setStyle("-fx-background-color: black; -fx-text-fill: " + primaryColor + "; -fx-border-color: " + primaryColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        btn.setOnMousePressed(e -> {
            app.playClickSound(); // 新增這行：滑鼠點擊下去時播放音效
            ScaleTransition st = new ScaleTransition(Duration.millis(50), btn);
            st.setToX(0.95);
            st.setToY(0.95);
            st.play();
        });
        btn.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(50), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
    }

    public void triggerErrorEffect(ImageView errorImg, int type) { if (errorImg == null || errorImg.getImage() == null) return; errorImg.setVisible(true); errorImg.setOpacity(0.0); errorImg.setScaleX(0.0); errorImg.setScaleY(0.0); app.playErrorSound(type); shakeScreen(); playFlashEffect(Color.rgb(255, 0, 0, 0.3), 300); FadeTransition fadeIn = new FadeTransition(Duration.millis(150), errorImg); fadeIn.setFromValue(0.0); fadeIn.setToValue(0.6); ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), errorImg); scaleUp.setFromX(0.0); scaleUp.setFromY(0.0); scaleUp.setToX(1.0); scaleUp.setToY(1.0); ParallelTransition emerge = new ParallelTransition(fadeIn, scaleUp); PauseTransition hold = new PauseTransition(Duration.millis(400)); FadeTransition fadeOut = new FadeTransition(Duration.millis(150), errorImg); fadeOut.setFromValue(0.6); fadeOut.setToValue(0.0); ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), errorImg); scaleDown.setToX(0.0); scaleDown.setToY(0.0); ParallelTransition submerge = new ParallelTransition(fadeOut, scaleDown); SequentialTransition seq = new SequentialTransition(emerge, hold, submerge); seq.setOnFinished(e -> errorImg.setVisible(false)); seq.play(); }
    public void updateTraceUI(double traceLevel) {
        if (traceLevel > 0) {
            traceWarningLabel.setVisible(true);
            int bars = (int)(Math.min(1.0, traceLevel) * 20); int dots = 20 - bars;
            String newText = String.format("⚠ 警告：反追蹤系統鎖定中 [%s%s] %d%% ⚠", "|".repeat(bars), ".".repeat(dots), (int)(traceLevel*100));
            setLabelTextIfChanged(traceWarningLabel, newText);
            traceWarningLabel.setEffect(new DropShadow(10, Color.RED));
        } else {
            traceWarningLabel.setVisible(false);
        }
    }
    public void updateFirewallUI() {
        if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.SPECTER && engine.bossPhase == 1) { firewallBarDisplay.setText(engine.firewallProgress > 0.9 ? "[! ! ! ! ! !]" : "[? ? ? ? ? ?]"); firewallBarDisplay.setTextFill(Color.DARKGRAY); firewallBarDisplay.setEffect(null); return; }
        if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.HYDRA && engine.bossPhase == 1) { StringBuilder sb = new StringBuilder(); for(int i=0; i<3; i++) { int bars = (int) Math.max(0, Math.min(10, engine.hydraWalls[i] * 10)); sb.append(i == engine.activeHydraHead ? "▶ " : "  "); sb.append("[").append("|".repeat(bars)).append(".".repeat(10-bars)).append("]"); if(i < 2) sb.append("\n"); } firewallBarDisplay.setText(sb.toString()); firewallBarDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 20)); firewallBarDisplay.setTextFill(Color.GREENYELLOW); return; }

        firewallBarDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 40)); int bars = (int) Math.max(0, Math.min(20, engine.firewallProgress * 20)); int dots = 20 - bars; int percent = (int) Math.round(engine.firewallProgress * 100); String heatWarning = engine.isOverheated ? " [LOCKED]" : "";
        if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.MIMIC && engine.bossPhase == 1) { firewallBarDisplay.setText("[" + "|".repeat(bars) + ".".repeat(dots) + "] " + percent + "%"); if (!engine.isMimicWindow) { firewallBarDisplay.setTextFill(Color.RED); firewallBarDisplay.setEffect(new DropShadow(15, Color.RED)); } else { firewallBarDisplay.setTextFill(Color.LIGHTPINK); firewallBarDisplay.setEffect(neonGlowPink); } return; }

        firewallBarDisplay.setText("[" + "|".repeat(bars) + ".".repeat(dots) + "] " + percent + "%" + heatWarning);
        if (engine.isOverheated) { firewallBarDisplay.setTextFill(Color.RED); firewallBarDisplay.setEffect(new DropShadow(15, Color.RED)); }
        else { firewallBarDisplay.setTextFill(Color.web("#33CCFF")); firewallBarDisplay.setEffect(neonGlowCyan); }
    }

    public void updatePulseScanUI() {
        int barWidth = 30; int pos = (int)(engine.pulseScanPos * barWidth); if (pos >= barWidth) pos = barWidth - 1; if (pos < 0) pos = 0;
        int zoneL = (int)(engine.pulseZoneMin * barWidth); int zoneR = (int)(engine.pulseZoneMax * barWidth);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barWidth; i++) {
            if (i == pos) bar.append("█"); else if (i >= zoneL && i <= zoneR) bar.append("▓"); else bar.append("░");
        }
        bar.append("]"); bar.append(String.format("  HITS: %d/%d", engine.pulseHitsCount, engine.pulseHitsRequired));
        firewallBarDisplay.setText(bar.toString()); firewallBarDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        if (engine.pulseJustHit) { firewallBarDisplay.setTextFill(Color.CYAN); firewallBarDisplay.setEffect(neonGlowCyan); }
        else { firewallBarDisplay.setTextFill(engine.pulseScanPos >= engine.pulseZoneMin && engine.pulseScanPos <= engine.pulseZoneMax ? Color.LIME : Color.web("#FF007F")); firewallBarDisplay.setEffect(null); }
    }

    // === SURGE UI 更新 ===
    public void updateSurgeUI(double elapsed) {
        double timeLeft = Math.max(0, 90.0 - elapsed);
        surgeTimeLabel.setText(String.format("T-MINUS: %.1fs", timeLeft));

        StringBuilder hpStr = new StringBuilder("HP: [ ");
        for(int i=0; i<3; i++) {
            if (i < engine.surgeHP) hpStr.append("♥ ");
            else hpStr.append("♡ ");
        }
        hpStr.append("]");
        surgeHPLabel.setText(hpStr.toString());

        boolean blink = (System.nanoTime() / 150_000_000L) % 2 == 0;

        for (int i = 0; i < 5; i++) {
            StackPane grid = (StackPane) surgeGridBox.getChildren().get(i);
            Label icon = (Label) grid.getChildren().get(0);

            if (i == engine.surgePlayerPos) icon.setText("▲"); else icon.setText("");

            if (engine.surgeExplosions[i]) {
                grid.setStyle("-fx-background-color: RED; -fx-border-color: WHITE; -fx-border-width: 4;");
                icon.setTextFill(Color.WHITE);
            } else if (engine.surgeWarnings[i]) {
                grid.setStyle("-fx-background-color: " + (blink ? "rgba(255,0,0,0.6)" : "rgba(100,0,0,0.5)") + "; -fx-border-color: RED; -fx-border-width: 2;");
                icon.setTextFill(Color.WHITE);
            } else {
                grid.setStyle("-fx-background-color: black; -fx-border-color: CYAN; -fx-border-width: 2;");
                icon.setTextFill(Color.CYAN);
            }
        }
        surgeLayer.toFront();
    }

    public void updateInterceptUI() {
        interceptTargetDisplay.getChildren().clear();
        if (engine.isBossFight && engine.currentBossType == HackEngine.BossType.SPECTER && engine.isSpecterHidden) { Label secretLabel = new Label("? ? ? ? ?"); secretLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 42)); secretLabel.setTextFill(Color.DARKGRAY); interceptTargetDisplay.getChildren().add(secretLabel); interceptLayer.toFront(); return; }
        String seq = engine.displaySequence.isEmpty() ? engine.targetSequence : engine.displaySequence;
        for (int i = 0; i < seq.length(); i++) {
            Label letterLabel = new Label(String.valueOf(seq.charAt(i))); letterLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 42));
            boolean isHighlighted = (engine.isBossFight && engine.currentBossType == HackEngine.BossType.MIMIC) ? (i == seq.length() - 1 - engine.sequenceIndex) : (i == engine.sequenceIndex);
            boolean isPassed = (engine.isBossFight && engine.currentBossType == HackEngine.BossType.MIMIC) ? (i > seq.length() - 1 - engine.sequenceIndex) : (i < engine.sequenceIndex);
            if (isPassed) { letterLabel.setTextFill(Color.web("#00FFCC")); letterLabel.setStyle("-fx-effect: dropshadow(three-pass-box, #00FFCC, 15, 0.6, 0, 0);"); } else {
                letterLabel.setStyle("-fx-background-color: rgba(0, 255, 204, 0.25); -fx-border-color: #00FFCC; -fx-border-width: 2; -fx-padding: 0 8 0 8; -fx-border-radius: 4; -fx-background-radius: 4;");
                letterLabel.setEffect(neonGlowCyan);
                if (isHighlighted) { letterLabel.setTextFill(Color.YELLOW); } else { letterLabel.setTextFill(Color.WHITE); }
            }
            interceptTargetDisplay.getChildren().add(letterLabel);
        }
        interceptLayer.toFront();
    }

    public void updatePulseInterceptUI(long now) {
        interceptTargetDisplay.getChildren().clear();
        for (int i = 0; i < engine.targetSequence.length(); i++) {
            String letter = String.valueOf(engine.targetSequence.charAt(i)); long diff = engine.pulseLetterDeadlines[i] - now; String beatBox = ""; Color boxColor = Color.web("#888888");
            if (i < engine.sequenceIndex) { beatBox = " [HIT]"; boxColor = Color.web("#00FFCC"); } else if (diff > engine.pulseBeatInterval) { beatBox = " [----]"; boxColor = Color.web("#888888"); } else if (diff < -engine.pulseLetterWindow) { beatBox = " [MISS]"; boxColor = Color.RED; } else {
                double ratio = (double) diff / engine.pulseBeatInterval;
                if (ratio > 0.8) beatBox = " [O---]"; else if (ratio > 0.6) beatBox = " [-O--]"; else if (ratio > 0.4) beatBox = " [--O-]"; else if (ratio > 0.2) beatBox = " [---O]"; else { beatBox = " [---O]"; boxColor = Color.LIME; }
            }
            Label letterLabel = new Label(letter); letterLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 28));
            if (i < engine.sequenceIndex) { letterLabel.setTextFill(Color.web("#00FFCC")); letterLabel.setStyle("-fx-effect: none;"); } else {
                letterLabel.setStyle("-fx-background-color: rgba(0, 255, 204, 0.25); -fx-border-color: #00FFCC; -fx-border-width: 2; -fx-padding: 0 8 0 8; -fx-border-radius: 4; -fx-background-radius: 4;"); letterLabel.setEffect(neonGlowCyan);
                if (i == engine.sequenceIndex) letterLabel.setTextFill(Color.YELLOW); else letterLabel.setTextFill(Color.WHITE);
            }
            Label boxLabel = new Label(beatBox); boxLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24)); boxLabel.setTextFill(boxColor);
            HBox group = new HBox(5, letterLabel, boxLabel); group.setAlignment(Pos.CENTER); interceptTargetDisplay.getChildren().add(group);
        }
        interceptLayer.toFront();
    }

    public void playPulseHitEffect() { ScaleTransition st = new ScaleTransition(Duration.millis(80), firewallBarDisplay); st.setFromX(1.15); st.setFromY(1.15); st.setToX(1.0); st.setToY(1.0); st.play(); }
    public void playFirewallSpacePopEffect() { firewallBarDisplay.setTextFill(Color.WHITE); ScaleTransition st = new ScaleTransition(Duration.millis(60), firewallBarDisplay); st.setFromX(1.06); st.setFromY(1.06); st.setToX(1.0); st.setToY(1.0); st.setOnFinished(e -> { updateFirewallUI(); }); st.play(); }
    private ImageView loadEmergeErrorImage(String fileName) { ImageView iv = new ImageView(); try { iv.setImage(new Image(getClass().getResource("/" + fileName).toExternalForm())); iv.setFitWidth(800); iv.setFitHeight(600); iv.setPreserveRatio(false); } catch (Exception e) {} iv.setOpacity(0.0); iv.setScaleX(0.0); iv.setScaleY(0.0); iv.setVisible(false); iv.setMouseTransparent(true); return iv; }

    private void buildEventLayers() {
        firewallLayer = new StackPane(); firewallLayer.setStyle("-fx-background-color: rgba(0, 40, 120, 0.55);"); VBox fwBox = new VBox(10); fwBox.setAlignment(Pos.CENTER); Label fwTitle = new Label("- FIREWALL BREAK ATTEMPT -"); fwTitle.setTextFill(Color.CYAN); fwTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        firewallBarDisplay = new Label("[||||||||||..........]"); firewallBarDisplay.setTextFill(Color.web("#33CCFF")); firewallBarDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 40)); firewallBarDisplay.setEffect(neonGlowCyan);
        fwBox.getChildren().addAll(fwTitle, new Label("⚡ MASH SPACEBAR NOW ⚡"), firewallBarDisplay); firewallLayer.getChildren().add(fwBox); StackPane.setAlignment(fwBox, Pos.TOP_CENTER); fwBox.setTranslateY(100); firewallLayer.setVisible(false);
        interceptLayer = new StackPane(); interceptLayer.setStyle("-fx-background-color: rgba(70, 0, 110, 0.7);"); VBox intBox = new VBox(10); intBox.setAlignment(Pos.CENTER); Label altTitle = new Label("! INTERCEPT !"); altTitle.setTextFill(Color.ORANGE); altTitle.setFont(Font.font("Consolas", 30)); interceptTargetDisplay = new HBox(15); interceptTargetDisplay.setAlignment(Pos.CENTER); interceptTimeDisplay = new Label("Time left: 3.0s"); interceptTimeDisplay.setTextFill(Color.WHITE); intBox.getChildren().addAll(altTitle, interceptTargetDisplay, interceptTimeDisplay); interceptLayer.getChildren().add(intBox); StackPane.setAlignment(intBox, Pos.BOTTOM_CENTER); intBox.setTranslateY(-100); interceptLayer.setVisible(false);
        decryptLayer = new StackPane(); decryptLayer.setStyle("-fx-background-color: rgba(0, 40, 0, 0.9);"); VBox decBox = new VBox(15); decBox.setAlignment(Pos.CENTER); Label decTitle = new Label("??? ENCRYPTED NODE ???"); decTitle.setTextFill(Color.LIME); decTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 35)); decryptTargetDisplay = new Label("MEMORIZE THIS"); decryptTargetDisplay.setTextFill(Color.WHITE); decryptTargetDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 50)); decryptTargetDisplay.setEffect(neonGlowGreen); decryptInputDisplay = new Label("> _"); decryptInputDisplay.setTextFill(Color.CYAN); decryptInputDisplay.setFont(Font.font("Consolas", 40)); decryptTimeDisplay = new Label("Time left: 4.0s"); decryptTimeDisplay.setTextFill(Color.WHITE); decBox.getChildren().addAll(decTitle, decryptTargetDisplay, decryptInputDisplay, decryptTimeDisplay); decryptLayer.getChildren().add(decBox); decryptLayer.setVisible(false);
        bugCatchLayer = new StackPane(); bugCatchLayer.setStyle("-fx-background-color: rgba(30, 0, 0, 0.85);"); bugCatchPane = new AnchorPane(); bugCatchPane.setPrefSize(800, 600); VBox bugInfoBox = new VBox(10); bugInfoBox.setAlignment(Pos.TOP_CENTER); Label bugTitle = new Label("- INVASIVE SWARM DETECTED -"); bugTitle.setTextFill(Color.RED); bugTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 30)); bugTitle.setEffect(new DropShadow(10, Color.RED)); bugScoreLabel = new Label("TARGET BUGS: 0 / 5"); bugScoreLabel.setTextFill(Color.LIME); bugScoreLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24)); bugScoreLabel.setEffect(neonGlowGreen); bugTimeLabel = new Label("Time left: 15.0s"); bugTimeLabel.setTextFill(Color.WHITE); bugTimeLabel.setFont(Font.font("Consolas", 24)); bugInfoBox.getChildren().addAll(bugTitle, bugScoreLabel, bugTimeLabel); bugCatchLayer.getChildren().addAll(bugInfoBox, bugCatchPane); StackPane.setAlignment(bugInfoBox, Pos.TOP_CENTER); bugInfoBox.setTranslateY(80); bugCatchLayer.setVisible(false);

        // === 構建 SURGE 專屬圖層 ===
        surgeLayer = new StackPane(); surgeLayer.setStyle("-fx-background-color: rgba(30, 10, 10, 0.90);");
        VBox surgeBox = new VBox(25); surgeBox.setAlignment(Pos.CENTER);
        Label surgeTitle = new Label("- SURGE PROTOCOL -"); surgeTitle.setTextFill(Color.RED); surgeTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 40));
        surgeTimeLabel = new Label("T-MINUS: 90.0s"); surgeTimeLabel.setTextFill(Color.WHITE); surgeTimeLabel.setFont(Font.font("Consolas", 35));
        surgeHPLabel = new Label("HP: [ ♥ ♥ ♥ ]"); surgeHPLabel.setTextFill(Color.web("#FF3366")); surgeHPLabel.setFont(Font.font("Consolas", 30));
        surgeGridBox = new HBox(15); surgeGridBox.setAlignment(Pos.CENTER);
        for(int i=0; i<5; i++) {
            StackPane grid = new StackPane(); grid.setPrefSize(90, 90); grid.setStyle("-fx-border-color: CYAN; -fx-border-width: 2; -fx-background-color: black;");
            Label playerIcon = new Label(""); playerIcon.setFont(Font.font("Consolas", FontWeight.BOLD, 45)); playerIcon.setTextFill(Color.CYAN);
            grid.getChildren().add(playerIcon); surgeGridBox.getChildren().add(grid);
        }
        Label controls = new Label(">>> USE [A] AND [D] TO EVADE <<<"); controls.setTextFill(Color.LIGHTGRAY); controls.setFont(Font.font("Consolas", 20));
        surgeBox.getChildren().addAll(surgeTitle, surgeTimeLabel, surgeHPLabel, surgeGridBox, controls); surgeLayer.getChildren().add(surgeBox); surgeLayer.setVisible(false);
    }

    public void spawnBugsForEvent() { bugCatchPane.getChildren().clear(); if (targetBugImg == null || obstacleBugImgs == null) return; double maxX = 650; double maxY = 400; ImageView targetView = new ImageView(targetBugImg); targetView.setFitWidth(80); targetView.setFitHeight(80); targetView.setLayoutX(50 + engine.random.nextDouble() * maxX); targetView.setLayoutY(150 + engine.random.nextDouble() * maxY); targetView.setStyle("-fx-cursor: hand;"); double targetScale = 0.5 + engine.random.nextDouble() * 0.5; targetView.setScaleX(targetScale); targetView.setScaleY(targetScale); TranslateTransition ttTarget = new TranslateTransition(Duration.millis(600 + engine.random.nextInt(500)), targetView); ttTarget.setByX((engine.random.nextDouble() - 0.5) * 200); ttTarget.setByY((engine.random.nextDouble() - 0.5) * 200); ttTarget.setAutoReverse(true); ttTarget.setCycleCount(Animation.INDEFINITE); ttTarget.play(); targetView.setOnMouseClicked(e -> { engine.bugsCaught++; app.playGunshotSound(); bugCatchPane.getChildren().remove(targetView); updateBugScoreUI(); if (engine.bugsCaught >= 5) { engine.isBugCatchFight = false; bugCatchLayer.setVisible(false); typeWriterUpdate(">>> BUG SWARM CLEARED."); engine.currentSegment++; } e.consume(); }); int obsCount = (engine.random.nextInt(3) + 1) + (p.currentLevel / 4); List<ImageView> obsList = new ArrayList<>(); for (int i = 0; i < obsCount; i++) { ImageView obsView = new ImageView(obstacleBugImgs[engine.random.nextInt(obstacleBugImgs.length)]); obsView.setFitWidth(80); obsView.setFitHeight(80); obsView.setLayoutX(50 + engine.random.nextDouble() * maxX); obsView.setLayoutY(150 + engine.random.nextDouble() * maxY); obsView.setStyle("-fx-cursor: hand;"); double obsScale = 0.6 + engine.random.nextDouble() * 0.6; obsView.setScaleX(obsScale); obsView.setScaleY(obsScale); TranslateTransition ttObs = new TranslateTransition(Duration.millis(600 + engine.random.nextInt(500)), obsView); ttObs.setByX((engine.random.nextDouble() - 0.5) * 250); ttObs.setByY((engine.random.nextDouble() - 0.5) * 250); ttObs.setAutoReverse(true); ttObs.setCycleCount(Animation.INDEFINITE); ttObs.play(); obsView.setOnMouseClicked(e -> { if (engine.bugsCaught > 0) engine.bugsCaught--; updateBugScoreUI(); app.playPictureHitSound(); bugCatchPane.getChildren().remove(obsView); triggerErrorEffect(errorImage2, 2); e.consume(); }); obsList.add(obsView); } bugCatchPane.getChildren().addAll(obsList); bugCatchPane.getChildren().add(targetView); }
    public void updateBugScoreUI() { bugScoreLabel.setText("TARGET BUGS: " + engine.bugsCaught + " / 5"); }
    private void buildRouteLayer() {
        routeLayer = new StackPane();
        // 1. 半透明底色讓大廳矩陣雨透出
        routeLayer.setStyle("-fx-background-color: rgba(5, 10, 15, 0.75);");

        VBox routeBox = new VBox(25); routeBox.setAlignment(Pos.CENTER);
        // 2. 加上全息投影的虛線邊框與發光效果
        routeBox.setMaxSize(600, 350);
        routeBox.setStyle("-fx-background-color: rgba(10, 20, 25, 0.85); -fx-border-color: #00FFCC; -fx-border-width: 2; -fx-border-style: dashed; -fx-padding: 30;");
        routeBox.setEffect(new DropShadow(15, Color.web("#00FFCC")));

        Label title = new Label(">>> SELECT NEXT NODE <<<"); title.setTextFill(Color.CYAN); title.setFont(Font.font("Consolas", 35)); title.setEffect(neonGlowCyan);

        // 標題閃爍動畫
        FadeTransition titleBlink = new FadeTransition(Duration.millis(1200), title); titleBlink.setFromValue(1.0); titleBlink.setToValue(0.4); titleBlink.setCycleCount(Animation.INDEFINITE); titleBlink.setAutoReverse(true); titleBlink.play();

        HBox btnBox = new HBox(40); btnBox.setAlignment(Pos.CENTER);
        Button btnNormal = createStyledButton("廢棄學術伺服器\n(難度: 0.8x | 獎勵: 0.7x)"); btnNormal.setOnAction(e -> { p.routeDiffMult = 0.8; p.routeRewardMult = 0.7; app.enterShop(); });
        Button btnHard = createStyledButton("高風險金融節點\n(難度: 1.5x | 獎勵: 2.0x)"); btnHard.setOnAction(e -> { p.routeDiffMult = 1.5; p.routeRewardMult = 2.0; app.enterShop(); });
        setupNeonButtonAnimation(btnNormal, "#00FFCC", "rgba(0, 255, 204, 0.15)");
        setupNeonButtonAnimation(btnHard, "#FF3333", "rgba(255, 51, 51, 0.15)");
        btnBox.getChildren().addAll(btnNormal, btnHard);

        Label warningText = new Label("⚠ ESTABLISHING SECURE TUNNEL... ⚠");
        warningText.setTextFill(Color.rgb(255, 200, 50, 0.8)); warningText.setFont(Font.font("Consolas", 14));

        routeBox.getChildren().addAll(title, btnBox, warningText); routeLayer.getChildren().add(routeBox); routeLayer.setVisible(false);
    }
    private void buildShopLayer() {
        shopLayer = new StackPane();
        shopLayer.setStyle("-fx-background-color: rgba(10, 5, 10, 0.80);");

        // 微調間距與大小以容納 5 排按鈕
        VBox shopBox = new VBox(10); shopBox.setAlignment(Pos.CENTER);
        shopBox.setMaxSize(720, 580);
        shopBox.setStyle("-fx-background-color: rgba(20, 5, 20, 0.85); -fx-border-color: #FF007F; -fx-border-width: 2; -fx-padding: 20; -fx-border-style: solid;");
        shopBox.setEffect(new DropShadow(20, Color.web("#FF007F")));

        Label shopTitle = new Label("--- BLACK MARKET ---"); shopTitle.setTextFill(Color.web("#FF007F")); shopTitle.setFont(Font.font("Consolas", 35)); shopTitle.setEffect(neonGlowPink);
        coinDisplay = new Label("DarkCoins: 0 ¢"); coinDisplay.setTextFill(Color.GOLD); coinDisplay.setFont(Font.font("Consolas", 22));

        shopDescLabel = new Label(">>> 游標懸停以查看組件說明 <<<"); shopDescLabel.setTextFill(Color.LIGHTGRAY); shopDescLabel.setFont(Font.font("Consolas", 13));
        shopDescLabel.setStyle("-fx-background-color: rgba(30, 20, 40, 0.8); -fx-padding: 8; -fx-border-color: #00FFCC; -fx-border-width: 1; -fx-border-style: dashed; -fx-max-width: 600; -fx-wrap-text: true;");
        shopDescLabel.setAlignment(Pos.CENTER); shopDescLabel.setTextAlignment(TextAlignment.CENTER); shopDescLabel.setMinHeight(50);

        btnShopClick = createShopButton("", 0); btnShopClick.setOnAction(e -> { int cost = 100 + p.upgClick * 150; if(p.buy(cost)) { p.upgClick++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopClick, "「重磅封包」\n增加每次敲擊空白鍵對防火牆造成的破壞力。");
        btnShopSpeed = createShopButton("", 0); btnShopSpeed.setOnAction(e -> { int cost = 150 + p.upgSpeed * 200; if(p.buy(cost)) { p.upgSpeed++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopSpeed, "「注入加速」\n提升一般節點的自動注入速度，減少滑鼠長按時間。");
        btnShopCoolant = createShopButton("", 0); btnShopCoolant.setOnAction(e -> { int cost = 120 + p.upgCoolant * 180; if(p.buy(cost)) { p.upgCoolant++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopCoolant, "「散熱組件」\n降低敲擊空白鍵產生的熱量，延緩過熱鎖定。");
        btnShopStealth = createShopButton("", 0); btnShopStealth.setOnAction(e -> { int cost = 120 + p.upgStealth * 180; if(p.buy(cost)) { p.upgStealth++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopStealth, "「隱蔽路由」\n減緩在一般節點被反追蹤系統鎖定的速度。");

        // === 全新機制商品 ===
        btnShopMiner = createShopButton("", 0); btnShopMiner.setOnAction(e -> { int cost = 150 + p.upgMiner * 200; if(p.buy(cost)) { p.upgMiner++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopMiner, "「木馬挖礦程式」\n每過一關額外增加金幣收益，但會顯著加快被反追蹤鎖定的速度。");
        btnShopShield = createShopButton("", 0); btnShopShield.setOnAction(e -> { if(p.hasTraceShield) return; int cost = 400; if(p.buy(cost)) { p.hasTraceShield = true; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopShield, "「備用快取護盾」\n持有時，若被反追蹤鎖定達 100%，將自動碎裂以完全抵禦該次進度扣除懲罰。");
        btnShopAutoSolve = createShopButton("", 0); btnShopAutoSolve.setOnAction(e -> { int cost = 300 + p.autoSolveCharges * 150; if(p.buy(cost)) { p.autoSolveCharges++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopAutoSolve, "「邏輯閘短路器」\n消耗品。在解密或攔截事件中按下快捷鍵 [3] ，直接強行自動破解通過。");
        btnShopOverload = createShopButton("", 0); btnShopOverload.setOnAction(e -> { int cost = 350 + p.overloadCharges * 200; if(p.buy(cost)) { p.overloadCharges++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopOverload, "「核心過載病毒」\n消耗品。在打擊防火牆時按 [4] 造成毀滅性巨量破壞，但核心將陷入長時間嚴重過熱。");

        btnShopEmp = createShopButton("", 0); btnShopEmp.setOnAction(e -> { int cost = 200 + p.empCharges * 150; if(p.buy(cost)) { p.empCharges++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopEmp, "「EMP 脈衝彈」\n消耗品。在防火牆戰鬥中按 [1] 瞬間炸毀大量防禦。");
        btnShopSlow = createShopButton("", 0); btnShopSlow.setOnAction(e -> { int cost = 250 + p.slowCharges * 200; if(p.buy(cost)) { p.slowCharges++; updateShopUI(); app.playSuccessSound(); } else { app.playNoMoneySound(); shakeScreen(); } }); addShopHoverDesc(btnShopSlow, "「超頻沙漏」\n消耗品。在限時戰鬥中按 [2] 延長駭入時間。");

        // 排版為雙欄 5 橫列
        HBox row1 = new HBox(15, btnShopClick, btnShopSpeed); row1.setAlignment(Pos.CENTER);
        HBox row2 = new HBox(15, btnShopCoolant, btnShopStealth); row2.setAlignment(Pos.CENTER);
        HBox row3 = new HBox(15, btnShopMiner, btnShopShield); row3.setAlignment(Pos.CENTER);
        HBox row4 = new HBox(15, btnShopEmp, btnShopSlow); row4.setAlignment(Pos.CENTER);
        HBox row5 = new HBox(15, btnShopAutoSolve, btnShopOverload); row5.setAlignment(Pos.CENTER);

        Button btnNext = createStyledButton(">>> INJECT PAYLOAD <<<");
        btnNext.setOnAction(e -> { engine.currentState = HackEngine.GameState.PLAYING; shopLayer.setVisible(false); gameLayer.setVisible(true); engine.resetEvents(); app.bossManager.checkBossLevel(); });
        setupNeonButtonAnimation(btnNext, "#00FFCC", "rgba(0, 255, 204, 0.2)");

        shopBox.getChildren().addAll(shopTitle, coinDisplay, shopDescLabel, row1, row2, row3, row4, row5, btnNext);
        shopLayer.getChildren().add(shopBox); shopLayer.setVisible(false);
    }
    private void addShopHoverDesc(Button btn, String desc) { btn.hoverProperty().addListener((obs, oldVal, newVal) -> { if (newVal) { shopDescLabel.setText(desc); shopDescLabel.setTextFill(Color.WHITE); } else { shopDescLabel.setText(">>> 游標懸停以查看組件說明 <<<"); shopDescLabel.setTextFill(Color.LIGHTGRAY); } }); }
    private void buildTalentLayerStructure() { talentLayer = new StackPane(); talentLayer.setStyle("-fx-background-color: #0d0214;"); treeGroup = new Group(); StackPane treePane = new StackPane(treeGroup); treePane.setAlignment(Pos.CENTER); treePane.setPrefSize(700, 320); descBox = new VBox(5); descBox.setAlignment(Pos.CENTER); descBox.setStyle("-fx-background-color: #160826; -fx-border-color: #FF007F; -fx-border-width: 2; -fx-padding: 15; -fx-max-width: 550;"); talentNameLabel = new Label(">>> 點擊任意節點解密核心天賦 <<<"); talentNameLabel.setTextFill(Color.CYAN); talentNameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16)); talentEffectLabel = new Label("選取節點以加載組件加成數據。"); talentEffectLabel.setTextFill(Color.LIGHTGRAY); talentEffectLabel.setFont(Font.font("Consolas", 14)); talentCostLabel = new Label(""); talentCostLabel.setTextFill(Color.GOLD); talentCostLabel.setFont(Font.font("Consolas", 14)); btnUpgradeTalent = createStyledButton(">>> 寫入天賦線路 <<<"); setupNeonButtonAnimation(btnUpgradeTalent, "#FF007F", "rgba(255, 0, 127, 0.2)"); btnUpgradeTalent.setVisible(false); descBox.getChildren().addAll(talentNameLabel, talentEffectLabel, talentCostLabel, btnUpgradeTalent); VBox talentLayout = new VBox(15); talentLayout.setAlignment(Pos.CENTER); Label title = new Label("== CYBERNETIC TALENT TREE =="); title.setTextFill(Color.web("#FF007F")); title.setFont(Font.font("Consolas", FontWeight.BOLD, 35)); title.setEffect(neonGlowPink); talentCoinDisplay = new Label("LEGACY COINS: 0 ¢"); talentCoinDisplay.setTextFill(Color.GOLD); talentCoinDisplay.setFont(Font.font("Consolas", 24)); Button btnBack = createStyledButton("<<< RETURN TO MENU"); btnBack.setOnAction(e -> app.closeTalentTree()); talentLayout.getChildren().addAll(title, talentCoinDisplay, treePane, descBox, btnBack); talentLayer.getChildren().add(talentLayout); talentLayer.setVisible(false); drawTalentTreeNodes(); }
    private void drawTalentTreeNodes() {
        treeGroup.getChildren().clear(); Group lineLayer = new Group(); Group nodeLayer = new Group(); treeGroup.getChildren().addAll(lineLayer, nodeLayer); StackPane corePane = new StackPane(); corePane.setMinSize(80, 80); corePane.setMaxSize(80, 80); Circle coreCircle = new Circle(32); coreCircle.setStrokeWidth(3); coreCircle.setStroke(Color.CYAN); coreCircle.setFill(Color.rgb(0, 40, 50)); Label coreLabel = new Label("🌐"); coreLabel.setFont(Font.font("Segoe UI Emoji", 18)); coreLabel.setTextFill(Color.WHITE); corePane.getChildren().addAll(coreCircle, coreLabel); corePane.setTranslateX(-40); corePane.setTranslateY(-40); corePane.setEffect(neonGlowCyan); nodeLayer.getChildren().add(corePane);

        // 重新架構為十字型佈局 (270度朝上、180度朝左、90度朝下、0度朝右)
        buildTalentBranch(1, 3, 270, 70, new Color[]{Color.CYAN, Color.rgb(0, 40, 50)}, 0, 0, "⚡", lineLayer, nodeLayer);
        buildTalentBranch(2, 5, 180, 70, new Color[]{Color.LIME, Color.rgb(0, 40, 0)}, 0, 0, "🔓", lineLayer, nodeLayer);
        buildTalentBranch(3, 3, 90, 70, new Color[]{Color.ORANGE, Color.rgb(40, 20, 0)}, 0, 0, "⏳", lineLayer, nodeLayer);
        buildTalentBranch(4, 3, 0, 70, new Color[]{Color.MAGENTA, Color.rgb(50, 0, 50)}, 0, 0, "🛡", lineLayer, nodeLayer); // 新增第四分支
    }
    private void buildTalentBranch(int id, int maxLevel, double angle, double startRadius, Color[] colors, double parentX, double parentY, String iconSymbol, Group lineLayer, Group nodeLayer) {
        double currentParentX = parentX; double currentParentY = parentY; double labelR = startRadius + (maxLevel * 52) + 25; double lx = labelR * Math.cos(Math.toRadians(angle)); double ly = labelR * Math.sin(Math.toRadians(angle));
        Label branchLabel = new Label(id == 1 ? "[ EMP MOD ]" : (id == 2 ? "[ FW BYPASS ]" : (id == 3 ? "[ BUFFER EXP ]" : "[ SIGNAL SHIELD ]")));
        branchLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 10)); branchLabel.setTextFill(colors[0]); branchLabel.setAlignment(Pos.CENTER); branchLabel.setPrefWidth(150); StackPane labelPane = new StackPane(branchLabel); labelPane.setTranslateX(lx - 75); labelPane.setTranslateY(ly - 10); nodeLayer.getChildren().add(labelPane);
        for (int i = 1; i <= maxLevel; i++) {
            double r = startRadius + (i-1) * 52; double x = r * Math.cos(Math.toRadians(angle)); double y = r * Math.sin(Math.toRadians(angle)); Color strokeColor = unlocked(id, i) ? colors[0] : (isNextAvailable(id, i) ? Color.LIGHTGRAY : Color.rgb(60, 60, 60)); Color fillColor = unlocked(id, i) ? colors[1] : (isNextAvailable(id, i) ? Color.rgb(30, 30, 35) : Color.rgb(15, 15, 15)); Line line = new Line(currentParentX, currentParentY, x, y); line.setStrokeWidth(3); line.setStroke(unlocked(id, i) ? colors[0] : Color.rgb(70, 70, 70));

            // === 駭客化優化：已解鎖的線條加入資料流動(虛線平移)動畫 ===
            if (unlocked(id, i)) {
                line.getStrokeDashArray().addAll(8d, 6d);
                Timeline dashAnim = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(line.strokeDashOffsetProperty(), 14)),
                        new KeyFrame(Duration.millis(400), new KeyValue(line.strokeDashOffsetProperty(), 0))
                );
                dashAnim.setCycleCount(Animation.INDEFINITE);
                dashAnim.play();
            }

            lineLayer.getChildren().add(line); StackPane nodePane = new StackPane(); nodePane.setMinSize(40, 40); nodePane.setMaxSize(40, 40); Circle c = new Circle(18); c.setStrokeWidth(3); c.setStroke(strokeColor); c.setFill(fillColor); Button btn = new Button(iconSymbol); btn.setFont(Font.font("Segoe UI Emoji", 14)); btn.setTextFill(unlocked(id, i) ? Color.WHITE : (isNextAvailable(id, i) ? colors[0] : Color.rgb(80, 80, 80))); btn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;");

            btn.setOnMouseEntered(e -> app.playHoverSound());
            btn.setOnMousePressed(e -> app.playClickSound());

            final int branchId = id; final int nodeLevel = i; btn.setOnAction(e -> app.selectTalentNode(branchId, nodeLevel)); nodePane.getChildren().addAll(c, btn); nodePane.setTranslateX(x - 20); nodePane.setTranslateY(y - 20); if (unlocked(id, i)) { DropShadow branchGlow = new DropShadow(10, colors[0]); nodePane.setEffect(branchGlow); } nodeLayer.getChildren().add(nodePane); currentParentX = x; currentParentY = y;
        }
    }
    public void updateComboDisplay(double multiplier) { comboDisplay.setText(String.format("COMBO: x%.1f", multiplier)); if (multiplier >= 3.0) { comboDisplay.setTextFill(Color.web("#FF007F")); comboDisplay.setEffect(neonGlowPink); uiBorder.setStyle(engine.random.nextInt(3) == 0 ? "-fx-text-fill: #FF007F; -fx-effect: dropshadow(three-pass-box, #FF007F, 10, 0, 0, 0);" : "-fx-text-fill: rgb(0, 255, 204); -fx-effect: none;"); } else if (multiplier >= 2.0) { comboDisplay.setTextFill(Color.ORANGE); comboDisplay.setEffect(new DropShadow(10, Color.ORANGE)); uiBorder.setStyle("-fx-text-fill: orange;"); } else { comboDisplay.setTextFill(Color.YELLOW); comboDisplay.setEffect(null); uiBorder.setStyle("-fx-text-fill: rgba(0, 255, 204, 0.5);"); } }
    public void playComboHitEffect(double multiplier) { if (multiplier < 2.0) return; double intensity = (multiplier >= 3.0) ? 5.0 : 2.5; TranslateTransition tt = new TranslateTransition(Duration.millis(30), gameLayer); tt.setFromX((engine.random.nextDouble() - 0.5) * intensity); tt.setFromY((engine.random.nextDouble() - 0.5) * intensity); tt.setToX(0f); tt.setToY(0f); tt.playFromStart(); }
    public void playDescFadeIn() { FadeTransition ft = new FadeTransition(Duration.millis(250), descBox); ft.setFromValue(0.2); ft.setToValue(1.0); ft.play(); }
    public void updateGlitchDisplay() { if (engine.activeGlitch == HackEngine.GlitchType.NONE) { glitchWarningLabel.setText(" [系統狀態：傳輸環境安全]"); glitchWarningLabel.setTextFill(Color.LIME); glitchWarningLabel.setEffect(null); } else if (engine.activeGlitch == HackEngine.GlitchType.NETWORK_LAG) { glitchWarningLabel.setText("⚠ 環境詛咒：[NETWORK_LAG] 延遲嚴重 ⚠"); glitchWarningLabel.setTextFill(Color.ORANGE); glitchWarningLabel.setEffect(new DropShadow(8, Color.ORANGE)); } else if (engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION) { glitchWarningLabel.setText("⚠ 環境詛咒：[VISUAL_DISTORTION] 視覺污染 ⚠"); glitchWarningLabel.setTextFill(Color.web("#FF007F")); glitchWarningLabel.setEffect(neonGlowPink); } else if (engine.activeGlitch == HackEngine.GlitchType.CORE_OVERLOAD) { glitchWarningLabel.setText("⚠ 環境詛咒：[CORE_OVERLOAD] 核心超載 ⚠"); glitchWarningLabel.setTextFill(Color.RED); glitchWarningLabel.setEffect(new DropShadow(12, Color.RED)); } }
    public void updateShopUI() {
        coinDisplay.setText("DarkCoins: " + p.darkCoins + " ¢");
        // 更新底部常駐技能列
        skillDisplay.setText(String.format("[1] EMP:%d  [2] SLOW:%d  [3] SKIP:%d  [4] VIRUS:%d  %s", p.empCharges, p.slowCharges, p.autoSolveCharges, p.overloadCharges, p.hasTraceShield ? "[🛡 SHIELD: ON]" : "[🛡 SHIELD: OFF]"));

        btnShopClick.setText(String.format("重磅封包 (Lv.%d) [Cost: %d¢]", p.upgClick, 100 + p.upgClick * 150));
        btnShopSpeed.setText(String.format("注入加速 (Lv.%d) [Cost: %d¢]", p.upgSpeed, 150 + p.upgSpeed * 200));
        btnShopCoolant.setText(String.format("散熱組件 (Lv.%d) [Cost: %d¢]", p.upgCoolant, 120 + p.upgCoolant * 180));
        btnShopStealth.setText(String.format("隱蔽路由 (Lv.%d) [Cost: %d¢]", p.upgStealth, 120 + p.upgStealth * 180));

        btnShopMiner.setText(String.format("木馬挖礦 (Lv.%d) [Cost: %d¢]", p.upgMiner, 150 + p.upgMiner * 200));
        btnShopShield.setText(p.hasTraceShield ? "備用快取護盾 [已裝備]" : "備用快取護盾 [Cost: 400¢]");
        btnShopAutoSolve.setText(String.format("邏輯短路器 [Cost: %d¢]", 300 + p.autoSolveCharges * 150));
        btnShopOverload.setText(String.format("核心過載病毒 [Cost: %d¢]", 350 + p.overloadCharges * 200));

        btnShopEmp.setText(String.format("EMP 脈衝彈 [Cost: %d¢]", 200 + p.empCharges * 150));
        btnShopSlow.setText(String.format("超頻沙漏 [Cost: %d¢]", 250 + p.slowCharges * 200));

        if (engine.isBossLevel(p.currentLevel + 1)) { shopDescLabel.setText("⚠ WARNING: BOSS ENCOUNTER IMMINENT ⚠\n建議補滿所有控制與防禦組件！"); shopDescLabel.setTextFill(Color.RED); shopDescLabel.setEffect(new DropShadow(5, Color.RED)); }
        else { shopDescLabel.setText(">>> 游標懸停以查看組件說明 <<<"); shopDescLabel.setTextFill(Color.LIGHTGRAY); shopDescLabel.setEffect(null); }
    }
    public void updateTalentUI() { talentCoinDisplay.setText("LEGACY COINS: " + p.legacyCoins + " ¢"); highScoreDisplay.setText("HIGHEST LAYER: " + p.highScore + "  |  LEGACY COINS: " + p.legacyCoins + " ¢"); drawTalentTreeNodes(); }
    public void updateDecryptUI() { decryptInputDisplay.setText("> " + engine.decryptInput + "_"); decryptInputDisplay.setTextFill(Color.web("#00FFCC")); decryptInputDisplay.setStyle("-fx-effect: dropshadow(three-pass-box, #00FFCC, 12, 0.4, 0, 0);"); ScaleTransition st = new ScaleTransition(Duration.millis(100), decryptInputDisplay); st.setFromX(1.1); st.setFromY(1.1); st.setToX(1.0); st.setToY(1.0); st.play(); }
    public void showGameOverStats(String reason, int level, int legacy, double maxCombo, int apm, double accuracy, String title) { gameOverReasonLabel.setText(reason); String stats = String.format("REACHED LAYER: %d\nMAX COMBO: x%.1f\nHACKING APM: %d\nACCURACY: %.1f%%\n\nCYBER TITLE EARNED:\n[ %s ]\n\nPERMANENT LEGACY COINS EARNED: +%d ¢", level, maxCombo, apm, accuracy, title, legacy); gameOverStatsLabel.setText(stats); gameOverLayer.setVisible(true); firewallLayer.setVisible(false); interceptLayer.setVisible(false); decryptLayer.setVisible(false); bugCatchLayer.setVisible(false); surgeLayer.setVisible(false); }
    private boolean unlocked(int id, int level) { if (id == 1) return p.talentStartEMP >= level; if (id == 2) return p.talentWeakFW >= level; if (id == 3) return p.talentFlashTime >= level; if (id == 4) return p.talentSignalShield >= level; return false; }
    private boolean isNextAvailable(int id, int level) { if (id == 1) return p.talentStartEMP == level - 1; if (id == 2) return p.talentWeakFW == level - 1; if (id == 3) return p.talentFlashTime == level - 1; if (id == 4) return p.talentSignalShield == level - 1; return false; }
    public void updateASCIIProgress() {
        if (engine.isBossFight) {
            setLabelTextIfChanged(progressDisplay, "LEVEL " + p.currentLevel + " [ SYSTEM OVERRIDE ]");
            if (engine.currentBossType == HackEngine.BossType.SURGE) setLabelTextIfChanged(statusLabel, ">>> SURGE PROTOCOL INITIATED <<<");
            else setLabelTextIfChanged(statusLabel, ">>> BOSS ENGAGED : PHASE " + engine.bossPhase);
        } else {
            StringBuilder sb = new StringBuilder("["); int fill = (int) Math.round(engine.progress * 20); int percentage = (int) Math.round(engine.progress * 100);
            for (int i=1; i<=20; i++) { if (i <= fill) sb.append("|"); else sb.append("."); }
            sb.append("] ").append(percentage).append("%");
            setLabelTextIfChanged(progressDisplay, "LEVEL " + p.currentLevel + " " + sb.toString());
            if (!engine.isHacking && !engine.isFirewallFight && !engine.isBugCatchFight && !engine.isInterceptFight && !engine.isDecryptFight && !engine.isSurgeFight)
                setLabelTextIfChanged(statusLabel, ">>> WARNING: LOSING PROGRESS... [RELEASED]");
            else if (engine.isHacking) setLabelTextIfChanged(statusLabel, ">>> INJECTING... BREACHING LAYER " + (engine.currentSegment+1));
        }
    }
    public void typeWriterUpdate(String t) { this.currentTargetText = t; statusLabel.setText(t); }
    private Button createStyledButton(String text) { Button btn = new Button(text); setupNeonButtonAnimation(btn, "#00FFCC", "rgba(0, 255, 204, 0.15)"); return btn; }
    private Button createShopButton(String name, int cost) { Button btn = new Button(name); setupNeonButtonAnimation(btn, "#00FF00", "rgba(0, 255, 0, 0.12)"); return btn; }
}