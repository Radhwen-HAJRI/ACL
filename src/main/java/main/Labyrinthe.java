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
    public BufferedImage imgFinalTreasure;
    public BufferedImage imgCoin;
    private BufferedImage[] doorFrames; 
    private BufferedImage[] explosionFrames; 
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

            explosionFrames = new BufferedImage[10];
            for (int i = 0; i < 10; i++) {
                explosionFrames[i] = ImageIO.read(getClass().getResourceAsStream("/Explosion_depart/Explosion_" + (i+1) + ".png"));
            }

            imgTresor = ImageIO.read(getClass().getResourceAsStream("/tiles/key.png"));
            imgCoin = ImageIO.read(getClass().getResourceAsStream("/tiles/coin.png"));
            imgFinalTreasure = ImageIO.read(getClass().getResourceAsStream("/tiles/tresor.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setPoints() {
        int startCol = 0;
        int startRow = 0;
        int endCol = 0;
        int endRow = 0;

        if (gp.currentMap == 0) {
            startCol = 10; 
            startRow = 7;
            
            endCol = 37; 
            endRow = 43;
        } 
        else if (gp.currentMap == 1) {
            
            startCol = 5;  
            startRow = 5;  
            
            endCol = 44; 
            endRow = 36;
        }
        
        // Application des coordonnées
        pointDepart = new Point(startCol * gp.tileSize, startRow * gp.tileSize);
        pointArrivee = new Point(endCol * gp.tileSize, endRow * gp.tileSize);
    }
    
    public void initializeCoins() {
        coinPositions = new ArrayList<>();
        
        // Trouver toutes les positions valides (tuiles 0 et 3)
        List<Point> validPositions = new ArrayList<>();
        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int tileType = mapTileNum[gp.currentMap][col][row];
                // Vérifier si c'est une tuile 0 ou 3
                if (tileType == 0 || tileType == 3) {
                    // Éviter les positions trop proches du départ et de l'arrivée
                    Point arriveeTile = new Point(pointArrivee.x / gp.tileSize, pointArrivee.y / gp.tileSize);  // ← Coords tile arrivee
                    if (col == arriveeTile.x && row == arriveeTile.y) continue;
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
        
       BufferedImage currentImage = null;
        int animationSpeed = 15; // Vitesse de l'animation

        // CAS 1 : NIVEAU 1 (La Porte)
        if (gp.currentMap == 0) {
            // Logique porte (3 images)
            if (doorFrames != null) {
                doorSpriteCounter++;
                if (doorSpriteCounter > animationSpeed) {
                    doorSpriteNum = (doorSpriteNum % 3) + 1; // Boucle 1, 2, 3
                    doorSpriteCounter = 0;
                }
                currentImage = doorFrames[doorSpriteNum - 1];
            }
        } 
        // CAS 2 : NIVEAU 2 (L'Explosion)
        else if (gp.currentMap == 1) {
            // Logique explosion (10 images)
            if (explosionFrames != null) {
                doorSpriteCounter++;
                if (doorSpriteCounter > 10) { // Un peu plus rapide que la porte
                    doorSpriteNum = (doorSpriteNum % 10) + 1; // Boucle 1 à 10
                    doorSpriteCounter = 0;
                }
                // Protection index au cas où
                int index = doorSpriteNum - 1;
                if (index >= 0 && index < 10) {
                    currentImage = explosionFrames[index];
                }
            }
        }

        
        // On dessine l'image choisie (Porte OU Explosion)
        if (currentImage != null) {
            // Taille de l'image (3x la tuile comme avant ?)
            int width = gp.tileSize * 3;
            int height = gp.tileSize * 3;
            int offsetX = (gp.tileSize - width) / 2;
            int offsetY = (gp.tileSize - height) / 2;
            
            g2.drawImage(currentImage, departScreenX + offsetX, departScreenY + offsetY, width, height, null);
        } 
        else {
            // Fallback (Carré noir si pas d'image)
            g2.setColor(Color.BLACK);
            g2.fillRect(departScreenX, departScreenY, gp.tileSize, gp.tileSize);
        }
        
        
        int keyCol = (int) (pointArrivee.x / gp.tileSize);
        int keyRow = (int) (pointArrivee.y / gp.tileSize);
        
            
        int arriveeScreenXKey = pointArrivee.x - gp.player.worldx + gp.player.screenX;
        int arriveeScreenYKey = pointArrivee.y - gp.player.worldy + gp.player.screenY;
        
        BufferedImage objectiveImage = null;
        
        if (gp.currentMap == 0) {
            objectiveImage = imgTresor; // Niveau 1 = La Clé
        } else {
            objectiveImage = imgFinalTreasure; // Niveau 2 = Le Trésor
        }
        
        // Dessin
        if (objectiveImage != null) {
            g2.drawImage(objectiveImage, arriveeScreenXKey, arriveeScreenYKey, gp.tileSize, gp.tileSize, null);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillRect(arriveeScreenXKey, arriveeScreenYKey, gp.tileSize, gp.tileSize);
        }
        

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