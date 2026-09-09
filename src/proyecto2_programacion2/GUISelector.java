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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import Logica.RutasSistema;

public class GUISelector extends JDialog {

    private final File carpetaBase;
    private File carpetaActual;
    private File archivoSeleccionado;
    private final String[] extensionesPermitidas;
    private boolean seleccionMultiple;
    private boolean seleccionConfirmada;
    private final LinkedHashSet<File> archivosSeleccionados = new LinkedHashSet<>();

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

    /** El perfil del sistema determina el acceso, aunque la cuenta de Instagram sea otra. */
    static File carpetaDelPerfil() throws IOException {
        File usuarios = RutasSistema.USUARIOS.getCanonicalFile();
        if (usuarioWinActivo.nombre == null || usuarioWinActivo.nombre.isBlank()) {
            throw new IOException("No hay un usuario del sistema identificado.");
        }
        File base = usuarioWinActivo.isAdmin ? usuarios
                : RutasSistema.usuario(usuarioWinActivo.nombre).getCanonicalFile();
        if (!base.toPath().startsWith(usuarios.toPath())
                || (!usuarioWinActivo.isAdmin && base.equals(usuarios))) {
            throw new IOException("La carpeta del perfil no es válida.");
        }
        Files.createDirectories(base.toPath());
        return base;
    }

    public static File seleccionarArchivo(Component parent, String titulo, String... extensiones) {
        File[] archivos = seleccionarDesdePerfil(parent, titulo, false, extensiones);
        return archivos.length == 0 ? null : archivos[0];
    }

    public static File[] seleccionarArchivos(Component parent, String titulo, String... extensiones) {
        return seleccionarDesdePerfil(parent, titulo, true, extensiones);
    }

    private static File[] seleccionarDesdePerfil(Component parent, String titulo,
            boolean multiple, String... extensiones) {
        try {
            Window owner = parent instanceof Window ? (Window) parent
                    : parent == null ? null : SwingUtilities.getWindowAncestor(parent);
            GUISelector selector = new GUISelector(owner, carpetaDelPerfil(), extensiones);
            selector.setTitle(titulo);
            selector.setSeleccionMultiple(multiple);
            selector.setVisible(true);
            return selector.getArchivosSeleccionados();
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Seleccionar archivo", JOptionPane.ERROR_MESSAGE);
            return new File[0];
        }
    }

    public void setSeleccionMultiple(boolean multiple) {
        seleccionMultiple = multiple;
        cargarArchivos(carpetaActual);
    }

    private boolean dentroDeBase(File archivo) {
        try {
            return archivo != null && archivo.getCanonicalFile().toPath()
                    .startsWith(carpetaBase.getCanonicalFile().toPath());
        } catch (IOException ex) {
            return false;
        }
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
        labelMensaje.setText((seleccionMultiple ? "Marca una o varias imágenes. " : "")
                + "Extensiones permitidas: " + String.join(", ", extensionesPermitidas));
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
        if (!dentroDeBase(carpeta)) {
            mostrarMensaje("No puedes salir de la carpeta base.", true);
            return;
        }
        panelLista.removeAll();
        grupoBotones = new ButtonGroup();
        archivoSeleccionado = null;
        archivosSeleccionados.clear();
        seleccionConfirmada = false;

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
            if (dentroDeBase(archivo) && (archivo.isDirectory() || archivoValido(archivo))) {
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

        if (!seleccionMultiple || archivo.isDirectory()) {
            grupoBotones.add(boton);
        }

        boton.addActionListener(e -> {
            if (seleccionMultiple && archivo.isFile()) {
                grupoBotones.clearSelection();
                if (boton.isSelected()) {
                    archivosSeleccionados.add(archivo);
                } else {
                    archivosSeleccionados.remove(archivo);
                }
                archivoSeleccionado = null;
                mostrarMensaje("Archivos seleccionados: " + archivosSeleccionados.size(), false);
                return;
            }
            if (boton.isSelected()) {
                archivosSeleccionados.clear();
                if (seleccionMultiple) {
                    for (Component elemento : panelLista.getComponents()) {
                        if (elemento instanceof JToggleButton otro && otro != boton) {
                            otro.setSelected(false);
                        }
                    }
                }
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
        if (archivo == null || !archivo.isFile() || !dentroDeBase(archivo)) {
            return false;
        }

        if (Arrays.asList(extensionesPermitidas).contains("*")) {
            return true;
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

        if (!archivoSeleccionado.isDirectory() || !dentroDeBase(archivoSeleccionado)) {
            mostrarMensaje("Solo puedes abrir carpetas.", true);
            return;
        }

        carpetaActual = archivoSeleccionado;
        cargarArchivos(carpetaActual);
        mostrarMensaje("Carpeta abierta: " + carpetaActual.getName(), false);
    }

    private void volverCarpeta() {
        if (carpetaActual.getAbsoluteFile().equals(carpetaBase.getAbsoluteFile())) {
            mostrarMensaje("Ya estás en la carpeta base.", true);
            return;
        }

        File padre = carpetaActual.getParentFile();

        if (padre != null && dentroDeBase(padre)) {
            carpetaActual = padre;
            cargarArchivos(carpetaActual);
            mostrarMensaje("Volviste a: " + carpetaActual.getName(), false);
        }
    }

    private void aceptarSeleccion() {
        if (seleccionMultiple && !archivosSeleccionados.isEmpty()) {
            if (archivosSeleccionados.stream().allMatch(this::archivoValido)) {
                seleccionConfirmada = true;
                dispose();
            } else {
                mostrarMensaje("Uno de los archivos seleccionados ya no está disponible.", true);
            }
            return;
        }
        if (archivoSeleccionado == null) {
            mostrarMensaje("Selecciona un archivo.", true);
            return;
        }

        if (!archivoValido(archivoSeleccionado)) {
            mostrarMensaje("Debes seleccionar un archivo, no una carpeta.", true);
            return;
        }

        archivosSeleccionados.clear();
        archivosSeleccionados.add(archivoSeleccionado);
        seleccionConfirmada = true;
        dispose();
    }

    private void cancelarSeleccion() {
        archivoSeleccionado = null;
        archivosSeleccionados.clear();
        seleccionConfirmada = false;
        dispose();
    }

    private void refrescar() {
        panelLista.revalidate();
        panelLista.repaint();
    }

    public File getArchivoSeleccionado() {
        File[] archivos = getArchivosSeleccionados();
        return archivos.length == 0 ? null : archivos[0];
    }

    public File[] getArchivosSeleccionados() {
        return seleccionConfirmada ? archivosSeleccionados.toArray(File[]::new) : new File[0];
    }
}




