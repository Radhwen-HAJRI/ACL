package main;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Font;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics2D;

import tile.TileManager;

public class Labyrinthe extends TileManager {
    
    public Point pointDepart;
    public Point pointArrivee;
    private BufferedImage imgTresor;
    private BufferedImage[] doorFrames;  
    private int doorSpriteCounter = 0;
    private int doorSpriteNum = 1; // 1=closed, 2=opening, 3=open
    private final int doorAnimationDelay = 20;
    
    public Labyrinthe(GamePanel gp) {
        super(gp); 
        loadMap("/maps/map01.txt");  
        setPoints(); 
        try {
            doorFrames = new BufferedImage[3];
            doorFrames[0] = ImageIO.read(getClass().getResourceAsStream("/door/door_closed.png"));   // Frame 1: Closed
            doorFrames[1] = ImageIO.read(getClass().getResourceAsStream("/door/door_opening.png"));  // Frame 2: Opening
            doorFrames[2] = ImageIO.read(getClass().getResourceAsStream("/door/door_opening.png"));  // Frame 3: Open
            imgTresor = ImageIO.read(getClass().getResourceAsStream("/tiles/key.png"));
            System.out.println("Porte animée et trésor chargées !");
        } catch (IOException e) {
            e.printStackTrace();
            doorFrames = null;
            imgTresor = null;
            System.out.println("Fallback : Images porte/trésor manquantes");
        }
    }
    
    private void setPoints() {
        int startCol = 10;  
        int startRow = 7;  
        pointDepart = new Point(startCol * gp.tileSize, startRow * gp.tileSize);
        
        int endCol = 37;  
        int endRow = 43;    
        pointArrivee = new Point(endCol * gp.tileSize, endRow * gp.tileSize);
    }
    
    public Point getPointDepart() {
        return pointDepart;
    }
    
    public Point getPointArrivee() {
        return pointArrivee;
    }
    
    public int[][] getGrille() {
        return mapTileNum; 
    }
    
    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);  

        int departScreenX = pointDepart.x - gp.player.worldx + gp.player.screenX;
        int departScreenY = pointDepart.y - gp.player.worldy + gp.player.screenY;
        int arriveeScreenX = pointArrivee.x - gp.player.worldx + gp.player.screenX;
        int arriveeScreenY = pointArrivee.y - gp.player.worldy + gp.player.screenY;
        
        // Animation porte
        doorSpriteCounter++;
        if (doorSpriteCounter > doorAnimationDelay) {
            doorSpriteNum = (doorSpriteNum % 3) + 1;  // 1→2→3→1
            doorSpriteCounter = 0;
        }
        int doorWidth = gp.tileSize * 3;  
        int doorHeight = gp.tileSize * 3;
        int offsetX = (gp.tileSize - doorWidth) / 2;  
        int offsetY = (gp.tileSize - doorHeight) / 2; 
        if (doorFrames != null) {
            BufferedImage currentFrame = doorFrames[doorSpriteNum - 1];
            if (currentFrame != null) {
                g2.drawImage(currentFrame, departScreenX + offsetX, departScreenY + offsetY, doorWidth, doorHeight, null);
            } else {
                g2.setColor(Color.BLACK);
                g2.fillRect(departScreenX + offsetX, departScreenY + offsetY, doorWidth, doorHeight);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 14)); 
                g2.drawString("PORTE", departScreenX + 5, departScreenY + doorHeight / 2);
            }
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(departScreenX + offsetX, departScreenY + offsetY, doorWidth, doorHeight);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("PORTE", departScreenX + 5, departScreenY + doorHeight / 2);
        }
        
        if (imgTresor != null) {
            g2.drawImage(imgTresor, arriveeScreenX, arriveeScreenY, gp.tileSize, gp.tileSize, null);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillRect(arriveeScreenX + 4, arriveeScreenY + 4, gp.tileSize - 8, gp.tileSize - 8); 
            g2.setColor(Color.ORANGE);
            g2.fillRect(arriveeScreenX, arriveeScreenY, 4, gp.tileSize);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("CLE", arriveeScreenX + 5, arriveeScreenY + 20);
        }
    }
}