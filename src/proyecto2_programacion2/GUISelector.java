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
import java.io.File;
import java.util.Arrays;

public class GUISelector extends JDialog {

    private final File carpetaBase;
    private File carpetaActual;
    private File archivoSeleccionado;
    private final String[] extensionesPermitidas;

    private JLabel labelRuta;
    private JLabel labelMensaje;
    private JPanel panelLista;
    private ButtonGroup grupoBotones;

    private JButton botonAceptar;
    private JButton botonCancelar;
    private JButton botonVolver;
    private JButton botonAbrir;

    public GUISelector(Window owner, File carpetaInicial, String... extensionesPermitidas) {
        super(owner, "Seleccionar archivo", ModalityType.APPLICATION_MODAL);

        if (carpetaInicial == null || !carpetaInicial.exists() || !carpetaInicial.isDirectory()) {
            throw new IllegalArgumentException("La carpeta inicial no es válida.");
        }

        if (extensionesPermitidas == null || extensionesPermitidas.length == 0) {
            throw new IllegalArgumentException("Debes indicar al menos una extensión permitida.");
        }

        this.carpetaBase = carpetaInicial;
        this.carpetaActual = carpetaInicial;
        this.extensionesPermitidas = normalizarExtensiones(extensionesPermitidas);

        configurarVentana();
        initComponentes();
        cargarArchivos(carpetaActual);
    }

    private void configurarVentana() {
        setSize(900, 600);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);
    }

    private void initComponentes() {
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(Color.BLACK);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        labelRuta = new JLabel("Ruta: ");
        labelRuta.setForeground(Color.WHITE);
        labelRuta.setFont(new Font("Arial", Font.BOLD, 13));

        labelMensaje = new JLabel("Extensiones permitidas: " + String.join(", ", extensionesPermitidas));
        labelMensaje.setForeground(Color.LIGHT_GRAY);
        labelMensaje.setFont(new Font("Arial", Font.PLAIN, 12));

        panelSuperior.add(labelRuta);
        panelSuperior.add(Box.createVerticalStrut(5));
        panelSuperior.add(labelMensaje);

        add(panelSuperior, BorderLayout.NORTH);

        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(Color.DARK_GRAY);

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(BorderFactory.createTitledBorder("Archivos"));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(scroll, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBackground(Color.BLACK);

        botonVolver = new JButton("Volver");
        botonAbrir = new JButton("Abrir carpeta");
        botonAceptar = new JButton("Aceptar");
        botonCancelar = new JButton("Cancelar");

        configurarBoton(botonVolver);
        configurarBoton(botonAbrir);
        configurarBoton(botonAceptar);
        configurarBoton(botonCancelar);

        botonVolver.addActionListener(e -> volverCarpeta());
        botonAbrir.addActionListener(e -> abrirSeleccionado());
        botonAceptar.addActionListener(e -> aceptarSeleccion());
        botonCancelar.addActionListener(e -> cancelarSeleccion());

        panelInferior.add(botonVolver);
        panelInferior.add(botonAbrir);
        panelInferior.add(botonAceptar);
        panelInferior.add(botonCancelar);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void configurarBoton(JButton boton) {
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.BLACK);
        boton.setFocusPainted(false);
    }

    private void mostrarMensaje(String texto, boolean error) {
        labelMensaje.setText(texto);

        if (error) {
            labelMensaje.setForeground(new Color(255, 120, 120));
        } else {
            labelMensaje.setForeground(new Color(140, 255, 140));
        }
    }

    private void restaurarMensajeBase() {
        labelMensaje.setText("Extensiones permitidas: " + String.join(", ", extensionesPermitidas));
        labelMensaje.setForeground(Color.LIGHT_GRAY);
    }

    private String[] normalizarExtensiones(String[] extensiones) {
        String[] copia = new String[extensiones.length];

        for (int i = 0; i < extensiones.length; i++) {
            String ext = extensiones[i].toLowerCase().trim();
            if (ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            copia[i] = ext;
        }

        return copia;
    }

    private void cargarArchivos(File carpeta) {
        panelLista.removeAll();
        grupoBotones = new ButtonGroup();
        archivoSeleccionado = null;

        labelRuta.setText("Ruta: " + carpeta.getAbsolutePath());
        restaurarMensajeBase();

        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            mostrarEtiqueta("No se pudo leer el contenido de la carpeta.");
            mostrarMensaje("No se pudo leer el contenido de la carpeta.", true);
            refrescar();
            return;
        }

        Arrays.sort(archivos, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        boolean hayElementos = false;

        for (File archivo : archivos) {
            if (archivo.isDirectory() || archivoValido(archivo)) {
                agregarElemento(archivo);
                hayElementos = true;
            }
        }

        if (!hayElementos) {
            mostrarEtiqueta("No hay archivos válidos en esta carpeta.");
            mostrarMensaje("No hay archivos válidos en esta carpeta.", true);
        }

        refrescar();
    }

    private void agregarElemento(File archivo) {
        String tipo = archivo.isDirectory() ? "[CARPETA]" : "[ARCHIVO]";
        String texto = tipo + " " + archivo.getName();

        JToggleButton boton = new JToggleButton(texto);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        boton.setPreferredSize(new Dimension(700, 45));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBackground(Color.BLACK);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);

        grupoBotones.add(boton);

        boton.addActionListener(e -> {
            if (boton.isSelected()) {
                archivoSeleccionado = archivo;

                if (archivo.isDirectory()) {
                    mostrarMensaje("Carpeta seleccionada: " + archivo.getName(), false);
                } else {
                    mostrarMensaje("Archivo seleccionado: " + archivo.getName(), false);
                }
            }
        });

        panelLista.add(boton);
        panelLista.add(Box.createVerticalStrut(5));
    }

    private void mostrarEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panelLista.add(label);
    }

    private boolean archivoValido(File archivo) {
        if (archivo == null || !archivo.isFile()) {
            return false;
        }

        String nombre = archivo.getName().toLowerCase();
        int punto = nombre.lastIndexOf('.');

        if (punto == -1 || punto == nombre.length() - 1) {
            return false;
        }

        String extension = nombre.substring(punto + 1);

        for (String ext : extensionesPermitidas) {
            if (extension.equals(ext)) {
                return true;
            }
        }

        return false;
    }

    private void abrirSeleccionado() {
        if (archivoSeleccionado == null) {
            mostrarMensaje("Selecciona una carpeta.", true);
            return;
        }

        if (!archivoSeleccionado.isDirectory()) {
            mostrarMensaje("Solo puedes abrir carpetas.", true);
            return;
        }

        carpetaActual = archivoSeleccionado;
        cargarArchivos(carpetaActual);
        mostrarMensaje("Carpeta abierta: " + carpetaActual.getName(), false);
    }

    private void volverCarpeta() {
        if (carpetaActual.equals(carpetaBase)) {
            mostrarMensaje("Ya estás en la carpeta base.", true);
            return;
        }

        File padre = carpetaActual.getParentFile();

        if (padre != null) {
            carpetaActual = padre;
            cargarArchivos(carpetaActual);
            mostrarMensaje("Volviste a: " + carpetaActual.getName(), false);
        }
    }

    private void aceptarSeleccion() {
        if (archivoSeleccionado == null) {
            mostrarMensaje("Selecciona un archivo.", true);
            return;
        }

        if (archivoSeleccionado.isDirectory()) {
            mostrarMensaje("Debes seleccionar un archivo, no una carpeta.", true);
            return;
        }

        dispose();
    }

    private void cancelarSeleccion() {
        archivoSeleccionado = null;
        dispose();
    }

    private void refrescar() {
        panelLista.revalidate();
        panelLista.repaint();
    }

    public File getArchivoSeleccionado() {
        return archivoSeleccionado;
    }
}




