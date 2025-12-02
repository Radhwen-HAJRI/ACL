package tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import main.GamePanel;

public class TileManager {
    
    public GamePanel gp;
    public Tile[] tile;
    public int mapTileNum[][][];

    public TileManager(GamePanel gp) {

        this.gp = gp;
        tile = new Tile[50];
        mapTileNum = new int[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];
        getTileImage();
        loadMap("/maps/map01.txt",0);
        loadMap("/maps/map02.txt", 1);
    }
    
    private void loadTile(int index, String filename, boolean collision) throws IOException {
    InputStream is = getClass().getResourceAsStream("/tiles/" + filename);
    if (is == null) {
        throw new IOException("⚠ Image introuvable : /tiles/" + filename);
    }
    tile[index] = new Tile();
    tile[index].image = ImageIO.read(is);
    tile[index].collision = collision;
}

    

    public void getTileImage() {
        try {
            loadTile(0, "grass0.png", false);
            loadTile(1, "wall.png", true);
            loadTile(2, "water.png", true);
            loadTile(3, "earth.png", false);
            loadTile(4, "tree.png", true);
            loadTile(5, "sand.png", false);
            loadTile(6, "key.png", false);
            loadTile(7, "door.png", true);
            loadTile(8, "minidoor.png", false);
            loadTile(9, "coin.png", false);
            loadTile(10, "black.png", false);
            loadTile(11, "black.png", false);
            loadTile(12, "water00.png", true);
            loadTile(13, "water01.png", true);
            loadTile(14, "water00.png", true);
            loadTile(15, "water00.png", true);
            loadTile(16, "water00.png", true);
            loadTile(17, "water00.png", true);
            loadTile(18, "water00.png", true);
            loadTile(19, "water00.png", true);
            loadTile(20, "water00.png", true);
            loadTile(21, "water00.png", true);
            loadTile(22, "water00.png", true);
            loadTile(23, "water00.png", true);
            loadTile(24, "water00.png", true);
            loadTile(25, "water00.png", true);
            loadTile(26, "black.png", false);
            loadTile(27, "wall.png", true);
            loadTile(28, "wall.png", true);
            loadTile(29, "wall.png", true);
            loadTile(30, "wall.png", true);
            loadTile(31, "wall.png", true);
            loadTile(32, "wall.png", true);
            loadTile(33, "wall.png", true);
            loadTile(34, "wall.png", true);
            loadTile(35, "wall.png", true);
            loadTile(36, "wall.png", true);
            loadTile(37, "wall.png", true);
            loadTile(38, "wall.png", true);
            loadTile(39, "earth.png", false);
            loadTile(40, "wall.png", true);
            loadTile(41, "lava.png", false);
            loadTile(42, "tree.png", true);
            loadTile(43, "tree.png", true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadMap(String filePath, int map) {
        try {

            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            int col = 0;
            int row = 0;
            while (row < gp.maxWorldRow) {
                String line = br.readLine();
                while (col < gp.maxWorldCol) {
                    String numbers[] = line.split(" ");
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[map][col][row] = num;
                    col++;
                }
                if (col == gp.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[gp.currentMap][worldCol][worldRow];
            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldx + gp.player.screenX;
            int screenY = worldY - gp.player.worldy + gp.player.screenY;

            if (worldX + gp.tileSize > gp.player.worldx - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldx + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldy - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldy + gp.player.screenY) {

                
                if (tileNum != 6 && tileNum != 9) {
                    g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                } else {
                    
                    if (tileNum == 6 || tileNum == 9) {
                        
                        g2.drawImage(tile[0].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                    }
                }
            }

            worldCol++;

            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}