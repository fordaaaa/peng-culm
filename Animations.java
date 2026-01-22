import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Animations {
    public static final int sprite_size = 24;
    
    private static Image[][] character_frames = new Image[4][24];
    private static Image[] explosion_frames = new Image[7];
    private static Image[] tile_frames = new Image[6];
    private static Image skyborder;
    
    private static String[] character_files = {"doux.png", "mort.png", "tard.png", "vita.png"};
    
    // description: load all sprites once
    // parms: none
    // returns: none
    static {
        load_sprites();
    }
    
    // description: load spritesheets and images
    // parms: none
    // returns: none
    private static void load_sprites() {
        load_characters();
        load_explosion();
        load_tiles();
        load_skyborder();
    }
    
    // description: load 4 character sheets into frame arrays
    // parms: none
    // returns: none
    private static void load_characters() {
        for (int charIdx = 0; charIdx < 4; charIdx++) {
            try {
                BufferedImage sheet = ImageIO.read(new File("util/Sprites/" + character_files[charIdx]));
                if (sheet == null) continue;
                for (int frame = 0; frame < 24; frame++) {
                    int x = frame * sprite_size;
                    if (x + sprite_size <= sheet.getWidth()) {
                        character_frames[charIdx][frame] = sheet.getSubimage(x, 0, sprite_size, sprite_size);
                    }
                }
            } catch (IOException e) {}
        }
    }
    
    // description: load explosion sheet (1 row, 7 frames)
    // parms: none
    // returns: none
    private static void load_explosion() {
        try {
            BufferedImage sheet = ImageIO.read(new File("util/Sprites/explosionsheet.png"));
            if (sheet != null) {
                for (int i = 0; i < 7; i++) {
                    int x = i * sprite_size;
                    if (x + sprite_size <= sheet.getWidth()) {
                        explosion_frames[i] = sheet.getSubimage(x, 0, sprite_size, sprite_size);
                    }
                }
            }
        } catch (IOException e) {}
    }
    
    // description: load tile pngs (grass/sand/path)
    // parms: none
    // returns: none
    private static void load_tiles() {
        String[] tile_files = {"basicgrass.png", "grasstwo.png", "grass3.png", "sand1.png", "sand2.png", "pth.png"};
        for (int i = 0; i < 6; i++) {
            try {
                BufferedImage img = ImageIO.read(new File("util/Sprites/Tiles/" + tile_files[i]));
                if (img != null) {
                    tile_frames[i] = img.getScaledInstance(sprite_size, sprite_size, Image.SCALE_SMOOTH);
                }
            } catch (IOException e) {}
        }
    }

    // description: load sky border image for out-of-bounds tiles
    // parms: none
    // returns: none
    private static void load_skyborder() {
        try {
            BufferedImage img = ImageIO.read(new File("util/Sprites/skyborder.jpg"));
            if (img == null) return;
            // scale to one tile; we draw it per out-of-bounds cell
            skyborder = img.getScaledInstance(sprite_size, sprite_size, Image.SCALE_SMOOTH);
        } catch (IOException e) {}
    }
    
    // description: get idle frame (0-5 loop)
    // parms: charIdx (0-3), frameIndex (any int)
    // returns: image for idle
    public static Image getcharacter_idle(int charIdx, int frameIndex) {
        if (charIdx < 0 || charIdx >= 4) return null;
        int idx = Math.floorMod(frameIndex, 6);
        return character_frames[charIdx][idx];
    }
    
    // description: get walk frame (6-13 loop)
    // parms: charIdx (0-3), frameIndex (any int)
    // returns: image for walk
    public static Image getcharacter_walk(int charIdx, int frameIndex) {
        if (charIdx < 0 || charIdx >= 4) return null;
        int idx = 6 + Math.floorMod(frameIndex, 8);
        return character_frames[charIdx][idx];
    }
    
    // description: get action frame (14-17)
    // parms: charIdx (0-3), frameIndex (any int)
    // returns: image for action
    public static Image getcharacter_action(int charIdx, int frameIndex) {
        if (charIdx < 0 || charIdx >= 4) return null;
        int idx = 14 + Math.floorMod(frameIndex, 4);
        return character_frames[charIdx][idx];
    }
    
    // description: get special frame (18-23)
    // parms: charIdx (0-3), frameIndex (any int)
    // returns: image for special
    public static Image getcharacter_special(int charIdx, int frameIndex) {
        if (charIdx < 0 || charIdx >= 4) return null;
        int idx = 18 + Math.floorMod(frameIndex, 6);
        return character_frames[charIdx][idx];
    }
    
    // description: get explosion frame (0-6)
    // parms: frameIndex (0-6)
    // returns: explosion image
    public static Image getexplosion(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= 7) return null;
        return explosion_frames[frameIndex];
    }
    
    // description: get a grass tile variant
    // parms: world_row, world_col
    // returns: grass tile image or null
    public static Image gettile_grass(int world_row, int world_col) {
        int variant = Math.abs(world_row + world_col) % 3;
        if (variant >= tile_frames.length || tile_frames[variant] == null) return null;
        return tile_frames[variant];
    }
    
    // description: get a sand tile variant
    // parms: world_row, world_col
    // returns: sand tile image or null
    public static Image gettile_sand(int world_row, int world_col) {
        int variant = 3 + (Math.abs(world_row + world_col) % 2);
        if (variant >= tile_frames.length || tile_frames[variant] == null) return null;
        return tile_frames[variant];
    }
    
    // description: get the path tile
    // parms: none
    // returns: path tile image or null
    public static Image gettile_path() {
        if (tile_frames[5] == null) return null;
        return tile_frames[5];
    }

    // description: get sky border tile image
    // parms: none
    // returns: sky border image or null
    public static Image getskyborder() {
        return skyborder;
    }
}
