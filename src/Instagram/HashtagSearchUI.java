package Instagram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

public class HashtagSearchUI extends JPanel {

    private final String currentUser;
    private final DefaultListModel<String> listModel;
    private final JList<String> resultList;
    private final java.util.List<String[]> postsHolder;

    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_BORDER = new Color(100, 100, 100);
    
    private final Color COLOR_BTN = new Color(255, 69, 0);
    private final Color COLOR_BTN_HOVER = new Color(200, 50, 0);
    private final Font FONT_CAOS = new Font("Comic Sans MS", Font.PLAIN, 12);

    public HashtagSearchUI(String currentUser) {
        this.currentUser = currentUser;
        this.postsHolder = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(400, 650));

        add(crearHeader(), BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setBackground(new Color(20, 20, 20));
        resultList.setForeground(COLOR_TEXT);
        resultList.setFont(FONT_CAOS);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        JScrollPane sp = new JScrollPane(resultList);
        sp.setBorder(null);

        add(sp, BorderLayout.CENTER);

        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = resultList.getSelectedIndex();
                    if (idx >= 0 && idx < postsHolder.size()) {
                        String[] selected = postsHolder.get(idx);
                        abrirPostEnContexto(selected);
                    }
                }
            }
        });
    }

    private void abrirPostEnContexto(String[] post) {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame)) {
            return;
        }
        JFrame frame = (JFrame) w;

        try {
            String imagRef = post.length > 0 ? post[0] : "";
            String autor = post.length > 1 ? post[1] : "";
            String fecha = post.length > 2 ? post[2] : "";
            String owner = post.length > 4 ? post[4] : autor;

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
                frame.setContentPane(HashtagSearchUI.this);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.revalidate();
                frame.repaint();
            };

            InstaPostUI postUI = new InstaPostUI(currentUser, ownerPosts, si, backAction);
            frame.setContentPane(postUI);
            frame.pack();
            frame.revalidate();
            frame.repaint();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error abriendo post: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(COLOR_BG);
        p.setPreferredSize(new Dimension(400, 70));

        JLabel lblBack = new JLabel("←");
        lblBack.setForeground(COLOR_BTN);
        lblBack.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBack.setBounds(10, 12, 30, 30);
        lblBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                volverAnterior();
            }
        });
        p.add(lblBack);

        JLabel title = new JLabel("Buscar Hashtag");
        title.setFont(new Font("Comic Sans MS", Font.BOLD | Font.ITALIC, 18));
        title.setForeground(COLOR_TEXT);
        title.setBounds(50, 10, 300, 30);
        p.add(title);

        JTextField txt = new JTextField();
        txt.setBounds(15, 40, 260, 25);
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(COLOR_TEXT);
        txt.setCaretColor(COLOR_TEXT); 
        txt.setFont(FONT_CAOS);
        txt.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        p.add(txt);

        JButton btn = new BotonRojo("Buscar #");
        btn.setBounds(285, 40, 90, 25);
        btn.addActionListener(ae -> ejecutarBusqueda(txt.getText()));
        p.add(btn);

        txt.addActionListener(ae -> ejecutarBusqueda(txt.getText()));

        return p;
    }

    private void ejecutarBusqueda(String raw) {
        listModel.clear();
        postsHolder.clear();
        if (raw == null) {
            return;
        }
        String q = raw.trim().toLowerCase();
        if (q.isEmpty()) {
            return;
        }
        if (q.startsWith("#")) {
            q = q.substring(1);
        }

        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager == null) {
                return;
            }

            ArrayList<String[]> exact = manager.getPostsByHashtag(q);
            ArrayList<String[]> results = new ArrayList<>();
            if (exact != null && !exact.isEmpty()) {
                results.addAll(exact);
            }

            java.util.Set<String> seen = new java.util.HashSet<>();
            for (String[] p : results) {
                String key = p[0] + "|" + p[1] + "|" + p[2];
                if (!seen.contains(key)) {
                    seen.add(key);
                    postsHolder.add(p);
                    String preview = "[" + p[2] + "] @" + p[1] + ": " + previewText(p[3]);
                    listModel.addElement(preview);
                }
            }

            if (listModel.isEmpty()) {
                listModel.addElement("No se encontraron posts para #" + q);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error buscando hashtags: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String previewText(String full) {
        if (full == null) {
            return "";
        }
        if (full.length() <= 60) {
            return full;
        }
        return full.substring(0, 60) + "...";
    }

    private void abrirPost(ArrayList<String[]> posts, int index) {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame)) {
            return;
        }
        JFrame frame = (JFrame) w;
        Runnable backAction = () -> {
            frame.setContentPane(HashtagSearchUI.this);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.revalidate();
            frame.repaint();
        };
        InstaPostUI postUI = new InstaPostUI(currentUser, posts, index, backAction);
        frame.setContentPane(postUI);
        frame.pack();
        frame.revalidate();
        frame.repaint();
    }

    private void volverAnterior() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame)) {
            return;
        }
        JFrame frame = (JFrame) w;
        frame.setContentPane(new InstaProfileUI(currentUser));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    private class BotonRojo extends JButton {

        public BotonRojo(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            
            setBackground(COLOR_BTN);
            setForeground(Color.WHITE);
            setFont(FONT_CAOS);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(COLOR_BTN_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(COLOR_BTN);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}