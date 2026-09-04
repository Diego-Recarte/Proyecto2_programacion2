package Instagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

/** Iconos vectoriales de navegación que no dependen de emojis ni de fuentes. */
final class InstaNavIcon implements Icon {

    enum Type {
        HOME, SEARCH, ADD, MESSAGE, PROFILE
    }

    private final Type type;
    private final int size;

    InstaNavIcon(Type type, int size) {
        this.type = type;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color foreground = component != null ? component.getForeground() : Color.WHITE;
        g.setColor(foreground != null ? foreground : Color.WHITE);
        float strokeWidth = Math.max(1.7f, size / 12f);
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double scale = size / 24d;
        g.scale(scale, scale);
        switch (type) {
            case HOME -> paintHome(g);
            case SEARCH -> paintSearch(g);
            case ADD -> paintAdd(g);
            case MESSAGE -> paintMessage(g);
            case PROFILE -> paintProfile(g);
        }
        g.dispose();
    }

    private void paintHome(Graphics2D g) {
        Path2D roof = new Path2D.Double();
        roof.moveTo(3, 11);
        roof.lineTo(12, 3.5);
        roof.lineTo(21, 11);
        g.draw(roof);
        g.draw(new Rectangle2D.Double(5.5, 10, 13, 10.5));
        g.draw(new Rectangle2D.Double(10, 14.5, 4, 6));
    }

    private void paintSearch(Graphics2D g) {
        g.draw(new Ellipse2D.Double(3.5, 3.5, 12.5, 12.5));
        g.drawLine(15, 15, 21, 21);
    }

    private void paintAdd(Graphics2D g) {
        g.draw(new Ellipse2D.Double(2.5, 2.5, 19, 19));
        g.drawLine(12, 7, 12, 17);
        g.drawLine(7, 12, 17, 12);
    }

    private void paintMessage(Graphics2D g) {
        Path2D plane = new Path2D.Double();
        plane.moveTo(2.5, 4);
        plane.lineTo(21.5, 11.5);
        plane.lineTo(13.5, 14);
        plane.lineTo(10.5, 21);
        plane.closePath();
        g.draw(plane);
        g.draw(new Line2D.Double(10.5, 13.5, 21.5, 4));
    }

    private void paintProfile(Graphics2D g) {
        g.draw(new Ellipse2D.Double(8, 3, 8, 8));
        g.draw(new Arc2D.Double(4, 10, 16, 11, 12, 156, Arc2D.OPEN));
    }
}
