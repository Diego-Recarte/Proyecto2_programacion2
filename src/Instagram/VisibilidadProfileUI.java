package Instagram;

import Logica.Ventanas.InstaImages;
import Logica.Ventanas.InstaWindowLayout;



import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import Logica.Estructuras.ListaEnlazada;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import Logica.Excepciones.ImageLoadException;


public class VisibilidadProfileUI extends JPanel {

    private final String profileUser;
    private final String viewer;

    private JLabel lblFoto;
    private JLabel lblName;
    private JLabel lblInfo;
    private JLabel lblStats;
    private JPanel gridFotos;

    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_BTN = new Color(255, 69, 0);
    private final Color COLOR_BTN_HOVER = new Color(200, 50, 0);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_BORDER = new Color(100, 100, 100);
    private final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FONT_CAOS = new Font("Segoe UI", Font.BOLD, 12);

    public VisibilidadProfileUI(String profileUser, String viewer) {
        this.profileUser = profileUser;
        this.viewer = viewer != null ? viewer : profileUser;

        putClientProperty("insta.manager", instaController.getInstance().getInsta(viewer));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 650));
        setBackground(COLOR_BG);

        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(COLOR_BG);

        contentContainer.add(crearPanelSuperior(), BorderLayout.NORTH);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(COLOR_BG);
        gridWrapper.add(crearPanelGrid(), BorderLayout.NORTH);

        contentContainer.add(gridWrapper, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(contentContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setBackground(COLOR_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);

        add(crearBarraNavegacionInferior(), BorderLayout.SOUTH);

        cargarDatosPerfil();
        InstaWindowLayout.install(this);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COLOR_BG);
        panel.setPreferredSize(new Dimension(400, 260));
        panel.setMinimumSize(new Dimension(400, 260));
        panel.setMaximumSize(new Dimension(400, 260));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        JLabel lblBack = new JLabel("←");
        lblBack.setForeground(COLOR_BTN);
        lblBack.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBack.setBounds(10, 10, 30, 30);
        lblBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(VisibilidadProfileUI.this);
                if (window instanceof JFrame) {
                    JFrame frame = (JFrame) window;
                    frame.setContentPane(new InstaEditProfileUI(viewer));
                    frame.pack();
                    frame.revalidate();
                    frame.repaint();
                }
            }
        });
        panel.add(lblBack);

        JLabel lblTitle = new JLabel("@" + profileUser);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(COLOR_TEXT);
        lblTitle.setBounds(60, 10, 250, 30);
        panel.add(lblTitle);

        lblFoto = new JLabel("Sin Rostro");
        lblFoto.setBounds(15, 50, 90, 90);
        lblFoto.setBorder(new LineBorder(COLOR_BTN, 3));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setForeground(Color.GRAY);
        panel.add(lblFoto);

        lblStats = new JLabel(statsHtml(0, 0, 0));
        lblStats.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStats.setForeground(COLOR_TEXT);
        lblStats.setHorizontalAlignment(SwingConstants.CENTER);
        lblStats.setBounds(120, 50, 260, 60);
        panel.add(lblStats);

        JButton btnFollow = new BotonRojo("Seguir");
        btnFollow.setBounds(130, 110, 112, 30);
        panel.add(btnFollow);

        JButton btnMessage = new BotonRojo("Mensaje");
        btnMessage.setBounds(252, 110, 118, 30);
        btnMessage.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame frame) {
                frame.setContentPane(new InstaChatUI(viewer, profileUser));
                frame.pack();
                frame.revalidate();
                frame.repaint();
            }
        });
        panel.add(btnMessage);

        lblName = new JLabel("Cargando nombre...");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(COLOR_TEXT);
        lblName.setBounds(15, 150, 350, 20);
        panel.add(lblName);

        lblInfo = new JLabel("Cargando datos...");
        lblInfo.setFont(FONT_TEXT);
        lblInfo.setForeground(Color.LIGHT_GRAY);
        lblInfo.setBounds(15, 175, 360, 70);
        lblInfo.setVerticalAlignment(SwingConstants.TOP);
        panel.add(lblInfo);

        btnFollow.addActionListener(e -> {
            try {
                instaManager manager = instaController.getInstance().getInsta(viewer);
                if (manager == null) {
                    return;
                }

                manager.setLoggedUser(viewer);
                boolean sigo = manager.isFollowing(profileUser);
                if (!sigo) {
                    boolean ok = manager.addFollow(profileUser);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Ahora sigues a " + profileUser, "Seguimiento", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo seguir a " + profileUser, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    int resp = JOptionPane.showConfirmDialog(this, "¿Dejar de seguir a " + profileUser + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) {
                        manager.quitarFollow(profileUser);
                        JOptionPane.showMessageDialog(this, "Has dejado de seguir a " + profileUser, "Seguimiento", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                cargarDatosPerfil();
                cargarPostsEnGrid();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error en operación: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel crearPanelGrid() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(COLOR_BG);

        JLabel lblGridTitle = new JLabel(" PUBLICACIONES");
        lblGridTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblGridTitle.setForeground(COLOR_BTN);
        lblGridTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        container.add(lblGridTitle, BorderLayout.NORTH);

        gridFotos = new JPanel(new GridLayout(0, 3, 2, 2));
        gridFotos.setBackground(COLOR_BG);

        cargarPostsEnGrid();

        container.add(gridFotos, BorderLayout.CENTER);
        return container;
    }

    private void cargarPostsEnGrid() {
        gridFotos.removeAll();
        try {
            instaManager manager = instaController.getInstance().getInsta(viewer);
            ListaEnlazada<String[]> posts = manager.getPosts(profileUser);

            if (posts == null || posts.isEmpty()) {
                JLabel lblVacio = new JLabel("Nada que ver aqui...", SwingConstants.CENTER);
                lblVacio.setForeground(Color.GRAY);
                gridFotos.setPreferredSize(new Dimension (400, 300));
                gridFotos.setMaximumSize(new Dimension (400, 300));
                gridFotos.setMinimumSize(new Dimension (400, 300));
               
                gridFotos.add(lblVacio);
            } else {
                int position = 0;
                for (String[] post : posts) {
                    final int i = position++;
                    final int index = i;
                    String mediaReference = post.length > 0 ? post[0] : "";
                    int mediaCount = InstaPostMedia.decode(mediaReference).size();
                    String rutaImg = InstaPostMedia.coverPath(mediaReference);

                    JPanel frameFoto = new JPanel(new BorderLayout());
                    frameFoto.setBackground(new Color(20, 20, 20));
                    frameFoto.setPreferredSize(new Dimension(130, 130));
                    frameFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    JLabel lblImg = new JLabel();
                    lblImg.setHorizontalAlignment(SwingConstants.CENTER);

                    ImageIcon icon = null;
                    try {
                        icon = recortarImagenCuadrada(rutaImg, 130);
                    } catch (ImageLoadException ex) {
                        System.err.println("Error cargando miniatura: " + ex.getMessage());
                    }
                    if (icon != null) {
                        lblImg.setIcon(icon);
                    } else {
                        lblImg.setText("?");
                        lblImg.setForeground(Color.GRAY);
                    }

                    if (mediaCount > 1) {
                        JLayeredPane thumbnailLayer = new JLayeredPane();
                        thumbnailLayer.setPreferredSize(new Dimension(130, 130));
                        lblImg.setBounds(0, 0, 130, 130);
                        thumbnailLayer.add(lblImg, JLayeredPane.DEFAULT_LAYER);
                        JLabel carouselBadge = crearIndicadorCarrusel(mediaCount);
                        carouselBadge.setBounds(88, 7, 36, 20);
                        thumbnailLayer.add(carouselBadge, JLayeredPane.PALETTE_LAYER);
                        frameFoto.setToolTipText("Carrusel de " + mediaCount + " imágenes");
                        frameFoto.add(thumbnailLayer, BorderLayout.CENTER);
                    } else {
                        frameFoto.add(lblImg, BorderLayout.CENTER);
                    }

                    frameFoto.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            Window window = SwingUtilities.getWindowAncestor(VisibilidadProfileUI.this);
                            if (window instanceof JFrame) {
                                JFrame frame = (JFrame) window;
                                try {
                                    instaManager manager = instaController.getInstance().getInsta(viewer);
                                    ListaEnlazada<String[]> allPosts = manager.getPosts(profileUser);

                                    Runnable backAction = () -> {
                                        frame.setContentPane(VisibilidadProfileUI.this);
                                        frame.pack();
                                        frame.revalidate();
                                        frame.repaint();
                                    };

                                    InstaPostUI postUI = new InstaPostUI(viewer, allPosts, index, backAction);
                                    frame.setContentPane(postUI);
                                    frame.pack();
                                    frame.revalidate();
                                    frame.repaint();
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(VisibilidadProfileUI.this, "Error abriendo post: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    });

                    gridFotos.add(frameFoto);
                }
            }
        } catch (IOException e) {
            gridFotos.add(new JLabel("Error cargando"));
        }
        gridFotos.revalidate();
        gridFotos.repaint();
    }

    private ImageIcon recortarImagenCuadrada(String ruta, int size) throws ImageLoadException {
        return InstaImages.icon(this, ruta, size, size, true);
    }

    private JLabel crearIndicadorCarrusel(int total) {
        JLabel badge = new JLabel("1/" + total, SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(new Color(20, 20, 20, 220));
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setBorder(BorderFactory.createLineBorder(new Color(255, 90, 35)));
        return badge;
    }

    void refreshActiveState(java.util.Set<String> activeUsers) {
        if (!activeUsers.contains(profileUser)) {
            lblName.setText("Cuenta no disponible");
            lblInfo.setText(""); lblStats.setText(""); lblFoto.setIcon(null); lblFoto.setText("");
            gridFotos.removeAll(); gridFotos.revalidate(); gridFotos.repaint();
        }
    }

    private void cargarDatosPerfil() {
        try {
            instaManager manager = instaController.getInstance().getInsta(viewer);
            if (manager == null) {
                return;
            }

            if (!manager.getStatusUser(profileUser)) {
                refreshActiveState(java.util.Set.of());
                return;
            }
            String rutaFoto = manager.getProfilePic(profileUser);
            if (rutaFoto != null && !rutaFoto.isEmpty() && !rutaFoto.equals("futura referencia de imagen aqui")) {
                ImageIcon icon = null;
                try {
                    icon = recortarImagenCuadrada(rutaFoto, 90);
                } catch (ImageLoadException ex) {
                    System.err.println("Error cargando foto de perfil: " + ex.getMessage());
                }
                if (icon != null) {
                    lblFoto.setIcon(icon);
                    lblFoto.setText("");
                } else {
                    lblFoto.setIcon(null);
                    lblFoto.setText("Sin Rostro");
                }

            } else {
                lblFoto.setIcon(null);
                lblFoto.setText("Sin Rostro");
            }

            String realName = manager.getRealName(profileUser);
            lblName.setText(realName != null ? realName : "Sin Nombre");

            int edad = manager.getAge(profileUser);
            char genero = manager.getGender(profileUser);
            String fecha = manager.getEntryDate(profileUser);
            String generoStr = (genero == 'M') ? "M" : "F";

            lblInfo.setText("<html>Edad: " + edad + " años<br>Género: " + generoStr + "<br>Desde: " + fecha + " · " + (manager.getStatusUser(profileUser) ? "Activa" : "Inactiva") + "</html>");

            int followers = manager.getFollowersCount(profileUser);
            int following = manager.getFollowingCount(profileUser);

            ListaEnlazada<String[]> posts = manager.getPosts(profileUser);
            int evidencias = (posts == null) ? 0 : posts.size();

            lblStats.setText(statsHtml(evidencias, followers, following));

            manager.setLoggedUser(viewer);
            boolean sigo = manager.isFollowing(profileUser);
            findAndSetFollowText(this, sigo ? "Dejar de seguir" : "Seguir");

        } catch (IOException e) {
            refreshActiveState(java.util.Set.of());
        }
    }

    private void findAndSetFollowText(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                String cur = b.getText() != null ? b.getText().toLowerCase() : "";
                if (cur.contains("seguir") || cur.contains("alterar")) {
                    b.setText(text);
                    return;
                }
            } else if (c instanceof Container) {
                findAndSetFollowText((Container) c, text);
            }
        }
    }

    private JPanel crearBarraNavegacionInferior() {
        JPanel bar = new JPanel(new GridLayout(1, 5));
        bar.setBackground(new Color(20, 20, 20));
        bar.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, COLOR_BTN));
        bar.setPreferredSize(new Dimension(400, 60));

        JButton btnInicio = crearBotonNav("Inicio", InstaNavIcon.Type.HOME);
        btnInicio.setForeground(COLOR_BTN);
        btnInicio.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                frame.setContentPane(new InstaFeedUI(viewer));
                frame.pack();
                frame.revalidate();
                frame.repaint();
            }
        });
        bar.add(btnInicio);

        JButton btnBuscar = crearBotonNav("Buscar", InstaNavIcon.Type.SEARCH);
        btnBuscar.setForeground(COLOR_BTN);
        btnBuscar.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                frame.setContentPane(new HashtagSearchUI(viewer));
                frame.pack();
                frame.revalidate();
                frame.repaint();
            }
        });
        bar.add(btnBuscar);

        JButton btnSubir = crearBotonNav("Subir", InstaNavIcon.Type.ADD);
        btnSubir.setForeground(COLOR_BTN);
        btnSubir.addActionListener(e -> InstaPostComposer.open(this, viewer, () -> {
            cargarDatosPerfil();
            cargarPostsEnGrid();
        }));

        bar.add(btnSubir);

        JButton btnChat = crearBotonNav("Mensajes", InstaNavIcon.Type.MESSAGE);
        InstaMessageBadge.install(btnChat);
        btnChat.setForeground(COLOR_BTN);
        btnChat.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame frame) {
                frame.setContentPane(new InstaChatUI(viewer));
                frame.pack();
                frame.revalidate();
                frame.repaint();
            }
        });
        bar.add(btnChat);

        JButton btnPerfil = crearBotonNav("Perfil", InstaNavIcon.Type.PROFILE);
        btnPerfil.setForeground(COLOR_BTN);
        btnPerfil.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                frame.setContentPane(new InstaProfileUI(viewer));
                frame.pack();
                frame.revalidate();
                frame.repaint();
            }
        });
        bar.add(btnPerfil);

        return bar;
    }


    private String statsHtml(int posts, int followers, int following) {
        return "<html><table style='color:white;text-align:center'><tr>"
                + "<td width='82'><b>" + posts + "</b><br>Publicaciones</td>"
                + "<td width='82'><b>" + followers + "</b><br>Seguidores</td>"
                + "<td width='82'><b>" + following + "</b><br>Seguidos</td>"
                + "</tr></table></html>";
    }

    private JButton crearBotonNav(String texto, InstaNavIcon.Type type) {
        JButton btn = new JButton(new InstaNavIcon(type, 24));
        btn.setToolTipText(texto);
        btn.setBackground(new Color(20, 20, 20));
        btn.setForeground(Color.GRAY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
