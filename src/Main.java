
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Load the level you want to edit
        String levelFile = "level1.json";
        Level level = AssetHandler.loadLevel(levelFile);

        if (level != null) {
            // Use SwingUtilities.invokeLater to ensure the GUI is created on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                TileMapEditor editor = new TileMapEditor(level, levelFile);
                editor.setVisible(true);
            });
        } else {
            System.out.println("Failed to load level: " + levelFile);
        }
    }
}
