package entity;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import main.GamePanel;

public class Monster extends Entity {

    GamePanel gp;
    public boolean isChaser = false;
    public int detectionRange = 300; // Ajout d'une valeur par défaut
    Random random = new Random();
    private int actionLockCounter = 0;
    private final int moveDelay = 60;

    // Listes pour les animations
    public ArrayList<BufferedImage> runAnim;
    public ArrayList<BufferedImage> attackAnim;
    public ArrayList<BufferedImage> runAnimLeft;
    public ArrayList<BufferedImage> attackAnimLeft;

    public String state = "wandering";
    public int attackRange;

    public Monster(GamePanel gp) {
        super();
        this.gp = gp;
        this.attackRange = gp.tileSize * 2;
        
        // Initialiser les listes
        runAnim = new ArrayList<>();
        attackAnim = new ArrayList<>();
        runAnimLeft = new ArrayList<>();
        attackAnimLeft = new ArrayList<>();

        setDefaultValues();
        getMonsterImage();
    }

    public void setDefaultValues() {
        speed = 1;
        direction = "down";
        worldx = 100; // Position initiale par défaut
        worldy = 100;
    }

    public void getMonsterImage() {
        try {
            // Charger les 6 frames de course
            for (int i = 1; i <= 6; i++) {
                BufferedImage runFrame = ImageIO.read(getClass().getResourceAsStream("/monsters/run_" + i + ".png"));
                runAnim.add(runFrame);
                runAnimLeft.add(flipImage(runFrame));
            }

            // Charger les 5 frames d'attaque
            for (int i = 1; i <= 5; i++) {
                BufferedImage attackFrame = ImageIO.read(getClass().getResourceAsStream("/monsters/attack_" + i + ".png"));
                attackAnim.add(attackFrame);
                attackAnimLeft.add(flipImage(attackFrame));
            }

        } catch (IOException e) {
            System.err.println("ERREUR: Impossible de charger un sprite de monstre.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERREUR: Problème avec les ressources d'image.");
            e.printStackTrace();
        }
    }

    private BufferedImage flipImage(BufferedImage image) {
        if (image == null) return null;
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    public void setState(String newState) {
        if (!this.state.equals(newState)) {
            this.state = newState;
            this.spriteNum = 1; // Commencer à 1 au lieu de 0 pour éviter IndexOutOfBounds
            this.spriteCounter = 0;
        }
    }

    public void update() {
        // Calcul de la distance avec le joueur
        int dx = Math.abs(gp.player.worldx - this.worldx);
        int dy = Math.abs(gp.player.worldy - this.worldy);
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (isChaser) {
            updateChaserAI(distance);
        } else {
            updateWandererAI(distance);
        }
    }

    private void updateChaserAI(double distance) {
        if (state.equals("attacking")) {
            if (distance > attackRange) {
                setState("chasing");
            }
            updateAttacking();

        } else if (state.equals("chasing")) {
            if (distance < attackRange) {
                setState("attacking");
            } else if (distance > detectionRange) {
                setState("wandering");
            } else {
                updateChasingMovement();
            }

        } else if (state.equals("wandering")) {
            if (distance < detectionRange) {
                setState("chasing");
            } else {
                updateWandering();
            }
        }
    }

    private void updateChasingMovement() {
        int dx = gp.player.worldx - this.worldx;
        int dy = gp.player.worldy - this.worldy;

        String primaryDirection = direction;
        String secondaryDirection = direction;

        if (Math.abs(dx) > Math.abs(dy)) {
            primaryDirection = (dx > 0) ? "right" : "left";
            secondaryDirection = (dy > 0) ? "down" : "up";
        } else {
            primaryDirection = (dy > 0) ? "down" : "up";
            secondaryDirection = (dx > 0) ? "right" : "left";
        }

        // Tente de bouger dans la direction principale
        int nextWorldX = worldx;
        int nextWorldY = worldy;

        switch (primaryDirection) {
            case "up": nextWorldY -= speed; break;
            case "down": nextWorldY += speed; break;
            case "left": nextWorldX -= speed; break;
            case "right": nextWorldX += speed; break;
        }

        if (gp.canMoveHere(nextWorldX, nextWorldY)) {
            worldx = nextWorldX;
            worldy = nextWorldY;
            direction = primaryDirection;
            updateAnimation();
            return;
        }

        // Tente la direction secondaire
        nextWorldX = worldx;
        nextWorldY = worldy;
        switch (secondaryDirection) {
            case "up": nextWorldY -= speed; break;
            case "down": nextWorldY += speed; break;
            case "left": nextWorldX -= speed; break;
            case "right": nextWorldX += speed; break;
        }
        
        if (gp.canMoveHere(nextWorldX, nextWorldY)) {
            worldx = nextWorldX;
            worldy = nextWorldY;
            direction = secondaryDirection;
            updateAnimation();
        }
    }

    private void updateWandererAI(double distance) {
        if (distance < attackRange && !state.equals("attacking")) {
            setState("attacking");
            if(gp.player.worldx < this.worldx) direction = "left";
            else direction = "right";
        } else if (distance >= attackRange && !state.equals("attacking")) {
            setState("wandering");
        }

        if (state.equals("wandering")) {
            updateWandering();
        } else if (state.equals("attacking")) {
            updateAttacking();
        }
    }

    private void updateWandering() {
        int nextWorldX = worldx;
        int nextWorldY = worldy;

        switch (direction) {
            case "up": nextWorldY -= speed; break;
            case "down": nextWorldY += speed; break;
            case "left": nextWorldX -= speed; break;
            case "right": nextWorldX += speed; break;
        }

        if (gp.canMoveHere(nextWorldX, nextWorldY)) {
            worldx = nextWorldX;
            worldy = nextWorldY;
            updateAnimation();
        } else {
            String[] directions = {"up", "down", "left", "right"};
            direction = directions[random.nextInt(4)];
        }

        actionLockCounter++;
        if (actionLockCounter >= moveDelay) {
            String[] directions = {"up", "down", "left", "right"};
            direction = directions[random.nextInt(4)];
            actionLockCounter = 0;
        }
    }

    private void updateAttacking() {
        spriteCounter++;
        if (spriteCounter > 8) {
            spriteNum = spriteNum + 1;
            if (spriteNum >= attackAnim.size()) {
                spriteNum = 0;
                setState("wandering");
            }
            spriteCounter = 0;
        }
    }

    private void updateAnimation() {
        spriteCounter++;
        if (spriteCounter > 10) {
            if (state.equals("wandering") || state.equals("chasing")) {
                spriteNum = (spriteNum + 1) % runAnim.size();
            }
            spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        ArrayList<BufferedImage> currentAnim = null;
        ArrayList<BufferedImage> currentAnimLeft = null;

        if (state.equals("wandering") || state.equals("chasing")) {
            currentAnim = runAnim;
            currentAnimLeft = runAnimLeft;
        } else if (state.equals("attacking")) {
            currentAnim = attackAnim;
            currentAnimLeft = attackAnimLeft;
        }

        if (currentAnim == null || currentAnim.isEmpty() || 
            spriteNum >= currentAnim.size() || spriteNum < 0) {
            return;
        }

        switch (direction) {
            case "left":
                image = currentAnimLeft.get(spriteNum);
                break;
            case "right":
            case "up":
            case "down":
                image = currentAnim.get(spriteNum);
                break;
        }

        if (image != null) {
            int screenX = worldx - gp.player.worldx + gp.player.screenX;
            int screenY = worldy - gp.player.worldy + gp.player.screenY;

            if (isOnScreen(screenX, screenY)) {
                g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
            }
        }
    }

    private boolean isOnScreen(int screenX, int screenY) {
        return screenX + gp.tileSize > 0 &&
               screenX - gp.tileSize < gp.screenWidth &&
               screenY + gp.tileSize > 0 &&
               screenY - gp.tileSize < gp.screenHeight;
    }
}