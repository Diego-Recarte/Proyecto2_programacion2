/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import javazoom.jl.player.advanced.AdvancedPlayer;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.tree.*;

public class Buscador extends JDialog {

    private JTree arbolArchivos;
    private DefaultTreeModel modeloArbol;
    private DefaultMutableTreeNode nodoRaiz;

    private GUIArchivosPanel exploradorPanel;

    private File carpetaBase;
    private File carpetaSeleccionada;

    private JLabel labelMensaje;
    private Timer timerMensaje;

    private JPanel panelEntrada;
    private JLabel labelEntrada;
    private JTextField textFieldEntrada;
    private JButton botonAceptarEntrada;
    private JButton botonCancelarEntrada;

    private Runnable accionPendiente;

    private JComboBox<String> comboOrden;
    private CriterioOrden criterioActual = CriterioOrden.NOMBRE;

    private final GUIPantallaPrincipal perfil;

    public enum CriterioOrden {
        NOMBRE, FECHA, TIPO, TAMANIO
    }

    public Buscador(GUIPantallaPrincipal perfil) {
        super(perfil, "Buscador", false);
        this.perfil = perfil;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(perfil);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);

        if (usuarioWinActivo.isAdmin) {
            carpetaBase = new File("./src/datos/windows/Z/infoUsuarios");
        } else {
            carpetaBase = new File("./src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre);
        }

        if (!carpetaBase.exists()) {
            carpetaBase.mkdirs();
        }

        initPanelSuperior();
        initArbolIzquierdo();
        initExplorador();

        setVisible(true);
    }

    private void initPanelSuperior() {
        JPanel contenedorSuperior = new JPanel();
        contenedorSuperior.setLayout(new BoxLayout(contenedorSuperior, BoxLayout.Y_AXIS));
        contenedorSuperior.setBackground(Color.BLACK);

        labelMensaje = new JLabel(" ");
        labelMensaje.setOpaque(true);
        labelMensaje.setBackground(new Color(25, 25, 25));
        labelMensaje.setForeground(Color.WHITE);
        labelMensaje.setFont(new Font("Arial", Font.BOLD, 14));
        labelMensaje.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        labelMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelOrden = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panelOrden.setBackground(Color.BLACK);
        panelOrden.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelOrden = new JLabel("Ordenar por:");
        labelOrden.setForeground(Color.WHITE);
        labelOrden.setFont(new Font("Arial", Font.BOLD, 13));

        comboOrden = new JComboBox<>(new String[]{"Nombre", "Fecha", "Tipo", "Tamaño"});
        comboOrden.setFont(new Font("Arial", Font.PLAIN, 13));
        comboOrden.addActionListener(e -> cambiarOrden());

        panelOrden.add(labelOrden);
        panelOrden.add(comboOrden);

        panelEntrada = new JPanel(new BorderLayout(8, 8));
        panelEntrada.setBackground(Color.BLACK);
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        panelEntrada.setVisible(false);
        panelEntrada.setAlignmentX(Component.LEFT_ALIGNMENT);

        labelEntrada = new JLabel("Escribe aquí:");
        labelEntrada.setForeground(Color.WHITE);
        labelEntrada.setFont(new Font("Arial", Font.BOLD, 13));

        textFieldEntrada = new JTextField();
        textFieldEntrada.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel panelBotonesEntrada = new JPanel(new GridLayout(1, 2, 8, 8));
        panelBotonesEntrada.setBackground(Color.BLACK);

        botonAceptarEntrada = new JButton("Aceptar");
        botonCancelarEntrada = new JButton("Cancelar");

        configurarBoton(botonAceptarEntrada);
        configurarBoton(botonCancelarEntrada);

        botonAceptarEntrada.addActionListener(e -> ejecutarAccionPendiente());
        botonCancelarEntrada.addActionListener(e -> ocultarEntrada());

        panelBotonesEntrada.add(botonAceptarEntrada);
        panelBotonesEntrada.add(botonCancelarEntrada);

        panelEntrada.add(labelEntrada, BorderLayout.WEST);
        panelEntrada.add(textFieldEntrada, BorderLayout.CENTER);
        panelEntrada.add(panelBotonesEntrada, BorderLayout.EAST);

        contenedorSuperior.add(labelMensaje);
        contenedorSuperior.add(panelOrden);
        contenedorSuperior.add(panelEntrada);

        add(contenedorSuperior, BorderLayout.NORTH);
    }

    private void initArbolIzquierdo() {
        nodoRaiz = new DefaultMutableTreeNode(carpetaBase);
        modeloArbol = new DefaultTreeModel(nodoRaiz);
        arbolArchivos = new JTree(modeloArbol);

        arbolArchivos.setRootVisible(true);
        arbolArchivos.setShowsRootHandles(true);
        arbolArchivos.setBackground(Color.BLACK);
        arbolArchivos.setForeground(Color.WHITE);

        arbolArchivos.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
            if (nodo == null) return;

            File archivo = (File) nodo.getUserObject();
            carpetaSeleccionada = archivo;

            if (archivo.isDirectory()) {
                exploradorPanel.setCarpetaActual(archivo);
                exploradorPanel.setCriterioOrden(criterioActual);
                exploradorPanel.recargarArchivos();
                mostrarMensaje("Carpeta seleccionada: " + archivo.getName(), false);
            }
        });

        JScrollPane scrollArbol = new JScrollPane(arbolArchivos);
        scrollArbol.setPreferredSize(new Dimension(400, 800));
        scrollArbol.setBorder(BorderFactory.createTitledBorder("Explorador"));

        add(scrollArbol, BorderLayout.WEST);

        cargarArbolEnThread();
    }

    private void cargarArbolEnThread() {
        mostrarMensaje("Cargando árbol de archivos...", false);

        Thread hiloArbol = new Thread(() -> {
            DefaultMutableTreeNode nuevaRaiz = crearNodoArchivo(carpetaBase);

            SwingUtilities.invokeLater(() -> {
                nodoRaiz = nuevaRaiz;
                modeloArbol.setRoot(nodoRaiz);
                modeloArbol.reload();

                for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
                    arbolArchivos.expandRow(i);
                }

                mostrarMensaje("Árbol cargado correctamente.", false);
            });
        });

        hiloArbol.setName("Hilo-Arbol-Buscador");
        hiloArbol.start();
    }

    private DefaultMutableTreeNode crearNodoArchivo(File archivo) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(archivo);

        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                java.util.Arrays.sort(hijos, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                for (File hijo : hijos) {
                    nodo.add(crearNodoArchivo(hijo));
                }
            }
        }

        return nodo;
    }

    private void initExplorador() {
        carpetaSeleccionada = carpetaBase;
        exploradorPanel = new GUIArchivosPanel(this, carpetaSeleccionada, carpetaBase);
        exploradorPanel.setCriterioOrden(criterioActual);
        add(exploradorPanel, BorderLayout.CENTER);
    }

    private void cambiarOrden() {
        String seleccion = (String) comboOrden.getSelectedItem();
        if (seleccion == null) return;

        switch (seleccion) {
            case "Nombre":
                criterioActual = CriterioOrden.NOMBRE;
                break;
            case "Fecha":
                criterioActual = CriterioOrden.FECHA;
                break;
            case "Tipo":
                criterioActual = CriterioOrden.TIPO;
                break;
            case "Tamaño":
                criterioActual = CriterioOrden.TAMANIO;
                break;
        }

        if (exploradorPanel != null) {
            exploradorPanel.setCriterioOrden(criterioActual);
            exploradorPanel.recargarArchivos();
        }

        mostrarMensaje("Orden aplicado: " + seleccion, false);
    }

    public void recargarArbol() {
        cargarArbolEnThread();
    }

    private void configurarBoton(JButton boton) {
        boton.setFont(new Font("Arial", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.BLACK);
        boton.setFocusPainted(false);
    }

    public CriterioOrden getCriterioActual() {
        return criterioActual;
    }

    public void mostrarMensaje(String mensaje, boolean error) {
        labelMensaje.setText(mensaje);

        if (error) {
            labelMensaje.setBackground(new Color(120, 30, 30));
        } else {
            labelMensaje.setBackground(new Color(30, 90, 40));
        }

        if (timerMensaje != null && timerMensaje.isRunning()) {
            timerMensaje.stop();
        }

        timerMensaje = new Timer(3000, e -> {
            labelMensaje.setText(" ");
            labelMensaje.setBackground(new Color(25, 25, 25));
        });
        timerMensaje.setRepeats(false);
        timerMensaje.start();
    }

    public void mostrarEntrada(String textoLabel, Runnable accion) {
        labelEntrada.setText(textoLabel);
        textFieldEntrada.setText("");
        accionPendiente = accion;
        panelEntrada.setVisible(true);
        textFieldEntrada.requestFocusInWindow();
        revalidate();
        repaint();
    }

    public String getTextoEntrada() {
        return textFieldEntrada.getText().trim();
    }

    public void ocultarEntrada() {
        textFieldEntrada.setText("");
        accionPendiente = null;
        panelEntrada.setVisible(false);
        revalidate();
        repaint();
    }

    private void ejecutarAccionPendiente() {
        if (accionPendiente != null) {
            accionPendiente.run();
        }
    }

    public void abrirArchivoEnVisualizador(File archivo) throws BuscadorException {
        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            throw new BuscadorException("El archivo seleccionado no es válido.");
        }

        String nombre = archivo.getName().toLowerCase();

        if (!nombre.endsWith(".jpg") && !nombre.endsWith(".jpeg") && !nombre.endsWith(".png")) {
            throw new BuscadorException("El archivo seleccionado no es una imagen válida.");
        }

        new GUIVisualizadorPantalla(perfil, archivo);
    }

    public void abrirArchivoEnWord(File archivo) throws BuscadorException {
        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            throw new BuscadorException("El archivo seleccionado no es válido.");
        }

        String nombre = archivo.getName().toLowerCase();

        if (!nombre.endsWith(".wrd")) {
            throw new BuscadorException("El archivo seleccionado no es un documento válido.");
        }

        new GUIpantallaWord(perfil, archivo, true);
    }

    public void abrirArchivoEnReproductor(File archivo) throws BuscadorException {
        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            throw new BuscadorException("El archivo seleccionado no es válido.");
        }

        String nombre = archivo.getName().toLowerCase();
        if (!nombre.endsWith(".mp5") && !nombre.endsWith(".mp3") && !nombre.endsWith(".wav")) {
            throw new BuscadorException("El archivo seleccionado no es una canción válida.");
        }

        new GUIReproductor(perfil, archivo);
    }
}
