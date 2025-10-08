package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
        // Implementation for loading player images goes here
        try {
            up1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
            up2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
            down1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
            down2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
            left1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
            left2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
            right1 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
            right2 = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/player/Walk.png"));
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void update() {
        if (keyH.upPressed == true ){
            direction = "up";
             y -= speed ;
        }
        if (keyH.downPressed== true ){
            direction = "down";
             y += speed ;
        }
        if (keyH.leftPressed == true ){

            direction = "left";
            x -= speed;
        } 
        if (keyH.rightPressed == true ){
            direction = "right";
            x += speed;
        } 
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch(direction) {
            case "up":
                image = up1;
                break;
            case "down":
                image = down1;
                break;
            case "left":
                image = left1;
                break;
            case "right":
                image = right1;
                break;
        }
        g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);

    }
}



