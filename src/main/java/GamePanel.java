package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

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

    int FPS = 60;
    Labyrinthe labyrinthM = new Labyrinthe(this);

    int squareX = 100;
    int squareY = 100;
    int speed = 4;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        player.x = labyrinthM.getPointDepart().x;
        player.y = labyrinthM.getPointDepart().y;
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

        player.update();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        labyrinthM.draw(g2);
        
        player.draw(g2);

        g2.dispose();
    }
}
