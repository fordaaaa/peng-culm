import java.util.Random;

// handles tile and bush generation for the world grid
public class WorldGeneration {

    // tile types
    public static final int TILE_GRASS = 0;
    public static final int TILE_SAND  = 1;
    public static final int TILE_PATH  = 2;

    private static final double DEFAULT_BUSH_DENSITY = 0.12; // chance to try placing a bush on a grass tile

    // generate base tiles: mostly grass with scattered sand patches
    public static int[][] generatetiles(int rows, int cols) {
        int[][] tiles = new int[rows][cols];

        // start everything as grass
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = TILE_GRASS;
            }
        }

        // carve some sand patches
        Random rng = new Random();
        int numSandPatches = Math.max(10, (rows * cols) / 150); // scale a bit with map size

        for (int i = 0; i < numSandPatches; i++) {
            int centerR = rng.nextInt(rows);
            int centerC = rng.nextInt(cols);
            int radius = 2 + rng.nextInt(2); // radius 2-3

            for (int dr = -radius; dr <= radius; dr++) {
                for (int dc = -radius; dc <= radius; dc++) {
                    int rr = centerR + dr;
                    int cc = centerC + dc;
                    if (rr < 0 || rr >= rows || cc < 0 || cc >= cols) continue;
                    if (dr * dr + dc * dc <= radius * radius) {
                        tiles[rr][cc] = TILE_SAND;
                    }
                }
            }
        }

        return tiles;
    }

    // generate bush variants for grass tiles only; -1 means no bush
    public static int[][] generatebushes(int[][] tileType) {
        int rows = tileType.length;
        int cols = tileType[0].length;
        int[][] bushes = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bushes[r][c] = -1; // default no bush
            }
        }

        Random rng = new Random();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (tileType[r][c] != TILE_GRASS) continue; // no plants on sand or path

                if (rng.nextDouble() < DEFAULT_BUSH_DENSITY && nobushnearby(bushes, r, c)) {
                    int variant = rng.nextInt(10); // 0..9 for 10 bush sprites
                    bushes[r][c] = variant;
                }
            }
        }

        return bushes;
    }

    // ensure bushes are not bunched too tightly: require empty 8-neighborhood
    private static boolean nobushnearby(int[][] bushes, int r, int c) {
        int rows = bushes.length;
        int cols = bushes[0].length;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr;
                int cc = c + dc;
                if (rr < 0 || rr >= rows || cc < 0 || cc >= cols) continue;
                if (bushes[rr][cc] >= 0) return false;
            }
        }
        return true;
    }
}
