
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Construct the full path to the initial level file in its new location
        String initialLevelPath = new File("resources/level1.json").getAbsolutePath();
        Level level = AssetHandler.loadLevel(initialLevelPath);

        if (level != null) {
            // Use SwingUtilities.invokeLater to ensure the GUI is created on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                TileMapEditor editor = new TileMapEditor(level, initialLevelPath);
                editor.setVisible(true);
            });
        } else {
            // Use a dialog box for unmissable feedback
            JOptionPane.showMessageDialog(null, 
                "Failed to load initial level file.\n" +
                "Please ensure the file exists at the following path:\n" +
                initialLevelPath,
                "Error Loading Level", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
