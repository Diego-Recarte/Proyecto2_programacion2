package Instagram;

import Logica.Ventanas.InstaWindowLayout;

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
        InstaWindowLayout.install(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        InstaSession session = InstaSession.find(this);
        if (session != null) session.close();
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
        String password = new String(txtPass.getPassword());
        if (username.isEmpty() || password.isEmpty()) return;
        btnLogin.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws IOException {
                return instaController.getInstance().getInsta().authenticate(username, password);
            }
            @Override protected void done() {
                btnLogin.setEnabled(true);
                try {
                    boolean active = get();
                    Window window = SwingUtilities.getWindowAncestor(InstaLoginUI.this);
                    if (window instanceof JFrame frame) {
                        frame.setContentPane(active ? new InstaFeedUI(username) : new InstaProfileEditUI(username));
                        frame.pack(); frame.revalidate(); frame.repaint();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    int choice = JOptionPane.showOptionDialog(InstaLoginUI.this,
                            cause.getMessage(), "No se pudo iniciar sesión", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.ERROR_MESSAGE, null, new String[]{"Reintentar", "Crear cuenta"}, "Reintentar");
                    txtPass.setText("");
                    if (choice == 1 && SwingUtilities.getWindowAncestor(InstaLoginUI.this) instanceof JFrame frame) {
                        frame.setContentPane(new InstaRegisterUI()); frame.pack();
                    } else txtUser.requestFocusInWindow();
                }
            }
        }.execute();
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
