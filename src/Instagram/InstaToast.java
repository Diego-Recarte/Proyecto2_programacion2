package Instagram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.Timer;

/** Avisos flotantes estilo Windows, sin robar el foco al escribir. */
final class InstaToast implements AutoCloseable {
    private final JFrame owner;
    private final List<JWindow> windows = new ArrayList<>();
    private final java.util.Map<JWindow, Timer> timers = new java.util.HashMap<>();

    InstaToast(JFrame owner) { this.owner = owner; }

    void show(String title, String text, Runnable action) {
        if (windows.size() >= 3) dismiss(windows.get(0));
        // Sin propietario nativo: permanece visible aunque Insta+ esté minimizado.
        JWindow toast = new JWindow((java.awt.Window) null, owner.getGraphicsConfiguration());
        toast.setFocusableWindowState(false);
        toast.setAlwaysOnTop(true);
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBackground(new Color(32, 34, 38));
        body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(85, 88, 95)),
                BorderFactory.createEmptyBorder(12, 16, 12, 12)));
        JLabel brand = new JLabel("Insta+  ·  " + title);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brand.setForeground(Color.WHITE);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(brand, BorderLayout.CENTER);
        JButton close = new JButton("×");
        close.setForeground(Color.LIGHT_GRAY);
        close.setContentAreaFilled(false);
        close.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        close.setFocusPainted(false);
        close.setToolTipText("Cerrar aviso");
        close.addActionListener(e -> dismiss(toast));
        heading.add(close, BorderLayout.EAST);
        body.add(heading, BorderLayout.NORTH);
        JTextArea preview = new JTextArea(text.length() > 140 ? text.substring(0, 137) + "…" : text);
        preview.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        preview.setForeground(Color.LIGHT_GRAY);
        preview.setOpaque(false);
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        body.add(preview, BorderLayout.CENTER);
        JButton open = new JButton("Abrir en Insta+");
        open.setBackground(new Color(52, 55, 62));
        open.setForeground(Color.WHITE);
        open.setFocusPainted(false);
        open.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        open.addActionListener(e -> { dismiss(toast); action.run(); });
        body.add(open, BorderLayout.SOUTH);
        toast.setContentPane(body);
        toast.setSize(new Dimension(365, 165));
        windows.add(toast);
        position();
        toast.setVisible(true);
        Timer timer = new Timer(6500, e -> dismiss(toast));
        timer.setRepeats(false);
        timers.put(toast, timer);
        timer.start();
    }

    private void position() {
        Rectangle bounds = owner.getGraphicsConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(owner.getGraphicsConfiguration());
        int bottom = bounds.y + bounds.height - insets.bottom - 12;
        for (int index = windows.size() - 1; index >= 0; index--) {
            JWindow toast = windows.get(index);
            bottom -= toast.getHeight();
            toast.setLocation(bounds.x + bounds.width - insets.right - toast.getWidth() - 12, bottom);
            bottom -= 10;
        }
    }

    private void dismiss(JWindow toast) {
        Timer timer = timers.remove(toast);
        if (timer != null) timer.stop();
        windows.remove(toast);
        toast.dispose();
        position();
    }

    @Override public void close() {
        for (JWindow toast : new ArrayList<>(windows)) dismiss(toast);
    }
}
