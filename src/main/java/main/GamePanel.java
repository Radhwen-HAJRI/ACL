package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

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
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    public Rectangle solidArea = new Rectangle(0,0,48,48);
    public int solidAreaDefaultX = 0;
    public int solidAreaDefaultY = 0;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;

    public Player player = new Player(this, keyH);
    
    Monster[] monsters;
    int nbMonsters;

    int FPS = 60;
    Labyrinthe labyrinthM = new Labyrinthe(this);
    

    private BufferedImage heartFull, heartEmpty;
    public SoundManager soundManager = new SoundManager();

    int squareX = 100;
    int squareY = 100;
    int speed = 4;

    boolean gameOver = false;  
    boolean gameWon = false;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        try {
            heartFull = ImageIO.read(getClass().getResourceAsStream("/ui/heart_full.png"));
            heartEmpty = ImageIO.read(getClass().getResourceAsStream("/ui/heart_empty.png"));
            System.out.println("Images cœurs chargées");
        } catch (IOException e) {
            e.printStackTrace();
            heartFull = null;
            heartEmpty = null;
            System.out.println("Images cœurs manquantes – Fallback cercles");
        }

        int centerCol = maxWorldCol / 2;
        int centerRow = maxWorldRow / 2;
        
        boolean found = false;
        for (int r = centerRow - 5; r <= centerRow + 5 && !found; r++) {
            for (int c = centerCol - 5; c <= centerCol + 5 && !found; c++) {
                if (labyrinthM.mapTileNum[c][r] == 0) {
                    player.worldx = c * tileSize;
                    player.worldy = r * tileSize;
                    found = true;
                    System.out.println(" Joueur placé en (" + c + "," + r + ")");
                }
            }
        }

        player.worldx = (int) labyrinthM.getPointDepart().x;
        player.worldy = (int) labyrinthM.getPointDepart().y;

        Random rand = new Random();
        nbMonsters = 7 + rand.nextInt(8);
        System.out.println("Nombre de monstres créés : " + nbMonsters);
        
        monsters = new Monster[nbMonsters];
        for (int i = 0; i < nbMonsters; i++) {
            monsters[i] = new Monster(this);
            
        if (rand.nextBoolean()) {
            monsters[i].isChaser = true;
        }
        
        boolean foundSpot = false;
        while (!foundSpot) {
            
            int col = rand.nextInt(this.maxWorldCol);
            int row = rand.nextInt(this.maxWorldRow);
     
            int tileNum = this.labyrinthM.mapTileNum[col][row];
            
            if (this.labyrinthM.tile[tileNum] != null && !this.labyrinthM.tile[tileNum].collision) {
                
                monsters[i].worldx = col * this.tileSize;
                monsters[i].worldy = row * this.tileSize;
              
                foundSpot = true; 
            }
            
        }
    }
    }

    public boolean canMoveHere(int nextX, int nextY) {
        int leftCol = (nextX) / tileSize;
        int rightCol = (nextX + tileSize - 1) / tileSize;  
        int topRow = (nextY) / tileSize;
        int bottomRow = (nextY + tileSize - 1) / tileSize;

        if (leftCol < 0 || rightCol >= maxWorldCol || topRow < 0 || bottomRow >= maxWorldRow) {
        return false;
        }

        int tileNum1 = labyrinthM.mapTileNum[leftCol][topRow]; 
        int tileNum2 = labyrinthM.mapTileNum[rightCol][topRow]; 
        int tileNum3 = labyrinthM.mapTileNum[leftCol][bottomRow]; 
        int tileNum4 = labyrinthM.mapTileNum[rightCol][bottomRow]; 

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

        int attackDistance =  this.tileSize  ;

        for (int i = 0; i < nbMonsters; i++) {
            if (monsters[i] != null && monsters[i].alive) {
                
                int dx = Math.abs(player.worldx - monsters[i].worldx);
                int dy = Math.abs(player.worldy - monsters[i].worldy);

                if (dx < this.tileSize && dy < this.tileSize && 
                    (dx*dx + dy*dy) < (attackDistance * attackDistance)) {
                    
                    monsters[i].health--; 
                    System.out.println("Monstre touché ! Vie restante : " + monsters[i].health);
                    player.hasHitThisSwing = true; 
                    
                    break; 
                }
            }
        }
    }
 
   public void update() {
    
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
        int tileNum = labyrinthM.mapTileNum[playerCol][playerRow];
        
        // Ramasser la clé de victoire
        if (tileNum == 6) { 
            player.keyCount++; 
            labyrinthM.mapTileNum[playerCol][playerRow] = 0; 
            System.out.println("Clé de victoire ramassée !");
        }
        
        // Ramasser une pièce
        if (tileNum == 9) { 
            player.coinCount++;
            labyrinthM.mapTileNum[playerCol][playerRow] = 0; 
            labyrinthM.removeCoin(playerCol, playerRow);
            System.out.println("Pièce ramassée ! Total = " + player.coinCount);
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
                        System.out.println("Collision ! PV restants: " + player.health);
                        
                        if (player.health <= 0) {
                            gameOver = true;
                            soundManager.playLose(); 
                            System.out.println("GAME OVER ! PV à 0 😵");
                            return;
                        }
                        break; 
                    }
                }
            }
        }

        int dx = Math.abs(player.worldx - (int)labyrinthM.pointArrivee.x);
        int dy = Math.abs(player.worldy - (int)labyrinthM.pointArrivee.y);
        if (dx < this.tileSize && dy < this.tileSize && player.keyCount > 0) {
            gameWon = true;
            soundManager.playWin(); 
            System.out.println("YOU WON! 🎉");
            return; 
        }
    }

    @Override
public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    
    labyrinthM.draw(g2);
    
    if (gameOver) {
        g2.setColor(Color.RED);
        g2.setFont(g2.getFont().deriveFont(72f)); 
        String gameOverText = "GAME OVER";
        int textWidth = (int)g2.getFontMetrics().stringWidth(gameOverText);
        int textHeight = (int)g2.getFontMetrics().getHeight();
        
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight + textHeight) / 2;
        
        g2.drawString(gameOverText, x, y);
        g2.setColor(Color.BLACK);
        g2.drawString(gameOverText, x + 5, y + 5);
        
    } else if (gameWon) {
        g2.setColor(Color.YELLOW);  
        g2.setFont(g2.getFont().deriveFont(72f));  
        String winText = "YOU WON!";
        int textWidth = (int)g2.getFontMetrics().stringWidth(winText);
        int textHeight = (int)g2.getFontMetrics().getHeight();
      
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight + textHeight) / 2;
        
        g2.setColor(Color.BLACK);
        g2.drawString(winText, x + 5, y + 5);
        g2.setColor(Color.GREEN);  
        g2.drawString(winText, x, y);
        
    } else {
        for (int i = 0; i < nbMonsters; i++) {
            if (monsters[i] != null && monsters[i].alive) { 
                monsters[i].draw(g2);
            }
        }
        
        player.draw(g2);
        
        // Affichage des cœurs (PV)
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
                g2.setColor(Color.BLACK);
                g2.drawOval(heartX, heartY, heartSize, heartSize);
            }
            g2.setColor(Color.WHITE);
        }

        // Affichage des pièces seulement (compteur de clé supprimé)
        int coinSize = 30;
        int coinX = 20;
        int coinY = startY + 60;

        if (labyrinthM.imgCoin != null) {
            g2.drawImage(labyrinthM.imgCoin, coinX, coinY, coinSize, coinSize, null);
        } else {
            g2.setColor(new Color(255, 215, 0));
            g2.fillOval(coinX, coinY, coinSize, coinSize);
            g2.setColor(new Color(218, 165, 32));
            g2.drawOval(coinX, coinY, coinSize, coinSize);
        }

        g2.setFont(g2.getFont().deriveFont(22f));
        g2.setColor(Color.WHITE);
        g2.drawString("x " + player.coinCount, coinX + coinSize + 10, coinY + 24);
        
        // Le compteur de clé a été supprimé de l'interface
    }
    
    g2.dispose();
}

    public int getOriginalTileSize() {
        return originalTileSize;
    }

    public int getScale() {
        return scale;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getMaxScreenCol() {
        return maxScreenCol;
    }

    public int getMaxScreenRow() {
        return maxScreenRow;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getMaxWorldCol() {
        return maxWorldCol;
    }

    public int getMaxWorldRow() {
        return maxWorldRow;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public KeyHandler getKeyH() {
        return keyH;
    }

    public void setKeyH(KeyHandler keyH) {
        this.keyH = keyH;
    }

    public Thread getGameThread() {
        return gameThread;
    }

    public void setGameThread(Thread gameThread) {
        this.gameThread = gameThread;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Monster[] getMonsters() {
        return monsters;
    }

    public void setMonsters(Monster[] monsters) {
        this.monsters = monsters;
    }

    public int getNbMonsters() {
        return nbMonsters;
    }

    public void setNbMonsters(int nbMonsters) {
        this.nbMonsters = nbMonsters;
    }

    public int getFPS() {
        return FPS;
    }

    public void setFPS(int fPS) {
        FPS = fPS;
    }

    public Labyrinthe getLabyrinthM() {
        return labyrinthM;
    }

    public void setLabyrinthM(Labyrinthe labyrinthM) {
        this.labyrinthM = labyrinthM;
    }

    public int getSquareX() {
        return squareX;
    }

    public void setSquareX(int squareX) {
        this.squareX = squareX;
    }

    public int getSquareY() {
        return squareY;
    }

    public void setSquareY(int squareY) {
        this.squareY = squareY;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}