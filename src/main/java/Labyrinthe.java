package main.java;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;
import tile.TileManager;

public class Labyrinthe extends TileManager {  // Étend TileManager pour réutiliser loadMap/draw/getTileImage
    
    private Point pointDepart;
    private Point pointArrivee;
    private BufferedImage imgPorte, imgTresor;
    
    public Labyrinthe(GamePanel gp) {
        super(gp); 
        loadMap("/maps/map.txt");  
        setPoints(); 
        // Charge les images pour porte et trésor
        try {
            imgPorte = ImageIO.read(getClass().getResourceAsStream("/tiles/door.png"));
            imgTresor = ImageIO.read(getClass().getResourceAsStream("/tiles/key.png"));
            System.out.println("Images porte et trésor chargées !");
        } catch (IOException e) {
            e.printStackTrace();
            imgPorte = null;
            imgTresor = null;
            System.out.println("Fallback : Images porte/trésor manquantes");
        }
    }
    
    // Méthode pour définir les points (hardcodé pour ce labyrinthe simple ; plus tard, cherche les 0 libres)
    private void setPoints() {
        // Départ : premier chemin accessible (tile 1,1 → pixels)
        pointDepart = new Point(1 * gp.tileSize, 1 * gp.tileSize);  // (32, 32)
        
        // Arrivée : Cherche le tile 0 le plus bas-droite possible (scan inverse pour "fin" du labyrinthe)
        pointArrivee = new Point(0, 0);  // Default si rien trouvé
        for (int row = gp.maxScreenRow - 1; row >= 0; row--) {  // Du bas vers haut
            for (int col = gp.maxScreenCol - 1; col >= 0; col--) {  // De droite vers gauche
                if (mapTileNum[col][row] == 0) {  // Tile herbe (libre)
                    pointArrivee = new Point(col * gp.tileSize, row * gp.tileSize);
                    System.out.println("Arrivée trouvée à tile [" + col + "][" + row + "] = (" + pointArrivee.x + "," + pointArrivee.y + ")");
                    return;  // Prend le premier trouvé (le plus bas-droite)
                }
            }
        }
    }
    
    // Méthodes du backlog
    public Point getPointDepart() {
        return pointDepart;
    }
    
    public Point getPointArrivee() {
        return pointArrivee;
    }
    
    // Optionnel : Méthode pour get la grille entière (pour debug ou futures tâches)
    public int[][] getGrille() {
        return mapTileNum;  // Retourne la map 16x16
    }
    
    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);  // Dessine d'abord la map (herbe/eau)
        
        // Porte au départ (superposée sur herbe)
        if (imgPorte != null) {
            g2.drawImage(imgPorte, pointDepart.x, pointDepart.y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback : Rectangle bois-vert avec texte
            g2.setColor(new Color(139, 69, 19));  // Marron bois
            g2.fillRect(pointDepart.x, pointDepart.y, gp.tileSize, gp.tileSize);
            g2.setColor(Color.GREEN);  // Bord vert
            g2.setStroke(new BasicStroke(3));  // Bord épais
            g2.drawRect(pointDepart.x, pointDepart.y, gp.tileSize, gp.tileSize);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("PORTE", pointDepart.x + 5, pointDepart.y + 20);
        }
        
        // Trésor à l'arrivée (superposé sur herbe)
        if (imgTresor != null) {
            g2.drawImage(imgTresor, pointArrivee.x, pointArrivee.y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback : Coffre jaune/or avec texte
            g2.setColor(Color.YELLOW);  // Or
            g2.fillRect(pointArrivee.x + 4, pointArrivee.y + 4, gp.tileSize - 8, gp.tileSize - 8);  // Cadre coffre
            g2.setColor(Color.ORANGE);  // Brillant
            g2.fillRect(pointArrivee.x, pointArrivee.y, 4, gp.tileSize);  // "Couvercle"
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("TRESOR", pointArrivee.x + 5, pointArrivee.y + 20);
        }
    }
}