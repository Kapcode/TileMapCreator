
import java.util.List;

public class Level {
    String levelName;
    PlayerStart playerStart;
    List<List<Integer>> tilemap;
    List<Enemy> enemies;
    List<Collectible> collectibles;
    String nextLevel;
}
