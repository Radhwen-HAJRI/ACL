package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    
    final int originalTileSize = 32;
    final int scale = 2;

    public final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 16;

    final int screenWidth = tileSize * maxScreenCol;
    final int screenHeight = tileSize * maxScreenRow;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    int FPS = 60;

    
    int squareX = 100;
    int squareY = 100;
    int speed = 4;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
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
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) (remainingTime / 1000000));
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        
        if (keyH.upPressed) squareY -= speed;
        if (keyH.downPressed) squareY += speed;
        if (keyH.leftPressed) squareX -= speed;
        if (keyH.rightPressed) squareX += speed;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Dessine le carré
        g2.setColor(Color.white);
        g2.fillRect(squareX, squareY, tileSize, tileSize);

        g2.dispose();
    }
}
