
package Instagram;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;

public class InteractionsUI extends JPanel {

    private final String currentUser;
    private final DefaultListModel<String> textModel;
    private final DefaultListModel<String[]> postsModel;
    private final JList<String> list;

    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_ACC = new Color(255, 69, 0);

    public InteractionsUI(String currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(400, 650));

        JPanel header = new JPanel(null);
        header.setBackground(COLOR_BG);
        header.setPreferredSize(new Dimension(400, 60));

        JLabel lblBack = new JLabel("←  Interacciones");
        lblBack.setForeground(COLOR_ACC);
        lblBack.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBack.setBounds(10, 12, 300, 30);
        lblBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                volverAnterior();
            }
        });
        header.add(lblBack);

        add(header, BorderLayout.NORTH);

        textModel = new DefaultListModel<>();
        postsModel = new DefaultListModel<>();
        list = new JList<>(textModel);
        list.setBackground(new Color(20, 20, 20));
        list.setForeground(COLOR_TEXT);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(46);
        list.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int sel = list.getSelectedIndex();
                    if (sel >= 0 && sel < postsModel.size()) {
                        String[] post = postsModel.get(sel);
                        abrirPost(post);
                    }
                }
            }
        });

        cargarMenciones();
    }

    private void cargarMenciones() {
        textModel.clear();
        postsModel.clear();

        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager == null) {
                textModel.addElement("Error: manager no disponible");
                return;
            }

            ArrayList<String[]> posts = manager.findPostsMentioning(currentUser);

            if (posts == null || posts.isEmpty()) {
                textModel.addElement("No te han mencionado todavía.");
                return;
            }

            for (String[] p : posts) {
                String owner = p.length > 4 ? p[4] : "(?)";
                String autor = p.length > 1 ? p[1] : "(?)";
                String fecha = p.length > 2 ? p[2] : "(?)";
                String contenido = p.length > 3 ? p[3] : "";
                String preview = contenido.length() > 60 ? contenido.substring(0, 60) + "..." : contenido;

                String line = "@" + owner + " · " + autor + " · " + fecha + " · " + preview;
                textModel.addElement(line);
                postsModel.addElement(p);
            }

        } catch (IOException ex) {
            textModel.clear();
            textModel.addElement("Error cargando interacciones: " + ex.getMessage());
        }
    }

    private void abrirPost(String[] postData) {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame)) {
            return;
        }
        JFrame f = (JFrame) w;

        try {
            String imagRef = postData.length > 0 ? postData[0] : "";
            String autor = postData.length > 1 ? postData[1] : "";
            String fecha = postData.length > 2 ? postData[2] : "";
            String owner = postData.length > 4 ? postData[4] : autor;

            instaManager manager = instaController.getInstance().getInsta();
            ArrayList<String[]> ownerPosts = new ArrayList<>();
            if (manager != null) {
                ownerPosts = manager.getPosts(owner);
            }

            int startIndex = 0;
            for (int i = 0; i < ownerPosts.size(); i++) {
                String[] p = ownerPosts.get(i);
                String pImg = p.length > 0 ? p[0] : "";
                String pAutor = p.length > 1 ? p[1] : "";
                String pFecha = p.length > 2 ? p[2] : "";
                if ((imagRef != null && !imagRef.isEmpty() && imagRef.equals(pImg))
                        || (autor != null && autor.equals(pAutor) && fecha != null && fecha.equals(pFecha))) {
                    startIndex = i;
                    break;
                }
            }

            final int si = startIndex;
            Runnable backAction = () -> {
                f.setContentPane(this);
                f.pack();
                f.setLocationRelativeTo(null);
                f.revalidate();
                f.repaint();
            };

            InstaPostUI postUI = new InstaPostUI(currentUser, ownerPosts, si, backAction);
            f.setContentPane(postUI);
            f.pack();
            f.revalidate();
            f.repaint();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error abriendo post: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volverAnterior() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame)) {
            return;
        }
        JFrame f = (JFrame) w;
        f.setContentPane(new InstaProfileUI(currentUser));
        f.pack();
        f.setLocationRelativeTo(null);
        f.revalidate();
        f.repaint();
    }
}
