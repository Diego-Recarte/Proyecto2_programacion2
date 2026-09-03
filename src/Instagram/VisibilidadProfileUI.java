package Instagram;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COLOR_BG);
        panel.setPreferredSize(new Dimension(400, 260));
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
                    frame.setLocationRelativeTo(null);
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
                frame.setLocationRelativeTo(null);
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
                instaManager manager = instaController.getInstance().getInsta();
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
            instaManager manager = instaController.getInstance().getInsta();
            ArrayList<String[]> posts = manager.getPosts(profileUser);

            if (posts == null || posts.isEmpty()) {
                JLabel lblVacio = new JLabel("Nada que ver aqui...", SwingConstants.CENTER);
                lblVacio.setForeground(Color.GRAY);
                lblVacio.setPreferredSize(new Dimension(380, 50));
                gridFotos.add(lblVacio);
            } else {
                for (int i = 0; i < posts.size(); i++) {
                    String[] post = posts.get(i);
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
                                    instaManager manager = instaController.getInstance().getInsta();
                                    ArrayList<String[]> allPosts = manager.getPosts(profileUser);

                                    Runnable backAction = () -> {
                                        frame.setContentPane(VisibilidadProfileUI.this);
                                        frame.pack();
                                        frame.setLocationRelativeTo(null);
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
        try {
            File f = new File(ruta);
            if (!f.exists()) {
                throw new ImageLoadException("Archivo no existe: " + ruta);
            }
            BufferedImage original = ImageIO.read(f);
            if (original == null) {
                throw new ImageLoadException("No se pudo leer la imagen: " + ruta);
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
            throw new ImageLoadException("Error I/O al leer imagen: " + ruta, e);
        } catch (ImageLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ImageLoadException("Error procesando imagen: " + ruta, e);
        }
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

    private void cargarDatosPerfil() {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager == null) {
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
            String generoStr = (genero == 'M') ? "Demonio" : (genero == 'F' ? "Bruja" : "Ente");

            lblInfo.setText("<html>Edad: " + edad + " años<br>Clase: " + generoStr + "<br>Desde: " + fecha + "</html>");

            int followers = manager.getFollowersCount(profileUser);
            int following = manager.getFollowingCount(profileUser);

            ArrayList<String[]> posts = manager.getPosts(profileUser);
            int evidencias = (posts == null) ? 0 : posts.size();

            lblStats.setText(statsHtml(evidencias, followers, following));

            manager.setLoggedUser(viewer);
            boolean sigo = manager.isFollowing(profileUser);
            findAndSetFollowText(this, sigo ? "Dejar de seguir" : "Seguir");

        } catch (IOException e) {
            lblName.setText("Error de conexión.");
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
                frame.setLocationRelativeTo(null);
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
                frame.setLocationRelativeTo(null);
                frame.revalidate();
                frame.repaint();
            }
        });
        bar.add(btnBuscar);

        JButton btnSubir = crearBotonNav("Subir", InstaNavIcon.Type.ADD);
        btnSubir.setForeground(COLOR_BTN);
        btnSubir.addActionListener(e -> {
            try {
                String osUser = null;
                try {
                    if (Logica.ManejoUsuarios.UserLogged.getInstance().getUserLogged() != null) {
                        osUser = Logica.ManejoUsuarios.UserLogged.getInstance().getUserLogged().getName();
                    }
                } catch (Exception ex) {
                    osUser = null;
                }
                final String targetUser = (osUser != null && !osUser.trim().isEmpty()) ? osUser : viewer;

                final File usersRoot = new File("src\\Z\\Usuarios");
                final File userRoot = new File(usersRoot, targetUser);
                final File imagesFolder = new File(userRoot, "Mis Imagenes");
                if (!imagesFolder.exists()) {
                    imagesFolder.mkdirs();
                }

                final String usersRootCanonical = safeCanonical(usersRoot);
                final String userRootCanonical = safeCanonical(userRoot);

                JFileChooser fileChooser = new JFileChooser(imagesFolder) {
                    @Override
                    public void approveSelection() {
                        File sel = getSelectedFile();
                        if (sel != null) {
                            try {
                                String selCan = sel.getCanonicalPath();
                                if (selCan.startsWith(usersRootCanonical) && !selCan.startsWith(userRootCanonical)) {
                                    JOptionPane.showMessageDialog(this,
                                            "Acceso denegado: no puedes seleccionar archivos dentro de la carpeta de otro usuario.",
                                            "Acceso Denegado",
                                            JOptionPane.WARNING_MESSAGE);
                                    return;
                                }
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(this,
                                        "Error verificando la ruta seleccionada.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        super.approveSelection();
                    }

                    @Override
                    public void setCurrentDirectory(File dir) {
                        if (dir != null) {
                            try {
                                String dirCan = dir.getCanonicalPath();
                                if (dirCan.startsWith(usersRootCanonical) && !dirCan.startsWith(userRootCanonical)) {
                                    super.setCurrentDirectory(imagesFolder);
                                    return;
                                }
                            } catch (IOException ex) {
                                super.setCurrentDirectory(imagesFolder);
                                return;
                            }
                        }
                        super.setCurrentDirectory(dir);
                    }
                };

                fileChooser.setDialogTitle("Selecciona la evidencia (no puedes acceder a carpetas de otros usuarios)");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg", "gif", "bmp", "webp"));

                fileChooser.addPropertyChangeListener(evt -> {
                    if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                        Object newVal = evt.getNewValue();
                        if (newVal instanceof File) {
                            File newDir = (File) newVal;
                            try {
                                String newCan = newDir.getCanonicalPath();
                                if (newCan.startsWith(usersRootCanonical) && !newCan.startsWith(userRootCanonical)) {
                                    SwingUtilities.invokeLater(() -> {
                                        fileChooser.setCurrentDirectory(imagesFolder);
                                        JOptionPane.showMessageDialog(this,
                                                "No puedes acceder a carpetas de otros usuarios.",
                                                "Acceso Denegado",
                                                JOptionPane.WARNING_MESSAGE);
                                    });
                                }
                            } catch (IOException ex) {
                                SwingUtilities.invokeLater(() -> fileChooser.setCurrentDirectory(imagesFolder));
                            }
                        }
                    }
                });

                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    try {
                        String selCan = selectedFile.getCanonicalPath();
                        if (selCan.startsWith(usersRootCanonical) && !selCan.startsWith(userRootCanonical)) {
                            JOptionPane.showMessageDialog(this,
                                    "Acceso denegado: no puedes seleccionar archivos dentro de la carpeta de otro usuario.",
                                    "Acceso Denegado",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error verificando la ruta seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String caption = JOptionPane.showInputDialog(this, "Escribe una descripción:", "Nuevo Post", JOptionPane.PLAIN_MESSAGE);
                    if (caption == null) {
                        caption = "";
                    }

                    try {
                        if (!imagesFolder.exists()) {
                            imagesFolder.mkdirs();
                        }
                        String uniqueName = System.currentTimeMillis() + "_" + selectedFile.getName();
                        File destFile = new File(imagesFolder, uniqueName);

                        if (!selectedFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
                            java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }

                        instaManager manager = instaController.getInstance().getInsta();
                        manager.setLoggedUser(viewer);
                        manager.addPost(destFile.getAbsolutePath(), viewer, caption);

                        JOptionPane.showMessageDialog(this, "Subido con éxito.");
                        cargarDatosPerfil();
                        cargarPostsEnGrid();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error al subir: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir selector: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        for (java.awt.event.ActionListener listener : btnSubir.getActionListeners()) {
            btnSubir.removeActionListener(listener);
        }
        btnSubir.addActionListener(e -> InstaPostComposer.open(this, viewer, () -> {
            cargarDatosPerfil();
            cargarPostsEnGrid();
        }));

        bar.add(btnSubir);

        JButton btnChat = crearBotonNav("Mensajes", InstaNavIcon.Type.MESSAGE);
        btnChat.setForeground(COLOR_BTN);
        btnChat.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame frame) {
                frame.setContentPane(new InstaChatUI(viewer));
                frame.pack();
                frame.setLocationRelativeTo(null);
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
                frame.setLocationRelativeTo(null);
                frame.revalidate();
                frame.repaint();
            }
        });
        bar.add(btnPerfil);

        return bar;
    }

    private String safeCanonical(File f) {
        try {
            return f.getCanonicalPath();
        } catch (IOException ex) {
            return f.getAbsolutePath();
        }
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
