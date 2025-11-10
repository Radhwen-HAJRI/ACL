
package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import main.GamePanel;

import javax.imageio.ImageIO;

public class Monster extends Entity {

    GamePanel gp;
    Random random = new Random();
    private int moveCounter = 0;
    private final int moveDelay = 30; // Change direction toutes les 30 frames (~0.5s à 60 FPS)

    public Monster(GamePanel gp) {
        this.gp = gp;
        setDefaultValues();
        getMonsterImage();
    }

    public void setDefaultValues() {
        worldx = gp.tileSize * 8;  // Milieu du labyrinthe
        worldy = gp.tileSize * 8;
        speed = 2; // Plus lent que le joueur
        direction = "down";
    }

    public void getMonsterImage() {
        try {
            // Chargement depuis resources (Maven)
            up1 = up2 = down1 = down2 = left1 = left2 = right1 = right2 =
                ImageIO.read(getClass().getResourceAsStream("/monsters/monster.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        moveCounter++;
        if (moveCounter >= moveDelay) {
            // Choisir une direction aléatoire
            String[] directions = {"up", "down", "left", "right"};
            direction = directions[random.nextInt(4)];
            moveCounter = 0;
        }

        // Déplacement SANS vérification de collision
        switch (direction) {
            case "up":    screenX -= speed; break;
            case "down":  screenY += speed; break;
            case "left":  screenX -= speed; break;
            case "right": screenX += speed; break;
        }

        // Animation simple (même sprite, mais on garde le système)
        spriteCounter++;
        if (spriteCounter > 12) {
            spriteNum = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;
        }

        // Optionnel : garder dans l'écran (rebond ou wrap-around)
        if (screenX < 0) screenX = 0;
        if (screenX > gp.screenWidth - gp.tileSize) screenX = gp.screenWidth - gp.tileSize;
        if (screenY < 0) screenY = 0;
        if (screenY > gp.screenHeight - gp.tileSize) screenY = gp.screenHeight - gp.tileSize;
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch (direction) {
            case "up":    image = up1; break;
            case "down":  image = down1; break;
            case "left":  image = left1; break;
            case "right": image = right1; break;
        }
        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
    }
}