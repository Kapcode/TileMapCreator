
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TileMapEditor extends JFrame {
    private Level level;
    private String currentFilePath;
    private final Map<Integer, Color> colorMap;

    private final JTextField paintValueField;
    private ToolMode currentTool = ToolMode.RECTANGLE_SELECT;

    private enum ToolMode { RECTANGLE_SELECT, BRUSH, ERASER }

    public TileMapEditor(Level level, String filePath) {
        this.level = level;
        this.currentFilePath = filePath;
        this.colorMap = new LinkedHashMap<>();
        setupColorMap();

        setTitle("Tile Map Editor - " + new File(currentFilePath).getName());
        setAppIcon();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridPanel gridPanel = new GridPanel(level, colorMap);
        JScrollPane scrollPane = new JScrollPane(gridPanel);

        // --- Menu Bar ---
        setJMenuBar(createMenuBar());

        // --- Tool Panel (Left) ---
        paintValueField = new JTextField("1", 5);
        paintValueField.setMaximumSize(new Dimension(100, 30));

        JToggleButton selectButton = new JToggleButton("Select", true);
        JToggleButton brushButton = new JToggleButton("Brush");
        JToggleButton eraserButton = new JToggleButton("Eraser");

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(selectButton); toolGroup.add(brushButton); toolGroup.add(eraserButton);

        selectButton.addActionListener(e -> currentTool = ToolMode.RECTANGLE_SELECT);
        brushButton.addActionListener(e -> currentTool = ToolMode.BRUSH);
        eraserButton.addActionListener(e -> currentTool = ToolMode.ERASER);

        JButton paintButton = new JButton("Paint Selection");
        paintButton.addActionListener(e -> gridPanel.paintSelection());

        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
        toolPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolButtonsPanel = new JPanel(new GridLayout(1, 3));
        toolButtonsPanel.add(selectButton); toolButtonsPanel.add(brushButton); toolButtonsPanel.add(eraserButton);
        toolPanel.add(new JLabel("Tools:"));
        toolPanel.add(toolButtonsPanel);
        toolPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        toolPanel.add(new JLabel("Paint Value:"));
        toolPanel.add(paintValueField);
        toolPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        toolPanel.add(paintButton);
        toolPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel legendPanel = createLegendPanel();
        toolPanel.add(legendPanel);
        toolPanel.add(Box.createVerticalGlue());

        getContentPane().add(toolPanel, BorderLayout.WEST);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setSize(900, 600);
        setLocationRelativeTo(null);
    }
    private void setAppIcon() {
        List<Image> icons = new ArrayList<>();
        // Load icons in different sizes, from smallest to largest
        String[] iconPaths = { "/icons/icon_16.png", "/icons/icon_32.png", "/icons/icon_64.png" };

        for (String path : iconPaths) {
            URL iconURL = getClass().getResource(path);
            if (iconURL != null) {
                icons.add(new ImageIcon(iconURL).getImage());
            } else {
                System.err.println("Warning: Could not find icon resource: " + path);
            }
        }

        if (!icons.isEmpty()) {
            setIconImages(icons);
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveFileAs());
        fileMenu.add(saveAsItem);

        return menuBar;
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser("resources");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Level newLevel = AssetHandler.loadLevel(file.getAbsolutePath());
            if (newLevel != null) {
                new TileMapEditor(newLevel, file.getAbsolutePath()).setVisible(true);
                this.dispose(); // Close the current window
            } else {
                JOptionPane.showMessageDialog(this, "Could not load level file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        AssetHandler.saveLevel(level, currentFilePath);
        JOptionPane.showMessageDialog(this, "File saved successfully!");
    }

    private void saveFileAs() {
        JFileChooser fileChooser = new JFileChooser("resources");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String newPath = file.getAbsolutePath();
            if (!newPath.toLowerCase().endsWith(".json")) {
                newPath += ".json";
            }
            this.currentFilePath = newPath;
            this.level.levelName = file.getName().replaceFirst("[.][^.]+$", "");
            setTitle("Tile Map Editor - " + file.getName());
            saveFile();
        }
    }

    private void setupColorMap() {
        colorMap.put(0, Color.WHITE);
        colorMap.put(1, new Color(30, 144, 255)); // Dodger Blue
        colorMap.put(2, new Color(220, 20, 60));  // Crimson Red
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

        for (Map.Entry<Integer, Color> entry : colorMap.entrySet()) {
            JPanel legendEntry = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            legendEntry.add(new ColorSwatch(entry.getValue()));
            legendEntry.add(new JLabel("= " + entry.getKey()));
            legendPanel.add(legendEntry);
        }
        return legendPanel;
    }

    private static class ColorSwatch extends JComponent {
        private final Color color;
        ColorSwatch(Color color) { this.color = color; }
        @Override public Dimension getPreferredSize() { return new Dimension(16, 16); }
        @Override protected void paintComponent(Graphics g) {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    private class GridPanel extends JPanel {
        private static final int TILE_WIDTH = 30, TILE_HEIGHT = 30;
        private final Level level;
        private final Map<Integer, Color> colorMap;
        private Point startPoint;
        private Rectangle selectionRect;

        public GridPanel(Level level, Map<Integer, Color> colorMap) {
            this.level = level;
            this.colorMap = colorMap;

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { handleMouse(e); }
                @Override public void mouseDragged(MouseEvent e) { handleMouse(e); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (currentTool == ToolMode.RECTANGLE_SELECT) {
                        repaint();
                    }
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void handleMouse(MouseEvent e) {
            int col = e.getX() / TILE_WIDTH;
            int row = e.getY() / TILE_HEIGHT;
            if (row < 0 || row >= level.tilemap.size() || col < 0 || col >= level.tilemap.get(0).size()) return;

            switch (currentTool) {
                case RECTANGLE_SELECT:
                    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                        startPoint = e.getPoint();
                        selectionRect = new Rectangle();
                    }
                    if (startPoint == null) return;
                    selectionRect.setBounds(Math.min(startPoint.x, e.getX()), Math.min(startPoint.y, e.getY()), Math.abs(startPoint.x - e.getX()), Math.abs(startPoint.y - e.getY()));
                    break;
                case BRUSH:
                    try { level.tilemap.get(row).set(col, Integer.parseInt(paintValueField.getText())); } catch (NumberFormatException ignored) {}
                    break;
                case ERASER:
                    level.tilemap.get(row).set(col, 0);
                    break;
            }
            repaint();
        }

        public void paintSelection() {
            if (selectionRect == null || currentTool != ToolMode.RECTANGLE_SELECT) return;
            try {
                int paintValue = Integer.parseInt(paintValueField.getText());
                int startCol = selectionRect.x / TILE_WIDTH;
                int startRow = selectionRect.y / TILE_HEIGHT;
                int endCol = (selectionRect.x + selectionRect.width) / TILE_WIDTH;
                int endRow = (selectionRect.y + selectionRect.height) / TILE_HEIGHT;

                for (int r = startRow; r <= endRow; r++) {
                    for (int c = startCol; c <= endCol; c++) {
                        if (r < level.tilemap.size() && c < level.tilemap.get(r).size()) {
                            level.tilemap.get(r).set(c, paintValue);
                        }
                    }
                }
                selectionRect = null;
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Paint Value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override public Dimension getPreferredSize() { return new Dimension(level.tilemap.get(0).size() * TILE_WIDTH, level.tilemap.size() * TILE_HEIGHT); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            Rectangle clip = g.getClipBounds();
            int startRow = clip.y / TILE_HEIGHT; int endRow = (clip.y + clip.height) / TILE_HEIGHT + 1;
            int startCol = clip.x / TILE_WIDTH; int endCol = (clip.x + clip.width) / TILE_WIDTH + 1;

            for (int row = startRow; row < endRow; row++) {
                if (row >= level.tilemap.size()) continue;
                for (int col = startCol; col < endCol; col++) {
                    if (col >= level.tilemap.get(row).size()) continue;
                    int value = level.tilemap.get(row).get(col);
                    Color bgColor = colorMap.getOrDefault(value, Color.GRAY);
                    int x = col * TILE_WIDTH; int y = row * TILE_HEIGHT;

                    g2d.setColor(bgColor);
                    g2d.fillRect(x, y, TILE_WIDTH, TILE_HEIGHT);
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawRect(x, y, TILE_WIDTH, TILE_HEIGHT);

                    double luminance = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
                    g2d.setColor(luminance > 0.5 ? Color.BLACK : Color.WHITE);
                    String text = String.valueOf(value);
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(text, x + (TILE_WIDTH - fm.stringWidth(text)) / 2, y + (TILE_HEIGHT - fm.getHeight()) / 2 + fm.getAscent());
                }
            }

            if (selectionRect != null && currentTool == ToolMode.RECTANGLE_SELECT) {
                g2d.setColor(new Color(100, 100, 100, 75));
                g2d.fill(selectionRect);
                g2d.setColor(new Color(100, 100, 100));
                g2d.draw(selectionRect);
            }
            g2d.dispose();
        }
    }
}
