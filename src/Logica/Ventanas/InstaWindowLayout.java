package Logica.Ventanas;

import java.awt.*;
import java.awt.event.*;
import java.util.IdentityHashMap;
import javax.swing.*;

/** Conserva el tamaño elegido y centra formularios al ampliar la vista. */
public final class InstaWindowLayout {
    private InstaWindowLayout() { }
    public static void install(JComponent content) {
        centerFixedForms(content);
        content.addHierarchyListener(event -> {
            if ((event.getChangeFlags() & java.awt.event.HierarchyEvent.PARENT_CHANGED) == 0) return;
            if (!(SwingUtilities.getWindowAncestor(content) instanceof JFrame frame)) return;
            frame.setResizable(true);
            frame.setMinimumSize(new Dimension(416, 620));
            if (frame.getRootPane().getClientProperty("insta.resize") == null) {
                frame.getRootPane().putClientProperty("insta.resize", true);
                frame.addComponentListener(new ComponentAdapter() {
                    @Override public void componentResized(ComponentEvent e) {
                        Dimension size = frame.getContentPane().getSize();
                        frame.getRootPane().putClientProperty("insta.size", size);
                        if (frame.getContentPane() instanceof JComponent view) view.putClientProperty("MODO_MOBILE", size.width < 650);
                    }
                });
            }
            Object saved = frame.getRootPane().getClientProperty("insta.size");
            if (saved instanceof Dimension size && size.width >= 400) content.setPreferredSize(size);
        });
    }

    public static void toggle(Component component) {
        if (SwingUtilities.getWindowAncestor(component) instanceof JFrame frame) {
            Insets insets = frame.getInsets();
            int width = frame.getContentPane().getWidth() < 650 ? 900 : 400;
            frame.setSize(width + insets.left + insets.right, Math.max(650 + insets.top + insets.bottom, frame.getHeight()));
        }
    }

    private static void centerFixedForms(Container container) {
        if (container.getLayout() == null && !(container instanceof JLayeredPane)) {
            IdentityHashMap<Component, Rectangle> bounds = new IdentityHashMap<>();
            for (Component child : container.getComponents()) bounds.put(child, child.getBounds());
            container.addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent event) {
                    int offset = Math.max(0, (container.getWidth() - 400) / 2);
                    bounds.forEach((child, rectangle) -> child.setBounds(rectangle.x + offset, rectangle.y, rectangle.width, rectangle.height));
                }
            });
        }
        for (Component child : container.getComponents()) if (child instanceof Container nested) centerFixedForms(nested);
    }
}
