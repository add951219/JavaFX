package com.example.project;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class UIManager {
    private final PlayerStats p;
    private final HackEngine engine;
    private final HelloApplication app;

    public StackPane root, menuLayer, introLayer, gameLayer, pauseLayer, firewallLayer, interceptLayer, decryptLayer, bugCatchLayer, shopLayer, gameOverLayer, routeLayer, talentLayer;
    public Label progressDisplay, statusLabel, uiBorder, matrixBg, coinDisplay, comboDisplay, highScoreDisplay, talentCoinDisplay;
    public Label firewallBarDisplay, interceptTimeDisplay, decryptTargetDisplay, decryptInputDisplay, decryptTimeDisplay, bugScoreLabel, bugTimeLabel;
    public Label gameOverReasonLabel, gameOverStatsLabel, skillDisplay, glitchWarningLabel, talentNameLabel, talentEffectLabel, talentCostLabel;

    public HBox interceptTargetDisplay;
    public AnchorPane bugCatchPane;

    public Label versionLabel, systemStatusLabel, bootWarningLabel;
    public Button btnUpgradeTalent, honeypotBtn;
    public VBox descBox;
    public ImageView errorImage1, errorImage2;

    public Slider menuVolumeSlider, pauseVolumeSlider;

    private Group treeGroup;
    public String currentTargetText = "";

    // 美術視覺特效物件
    private DropShadow neonGlowCyan, neonGlowPink, neonGlowGreen;

    // 高科技視覺疊加層
    public Rectangle flashOverlay;
    public Rectangle scanline;

    // 蟲子圖片快取
    private Image targetBugImg;
    private Image[] obstacleBugImgs;

    public UIManager(PlayerStats p, HackEngine engine, HelloApplication app) {
        this.p = p; this.engine = engine; this.app = app;
        initVisualEffects();
        buildVisuals();
    }

    private void initVisualEffects() {
        neonGlowCyan = new DropShadow(); neonGlowCyan.setColor(Color.rgb(0, 255, 204, 0.8)); neonGlowCyan.setRadius(15); neonGlowCyan.setSpread(0.2);
        neonGlowPink = new DropShadow(); neonGlowPink.setColor(Color.rgb(255, 0, 127, 0.8)); neonGlowPink.setRadius(15); neonGlowPink.setSpread(0.2);
        neonGlowGreen = new DropShadow(); neonGlowGreen.setColor(Color.rgb(0, 255, 0, 0.8)); neonGlowGreen.setRadius(12); neonGlowGreen.setSpread(0.1);

        try {
            targetBugImg = new Image(getClass().getResource("/bug.png").toExternalForm());
            obstacleBugImgs = new Image[]{
                    new Image(getClass().getResource("/chen1.jpg").toExternalForm()),
                    new Image(getClass().getResource("/chen2.jpg").toExternalForm()),
                    new Image(getClass().getResource("/chen3.png").toExternalForm())
            };
        } catch (Exception e) { System.out.println("蟲子圖片載入失敗，請確認圖檔路徑"); }
    }

    private void buildVisuals() {
        root = new StackPane(); root.setStyle("-fx-background-color: #0b0c10;");
        matrixBg = new Label(); matrixBg.setTextFill(Color.rgb(0, 255, 204, 0.15)); matrixBg.setFont(Font.font("Consolas", 12)); matrixBg.setAlignment(Pos.TOP_LEFT);
        matrixBg.setEffect(new GaussianBlur(1.5));

        Label crtOverlay = new Label(); crtOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); crtOverlay.setStyle("-fx-background-color: repeating-linear-gradient(0deg, rgba(0,0,0,0) 0px, rgba(0,0,0,0) 1px, rgba(0,255,0,0.03) 2px, rgba(0,255,0,0.03) 3px);"); crtOverlay.setMouseTransparent(true);

        menuLayer = new StackPane(); menuLayer.setStyle("-fx-background-color: rgba(11, 12, 16, 0.95);");
        VBox menuBox = new VBox(20); menuBox.setAlignment(Pos.CENTER);

        versionLabel = new Label("CONNECTION: ACTIVE // PROTOCOL: NEON_CORE_v2.85"); versionLabel.setTextFill(Color.rgb(0, 255, 204, 0.6)); versionLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        Label title = new Label("NEON BREACH"); title.setTextFill(Color.CYAN); title.setFont(Font.font("Impact", 75)); title.setEffect(neonGlowCyan);
        highScoreDisplay = new Label("HIGHEST LAYER: " + p.highScore + "  |  LEGACY COINS: " + p.legacyCoins + " ¢"); highScoreDisplay.setTextFill(Color.LIME); highScoreDisplay.setFont(Font.font("Consolas", 20));
        systemStatusLabel = new Label("[ SYSTEM STATUS: IDLE - READY FOR INJECTION ]"); systemStatusLabel.setTextFill(Color.YELLOW); systemStatusLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));

        Button btnStart = createStyledButton(">>> INITIATE HACK <<<"); btnStart.setOnAction(e -> app.startIntroSequence());
        Button btnOpenTalents = createStyledButton(">>> CYBER TALENTS <<<"); setupNeonButtonAnimation(btnOpenTalents, "#FF007F", "rgba(255, 0, 127, 0.2)"); btnOpenTalents.setOnAction(e -> app.openTalentTree());
        Button btnExit = createStyledButton(">>> DISCONNECT SYSTEM <<<"); setupNeonButtonAnimation(btnExit, "#555555", "rgba(85, 85, 85, 0.1)"); btnExit.setOnAction(e -> System.exit(0));

        bootWarningLabel = new Label("⚠ WARNING: LOCAL SUBNET REPORTING QUANTUM FLUCTUATIONS ⚠"); bootWarningLabel.setTextFill(Color.rgb(255, 50, 50, 0.6)); bootWarningLabel.setFont(Font.font("Consolas", 11));

        Label menuVolLabel = new Label("SYS_VOL:"); menuVolLabel.setTextFill(Color.CYAN); menuVolLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        menuVolumeSlider = new Slider(0, 1, 0.5); menuVolumeSlider.setMaxWidth(200); menuVolumeSlider.setStyle("-fx-cursor: hand;");
        HBox menuVolBox = new HBox(10, menuVolLabel, menuVolumeSlider); menuVolBox.setAlignment(Pos.CENTER);

        menuBox.getChildren().addAll(versionLabel, title, highScoreDisplay, systemStatusLabel, btnStart, btnOpenTalents, btnExit, bootWarningLabel, menuVolBox); menuLayer.getChildren().add(menuBox);

        FadeTransition statusBlink = new FadeTransition(Duration.millis(800), systemStatusLabel); statusBlink.setFromValue(1.0); statusBlink.setToValue(0.3); statusBlink.setCycleCount(Animation.INDEFINITE); statusBlink.setAutoReverse(true); statusBlink.play();
        FadeTransition warningBlink = new FadeTransition(Duration.millis(1200), bootWarningLabel); warningBlink.setFromValue(0.6); warningBlink.setToValue(0.2); warningBlink.setCycleCount(Animation.INDEFINITE); warningBlink.setAutoReverse(true); warningBlink.play();

        introLayer = new StackPane(); introLayer.setStyle("-fx-background-color: black;"); Label introText = new Label(); introText.setTextFill(Color.LIME); introText.setFont(Font.font("Consolas", 24)); introLayer.getChildren().add(introText); introLayer.setVisible(false);

        uiBorder = new Label("╔════════════════════════════════════════════╗\n║                                            ║\n║                                            ║\n║                                            ║\n╚════════════════════════════════════════════╝");
        uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5)); uiBorder.setFont(Font.font("Consolas", 20)); uiBorder.setAlignment(Pos.CENTER);
        VBox gameBox = new VBox(10); gameBox.setAlignment(Pos.CENTER);
        statusLabel = new Label(""); statusLabel.setTextFill(Color.CYAN); statusLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        glitchWarningLabel = new Label(" [系統狀態：傳輸環境安全]"); glitchWarningLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 15)); glitchWarningLabel.setTextFill(Color.LIME);
        progressDisplay = new Label("LEVEL 1 [....☼....☼....☼....] 0%"); progressDisplay.setTextFill(Color.LIME); progressDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 28));
        comboDisplay = new Label("COMBO: x1.0"); comboDisplay.setTextFill(Color.YELLOW); comboDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        gameBox.getChildren().addAll(statusLabel, glitchWarningLabel, comboDisplay, progressDisplay); gameLayer = new StackPane(uiBorder, gameBox); gameLayer.setVisible(false);
        skillDisplay = new Label("[1] EMP: 0   [2] SLOW: 0"); skillDisplay.setTextFill(Color.WHITE); skillDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 16)); StackPane.setAlignment(skillDisplay, Pos.BOTTOM_LEFT); gameLayer.getChildren().add(skillDisplay);

        honeypotBtn = new Button("⚠ [NODE VULNERABILITY] CLICK FOR 300 ¢"); honeypotBtn.setStyle("-fx-background-color: #330000; -fx-text-fill: #FF3333; -fx-border-color: red; -fx-cursor: hand;"); StackPane.setAlignment(honeypotBtn, Pos.TOP_RIGHT); honeypotBtn.setVisible(false); honeypotBtn.setOnAction(e -> app.handleHoneypotTrap()); gameLayer.getChildren().add(honeypotBtn);

        buildEventLayers(); buildRouteLayer(); buildShopLayer(); buildTalentLayerStructure();

        pauseLayer = new StackPane(); pauseLayer.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        VBox pauseBox = new VBox(20); pauseBox.setAlignment(Pos.CENTER); Label pauseTitle = new Label("SYSTEM PAUSED"); pauseTitle.setTextFill(Color.WHITE); pauseTitle.setFont(Font.font("Consolas", 40));

        Label pauseVolLabel = new Label("SYS_VOL:"); pauseVolLabel.setTextFill(Color.CYAN); pauseVolLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        pauseVolumeSlider = new Slider(0, 1, 0.5); pauseVolumeSlider.setMaxWidth(200); pauseVolumeSlider.setStyle("-fx-cursor: hand;");
        HBox pauseVolBox = new HBox(10, pauseVolLabel, pauseVolumeSlider); pauseVolBox.setAlignment(Pos.CENTER);

        menuVolumeSlider.valueProperty().bindBidirectional(pauseVolumeSlider.valueProperty());
        menuVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> { app.setBgmVolume(newVal.doubleValue()); });

        Button btnResume = createStyledButton("RESUME"); btnResume.setOnAction(e -> { engine.currentState = HackEngine.GameState.PLAYING; pauseLayer.setVisible(false); });
        Button btnMenuPause = createStyledButton("ABORT TO MENU"); btnMenuPause.setOnAction(e -> app.returnToMenu());

        pauseBox.getChildren().addAll(pauseTitle, pauseVolBox, btnResume, btnMenuPause); pauseLayer.getChildren().add(pauseBox); pauseLayer.setVisible(false);

        gameOverLayer = new StackPane(); gameOverLayer.setStyle("-fx-background-color: rgba(139, 0, 0, 0.95);");
        VBox overBox = new VBox(20); overBox.setAlignment(Pos.CENTER);
        gameOverReasonLabel = new Label(""); gameOverReasonLabel.setTextFill(Color.RED); gameOverReasonLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 45));

        gameOverStatsLabel = new Label(""); gameOverStatsLabel.setTextFill(Color.WHITE); gameOverStatsLabel.setFont(Font.font("Consolas", 18)); gameOverStatsLabel.setTextAlignment(TextAlignment.CENTER);

        Button btnRestart = createStyledButton("SYSTEM REBOOT"); btnRestart.setOnAction(e -> app.resetGame());
        Button btnMenuDead = createStyledButton("RETURN TO MENU"); btnMenuDead.setOnAction(e -> app.returnToMenu());
        overBox.getChildren().addAll(gameOverReasonLabel, gameOverStatsLabel, btnRestart, btnMenuDead); gameOverLayer.getChildren().add(overBox); gameOverLayer.setVisible(false);

        errorImage1 = loadEmergeErrorImage("error1.jpg"); errorImage2 = loadEmergeErrorImage("error2.jpg");

        flashOverlay = new Rectangle(800, 600, Color.TRANSPARENT); flashOverlay.setMouseTransparent(true);
        scanline = new Rectangle(800, 6, Color.CYAN); scanline.setVisible(false); scanline.setMouseTransparent(true);

        root.getChildren().addAll(matrixBg, gameLayer, crtOverlay, firewallLayer, interceptLayer, decryptLayer, bugCatchLayer, routeLayer, shopLayer, introLayer, gameOverLayer, talentLayer, pauseLayer, menuLayer, errorImage1, errorImage2, flashOverlay, scanline);
    }

    public void playSweepTransition(Color color) {
        scanline.setFill(color);
        if(color.equals(Color.CYAN)) scanline.setEffect(neonGlowCyan); else if (color.equals(Color.web("#FF007F"))) scanline.setEffect(neonGlowPink); else scanline.setEffect(neonGlowGreen);
        scanline.setVisible(true); scanline.setTranslateY(-300);
        TranslateTransition sweep = new TranslateTransition(Duration.millis(350), scanline); sweep.setToY(300);
        FadeTransition ft = new FadeTransition(Duration.millis(350), scanline); ft.setFromValue(1.0); ft.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(sweep, ft); pt.setOnFinished(e -> scanline.setVisible(false)); pt.play();
    }

    public void playPulseEffect() {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), root); st.setFromX(1.0); st.setFromY(1.0); st.setToX(1.015); st.setToY(1.015); st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    public void shakeScreen() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(40), root); tt.setFromX(0f); tt.setByX(5f); tt.setCycleCount(4); tt.setAutoReverse(true); tt.playFromStart();
    }

    public void playFlashEffect(Color c, double durationMillis) {
        flashOverlay.setFill(c); FadeTransition ft = new FadeTransition(Duration.millis(durationMillis), flashOverlay); ft.setFromValue(0.25); ft.setToValue(0.0); ft.play();
    }

    private void setupNeonButtonAnimation(Button btn, String primaryColor, String glowBgColor) {
        btn.setStyle("-fx-background-color: black; -fx-text-fill: " + primaryColor + "; -fx-border-color: " + primaryColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;");
        DropShadow btnGlow = new DropShadow(8, Color.web(primaryColor));
        btn.setOnMouseEntered(e -> { btn.setEffect(btnGlow); btn.setStyle("-fx-background-color: " + glowBgColor + "; -fx-text-fill: white; -fx-border-color: " + primaryColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;"); ScaleTransition st = new ScaleTransition(Duration.millis(100), btn); st.setToX(1.05); st.setToY(1.05); st.play(); });
        btn.setOnMouseExited(e -> { btn.setEffect(null); btn.setStyle("-fx-background-color: black; -fx-text-fill: " + primaryColor + "; -fx-border-color: " + primaryColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;"); ScaleTransition st = new ScaleTransition(Duration.millis(100), btn); st.setToX(1.0); st.setToY(1.0); st.play(); });
        btn.setOnMousePressed(e -> { ScaleTransition st = new ScaleTransition(Duration.millis(50), btn); st.setToX(0.95); st.setToY(0.95); st.play(); });
        btn.setOnMouseReleased(e -> { ScaleTransition st = new ScaleTransition(Duration.millis(50), btn); st.setToX(1.05); st.setToY(1.05); st.play(); });
    }

    public void triggerErrorEffect(ImageView errorImg, int type) {
        if (errorImg == null || errorImg.getImage() == null) return;
        errorImg.setVisible(true); errorImg.setOpacity(0.0); errorImg.setScaleX(0.0); errorImg.setScaleY(0.0);
        app.playErrorSound(type); shakeScreen(); playFlashEffect(Color.rgb(255, 0, 0, 0.3), 300);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), errorImg); fadeIn.setFromValue(0.0); fadeIn.setToValue(0.6);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), errorImg); scaleUp.setFromX(0.0); scaleUp.setFromY(0.0); scaleUp.setToX(1.0); scaleUp.setToY(1.0);
        ParallelTransition emerge = new ParallelTransition(fadeIn, scaleUp); PauseTransition hold = new PauseTransition(Duration.millis(400));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), errorImg); fadeOut.setFromValue(0.6); fadeOut.setToValue(0.0);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), errorImg); scaleDown.setToX(0.0); scaleDown.setToY(0.0);
        ParallelTransition submerge = new ParallelTransition(fadeOut, scaleDown);
        SequentialTransition seq = new SequentialTransition(emerge, hold, submerge); seq.setOnFinished(e -> errorImg.setVisible(false)); seq.play();
    }

    public void updateFirewallUI() { int bars = (int) Math.max(0, Math.min(20, engine.firewallProgress * 20)); int dots = 20 - bars; firewallBarDisplay.setText("[" + "|".repeat(bars) + ".".repeat(dots) + "]"); }

    public void playFirewallSpacePopEffect() {
        firewallBarDisplay.setTextFill(Color.WHITE); ScaleTransition st = new ScaleTransition(Duration.millis(60), firewallBarDisplay); st.setFromX(1.06); st.setFromY(1.06); st.setToX(1.0); st.setToY(1.0); st.setOnFinished(e -> { firewallBarDisplay.setTextFill(Color.web("#33CCFF")); }); st.play();
    }

    private ImageView loadEmergeErrorImage(String fileName) {
        ImageView iv = new ImageView();
        try { iv.setImage(new Image(getClass().getResource("/" + fileName).toExternalForm())); iv.setFitWidth(800); iv.setFitHeight(600); iv.setPreserveRatio(false); } catch (Exception e) {}
        iv.setOpacity(0.0); iv.setScaleX(0.0); iv.setScaleY(0.0); iv.setVisible(false); iv.setMouseTransparent(true); return iv;
    }

    private void buildEventLayers() {
        firewallLayer = new StackPane(); firewallLayer.setStyle("-fx-background-color: rgba(0, 40, 120, 0.55);"); VBox fwBox = new VBox(10); fwBox.setAlignment(Pos.CENTER); Label fwTitle = new Label("- FIREWALL BREAK ATTEMPT -"); fwTitle.setTextFill(Color.CYAN); fwTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        firewallBarDisplay = new Label("[||||||||||..........]"); firewallBarDisplay.setTextFill(Color.web("#33CCFF")); firewallBarDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 40)); firewallBarDisplay.setEffect(neonGlowCyan);
        fwBox.getChildren().addAll(fwTitle, new Label("⚡ MASH SPACEBAR NOW ⚡"), firewallBarDisplay); firewallLayer.getChildren().add(fwBox); StackPane.setAlignment(fwBox, Pos.TOP_CENTER); fwBox.setTranslateY(100); firewallLayer.setVisible(false);

        interceptLayer = new StackPane(); interceptLayer.setStyle("-fx-background-color: rgba(70, 0, 110, 0.7);"); VBox intBox = new VBox(10); intBox.setAlignment(Pos.CENTER); Label altTitle = new Label("! INTERCEPT !"); altTitle.setTextFill(Color.ORANGE); altTitle.setFont(Font.font("Consolas", 30));
        interceptTargetDisplay = new HBox(15); interceptTargetDisplay.setAlignment(Pos.CENTER);
        interceptTimeDisplay = new Label("Time left: 3.0s"); interceptTimeDisplay.setTextFill(Color.WHITE); intBox.getChildren().addAll(altTitle, interceptTargetDisplay, interceptTimeDisplay); interceptLayer.getChildren().add(intBox); StackPane.setAlignment(intBox, Pos.BOTTOM_CENTER); intBox.setTranslateY(-100); interceptLayer.setVisible(false);

        decryptLayer = new StackPane(); decryptLayer.setStyle("-fx-background-color: rgba(0, 40, 0, 0.9);"); VBox decBox = new VBox(15); decBox.setAlignment(Pos.CENTER); Label decTitle = new Label("??? ENCRYPTED NODE ???"); decTitle.setTextFill(Color.LIME); decTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 35)); decryptTargetDisplay = new Label("MEMORIZE THIS"); decryptTargetDisplay.setTextFill(Color.WHITE); decryptTargetDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 50)); decryptTargetDisplay.setEffect(neonGlowGreen); decryptInputDisplay = new Label("> _"); decryptInputDisplay.setTextFill(Color.CYAN); decryptInputDisplay.setFont(Font.font("Consolas", 40)); decryptTimeDisplay = new Label("Time left: 4.0s"); decryptTimeDisplay.setTextFill(Color.WHITE); decBox.getChildren().addAll(decTitle, decryptTargetDisplay, decryptInputDisplay, decryptTimeDisplay); decryptLayer.getChildren().add(decBox); decryptLayer.setVisible(false);

        // === 新增：打蟲子關卡 UI ===
        bugCatchLayer = new StackPane();
        bugCatchLayer.setStyle("-fx-background-color: rgba(30, 0, 0, 0.85);");
        bugCatchPane = new AnchorPane();
        bugCatchPane.setPrefSize(800, 600); // 負責讓蟲子隨機座標

        VBox bugInfoBox = new VBox(10);
        bugInfoBox.setAlignment(Pos.TOP_CENTER);
        Label bugTitle = new Label("- INVASIVE SWARM DETECTED -");
        bugTitle.setTextFill(Color.RED);
        bugTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        bugTitle.setEffect(new DropShadow(10, Color.RED));

        bugScoreLabel = new Label("TARGET BUGS: 0 / 5");
        bugScoreLabel.setTextFill(Color.LIME);
        bugScoreLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        bugScoreLabel.setEffect(neonGlowGreen);

        bugTimeLabel = new Label("Time left: 15.0s");
        bugTimeLabel.setTextFill(Color.WHITE);
        bugTimeLabel.setFont(Font.font("Consolas", 24));

        bugInfoBox.getChildren().addAll(bugTitle, bugScoreLabel, bugTimeLabel);
        bugCatchLayer.getChildren().addAll(bugInfoBox, bugCatchPane);
        StackPane.setAlignment(bugInfoBox, Pos.TOP_CENTER);
        bugInfoBox.setTranslateY(80);
        bugCatchLayer.setVisible(false);
    }

    // === 新增：刷新畫面上的標靶與障礙物 ===
    public void spawnBugsForEvent() {
        bugCatchPane.getChildren().clear();
        if (targetBugImg == null || obstacleBugImgs == null) return;

        double maxX = 650; // 安全範圍
        double maxY = 400;

        // 1. 生成 1 隻標靶
        ImageView targetView = new ImageView(targetBugImg);
        targetView.setFitWidth(80); targetView.setFitHeight(80);
        targetView.setLayoutX(50 + engine.random.nextDouble() * maxX);
        targetView.setLayoutY(150 + engine.random.nextDouble() * maxY);
        targetView.setStyle("-fx-cursor: hand;");

        targetView.setOnMouseClicked(e -> {
            engine.bugsCaught++;
            app.playSuccessSound();
            bugCatchPane.getChildren().remove(targetView);
            updateBugScoreUI();
            if (engine.bugsCaught >= 5) {
                engine.isBugCatchFight = false;
                bugCatchLayer.setVisible(false);
                typeWriterUpdate(">>> BUG SWARM CLEARED.");
                engine.currentSegment++;
            }
            e.consume(); // 防止點擊穿透觸發 hacking
        });

        // 2. 生成 1~3 隻隨機障礙蟲
        int obsCount = engine.random.nextInt(3) + 1;
        List<ImageView> obsList = new ArrayList<>();
        for (int i = 0; i < obsCount; i++) {
            ImageView obsView = new ImageView(obstacleBugImgs[engine.random.nextInt(obstacleBugImgs.length)]);
            obsView.setFitWidth(80); obsView.setFitHeight(80);
            obsView.setLayoutX(50 + engine.random.nextDouble() * maxX);
            obsView.setLayoutY(150 + engine.random.nextDouble() * maxY);
            obsView.setStyle("-fx-cursor: hand;");

            obsView.setOnMouseClicked(e -> {
                if (engine.bugsCaught > 0) engine.bugsCaught--; // 扣分
                updateBugScoreUI();
                bugCatchPane.getChildren().remove(obsView);
                triggerErrorEffect(errorImage2, 2); // 觸發 error2 升級失敗同款影音
                e.consume();
            });
            obsList.add(obsView);
        }

        bugCatchPane.getChildren().addAll(obsList);
        bugCatchPane.getChildren().add(targetView); // 目標放在最上層避免被完全遮擋
    }

    public void updateBugScoreUI() {
        bugScoreLabel.setText("TARGET BUGS: " + engine.bugsCaught + " / 5");
    }

    private void buildRouteLayer() {
        routeLayer = new StackPane(); routeLayer.setStyle("-fx-background-color: rgba(10, 30, 10, 0.95);"); VBox routeBox = new VBox(20); routeBox.setAlignment(Pos.CENTER); Label title = new Label(">>> SELECT NEXT NODE <<<"); title.setTextFill(Color.LIME); title.setFont(Font.font("Consolas", 35)); title.setEffect(neonGlowGreen); HBox btnBox = new HBox(30); btnBox.setAlignment(Pos.CENTER);
        Button btnNormal = createStyledButton("廢棄學術伺服器\n(難度: 0.8x | 獎勵: 0.7x)"); btnNormal.setOnAction(e -> { p.routeDiffMult = 0.8; p.routeRewardMult = 0.7; app.enterShop(); });
        Button btnHard = createStyledButton("高風險金融節點\n(難度: 1.5x | 獎勵: 2.0x)"); btnHard.setOnAction(e -> { p.routeDiffMult = 1.5; p.routeRewardMult = 2.0; app.enterShop(); });
        setupNeonButtonAnimation(btnNormal, "#00FFCC", "rgba(0, 255, 204, 0.15)"); setupNeonButtonAnimation(btnHard, "#FF3333", "rgba(255, 51, 51, 0.15)");
        btnBox.getChildren().addAll(btnNormal, btnHard); routeBox.getChildren().addAll(title, btnBox); routeLayer.getChildren().add(routeBox); routeLayer.setVisible(false);
    }

    private void buildShopLayer() {
        shopLayer = new StackPane(); shopLayer.setStyle("-fx-background-color: #050505;"); VBox shopBox = new VBox(10); shopBox.setAlignment(Pos.CENTER); Label shopTitle = new Label("--- BLACK MARKET ---"); shopTitle.setTextFill(Color.LIME); shopTitle.setFont(Font.font("Consolas", 40)); shopTitle.setEffect(neonGlowGreen); coinDisplay = new Label("DarkCoins: 0 ¢"); coinDisplay.setTextFill(Color.GOLD); coinDisplay.setFont(Font.font("Consolas", 25));
        Button btn1 = createShopButton("重磅封包 (Lv."+p.upgClick+") [Cost: 100¢]", 100); btn1.setOnAction(e -> { if(p.buy(100)) p.upgClick++; btn1.setText("重磅封包 (Lv."+p.upgClick+") [Cost: 100¢]"); updateShopUI(); });
        Button btn2 = createShopButton("注入加速 (Lv."+p.upgSpeed+") [Cost: 150¢]", 150); btn2.setOnAction(e -> { if(p.buy(150)) p.upgSpeed++; btn2.setText("注入加速 (Lv."+p.upgSpeed+") [Cost: 150¢]"); updateShopUI(); });
        Button btnEmp = createShopButton("EMP 脈衝彈 (炸 firewall) [Cost: 200¢]", 200); btnEmp.setOnAction(e -> { if(p.buy(200)) p.empCharges++; updateShopUI(); });
        Button btnSlow = createShopButton("超頻沙漏 (緩速破解) [Cost: 250¢]", 250); btnSlow.setOnAction(e -> { if(p.buy(250)) p.slowCharges++; updateShopUI(); });
        Button btnNext = createStyledButton(">>> INJECT PAYLOAD <<<"); btnNext.setOnAction(e -> { engine.currentState = HackEngine.GameState.PLAYING; shopLayer.setVisible(false); gameLayer.setVisible(true); engine.resetEvents(); app.checkBossLevel(); });
        shopBox.getChildren().addAll(shopTitle, coinDisplay, btn1, btn2, btnEmp, btnSlow, btnNext); shopLayer.getChildren().add(shopBox); shopLayer.setVisible(false);
    }

    private void buildTalentLayerStructure() {
        talentLayer = new StackPane(); talentLayer.setStyle("-fx-background-color: #0d0214;"); treeGroup = new Group(); StackPane treePane = new StackPane(treeGroup); treePane.setAlignment(Pos.CENTER); treePane.setPrefSize(700, 320); descBox = new VBox(5); descBox.setAlignment(Pos.CENTER); descBox.setStyle("-fx-background-color: #160826; -fx-border-color: #FF007F; -fx-border-width: 2; -fx-padding: 15; -fx-max-width: 550;"); talentNameLabel = new Label(">>> 點擊任意節點解密核心天賦 <<<"); talentNameLabel.setTextFill(Color.CYAN); talentNameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16)); talentEffectLabel = new Label("選取節點以加載組件加成數據。"); talentEffectLabel.setTextFill(Color.LIGHTGRAY); talentEffectLabel.setFont(Font.font("Consolas", 14)); talentCostLabel = new Label(""); talentCostLabel.setTextFill(Color.GOLD); talentCostLabel.setFont(Font.font("Consolas", 14));
        btnUpgradeTalent = createStyledButton(">>> 寫入天賦線路 <<<"); setupNeonButtonAnimation(btnUpgradeTalent, "#FF007F", "rgba(255, 0, 127, 0.2)"); btnUpgradeTalent.setVisible(false);
        descBox.getChildren().addAll(talentNameLabel, talentEffectLabel, talentCostLabel, btnUpgradeTalent); VBox talentLayout = new VBox(15); talentLayout.setAlignment(Pos.CENTER); Label title = new Label("== CYBERNETIC TALENT TREE =="); title.setTextFill(Color.web("#FF007F")); title.setFont(Font.font("Consolas", FontWeight.BOLD, 35)); title.setEffect(neonGlowPink); talentCoinDisplay = new Label("LEGACY COINS: 0 ¢"); talentCoinDisplay.setTextFill(Color.GOLD); talentCoinDisplay.setFont(Font.font("Consolas", 24)); Button btnBack = createStyledButton("<<< RETURN TO MENU"); btnBack.setOnAction(e -> app.closeTalentTree()); talentLayout.getChildren().addAll(title, talentCoinDisplay, treePane, descBox, btnBack); talentLayer.getChildren().add(talentLayout); talentLayer.setVisible(false); drawTalentTreeNodes();
    }

    private void drawTalentTreeNodes() {
        treeGroup.getChildren().clear(); Group lineLayer = new Group(); Group nodeLayer = new Group(); treeGroup.getChildren().addAll(lineLayer, nodeLayer); StackPane corePane = new StackPane(); corePane.setMinSize(80, 80); corePane.setMaxSize(80, 80); Circle coreCircle = new Circle(32); coreCircle.setStrokeWidth(3); coreCircle.setStroke(Color.CYAN); coreCircle.setFill(Color.rgb(0, 40, 50)); Label coreLabel = new Label("🌐"); coreLabel.setFont(Font.font("Segoe UI Emoji", 18)); coreLabel.setTextFill(Color.WHITE); corePane.getChildren().addAll(coreCircle, coreLabel); corePane.setTranslateX(-40); corePane.setTranslateY(-40); corePane.setEffect(neonGlowCyan); nodeLayer.getChildren().add(corePane); buildTalentBranch(1, 3, 270, 70, new Color[]{Color.CYAN, Color.rgb(0, 40, 50)}, 0, 0, "⚡", lineLayer, nodeLayer); buildTalentBranch(2, 5, 150, 70, new Color[]{Color.LIME, Color.rgb(0, 40, 0)}, 0, 0, "🔓", lineLayer, nodeLayer); buildTalentBranch(3, 3, 30, 70, new Color[]{Color.ORANGE, Color.rgb(40, 20, 0)}, 0, 0, "⏳", lineLayer, nodeLayer);
    }

    private void buildTalentBranch(int id, int maxLevel, double angle, double startRadius, Color[] colors, double parentX, double parentY, String iconSymbol, Group lineLayer, Group nodeLayer) {
        double currentParentX = parentX; double currentParentY = parentY; double labelR = startRadius + (maxLevel * 52) + 25; double lx = labelR * Math.cos(Math.toRadians(angle)); double ly = labelR * Math.sin(Math.toRadians(angle)); Label branchLabel = new Label(id == 1 ? "[ EMP MOD ]" : (id == 2 ? "[ FW BYPASS ]" : "[ BUFFER EXP ]")); branchLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 10)); branchLabel.setTextFill(colors[0]); branchLabel.setAlignment(Pos.CENTER); branchLabel.setPrefWidth(150); StackPane labelPane = new StackPane(branchLabel); labelPane.setTranslateX(lx - 75); labelPane.setTranslateY(ly - 10); nodeLayer.getChildren().add(labelPane);
        for (int i = 1; i <= maxLevel; i++) {
            double r = startRadius + (i-1) * 52; double x = r * Math.cos(Math.toRadians(angle)); double y = r * Math.sin(Math.toRadians(angle)); Color strokeColor = unlocked(id, i) ? colors[0] : (isNextAvailable(id, i) ? Color.LIGHTGRAY : Color.rgb(60, 60, 60)); Color fillColor = unlocked(id, i) ? colors[1] : (isNextAvailable(id, i) ? Color.rgb(30, 30, 35) : Color.rgb(15, 15, 15)); Line line = new Line(currentParentX, currentParentY, x, y); line.setStrokeWidth(3); line.setStroke(unlocked(id, i) ? colors[0] : Color.rgb(70, 70, 70)); lineLayer.getChildren().add(line); StackPane nodePane = new StackPane(); nodePane.setMinSize(40, 40); nodePane.setMaxSize(40, 40); Circle c = new Circle(18); c.setStrokeWidth(3); c.setStroke(strokeColor); c.setFill(fillColor); Button btn = new Button(iconSymbol); btn.setFont(Font.font("Segoe UI Emoji", 14)); btn.setTextFill(unlocked(id, i) ? Color.WHITE : (isNextAvailable(id, i) ? colors[0] : Color.rgb(80, 80, 80))); btn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;"); final int branchId = id; final int nodeLevel = i; btn.setOnAction(e -> app.selectTalentNode(branchId, nodeLevel)); nodePane.getChildren().addAll(c, btn); nodePane.setTranslateX(x - 20); nodePane.setTranslateY(y - 20);
            if (unlocked(id, i)) { DropShadow branchGlow = new DropShadow(10, colors[0]); nodePane.setEffect(branchGlow); }
            nodeLayer.getChildren().add(nodePane); currentParentX = x; currentParentY = y;
        }
    }

    public void updateComboDisplay(double multiplier) {
        comboDisplay.setText(String.format("COMBO: x%.1f", multiplier));
        if (multiplier >= 3.0) { comboDisplay.setTextFill(Color.web("#FF007F")); comboDisplay.setEffect(neonGlowPink); uiBorder.setStyle(engine.random.nextInt(3) == 0 ? "-fx-text-fill: #FF007F; -fx-effect: dropshadow(three-pass-box, #FF007F, 10, 0, 0, 0);" : "-fx-text-fill: rgb(0, 255, 204); -fx-effect: none;"); } else if (multiplier >= 2.0) { comboDisplay.setTextFill(Color.ORANGE); comboDisplay.setEffect(new DropShadow(10, Color.ORANGE)); uiBorder.setStyle("-fx-text-fill: orange;"); } else { comboDisplay.setTextFill(Color.YELLOW); comboDisplay.setEffect(null); uiBorder.setStyle("-fx-text-fill: rgba(0, 255, 204, 0.5);"); }
    }

    public void playComboHitEffect(double multiplier) { if (multiplier < 2.0) return; double intensity = (multiplier >= 3.0) ? 5.0 : 2.5; TranslateTransition tt = new TranslateTransition(Duration.millis(30), gameLayer); tt.setFromX((engine.random.nextDouble() - 0.5) * intensity); tt.setFromY((engine.random.nextDouble() - 0.5) * intensity); tt.setToX(0f); tt.setToY(0f); tt.playFromStart(); }
    public void playDescFadeIn() { FadeTransition ft = new FadeTransition(Duration.millis(250), descBox); ft.setFromValue(0.2); ft.setToValue(1.0); ft.play(); }

    public void updateGlitchDisplay() {
        if (engine.activeGlitch == HackEngine.GlitchType.NONE) { glitchWarningLabel.setText(" [系統狀態：傳輸環境安全]"); glitchWarningLabel.setTextFill(Color.LIME); glitchWarningLabel.setEffect(null); } else if (engine.activeGlitch == HackEngine.GlitchType.NETWORK_LAG) { glitchWarningLabel.setText("⚠ 環境詛咒：[NETWORK_LAG] 延遲嚴重 ⚠"); glitchWarningLabel.setTextFill(Color.ORANGE); glitchWarningLabel.setEffect(new DropShadow(8, Color.ORANGE)); } else if (engine.activeGlitch == HackEngine.GlitchType.VISUAL_DISTORTION) { glitchWarningLabel.setText("⚠ 環境詛咒：[VISUAL_DISTORTION] 視覺污染 ⚠"); glitchWarningLabel.setTextFill(Color.web("#FF007F")); glitchWarningLabel.setEffect(neonGlowPink); } else if (engine.activeGlitch == HackEngine.GlitchType.CORE_OVERLOAD) { glitchWarningLabel.setText("⚠ 環境詛咒：[CORE_OVERLOAD] 核心超載 ⚠"); glitchWarningLabel.setTextFill(Color.RED); glitchWarningLabel.setEffect(new DropShadow(12, Color.RED)); }
    }

    public void updateShopUI() { coinDisplay.setText("DarkCoins: " + p.darkCoins + " ¢"); skillDisplay.setText("[1] EMP: " + p.empCharges + "   [2] SLOW: " + p.slowCharges); }
    public void updateTalentUI() { talentCoinDisplay.setText("LEGACY COINS: " + p.legacyCoins + " ¢"); highScoreDisplay.setText("HIGHEST LAYER: " + p.highScore + "  |  LEGACY COINS: " + p.legacyCoins + " ¢"); drawTalentTreeNodes(); }

    public void updateInterceptUI() {
        interceptTargetDisplay.getChildren().clear(); String seq = engine.targetSequence;
        for (int i = 0; i < seq.length(); i++) {
            Label letterLabel = new Label(String.valueOf(seq.charAt(i))); letterLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 42));
            if (i < engine.sequenceIndex) { letterLabel.setTextFill(Color.web("#00FFCC")); letterLabel.setStyle("-fx-effect: dropshadow(three-pass-box, #00FFCC, 15, 0.6, 0, 0);"); if (i == engine.sequenceIndex - 1) { letterLabel.setScaleX(1.5); letterLabel.setScaleY(1.5); ScaleTransition st = new ScaleTransition(Duration.millis(120), letterLabel); st.setToX(1.0); st.setToY(1.0); st.play(); } } else if (i == engine.sequenceIndex) { letterLabel.setTextFill(Color.WHITE); letterLabel.setStyle("-fx-background-color: rgba(0, 255, 204, 0.25); -fx-border-color: #00FFCC; -fx-border-width: 2; -fx-padding: 0 8 0 8; -fx-border-radius: 4; -fx-background-radius: 4;"); letterLabel.setEffect(neonGlowCyan); } else { letterLabel.setTextFill(Color.web("#444444")); letterLabel.setStyle("-fx-effect: none;"); }
            interceptTargetDisplay.getChildren().add(letterLabel);
        }
    }

    public void updateDecryptUI() {
        decryptInputDisplay.setText("> " + engine.decryptInput + "_"); decryptInputDisplay.setTextFill(Color.web("#00FFCC")); decryptInputDisplay.setStyle("-fx-effect: dropshadow(three-pass-box, #00FFCC, 12, 0.4, 0, 0);");
        ScaleTransition st = new ScaleTransition(Duration.millis(100), decryptInputDisplay); st.setFromX(1.1); st.setFromY(1.1); st.setToX(1.0); st.setToY(1.0); st.play();
    }

    public void showGameOverStats(String reason, int level, int legacy, double maxCombo, int apm, double accuracy, String title) {
        gameOverReasonLabel.setText(reason);
        String stats = String.format("REACHED LAYER: %d\nMAX COMBO: x%.1f\nHACKING APM: %d\nACCURACY: %.1f%%\n\nCYBER TITLE EARNED:\n[ %s ]\n\nPERMANENT LEGACY COINS EARNED: +%d ¢", level, maxCombo, apm, accuracy, title, legacy);
        gameOverStatsLabel.setText(stats); gameOverLayer.setVisible(true); firewallLayer.setVisible(false); interceptLayer.setVisible(false); decryptLayer.setVisible(false); bugCatchLayer.setVisible(false);
    }

    private boolean unlocked(int id, int level) { if (id == 1) return p.talentStartEMP >= level; if (id == 2) return p.talentWeakFW >= level; if (id == 3) return p.talentFlashTime >= level; return false; }
    private boolean isNextAvailable(int id, int level) { if (id == 1) return p.talentStartEMP == level - 1; if (id == 2) return p.talentWeakFW == level - 1; if (id == 3) return p.talentFlashTime == level - 1; return false; }

    public void updateASCIIProgress() { StringBuilder sb = new StringBuilder("["); for (int i=1; i<=20; i++) { if (i <= (engine.progress*20)) sb.append("|"); else sb.append("."); } sb.append("] ").append((int)(engine.progress*100)).append("%"); progressDisplay.setText("LEVEL " + p.currentLevel + " " + sb.toString()); if (!engine.isHacking && !engine.isFirewallFight && !engine.isBugCatchFight) statusLabel.setText(">>> WARNING: LOSING PROGRESS... [RELEASED]"); else if (engine.isHacking) statusLabel.setText(">>> INJECTING... BREACHING LAYER " + (engine.currentSegment+1)); }
    public void typeWriterUpdate(String t) { this.currentTargetText = t; statusLabel.setText(t); }

    private Button createStyledButton(String text) { Button btn = new Button(text); setupNeonButtonAnimation(btn, "#00FFCC", "rgba(0, 255, 204, 0.15)"); return btn; }
    private Button createShopButton(String name, int cost) { Button btn = new Button(name); setupNeonButtonAnimation(btn, "#00FF00", "rgba(0, 255, 0, 0.12)"); return btn; }
}