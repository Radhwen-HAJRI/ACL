package test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entity.Player;
import main.GamePanel;
import main.KeyHandler;

class PlayerTest {

    private GamePanel gp;
    private KeyHandler keyH;
    private Player player;

    @BeforeEach
    void setUp() {
        gp = new GamePanel();
        keyH = new KeyHandler();
        player = new Player(gp, keyH);
    }

    @Test
    void testPlayerInitialization() {
        assertNotNull(player);
        assertEquals("down", player.direction);
        assertEquals(4, player.speed);
        
        // Vérifier que screenX et screenY sont calculés correctement
        int expectedScreenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        int expectedScreenY = gp.screenHeight / 2 - (gp.tileSize / 2);
        assertEquals(expectedScreenX, player.screenX);
        assertEquals(expectedScreenY, player.screenY);
    }

    @Test
    void testPlayerImagesLoaded() {
        // Vérifier que toutes les images de sprites sont chargées
        // Note: Ces tests peuvent échouer si les images ne sont pas disponibles dans l'environnement de test
        try {
            assertNotNull(player.up1, "up1 image should be loaded");
            assertNotNull(player.up2, "up2 image should be loaded");
            assertNotNull(player.down1, "down1 image should be loaded");
            assertNotNull(player.down2, "down2 image should be loaded");
            assertNotNull(player.left1, "left1 image should be loaded");
            assertNotNull(player.left2, "left2 image should be loaded");
            assertNotNull(player.right1, "right1 image should be loaded");
            assertNotNull(player.right2, "right2 image should be loaded");
        } catch (Exception e) {
            System.out.println("Image loading test skipped: " + e.getMessage());
        }
    }

    @Test
    void testDefaultPosition() {
        // Vérifier la position initiale du joueur
        int expectedWorldX = gp.tileSize * 23;
        int expectedWorldY = gp.tileSize * 21;
        assertEquals(expectedWorldX, player.worldx);
        assertEquals(expectedWorldY, player.worldy);
    }

    @Test
    void testPlayerMovementUpdate() {
        // Tester que la méthode update ne crash pas
        assertDoesNotThrow(() -> player.update());
        
        // Tester avec différentes touches pressées
        keyH.upPressed = true;
        assertDoesNotThrow(() -> player.update());
        
        keyH.downPressed = true;
        assertDoesNotThrow(() -> player.update());
        
        keyH.leftPressed = true;
        assertDoesNotThrow(() -> player.update());
        
        keyH.rightPressed = true;
        assertDoesNotThrow(() -> player.update());
    }

    @Test
    void testDirectionChange() {
        // Tester le changement de direction
        keyH.upPressed = true;
        player.update();
        assertEquals("up", player.direction);
        
        keyH.upPressed = false;
        keyH.downPressed = true;
        player.update();
        assertEquals("down", player.direction);
        
        keyH.downPressed = false;
        keyH.leftPressed = true;
        player.update();
        assertEquals("left", player.direction);
        
        keyH.leftPressed = false;
        keyH.rightPressed = true;
        player.update();
        assertEquals("right", player.direction);
    }

    @Test
    void testSpriteAnimation() {
        // Tester que l'animation du sprite fonctionne
        keyH.upPressed = true;
        
        int initialSpriteNum = player.spriteNum;
        
        // Simuler plusieurs updates pour faire avancer l'animation
        for (int i = 0; i < 15; i++) {
            player.update();
        }
        
        // Le spriteNum devrait avoir changé ou le compteur avoir avancé
        assertTrue(player.spriteNum != initialSpriteNum || player.spriteCounter > 0, 
            "Sprite animation should advance with movement");
    }

    @Test
    void testPlayerFinalClass() {
        // Vérifier que la classe est bien final
        int modifiers = Player.class.getModifiers();
        assertTrue(Modifier.isFinal(modifiers), "Player class should be final");
    }

    @Test
    void testMultipleUpdatesWithoutMovement() {
        // Tester plusieurs updates sans mouvement
        keyH.upPressed = false;
        keyH.downPressed = false;
        keyH.leftPressed = false;
        keyH.rightPressed = false;
        
        int initialWorldX = player.worldx;
        int initialWorldY = player.worldy;
        
        for (int i = 0; i < 10; i++) {
            player.update();
        }
        
        // La position ne devrait pas changer sans touches pressées
        assertEquals(initialWorldX, player.worldx);
        assertEquals(initialWorldY, player.worldy);
    }

    
}