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
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            // Reset sprite si pas de mouvement
            spriteCounter++;
            if (spriteCounter > 12) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }

            // Check et bouge par direction
            if (keyH.upPressed) {
                int nextY = worldy - speed; 
                if (gp.canMoveHere(worldx, nextY)) {  
                    worldy = nextY;
                }
                direction = "up";
            }
            else if (keyH.downPressed) {  
                int nextY = worldy + speed;
                if (gp.canMoveHere(worldx, nextY)) {
                    worldy = nextY;
                }
                direction = "down";
            }
            if (keyH.leftPressed) {
                int nextX = worldx - speed;
                if (gp.canMoveHere(nextX, worldy)) {
                    worldx = nextX;
                }
                direction = "left";
            }
            else if (keyH.rightPressed) {
                int nextX = worldx + speed;
                if (gp.canMoveHere(nextX, worldy)) {
                    worldx = nextX;
                }
                direction = "right";
            }

            spriteCounter++;
            if (spriteCounter > 12) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }
        // Gère invincibilité 
        if (invincibleCounter > 0) {
            invincibleCounter--; 
        }
    }

    public void draw(Graphics2D g2) {
        // Flash rouge sur collision 
        if (invincibleCounter > 0) {
            float alpha = (invincibleCounter / (float) invincibleDuration);  
            g2.setColor(new Color(1.0f, 0.0f, 0.0f, alpha * 0.5f));  
            g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);  
            
            if (invincibleCounter % 4 < 2) {  
                return;  
            }
        }
        BufferedImage image = null;
        switch(direction) {
            case "up":
                if (spriteNum ==1) {
                    image = up1;
                }
                if (spriteNum ==2) {
                    image = up2;
                }
                break;
            case "down":
                if (spriteNum ==1) {
                image = down1;
            }
                if (spriteNum ==2) {
                    image = down2;  
                }
                break;
            case "left":
            
                if (spriteNum ==1) {
                image = left1;
            }
                if (spriteNum ==2) {    
                    image = left2;
                }

                break;
            case "right":
                if (spriteNum ==1) {
                    image = right1 ;
                }
                if (spriteNum ==2) {
                    image = right2;
                }

                break;
        }
        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);

    }
}



