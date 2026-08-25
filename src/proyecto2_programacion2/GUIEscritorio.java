/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

    private Dimension pantalla;

    public GUIEscritorio(CardLayout principal, JPanel cards) {
        ImageIcon iconoFondo = new ImageIcon(
                getClass().getResource("/imagenes/windows/fondoEscritorio.jpg")
        );

        pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        imagenFondo = iconoFondo.getImage().getScaledInstance(
                pantalla.width,
                pantalla.height,
                Image.SCALE_SMOOTH
        );

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(pantalla.width, pantalla.height));

        initEscritorio();
        initBotones(principal, cards);
        initBarra(principal, cards);
        initApagar( principal,  cards) ;

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
    }

    public void initEscritorio() {
        escritorio = new JPanel(null);
        escritorio.setOpaque(false);
        escritorio.setPreferredSize(new Dimension(pantalla.width, pantalla.height - 60));
        add(escritorio, BorderLayout.CENTER);
    }

    public void initBotones(CardLayout principal, JPanel cards) {
        windows = crearBotonEscritorio("/imagenes/windows/iconosApp/windows.png", 50, 50);
        insta = crearBotonEscritorio("/imagenes/windows/iconosApp/insta.png", 50, 150);
        terminal = crearBotonEscritorio("/imagenes/windows/iconosApp/cmd.png", 50, 250);
        reproductor = crearBotonEscritorio("/imagenes/windows/iconosApp/reproductor.png", 50, 350);
        buscador = crearBotonEscritorio("/imagenes/windows/iconosApp/buscador.png", 150, 50);
        visualizador = crearBotonEscritorio("/imagenes/windows/iconosApp/visualizador.png", 250, 250);
        word = crearBotonEscritorio("/imagenes/windows/iconosApp/word.png", 150, 250);

       
        escritorio.add(insta);
        escritorio.add(reproductor);
        escritorio.add(buscador);
        escritorio.add(visualizador);
        escritorio.add(word);

        initWindows(principal, cards);

        windows.addActionListener(e -> {
            panelWindows.setVisible(!panelWindows.isVisible());
        });
    }

    private JButton crearBotonEscritorio(String ruta, int x, int y) {
        JButton boton = new JButton();

        aplicarTamanoBotonYIcono(boton, ruta, 90, 90);

        boton.setBounds(x, y, 90, 90);
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

       
        aplicarTamanoBotonYIcono(boton, ruta, 50, 50);

        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setMargin(new Insets(0, 0, 0, 0));

        return boton;
    }

    private void aplicarTamanoBotonYIcono(JButton boton, String ruta, int ancho, int alto) {
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource(ruta));
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);

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

    public void initBarra(CardLayout principal, JPanel cards) {
        JPanel barra = new JPanel();
        barra.setLayout(new FlowLayout(FlowLayout.LEADING, 12, 6));
        barra.setPreferredSize(new Dimension(pantalla.width, 60));
        barra.setBackground(Color.DARK_GRAY);

        JButton btnWindowsBarra = crearBotonBarra("/imagenes/windows/iconosApp/windows.png");
        JButton btnInstaBarra = crearBotonBarra("/imagenes/windows/iconosApp/insta.png");
        JButton btnTerminalBarra = crearBotonBarra("/imagenes/windows/iconosApp/cmd.png");
        JButton btnReproductorBarra = crearBotonBarra("/imagenes/windows/iconosApp/reproductor.png");
        JButton btnBuscadorBarra = crearBotonBarra("/imagenes/windows/iconosApp/buscador.png");
        JButton btnWordBarra = crearBotonBarra("/imagenes/windows/iconosApp/word.png");

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

    public void initApagar(CardLayout principal, JPanel cards) {
        panelWindows = new JPanel();
        panelWindows.setLayout(new BoxLayout(panelWindows, BoxLayout.Y_AXIS));
        panelWindows.setBackground(Color.WHITE);
        panelWindows.setBounds(900, 660, 120, 80);
        panelWindows.setVisible(false);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setBackground(Color.white);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setHorizontalAlignment(SwingConstants.LEFT);
        
        JButton btnApagar = new JButton("Apagar");
        btnApagar.setBackground(Color.white);
        btnApagar.setBorderPainted(false);
        btnApagar.setHorizontalAlignment(SwingConstants.LEFT);
        
        
        JButton btncrear = new JButton("Crear Usuario");
        btncrear.setBackground(Color.white);
        btncrear.setBorderPainted(false);
        btncrear.setHorizontalAlignment(SwingConstants.LEFT);
        
        
        btncrear.addActionListener(e -> principal.show(cards, "login"));//*************************************
        btnCerrarSesion.addActionListener(e -> principal.show(cards, "login"));
        btnApagar.addActionListener(e -> System.exit(0));
        //***********************************************************************condicional
        panelWindows.add(btnCerrarSesion);
        panelWindows.add(btnApagar);

        escritorio.add(panelWindows);
    }
    
    public void initWindows(CardLayout principal, JPanel cards){
        panelwindowsajustes= new JPanel();
        panelwindowsajustes.setLayout(new BoxLayout(panelwindowsajustes, BoxLayout.X_AXIS));
        panelwindowsajustes.setBackground(new Color(0, 0, 0,  200) );
        panelwindowsajustes.setBounds(550, 740, 500, 50);
        panelwindowsajustes.setPreferredSize(new Dimension(550, 740));
        panelwindowsajustes.setVisible(false);
        
        
        JButton Usuario = new JButton("Usuario"); //******* nombre del usuario que este dentro del escritorio
        Usuario.setOpaque(false);
        Usuario.setForeground(Color.white);
        Usuario.setHorizontalAlignment(SwingConstants.CENTER);
        Usuario.setFont(new Font("Arial", Font.BOLD, 20));
        Usuario.setOpaque(false);
        Usuario.setContentAreaFilled(false);
        Usuario.setBorderPainted(false);
        Usuario.setFocusPainted(false);
        
        
        JButton inicio = new JButton("inicio");
        inicio.setForeground(Color.white);
        inicio.setBorderPainted(false);
        inicio.setContentAreaFilled(false);
        inicio.setFont(new Font("Arial", Font.BOLD, 20));
        inicio.setHorizontalAlignment(SwingConstants.CENTER);
        inicio.setOpaque(false);
        inicio.setContentAreaFilled(false);
        inicio.setBorderPainted(false);
        inicio.setFocusPainted(false);
        inicio.addActionListener(ev->{
        panelWindows.setVisible(!panelWindows.isVisible());
            
            
        });
        
        
        
        
        panelwindowsajustes.add(Usuario);
        panelwindowsajustes.add(Box.createHorizontalStrut(250));
        panelwindowsajustes.add(inicio);
        escritorio.add(panelwindowsajustes);
        
        
    }
}