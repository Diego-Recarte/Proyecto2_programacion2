package Instagram;

import Logica.Excepciones.ImageLoadException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Carrusel de imágenes con navegación, contador y puntos de posición. */
final class InstaMediaCarousel extends JPanel {

    private final List<String> imagePaths;
    private final int imageWidth;
    private final int imageHeight;
    private final JLabel imageLabel = new JLabel();
    private final JLabel counter = new JLabel();
    private final JButton previous = arrowButton("<", "Imagen anterior");
    private final JButton next = arrowButton(">", "Imagen siguiente");
    private final DotsPanel dots = new DotsPanel();
    private int currentIndex;

    InstaMediaCarousel(List<String> imagePaths, int imageWidth, int imageHeight, Runnable imageAction) {
        this.imagePaths = imagePaths == null ? List.of() : new ArrayList<>(imagePaths);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.BLACK);
        setAlignmentX(CENTER_ALIGNMENT);
        setPreferredSize(new Dimension(imageWidth, imageHeight + 22));
        setMaximumSize(new Dimension(imageWidth, imageHeight + 22));

        JLayeredPane viewport = new JLayeredPane();
        viewport.setOpaque(true);
        viewport.setBackground(Color.BLACK);
        viewport.setPreferredSize(new Dimension(imageWidth, imageHeight));

        imageLabel.setBounds(0, 0, imageWidth, imageHeight);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setForeground(Color.GRAY);
        if (imageAction != null) {
            imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            imageLabel.setToolTipText("Abrir publicación");
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    imageAction.run();
                }
            });
        }
        viewport.add(imageLabel, JLayeredPane.DEFAULT_LAYER);

        int arrowSize = 34;
        previous.setBounds(8, (imageHeight - arrowSize) / 2, arrowSize, arrowSize);
        next.setBounds(imageWidth - arrowSize - 8, (imageHeight - arrowSize) / 2, arrowSize, arrowSize);
        previous.addActionListener(event -> showImage(currentIndex - 1));
        next.addActionListener(event -> showImage(currentIndex + 1));
        viewport.add(previous, JLayeredPane.PALETTE_LAYER);
        viewport.add(next, JLayeredPane.PALETTE_LAYER);

        counter.setOpaque(true);
        counter.setBackground(new Color(25, 25, 25, 210));
        counter.setForeground(Color.WHITE);
        counter.setFont(new Font("Segoe UI", Font.BOLD, 11));
        counter.setHorizontalAlignment(SwingConstants.CENTER);
        counter.setBounds(imageWidth - 58, 10, 48, 24);
        counter.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        viewport.add(counter, JLayeredPane.PALETTE_LAYER);

        add(viewport, BorderLayout.CENTER);
        dots.setPreferredSize(new Dimension(imageWidth, 22));
        add(dots, BorderLayout.SOUTH);
        showImage(0);
    }

    int getImageCount() {
        return imagePaths.size();
    }

    private void showImage(int requestedIndex) {
        if (imagePaths.isEmpty()) {
            imageLabel.setText("No hay imágenes seleccionadas");
            previous.setVisible(false);
            next.setVisible(false);
            counter.setVisible(false);
            return;
        }
        currentIndex = Math.max(0, Math.min(requestedIndex, imagePaths.size() - 1));
        imageLabel.setIcon(null);
        imageLabel.setText("");
        try {
            imageLabel.setIcon(fitImage(imagePaths.get(currentIndex), imageWidth, imageHeight));
        } catch (ImageLoadException ex) {
            imageLabel.setText("Imagen no disponible");
        }

        boolean multiple = imagePaths.size() > 1;
        previous.setVisible(multiple && currentIndex > 0);
        next.setVisible(multiple && currentIndex < imagePaths.size() - 1);
        counter.setVisible(multiple);
        counter.setText((currentIndex + 1) + "/" + imagePaths.size());
        dots.repaint();
    }

    private ImageIcon fitImage(String path, int maxWidth, int maxHeight) throws ImageLoadException {
        try {
            BufferedImage source = ImageIO.read(new File(path));
            if (source == null) {
                throw new IOException("Formato no reconocido");
            }
            double scale = Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight());
            int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
            Image scaled = source.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException ex) {
            throw new ImageLoadException("No se pudo cargar " + path, ex);
        }
    }

    private static JButton arrowButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Segoe UI", Font.BOLD, 22));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(35, 35, 35));
        button.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private final class DotsPanel extends JPanel {

        private DotsPanel() {
            setOpaque(true);
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (imagePaths.size() <= 1) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(1f));
            int visibleDots = Math.min(imagePaths.size(), 10);
            int spacing = 12;
            int startX = (getWidth() - ((visibleDots - 1) * spacing)) / 2;
            int startIndex = Math.max(0, Math.min(currentIndex - visibleDots / 2, imagePaths.size() - visibleDots));
            for (int dot = 0; dot < visibleDots; dot++) {
                int imageIndex = startIndex + dot;
                int diameter = imageIndex == currentIndex ? 7 : 5;
                int x = startX + dot * spacing - diameter / 2;
                int y = (getHeight() - diameter) / 2;
                g.setColor(imageIndex == currentIndex ? new Color(255, 90, 35) : new Color(105, 105, 105));
                g.fillOval(x, y, diameter, diameter);
            }
            g.dispose();
        }
    }
}
