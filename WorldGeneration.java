import java.util.Random;

public class WorldGeneration {
    public static final int TILE_GRASS = 0;
    public static final int TILE_SAND  = 1;
    public static final int TILE_PATH  = 2;

    // description: generate a simple world (mostly grass with some sand blobs)
    // parms: rows (world height), cols (world width)
    // returns: 2d array of tile types
    public static int[][] generatetiles(int rows, int cols) {
        int[][] tiles = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = TILE_GRASS;
            }
        }
        Random rng = new Random();
        int numSandPatches = Math.max(10, (rows * cols) / 150);
        for (int i = 0; i < numSandPatches; i++) {
            int centerR = rng.nextInt(rows);
            int centerC = rng.nextInt(cols);
            int radius = 2 + rng.nextInt(2);
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
}
