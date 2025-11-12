package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;
import main.KeyHandler;
import main.SoundManager;

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
    public int keyCount = 0;  // nombre de clés que le joueur possède


    private String lastDirection = "";

    // Images d'attaque
    public BufferedImage attackUp1, attackUp2, attackDown1, attackDown2,
                         attackLeft1, attackLeft2, attackRight1, attackRight2;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        solidAreaDefaultX = 8;
        solidAreaDefaultY = 16; 
        
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

    public void runAttackAnimation() {
        attackSpriteCounter++;
        
        // Vitesse de l'animation 
        if (attackSpriteCounter > 10) { 
            if (spriteNum == 1) {
                spriteNum = 2; 
            }
            else if (spriteNum == 2) {
                spriteNum = 1; 
                state = "walking"; 
            }
            attackSpriteCounter = 0;
        }
    }

   
    public void update() {
        if (state.equals("attacking")) {
            runAttackAnimation();
        } 
        else if (keyH.attackPressed) {
            state = "attacking";
            keyH.attackPressed = false; 
            attackSpriteCounter = 0;    
            spriteNum = 1;  
            hasHitThisSwing = false; 
        }
        else if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            state = "walking";
            
            if (keyH.upPressed) {
                int nextY = worldy - speed; 
                if (gp.canMoveHere(worldx, nextY)) { worldy = nextY; }
                direction = "up";
            }
            if (keyH.downPressed) {  
                int nextY = worldy + speed;
                if (gp.canMoveHere(worldx, nextY)) { worldy = nextY; }
                direction = "down";
            }
            if (keyH.leftPressed) {
                int nextX = worldx - speed;
                if (gp.canMoveHere(nextX, worldy)) { worldx = nextX; }
                direction = "left";
            }
            if (keyH.rightPressed) {
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
             state = "walking"; 
        }

        // Son mouvement sur changement de direction
        if (!direction.equals(lastDirection) && (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed)) {
            gp.soundManager.playMove();  // Joue si direction changée
        }
        lastDirection = direction;
        
        // Gère l'invincibilité
        if (invincibleCounter > 0) {
            invincibleCounter--; 
        }
    }


    public void draw(Graphics2D g2) {
        
        if (invincibleCounter > 0) {
            float alpha = (invincibleCounter / (float) invincibleDuration);  
            g2.setColor(new Color(1.0f, 0.0f, 0.0f, alpha * 0.5f));  
            g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);  
            
            if (invincibleCounter % 4 < 2) { 
                return;
            }
        }

        BufferedImage image = null;

        if (state.equals("attacking")) {
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
        
        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
    }
}



