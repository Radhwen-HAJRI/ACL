package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import tile.TileManager;

public class Labyrinthe extends TileManager {
    
    public Point pointDepart;
    public Point pointArrivee;
    public BufferedImage imgTresor;
    public BufferedImage imgCoin;
    private BufferedImage[] doorFrames;  
    private int doorSpriteCounter = 0;
    private int doorSpriteNum = 1;
    private final int doorAnimationDelay = 20;
    
    public List<Point> coinPositions;
    private Random random;
    
    public Labyrinthe(GamePanel gp) {
        super(gp); 
        loadMap("/maps/map01.txt",0);
        
        // Remplacer l'ancienne clé par des pièces
        mapTileNum[gp.currentMap][23][7] = 9;
        mapTileNum[gp.currentMap][37][43] = 6;
    
        if (tile[6] != null) {
            tile[6].collision = false;
        }
        if (tile[9] != null) {
            tile[9].collision = false;
        }
        
        setPoints(); 
        random = new Random();
        initializeCoins();
        
        try {
            doorFrames = new BufferedImage[3];
            doorFrames[0] = ImageIO.read(getClass().getResourceAsStream("/door/door_closed.png"));
            doorFrames[1] = ImageIO.read(getClass().getResourceAsStream("/door/door_opening.png"));
            doorFrames[2] = ImageIO.read(getClass().getResourceAsStream("/door/door_opening.png"));
            imgTresor = ImageIO.read(getClass().getResourceAsStream("/tiles/key.png"));
            imgCoin = ImageIO.read(getClass().getResourceAsStream("/tiles/coin.png"));
            System.out.println("Porte animée, clé et pièces chargées !");
        } catch (IOException e) {
            e.printStackTrace();
            doorFrames = null;
            imgTresor = null;
            imgCoin = null;
            System.out.println("Fallback : Images manquantes");
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
    
    private void initializeCoins() {
        coinPositions = new ArrayList<>();
        
        // Trouver toutes les positions valides (tuiles 0 et 3)
        List<Point> validPositions = new ArrayList<>();
        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int tileType = mapTileNum[gp.currentMap][col][row];
                // Vérifier si c'est une tuile 0 ou 3
                if (tileType == 0 || tileType == 3) {
                    // Éviter les positions trop proches du départ et de l'arrivée
                    if (!isTooCloseToSpecialPositions(col, row)) {
                        validPositions.add(new Point(col, row));
                    }
                }
            }
        }
        
        System.out.println("Positions valides trouvées: " + validPositions.size());
        
        // Placer 15 pièces aléatoirement sur les positions valides
        int numberOfCoins = Math.min(15, validPositions.size());
        for (int i = 0; i < numberOfCoins; i++) {
            if (validPositions.isEmpty()) break;
            
            int randomIndex = random.nextInt(validPositions.size());
            Point coinPos = validPositions.get(randomIndex);
            coinPositions.add(new Point(coinPos.x * gp.tileSize, coinPos.y * gp.tileSize));
            
            // Marquer cette position comme pièce dans la carte
            mapTileNum[gp.currentMap][coinPos.x][coinPos.y] = 9;
            
            // Retirer cette position pour éviter les doublons
            validPositions.remove(randomIndex);
        }
        
        System.out.println("Pièces placées: " + coinPositions.size());
    }
    
    private boolean isTooCloseToSpecialPositions(int col, int row) {
        // Éviter les positions trop proches du point de départ
        int startCol = pointDepart.x / gp.tileSize;
        int startRow = pointDepart.y / gp.tileSize;
        double distToStart = Math.sqrt(Math.pow(col - startCol, 2) + Math.pow(row - startRow, 2));
        
        // Éviter les positions trop proches du point d'arrivée
        int endCol = pointArrivee.x / gp.tileSize;
        int endRow = pointArrivee.y / gp.tileSize;
        double distToEnd = Math.sqrt(Math.pow(col - endCol, 2) + Math.pow(row - endRow, 2));
        
        // Retourner true si trop proche de l'une des positions spéciales
        return distToStart < 5 || distToEnd < 5;
    }
    
    public Point getPointDepart() {
        return pointDepart;
    }
    
    public Point getPointArrivee() {
        return pointArrivee;
    }
    
    public int[][] getGrille() {
        return mapTileNum[gp.currentMap]; 
    }
    
    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);  

        int departScreenX = pointDepart.x - gp.player.worldx + gp.player.screenX;
        int departScreenY = pointDepart.y - gp.player.worldy + gp.player.screenY;
        int arriveeScreenX = pointArrivee.x - gp.player.worldx + gp.player.screenX;
        int arriveeScreenY = pointArrivee.y - gp.player.worldy + gp.player.screenY;
        
        doorSpriteCounter++;
        if (doorSpriteCounter > doorAnimationDelay) {
            doorSpriteNum = (doorSpriteNum % 3) + 1;
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
        
        // Clé de victoire
        int keyCol = (int) (pointArrivee.x / gp.tileSize);
        int keyRow = (int) (pointArrivee.y / gp.tileSize);
        if (mapTileNum[gp.currentMap][keyCol][keyRow] == 6) {
            int arriveeScreenXKey = pointArrivee.x - gp.player.worldx + gp.player.screenX;
            int arriveeScreenYKey = pointArrivee.y - gp.player.worldy + gp.player.screenY;
            
            if (imgTresor != null) {
                g2.drawImage(imgTresor, arriveeScreenXKey, arriveeScreenYKey, gp.tileSize, gp.tileSize, null);
            } else {
                g2.setColor(Color.YELLOW);
                g2.fillRect(arriveeScreenXKey + 4, arriveeScreenYKey + 4, gp.tileSize - 8, gp.tileSize - 8); 
                g2.setColor(Color.ORANGE);
                g2.fillRect(arriveeScreenXKey, arriveeScreenYKey, 4, gp.tileSize);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.drawString("CLE", arriveeScreenXKey + 5, arriveeScreenYKey + 20);
            }
        }

        // Dessiner les pièces
        for (Point coin : coinPositions) {
            int coinCol = coin.x / gp.tileSize;
            int coinRow = coin.y / gp.tileSize;
            
            if (mapTileNum[gp.currentMap][coinCol][coinRow] == 9) {
                int coinScreenX = coin.x - gp.player.worldx + gp.player.screenX;
                int coinScreenY = coin.y - gp.player.worldy + gp.player.screenY;

                if (imgCoin != null) {
                    g2.drawImage(imgCoin, coinScreenX, coinScreenY, gp.tileSize, gp.tileSize, null);
                } else {
                    g2.setColor(new Color(255, 215, 0));
                    g2.fillOval(coinScreenX + 4, coinScreenY + 4, gp.tileSize - 8, gp.tileSize - 8);
                    g2.setColor(new Color(218, 165, 32));
                    g2.drawOval(coinScreenX + 4, coinScreenY + 4, gp.tileSize - 8, gp.tileSize - 8);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    g2.drawString("$", coinScreenX + 12, coinScreenY + 22);
                }
            }
        }
    }
    
    public void removeCoin(int col, int row) {
        coinPositions.removeIf(coin -> 
            coin.x / gp.tileSize == col && coin.y / gp.tileSize == row);
    }
}