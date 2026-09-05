package Instagram;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.*;


public class InstaLoginUI extends JPanel {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JLabel lblRegister;

    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_BTN = new Color(255, 69, 0);
    private final Color COLOR_BORDER = new Color(100, 100, 100);
    private final Color COLOR_TEXT = Color.WHITE;

    private final int ANCHO = 400;
    private final int ALTO = 650;

    public InstaLoginUI() {
        setLayout(null);
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(ANCHO, ALTO));

        initComponentes();
    }

    private void initComponentes() {
        int startY = 140;

        JLabel lblLogo = new JLabel("Instagram");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblLogo.setForeground(COLOR_TEXT);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(40, startY, 320, 60);
        add(lblLogo);

        JLabel lblUser = new JLabel("Usuario");
        lblUser.setBounds(50, startY + 70, 100, 20);
        lblUser.setForeground(Color.LIGHT_GRAY);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(lblUser);

        txtUser = new JTextField();
        txtUser.setBounds(50, startY + 90, 300, 40);
        estilizarCampo(txtUser);
        add(txtUser);

        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setBounds(50, startY + 140, 100, 20);
        lblPass.setForeground(Color.LIGHT_GRAY);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setBounds(50, startY + 160, 300, 40);
        estilizarCampo(txtPass);
        add(txtPass);

        btnLogin = new JButton("Entrar");
        btnLogin.setBounds(50, startY + 220, 300, 40);
        btnLogin.setBackground(COLOR_BTN);
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(e -> realizarLogin());
        add(btnLogin);

        JSeparator sep = new JSeparator();
        sep.setBounds(50, startY + 280, 300, 10);
        sep.setForeground(COLOR_BORDER);
        add(sep);

        lblRegister = new JLabel("¿No tienes cuenta? Crea una");
        lblRegister.setBounds(40, startY + 300, 320, 30);
        lblRegister.setHorizontalAlignment(SwingConstants.CENTER);
        lblRegister.setForeground(COLOR_BTN);
        lblRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(InstaLoginUI.this);
                if (window instanceof JFrame) {
                    JFrame frame = (JFrame) window;
                    frame.setAlwaysOnTop(true);
                    frame.setContentPane(new InstaRegisterUI());
                    frame.pack();
                    frame.revalidate();
                    frame.repaint();
                }
            }
        });
        add(lblRegister);
    }

    private void estilizarCampo(JTextField txt) {
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(COLOR_TEXT);
        txt.setCaretColor(COLOR_TEXT);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 5)));
    }

    private void realizarLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "¡Llena todo!", "Error Fatal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            instaManager manager = instaController.getInstance().getInsta();

            if (manager == null) {
                JOptionPane.showMessageDialog(this, "Error crítico: El sistema no vive.", "Muerte", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String storedPass = manager.getPassword(username);

            if (storedPass == null) {
                JOptionPane.showMessageDialog(this,
                        "Ese usuario no existe en este plano existencial.",
                        "Usuario no encontrado",
                        JOptionPane.ERROR_MESSAGE);
                txtUser.requestFocusInWindow();
                return;
            }

            if (!storedPass.equals(password)) {
                JOptionPane.showMessageDialog(this,
                        "Contraseña incorrecta.",
                        "Login Fallido",
                        JOptionPane.ERROR_MESSAGE);
                txtPass.setText("");
                txtPass.requestFocusInWindow();
                return;
            }

            if (!manager.getStatusUser(username)) {
                int reactivate = JOptionPane.showConfirmDialog(this,
                        "Esta cuenta está desactivada. ¿Deseas reactivarla e iniciar sesión?",
                        "Cuenta desactivada", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (reactivate != JOptionPane.YES_OPTION || !manager.activateUser(username)) {
                    return;
                }
            }

            try {
                manager.setLoggedUser(username);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Advertencia: no se pudo fijar sesión: " + ex.getMessage());
            }

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                frame.setAlwaysOnTop(true);
                // El feed es la pantalla principal después de iniciar sesión.
                frame.setContentPane(new InstaFeedUI(username));
                frame.pack();
                frame.revalidate();
                frame.repaint();
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        instaManager manager = new instaManager();
        instaController.getInstance().setInsta(manager);

        JFrame frame = new JFrame("Instagram Login Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setContentPane(new InstaLoginUI());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
