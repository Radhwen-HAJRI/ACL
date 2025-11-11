package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

import javax.swing.JPanel;

import entity.Monster; 
import entity.Player;
import main.Labyrinthe;


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
    
    // Monster monster ; 
    Monster[] monsters;
    int nbMonsters;

    int FPS = 60;
    Labyrinthe labyrinthM = new Labyrinthe(this);

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
                    System.out.println("✅ Joueur placé en (" + c + "," + r + ")");
                }
            }
        }

        player.worldx = (int) labyrinthM.getPointDepart().x;
        player.worldy = (int) labyrinthM.getPointDepart().y;

        //Monster monster = new Monster(this); // Crée le monstre
        Random rand = new Random();
        nbMonsters = 6 + rand.nextInt(7);  // 4,5,6,7,8
        System.out.println("Nombre de monstres créés : " + nbMonsters);
        
        // DANS GamePanel.java - constructeur GamePanel()

        monsters = new Monster[nbMonsters];
        for (int i = 0; i < nbMonsters; i++) {
            monsters[i] = new Monster(this);
            
        // Une chance sur deux d'être un "Chaser"
        if (rand.nextBoolean()) {
            monsters[i].isChaser = true;
        }
        // --- NOUVELLE LOGIQUE DE SPAWN SÉCURISÉ ---
        boolean foundSpot = false;
        while (!foundSpot) {
            
            // 1. Choisir des coordonnées de TILE (col, row) au hasard dans le MONDE ENTIER
            int col = rand.nextInt(this.maxWorldCol);
            int row = rand.nextInt(this.maxWorldRow);
            
            // 2. Récupérer le type de tile à cet endroit
            int tileNum = this.labyrinthM.mapTileNum[col][row];
            
            // 3. Vérifier si ce tile n'est PAS un obstacle
            // On vérifie que le tile existe et que sa propriété 'collision' est false
            if (this.labyrinthM.tile[tileNum] != null && !this.labyrinthM.tile[tileNum].collision) {
                
                // 4. C'est un bon endroit ! On place le monstre (en pixels)
                monsters[i].worldx = col * this.tileSize;
                monsters[i].worldy = row * this.tileSize;
                
                // 5. On sort de la boucle (pour ce monstre)
                foundSpot = true; 
            }
            
            // Si c'est un mur (collision == true), la boucle 'while' recommence
            // et va tester de nouvelles coordonnées au hasard.
        }
    }
    }

   

    // Méthode pour checker si une position collide avec un tile obstacle
    public boolean canMoveHere(int nextX, int nextY) {
        // Calcule les tiles touchés par la hitbox du héros (4 coins pour précision)
        int leftCol = (nextX) / tileSize;
        int rightCol = (nextX + tileSize - 1) / tileSize;  // -1 pour bord
        int topRow = (nextY) / tileSize;
        int bottomRow = (nextY + tileSize - 1) / tileSize;

        // Vérifie limites écran (pas hors map)
        if (leftCol < 0 || rightCol >= maxWorldCol || topRow < 0 || bottomRow >= maxWorldRow) {
        return false;
        }

        // Check collision sur les 4 coins (via map de labyrinthM)
        int tileNum1 = labyrinthM.mapTileNum[leftCol][topRow];  // Coin haut-gauche
        int tileNum2 = labyrinthM.mapTileNum[rightCol][topRow]; // Haut-droite
        int tileNum3 = labyrinthM.mapTileNum[leftCol][bottomRow]; // Bas-gauche
        int tileNum4 = labyrinthM.mapTileNum[rightCol][bottomRow]; // Bas-droite

        // Si un tile a collision=true (ex: water), bloque
        if (labyrinthM.tile[tileNum1] != null && labyrinthM.tile[tileNum1].collision) return false;
        if (labyrinthM.tile[tileNum2] != null && labyrinthM.tile[tileNum2].collision) return false;
        if (labyrinthM.tile[tileNum3] != null && labyrinthM.tile[tileNum3].collision) return false;
        if (labyrinthM.tile[tileNum4] != null && labyrinthM.tile[tileNum4].collision) return false;

        return true;  // Libre !
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

    public void update() {
    
        player.update();

        if (!gameWon && !gameOver) {  // Seulement si pas déjà fini
            int dx = Math.abs(player.worldx - (int)labyrinthM.pointArrivee.x);
            int dy = Math.abs(player.worldy - (int)labyrinthM.pointArrivee.y);
            //Object gp;
            if (dx < labyrinthM.gp.tileSize && dy < labyrinthM.gp.tileSize) {  // Dans le même tile (tolérance)
                gameWon = true;
                System.out.println("YOU WON! 🎉");  // Debug console
            }
        } else {
            return;
        }

        // Détecte collision joueur-monstre
        for (int i = 0; i < nbMonsters; i++) {
            // Distance < tileSize/2 → collision
            int dx = Math.abs(player.worldx - monsters[i].worldx);
            int dy = Math.abs(player.worldy - monsters[i].worldy);
            if (dx < this.tileSize && dy < this.tileSize) {
                gameOver = true;
                System.out.println("GAME OVER ! 😵");
                return;
            }
        }
        
        // Met à jour les monstres seulement si pas Game Over
        for (int i = 0; i < nbMonsters; i++) {
            monsters[i].update();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    
    // Dessine toujours le labyrinthe
    labyrinthM.draw(g2);
    
    if (gameOver) {
        // GAME OVER ROUGE ÉNORME
        g2.setColor(Color.RED);
        g2.setFont(g2.getFont().deriveFont(72f));  // TAILLE GÉANTE
        String gameOverText = "GAME OVER";
        int textWidth = (int)g2.getFontMetrics().stringWidth(gameOverText);
        int textHeight = (int)g2.getFontMetrics().getHeight();
        
        // Centre le texte
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight + textHeight) / 2;
        
        g2.drawString(gameOverText, x, y);
        
        // Effet ombre (optionnel, plus stylé)
        g2.setColor(Color.BLACK);
        g2.drawString(gameOverText, x + 5, y + 5);
        
    } else if (gameWon) {
        // YOU WON VERT/JAUNE ÉNORME
        g2.setColor(Color.YELLOW);  // Fond jaune
        g2.setFont(g2.getFont().deriveFont(72f));  // TAILLE GÉANTE
        String winText = "YOU WON!";
        int textWidth = (int)g2.getFontMetrics().stringWidth(winText);
        int textHeight = (int)g2.getFontMetrics().getHeight();
        
        // Centre le texte
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight + textHeight) / 2;
        
        // Effet ombre (optionnel)
        g2.setColor(Color.BLACK);
        g2.drawString(winText, x + 5, y + 5);
        g2.setColor(Color.GREEN);  // Texte vert
        g2.drawString(winText, x, y);
        
        // Optionnel : Instructions rejouer
        g2.setFont(g2.getFont().deriveFont(24f));
        g2.setColor(Color.WHITE);
        g2.drawString("Appuie sur R pour recommencer", x - 50, y + 50);
        
    } else {
        // Jeu normal (déjà là)
        player.draw(g2);
        for (int i = 0; i < nbMonsters; i++) {
            monsters[i].draw(g2);
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
