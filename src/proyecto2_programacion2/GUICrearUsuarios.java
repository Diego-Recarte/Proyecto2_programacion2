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
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;

public class GUICrearUsuarios extends JPanel {

    private JPasswordField contra;
    private JTextField user;
    private JLabel label;
    private Timer tempo;
    private Image imagenFondo;
    private String nombre;
    private GUIPantallaPrincipal padre;

    public GUICrearUsuarios(GUIPantallaPrincipal padre, CardLayout principal, JPanel cards) {
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

        inicializarTitulo();
        Inicializarbotones(principal, cards);
        inicializarBotonExit();
        inicializarTimer();

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
    }

    public void inicializarTitulo() {
        JLabel titulo = new JLabel("Crear Cuenta");
        titulo.setFont(new Font("Arial", Font.BOLD, 30));
        titulo.setForeground(Color.WHITE);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbcTitulo = new GridBagConstraints();
        gbcTitulo.gridx = 0;
        gbcTitulo.gridy = 0;
        gbcTitulo.weightx = 1.0;
        gbcTitulo.insets = new Insets(100, 0, 20, 0);
        gbcTitulo.anchor = GridBagConstraints.CENTER;

        add(titulo, gbcTitulo);
    }

    public void inicializarTimer() {
        tempo = new Timer(2100, ev -> {
            label.setVisible(false);
            repaint();
            tempo.stop();
        });
    }

    public void Inicializarbotones(CardLayout principal, JPanel cards) {
        JPanel panelLogin = new JPanel();
        panelLogin.setLayout(new GridLayout(8, 1, 8, 8));
        panelLogin.setBackground(Color.WHITE);
        panelLogin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelLogin.setPreferredSize(new Dimension(350, 420));
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
        chkMostrar.setOpaque(false);
        chkMostrar.setBackground(Color.WHITE);
        chkMostrar.setFocusable(false);

        chkMostrar.addActionListener(e -> {
            if (chkMostrar.isSelected()) {
                contra.setEchoChar((char) 0);
            } else {
                contra.setEchoChar('•');
            }
        });

        panelLogin.add(chkMostrar);

        JPanel panelradio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelradio.setOpaque(false);
        panelradio.setPreferredSize(new Dimension(100, 20));

        JPanel paneltipo = new JPanel(new GridLayout(1, 1));
        paneltipo.setOpaque(false);
        paneltipo.setPreferredSize(new Dimension(330, 40));
        paneltipo.add(panelradio);

        panelLogin.add(paneltipo);

        label = new JLabel("Texto");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.RED);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVisible(false);
        panelLogin.add(label);

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("Arial", Font.BOLD, 15));
        btnIngresar.setBackground(Color.BLACK);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusable(false);

        btnIngresar.addActionListener(e -> {
            nombre = user.getText().trim();

            int compU = ComprobarU();
            int compC = ComprobarC();

            if (compU == 2 && compC == 2) {
                Seguir(principal, cards);
            } else {
                if (compU == 0 && compC == 0) {
                    label.setText("Falta User y Contra");
                }
                label.setVisible(true);
                repaint();
                tempo.start();
            }
        });

        panelLogin.add(btnIngresar);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        add(panelLogin, gbc);
    }

    private int ComprobarC() {
        char[] caracteres = contra.getPassword();
        ArrayList<Character> passwordIngresada = new ArrayList<>();

        for (char caracter : caracteres) {
            passwordIngresada.add(caracter);
        }

        boolean tieneNumero = false;
        boolean tieneLetra = false;
        boolean tieneEspecial = false;
        boolean tieneMayuscula = false;

        if (caracteres.length == 0) {
            label.setText("Ingrese contraseña");
            return 0;
        } else if (caracteres.length < 8) {
            label.setText("Debe tener minimo 8 caracteres");
            return 1;
        } else {
            for (Character caracter : passwordIngresada) {
                if (Character.isDigit(caracter)) {
                    tieneNumero = true;
                }

                if (Character.isLetter(caracter)) {
                    tieneLetra = true;
                }

                if (!Character.isLetterOrDigit(caracter)) {
                    tieneEspecial = true;
                }

                if (Character.isUpperCase(caracter)) {
                    tieneMayuscula = true;
                }
            }

            if (!tieneNumero) {
                label.setText("Debe tener al menos un numero");
                label.setVisible(true);
                tempo.start();
                return 1;
            } else if (!tieneLetra) {
                label.setText("Debe tener letras y no solo numeros");
                label.setVisible(true);
                tempo.start();
                return 1;
            } else if (!tieneEspecial) {
                label.setText("Debe tener al menos un caracter especial");
                label.setVisible(true);
                tempo.start();
                return 1;
            } else if (!tieneMayuscula) {
                label.setText("Debe tener al menos una letra mayuscula");
                label.setVisible(true);
                tempo.start();
                return 1;
            }
        }

        return 2;
    }

    private int ComprobarU() {
        if (nombre.length() == 0) {
            label.setText("Ingrese usuario");
            return 0;
        } else {
            return 2;
        }
    }

    public void inicializarBotonExit() {
        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Arial", Font.BOLD, 14));
        btnExit.setOpaque(false);
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusable(false);

        btnExit.addActionListener(e -> System.exit(0));

        GridBagConstraints gbcExit = new GridBagConstraints();
        gbcExit.gridx = 0;
        gbcExit.gridy = 2;
        gbcExit.weightx = 1.0;
        gbcExit.weighty = 1.0;
        gbcExit.anchor = GridBagConstraints.SOUTHWEST;
        gbcExit.insets = new Insets(0, 20, 20, 0);

        add(btnExit, gbcExit);
    }

    public void Seguir(CardLayout principal, JPanel cards) {
        ArchivoUsuarioWin archivo = new ArchivoUsuarioWin();

        try {
            if (!archivo.UsuarioExiste(nombre)) {
                if (!archivo.existeAdmin()) {
                    UsuarioWin win = new UsuarioWin(nombre, contra.getPassword(), true);
                    archivo.agregarUsuario(win);
                    archivo.login(nombre, contra);
                    padre.mostrarEscritorio();
                } else {
                    UsuarioWin win = new UsuarioWin(nombre, contra.getPassword(), false);
                    archivo.agregarUsuario(win);
                    archivo.login(nombre, contra);
                    padre.mostrarEscritorio();
                }
            } else {
                label.setText("el usuario ya existe");
                label.setVisible(true);
                tempo.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}