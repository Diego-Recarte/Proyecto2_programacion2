package Instagram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class HashtagSearchUI extends JPanel {

    private final String currentUser;
    private final DefaultListModel<String> listModel;
    private final JList<String> resultList;
    private final java.util.List<String[]> postsHolder;
    private final DefaultListModel<String> suggestionModel;
    private final JList<String> suggestionList;
    private final JLabel suggestionTitle;
    private final JTextField searchField;
    private final Timer suggestionTimer;
    private int suggestionRequestId;
    private int searchRequestId;
    private boolean applyingSuggestion;

    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_BORDER = new Color(100, 100, 100);
    
    private final Color COLOR_BTN = new Color(255, 69, 0);
    private final Color COLOR_BTN_HOVER = new Color(200, 50, 0);
    private final Font FONT_CAOS = new Font("Segoe UI", Font.PLAIN, 12);

    public HashtagSearchUI(String currentUser) {
        this(currentUser, null);
    }

    public HashtagSearchUI(String currentUser, String initialHashtag) {
        this.currentUser = currentUser;
        this.postsHolder = new ArrayList<>();
        this.listModel = new DefaultListModel<>();
        this.resultList = new JList<>(listModel);
        this.suggestionModel = new DefaultListModel<>();
        this.suggestionList = new JList<>(suggestionModel);
        this.suggestionTitle = new JLabel("Hashtags sugeridos");
        this.searchField = new JTextField();
        this.suggestionTimer = new Timer(250, event -> cargarSugerencias());
        this.suggestionTimer.setRepeats(false);

        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(400, 650));

        add(crearZonaBusqueda(), BorderLayout.NORTH);

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

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int index = suggestionList.locationToIndex(event.getPoint());
                Rectangle bounds = index >= 0 ? suggestionList.getCellBounds(index, index) : null;
                if (bounds != null && bounds.contains(event.getPoint())) {
                    usarSugerencia(suggestionModel.get(index));
                }
            }
        });
        suggestionList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "buscarHashtag");
        suggestionList.getActionMap().put("buscarHashtag", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int index = suggestionList.getSelectedIndex();
                if (index >= 0) {
                    usarSugerencia(suggestionModel.get(index));
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                programarSugerencias();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                programarSugerencias();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                programarSugerencias();
            }
        });
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                programarSugerencias();
            }
        });
        if (initialHashtag == null || initialHashtag.isBlank()) {
            SwingUtilities.invokeLater(this::programarSugerencias);
        } else {
            SwingUtilities.invokeLater(() -> {
                applyingSuggestion = true;
                searchField.setText("#" + normalizarConsulta(initialHashtag));
                applyingSuggestion = false;
                ejecutarBusqueda(searchField.getText());
            });
        }
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

    private JPanel crearZonaBusqueda() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(COLOR_BG);
        area.add(crearHeader(), BorderLayout.NORTH);

        JPanel suggestions = new JPanel(new BorderLayout(8, 3));
        suggestions.setBackground(new Color(20, 20, 20));
        suggestions.setBorder(BorderFactory.createEmptyBorder(4, 12, 7, 12));
        suggestions.setPreferredSize(new Dimension(400, 64));

        suggestionTitle.setForeground(new Color(185, 185, 185));
        suggestionTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        suggestions.add(suggestionTitle, BorderLayout.NORTH);

        suggestionList.setBackground(new Color(20, 20, 20));
        suggestionList.setForeground(COLOR_TEXT);
        suggestionList.setSelectionBackground(new Color(75, 35, 20));
        suggestionList.setSelectionForeground(Color.WHITE);
        suggestionList.setFont(new Font("Segoe UI", Font.BOLD, 12));
        suggestionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        suggestionList.setVisibleRowCount(1);
        suggestionList.setFixedCellWidth(116);
        suggestionList.setFixedCellHeight(28);
        suggestionList.setCursor(new Cursor(Cursor.HAND_CURSOR));
        suggestionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                label.setBackground(selected ? new Color(110, 45, 20) : new Color(38, 38, 38));
                label.setForeground(selected ? Color.WHITE : new Color(255, 125, 75));
                return label;
            }
        });
        JScrollPane suggestionScroll = new JScrollPane(suggestionList);
        suggestionScroll.setBorder(null);
        suggestionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        suggestionScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        suggestions.add(suggestionScroll, BorderLayout.CENTER);
        area.add(suggestions, BorderLayout.CENTER);
        return area;
    }

    private JPanel crearHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(COLOR_BG);
        p.setPreferredSize(new Dimension(400, 76));

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
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(COLOR_TEXT);
        title.setBounds(50, 10, 300, 30);
        p.add(title);

        searchField.setBounds(15, 42, 260, 27);
        searchField.setBackground(new Color(30, 30, 30));
        searchField.setForeground(COLOR_TEXT);
        searchField.setCaretColor(COLOR_TEXT);
        searchField.setFont(FONT_CAOS);
        searchField.setToolTipText("Escribe un hashtag, por ejemplo #viajes");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        p.add(searchField);

        JButton btn = new BotonRojo("Buscar #");
        btn.setBounds(285, 42, 90, 27);
        btn.addActionListener(ae -> ejecutarBusqueda(searchField.getText()));
        p.add(btn);

        searchField.addActionListener(ae -> ejecutarBusqueda(searchField.getText()));

        return p;
    }

    private void ejecutarBusqueda(String raw) {
        int requestId = ++searchRequestId;
        listModel.clear();
        postsHolder.clear();
        String q = normalizarConsulta(raw);
        if (q.isBlank()) {
            listModel.addElement("Escribe o elige un hashtag sugerido.");
            return;
        }
        suggestionTimer.stop();
        suggestionRequestId++;
        suggestionTitle.setText("Resultados para #" + q);
        listModel.addElement("Buscando #" + q + "...");
        new SwingWorker<ArrayList<String[]>, Void>() {
            @Override
            protected ArrayList<String[]> doInBackground() throws Exception {
                instaManager manager = instaController.getInstance().getInsta();
                return manager != null ? manager.getPostsByHashtag(q) : new ArrayList<>();
            }

            @Override
            protected void done() {
                if (requestId != searchRequestId) {
                    return;
                }
                listModel.clear();
                postsHolder.clear();
                try {
                    ArrayList<String[]> results = get();
                    java.util.Set<String> seen = new java.util.HashSet<>();
                    for (String[] post : results) {
                        String key = value(post, 0) + "|" + value(post, 1) + "|" + value(post, 2);
                        if (seen.add(key)) {
                            postsHolder.add(post);
                            listModel.addElement("[" + value(post, 2) + "] @" + value(post, 1)
                                    + ": " + previewText(value(post, 3)));
                        }
                    }
                    if (listModel.isEmpty()) {
                        listModel.addElement("No se encontraron posts para #" + q);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    listModel.addElement("No se pudo completar la búsqueda.");
                    JOptionPane.showMessageDialog(HashtagSearchUI.this,
                            "Error buscando hashtags: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void programarSugerencias() {
        if (!applyingSuggestion) {
            suggestionTimer.restart();
        }
    }

    private void cargarSugerencias() {
        String prefix = normalizarConsulta(searchField.getText());
        int requestId = ++suggestionRequestId;
        suggestionTitle.setText(prefix.isBlank() ? "Hashtags populares" : "Sugerencias para #" + prefix);
        new SwingWorker<ArrayList<String>, Void>() {
            @Override
            protected ArrayList<String> doInBackground() throws Exception {
                instaManager manager = instaController.getInstance().getInsta();
                return manager != null ? manager.getHashtagSuggestions(prefix, 12) : new ArrayList<>();
            }

            @Override
            protected void done() {
                if (requestId != suggestionRequestId) {
                    return;
                }
                suggestionModel.clear();
                try {
                    for (String hashtag : get()) {
                        suggestionModel.addElement(hashtag);
                    }
                    if (suggestionModel.isEmpty()) {
                        suggestionTitle.setText(prefix.isBlank()
                                ? "Aún no hay hashtags en el sistema"
                                : "No hay hashtags que comiencen con #" + prefix);
                    }
                } catch (Exception ex) {
                    suggestionTitle.setText("No se pudieron cargar sugerencias");
                }
            }
        }.execute();
    }

    private void usarSugerencia(String hashtag) {
        if (hashtag == null || hashtag.isBlank()) {
            return;
        }
        applyingSuggestion = true;
        searchField.setText(hashtag);
        applyingSuggestion = false;
        suggestionTimer.stop();
        ejecutarBusqueda(hashtag);
    }

    private String normalizarConsulta(String raw) {
        if (raw == null) {
            return "";
        }
        String query = raw.trim().toLowerCase(java.util.Locale.ROOT);
        while (query.startsWith("#")) {
            query = query.substring(1);
        }
        return query.trim();
    }

    private static String value(String[] values, int index) {
        return values != null && index >= 0 && index < values.length && values[index] != null
                ? values[index] : "";
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
        frame.setContentPane(new InstaFeedUI(currentUser));
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
