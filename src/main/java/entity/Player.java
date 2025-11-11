package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;
import main.KeyHandler;

public final class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;
    public int health = 3; 
    public int invincibleCounter = 0;  
    public final int invincibleDuration = 60;  

    public String state = "walking"; 
    public boolean hasHitThisSwing = false; 
    public int attackSpriteCounter = 0; 

    // Images d'attaque
    public BufferedImage attackUp1, attackUp2, attackDown1, attackDown2,
                         attackLeft1, attackLeft2, attackRight1, attackRight2;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);
        setDefaultValues();
        getPlayerImage();
        health = 3;

    }

    public void setDefaultValues() {
        worldx = gp.tileSize * 23;
        worldy = gp.tileSize * 21;
        speed = 4;
        direction = "down";

    }

    public void getPlayerImage() {
        
        try {
            up1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_up_1.png"));
            up2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_up_2.png"));
            down1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_down_1.png"));
            down2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_down_2.png"));
            left1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_left_1.png"));
            left2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_left_2.png"));
            right1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_right_1.png"));
            right2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_right_2.png"));
            attackUp1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_up_1.png"));
            attackUp2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_up_2.png"));
            attackDown1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_down_1.png"));
            attackDown2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_down_2.png"));
            attackLeft1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_left_1.png"));
            attackLeft2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_left_2.png"));
            attackRight1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_right_1.png"));
            attackRight2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/player/boy_pick_right_2.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/**
     * Gère le timing de l'animation d'attaque (2 frames)
     */
    public void runAttackAnimation() {
        attackSpriteCounter++;
        
        // Vitesse de l'animation (change toutes les 10 frames de jeu)
        if (attackSpriteCounter > 10) { 
            if (spriteNum == 1) {
                spriteNum = 2; // Passe à la 2ème frame d'attaque
            }
            else if (spriteNum == 2) {
                spriteNum = 1; // Revient à la 1ère
                state = "walking"; // L'animation d'attaque est terminée
            }
            attackSpriteCounter = 0;
        }
    }

   
    public void update() {
        if (state.equals("attacking")) {
            // Si on est en train d'attaquer, on joue l'animation
            runAttackAnimation();
        } 
        else if (keyH.attackPressed) {
            // Si on appuie sur Espace (et qu'on n'attaque pas déjà)
            state = "attacking";
            keyH.attackPressed = false; // "Consomme" la pression
            attackSpriteCounter = 0;    // Réinitialise l'animation
            spriteNum = 1;              // Commence à la frame 1
            hasHitThisSwing = false;    // N'a pas encore touché
        }
        else if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            // --- C'est votre ancien code de mouvement, maintenant dans un "else if" ---
            state = "walking";
            
            // Check et bouge par direction
            if (keyH.upPressed) {
                int nextY = worldy - speed; 
                if (gp.canMoveHere(worldx, nextY)) { worldy = nextY; }
                direction = "up";
            }
            else if (keyH.downPressed) {  
                int nextY = worldy + speed;
                if (gp.canMoveHere(worldx, nextY)) { worldy = nextY; }
                direction = "down";
            }
            else if (keyH.leftPressed) {
                int nextX = worldx - speed;
                if (gp.canMoveHere(nextX, worldy)) { worldx = nextX; }
                direction = "left";
            }
            else if (keyH.rightPressed) {
                int nextX = worldx + speed;
                if (gp.canMoveHere(nextX, worldy)) { worldx = nextX; }
                direction = "right";
            }

            // Animation de marche
            spriteCounter++;
            if (spriteCounter > 12) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
             state = "walking"; // Ou "idle"
             // spriteNum = 1; // Optionnel : revenir à la frame 1 si immobile
        }
        
        // Gère l'invincibilité (votre code existant)
        if (invincibleCounter > 0) {
            invincibleCounter--; 
        }
    }


    public void draw(Graphics2D g2) {
        
        // --- 1. LOGIQUE D'INVINCIBILITÉ (votre code existant) ---
        if (invincibleCounter > 0) {
            float alpha = (invincibleCounter / (float) invincibleDuration);  
            g2.setColor(new Color(1.0f, 0.0f, 0.0f, alpha * 0.5f));  
            g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);  
            
            if (invincibleCounter % 4 < 2) { // Clignote
                return;  // Ne dessine pas le sprite pour le faire "disparaître"
            }
        }

        // --- 2. LOGIQUE DE SÉLECTION D'IMAGE ---
        BufferedImage image = null;

        if (state.equals("attacking")) {
            // Le joueur attaque : choisir les sprites d'attaque
            switch(direction) {
                case "up":
                    image = (spriteNum == 1) ? attackUp1 : attackUp2;
                    break;
                case "down":
                    image = (spriteNum == 1) ? attackDown1 : attackDown2;
                    break;
                case "left":
                    image = (spriteNum == 1) ? attackLeft1 : attackLeft2;
                    break;
                case "right":
                    image = (spriteNum == 1) ? attackRight1 : attackRight2;
                    break;
            }
        } else {
            // Le joueur marche : choisir les sprites de marche (votre ancien code)
            switch(direction) {
                case "up":
                    image = (spriteNum == 1) ? up1 : up2;
                    break;
                case "down":
                    image = (spriteNum == 1) ? down1 : down2;
                    break;
                case "left":
                    image = (spriteNum == 1) ? left1 : left2;
                    break;
                case "right":
                    image = (spriteNum == 1) ? right1 : right2;
                    break;
            }
        }
        
        // --- 3. DESSIN FINAL ---
        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
    }
}



