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
import java.io.File;
import java.util.Arrays;

public class GUIVisualizadorMenu extends JPanel {
    

    private File carpeta;
    private File[] archivos;

    private JLabel labeltitulo;
    private JPanel panelGrid;

    private GUIVisualizadorPantalla padre;
    private CardLayout principal;
    private JPanel cards;
    private GUIVisualizadorPrincipal principalV;

    public GUIVisualizadorMenu(GUIVisualizadorPantalla padre, CardLayout principal, JPanel cards, File carpeta, File[] archivos, GUIVisualizadorPrincipal principalV) {
        this.padre = padre;
        this.principal = principal;
        this.cards = cards;
        this.carpeta = carpeta;
        this.archivos = archivos != null ? archivos : new File[0];
        this.principalV = principalV;

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(800, 400));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.BLACK);

        initComponentes(principal, cards);
    }

    private void initComponentes(CardLayout principal, JPanel cards) {
        initBarraNorte();
        initPanelCentral();
    }

    private void initBarraNorte() {
    JPanel panel = new JPanel();

    panel.setLayout(new BorderLayout(2, 2));
    panel.setPreferredSize(new Dimension(800, 50));
    panel.setMaximumSize(new Dimension(800, 50));
    panel.setMinimumSize(new Dimension(800, 50));
    panel.setBackground(Color.GRAY);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JButton botonVolver = new JButton("Volver al visualizador");
    botonVolver.setFont(new Font("Arial", Font.BOLD, 10));
    botonVolver.setForeground(Color.WHITE);
    botonVolver.setBackground(Color.BLACK);
    botonVolver.setFocusPainted(false);
    botonVolver.addActionListener(e -> padre.mostrarCard("principal"));

    labeltitulo = new JLabel("Carpeta");
    labeltitulo.setFont(new Font("Arial", Font.BOLD, 15));
    labeltitulo.setForeground(Color.WHITE);
    labeltitulo.setOpaque(false);
    labeltitulo.setHorizontalAlignment(SwingConstants.CENTER);
    labeltitulo.setPreferredSize(new Dimension(300, 45));

    panel.add(botonVolver, BorderLayout.WEST);
    panel.add(labeltitulo, BorderLayout.CENTER);

    add(panel, BorderLayout.NORTH);
    }

    private void initPanelCentral() {
        panelGrid = new JPanel();
        panelGrid.setBackground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(panelGrid);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(scroll, BorderLayout.CENTER);

        reconstruirGrid();
    }

    public void actualizarContenido(File carpeta, File[] archivos) {
    this.carpeta = carpeta;
    this.archivos = archivos != null ? archivos : new File[0];
    reconstruirGrid();
    }

    private void reconstruirGrid() {
    panelGrid.removeAll();

    if (carpeta != null) {
    labeltitulo.setText(carpeta.getName());
    } else {
    labeltitulo.setText("Sin carpeta");
    }

    if (archivos == null) {
    archivos = new File[0];
    }

    archivos = filtrarImagenes(archivos);

    if (archivos.length == 0) {
        panelGrid.setLayout(new BorderLayout());
        JLabel vacio = new JLabel("No hay imagenes en esta carpeta.");
        vacio.setForeground(Color.WHITE);
        vacio.setHorizontalAlignment(SwingConstants.CENTER);
        panelGrid.add(vacio, BorderLayout.CENTER);
    } else {

        int filas = (int) Math.ceil((double) archivos.length / 2);

        panelGrid.setLayout(new GridLayout(filas, 2, 10, 10));

        for (File archivoImagen : archivos) {
            JButton boton = crearBotonImagen(archivoImagen);
            panelGrid.add(boton);
        }
    }

    panelGrid.revalidate();
    panelGrid.repaint();
    }

    private File[] filtrarImagenes(File[] archivosEntrada) {
        return Arrays.stream(archivosEntrada)
        .filter(a -> a != null && a.isFile())
        .filter(a -> {
        String n = a.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png");
        }).sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toArray(File[]::new);
    }

    private JButton crearBotonImagen(File archivoImagen) {
        JButton boton = new JButton();
        boton.putClientProperty("file", archivoImagen);
        boton.setBackground(Color.BLACK);
        boton.setFocusPainted(false);

        ImageIcon iconoOriginal = new ImageIcon(archivoImagen.getAbsolutePath());
        Image miniatura = iconoOriginal.getImage().getScaledInstance(300, 220, Image.SCALE_SMOOTH);
        boton.setIcon(new ImageIcon(miniatura));
        boton.setText(archivoImagen.getName());
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        boton.setForeground(Color.WHITE);

        boton.addActionListener(ev -> {
            File file = (File) boton.getClientProperty("file");
                if (file != null) {
                principalV.abrirImagen(file);
                }
        });

        return boton;
        }
    }
