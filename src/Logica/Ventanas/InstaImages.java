package Logica.Ventanas;

import Instagram.InstaPostMedia;
import Instagram.instaController;
import Instagram.instaManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Lee, descarga y escala imagenes fuera del hilo de Swing. */
public final class InstaImages {
    @FunctionalInterface private interface Source { BufferedImage read() throws Exception; }
    private InstaImages() { }

    public static ImageIcon icon(Component owner, String path, int width, int height, boolean crop) {
        instaManager manager = client(owner);
        return load(owner, () -> InstaPostMedia.readImage(path, manager), width, height, crop);
    }

    public static ImageIcon encoded(Component owner, String payload, int size) {
        return load(owner, () -> {
            int separator = payload.indexOf('\n');
            byte[] bytes = java.util.Base64.getDecoder().decode(payload.substring(separator + 1));
            return javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
        }, size, size, false);
    }

    public static ImageIcon avatar(Component owner, String username, int size) {
        instaManager manager = client(owner);
        return load(owner, () -> InstaPostMedia.readImage(manager.getProfilePic(username), manager), size, size, true);
    }

    private static instaManager client(Component owner) {
        for (Component current = owner; current != null; current = current.getParent()) {
            if (current instanceof JComponent component && component.getClientProperty("insta.manager") instanceof instaManager manager) return manager;
        }
        return instaController.getInstance().getInsta();
    }

    private static ImageIcon load(Component owner, Source source, int width, int height, boolean crop) {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D initial = placeholder.createGraphics();
        initial.setColor(new Color(35, 35, 35)); initial.fillRect(0, 0, width, height); initial.dispose();
        ImageIcon icon = new ImageIcon(placeholder);
        new SwingWorker<BufferedImage, Void>() {
            @Override protected BufferedImage doInBackground() throws Exception {
                BufferedImage original = source.read();
                double scale = crop ? Math.max((double) width / original.getWidth(), (double) height / original.getHeight())
                        : Math.min((double) width / original.getWidth(), (double) height / original.getHeight());
                int w = Math.max(1, (int) Math.round(original.getWidth() * scale));
                int h = Math.max(1, (int) Math.round(original.getHeight() * scale));
                BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = result.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(original, (width - w) / 2, (height - h) / 2, w, h, null); g.dispose();
                return result;
            }
            @Override protected void done() {
                try { icon.setImage(get()); }
                catch (Exception ex) { icon.setDescription("Imagen no disponible"); }
                owner.repaint();
            }
        }.execute();
        return icon;
    }
}
