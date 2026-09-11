package Instagram;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JButton;

/** Contador compartido pintado sobre el icono, sin temporizadores por pestaña. */
final class InstaMessageBadge implements Icon {
    private final Icon base = new InstaNavIcon(InstaNavIcon.Type.MESSAGE, 24);

    static void install(JButton button) {
        button.setText("");
        button.setIcon(new InstaMessageBadge());
    }

    @Override public int getIconWidth() { return 40; }
    @Override public int getIconHeight() { return 32; }

    @Override public void paintIcon(Component component, Graphics g, int x, int y) {
        base.paintIcon(component, g, x + 2, y + 7);
        InstaSession session = InstaSession.find(component);
        int count = session == null ? 0 : session.unreadCount();
        if (component instanceof JButton button) {
            String description = count > 0 ? "Mensajes: " + count + " sin leer" : "Mensajes";
            button.setToolTipText(description);
            button.getAccessibleContext().setAccessibleName(description);
        }
        if (count == 0) return;
        Graphics copy = g.create();
        copy.setColor(new Color(235, 55, 70));
        String label = count > 99 ? "99+" : Integer.toString(count);
        copy.setFont(new Font("Segoe UI", Font.BOLD, 10));
        int width = Math.max(17, copy.getFontMetrics().stringWidth(label) + 7);
        copy.fillRoundRect(x + 18, y, width, 17, 17, 17);
        copy.setColor(Color.WHITE);
        copy.drawString(label, x + 18 + (width - copy.getFontMetrics().stringWidth(label)) / 2, y + 12);
        copy.dispose();
    }
}
