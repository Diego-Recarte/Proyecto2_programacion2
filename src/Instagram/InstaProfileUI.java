/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Instagram;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import Logica.ManejoUsuarios.UserLogged;
import Logica.Excepciones.ImageLoadException;

/**
 *
 * @author esteb
 */
public class InstaProfileUI extends JPanel {

    private final String username;
    private final String viewer;
    private JLabel lblFoto;
    private JLabel lblName;
    private JLabel lblInfo;
    private JLabel lblStats;
    private JPanel gridFotos;

    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_BORDER = new Color(100, 100, 100);
    private final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 12);

    private final Color COLOR_BTN = new Color(255, 69, 0);
    private final Color COLOR_BTN_HOVER = new Color(200, 50, 0);
    private final Font FONT_CAOS = new Font("Segoe UI", Font.BOLD, 12);

    public InstaProfileUI(String username) {
        this(username, username);
    }

    public InstaProfileUI(String username, String viewer) {
        this.username = username;
        this.viewer = viewer != null ? viewer : username;

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

        if (!viewer.equals(username)) {
            JLabel lblBack = new JLabel("←  Volver");
            lblBack.setForeground(COLOR_BTN);
            lblBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblBack.setBounds(10, 10, 120, 30);
            lblBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblBack.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Window window = SwingUtilities.getWindowAncestor(InstaProfileUI.this);
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
        } else {
            JButton btnLogout = new BotonRojo("Salir");
            btnLogout.setBounds(310, 10, 70, 25);
            btnLogout.addActionListener(e -> {
                int resp = JOptionPane.showConfirmDialog(
                        this,
                        "¿Realmente deseas Salir?",
                        "Confirmar Cierre de Sesión",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (resp == JOptionPane.YES_OPTION) {
                    cerrarSesion();
                }
            });
            panel.add(btnLogout);
        }

        int titleX = viewer.equals(username) ? 15 : 60;
        JLabel lblTitle = new JLabel("@" + username);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(COLOR_TEXT);
        lblTitle.setBounds(titleX, 10, 250, 30);
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

        if (viewer.equals(username)) {
            JButton btnEdit = new BotonRojo("Editar perfil");
            btnEdit.setBounds(130, 110, 240, 30);
            btnEdit.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(InstaProfileUI.this);
                if (window instanceof JFrame) {
                    JFrame frame = (JFrame) window;
                    frame.setContentPane(new InstaProfileEditUI(username));
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.revalidate();
                    frame.repaint();
                }
            });
            panel.add(btnEdit);
        } else {
            JButton btnFollow = new BotonRojo("Seguir");
            btnFollow.setBounds(130, 110, 110, 30);
            panel.add(btnFollow);

            JButton btnVerTweets = new BotonRojo("Mensaje");
            btnVerTweets.setBounds(250, 110, 120, 30);
            panel.add(btnVerTweets);

            btnFollow.addActionListener(e -> {
                try {
                    instaManager manager = instaController.getInstance().getInsta();
                    if (manager == null) {
                        return;
                    }
                    manager.setLoggedUser(viewer);
                    boolean sigo = manager.isFollowing(username);
                    if (!sigo) {
                        boolean ok = manager.addFollow(username);
                        if (ok) {
                            JOptionPane.showMessageDialog(this, "Ahora sigues a " + username);
                        } else {
                            JOptionPane.showMessageDialog(this, "No se pudo seguir.");
                        }
                    } else {
                        int resp = JOptionPane.showConfirmDialog(this, "¿Dejar de seguir a " + username + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                        if (resp == JOptionPane.YES_OPTION) {
                            manager.quitarFollow(username);
                            JOptionPane.showMessageDialog(this, "Has dejado de seguir a " + username);
                        }
                    }
                    cargarDatosPerfil();
                    cargarPostsEnGrid();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error follow: " + ex.getMessage());
                }
            });

            btnVerTweets.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JFrame frame) {
                    frame.setContentPane(new InstaChatUI(viewer, username));
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.revalidate();
                    frame.repaint();
                }
            });
        }

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
            ArrayList<String[]> posts = manager.getPosts(username);

            if (posts == null || posts.isEmpty()) {
                JLabel lblVacio = new JLabel("Nada que ver aqui...", SwingConstants.CENTER);
                lblVacio.setForeground(Color.GRAY);
                lblVacio.setPreferredSize(new Dimension(380, 50));
                gridFotos.add(lblVacio);
            } else {
                for (int i = 0; i < posts.size(); i++) {
                    String[] post = posts.get(i);
                    String mediaReference = post.length > 0 ? post[0] : "";
                    int mediaCount = InstaPostMedia.decode(mediaReference).size();
                    String rutaImg = InstaPostMedia.coverPath(mediaReference);
                    final int index = i;

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
                            Window window = SwingUtilities.getWindowAncestor(InstaProfileUI.this);
                            if (window instanceof JFrame) {
                                JFrame frame = (JFrame) window;
                                try {
                                    instaManager manager = instaController.getInstance().getInsta();
                                    ArrayList<String[]> allPosts = manager.getPosts(username);

                                    Runnable backAction = () -> {
                                        frame.setContentPane(InstaProfileUI.this);
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
                                    JOptionPane.showMessageDialog(InstaProfileUI.this, "Error abriendo post: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            ImageIcon originalIcon = new ImageIcon(ruta);
            if (originalIcon.getIconWidth() <= 0 || originalIcon.getIconHeight() <= 0) {
                throw new ImageLoadException("Icon inválido o no se pudo cargar: " + ruta);
            }
            Image img = originalIcon.getImage();
            BufferedImage buffered = new BufferedImage(originalIcon.getIconWidth(), originalIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffered.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            int w = buffered.getWidth();
            int h = buffered.getHeight();
            int cropSize = Math.min(w, h);
            int x = (w - cropSize) / 2;
            int y = (h - cropSize) / 2;
            BufferedImage cropped = buffered.getSubimage(x, y, cropSize, cropSize);
            Image scaled = cropped.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
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

    private void subirPost() {
        String osUser = null;
        try {
            if (UserLogged.getInstance().getUserLogged() != null) {
                osUser = UserLogged.getInstance().getUserLogged().getName();
            }
        } catch (Exception ex) {
            osUser = null;
        }

        final String targetUser = (osUser != null && !osUser.trim().isEmpty()) ? osUser : username;

        final File usersRoot = new File("src\\Z\\Usuarios");
        final File userRoot = new File(usersRoot, targetUser);
        final File imagesFolder = new File(userRoot, "Mis Imagenes");

        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        JFileChooser fc = new JFileChooser(imagesFolder);
        fc.setDialogTitle("Selecciona la evidencia");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg", "gif", "bmp", "webp"));

        int r = fc.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = fc.getSelectedFile();
        String caption = JOptionPane.showInputDialog(this, "Escribe una descripción:", "Nuevo Post", JOptionPane.PLAIN_MESSAGE);
        if (caption == null) {
            caption = "";
        }

        try {
            instaManager manager = instaController.getInstance().getInsta();
            manager.setLoggedUser(username);

            String unique = System.currentTimeMillis() + "_" + selected.getName();
            File dest = new File(imagesFolder, unique);

            boolean sameFile = false;
            try {
                if (dest.exists()) {
                    sameFile = java.nio.file.Files.isSameFile(selected.toPath(), dest.toPath());
                }
                if (!sameFile) {
                    File[] all = imagesFolder.listFiles();
                    if (all != null) {
                        for (File f : all) {
                            try {
                                if (java.nio.file.Files.isSameFile(selected.toPath(), f.toPath())) {
                                    sameFile = true;
                                    dest = f;
                                    break;
                                }
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                sameFile = false;
            }

            if (!sameFile) {
                String srcHash = sha1OfFile(selected);
                boolean match = false;

                File[] all = imagesFolder.listFiles();
                if (all != null) {
                    for (File f : all) {
                        if (!f.isFile()) {
                            continue;
                        }
                        if (srcHash.equals(sha1OfFile(f))) {
                            dest = f;
                            match = true;
                            break;
                        }
                    }
                }

                if (!match) {
                    java.nio.file.Path srcP = selected.toPath();
                    java.nio.file.Path dstP = dest.toPath();
                    try {
                        if (!srcP.toRealPath().equals(dstP.getParent().toRealPath().resolve(dstP.getFileName()))) {
                            java.nio.file.Files.copy(srcP, dstP, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            dest = selected;
                        }
                    } catch (Exception ex) {
                        java.nio.file.Files.copy(srcP, dstP, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            manager.addPost(dest.getAbsolutePath(), username, caption);
            JOptionPane.showMessageDialog(this, "Post subido.");
            cargarDatosPerfil();
            cargarPostsEnGrid();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private static String sha1OfFile(File f) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
        java.io.InputStream is = new java.io.FileInputStream(f);
        byte[] buf = new byte[8192];
        int r;
        while ((r = is.read(buf)) > 0) {
            md.update(buf, 0, r);
        }
        is.close();
        byte[] dig = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : dig) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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

    private void cargarDatosPerfil() {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager == null) {
                return;
            }

            String rutaFoto = manager.getProfilePic(username);
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

            String realName = manager.getRealName(username);
            lblName.setText(realName != null ? realName : "Sin Nombre");

            int edad = manager.getAge(username);
            char genero = manager.getGender(username);
            String fecha = manager.getEntryDate(username);
            String generoStr = (genero == 'M') ? "M️" : (genero == 'F' ? "F️" : "Ninguno");

            lblInfo.setText("<html>Edad: " + edad + " años<br>Genero: " + generoStr + "<br>Desde: " + fecha + "</html>");

            ArrayList<String[]> posts = manager.getPosts(username);
            int Publicaciones = (posts == null) ? 0 : posts.size();
            int Seguidores = manager.getFollowersCount(username);
            int Seguidos = manager.getFollowingCount(username);

            lblStats.setText(statsHtml(Publicaciones, Seguidores, Seguidos));

        } catch (IOException e) {
            lblName.setText("Error de conexión.");
        }
    }

    private void cerrarSesion() {
        instaManager manager = instaController.getInstance().getInsta();
        if (manager != null) {
            manager.loggoutUser();
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.setContentPane(new InstaLoginUI());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.revalidate();
            frame.repaint();
        }
    }

    private String statsHtml(int posts, int followers, int following) {
        return "<html><table style='color:white;text-align:center'><tr>"
                + "<td width='82'><b>" + posts + "</b><br>Publicaciones</td>"
                + "<td width='82'><b>" + followers + "</b><br>Seguidores</td>"
                + "<td width='82'><b>" + following + "</b><br>Seguidos</td>"
                + "</tr></table></html>";
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
