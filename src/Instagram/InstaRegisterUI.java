package Instagram;

import Logica.Excepciones.InvalidDataException;
import Logica.Ventanas.genFondos;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class InstaRegisterUI extends JPanel {

    private JTextField txtNombre;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtEdad;
    private JComboBox<String> cbGenero;

    private JLabel lblFotoPreview;
    private String rutaFotoSeleccionada = "";

    private JButton btnRegistrar;
    private JLabel lblBackLogin;

    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_BTN = new Color(255, 69, 0);
    private final Color COLOR_BTN_HOVER = new Color(200, 50, 0);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_BORDER = new Color(100, 100, 100);
    private final Font FONT_CAOS = new Font("Segoe UI", Font.BOLD, 12);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);

    genFondos panelFondos;

    public InstaRegisterUI() {
        setLayout(null);
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(400, 650));
        initComponentes();
    }

    private void initComponentes() {
        JLabel lblTitulo = new JLabel("Únete a la comunidad");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COLOR_TEXT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBounds(40, 20, 320, 30);
        add(lblTitulo);

        JLabel lblSub = new JLabel(", sube reals y chatea con tus amigos.");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSub.setForeground(Color.LIGHT_GRAY);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setBounds(40, 50, 320, 30);
        add(lblSub);

        lblFotoPreview = new JLabel();
        lblFotoPreview.setBounds(150, 90, 100, 100);
        lblFotoPreview.setBorder(new LineBorder(COLOR_BTN, 2));
        lblFotoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblFotoPreview.setForeground(COLOR_TEXT);
        lblFotoPreview.setFont(FONT_CAOS);
        lblFotoPreview.setText("Tu perfil");
        add(lblFotoPreview);

        JButton btnAddFoto = new BotonRojo("Subir real");
        btnAddFoto.setBounds(130, 200, 140, 25);
        btnAddFoto.addActionListener(e -> seleccionarFotoPerfil());
        add(btnAddFoto);

        int yStart = 240;
        int gap = 50;

        txtNombre = crearCampoTexto("Nombre", yStart);
        txtUsername = crearCampoTexto("Nombre de usuario", yStart + gap);
        txtPassword = crearCampoPassword("Contrasenia", yStart + gap * 2);

        txtEdad = new JTextField();
        txtEdad.setBounds(50, yStart + gap * 3, 140, 40);
        estilizarCampo(txtEdad, "Edad");
        add(txtEdad);

        cbGenero = new JComboBox<>(new String[]{"M", "F"});
        cbGenero.setBounds(210, yStart + gap * 3, 140, 40);
        cbGenero.setUI(new DarkComboBoxUI());
        cbGenero.setBackground(new Color(30, 30, 30));
        cbGenero.setForeground(COLOR_TEXT);
        cbGenero.setFont(FONT_CAOS);
        cbGenero.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER), "Género", 0, 0,
                new Font("Segoe UI", Font.PLAIN, 12), Color.LIGHT_GRAY));
        add(cbGenero);

        btnRegistrar = new BotonRojo("Invocar Cuenta");
        btnRegistrar.setBounds(50, yStart + gap * 4 + 10, 300, 35);
        btnRegistrar.addActionListener(e -> realizarRegistro());
        add(btnRegistrar);

        lblBackLogin = new JLabel("¿Ya tienes cuenta? Entra aquí");
        lblBackLogin.setBounds(50, 600, 300, 30);
        lblBackLogin.setHorizontalAlignment(SwingConstants.CENTER);
        lblBackLogin.setForeground(COLOR_BTN);
        lblBackLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBackLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblBackLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(InstaRegisterUI.this);
                if (window instanceof JFrame frame) {
                    frame.setContentPane(new InstaLoginUI());
                    frame.pack();
                    frame.revalidate();
                    frame.repaint();
                }
            }
        });
        add(lblBackLogin);
    }

    private void estilizarCampo(JTextField txt, String titulo) {
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(COLOR_TEXT);
        txt.setCaretColor(COLOR_TEXT);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                titulo, 0, 0,
                new Font("Segoe UI", Font.PLAIN, 12),
                Color.LIGHT_GRAY));
    }

    private JTextField crearCampoTexto(String titulo, int y) {
        JTextField txt = new JTextField();
        txt.setBounds(50, y, 300, 40);
        estilizarCampo(txt, titulo);
        add(txt);
        return txt;
    }

    private JPasswordField crearCampoPassword(String titulo, int y) {
        JPasswordField txt = new JPasswordField();
        txt.setBounds(50, y, 300, 40);
        estilizarCampo(txt, titulo);
        add(txt);
        return txt;
    }

    private void seleccionarFotoPerfil() {

        final File usersRoot = new File("src\\Z\\Usuarios");

        String osUser = null;
        try {
            if (Logica.ManejoUsuarios.UserLogged.getInstance().getUserLogged() != null) {
                osUser = Logica.ManejoUsuarios.UserLogged.getInstance().getUserLogged().getName();
            }
        } catch (Exception ex) {
            osUser = null;
        }
        if (osUser == null || osUser.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay usuario del sistema identificado.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final File userRoot = new File(usersRoot, osUser);
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
                        if (selCan.startsWith(usersRootCanonical)
                                && !selCan.startsWith(userRootCanonical)) {
                            JOptionPane.showMessageDialog(this,
                                    "Acceso denegado: solo puedes seleccionar imágenes de tu carpeta.",
                                    "Acceso Denegado",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Error verificando la ruta seleccionada.",
                                "Error", JOptionPane.ERROR_MESSAGE);
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
                        if (dirCan.startsWith(usersRootCanonical)
                                && !dirCan.startsWith(userRootCanonical)) {
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

        fileChooser.setDialogTitle("Seleccionar foto de perfil (solo desde tu carpeta)");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (JPG, PNG, JPEG, GIF, BMP, WEBP)",
                "jpg", "png", "jpeg", "gif", "bmp", "webp"));

        int res = fileChooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File sel = fileChooser.getSelectedFile();
        if (sel == null) {
            return;
        }

        try {
            String selCan = sel.getCanonicalPath();
            if (selCan.startsWith(usersRootCanonical)
                    && !selCan.startsWith(userRootCanonical)) {
                JOptionPane.showMessageDialog(this,
                        "Acceso denegado: solo puedes seleccionar imágenes de tu carpeta.",
                        "Acceso Denegado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int dot = sel.getName().lastIndexOf('.');
            String ext = (dot > 0) ? sel.getName().substring(dot + 1) : "jpg";
            File dest = new File(userRoot, "profile." + ext);

            if (!selCan.equals(dest.getCanonicalPath())) {
                java.nio.file.Files.copy(sel.toPath(), dest.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            rutaFotoSeleccionada = dest.getAbsolutePath();

            ImageIcon icon = new ImageIcon(rutaFotoSeleccionada);
            Image img = icon.getImage().getScaledInstance(
                    lblFotoPreview.getWidth(),
                    lblFotoPreview.getHeight(),
                    Image.SCALE_SMOOTH);

            lblFotoPreview.setIcon(new ImageIcon(img));
            lblFotoPreview.setText("");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al procesar la imagen: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String safeCanonical(File f) {
        try {
            return f.getCanonicalPath();
        } catch (IOException ex) {
            return f.getAbsolutePath();
        }
    }

    private void realizarRegistro() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String edadStr = txtEdad.getText().trim();
        char genero = cbGenero.getSelectedItem().toString().charAt(0);

        try {
            if (nombre.isEmpty() || username.isEmpty() || password.isEmpty() || edadStr.isEmpty()) {
                throw new InvalidDataException("Todos los campos deben estar llenos.");
            }

            int edad;
            try {
                edad = Integer.parseInt(edadStr);
            } catch (NumberFormatException e) {
                throw new InvalidDataException("La edad debe ser un número.", e);
            }

            if (edad < 13) {
                throw new InvalidDataException("Debes tener al menos 13 años para unirte.");
            }

            instaManager manager = instaController.getInstance().getInsta();

            if (manager.checkUserExistance(username)) {
                throw new InvalidDataException("Ese alias ya está en uso. Elige otro.");
            }

            manager.addNewUser(nombre, genero, username, password, edad, rutaFotoSeleccionada);

            JOptionPane.showMessageDialog(this,
                    "¡Bienvenido a Instagram!",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            txtNombre.setText("");
            txtUsername.setText("");
            txtPassword.setText("");
            txtEdad.setText("");
            lblFotoPreview.setIcon(null);
            lblFotoPreview.setText("Tu Cara");
            rutaFotoSeleccionada = "";

            lblBackLogin.dispatchEvent(new MouseEvent(lblBackLogin,
                    MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(),
                    0, 0, 0, 1, false));

        } catch (InvalidDataException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Datos Inválidos",
                    JOptionPane.WARNING_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error interno: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class DarkComboBoxUI extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton();
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);

            btn.setIcon(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_TEXT);

                    int size = 8;
                    int tx = x + (getIconWidth() - size) / 2;
                    int ty = y + (getIconHeight() - size) / 2 + 2;
                    g2.fillPolygon(
                            new int[]{tx, tx + size, tx + size / 2},
                            new int[]{ty, ty, ty + size / 2},
                            3
                    );
                    g2.dispose();
                }

                @Override
                public int getIconWidth() {
                    return 20;
                }

                @Override
                public int getIconHeight() {
                    return 20;
                }
            });

            return btn;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(new Color(30, 30, 30));
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroller = super.createScroller();
                    scroller.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
                    list.setBackground(new Color(30, 30, 30));
                    list.setForeground(COLOR_TEXT);
                    return scroller;
                }
            };
        }
    }

    private class BotonRojo extends JButton {

        public BotonRojo(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBackground(COLOR_BTN);
            setForeground(Color.BLACK);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
