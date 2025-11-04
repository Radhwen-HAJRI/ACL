package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;
import main.KeyHandler;

public final class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
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
            // Reset sprite si pas de mouvement (idle plus tard)
            spriteCounter++;
            if (spriteCounter > 12) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }

            // Check et bouge par direction (priorité : une seule à la fois, mais tolère diag)
            if (keyH.upPressed) {
                int nextY = y - speed;  // Nouvelle pos test
                if (gp.canMoveHere(x, nextY)) {  // Si libre
                    y = nextY;
                }
                direction = "up";
            }
            else if (keyH.downPressed) {  // 'else' pour éviter diag trop rapide ? Optionnel, enlève si tu veux diag
                int nextY = y + speed;
                if (gp.canMoveHere(x, nextY)) {
                    y = nextY;
                }
                direction = "down";
            }
            if (keyH.leftPressed) {
                int nextX = x - speed;
                if (gp.canMoveHere(nextX, y)) {
                    x = nextX;
                }
                direction = "left";
            }
            else if (keyH.rightPressed) {
                int nextX = x + speed;
                if (gp.canMoveHere(nextX, y)) {
                    x = nextX;
                }
                direction = "right";
            }

            spriteCounter++;
            if (spriteCounter > 12) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }
    }



    /*public void update() {
    
    if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

        if (keyH.upPressed) {
            direction = "up";
            y -= speed;
        }
        if (keyH.downPressed) {
            direction = "down";
            y += speed;
        }
        if (keyH.leftPressed) {
            direction = "left";
            x -= speed;
        }
        if (keyH.rightPressed) {
            direction = "right";
            x += speed;
        }

        
        spriteCounter++;
        if (spriteCounter > 12) {
            spriteNum = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;
        }
    }
}*/


    public void draw(Graphics2D g2) {
        //g2.setColor(java.awt.Color.white);
        //g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
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
        g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);

    }
}



