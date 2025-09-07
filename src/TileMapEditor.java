
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TileMapEditor extends JFrame {
    private final Level level;
    private final String levelName;

    // Tool components
    private final JTextField paintValueField;

    public TileMapEditor(Level level, String levelName) {
        this.level = level;
        this.levelName = levelName;

        setTitle("Tile Map Editor - " + levelName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Create the new, high-performance GridPanel ---
        GridPanel gridPanel = new GridPanel(level);
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

    private void saveMap(java.awt.event.ActionEvent e) {
        // The level data is now the single source of truth, so we just save it.
        AssetHandler.saveLevel(level, levelName);
        JOptionPane.showMessageDialog(this, "Level saved successfully!");
    }

    /**
     * A high-performance panel that manually draws the tile grid.
     */
    private class GridPanel extends JPanel {
        private static final int TILE_WIDTH = 30;
        private static final int TILE_HEIGHT = 30;

        private final Level level;
        private Point startPoint;
        private Rectangle selectionRect;

        public GridPanel(Level level) {
            this.level = level;

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    selectionRect = new Rectangle();
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(startPoint.x - e.getX());
                    int height = Math.abs(startPoint.y - e.getY());
                    selectionRect.setBounds(x, y, width, height);
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // The selection rectangle is now stored and will be used for painting.
                    repaint();
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        public void paintSelection() {
            if (selectionRect == null) return;

            try {
                int paintValue = Integer.parseInt(paintValueField.getText());
                int startCol = selectionRect.x / TILE_WIDTH;
                int startRow = selectionRect.y / TILE_HEIGHT;
                int endCol = (selectionRect.x + selectionRect.width) / TILE_WIDTH;
                int endRow = (selectionRect.y + selectionRect.height) / TILE_HEIGHT;

                for (int row = startRow; row <= endRow; row++) {
                    for (int col = startCol; col <= endCol; col++) {
                        if (row < level.tilemap.size() && col < level.tilemap.get(row).size()) {
                            level.tilemap.get(row).set(col, paintValue);
                        }
                    }
                }
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Paint Value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int width = level.tilemap.get(0).size() * TILE_WIDTH;
            int height = level.tilemap.size() * TILE_HEIGHT;
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            // Get the visible part of the panel
            Rectangle clip = g.getClipBounds();

            // Calculate the range of tiles to draw
            int startRow = clip.y / TILE_HEIGHT;
            int endRow = (clip.y + clip.height) / TILE_HEIGHT + 1;
            int startCol = clip.x / TILE_WIDTH;
            int endCol = (clip.x + clip.width) / TILE_WIDTH + 1;

            // Draw only the visible tiles
            for (int row = startRow; row < endRow; row++) {
                if (row >= level.tilemap.size()) continue;
                for (int col = startCol; col < endCol; col++) {
                    if (col >= level.tilemap.get(row).size()) continue;

                    int x = col * TILE_WIDTH;
                    int y = row * TILE_HEIGHT;
                    String text = String.valueOf(level.tilemap.get(row).get(col));

                    // Draw tile background
                    g2d.setColor(UIManager.getColor("TextField.background"));
                    g2d.fillRect(x, y, TILE_WIDTH, TILE_HEIGHT);

                    // Draw tile border
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawRect(x, y, TILE_WIDTH, TILE_HEIGHT);

                    // Draw tile value
                    g2d.setColor(Color.BLACK);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textX = x + (TILE_WIDTH - textWidth) / 2;
                    int textY = y + (TILE_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
                    g2d.drawString(text, textX, textY);
                }
            }

            // Draw the selection rectangle on top
            if (selectionRect != null) {
                g2d.setColor(new Color(50, 100, 255, 50)); // Semi-transparent blue
                g2d.fill(selectionRect);
                g2d.setColor(new Color(50, 100, 255)); // Blue border
                g2d.draw(selectionRect);
            }

            g2d.dispose();
        }
    }
}
