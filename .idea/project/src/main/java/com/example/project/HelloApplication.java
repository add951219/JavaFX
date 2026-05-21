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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;
import java.util.prefs.Preferences;

public class HelloApplication extends Application {

    private enum GameState { MAIN_MENU, INTRO, PLAYING, PAUSED, SHOP, GAMEOVER }
    private GameState currentState = GameState.MAIN_MENU;

    // --- 核心進度變數 ---
    private boolean isHacking = false;
    private double progress = 0.0;
    private int currentLevel = 1;
    private int currentSegment = 0;
    private final int TOTAL_SEGMENTS = 4;

    // --- 資源與升級變數 ---
    private int darkCoins = 0;
    private int upgClick = 0;
    private int upgSpeed = 0;
    private int upgShield = 0;
    private int upgBot = 0;
    private int upgMine = 0;

    // --- 系統深度與紀錄 ---
    private double comboMultiplier = 1.0;
    private int comboFrames = 0;
    private int highScore = 0;


    // --- 事件狀態 ---
    private boolean isFirewallFight = false;
    private double firewallProgress = 0.5;
    private boolean isInterceptFight = false;
    private String targetSequence = "";
    private int sequenceIndex = 0;
    private long interceptDeadline = 0;
    private boolean isHoneypotActive = false;
    private long honeypotExpireTime = 0;
    private final Random random = new Random();

    // --- UI 與層級 ---
    private StackPane root;
    private Label progressDisplay, statusLabel, uiBorder, matrixBg;
    private StackPane menuLayer, introLayer, gameLayer, pauseLayer, firewallLayer, interceptLayer, shopLayer, gameOverLayer;
    private Label coinDisplay, comboDisplay, highScoreDisplay, firewallBarDisplay, interceptTargetDisplay, interceptTimeDisplay;
    private Label gameOverReasonLabel, gameOverStatsLabel; // 獨立出來以修復 Bug
    private Button honeypotBtn;
    private String currentTargetText = "";

    // --- 音效 ---
    private MediaPlayer bgmPlayer;
    private double currentVolume = 0.5;

    @Override
    public void start(Stage stage) {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0b0c10;");

        initAudio();
        buildVisuals();

        Scene scene = new Scene(root, 800, 600);
        setupInputHandlers(scene);
        startGameLoop();

        stage.setScene(scene);
        stage.setTitle("Neon Breach - Clear UI Edition");
        stage.show();
        root.requestFocus();
    }

    private void initAudio() {
        try {
            // File file = new File("src/main/resources/music.mp3");
            // bgmPlayer = new MediaPlayer(new Media(file.toURI().toString()));
            // bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            // bgmPlayer.setVolume(currentVolume);
            // bgmPlayer.play();
        } catch (Exception e) {
            System.out.println("找不到音樂檔案，略過 BGM 播放。");
        }
    }

    private void buildVisuals() {
        // --- 背景：Cyberpunk 矩陣網格 ---
        matrixBg = new Label();
        matrixBg.setTextFill(Color.rgb(0, 255, 204, 0.15));
        matrixBg.setFont(Font.font("Consolas", 12));
        matrixBg.setAlignment(Pos.TOP_LEFT);

        // 移除了粉紅光與 blooms，改為清爽的微弱綠色掃描線
        Label crtOverlay = new Label();
        crtOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        crtOverlay.setStyle("-fx-background-color: repeating-linear-gradient(0deg, rgba(0,0,0,0) 0px, rgba(0,0,0,0) 1px, rgba(0,255,0,0.03) 2px, rgba(0,255,0,0.03) 3px);");
        crtOverlay.setMouseTransparent(true);

        // --- 主選單層 ---
        menuLayer = new StackPane();
        menuLayer.setStyle("-fx-background-color: rgba(11, 12, 16, 0.95);");
        VBox menuBox = new VBox(30);
        menuBox.setAlignment(Pos.CENTER);
        Label title = new Label("NEON BREACH");
        title.setTextFill(Color.CYAN); // 改為清晰的青色
        title.setFont(Font.font("Impact", 70));

        highScoreDisplay = new Label("HIGHEST LAYER: " + highScore);
        highScoreDisplay.setTextFill(Color.LIME);
        highScoreDisplay.setFont(Font.font("Consolas", 20));

        Button btnStart = createStyledButton(">>> INITIATE HACK <<<");
        btnStart.setOnAction(e -> startIntroSequence());

        Slider volSlider = new Slider(0, 1, currentVolume);
        volSlider.setMaxWidth(200);
        volSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentVolume = newVal.doubleValue();
            if (bgmPlayer != null) bgmPlayer.setVolume(currentVolume);
        });
        Label volLabel = new Label("System Volume");
        volLabel.setTextFill(Color.WHITE);

        menuBox.getChildren().addAll(title, highScoreDisplay, btnStart, volLabel, volSlider);
        menuLayer.getChildren().add(menuBox);

        // --- 開場動畫層 ---
        introLayer = new StackPane();
        introLayer.setStyle("-fx-background-color: black;");
        Label introText = new Label();
        introText.setTextFill(Color.LIME);
        introText.setFont(Font.font("Consolas", 24));
        introLayer.getChildren().add(introText);
        introLayer.setVisible(false);

        // --- 遊戲 UI 層 ---
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

        // 蜜罐陷阱 (移除刺眼特效，維持紅色警告感)
        honeypotBtn = new Button("⚠ [NODE VULNERABILITY] CLICK FOR 300 ¢");
        honeypotBtn.setStyle("-fx-background-color: #330000; -fx-text-fill: #FF3333; -fx-border-color: red; -fx-cursor: hand;");
        StackPane.setAlignment(honeypotBtn, Pos.TOP_RIGHT);
        honeypotBtn.setVisible(false);
        honeypotBtn.setOnAction(e -> handleHoneypotTrap());
        gameLayer.getChildren().add(honeypotBtn);

        // --- 事件層 ---
        buildEventLayers();

        // --- 暫停層 ---
        pauseLayer = new StackPane();
        pauseLayer.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        VBox pauseBox = new VBox(20);
        pauseBox.setAlignment(Pos.CENTER);
        Label pauseTitle = new Label("SYSTEM PAUSED");
        pauseTitle.setTextFill(Color.WHITE);
        pauseTitle.setFont(Font.font("Consolas", 40));
        Button btnResume = createStyledButton("RESUME");
        btnResume.setOnAction(e -> { currentState = GameState.PLAYING; pauseLayer.setVisible(false); });
        Button btnMenuPause = createStyledButton("ABORT TO MENU");
        btnMenuPause.setOnAction(e -> returnToMenu());
        pauseBox.getChildren().addAll(pauseTitle, btnResume, btnMenuPause);
        pauseLayer.getChildren().add(pauseBox);
        pauseLayer.setVisible(false);

        // --- 商店層 ---
        buildShopLayer();

        // --- Game Over 層 (預先建構，解決無法點擊的 Bug) ---
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

        // 依序加入所有層，Z-order 順序不變
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
        interceptLayer.setStyle("-fx-background-color: rgba(100, 0, 150, 0.8);"); // 改為暗紅色背景
        VBox intBox = new VBox(20);
        intBox.setAlignment(Pos.CENTER);
        Label intTitle = new Label("!!! PACKET INTERCEPTED !!!");
        intTitle.setTextFill(Color.ORANGE); // 改為清晰的橘色
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
        btn1.setOnAction(e -> { if(buy(100)) upgClick++; btn1.setText("重磅封包 (Lv."+upgClick+")"); });

        Button btn2 = createShopButton("注入加速 (抵抗退速)", 150);
        btn2.setOnAction(e -> { if(buy(150)) upgSpeed++; btn2.setText("注入加速 (Lv."+upgSpeed+")"); });

        Button btn3 = createShopButton("ICE 護盾 (抵擋一次失誤)", 300);
        btn3.setOnAction(e -> { if(buy(300)) upgShield++; btn3.setText("ICE 護盾 (剩餘:"+upgShield+")"); });

        Button btn4 = createShopButton("幽靈 Bot (自動推進)", 500);
        btn4.setOnAction(e -> { if(buy(500)) upgBot++; btn4.setText("幽靈 Bot (Lv."+upgBot+")"); });

        Button btn5 = createShopButton("背景挖礦機 (被動收入)", 400);
        btn5.setOnAction(e -> { if(buy(400)) upgMine++; btn5.setText("背景挖礦機 (Lv."+upgMine+")"); });

        Button btnNext = createStyledButton(">>> NEXT LAYER <<<");
        btnNext.setOnAction(e -> {
            currentState = GameState.PLAYING;
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
            if (currentState == GameState.PLAYING && e.getButton() == MouseButton.PRIMARY && !isFirewallFight && !isInterceptFight) {
                isHacking = true;
            }
        });
        scene.setOnMouseReleased(e -> {
            if (currentState == GameState.PLAYING && e.getButton() == MouseButton.PRIMARY) {
                isHacking = false;
            }
        });

        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (currentState == GameState.PLAYING) {
                    currentState = GameState.PAUSED;
                    pauseLayer.setVisible(true);
                } else if (currentState == GameState.PAUSED) {
                    currentState = GameState.PLAYING;
                    pauseLayer.setVisible(false);
                }
            }

            if (currentState != GameState.PLAYING) return;

            if (e.getCode() == KeyCode.SPACE && isFirewallFight) {
                firewallProgress += 0.05 + (upgClick * 0.015);
                updateFirewallUI();
                e.consume();
            }
            if (isInterceptFight) {
                String inputKey = e.getCode().toString();
                String expectedKey = targetSequence.substring(sequenceIndex, sequenceIndex + 1);
                if (inputKey.equals(expectedKey)) {
                    sequenceIndex++;
                    updateInterceptUI();
                    if (sequenceIndex >= targetSequence.length()) {
                        isInterceptFight = false;
                        interceptLayer.setVisible(false);
                        typeWriterUpdate(">>> PACKET REROUTED. NODE SECURED.");
                        currentSegment++;
                    }
                }
                e.consume();
            }
        });
    }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (currentState != GameState.PLAYING) return;

            long now = System.nanoTime();
            if (random.nextInt(10) == 0) matrixBg.setText(generateRandomCode());

            if (upgMine > 0 && random.nextDouble() < 0.01) darkCoins += upgMine;

            if (!isFirewallFight && !isInterceptFight && isHacking && !isHoneypotActive && random.nextDouble() < 0.002) {
                isHoneypotActive = true;
                honeypotBtn.setVisible(true);
                honeypotExpireTime = now + 1_500_000_000L + random.nextInt(1_000_000_000);
            }
            if (isHoneypotActive && now > honeypotExpireTime) {
                isHoneypotActive = false;
                honeypotBtn.setVisible(false);
            }

            if (isHacking && !isFirewallFight && !isInterceptFight) {
                comboFrames++;
                comboMultiplier = Math.min(3.0, 1.0 + (comboFrames / 180.0));
            } else {
                comboFrames = 0;
                comboMultiplier = 1.0;
            }
            comboDisplay.setText(String.format("COMBO: x%.1f", comboMultiplier));

            if (isFirewallFight) {
                firewallProgress -= (0.003 + (currentLevel * 0.0008));
                updateFirewallUI();
                if (firewallProgress <= 0) triggerGameOver(">>> CONNECTION BLOCKED <<<");
                else if (firewallProgress >= 1.0) {
                    isFirewallFight = false;
                    firewallLayer.setVisible(false);
                    typeWriterUpdate(">>> FIREWALL NEUTRALIZED.");
                    currentSegment++;
                }
            } else if (isInterceptFight) {
                double timeLeft = (interceptDeadline - now) / 1_000_000_000.0;
                interceptTimeDisplay.setText(String.format("Time left: %.1fs", Math.max(0, timeLeft)));
                if (now > interceptDeadline) handleInterceptFailure();
            } else {
                double securedProgress = currentSegment * 0.25;
                double targetCheckpoint = (currentSegment + 1) * 0.25;

                progress += (upgBot * 0.0005);

                if (isHacking) {
                    progress += 0.0022 + (upgSpeed * 0.0006);
                    if (progress >= targetCheckpoint) {
                        progress = targetCheckpoint;
                        isHacking = false;
                        if (currentSegment < TOTAL_SEGMENTS - 1) triggerCheckpointEvent();
                        else playLevelClearExplosion();
                    }
                } else {
                    double bounceDecay = 0.0010 + (currentLevel * 0.0004);
                    progress -= bounceDecay;
                    if (progress < securedProgress) progress = securedProgress;
                }
                updateASCIIProgress();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void triggerCheckpointEvent() {
        shakeScreen();
        if (random.nextBoolean()) {
            isFirewallFight = true;
            firewallProgress = 0.5;
            firewallLayer.setVisible(true);
            updateFirewallUI();
        } else {
            isInterceptFight = true;
            sequenceIndex = 0;
            int len = 4 + (currentLevel / 2);
            String[] pool = {"W", "A", "S", "D"};
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<len; i++) sb.append(pool[random.nextInt(pool.length)]);
            targetSequence = sb.toString();
            updateInterceptUI();
            interceptDeadline = System.nanoTime() + (long)(Math.max(1.5, 4.5 - currentLevel*0.3) * 1_000_000_000L);
            interceptLayer.setVisible(true);
        }
    }

    private void handleHoneypotTrap() {
        isHoneypotActive = false;
        honeypotBtn.setVisible(false);
        if (upgShield > 0) {
            upgShield--;
            typeWriterUpdate(">>> HONEYPOT TRIGGERED! ICE SHIELD DEPLOYED. (Remaining: "+upgShield+")");
        } else {
            darkCoins = Math.max(0, darkCoins - 60);
            progress = Math.max(currentSegment * 0.25, progress - 0.08);
            shakeScreen();
            typeWriterUpdate(">>> HONEYPOT TRIGGERED! RESOURCES DRAINED!");
        }
    }

    private void handleInterceptFailure() {
        isInterceptFight = false;
        interceptLayer.setVisible(false);
        if (upgShield > 0) {
            upgShield--;
            typeWriterUpdate(">>> PACKET LOST! ICE SHIELD ABSORBED IMPACT.");
        } else {
            progress = currentSegment * 0.25;
            shakeScreen();
            typeWriterUpdate(">>> PACKET LOST! CRYPTO-BARRIER COLLAPSED.");
        }
    }

    private void playLevelClearExplosion() {
        currentState = GameState.PAUSED;
        isHacking = false;

        Timeline explosion = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            statusLabel.setText(generateRandomCode().substring(0, 40));
            uiBorder.setTextFill(Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
        }));
        explosion.setCycleCount(15);
        explosion.setOnFinished(e -> triggerLevelClear());
        explosion.play();
    }

    private void triggerLevelClear() {
        int earned = (int)((currentLevel * 100) * comboMultiplier);
        darkCoins += earned;
        currentLevel++;
        progress = 0.0;
        currentSegment = 0;

        if (currentLevel > highScore) {
            highScore = currentLevel;
        }

        coinDisplay.setText("DarkCoins: " + darkCoins + " ¢");
        uiBorder.setTextFill(Color.rgb(0, 255, 204, 0.5));

        currentState = GameState.SHOP;
        gameLayer.setVisible(false);
        shopLayer.setVisible(true);
    }

    // --- 修改此處：不再每次重生物件，單純切換顯示文字 ---
    private void triggerGameOver(String reason) {
        currentState = GameState.GAMEOVER;
        shakeScreen();

        // 更新早已建構好的 Label
        gameOverReasonLabel.setText(reason);
        gameOverStatsLabel.setText("REACHED LAYER: " + currentLevel);

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
        currentState = GameState.INTRO;
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
            currentState = GameState.PLAYING;
        }));
        introTimeline.play();
    }

    private void resetGame() {
        currentLevel = 1; progress = 0.0; currentSegment = 0; darkCoins = 0;
        upgClick = 0; upgSpeed = 0; upgShield = 0; upgBot = 0; upgMine = 0;

        // --- 【修復】重置所有事件與狀態 ---
        isFirewallFight = false;
        isInterceptFight = false;
        isHacking = false;
        isHoneypotActive = false;
        firewallProgress = 0.5; // 防火牆血量拉回一半
        comboFrames = 0;
        comboMultiplier = 1.0;

        // --- 【修復】確保所有事件的 UI 都在關閉狀態 ---
        firewallLayer.setVisible(false);
        interceptLayer.setVisible(false);
        honeypotBtn.setVisible(false);
        gameOverLayer.setVisible(false);

        gameLayer.setVisible(true);
        currentState = GameState.PLAYING;
    }

    private void returnToMenu() {
        pauseLayer.setVisible(false);
        gameLayer.setVisible(false);
        shopLayer.setVisible(false);
        gameOverLayer.setVisible(false);
        menuLayer.setVisible(true);
        highScoreDisplay.setText("HIGHEST LAYER: " + highScore);
        resetGame();
        currentState = GameState.MAIN_MENU;
    }

    private void updateFirewallUI() { firewallBarDisplay.setText("[" + "|".repeat((int)(firewallProgress*20)) + ".".repeat(20-(int)(firewallProgress*20)) + "]"); }
    private void updateInterceptUI() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<targetSequence.length(); i++) sb.append(i<sequenceIndex ? "- " : targetSequence.charAt(i)+" ");
        interceptTargetDisplay.setText(sb.toString().trim());
    }
    private void updateASCIIProgress() {
        StringBuilder sb = new StringBuilder("[");
        for (int i=1; i<=20; i++) {
            if (i <= (progress*20)) sb.append("|");
            else if (i%5==0 && i!=20) sb.append("☼");
            else sb.append(".");
        }
        sb.append("] ").append((int)(progress*100)).append("%");
        progressDisplay.setText("LEVEL " + currentLevel + " " + sb.toString());
        if (!isHacking && !isFirewallFight) statusLabel.setText(">>> WARNING: LOSING PROGRESS... [RELEASED]");
        else if (isHacking) statusLabel.setText(">>> INJECTING... BREACHING LAYER " + (currentSegment+1));
    }
    private void typeWriterUpdate(String t) { currentTargetText = t; statusLabel.setText(t); }
    private boolean buy(int c) { if(darkCoins>=c){darkCoins-=c; coinDisplay.setText("DarkCoins: "+darkCoins+" ¢"); return true;} return false; }
    private String generateRandomCode() { StringBuilder sb = new StringBuilder(); for(int i=0; i<1500; i++) sb.append((char)(random.nextInt(94)+33)); return sb.toString(); }

    public static void main(String[] args) { launch(); }
}