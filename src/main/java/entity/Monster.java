package entity;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import main.GamePanel;

import javax.imageio.ImageIO;

public class Monster extends Entity {

    GamePanel gp;
    public boolean isChaser = false; // Par défaut, un monstre n'est pas un chaser
    public int detectionRange;     // La distance à laquelle il vous "voit"
    Random random = new Random();
    private int actionLockCounter = 0;
    private final int moveDelay = 60; // Change de direction toutes les 2s

    // Listes pour les animations
    public ArrayList<BufferedImage> runAnim;
    public ArrayList<BufferedImage> attackAnim;

    // Listes pour les animations retournées (vers la gauche)
    public ArrayList<BufferedImage> runAnimLeft;
    public ArrayList<BufferedImage> attackAnimLeft;

    public String state = "wandering"; // "wandering" ou "attacking"
    public int attackRange ; // Attaque si le joueur est à 2 cases

    public Monster(GamePanel gp) {
        super(); // Appelle le constructeur de Entity
        this.gp = gp;
        this.attackRange = gp.tileSize * 2;
        // Initialisation déplacée ICI :
        this.attackRange = gp.tileSize * 2; // <-- en utilisant 'this.gp'
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
    }

    /**
     * Charge les *séries* d'images dans les listes d'animation.
     */
    public void getMonsterImage() {
        try {
            // Charger les 6 frames de course
            for (int i = 1; i <= 6; i++) {
                BufferedImage runFrame = ImageIO.read(getClass().getResourceAsStream("/monsters/run_" + i + ".png"));
                runAnim.add(runFrame);
                runAnimLeft.add(flipImage(runFrame)); // Ajouter la version retournée
            }

            // Charger les 5 frames d'attaque
            for (int i = 1; i <= 5; i++) {
                BufferedImage attackFrame = ImageIO.read(getClass().getResourceAsStream("/monsters/attack_" + i + ".png"));
                attackAnim.add(attackFrame);
                attackAnimLeft.add(flipImage(attackFrame)); // Ajouter la version retournée
            }

        } catch (IOException e) {
            System.err.println("ERREUR: Impossible de charger un sprite de monstre. Avez-vous découpé les images ?");
            e.printStackTrace();
        }
    }

    /**
     * Fonction utilitaire pour retourner une image horizontalement
     */
    private BufferedImage flipImage(BufferedImage image) {
        if (image == null) return null;
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    /**
     * Change l'état du monstre et réinitialise l'animation.
     */
    public void setState(String newState) {
        if (!this.state.equals(newState)) {
            this.state = newState;
            this.spriteNum = 0; // Réinitialise l'index de l'animation
            this.spriteCounter = 0; // Réinitialise le compteur de temps
        }
    }
 


    /**
     * IA pour les monstres "Chaser" (Poursuiveurs)
     * Gère les 3 états : balade, poursuite, attaque.
     */
    private void updateChaserAI(double distance) {
        
        if (state.equals("attacking")) {
            // Si le joueur s'échappe pendant l'attaque, on le poursuit
            if (distance > attackRange) {
                setState("chasing");
            }
            updateAttacking(); // Joue l'animation d'attaque

        } else if (state.equals("chasing")) {
            // Le joueur est-il assez proche pour ATTAQUER ?
            if (distance < attackRange) {
                setState("attacking");
            }
            // Le joueur s'est-il enfui trop loin ? (hors de vue)
            else if (distance > detectionRange) {
                setState("wandering");
            }
            // SINON, on continue de poursuivre
            else {
                updateChasingMovement(); // NOUVELLE méthode de poursuite
            }

        } else if (state.equals("wandering")) {
            // Le joueur est-il entré dans la zone de DÉTECTION ?
            if (distance < detectionRange) {
                setState("chasing");
            }
            // Sinon, on se balade
            else {
                updateWandering(); // L'ancienne méthode de balade (rebond)
            }
        }
    }


    /**
     * Calcule le mouvement optimal pour se rapprocher du joueur
     * (IA "Greedy" simple)
     */
    private void updateChasingMovement() {
        // Calcule la direction vers le joueur
        int dx = gp.player.worldx - this.worldx;
        int dy = gp.player.worldy - this.worldy;

        String primaryDirection = direction;
        String secondaryDirection = direction;

        // Choisit la direction principale (horizontale ou verticale)
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

        // Vérifie si la voie est libre
        if (gp.canMoveHere(nextWorldX, nextWorldY)) {
            worldx = nextWorldX;
            worldy = nextWorldY;
            direction = primaryDirection; // Confirme la direction
            
            // Animation de course
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum + 1) % runAnim.size();
                spriteCounter = 0;
            }
            return; // Mouvement réussi
        }

        // SINON, la voie principale est bloquée. On tente la direction secondaire.
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
            direction = secondaryDirection; // Confirme la direction
            
            // Animation de course
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum + 1) % runAnim.size();
                spriteCounter = 0;
            }
        }
        // Si les deux sont bloqués, le monstre ne bouge pas cette frame.
    }
    /**
     * La logique principale du monstre, gère les états.
     */
   
    public void update() {
        // Calcul de la distance avec le joueur
        int dx = Math.abs(gp.player.worldx - this.worldx);
        int dy = Math.abs(gp.player.worldy - this.worldy);
        double distance = Math.sqrt(dx * dx + dy * dy);

        // --- IA Basée sur le type de monstre ---
        if (isChaser) {
            // Ce monstre est un "Chaser" (Poursuiveur)
            updateChaserAI(distance);
        } else {
            // Ce monstre est un "Wanderer" (Errant)
            updateWandererAI(distance);
        }
    }


/**
     * IA pour les monstres "Wanderer" (Errants)
     * (C'est votre ancienne logique de update())
     */
    private void updateWandererAI(double distance) {
        // 1. Vérifier la distance avec le joueur pour changer d'état
        // Si le joueur est proche ET qu'on n'attaque pas déjà, on attaque
        if (distance < attackRange && !state.equals("attacking")) {
            setState("attacking");
            // Le monstre se tourne vers le joueur (simplifié)
            if(gp.player.worldx < this.worldx) direction = "left";
            else direction = "right";

        } // Si le joueur est loin ET qu'on n'est pas en train d'attaquer, on se balade
        else if (distance >= attackRange && !state.equals("attacking")) {
            setState("wandering");
        }

        // 2. Exécuter la logique de l'état actuel
        if (state.equals("wandering")) {
            updateWandering(); // Logique de balade (rebondit sur les murs)
        } else if (state.equals("attacking")) {
            updateAttacking(); // Logique d'animation d'attaque
        }
    }
   /**
     * Logique de l'état "Wandering" (se balader) - AMÉLIORÉE
     */
    private void updateWandering() {
        
        // --- Physique (collisions) ---
        int nextWorldX = worldx;
        int nextWorldY = worldy;

        switch (direction) {
            case "up": nextWorldY -= speed; break;
            case "down": nextWorldY += speed; break;
            case "left": nextWorldX -= speed; break;
            case "right": nextWorldX += speed; break;
        }

        // On vérifie la collision
        if (gp.canMoveHere(nextWorldX, nextWorldY)) {
            
            // C'EST LIBRE : On bouge
            worldx = nextWorldX;
            worldy = nextWorldY;

            // --- Animation (course) ---
            spriteCounter++;
            if (spriteCounter > 10) { // Vitesse de l'animation de course
                spriteNum = (spriteNum + 1) % runAnim.size(); // Boucle sur 6 frames
                spriteCounter = 0;
            }
        } else {
            // C'EST BLOQUÉ (MUR) : On choisit une nouvelle direction IMMÉDIATEMENT
            String[] directions = {"up", "down", "left", "right"};
            direction = directions[random.nextInt(4)];
        }

        // --- IA (changement aléatoire) ---
        // Pour qu'ils n'aillent pas en ligne droite pour toujours,
        // on ajoute un changement de direction aléatoire après un certain temps.
        actionLockCounter++;
        if (actionLockCounter >= moveDelay) {
            String[] directions = {"up", "down", "left", "right"};
            direction = directions[random.nextInt(4)];
            actionLockCounter = 0;
        }
    }

    /**
     * Logique de l'état "Attacking" (joue l'animation d'attaque)
     */
    private void updateAttacking() {
        // Le monstre s'arrête de bouger
        // Il joue son animation d'attaque
        spriteCounter++;
        if (spriteCounter > 8) { // Vitesse de l'animation d'attaque
            spriteNum = spriteNum + 1; // Avance à la frame suivante

            // Si l'animation est terminée
            if (spriteNum >= attackAnim.size()) {
                spriteNum = 0; // Réinitialise
                setState("wandering"); // Retourne se balader
            }
            spriteCounter = 0;
        }
    }

    /**
     * Dessine le bon sprite pour l'état actuel.
     */
   
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        ArrayList<BufferedImage> currentAnim = null;
        ArrayList<BufferedImage> currentAnimLeft = null;

        // Choisir le bon set d'animation (course ou attaque)
        if (state.equals("wandering")) {
            currentAnim = runAnim;
            currentAnimLeft = runAnimLeft;
        } else if (state.equals("attacking")) {
            currentAnim = attackAnim;
            currentAnimLeft = attackAnimLeft;
        }

        // S'assurer qu'on ne plante pas
        if (currentAnim == null || currentAnim.isEmpty()) return;
        if (spriteNum >= currentAnim.size()) spriteNum = 0; // Sécurité

        // Choisir l'image (frame) et la direction
        switch (direction) {
            case "left":
                image = currentAnimLeft.get(spriteNum);
                break;
            case "right":
            case "up": // On réutilise 'right' pour 'up' et 'down'
            case "down":
                image = currentAnim.get(spriteNum);
                break;
        }

        // Calcul de la position à l'écran (caméra)
        int screenX = worldx - gp.player.worldx + gp.player.screenX;
        int screenY = worldy - gp.player.worldy + gp.player.screenY;

        // Optimisation (ne pas dessiner si hors écran)
        if (worldx + gp.tileSize > gp.player.worldx - gp.player.screenX &&
            worldx - gp.tileSize < gp.player.worldx + gp.player.screenX &&
            worldy + gp.tileSize > gp.player.worldy - gp.player.screenY &&
            worldy - gp.tileSize < gp.player.worldy + gp.player.screenY) {
            
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }
}

