package com.example.project;

public class PlayerStats {
    public int currentLevel = 1;
    public int darkCoins = 0;
    public int upgClick = 0;
    public int upgSpeed = 0;
    public int upgShield = 0;
    public int upgBot = 0;
    public int upgMine = 0;
    public int highScore = 0;

    public boolean buy(int cost) {
        if (darkCoins >= cost) {
            darkCoins -= cost;
            return true;
        }
        return false;
    }

    public void reset() {
        currentLevel = 1;
        darkCoins = 0;
        upgClick = 0;
        upgSpeed = 0;
        upgShield = 0;
        upgBot = 0;
        upgMine = 0;
    }
}