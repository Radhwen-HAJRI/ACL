
package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import main.GamePanel;

public class Fire extends Entity {

    GamePanel gp;
    ArrayList<BufferedImage> fireAnim;
    
    public Fire(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.worldx = x;
        this.worldy = y;
        
        // Initialiser la liste d'animation
        fireAnim = new ArrayList<>();
        getFireImage();
    }

    public void getFireImage() {
        try {
            // Chargement des 6 images de feu
            for (int i = 1; i <= 6; i++) {
                BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/Feu/Feu_" + i + ".png"));
                fireAnim.add(image);
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement image feu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update() {
        // Gérer l'animation
        spriteCounter++;
        if (spriteCounter > 10) { // Vitesse de l'animation
            spriteNum++;
            if (spriteNum >= fireAnim.size()) {
                spriteNum = 0;
            }
            spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = worldx - gp.player.worldx + gp.player.screenX;
        int screenY = worldy - gp.player.worldy + gp.player.screenY;

        // Ne dessiner que si visible à l'écran (Optimisation)
        if (worldx + gp.tileSize > gp.player.worldx - gp.player.screenX &&
            worldx - gp.tileSize < gp.player.worldx + gp.player.screenX &&
            worldy + gp.tileSize > gp.player.worldy - gp.player.screenY &&
            worldy - gp.tileSize < gp.player.worldy + gp.player.screenY) {

            if (!fireAnim.isEmpty()) {
                g2.drawImage(fireAnim.get(spriteNum), screenX, screenY, gp.tileSize, gp.tileSize, null);
            }
        }
    }
}