/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import Instagram.InstaLoginUI;
import Instagram.instaController;
import Instagram.instaManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;

public class GUIEscritorio extends JPanel {
    private Image imagenFondo;
    private JPanel escritorio;
    private JPanel panelWindows;
    private JPanel panelwindowsajustes;

    private JButton windows;
    private JButton insta;
    private JButton terminal;
    private JButton reproductor;
    private JButton buscador;
    private JButton visualizador;
    private JButton word;
    private JButton btnUsuario;
    private JButton btncrear;
    private int siguienteOffsetInstagram;

    private Dimension pantalla;

    public GUIEscritorio(GUIPantallaPrincipal padre, CardLayout principal, JPanel cards) {
        ImageIcon iconoFondo = new ImageIcon( getClass().getResource("/datos/windows/Z/imagenes/windows/fondoEscritorio.jpg"));

        pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        imagenFondo = iconoFondo.getImage().getScaledInstance(
                pantalla.width,
                pantalla.height,
                Image.SCALE_SMOOTH
        );

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(pantalla.width, pantalla.height));

        initEscritorio();
        initBotones(padre, principal, cards);
        initBarra(padre, principal, cards);
        initApagar(principal, cards);

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
    }

    public void actualizarUsuarioActivo() {
        if (btnUsuario != null) {
            btnUsuario.setText(usuarioWinActivo.nombre);
            if (usuarioWinActivo.isAdmin){
                panelWindows.add(btncrear);
                usuarioWinActivo.raiz= new File("src/datos/windows/Z/infoUsuarios");
            }else{
               panelWindows.remove(btncrear); 
               usuarioWinActivo.raiz= new File("src/datos/windows/Z/infoUsuarios/"+usuarioWinActivo.nombre);
            }
            
            
            
            
            btnUsuario.revalidate();
            btnUsuario.repaint();
        }
    }

    public void initEscritorio() {
        escritorio = new JPanel(null);
        escritorio.setOpaque(false);
        escritorio.setPreferredSize(new Dimension(pantalla.width, pantalla.height - 60));
        add(escritorio, BorderLayout.CENTER);
    }

    public void initBotones(GUIPantallaPrincipal padre, CardLayout principal, JPanel cards) {
        windows = crearBotonEscritorio("/datos/windows/Z/imagenes/windows/iconosApp/windows.png", 50, 50, "");
        insta = crearBotonEscritorio("/datos/windows/Z/imagenes/windows/iconosApp/insta.png", 50, 150, "Insta+");
        terminal = crearBotonEscritorio("/datos/windows/Z/imagenes/windows/iconosApp/cmd.png", 50, 250, "CMD");
        reproductor = crearBotonEscritorio("/datos/windows/Z/imagenes/windows/iconosApp/reproductor.png", 50, 350, "reproductor");
        buscador = crearBotonEscritorio("/datos/windows/Z/imagenes/windows/iconosApp/buscador.png", 150, 50, "buscador");
        visualizador = crearBotonEscritorio("/datos/windows/Z/imagenes/windows/iconosApp/visualizador.png", 250, 250, "Visualizador");
        visualizador.addActionListener(ev->{
        new GUIVisualizadorPantalla (padre, null);
        });
        
        word = crearBotonEscritorio("/datos/windows/Z/imagenes/windows/iconosApp/word.png", 150, 250, "Word");
         buscador.addActionListener(ev->{
        new Buscador(padre);
        });

        word.addActionListener(ev -> new GUIpantallaWord(padre, null,false));

        insta.addActionListener(ev -> abrirInstagram());
        reproductor.addActionListener(ev -> abrirReproductor(padre));


        escritorio.add(insta);
        escritorio.add(reproductor);
        escritorio.add(buscador);
        escritorio.add(visualizador);
        escritorio.add(word);

        initWindows(principal, cards);

        windows.addActionListener(e -> panelWindows.setVisible(!panelWindows.isVisible()));
    }

    private JButton crearBotonEscritorio(String ruta, int x, int y, String nombre) {
        JButton boton = new JButton();

        aplicarTamanoBotonYIcono(boton, ruta, 90, 90, nombre);

        boton.setBounds(x, y, 90, 120);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setHorizontalAlignment(SwingConstants.CENTER);

        hacerArrastrable(boton);
        return boton;
    }

    private JButton crearBotonBarra(String ruta) {
        JButton boton = new JButton();

        aplicarTamanoBotonYIcono(boton, ruta, 50, 50, null);

        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setMargin(new Insets(0, 0, 0, 0));

        return boton;
    }

    private void aplicarTamanoBotonYIcono(JButton boton, String ruta, int ancho, int alto, String nombre) {
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource(ruta));
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);

        if (nombre != null) {
            boton.setText(nombre);
            boton.setFont(new Font("Arial", Font.BOLD, 8));
            boton.setForeground(Color.white);
            boton.setHorizontalTextPosition(SwingConstants.CENTER);
            boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        }

        boton.setOpaque(false);
        boton.setIcon(iconoEscalado);

        Dimension size = new Dimension(ancho, alto);
        boton.setPreferredSize(size);
        boton.setMinimumSize(size);
        boton.setMaximumSize(size);
        boton.setSize(size);
    }

    private void hacerArrastrable(JButton boton) {
        final Point[] clickOffset = {null};

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clickOffset[0] = e.getPoint();
            }
        });

        boton.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (clickOffset[0] == null) {
                    return;
                }

                int nuevoX = boton.getX() + e.getX() - clickOffset[0].x;
                int nuevoY = boton.getY() + e.getY() - clickOffset[0].y;

                nuevoX = Math.max(0, Math.min(nuevoX, escritorio.getWidth() - boton.getWidth()));
                nuevoY = Math.max(0, Math.min(nuevoY, escritorio.getHeight() - boton.getHeight()));

                boton.setLocation(nuevoX, nuevoY);
            }
        });
    }

    public void initBarra(GUIPantallaPrincipal padre, CardLayout principal, JPanel cards) {
        JPanel barra = new JPanel();
        barra.setLayout(new FlowLayout(FlowLayout.LEADING, 12, 6));
        barra.setPreferredSize(new Dimension(pantalla.width, 60));
        barra.setBackground(Color.DARK_GRAY);

        JButton btnWindowsBarra = crearBotonBarra("/datos/windows/Z/imagenes/windows/iconosApp/windows.png");
        JButton btnInstaBarra = crearBotonBarra("/datos/windows/Z/imagenes/windows/iconosApp/insta.png");
        JButton btnTerminalBarra = crearBotonBarra("/datos/windows/Z/imagenes/windows/iconosApp/cmd.png");
        JButton btnReproductorBarra = crearBotonBarra("/datos/windows/Z/imagenes/windows/iconosApp/reproductor.png");
        
        JButton btnBuscadorBarra = crearBotonBarra("/datos/windows/Z/imagenes/windows/iconosApp/buscador.png");
        btnBuscadorBarra.addActionListener(ev->{
        new Buscador(padre);
        });
        
        JButton btnWordBarra = crearBotonBarra("/datos/windows/Z/imagenes/windows/iconosApp/word.png");

        btnTerminalBarra.addActionListener(ev -> new PanelCMD(padre));

        btnWordBarra.addActionListener(ev -> new GUIpantallaWord(padre, null,false));

        btnInstaBarra.addActionListener(ev -> abrirInstagram());
        btnReproductorBarra.addActionListener(ev -> abrirReproductor(padre));


        btnWindowsBarra.addActionListener(e -> {
            panelwindowsajustes.setVisible(!panelwindowsajustes.isVisible());
            panelWindows.setVisible(false);
        });

        barra.add(Box.createHorizontalStrut(550));
        barra.add(btnWindowsBarra);
        barra.add(Box.createHorizontalStrut(30));
        barra.add(btnBuscadorBarra);
        barra.add(btnTerminalBarra);
        barra.add(btnReproductorBarra);
        barra.add(btnWordBarra);
        barra.add(btnInstaBarra);

        add(barra, BorderLayout.SOUTH);
    }

    private void abrirInstagram() {
        if (instaController.getInstance().getInsta() == null) {
            instaController.getInstance().setInsta(new instaManager());
        }

        JFrame nuevaVentana = new JFrame("Instagram");
        nuevaVentana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nuevaVentana.setResizable(false);
        nuevaVentana.setAlwaysOnTop(true);
        nuevaVentana.setContentPane(new InstaLoginUI());
        nuevaVentana.pack();
        nuevaVentana.setLocationRelativeTo(null);

        Point posicionCentrada = nuevaVentana.getLocation();
        int offset = siguienteOffsetInstagram;
        int x = Math.min(posicionCentrada.x + offset,
                Math.max(0, pantalla.width - nuevaVentana.getWidth()));
        int y = Math.min(posicionCentrada.y + offset,
                Math.max(0, pantalla.height - nuevaVentana.getHeight()));
        nuevaVentana.setLocation(x, y);
        siguienteOffsetInstagram = (siguienteOffsetInstagram + 30) % 180;

        nuevaVentana.setVisible(true);
        nuevaVentana.toFront();
    }

    private void abrirReproductor(GUIPantallaPrincipal padre) {
        new GUIReproductor(padre);
    }

    public void initApagar(CardLayout principal, JPanel cards) {
        panelWindows = new JPanel();
        panelWindows.setLayout(new BoxLayout(panelWindows, BoxLayout.Y_AXIS));
        panelWindows.setBackground(Color.WHITE);
        panelWindows.setBounds(900, 660, 120, 110);
        panelWindows.setVisible(false);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setBackground(Color.white);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setHorizontalAlignment(SwingConstants.LEFT);

        JButton btnApagar = new JButton("Apagar");
        btnApagar.setBackground(Color.white);
        btnApagar.setBorderPainted(false);
        btnApagar.setHorizontalAlignment(SwingConstants.LEFT);

         btncrear = new JButton("Crear Usuario");
        btncrear.setBackground(Color.white);
        btncrear.setBorderPainted(false);
        btncrear.setHorizontalAlignment(SwingConstants.LEFT);

        btncrear.addActionListener(e -> {
            panelWindows.setVisible(false);
            panelwindowsajustes.setVisible(false);
            principal.show(cards, "crear");
        });

        btnCerrarSesion.addActionListener(e -> {
            panelWindows.setVisible(false);
            panelwindowsajustes.setVisible(false);
            principal.show(cards, "login");
        });

        btnApagar.addActionListener(e -> System.exit(0));

        panelWindows.add(btnCerrarSesion);
        
             
        
       
        panelWindows.add(btnApagar);

        escritorio.add(panelWindows);
    }

    public void initWindows(CardLayout principal, JPanel cards) {
        panelwindowsajustes = new JPanel();
        panelwindowsajustes.setLayout(new BoxLayout(panelwindowsajustes, BoxLayout.X_AXIS));
        panelwindowsajustes.setBackground(new Color(0, 0, 0, 200));
        panelwindowsajustes.setBounds(550, 740, 500, 50);
        panelwindowsajustes.setPreferredSize(new Dimension(550, 740));
        panelwindowsajustes.setVisible(false);

        btnUsuario = new JButton("");
        btnUsuario.setForeground(Color.white);
        btnUsuario.setHorizontalAlignment(SwingConstants.CENTER);
        btnUsuario.setFont(new Font("Arial", Font.BOLD, 20));
        btnUsuario.setOpaque(false);
        btnUsuario.setContentAreaFilled(false);
        btnUsuario.setBorderPainted(false);
        btnUsuario.setFocusPainted(false);

        JButton inicio = new JButton("inicio");
        inicio.setForeground(Color.white);
        inicio.setFont(new Font("Arial", Font.BOLD, 20));
        inicio.setHorizontalAlignment(SwingConstants.CENTER);
        inicio.setOpaque(false);
        inicio.setContentAreaFilled(false);
        inicio.setBorderPainted(false);
        inicio.setFocusPainted(false);

        inicio.addActionListener(ev -> panelWindows.setVisible(!panelWindows.isVisible()));

        panelwindowsajustes.add(btnUsuario);
        panelwindowsajustes.add(Box.createHorizontalStrut(250));
        panelwindowsajustes.add(inicio);
        escritorio.add(panelwindowsajustes);
    }
}
