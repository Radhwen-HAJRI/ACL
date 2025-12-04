package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;  
import java.awt.Font;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import entity.Monster; 
import entity.Player;

public class GamePanel extends JPanel implements Runnable {

    public final int originalTileSize = 16;
    public final int scale = 2;

    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 16;

    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;

    public final int maxMap = 10;
    public int currentMap = 0;

    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    public Rectangle solidArea = new Rectangle(0,0,48,48);
    public int solidAreaDefaultX = 0;
    public int solidAreaDefaultY = 0;

    public enum GameState { MENU, PLAYING, GAMEOVER, WON }
    public GameState gameState = GameState.MENU;

    private String[] menuOptions = {"NEW GAME", "SOUND ON/OFF", "QUIT"};
    private int currentMenuIndex = 0; // 0=NEW, 1=SOUND, 2=QUIT

    

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;

    public Player player = new Player(this, keyH);

    public Monster[] monsters;
    int nbMonsters;

    int FPS = 60;
    Labyrinthe labyrinthM = new Labyrinthe(this);
    
    private BufferedImage heartFull, heartEmpty, menuBg;
    public SoundManager soundManager = new SoundManager();

    int squareX = 100;
    int squareY = 100;
    int speed = 4;

    boolean gameOver = false;  
    boolean gameWon = false;

    private int gameOverTimer = 0; 
    private final int gameOverDelay = 180;

    boolean transitioning = false;
    long transitionStart = 0;

    // Variables pour le message de pièces manquantes
    boolean showMissingCoinsMessage = false;
    String missingCoinsMessage = "";
    long missingCoinsMessageStart = 0;
    int missingCoinsMessageDuration = 2000; // 2 secondes

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        try {
            heartFull = ImageIO.read(getClass().getResourceAsStream("/ui/heart_full.png"));
            heartEmpty = ImageIO.read(getClass().getResourceAsStream("/ui/heart_empty.png"));
        } catch (IOException e) {
            heartFull = null;
            heartEmpty = null;
        }

        try {
            menuBg = ImageIO.read(getClass().getResourceAsStream("/tiles/menu_bg.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int centerCol = maxWorldCol / 2;
        int centerRow = maxWorldRow / 2;
        
        boolean found = false;
        for (int r = centerRow - 5; r <= centerRow + 5 && !found; r++) {
            for (int c = centerCol - 5; c <= centerCol + 5 && !found; c++) {
                if (labyrinthM.mapTileNum[currentMap][c][r] == 0) {
                    player.worldx = c * tileSize;
                    player.worldy = r * tileSize;
                    found = true;
                }
            }
        }

        player.worldx = (int) labyrinthM.getPointDepart().x;
        player.worldy = (int) labyrinthM.getPointDepart().y;

        Random rand = new Random();
        nbMonsters = 7 + rand.nextInt(8);
        spawnMonsters(); 
    }

    public boolean canMoveHere(int nextX, int nextY) {
        int leftCol = (nextX) / tileSize;
        int rightCol = (nextX + tileSize - 1) / tileSize;  
        int topRow = (nextY) / tileSize;
        int bottomRow = (nextY + tileSize - 1) / tileSize;

        if (leftCol < 0 || rightCol >= maxWorldCol || topRow < 0 || bottomRow >= maxWorldRow) {
            return false;
        }

        int tileNum1 = labyrinthM.mapTileNum[currentMap][leftCol][topRow]; 
        int tileNum2 = labyrinthM.mapTileNum[currentMap][rightCol][topRow]; 
        int tileNum3 = labyrinthM.mapTileNum[currentMap][leftCol][bottomRow]; 
        int tileNum4 = labyrinthM.mapTileNum[currentMap][rightCol][bottomRow]; 

        if (labyrinthM.tile[tileNum1] != null && labyrinthM.tile[tileNum1].collision) return false;
        if (labyrinthM.tile[tileNum2] != null && labyrinthM.tile[tileNum2].collision) return false;
        if (labyrinthM.tile[tileNum3] != null && labyrinthM.tile[tileNum3].collision) return false;
        if (labyrinthM.tile[tileNum4] != null && labyrinthM.tile[tileNum4].collision) return false;

        return true; 
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void spawnMonsters() {
        monsters = new Monster[nbMonsters];
        Random rand = new Random();

        for (int i = 0; i < nbMonsters; i++) {
            monsters[i] = new Monster(this);
            
            if (rand.nextBoolean()) {
                monsters[i].isChaser = true;
            }
            
            boolean foundSpot = false;
            while (!foundSpot) {
                int col = rand.nextInt(this.maxWorldCol);
                int row = rand.nextInt(this.maxWorldRow);
         
                int tileNum = this.labyrinthM.mapTileNum[currentMap][col][row];
                
                if (this.labyrinthM.tile[tileNum] != null && !this.labyrinthM.tile[tileNum].collision) {
                    
                    int distPlayerX = Math.abs((col * tileSize) - player.worldx);
                    int distPlayerY = Math.abs((row * tileSize) - player.worldy);

                    if (distPlayerX > tileSize * 5 || distPlayerY > tileSize * 5) {
                        monsters[i].worldx = col * this.tileSize;
                        monsters[i].worldy = row * this.tileSize;
                        foundSpot = true; 
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                if (remainingTime < 0)
                    remainingTime = 0;
                Thread.sleep((long) (remainingTime / 1000000));
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkPlayerAttack() {
        if (!player.state.equals("attacking") || player.hasHitThisSwing) {
            return;
        }

        int attackRange = this.tileSize * 1; 

        for (int i = 0; i < nbMonsters; i++) {
            if (monsters[i] != null && monsters[i].alive) {
                
                int dx = Math.abs(player.worldx - monsters[i].worldx);
                int dy = Math.abs(player.worldy - monsters[i].worldy);

                if (dx < attackRange && dy < attackRange) {
                    monsters[i].health--; 
                    player.hasHitThisSwing = true; 
                }
            }
        }
    }

    private void updateMenu() {
        if (keyH.upMenuPressed) {
            currentMenuIndex--;
            if (currentMenuIndex < 0) currentMenuIndex = menuOptions.length - 1;
            keyH.upMenuPressed = false;
        }
        if (keyH.downMenuPressed) {
            currentMenuIndex++;
            if (currentMenuIndex >= menuOptions.length) currentMenuIndex = 0;
            keyH.downMenuPressed = false;
        }
        if (keyH.enterPressed) {
            keyH.enterPressed = false;
            switch (currentMenuIndex) {
                case 0: 
                    gameState = GameState.PLAYING;
                    break;
                case 1: 
                    soundManager.toggleMute();
                    if (soundManager.isMuted()) {
                        menuOptions[1] = "SOUND OFF";  // État OFF
                    } else {
                        menuOptions[1] = "SOUND ON";   // État ON
                    }
                    break;
                case 2: System.exit(0); break;
            }
        }
    }

    public void update() {

        if (transitioning) {
            if (System.currentTimeMillis() - transitionStart > 1000) {

                currentMap++;

                player.health = 3;  
                player.invincibleCounter = 0;  
                soundManager.playWin();  

                labyrinthM.setPoints(); 

                player.worldx = (int) labyrinthM.getPointDepart().x;
                player.worldy = (int) labyrinthM.getPointDepart().y;

                spawnMonsters();

                transitioning = false;
            }
            return;
        }

        // Timer auto-retour menu après game over
        if (gameOver && gameState == GameState.GAMEOVER || gameWon && gameState == GameState.WON) {  
            gameOverTimer++;
            if (gameOverTimer >= gameOverDelay) {
                gameState = GameState.MENU;  //Retour menu
                gameOver = false; 
                gameWon = false;
                gameOverTimer = 0; 
                player.health = 3;  
                player.invincibleCounter = 0;  
                soundManager.playWin();  

                labyrinthM.setPoints();
                System.out.println("Retour auto au menu après délai !");
            }
        }
        
        if (gameState == GameState.MENU) {
            updateMenu();
            return;
        }
    
        if (gameOver || gameWon) {
            return; 
        }

        player.update();
        checkPlayerAttack(); 

        for (int i = 0; i < nbMonsters; i++) {
            if (monsters[i] != null) {
                if (monsters[i].health <= 0) {
                    monsters[i].alive = false;
                }
                if (monsters[i].alive) {
                    monsters[i].update();
                }
            }
        }

        int playerCol = player.worldx / tileSize;
        int playerRow = player.worldy / tileSize;
        int tileNum = labyrinthM.mapTileNum[currentMap][playerCol][playerRow];
        
        if (tileNum == 6) { 
            player.keyCount++; 
            labyrinthM.mapTileNum[currentMap][playerCol][playerRow] = 0; 
        }
        
        if (tileNum == 9) { 
            player.coinCount++;
            labyrinthM.mapTileNum[currentMap][playerCol][playerRow] = 0; 
            labyrinthM.removeCoin(playerCol, playerRow);
        }

        if (player.invincibleCounter == 0 && !player.state.equals("attacking")) {
            for (int i = 0; i < nbMonsters; i++) {
                if (monsters[i] != null && monsters[i].alive) {
                    
                    int dx = Math.abs(player.worldx - monsters[i].worldx);
                    int dy = Math.abs(player.worldy - monsters[i].worldy);
                    int hitRange = this.tileSize / 2; 

                    if (dx < hitRange && dy < hitRange) {  
                        player.health--;
                        player.invincibleCounter = player.invincibleDuration; 
                        
                        if (player.health <= 0) {
                            gameOver = true;
                            gameState = GameState.GAMEOVER;
                            soundManager.playLose(); 
                            return;
                        }
                        break; 
                    }
                }
            }
        }

        // Vérifie si le joueur est sur la case d'arrivée
        int dx = Math.abs(player.worldx - (int)labyrinthM.pointArrivee.x);
        int dy = Math.abs(player.worldy - (int)labyrinthM.pointArrivee.y);

        if (dx < this.tileSize && dy < this.tileSize && player.keyCount > 0) {
            if (currentMap == 1) {  // Seulement win sur map 2
                gameWon = true;
                gameState = GameState.WON;
                soundManager.playWin();
            } else if (currentMap == 0 && player.coinCount >= 10 && !transitioning) {
                transitioning = true;
                transitionStart = System.currentTimeMillis();
                soundManager.playWin();
                player.coinCount -= 10;
                showMissingCoinsMessage = false;
            } else if (currentMap == 0 && player.coinCount < 10) {
                showMissingCoinsMessage = true;
                missingCoinsMessage = "Il te manque " + (10 - player.coinCount) + " pièces !";
                missingCoinsMessageStart = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        labyrinthM.draw(g2);

        if (transitioning) {
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            return;
        }

        if (gameState == GameState.MENU) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);

            if (menuBg != null) g2.drawImage(menuBg, 0, 0, screenWidth, screenHeight, null);

            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48f));  // Gros bold
            String title = "RedOne Labyrinthe";
            int titleWidth = (int) g2.getFontMetrics().stringWidth(title);
            int titleX = (screenWidth - titleWidth) / 2;
            int titleY = 100; 
            
            // Ombre titre (effet 3D)
            g2.setColor(Color.GRAY);
            g2.drawString(title, titleX + 3, titleY + 3);
            g2.setColor(Color.BLACK);
            g2.drawString(title, titleX, titleY);

            g2.setFont(g2.getFont().deriveFont(32f));
            for (int i = 0; i < menuOptions.length; i++) {
                if (i == currentMenuIndex) g2.setColor(Color.YELLOW);
                else g2.setColor(Color.BLACK);
                String text = menuOptions[i];
                int textWidth = g2.getFontMetrics().stringWidth(text);
                g2.drawString(text, (screenWidth - textWidth) / 2, 250 + i * 50);
            }
            return;
        }

        if (gameState == GameState.GAMEOVER) {
            g2.setColor(Color.RED);
            g2.setFont(g2.getFont().deriveFont(72f)); 
            String gameOverText = "GAME OVER";
            int textWidth = (int)g2.getFontMetrics().stringWidth(gameOverText);
            int textHeight = (int)g2.getFontMetrics().getHeight();
            
            int x = (screenWidth - textWidth) / 2;
            int y = (screenHeight + textHeight) / 2;
            
            g2.drawString(gameOverText, x, y);

            int remainingSeconds = (gameOverDelay - gameOverTimer) / 60;
            if (remainingSeconds > 0) {
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(24f));
                g2.drawString("Retour menu dans " + remainingSeconds + "s...", screenWidth / 2 - 100, screenHeight / 2 + 50);
            }
            
        } else if (gameState == GameState.WON) {
            g2.setColor(Color.YELLOW);  
            g2.setFont(g2.getFont().deriveFont(72f));  
            String winText = "YOU WON!";
            int textWidth = (int)g2.getFontMetrics().stringWidth(winText);
            int textHeight = (int)g2.getFontMetrics().getHeight();
          
            int x = (screenWidth - textWidth) / 2;
            int y = (screenHeight + textHeight) / 2;
            
            g2.drawString(winText, x, y);
            
        } else {
            for (int i = 0; i < nbMonsters; i++) {
                if (monsters[i] != null && monsters[i].alive) { 
                    monsters[i].draw(g2);
                }
            }
            
            player.draw(g2);
            
            int heartSize = 30;
            int startX = 20;
            int startY = 30; 

            g2.setFont(g2.getFont().deriveFont(18f)); 
            g2.setColor(Color.WHITE);
            g2.drawString("PV:", startX, startY - 5); 

            for (int i = 0; i < 3; i++) { 
                int heartX = startX + i * (heartSize + 5); 
                int heartY = startY;
                
                if (heartFull != null && heartEmpty != null) {
                    if (i < player.health) {
                        g2.drawImage(heartFull, heartX, heartY, heartSize, heartSize, null);
                    } else {
                        g2.drawImage(heartEmpty, heartX, heartY, heartSize, heartSize, null);
                    }
                } else {
                    if (i < player.health) {
                        g2.setColor(Color.RED); 
                    } else {
                        g2.setColor(Color.GRAY);
                    }
                    g2.fillOval(heartX, heartY, heartSize, heartSize);
                }
                g2.setColor(Color.WHITE);
            }

            int coinSize = 30;
            int coinX = 20;
            int coinY = startY + 60;

            if (labyrinthM.imgCoin != null) {
                g2.drawImage(labyrinthM.imgCoin, coinX, coinY, coinSize, coinSize, null);
            } else {
                g2.setColor(new Color(255, 215, 0));
                g2.fillOval(coinX, coinY, coinSize, coinSize);
            }

            g2.setFont(g2.getFont().deriveFont(22f));
            g2.setColor(Color.WHITE);
            g2.drawString("x " + player.coinCount, coinX + coinSize + 10, coinY + 24);

            // Affiche le message de pièces manquantes
            if (showMissingCoinsMessage) {
                long elapsed = System.currentTimeMillis() - missingCoinsMessageStart;
                if (elapsed < missingCoinsMessageDuration) {
                    g2.setColor(Color.YELLOW);
                    g2.setFont(g2.getFont().deriveFont(28f));
                    int textWidth = g2.getFontMetrics().stringWidth(missingCoinsMessage);
                    g2.drawString(missingCoinsMessage, (screenWidth - textWidth) / 2, 50);
                } else {
                    showMissingCoinsMessage = false;
                }
            }
        }
        
        g2.dispose();
    }

    // Getter et Setter
    public int getOriginalTileSize() { return originalTileSize; }
    public int getScale() { return scale; }
    public int getTileSize() { return tileSize; }
    public int getMaxScreenCol() { return maxScreenCol; }
    public int getMaxScreenRow() { return maxScreenRow; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public int getMaxWorldCol() { return maxWorldCol; }
    public int getMaxWorldRow() { return maxWorldRow; }
    public int getWorldWidth() { return worldWidth; }
    public int getWorldHeight() { return worldHeight; }
    public KeyHandler getKeyH() { return keyH; }
    public void setKeyH(KeyHandler keyH) { this.keyH = keyH; }
    public Thread getGameThread() { return gameThread; }
    public void setGameThread(Thread gameThread) { this.gameThread = gameThread; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Monster[] getMonsters() { return monsters; }
    public void setMonsters(Monster[] monsters) { this.monsters = monsters; }
    public int getNbMonsters() { return nbMonsters; }
    public void setNbMonsters(int nbMonsters) { this.nbMonsters = nbMonsters; }
    public int getFPS() { return FPS; }
    public void setFPS(int fPS) { FPS = fPS; }
    public Labyrinthe getLabyrinthM() { return labyrinthM; }
    public void setLabyrinthM(Labyrinthe labyrinthM) { this.labyrinthM = labyrinthM; }
    public int getSquareX() { return squareX; }
    public void setSquareX(int squareX) { this.squareX = squareX; }
    public int getSquareY() { return squareY; }
    public void setSquareY(int squareY) { this.squareY = squareY; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
}
