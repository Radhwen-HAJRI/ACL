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
    public BufferedImage imgCoin2; 
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
        
        
        mapTileNum[gp.currentMap][23][7] = 9;
        mapTileNum[gp.currentMap][37][43] = 6;
    
        if (tile[6] != null) {
            tile[6].collision = false;
        }
        if (tile[9] != null) {
            tile[9].collision = false;
        }

        
        if (tile.length > 10 && tile[10] != null) {
            tile[10].collision = false;
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
            
            imgCoin2 = ImageIO.read(getClass().getResourceAsStream("/tiles/coin2.png"));
            imgFinalTreasure = ImageIO.read(getClass().getResourceAsStream("/tiles/tresor.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    @Override
    public void loadMap(String filePath, int mapIndex) {
        super.loadMap(filePath, mapIndex);
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
        
       
        pointDepart = new Point(startCol * gp.tileSize, startRow * gp.tileSize);
        pointArrivee = new Point(endCol * gp.tileSize, endRow * gp.tileSize);
    }
    
    public void initializeCoins() {
        coinPositions = new ArrayList<>();
        
        
        List<Point> validPositions = new ArrayList<>();
        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int tileType = mapTileNum[gp.currentMap][col][row];
                
                
                if (tileType == 0 || tileType == 3 || tileType == 10) { 
                    
                    Point arriveeTile = new Point(pointArrivee.x / gp.tileSize, pointArrivee.y / gp.tileSize);  
                    if (col == arriveeTile.x && row == arriveeTile.y) continue;
                    if (!isTooCloseToSpecialPositions(col, row)) {
                        validPositions.add(new Point(col, row));
                    }
                }
            }
        }
        
        System.out.println("Positions valides trouvées: " + validPositions.size());
        
        
        int numberOfCoins = Math.min(15, validPositions.size());
        for (int i = 0; i < numberOfCoins; i++) {
            if (validPositions.isEmpty()) break;
            
            int randomIndex = random.nextInt(validPositions.size());
            Point coinPos = validPositions.get(randomIndex);
            coinPositions.add(new Point(coinPos.x * gp.tileSize, coinPos.y * gp.tileSize));
            
           
            mapTileNum[gp.currentMap][coinPos.x][coinPos.y] = 9;
            
            
            validPositions.remove(randomIndex);
        }
        
        System.out.println("Pièces placées: " + coinPositions.size());
    }
    
    private boolean isTooCloseToSpecialPositions(int col, int row) {
        
        int startCol = pointDepart.x / gp.tileSize;
        int startRow = pointDepart.y / gp.tileSize;
        double distToStart = Math.sqrt(Math.pow(col - startCol, 2) + Math.pow(row - startRow, 2));
        
       
        int endCol = pointArrivee.x / gp.tileSize;
        int endRow = pointArrivee.y / gp.tileSize;
        double distToEnd = Math.sqrt(Math.pow(col - endCol, 2) + Math.pow(row - endRow, 2));
        
        
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
        int animationSpeed = 15; 

       
        if (gp.currentMap == 0) {
            
            if (doorFrames != null) {
                doorSpriteCounter++;
                if (doorSpriteCounter > animationSpeed) {
                    doorSpriteNum = (doorSpriteNum % 3) + 1;
                    doorSpriteCounter = 0;
                }
                currentImage = doorFrames[doorSpriteNum - 1];
            }
        } 
        
        else if (gp.currentMap == 1) {
            
            if (explosionFrames != null) {
                doorSpriteCounter++;
                if (doorSpriteCounter > 10) { 
                    doorSpriteNum = (doorSpriteNum % 10) + 1;
                    doorSpriteCounter = 0;
                }
                
                int index = doorSpriteNum - 1;
                if (index >= 0 && index < 10) {
                    currentImage = explosionFrames[index];
                }
            }
        }

        
       
        if (currentImage != null) {
            
            int width = gp.tileSize * 3;
            int height = gp.tileSize * 3;
            int offsetX = (gp.tileSize - width) / 2;
            int offsetY = (gp.tileSize - height) / 2;
            
            g2.drawImage(currentImage, departScreenX + offsetX, departScreenY + offsetY, width, height, null);
        } 
        else {
            
            g2.setColor(Color.BLACK);
            g2.fillRect(departScreenX, departScreenY, gp.tileSize, gp.tileSize);
        }
        
        
        int keyCol = (int) (pointArrivee.x / gp.tileSize);
        int keyRow = (int) (pointArrivee.y / gp.tileSize);
        
            
        int arriveeScreenXKey = pointArrivee.x - gp.player.worldx + gp.player.screenX;
        int arriveeScreenYKey = pointArrivee.y - gp.player.worldy + gp.player.screenY;
        
        BufferedImage objectiveImage = null;
        
        if (gp.currentMap == 0) {
            objectiveImage = imgTresor; 
        } else {
            objectiveImage = imgFinalTreasure; 
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
                
                
                BufferedImage coinImage = imgCoin;
                if (gp.currentMap == 1) { 
                    coinImage = imgCoin2;
                }
                

                if (coinImage != null) {
                    g2.drawImage(coinImage, coinScreenX, coinScreenY, gp.tileSize, gp.tileSize, null);
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

    public void resetMaps() {
        
        loadMap("/maps/map01.txt", 0);
        
        
        mapTileNum[0][23][7] = 9; 
        mapTileNum[0][37][43] = 6;
        
        
        if (tile[6] != null) tile[6].collision = false;
        if (tile[9] != null) tile[9].collision = false;

        
        if (tile.length > 10 && tile[10] != null) {
            tile[10].collision = false;
        }

       
        setPoints();

        
        initializeCoins();
    }
}
