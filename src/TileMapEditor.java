
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class TileMapEditor extends JFrame {
    private final Level level;
    private final String levelName;
    private final Map<Integer, Color> colorMap;

    // Tool components
    private final JTextField paintValueField;

    public TileMapEditor(Level level, String levelName) {
        this.level = level;
        this.levelName = levelName;
        this.colorMap = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
        setupColorMap();

        setTitle("Tile Map Editor - " + levelName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Create the new, high-performance GridPanel ---
        GridPanel gridPanel = new GridPanel(level, colorMap);
        JScrollPane scrollPane = new JScrollPane(gridPanel);

        // --- Tool Panel (Left) ---
        paintValueField = new JTextField("1", 5);
        paintValueField.setMaximumSize(new Dimension(100, 30));

        JButton paintButton = new JButton("Paint Selection");
        paintButton.addActionListener(e -> gridPanel.paintSelection());

        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
        toolPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        toolPanel.add(new JLabel("Paint Value:"));
        toolPanel.add(paintValueField);
        toolPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        toolPanel.add(paintButton);
        toolPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- Color Legend Panel ---
        JPanel legendPanel = createLegendPanel();
        toolPanel.add(legendPanel);

        toolPanel.add(Box.createVerticalGlue());

        // --- Bottom Panel (Save Button) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this::saveMap);
        bottomPanel.add(saveButton);

        // --- Add panels to the frame ---
        getContentPane().add(toolPanel, BorderLayout.WEST);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void setupColorMap() {
        colorMap.put(0, Color.WHITE);
        colorMap.put(1, new Color(30, 144, 255)); // Dodger Blue
        colorMap.put(2, new Color(220, 20, 60));  // Crimson Red
        // Add more colors here
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

        for (Map.Entry<Integer, Color> entry : colorMap.entrySet()) {
            JPanel legendEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
            legendEntry.add(new ColorSwatch(entry.getValue()));
            legendEntry.add(new JLabel("= " + entry.getKey()));
            legendPanel.add(legendEntry);
        }
        return legendPanel;
    }

    private void saveMap(java.awt.event.ActionEvent e) {
        AssetHandler.saveLevel(level, levelName);
        JOptionPane.showMessageDialog(this, "Level saved successfully!");
    }

    /** A small colored square for the legend. */
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
        private static final int TILE_WIDTH = 30;
        private static final int TILE_HEIGHT = 30;

        private final Level level;
        private final Map<Integer, Color> colorMap;
        private Point startPoint;
        private Rectangle selectionRect;

        public GridPanel(Level level, Map<Integer, Color> colorMap) {
            this.level = level;
            this.colorMap = colorMap;

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { startPoint = e.getPoint(); selectionRect = new Rectangle(); repaint(); }
                @Override public void mouseDragged(MouseEvent e) { selectionRect.setBounds(Math.min(startPoint.x, e.getX()), Math.min(startPoint.y, e.getY()), Math.abs(startPoint.x - e.getX()), Math.abs(startPoint.y - e.getY())); repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        public void paintSelection() {
            if (selectionRect == null) return;
            try {
                int paintValue = Integer.parseInt(paintValueField.getText());
                int startCol = selectionRect.x / TILE_WIDTH; int startRow = selectionRect.y / TILE_HEIGHT;
                int endCol = (selectionRect.x + selectionRect.width) / TILE_WIDTH; int endRow = (selectionRect.y + selectionRect.height) / TILE_HEIGHT;
                for (int r = startRow; r <= endRow; r++) {
                    for (int c = startCol; c <= endCol; c++) {
                        if (r < level.tilemap.size() && c < level.tilemap.get(r).size()) level.tilemap.get(r).set(c, paintValue);
                    }
                }
                repaint();
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid Paint Value.", "Error", JOptionPane.ERROR_MESSAGE); }
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

            if (selectionRect != null) {
                g2d.setColor(new Color(100, 100, 100, 75));
                g2d.fill(selectionRect);
                g2d.setColor(new Color(100, 100, 100));
                g2d.draw(selectionRect);
            }
            g2d.dispose();
        }
    }
}
