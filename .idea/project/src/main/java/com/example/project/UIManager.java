package com.example.project;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class UIManager {
    private final PlayerStats p;
    private final HackEngine engine;
    private final HelloApplication app; // 用來呼叫主程式的邏輯

    // 所有對外的圖層與 UI 元件
    public StackPane root, menuLayer, introLayer, gameLayer, pauseLayer, firewallLayer, interceptLayer, decryptLayer, shopLayer, gameOverLayer, routeLayer, talentLayer;
    public Label progressDisplay, statusLabel, uiBorder, matrixBg, coinDisplay, comboDisplay, highScoreDisplay, talentCoinDisplay;
    public Label firewallBarDisplay, interceptTargetDisplay, interceptTimeDisplay, decryptTargetDisplay, decryptInputDisplay, decryptTimeDisplay;
    public Label gameOverReasonLabel, gameOverStatsLabel, skillDisplay;
    public Button honeypotBtn;
    public Button btnTalent1, btnTalent2, btnTalent3; // 天賦按鈕

    public String currentTargetText = "";

    public UIManager(PlayerStats p, HackEngine engine, HelloApplication app) {
        this.p = p;
        this.engine = engine;
        this.app = app;
        buildVisuals();
    }

    private void buildVisuals() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0b0c10;");

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
        VBox menuBox = new VBox(25);
        menuBox.setAlignment(Pos.CENTER);
        Label title = new Label("NEON BREACH");
        title.setTextFill(Color.CYAN);
        title.setFont(Font.font("Impact", 70));

        highScoreDisplay = new Label("HIGHEST LAYER: " + p.highScore + "  |  LEGACY COINS: " + p.legacyCoins + " ¢");
        highScoreDisplay.setTextFill(Color.LIME);
        highScoreDisplay.setFont(Font.font("Consolas", 20));

        Button btnStart = createStyledButton(">>> INITIATE HACK <<<");
        btnStart.setOnAction(e -> app.startIntroSequence()); // 呼叫 Controller 邏輯

        // 新增：天賦樹切換按鈕
        Button btnOpenTalents = createStyledButton(">>> CYBER TALENTS <<<");
        btnOpenTalents.setStyle("-fx-background-color: black; -fx-text-fill: #FF007F; -fx-border-color: #FF007F; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-cursor: hand;");
        btnOpenTalents.setOnAction(e -> app.openTalentTree());

        menuBox.getChildren().addAll(title, highScoreDisplay, btnStart, btnOpenTalents);
        menuLayer.getChildren().add(menuBox);

        // 開場動畫層
        introLayer = new StackPane();
        introLayer.setStyle("-fx-background-color: black;");
        Label introText = new Label();
        introText.setTextFill(Color.LIME);
        introText.setFont(Font.font("Consolas", 24));
        introLayer.getChildren().add(introText);
        introLayer.setVisible(false);

        // 主遊戲 UI
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

        skillDisplay = new Label("[1] EMP: 0   [2] SLOW: 0");
        skillDisplay.setTextFill(Color.WHITE);
        skillDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        StackPane.setAlignment(skillDisplay, Pos.BOTTOM_LEFT);
        gameLayer.getChildren().add(skillDisplay);

        honeypotBtn = new Button("⚠ [NODE VULNERABILITY] CLICK FOR 300 ¢");
        honeypotBtn.setStyle("-fx-background-color: #330000; -fx-text-fill: #FF3333; -fx-border-color: red; -fx-cursor: hand;");
        StackPane.setAlignment(honeypotBtn, Pos.TOP_RIGHT);
        honeypotBtn.setVisible(false);
        honeypotBtn.setOnAction(e -> app.handleHoneypotTrap());
        gameLayer.getChildren().add(honeypotBtn);

        buildEventLayers();
        buildRouteLayer();
        buildShopLayer();
        buildTalentLayer(); // 新增：渲染天賦樹畫面

        // 暫停層
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
        btnMenuPause.setOnAction(e -> app.returnToMenu());
        pauseBox.getChildren().addAll(pauseTitle, btnResume, btnMenuPause);
        pauseLayer.getChildren().add(pauseBox);
        pauseLayer.setVisible(false);

        // 死亡層
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
        btnRestart.setOnAction(e -> app.resetGame());
        Button btnMenuDead = createStyledButton("RETURN TO MENU");
        btnMenuDead.setOnAction(e -> app.returnToMenu());
        overBox.getChildren().addAll(gameOverReasonLabel, gameOverStatsLabel, btnRestart, btnMenuDead);
        gameOverLayer.getChildren().add(overBox);
        gameOverLayer.setVisible(false);

        root.getChildren().addAll(matrixBg, gameLayer, crtOverlay, firewallLayer, interceptLayer, decryptLayer, routeLayer, shopLayer, introLayer, gameOverLayer, talentLayer, pauseLayer, menuLayer);
    }

    private void buildEventLayers() {
        firewallLayer = new StackPane();
        firewallLayer.setStyle("-fx-background-color: rgba(0, 80, 255, 0.6);");
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

        decryptLayer = new StackPane();
        decryptLayer.setStyle("-fx-background-color: rgba(0, 50, 0, 0.85);");
        VBox decBox = new VBox(15);
        decBox.setAlignment(Pos.CENTER);
        Label decTitle = new Label("??? ENCRYPTED NODE ???");
        decTitle.setTextFill(Color.LIME);
        decTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 35));

        decryptTargetDisplay = new Label("MEMORIZE THIS");
        decryptTargetDisplay.setTextFill(Color.WHITE);
        decryptTargetDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 50));

        decryptInputDisplay = new Label("> _");
        decryptInputDisplay.setTextFill(Color.CYAN);
        decryptInputDisplay.setFont(Font.font("Consolas", 40));

        decryptTimeDisplay = new Label("Time left: 4.0s");
        decryptTimeDisplay.setTextFill(Color.WHITE);

        decBox.getChildren().addAll(decTitle, decryptTargetDisplay, decryptInputDisplay, decryptTimeDisplay);
        decryptLayer.getChildren().add(decBox);
        decryptLayer.setVisible(false);
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
        btnNormal.setOnAction(e -> { p.routeDiffMult = 0.8; p.routeRewardMult = 0.7; app.enterShop(); });

        Button btnHard = createStyledButton("高風險金融節點\n(難度: 1.5x | 獎勵: 2.0x)");
        btnHard.setStyle("-fx-background-color: #300; -fx-text-fill: #f55; -fx-border-color: red; -fx-font-family: 'Consolas'; -fx-font-size: 16px;");
        btnHard.setOnAction(e -> { p.routeDiffMult = 1.5; p.routeRewardMult = 2.0; app.enterShop(); });

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

        Button btn1 = createShopButton("重磅封包 (Lv."+p.upgClick+")", 100);
        btn1.setOnAction(e -> { if(p.buy(100)) p.upgClick++; btn1.setText("重磅封包 (Lv."+p.upgClick+")"); updateShopUI(); });

        Button btn2 = createShopButton("注入加速 (Lv."+p.upgSpeed+")", 150);
        btn2.setOnAction(e -> { if(p.buy(150)) p.upgSpeed++; btn2.setText("注入加速 (Lv."+p.upgSpeed+")"); updateShopUI(); });

        Button btnEmp = createShopButton("EMP 脈衝彈 (炸 firewall)", 200);
        btnEmp.setOnAction(e -> { if(p.buy(200)) p.empCharges++; updateShopUI(); });

        Button btnSlow = createShopButton("超頻沙漏 (緩速破解)", 250);
        btnSlow.setOnAction(e -> { if(p.buy(250)) p.slowCharges++; updateShopUI(); });

        Button btnNext = createStyledButton(">>> INJECT PAYLOAD <<<");
        btnNext.setOnAction(e -> {
            engine.currentState = HackEngine.GameState.PLAYING;
            shopLayer.setVisible(false);
            gameLayer.setVisible(true);
            engine.resetEvents();
            app.checkBossLevel();
        });

        shopBox.getChildren().addAll(shopTitle, coinDisplay, btn1, btn2, btnEmp, btnSlow, btnNext);
        shopLayer.getChildren().add(shopBox);
        shopLayer.setVisible(false);
    }

    // 新增：建構永久天賦畫面圖層
    private void buildTalentLayer() {
        talentLayer = new StackPane();
        talentLayer.setStyle("-fx-background-color: #0d0214;"); // 深紫色駭客風格
        VBox talentBox = new VBox(20);
        talentBox.setAlignment(Pos.CENTER);

        Label title = new Label("== CYBERNETIC TALENT TREE ==");
        title.setTextFill(Color.web("#FF007F"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 35));

        talentCoinDisplay = new Label("LEGACY COINS: 0 ¢");
        talentCoinDisplay.setTextFill(Color.GOLD);
        talentCoinDisplay.setFont(Font.font("Consolas", 24));

        btnTalent1 = createShopButton("控制組件優化", 50);
        btnTalent1.setOnAction(e -> app.upgradeTalent(1, 50));

        btnTalent2 = createShopButton("防火牆漏洞利用", 75);
        btnTalent2.setOnAction(e -> app.upgradeTalent(2, 75));

        btnTalent3 = createShopButton("緩衝記憶體擴充", 100);
        btnTalent3.setOnAction(e -> app.upgradeTalent(3, 100));

        Button btnBack = createStyledButton("<<< RETURN TO MENU");
        btnBack.setOnAction(e -> app.closeTalentTree());

        talentBox.getChildren().addAll(title, talentCoinDisplay, btnTalent1, btnTalent2, btnTalent3, btnBack);
        talentLayer.getChildren().add(talentBox);
        talentLayer.setVisible(false);
    }

    // --- UI 更新工具方法 ---
    public void updateShopUI() {
        coinDisplay.setText("DarkCoins: " + p.darkCoins + " ¢");
        skillDisplay.setText("[1] EMP: " + p.empCharges + "   [2] SLOW: " + p.slowCharges);
    }

    // 新增：更新天賦樹顯示
    public void updateTalentUI() {
        talentCoinDisplay.setText("LEGACY COINS: " + p.legacyCoins + " ¢");
        highScoreDisplay.setText("HIGHEST LAYER: " + p.highScore + "  |  LEGACY COINS: " + p.legacyCoins + " ¢");

        btnTalent1.setText("控制組件優化 (Lv." + p.talentStartEMP + "/3) [50 ¢]\n-> 開局自帶同等數量 EMP 彈");
        btnTalent2.setText("防火牆漏洞利用 (Lv." + p.talentWeakFW + "/5) [75 ¢]\n-> 降低防護牆初始厚度 " + (p.talentWeakFW * 5) + "%");
        btnTalent3.setText("緩衝記憶體擴充 (Lv." + p.talentFlashTime + "/3) [100 ¢]\n-> 延長解密閃現記憶時間 +" + String.format("%.1f", p.talentFlashTime * 0.15) + "s");
    }

    public void updateFirewallUI() { firewallBarDisplay.setText("[" + "|".repeat((int)(engine.firewallProgress*20)) + ".".repeat(20-(int)(engine.firewallProgress*20)) + "]"); }

    public void updateInterceptUI() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<engine.targetSequence.length(); i++) sb.append(i<engine.sequenceIndex ? "- " : engine.targetSequence.charAt(i)+" ");
        interceptTargetDisplay.setText(sb.toString().trim());
    }

    public void updateDecryptUI() { decryptInputDisplay.setText("> " + engine.decryptInput + "_"); }

    public void updateASCIIProgress() {
        StringBuilder sb = new StringBuilder("[");
        for (int i=1; i<=20; i++) {
            if (i <= (engine.progress*20)) sb.append("|"); else sb.append(".");
        }
        sb.append("] ").append((int)(engine.progress*100)).append("%");
        progressDisplay.setText("LEVEL " + p.currentLevel + " " + sb.toString());
        if (!engine.isHacking && !engine.isFirewallFight) statusLabel.setText(">>> WARNING: LOSING PROGRESS... [RELEASED]");
        else if (engine.isHacking) statusLabel.setText(">>> INJECTING... BREACHING LAYER " + (engine.currentSegment+1));
    }

    public void typeWriterUpdate(String t) {
        currentTargetText = t;
        statusLabel.setText(t);
    }

    public void shakeScreen() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), root);
        tt.setFromX(0f); tt.setByX(10f); tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.playFromStart();
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
}