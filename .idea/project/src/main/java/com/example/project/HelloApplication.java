package com.example.project;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Random;

public class HelloApplication extends Application {

    private enum GameState { PLAYING, SHOP, GAMEOVER }
    private GameState currentState = GameState.PLAYING;

    private boolean isHacking = false;
    private double progress = 0.0;
    private int currentLevel = 1;

    private int darkCoins = 0;
    private int upgClick = 0;
    private int upgSpeed = 0;

    private boolean isFirewallFight = false;
    private double firewallProgress = 0.5;

    private long lastEventTime = 0;
    private long nextEventTime = 0;
    private int nextEventType = 0;

    private final Random random = new Random();

    private Label progressDisplay;
    private Label matrixBg;
    private Label statusLabel;
    private Label crtOverlay;
    private Label uiBorder;
    private String currentTargetText = "";

    private StackPane firewallLayer;
    private StackPane shopLayer;
    private Label firewallBarDisplay;
    private Label coinDisplay;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000100;");

        matrixBg = new Label();
        matrixBg.setTextFill(Color.rgb(0, 150, 0, 0.15));
        matrixBg.setFont(Font.font("Consolas", 12));
        matrixBg.setAlignment(Pos.TOP_LEFT);
        matrixBg.setMaxWidth(Double.MAX_VALUE);
        matrixBg.setMaxHeight(Double.MAX_VALUE);

        crtOverlay = new Label();
        crtOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        crtOverlay.setStyle("-fx-background-color: repeating-linear-gradient(0deg, rgba(0,0,0,0) 0px, rgba(0,0,0,0) 1px, rgba(0,30,0,0.05) 2px, rgba(0,30,0,0.05) 3px); -fx-effect: blooms(0.5);");
        crtOverlay.setMouseTransparent(true);

        uiBorder = new Label("╔════════════════════════════════════════════╗\n" +
                "║                                            ║\n" +
                "║                                            ║\n" +
                "║                                            ║\n" +
                "╚════════════════════════════════════════════╝");
        uiBorder.setTextFill(Color.rgb(0, 255, 0, 0.5));
        uiBorder.setFont(Font.font("Consolas", 20));
        uiBorder.setAlignment(Pos.CENTER);

        VBox mainBox = new VBox(15);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setMaxSize(600, 200);

        statusLabel = new Label("");
        statusLabel.setTextFill(Color.LIME);
        statusLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(580);
        typeWriterUpdate(">>> 系統核心連線... 長按 [滑鼠左鍵] 開始注入資料。");

        progressDisplay = new Label("LEVEL 1 [....................] 0%");
        progressDisplay.setTextFill(Color.LIME);
        progressDisplay.setFont(Font.font("Consolas", FontWeight.BOLD, 28));
        progressDisplay.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 10, 0, 0, 0);");

        mainBox.getChildren().addAll(statusLabel, progressDisplay);
        StackPane uiContainer = new StackPane(uiBorder, mainBox);

        firewallLayer = new StackPane();
        firewallLayer.setStyle("-fx-background-color: rgba(0, 80, 255, 0.6); -fx-effect: blooms(0.8);");
        VBox fwBox = new VBox(20);
        fwBox.setAlignment(Pos.CENTER);
        Label fwTitle = new Label("--- FIREWALL INTERCEPT ---");
        fwTitle.setTextFill(Color.CYAN);
        fwTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 35));
        Label fwSub = new Label("Mash [SPACEBAR] to Flood Network!");
        fwSub.setTextFill(Color.WHITE);
        fwSub.setFont(Font.font("Consolas", 20));
        firewallBarDisplay = new Label("[||||||||||..........]");
        firewallBarDisplay.setTextFill(Color.WHITE);
        firewallBarDisplay.setFont(Font.font("Consolas", 45));
        fwBox.getChildren().addAll(fwTitle, fwSub, firewallBarDisplay);
        firewallLayer.getChildren().add(fwBox);
        firewallLayer.setVisible(false);

        shopLayer = new StackPane();
        shopLayer.setStyle("-fx-background-color: #050505;");
        VBox shopBox = new VBox(20);
        shopBox.setAlignment(Pos.CENTER);

        Label shopTitle = new Label("--- THE BLACK MARKET ---");
        shopTitle.setTextFill(Color.LIME);
        shopTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 40));

        coinDisplay = new Label("DarkCoins: 0 ¢");
        coinDisplay.setTextFill(Color.GOLD);
        coinDisplay.setFont(Font.font("Consolas", 25));

        Button btnClick = createShopButton("重磅封包 (空白鍵威力)", 100);
        btnClick.setOnAction(e -> { if(buy(100)) upgClick++; updateShop(btnClick, null); });

        Button btnSpeed = createShopButton("注入加速 (下載速度)", 150);
        btnSpeed.setOnAction(e -> { if(buy(150)) upgSpeed++; updateShop(null, btnSpeed); });

        Button btnNext = new Button(">>> 繼續潛入下一層 <<<");
        btnNext.setStyle("-fx-background-color: black; -fx-text-fill: cyan; -fx-border-color: cyan; -fx-font-family: 'Consolas'; -fx-font-size: 20px;");
        btnNext.setOnAction(e -> {
            currentState = GameState.PLAYING;
            shopLayer.setVisible(false);
            uiContainer.setVisible(true);
            scheduleNextEvent(System.nanoTime());
            root.requestFocus();
        });

        shopBox.getChildren().addAll(shopTitle, coinDisplay, btnClick, btnSpeed, btnNext);
        shopLayer.getChildren().add(shopBox);
        shopLayer.setVisible(false);

        root.getChildren().addAll(matrixBg, uiContainer, crtOverlay, firewallLayer, shopLayer);

        Scene scene = new Scene(root, 800, 600);

        scene.setOnMousePressed(e -> {
            if (currentState != GameState.PLAYING) return;
            if (e.getButton() == MouseButton.PRIMARY) {
                isHacking = true;
                root.requestFocus();
            }
        });

        scene.setOnMouseReleased(e -> {
            if (currentState != GameState.PLAYING) return;
            if (e.getButton() == MouseButton.PRIMARY) {
                isHacking = false;
                if (!isFirewallFight) {
                    typeWriterUpdate(">>> PAUSED... Re-engage mouse button.");
                }
            }
        });

        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (currentState != GameState.PLAYING) return;
            if (e.getCode() == KeyCode.SPACE && isFirewallFight) {
                firewallProgress += 0.05 + (upgClick * 0.015);
                updateFirewallUI();
                e.consume();
            }
        });

        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastBgUpdate = 0;

            @Override
            public void handle(long now) {
                if (currentState == GameState.GAMEOVER) return;

                if (now - lastBgUpdate > 100_000_000) {
                    matrixBg.setText(generateRandomCode());
                    lastBgUpdate = now;
                }

                updateAesthetics(now);

                if (currentState == GameState.SHOP) return;

                if (!isFirewallFight) {
                    if (nextEventTime == 0) scheduleNextEvent(now);

                    if (now >= nextEventTime) {
                        if (nextEventType == 2) {
                            isFirewallFight = true;
                            firewallProgress = 0.5;
                            firewallLayer.setVisible(true);
                            updateFirewallUI();
                            lastEventTime = now;
                            root.requestFocus();
                        }
                        nextEventTime = 0;
                    }
                }
                else if (isFirewallFight) {
                    firewallProgress -= (0.002 + (currentLevel * 0.0005));
                    updateFirewallUI();

                    if (firewallProgress <= 0) {
                        triggerGameOver(">>> FAILED <<<\nFIREWALL BLOCKED CONNECTION.");
                    } else if (firewallProgress >= 1.0) {
                        isFirewallFight = false;
                        firewallLayer.setVisible(false);
                        typeWriterUpdate(">>> FIREWALL NEUTRALIZED.");
                        scheduleNextEvent(now);
                    }
                }

                if (isHacking && progress < 1.0 && !isFirewallFight) {
                    // 下載速度加成調高五倍
                    progress += 0.002 + (upgSpeed * 0.005);
                    updateASCIIProgress();
                }
            }
        };
        gameLoop.start();

        stage.setScene(scene);
        stage.setTitle("System Hack - Action Edition");
        stage.show();
        root.requestFocus();
    }

    private void scheduleNextEvent(long now) {
        nextEventTime = now + 3_000_000_000L + random.nextInt(2_000_000_000);
        double roll = random.nextDouble();
        if (roll < 0.4) nextEventType = 2; // 40% 機率遇到防火牆
        else nextEventType = 0;
    }

    private void updateAesthetics(long now) {
        double borderOpacity = 0.4 + (Math.sin(now / 200_000_000.0) * 0.2);
        if (isHacking && !isFirewallFight) {
            double r = Math.min(1.0, (currentLevel - 1) * 0.1);
            uiBorder.setTextFill(Color.color(r, 1.0 - r, 0.0, 0.6 + (progress * 0.4)));
        } else if (isFirewallFight) {
            uiBorder.setTextFill(Color.color(0.0, 0.8, 1.0, 0.8));
        } else {
            uiBorder.setTextFill(Color.rgb(0, 255, 0, borderOpacity));
        }

        if (!statusLabel.getText().equals(currentTargetText)) {
            int currentLen = statusLabel.getText().length();
            if (currentLen < currentTargetText.length()) {
                statusLabel.setText(currentTargetText.substring(0, currentLen + 1));
            }
        }
    }

    private void typeWriterUpdate(String text) {
        this.currentTargetText = text;
        if (!text.startsWith(statusLabel.getText().replace("▋", ""))) {
            statusLabel.setText("");
        }
    }

    private void updateASCIIProgress() {
        int bars = (int) (progress * 20);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) sb.append(i < bars ? "|" : ".");
        sb.append("] ").append((int)(progress * 100)).append("%");

        progressDisplay.setText("LEVEL " + currentLevel + " " + sb.toString());
        statusLabel.setText(">>> INTRUDING SYSTEM... BREACHING LAYER " + currentLevel + " [HOLDING]");

        if (progress >= 1.0) {
            triggerLevelClear();
        }
    }

    private void triggerLevelClear() {
        isHacking = false;
        int earned = currentLevel * 100;
        darkCoins += earned;
        currentLevel++;
        progress = 0.0;

        coinDisplay.setText("DarkCoins: " + darkCoins + " ¢");
        typeWriterUpdate(">>> LAYER CLEARED! Acquired " + earned + "¢. Entering Black Market...");

        currentState = GameState.SHOP;
        shopLayer.setVisible(true);
    }

    private void updateFirewallUI() {
        int bars = (int) (firewallProgress * 20);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) sb.append(i < bars ? "|" : ".");
        sb.append("]");
        firewallBarDisplay.setText(sb.toString());
    }

    private void triggerGameOver(String reason) {
        currentState = GameState.GAMEOVER;
        isHacking = false;

        StackPane gameOverLayer = new StackPane();
        gameOverLayer.setStyle("-fx-background-color: rgba(139, 0, 0, 0.9);");
        Label overText = new Label(reason);
        overText.setTextFill(Color.RED);
        overText.setFont(Font.font("Consolas", FontWeight.BOLD, 45));
        overText.setAlignment(Pos.CENTER);
        gameOverLayer.getChildren().add(overText);

        ((StackPane) firewallLayer.getParent()).getChildren().add(gameOverLayer);
        firewallLayer.setVisible(false);
        shopLayer.setVisible(false);
    }

    private Button createShopButton(String name, int cost) {
        Button btn = new Button(name + " [Cost: " + cost + "¢]");
        btn.setStyle("-fx-background-color: #111; -fx-text-fill: lime; -fx-border-color: lime; -fx-font-family: 'Consolas'; -fx-font-size: 16px;");
        return btn;
    }

    private boolean buy(int cost) {
        if (darkCoins >= cost) {
            darkCoins -= cost;
            coinDisplay.setText("DarkCoins: " + darkCoins + " ¢");
            return true;
        }
        return false;
    }

    private void updateShop(Button c, Button s) {
        if (c != null) c.setText("重磅封包 (Lv." + upgClick + ") [已升級]");
        if (s != null) s.setText("注入加速 (Lv." + upgSpeed + ") [已升級]");
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 1500; i++) sb.append((char)(random.nextInt(94) + 33));
        return sb.toString();
    }

    public static void main(String[] args) { launch(); }
}