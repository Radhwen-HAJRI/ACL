package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

    // world map parameters
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;

    public Player player = new Player(this, keyH);
    
    Monster[] monsters;
    int nbMonsters;

    int FPS = 60;
    Labyrinthe labyrinthM = new Labyrinthe(this);
    private BufferedImage heartFull, heartEmpty;

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

        // Charge images HUD cœurs
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

        // --- Trouver le centre du labyrinthe ---
        int centerCol = maxWorldCol / 2;
        int centerRow = maxWorldRow / 2;
        
        boolean found = false;
        for (int r = centerRow - 5; r <= centerRow + 5 && !found; r++) {
            for (int c = centerCol - 5; c <= centerCol + 5 && !found; c++) {
                if (labyrinthM.mapTileNum[c][r] == 0) { // 0 = tuile vide
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
        nbMonsters = 7 + rand.nextInt(8);  // 4,5,6,7,8
        System.out.println("Nombre de monstres créés : " + nbMonsters);
        
        monsters = new Monster[nbMonsters];
        for (int i = 0; i < nbMonsters; i++) {
            monsters[i] = new Monster(this);
            
        // Une chance sur deux d'être un "Chaser"
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
        // Calcule les tiles touchés par la hitbox du héros 
        int leftCol = (nextX) / tileSize;
        int rightCol = (nextX + tileSize - 1) / tileSize;  
        int topRow = (nextY) / tileSize;
        int bottomRow = (nextY + tileSize - 1) / tileSize;

        if (leftCol < 0 || rightCol >= maxWorldCol || topRow < 0 || bottomRow >= maxWorldRow) {
        return false;
        }

        // Check collision sur les 4 coins 
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
/**
     * Vérifie si le joueur (en état d'attaque) touche un monstre.
     * C'est ici qu'est gérée la "demi-case" de portée.
     */
    public void checkPlayerAttack() {
        // On ne vérifie que si le joueur est EN TRAIN d'attaquer
        // ET s'il n'a pas déjà touché qqn avec ce coup
        if (!player.state.equals("attacking") || player.hasHitThisSwing) {
            return;
        }

        // 1. Définir la portée d'attaque (demi-case, comme demandé)
        int attackDistance =  this.tileSize  ;

        // 2. Parcourir tous les monstres
        for (int i = 0; i < nbMonsters; i++) {
            if (monsters[i] != null && monsters[i].alive) {
                
                // 3. Calculer la distance entre le joueur et le monstre
                int dx = Math.abs(player.worldx - monsters[i].worldx);
                int dy = Math.abs(player.worldy - monsters[i].worldy);

                // 4. Vérifier la collision
                // On vérifie si les DEUX distances (dx et dy) sont DANS la "hitbox"
                // ET si la distance totale est assez petite (demi-case)
                if (dx < this.tileSize && dy < this.tileSize && 
                    (dx*dx + dy*dy) < (attackDistance * attackDistance)) {
                    
                    // C'EST TOUCHÉ !
                    monsters[i].health--; // Le monstre perd 1 PV
                    System.out.println("Monstre touché ! Vie restante : " + monsters[i].health);
                    player.hasHitThisSwing = true; // Le joueur a touché, son coup est "fini"
                    
                    // Optionnel : un monstre ne peut pas être un "Chaser" s'il est touché
                    // monsters[i].isChaser = false; 
                    // monsters[i].setState("wandering");
                    
                    // On sort de la boucle (1 coup = 1 monstre touché max)
                    break; 
                }
            }
        }
    }
 
    public void update() {
    
        if (gameOver || gameWon) {
            return; // Arrête tout si le jeu est fini
        }

        // --- 1. Mettre à jour le joueur ---
        player.update();

        // --- 2. Vérifier si le joueur attaque un monstre ---
        checkPlayerAttack(); // Notre nouvelle méthode

        // --- 3. Mettre à jour les monstres ---
        for (int i = 0; i < nbMonsters; i++) {
            if (monsters[i] != null) {
                
                // Si le monstre n'a plus de vie, il est mort
                if (monsters[i].health <= 0) {
                    monsters[i].alive = false;
                }
                
                // Seuls les monstres en vie sont mis à jour
                if (monsters[i].alive) {
                    monsters[i].update();
                }
            }
        }

        // --- 4. Vérifier les collisions et conditions de fin ---
        
        // Détecte collision joueur-monstre (le joueur prend des dégâts)
        if (player.invincibleCounter == 0 && !player.state.equals("attacking")) {
            for (int i = 0; i < nbMonsters; i++) {
                // On vérifie seulement si le monstre est VIVANT
                if (monsters[i] != null && monsters[i].alive) {
                    
                    // C'est votre "demi-case" pour le GAME OVER
                    int dx = Math.abs(player.worldx - monsters[i].worldx);
                    int dy = Math.abs(player.worldy - monsters[i].worldy);
                    int hitRange = this.tileSize / 2; // Demi-case

                    if (dx < hitRange && dy < hitRange) {  
                        player.health--;
                        player.invincibleCounter = player.invincibleDuration; // Active i-frames
                        System.out.println("Collision ! PV restants: " + player.health);
                        
                        if (player.health <= 0) {
                            gameOver = true;
                            System.out.println("GAME OVER ! PV à 0 😵");
                            return; // Sort de update()
                        }
                        break; // Une seule collision par frame
                    }
                }
            }
        }

        // Condition de victoire
        int dx = Math.abs(player.worldx - (int)labyrinthM.pointArrivee.x);
        int dy = Math.abs(player.worldy - (int)labyrinthM.pointArrivee.y);
        if (dx < this.tileSize && dy < this.tileSize) {
            gameWon = true;
            System.out.println("YOU WON! 🎉");
            return; // Sort de update()
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
                    g2.setColor(Color.BLACK);
                    g2.drawOval(heartX, heartY, heartSize, heartSize);
                }
                g2.setColor(Color.WHITE);
            }

            
            for (int i = 0; i < nbMonsters; i++) {
              if (monsters[i] != null && monsters[i].alive) { // <-- LIGNE MODIFIÉE
                    monsters[i].draw(g2);
                }
            }
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
