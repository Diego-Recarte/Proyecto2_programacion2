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

public class GUIVisualizadorPrincipal extends JPanel {

    private File archivo;
    private ImageIcon imagen;
    private File carpeta;
    private int index = 0;
    private File[] archivos;
    private JLabel labelimagen;
    private JButton botonanterior;
    private JButton botonpost;
    private JLabel labeltitulo;

    private GUIVisualizadorPantalla padre;
    private CardLayout principal;
    private JPanel cards;

    public GUIVisualizadorPrincipal(GUIVisualizadorPantalla padre, CardLayout principal, JPanel cards, File imagen) {
        this.padre = padre;
        this.principal = principal;
        this.cards = cards;
        this.archivo = imagen;

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(800, 400));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.BLACK);

        initComponentes(principal, cards);
        initImagen();
        cambiarImagen();
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

        JButton boton = new JButton("Buscar otra carpeta");

        boton.setFont(new Font("Arial", Font.BOLD, 9));
        boton.setPreferredSize(new Dimension(150, 45));
        boton.setMaximumSize(new Dimension(150, 45));
        boton.setMinimumSize(new Dimension(150, 45));
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setHorizontalAlignment(SwingConstants.CENTER);

        boton.addActionListener(e -> buscarOtraCarpeta());

        JButton boton2 = new JButton("Buscar esta carpeta");

        boton2.setFont(new Font("Arial", Font.BOLD, 9));
        boton2.setPreferredSize(new Dimension(150, 45));
        boton2.setMaximumSize(new Dimension(150, 45));
        boton2.setMinimumSize(new Dimension(150, 45));
        boton2.setForeground(Color.WHITE);
        boton2.setBackground(Color.BLACK);
        boton2.setFocusPainted(false);
        boton2.setBorderPainted(false);
        boton2.setContentAreaFilled(false);
        boton2.setHorizontalAlignment(SwingConstants.CENTER);

        boton2.addActionListener(e -> abrirMenuCarpetaActual());

        labeltitulo = new JLabel(" ");

        labeltitulo.setFont(new Font("Arial", Font.BOLD, 10));
        labeltitulo.setForeground(Color.WHITE);
        labeltitulo.setOpaque(false);
        labeltitulo.setHorizontalAlignment(SwingConstants.CENTER);
        labeltitulo.setPreferredSize(new Dimension(300, 45));

        panel.add(boton, BorderLayout.EAST);
        panel.add(labeltitulo, BorderLayout.CENTER);
        panel.add(boton2, BorderLayout.WEST);

        add(panel, BorderLayout.NORTH);
    }

    private void initPanelCentral() {
        JPanel panelImagen = new JPanel();

        panelImagen.setLayout(new BorderLayout(10, 10));
        panelImagen.setPreferredSize(new Dimension(800, 400));
        panelImagen.setMaximumSize(new Dimension(800, 400));
        panelImagen.setMinimumSize(new Dimension(800, 400));
        panelImagen.setOpaque(true);
        panelImagen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelImagen.setBackground(Color.black);

        labelimagen = new JLabel("");

        labelimagen.setFont(new Font("Arial", Font.BOLD, 10));
        labelimagen.setForeground(Color.WHITE);
        labelimagen.setBackground(Color.WHITE);
        labelimagen.setHorizontalAlignment(SwingConstants.CENTER);
        labelimagen.setPreferredSize(new Dimension(500, 350));
        labelimagen.setMinimumSize(new Dimension(500, 350));
        labelimagen.setMaximumSize(new Dimension(500, 350));

        botonanterior = new JButton("<");

        botonanterior.setFont(new Font("Arial", Font.BOLD, 20));
        botonanterior.setPreferredSize(new Dimension(50, 50));
        botonanterior.setMaximumSize(new Dimension(50, 50));
        botonanterior.setMinimumSize(new Dimension(50, 50));
        botonanterior.setForeground(Color.WHITE);
        botonanterior.setBackground(Color.WHITE);
        botonanterior.setFocusPainted(false);
        botonanterior.setBorderPainted(false);
        botonanterior.setContentAreaFilled(false);
        botonanterior.setHorizontalAlignment(SwingConstants.CENTER);

        botonanterior.addActionListener(e -> {
            if (archivos == null || archivos.length == 0) {
                return;
            }

            if (index > 0) {
                index--;
                cambiarImagen();
            }
        });

        botonpost = new JButton(">");

        botonpost.setFont(new Font("Arial", Font.BOLD, 20));
        botonpost.setPreferredSize(new Dimension(50, 50));
        botonpost.setMaximumSize(new Dimension(50, 50));
        botonpost.setMinimumSize(new Dimension(50, 50));
        botonpost.setForeground(Color.WHITE);
        botonpost.setBackground(Color.WHITE);
        botonpost.setFocusPainted(false);
        botonpost.setBorderPainted(false);
        botonpost.setContentAreaFilled(false);
        botonpost.setHorizontalAlignment(SwingConstants.CENTER);

        botonpost.addActionListener(e -> {
            if (archivos == null || archivos.length == 0) {
                return;
            }

            if (index < archivos.length - 1) {
                index++;
                cambiarImagen();
            }
        });

        panelImagen.add(botonanterior, BorderLayout.WEST);
        panelImagen.add(labelimagen, BorderLayout.CENTER);
        panelImagen.add(botonpost, BorderLayout.EAST);

        add(panelImagen, BorderLayout.CENTER);
    }

    private void initImagen() {
        if (archivo == null) {
            carpeta = new File("src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre + "/misImagenes");

            archivos = obtenerImagenesDeCarpeta(carpeta);

            if (archivos.length > 0) {
                archivo = archivos[index];
            }

        } else {
            carpeta = archivo.getParentFile();
            archivos = obtenerImagenesDeCarpeta(carpeta);

            for (int i = 0; i < archivos.length; i++) {
                if (archivos[i].equals(archivo)) {
                    index = i;
                    break;
                }
            }
        }

        if (archivos.length == 0) {
            labelimagen.setText("No hay imagenes");
            labelimagen.setFont(new Font("Arial", Font.BOLD, 20));
            labelimagen.setForeground(Color.RED);
            labelimagen.setIcon(null);
            botonpost.setEnabled(false);
            botonanterior.setEnabled(false);
        } else {
            botonpost.setEnabled(true);
            botonanterior.setEnabled(true);
        }
    }

    private File[] obtenerImagenesDeCarpeta(File carpeta) {
        if (carpeta == null || !carpeta.exists() || !carpeta.isDirectory()) {
            return new File[0];
        }

        File[] imgs = carpeta.listFiles((dir, nombre) -> {
            String n = nombre.toLowerCase();
            return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png");
        });

        if (imgs == null) {
            imgs = new File[0];
        }

        Arrays.sort(imgs, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return imgs;
    }

    private void cambiarImagen() {
        if (archivos == null || archivos.length == 0) {
            labelimagen.setText("No hay imagenes");
            labelimagen.setIcon(null);
            labeltitulo.setText("Sin imagen");
            return;
        }

        if (index < 0) {
            index = 0;
        }

        if (index >= archivos.length) {
            index = archivos.length - 1;
        }

        archivo = archivos[index];
        File archivoActual = archivo;

        labelimagen.setText("Cargando imagen...");
        labelimagen.setIcon(null);
        labeltitulo.setText(archivoActual.getName());
        botonanterior.setEnabled(false);
        botonpost.setEnabled(false);

        Thread hiloImagen = new Thread(() -> {
            ImageIcon imagen1 = new ImageIcon(archivoActual.getAbsolutePath());
            Image imagenEscalada = imagen1.getImage().getScaledInstance(500, 350, Image.SCALE_SMOOTH);
            ImageIcon imagenCargada = new ImageIcon(imagenEscalada);

            SwingUtilities.invokeLater(() -> {
                imagen = imagenCargada;
                labelimagen.setText("");
                labelimagen.setIcon(imagen);
                labeltitulo.setText(archivoActual.getName());

                labelimagen.repaint();
                labelimagen.revalidate();

                actualizarBotones();
            });
        });

        hiloImagen.start();
    }

    private void actualizarBotones() {
        if (archivos == null || archivos.length == 0) {
            botonanterior.setEnabled(false);
            botonpost.setEnabled(false);
            return;
        }

        botonanterior.setEnabled(index > 0);
        botonpost.setEnabled(index < archivos.length - 1);
    }

    private void abrirMenuCarpetaActual() {
        padre.actualizarMenu(carpeta, archivos);
        padre.mostrarCard("menu");
    }

    private void buscarOtraCarpeta() {
        File carpetaInicial;

        if (usuarioWinActivo.isAdmin) {
            carpetaInicial = new File("src/datos/windows/Z/infoUsuarios");
        } else {
            carpetaInicial = new File("src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre);
        }

        GUISelector selector = new GUISelector(padre, carpetaInicial, "jpg", "jpeg", "png");

        selector.setVisible(true);

        File seleccionado = selector.getArchivoSeleccionado();

        if (seleccionado != null) {
            abrirImagen(seleccionado);
        }
    }

    public void abrirImagen(File nuevaImagen) {
        if (nuevaImagen == null || !nuevaImagen.exists() || !nuevaImagen.isFile()) {
            return;
        }

        String nombre = nuevaImagen.getName().toLowerCase();
        if (!(nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".png"))) {
            return;
        }

        archivo = nuevaImagen;
        carpeta = nuevaImagen.getParentFile();
        archivos = obtenerImagenesDeCarpeta(carpeta);

        index = 0;
        for (int i = 0; i < archivos.length; i++) {
            if (archivos[i].equals(archivo)) {
                index = i;
                break;
            }
        }

        cambiarImagen();
        padre.actualizarMenu(carpeta, archivos);
        padre.mostrarCard("principal");
    }

    public File getCarpetaActual() {
        return carpeta;
    }

    public File[] getArchivos() {
        return archivos;
    }
}