package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

import javax.swing.JPanel;

import entity.Monster; 
import entity.Player;
import main.java.Labyrinthe;
import tile.TileManager;


public class GamePanel extends JPanel implements Runnable {

    public final int originalTileSize = 16;
    public final int scale = 2;

    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 16;

    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;

    Player player = new Player(this, keyH);
   // Monster monster ; 
   Monster[] monsters;
   int nbMonsters;

    int FPS = 60;
    Labyrinthe labyrinthM = new Labyrinthe(this);

    int squareX = 100;
    int squareY = 100;
    int speed = 4;

    boolean gameOver = false;  // ← NOUVEAU

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        player.x = labyrinthM.getPointDepart().x;
        player.y = labyrinthM.getPointDepart().y;

        //Monster monster = new Monster(this); // Crée le monstre
    Random rand = new Random();
    nbMonsters = 4 + rand.nextInt(5);  // 4,5,6,7,8
    System.out.println("Nombre de monstres créés : " + nbMonsters);
    
    monsters = new Monster[nbMonsters];
    for (int i = 0; i < nbMonsters; i++) {
        monsters[i] = new Monster(this);
        // Position aléatoire dans le labyrinthe (évite le spawn du joueur)
        monsters[i].x = this.tileSize * rand.nextInt(this.maxScreenCol - 1) + this.tileSize;
        monsters[i].y = this.tileSize * rand.nextInt(this.maxScreenRow - 1) + this.tileSize;
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
        if (leftCol < 0 || rightCol >= maxScreenCol || topRow < 0 || bottomRow >= maxScreenRow) {
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

       if (gameOver) return;  // Arrête tout si Game Over
    
    player.update();
    
    // Détecte collision joueur-monstre
    for (int i = 0; i < nbMonsters; i++) {
        // Distance < tileSize/2 → collision
        int dx = Math.abs(player.x - monsters[i].x);
        int dy = Math.abs(player.y - monsters[i].y);
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
        
    } else {
        // Jeu normal
        player.draw(g2);
        for (int i = 0; i < nbMonsters; i++) {
            monsters[i].draw(g2);
        }
    }
    
    g2.dispose();
    }
}
