package Instagram;

import Logica.Excepciones.ImageLoadException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class InstaPostUI extends JLayeredPane {

    private final String currentUser;
    private final ArrayList<String[]> feedPosts;
    private final int startIndex;
    private final Runnable backAction;

    private String currentPostAuthor;
    private String currentImagePath;
    private JLabel labelContadorSeleccionado;

    private JPanel topBarPanel;
    private JPanel mainContentPanel;
    private JScrollPane mainScrollPane;
    private JPanel commentsDrawer;
    private boolean commentsVisible = false;

    private DefaultListModel<ComentarioData> listModelComentarios;
    private JList<ComentarioData> listaComentarios;
    private JTextField txtComentario;
    private JLabel lblTituloComentarios;

    private final int TOP_BAR_HEIGHT = 50;
    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_DRAWER_BG = new Color(30, 30, 30);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);

    public InstaPostUI(String currentUser, ArrayList<String[]> posts, int startIndex) {
        this(currentUser, posts, startIndex, null);
    }

    public InstaPostUI(String currentUser, ArrayList<String[]> posts, int startIndex, Runnable backAction) {
        this.currentUser = currentUser;
        this.feedPosts = posts != null ? posts : new ArrayList<>();
        this.startIndex = Math.max(0, startIndex);
        this.backAction = backAction;
        initOnce();
    }

    public InstaPostUI(String currentUser, String authorToShowAll) {
        this(currentUser, authorToShowAll, null);
    }

    public InstaPostUI(String currentUser, String authorToShowAll, Runnable backAction) {
        this.currentUser = currentUser;
        ArrayList<String[]> loaded = new ArrayList<>();
        Runnable ba = backAction;
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager != null && authorToShowAll != null) {
                loaded = manager.getPosts(authorToShowAll);
            }
        } catch (IOException e) {
        }
        this.feedPosts = loaded != null ? loaded : new ArrayList<>();
        this.startIndex = 0;
        this.backAction = ba;
        initOnce();
    }

    private void initOnce() {
        setLayout(null);
        setBackground(COLOR_BG);
        setOpaque(true);
        setPreferredSize(new Dimension(400, 650));

        crearTopBar();
        crearInterfazFeed();
        crearInterfazComentarios();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                actualizarDimensiones();
            }
        });

        SwingUtilities.invokeLater(() -> {
            actualizarDimensiones();
            adjustMainContentWidth();
            scrollToIndex(this.startIndex);
        });
    }

    private void actualizarDimensiones() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        if (topBarPanel != null) {
            topBarPanel.setBounds(0, 0, w, TOP_BAR_HEIGHT);
        }

        if (mainScrollPane != null) {
            mainScrollPane.setBounds(0, TOP_BAR_HEIGHT, w, h - TOP_BAR_HEIGHT);
            mainScrollPane.revalidate();
        }

        if (commentsDrawer != null) {
            int drawerH = (int) (h * 0.7);
            if (commentsVisible) {
                commentsDrawer.setBounds(0, h - drawerH, w, drawerH);
            } else {
                commentsDrawer.setBounds(0, h, w, drawerH);
            }
        }
        adjustMainContentWidth();
        revalidate();
        repaint();
    }

    private void adjustMainContentWidth() {
        if (mainScrollPane == null || mainContentPanel == null) {
            return;
        }
        JViewport vp = mainScrollPane.getViewport();
        if (vp == null) {
            return;
        }
        int viewportWidth = vp.getWidth();
        if (viewportWidth <= 0) {
            viewportWidth = mainScrollPane.getWidth();
        }
        if (viewportWidth > 0) {
            mainContentPanel.setPreferredSize(new Dimension(viewportWidth, mainContentPanel.getPreferredSize().height));
            mainContentPanel.revalidate();
        }
    }

    private void scrollToIndex(int index) {
        if (index >= 0 && index < feedPosts.size()) {
            int componentIndex = index * 2;
            if (componentIndex < mainContentPanel.getComponentCount()) {
                Component c = mainContentPanel.getComponent(componentIndex);
                mainScrollPane.getViewport().setViewPosition(new Point(0, c.getY()));
            }
        }
    }

    private void crearTopBar() {
        topBarPanel = new JPanel(new BorderLayout());
        topBarPanel.setBackground(COLOR_BG);
        topBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        JLabel btnBack = new JLabel("  < Volver");
        btnBack.setForeground(new Color(255, 69, 0));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                regresarAlPerfil();
            }
        });

        JLabel lblTitle = new JLabel("Publicaciones");
        lblTitle.setForeground(COLOR_TEXT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        topBarPanel.add(btnBack, BorderLayout.WEST);
        topBarPanel.add(lblTitle, BorderLayout.CENTER);
        topBarPanel.add(Box.createHorizontalStrut(80), BorderLayout.EAST);

        add(topBarPanel, JLayeredPane.PALETTE_LAYER);
    }

    private void crearInterfazFeed() {
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(COLOR_BG);
        mainContentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (int i = 0; i < feedPosts.size(); i++) {
            String[] post = feedPosts.get(i);
            JPanel tarjeta = crearTarjetaPost(post);

            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setBackground(COLOR_BG);
            wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 0, 0);

            tarjeta.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));

            wrapper.add(tarjeta, gbc);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
            mainContentPanel.add(wrapper);

            JPanel separator = new JPanel();
            separator.setBackground(new Color(20, 20, 20));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            separator.setPreferredSize(new Dimension(400, 1));
            separator.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainContentPanel.add(separator);
        }

        mainContentPanel.add(Box.createVerticalStrut(100));

        mainScrollPane = new JScrollPane(mainContentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainScrollPane.getViewport().addChangeListener(e -> adjustMainContentWidth());

        add(mainScrollPane, JLayeredPane.DEFAULT_LAYER);
    }

    private JPanel crearTarjetaPost(String[] post) {
        String author = "Desconocido";
        String imgPath = "";
        String caption = "";

        if (post != null) {
            if (post.length > 3 && post[3] != null) {
                caption = post[3];
            }
            if (post.length > 1) {
                boolean firstIsPath = looksLikePath(post[0]);
                boolean secondIsPath = looksLikePath(post[1]);
                if (firstIsPath && !secondIsPath) {
                    imgPath = post[0];
                    author = post[1];
                } else if (secondIsPath && !firstIsPath) {
                    imgPath = post[1];
                    author = post[0];
                } else {
                    if (!firstIsPath && secondIsPath) {
                        author = post[0];
                        imgPath = post[1];
                    } else {
                        author = post[0];
                        imgPath = post.length > 1 ? post[1] : "";
                    }
                }
            } else if (post.length == 1) {
                author = post[0];
            }
        }

        File check = new File(imgPath);
        if (!check.exists() && post != null && post.length > 1) {
            if (looksLikePath(post[1]) && !looksLikePath(post[0])) {
                imgPath = post[1];
                author = post[0];
            } else if (looksLikePath(post[0]) && !looksLikePath(post[1])) {
                imgPath = post[0];
                author = post.length > 1 ? post[1] : author;
            }
        }

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_BG);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));

        card.add(crearHeaderPostIndividual(author));
        card.add(crearImagenPost(imgPath));

        int commentCount = 0;
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager != null) {
                ArrayList<String[]> comments = manager.getComments(author, imgPath);
                if ((comments == null || comments.isEmpty()) && looksLikePath(author)) {
                    comments = manager.getComments(imgPath, author);
                }
                if (comments != null) {
                    commentCount = comments.size();
                }
            }
        } catch (Exception e) {
        }

        JPanel accionesPanel = crearAccionesPost(author, imgPath, commentCount);
        card.add(accionesPanel);

        if (caption != null && !caption.isEmpty()) {
            JPanel pCaption = new JPanel(new BorderLayout());
            pCaption.setBackground(COLOR_BG);
            pCaption.setBorder(new EmptyBorder(0, 15, 20, 15));
            pCaption.setAlignmentX(Component.CENTER_ALIGNMENT);
            pCaption.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));

            JEditorPane captionText = InstaSocialText.createCaption(author, caption, 350,
                    this::abrirHashtag, this::abrirMencion);
            pCaption.add(captionText, BorderLayout.CENTER);
            card.add(pCaption);
        } else {
            card.add(Box.createVerticalStrut(10));
        }

        card.revalidate();
        return card;
    }

    private boolean looksLikePath(String s) {
        if (s == null) {
            return false;
        }
        String low = s.toLowerCase();
        return low.contains("/") || low.contains("\\") || low.contains(":") || low.endsWith(".jpg") || low.endsWith(".jpeg") || low.endsWith(".png");
    }

    private JPanel crearHeaderPostIndividual(String author) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        header.setPreferredSize(new Dimension(380, 50));
        header.setBorder(new EmptyBorder(0, 10, 0, 10));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pUser.setBackground(COLOR_BG);

        JLabel lblAvatar = new JLabel();
        lblAvatar.setIcon(generarAvatar(author, 30));

        JLabel lblName = new JLabel(author);
        lblName.setForeground(COLOR_TEXT);
        lblName.setFont(FONT_BOLD);

        pUser.add(lblAvatar);
        pUser.add(lblName);

        header.add(pUser, BorderLayout.WEST);

        JLabel lblMenu = new JLabel("•••");
        lblMenu.setForeground(Color.GRAY);
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(lblMenu, BorderLayout.EAST);

        return header;
    }

    private JPanel crearImagenPost(String path) {
        List<String> mediaPaths = InstaPostMedia.decode(path);
        if (mediaPaths.size() > 1) {
            JPanel carouselContainer = new JPanel(new GridBagLayout());
            carouselContainer.setBackground(COLOR_BG);
            carouselContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
            InstaMediaCarousel carousel = new InstaMediaCarousel(mediaPaths, 360, 400, null);
            carouselContainer.add(carousel);
            carouselContainer.setPreferredSize(new Dimension(380, 422));
            carouselContainer.setMaximumSize(new Dimension(380, 422));
            return carouselContainer;
        }

        String singlePath = mediaPaths.isEmpty() ? "" : mediaPaths.get(0);
        JPanel imgContainer = new JPanel(new GridBagLayout());
        imgContainer.setBackground(COLOR_BG);
        imgContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        imgContainer.setMaximumSize(new Dimension(380, 470));

        JLabel lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);
        lblImagen.setOpaque(false);

        ImageIcon icon = null;
        try {
            icon = ajustarImagenAlFeed(singlePath, 360, 450);
        } catch (ImageLoadException ex) {
            System.err.println("Error cargando imagen: " + ex.getMessage());
        }

        if (icon != null) {
            lblImagen.setIcon(icon);
            lblImagen.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        } else {
            lblImagen.setText("Imagen no encontrada");
            lblImagen.setForeground(Color.GRAY);
            lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
            lblImagen.setPreferredSize(new Dimension(360, 240));
            lblImagen.setBorder(new LineBorder(Color.DARK_GRAY));
        }

        int containerHeight = icon != null ? icon.getIconHeight() + 16 : 256;
        imgContainer.setPreferredSize(new Dimension(380, containerHeight));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        imgContainer.add(lblImagen, gbc);

        return imgContainer;
    }

    private JPanel crearAccionesPost(String author, String path, int commentCount) {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        acciones.setBackground(COLOR_BG);
        acciones.setBorder(new EmptyBorder(18, 8, 8, 8));
        acciones.setAlignmentX(Component.CENTER_ALIGNMENT);
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        boolean initiallyLiked = false;
        int initialLikeCount = 0;
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager != null) {
                initiallyLiked = manager.hasLiked(author, path, currentUser);
                initialLikeCount = manager.getLikeCount(author, path);
            }
        } catch (IOException ignored) {
        }

        JPanel likeGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        likeGroup.setOpaque(false);
        JLabel lblLike = new JLabel(initiallyLiked ? "♥" : "♡");
        lblLike.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 29));
        lblLike.setForeground(initiallyLiked ? new Color(255, 48, 64) : COLOR_TEXT);
        lblLike.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel lblLikeCount = new JLabel(String.valueOf(initialLikeCount));
        lblLikeCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLikeCount.setForeground(COLOR_TEXT);

        lblLike.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    instaManager manager = instaController.getInstance().getInsta();
                    int total = manager.toggleLike(author, path, currentUser);
                    boolean liked = manager.hasLiked(author, path, currentUser);
                    lblLike.setText(liked ? "♥" : "♡");
                    lblLike.setForeground(liked ? new Color(255, 48, 64) : COLOR_TEXT);
                    lblLikeCount.setText(String.valueOf(total));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(InstaPostUI.this,
                            "No se pudo guardar el corazón.", "Publicación", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        likeGroup.add(lblLike);
        likeGroup.add(lblLikeCount);

        JPanel commentGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        commentGroup.setOpaque(false);

        JLabel lblComment = new JLabel("💬");
        lblComment.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblComment.setForeground(COLOR_TEXT);
        lblComment.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblComment.setBorder(new EmptyBorder(2, 0, 0, 0));

        JLabel lblCount = new JLabel(String.valueOf(commentCount));
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCount.setForeground(Color.LIGHT_GRAY);
        lblCount.setVerticalAlignment(SwingConstants.CENTER);
        lblCount.setBorder(new EmptyBorder(0, 2, 0, 8));
        lblCount.setPreferredSize(new Dimension(Math.max(24, (int) lblCount.getPreferredSize().getWidth()), (int) lblCount.getPreferredSize().getHeight()));

        lblComment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirComentariosDe(author, path, lblCount);
            }
        });

        commentGroup.add(lblComment);
        commentGroup.add(lblCount);

        acciones.add(likeGroup);
        acciones.add(commentGroup);

        if (currentUser.equals(author)) {
            JLabel delete = new JLabel("🗑");
            delete.setToolTipText("Eliminar publicación");
            delete.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            delete.setForeground(Color.LIGHT_GRAY);
            delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            delete.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    eliminarPublicacion(author, path);
                }
            });
            acciones.add(delete);
        }

        return acciones;
    }

    private void eliminarPublicacion(String author, String path) {
        int answer = JOptionPane.showConfirmDialog(this,
                "¿Eliminar esta publicación definitivamente?",
                "Eliminar publicación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager != null && manager.deletePost(author, path)) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JFrame frame) {
                    frame.setContentPane(new InstaProfileUI(currentUser));
                    frame.pack();
                    frame.revalidate();
                    frame.repaint();
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar: " + ex.getMessage(),
                    "Eliminar publicación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearInterfazComentarios() {
        commentsDrawer = new JPanel(new BorderLayout());
        commentsDrawer.setBackground(COLOR_DRAWER_BG);
        commentsDrawer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(100, 100, 100)));

        JPanel headerDrawer = new JPanel(new BorderLayout());
        headerDrawer.setBackground(COLOR_DRAWER_BG);
        headerDrawer.setBorder(new EmptyBorder(10, 0, 10, 0));

        lblTituloComentarios = new JLabel("Comentarios");
        lblTituloComentarios.setForeground(COLOR_TEXT);
        lblTituloComentarios.setFont(FONT_BOLD);
        lblTituloComentarios.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnCerrar = new JButton("X");
        btnCerrar.setForeground(Color.GRAY);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> mostrarComentarios(false));

        headerDrawer.add(lblTituloComentarios, BorderLayout.CENTER);
        headerDrawer.add(btnCerrar, BorderLayout.EAST);

        listModelComentarios = new DefaultListModel<>();
        listaComentarios = new JList<>(listModelComentarios);
        listaComentarios.setBackground(COLOR_DRAWER_BG);
        listaComentarios.setCellRenderer(new ComentarioRenderer());
        listaComentarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollComments = new JScrollPane(listaComentarios);
        scrollComments.setBorder(null);
        scrollComments.getViewport().setBackground(COLOR_DRAWER_BG);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(40, 40, 40));
        inputPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel lblMyPic = new JLabel();
        lblMyPic.setIcon(generarAvatar(currentUser, 30));

        txtComentario = new JTextField();
        txtComentario.setBackground(new Color(40, 40, 40));
        txtComentario.setForeground(Color.WHITE);
        txtComentario.setCaretColor(Color.WHITE);
        txtComentario.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton btnEnviar = new JButton("Publicar");
        btnEnviar.setForeground(new Color(0, 149, 246));
        btnEnviar.setBorderPainted(false);
        btnEnviar.setContentAreaFilled(false);
        btnEnviar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEnviar.addActionListener(e -> publicarComentario());

        inputPanel.add(lblMyPic, BorderLayout.WEST);
        inputPanel.add(txtComentario, BorderLayout.CENTER);
        inputPanel.add(btnEnviar, BorderLayout.EAST);

        commentsDrawer.add(headerDrawer, BorderLayout.NORTH);
        commentsDrawer.add(scrollComments, BorderLayout.CENTER);
        commentsDrawer.add(inputPanel, BorderLayout.SOUTH);

        add(commentsDrawer, Integer.valueOf(JLayeredPane.PALETTE_LAYER + 10));
    }

    private void abrirComentariosDe(String author, String path, JLabel labelContador) {
        this.currentPostAuthor = author;
        this.currentImagePath = path;
        this.labelContadorSeleccionado = labelContador;

        listModelComentarios.clear();

        try {
            instaManager manager = instaController.getInstance().getInsta();
            ArrayList<String[]> comments = null;
            if (manager != null) {
                comments = manager.getComments(author, path);
                if ((comments == null || comments.isEmpty()) && looksLikePath(author)) {
                    comments = manager.getComments(path, author);
                }
            }

            if (comments != null && !comments.isEmpty()) {
                for (String[] com : comments) {
                    String user = com.length > 0 ? com[0] : "usuario";
                    String text = com.length > 1 ? com[1] : "(sin texto)";
                    String date = com.length > 2 ? com[2] : "";
                    listModelComentarios.addElement(new ComentarioData(user, text, date));
                }
                if (labelContador != null) {
                    labelContador.setText(String.valueOf(comments.size()));
                }
            } else {
                if (labelContador != null) {
                    labelContador.setText("0");
                }
            }
        } catch (Exception e) {
        }
        mostrarComentarios(true);
    }

    private void mostrarComentarios(boolean mostrar) {
        commentsVisible = mostrar;
        actualizarDimensiones();
    }

    private void publicarComentario() {
        String texto = txtComentario.getText().trim();
        if (!texto.isEmpty() && currentPostAuthor != null) {
            try {
                instaManager manager = instaController.getInstance().getInsta();
                if (manager != null) {
                    manager.addComment(currentPostAuthor, currentImagePath, currentUser, texto);
                }

                listModelComentarios.addElement(new ComentarioData(currentUser, texto, // fecha actual:
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date())));
                txtComentario.setText("");
                listaComentarios.ensureIndexIsVisible(listModelComentarios.getSize() - 1);

                if (labelContadorSeleccionado != null) {
                    try {
                        String shown = labelContadorSeleccionado.getText().replaceAll("[^0-9]", "");
                        int num = shown.isEmpty() ? 0 : Integer.parseInt(shown);
                        labelContadorSeleccionado.setText(String.valueOf(num + 1)); // plain number
                    } catch (Exception e) {
                        labelContadorSeleccionado.setText("1");
                    }
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private static class ComentarioData {

        String usuario, texto, fecha;

        public ComentarioData(String u, String t, String f) {
            usuario = u;
            texto = t;
            fecha = f;
        }
    }

    private class ComentarioRenderer implements ListCellRenderer<ComentarioData> {

        @Override
        public Component getListCellRendererComponent(JList<? extends ComentarioData> list, ComentarioData value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(10, 5));
            panel.setBackground(COLOR_DRAWER_BG);
            panel.setBorder(new EmptyBorder(8, 10, 8, 10));

            JLabel lblAvatar = new JLabel(generarAvatar(value.usuario, 32));
            lblAvatar.setPreferredSize(new Dimension(34, 34));
            panel.add(lblAvatar, BorderLayout.WEST);

            JPanel center = new JPanel(new BorderLayout());
            center.setBackground(COLOR_DRAWER_BG);
            JLabel lblName = new JLabel(value.usuario);
            lblName.setFont(FONT_BOLD);
            lblName.setForeground(COLOR_TEXT);

            JTextArea areaTexto = new JTextArea(value.texto);
            areaTexto.setBackground(COLOR_DRAWER_BG);
            areaTexto.setForeground(new Color(220, 220, 220));
            areaTexto.setFont(FONT_PLAIN);
            areaTexto.setLineWrap(true);
            areaTexto.setWrapStyleWord(true);
            areaTexto.setEditable(false);

            center.add(lblName, BorderLayout.NORTH);
            center.add(areaTexto, BorderLayout.CENTER);

            JLabel lblFecha = new JLabel(value.fecha != null ? value.fecha : "");
            lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblFecha.setForeground(Color.LIGHT_GRAY);
            lblFecha.setHorizontalAlignment(SwingConstants.RIGHT);

            panel.add(center, BorderLayout.CENTER);
            panel.add(lblFecha, BorderLayout.EAST);

            return panel;
        }
    }

    private ImageIcon generarAvatar(String nombre, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int hash = nombre.hashCode();
        Color c = new Color((hash & 0xFF0000) >> 16, (hash & 0x00FF00) >> 8, hash & 0x0000FF);
        if (c.getRed() + c.getGreen() + c.getBlue() < 200) {
            c = c.brighter();
        }
        g2.setColor(c);
        g2.fill(new Ellipse2D.Double(0, 0, size, size));
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        String inicial = nombre.length() > 0 ? nombre.substring(0, 1).toUpperCase() : "?";
        int x = (size - fm.stringWidth(inicial)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(inicial, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }

    private ImageIcon recortarImagenCuadrada(String ruta, int size) throws ImageLoadException {
        try {
            File f = new File(ruta);
            if (!f.exists()) {
                throw new ImageLoadException("Archivo de imagen no existe: " + ruta);
            }

            BufferedImage original = ImageIO.read(f);
            if (original == null) {
                throw new ImageLoadException("No se pudo leer la imagen (null): " + ruta);
            }

            int w = original.getWidth();
            int h = original.getHeight();
            int cropSize = Math.min(w, h);
            int x = (w - cropSize) / 2;
            int y = (h - cropSize) / 2;

            BufferedImage cropped = original.getSubimage(x, y, cropSize, cropSize);
            Image scaled = cropped.getScaledInstance(size, size, Image.SCALE_SMOOTH);

            return new ImageIcon(scaled);

        } catch (IOException e) {
            throw new ImageLoadException("Error leyendo la imagen: " + ruta, e);
        } catch (Exception e) {
            throw new ImageLoadException("Error al procesar la imagen: " + ruta, e);
        }
    }

    private ImageIcon ajustarImagenAlFeed(String ruta, int maxWidth, int maxHeight) throws ImageLoadException {
        try {
            File file = new File(ruta);
            if (!file.isFile()) {
                throw new ImageLoadException("Archivo de imagen no existe: " + ruta);
            }
            BufferedImage original = ImageIO.read(file);
            if (original == null) {
                throw new ImageLoadException("No se pudo leer la imagen: " + ruta);
            }
            double scale = Math.min((double) maxWidth / original.getWidth(), (double) maxHeight / original.getHeight());
            int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
            return new ImageIcon(original.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (ImageLoadException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ImageLoadException("Error leyendo la imagen: " + ruta, ex);
        }
    }

    private void regresarAlPerfil() {
        if (backAction != null) {
            try {
                backAction.run();
                return;
            } catch (Exception ex) {
            }
        }

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.setContentPane(new InstaProfileUI(currentUser));
            frame.pack();
            frame.revalidate();
            frame.repaint();
        }
    }

    private void abrirHashtag(String hashtag) {
        if (hashtag == null || hashtag.isBlank()) {
            return;
        }
        mostrarPanel(new HashtagSearchUI(currentUser, hashtag));
    }

    private void abrirMencion(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String resolved = resolveUsername(username);
        if (resolved == null) {
            JOptionPane.showMessageDialog(this, "La mención @" + username
                    + " está guardada, pero ese usuario no existe o está desactivado.",
                    "Mención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        mostrarPanel(resolved.equalsIgnoreCase(currentUser)
                ? new InstaProfileUI(currentUser)
                : new VisibilidadProfileUI(resolved, currentUser));
    }

    private String resolveUsername(String username) {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager != null) {
                for (String candidate : manager.searchUsers(username)) {
                    if (candidate.equalsIgnoreCase(username)) {
                        return candidate;
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private void mostrarPanel(Component panel) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            frame.setContentPane((Container) panel);
            frame.pack();
            frame.revalidate();
            frame.repaint();
        }
    }
}
