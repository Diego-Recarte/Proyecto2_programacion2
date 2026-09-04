/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;



public class GUILogin extends JPanel {

    private JPasswordField contra;
    private JTextField user;
    private Timer tempo;
    private JLabel label;
    private Image imagenFondo;
    private GUIPantallaPrincipal padre;

    public GUILogin(GUIPantallaPrincipal padre, CardLayout principal, JPanel cards) {
        this.padre = padre;

        ImageIcon icono = new ImageIcon(getClass().getResource("/datos/windows/Z/imagenes/windows/fondoLogin.jpg"));
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        imagenFondo = icono.getImage().getScaledInstance(
                pantalla.width,
                pantalla.height,
                Image.SCALE_SMOOTH
        );

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(pantalla.width, pantalla.height));

        inicializarBotones(principal, cards);
        inicializarBotonExit();
        inicializarTimer();

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
    }

    public void inicializarTimer() {
        tempo = new Timer(2100, ev -> {
            label.setVisible(false);
            repaint();
            tempo.stop();
        });
    }

    public void inicializarBotones(CardLayout principal, JPanel cards) {
        JPanel panelLogin = new JPanel();
        panelLogin.setLayout(new GridLayout(7, 1, 10, 10));
        panelLogin.setBackground(Color.WHITE);
        panelLogin.setPreferredSize(new Dimension(350, 280));
        panelLogin.setOpaque(false);

        JLabel lblUsuario = new JLabel("Usuario");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 16));
        panelLogin.add(lblUsuario);

        user = new JTextField();
        user.setFont(new Font("Arial", Font.PLAIN, 15));
        panelLogin.add(user);

        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 16));
        panelLogin.add(lblPassword);

        contra = new JPasswordField();
        contra.setFont(new Font("Arial", Font.PLAIN, 15));
        panelLogin.add(contra);

        JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");
        chkMostrar.setBackground(Color.WHITE);
        chkMostrar.setFocusable(false);
        chkMostrar.setOpaque(false);

        chkMostrar.addActionListener(e -> {
            if (chkMostrar.isSelected()) {
                contra.setEchoChar((char) 0);
            } else {
                contra.setEchoChar('•');
            }
        });

        panelLogin.add(chkMostrar);

        label = new JLabel("Texto");
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(Color.RED);
        label.setOpaque(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(250, 90));
        label.setVisible(false);
        panelLogin.add(label);

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("Arial", Font.BOLD, 15));
        btnIngresar.setBackground(Color.black);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusable(false);

        btnIngresar.addActionListener(e -> {
            ArchivoUsuarioWin archivo = new ArchivoUsuarioWin();
            int resultado = archivo.login(user.getText().trim(), contra);

            switch (resultado) {
                case 1:
                    user.setText("");
                    contra.setText("");
                   
                    padre.mostrarEscritorio();
                    break;

                case 2:
                    label.setText("contraseña incorrecta");
                    label.setVisible(true);
                    tempo.start();
                    break;

                case 3:
                    label.setText("usuario incorrecto");
                    label.setVisible(true);
                    tempo.start();
                    break;
            }
        });

        panelLogin.add(btnIngresar);

        GridBagConstraints gbcLogin = new GridBagConstraints();
        gbcLogin.gridx = 0;
        gbcLogin.gridy = 0;
        gbcLogin.weightx = 1.0;
        gbcLogin.weighty = 1.0;
        gbcLogin.anchor = GridBagConstraints.CENTER;

        add(panelLogin, gbcLogin);
    }

    public void inicializarBotonExit() {
        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Arial", Font.BOLD, 14));
        btnExit.setOpaque(false);
        btnExit.setFocusable(false);

        btnExit.addActionListener(e -> System.exit(0));

        GridBagConstraints gbcExit = new GridBagConstraints();
        gbcExit.gridx = 0;
        gbcExit.gridy = 1;
        gbcExit.weightx = 1.0;
        gbcExit.weighty = 1.0;
        gbcExit.anchor = GridBagConstraints.SOUTHWEST;
        gbcExit.insets = new Insets(0, 20, 20, 0);

        add(btnExit, gbcExit);
    }
}