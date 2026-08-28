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
import java.io.File;
import java.util.Arrays;
import javax.swing.*;

public class Buscador extends JDialog {

    private JPanel panelOpciones;
    private ButtonGroup grupoCarpetas;
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

    public Buscador(GUIPantallaPrincipal perfil) {
        super(perfil, "Buscador", false);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1150, 800);
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
        initBarraIzquierda();
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
        contenedorSuperior.add(panelEntrada);

        add(contenedorSuperior, BorderLayout.NORTH);
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

    private void initBarraIzquierda() {
        JPanel barraIzquierda = new JPanel(new BorderLayout(10, 10));
        barraIzquierda.setPreferredSize(new Dimension(250, 800));
        barraIzquierda.setBackground(Color.BLACK);
        barraIzquierda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelAcciones = new JPanel(new GridLayout(1, 2, 10, 10));
        panelAcciones.setBackground(Color.BLACK);

        JButton crear = new JButton("Crear");
        JButton borrar = new JButton("Borrar");

        configurarBoton(crear);
        configurarBoton(borrar);

        crear.addActionListener(e -> pedirCrearCarpetaLateral());
        borrar.addActionListener(e -> borrarCarpetaLateral());

        panelAcciones.add(crear);
        panelAcciones.add(borrar);

        panelOpciones = new JPanel();
        panelOpciones.setLayout(new BoxLayout(panelOpciones, BoxLayout.Y_AXIS));
        panelOpciones.setBackground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(panelOpciones);
        scroll.setBorder(BorderFactory.createTitledBorder("Carpetas"));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        barraIzquierda.add(panelAcciones, BorderLayout.NORTH);
        barraIzquierda.add(scroll, BorderLayout.CENTER);

        add(barraIzquierda, BorderLayout.WEST);

        recargarCarpetas();
    }

    private void initExplorador() {
        carpetaSeleccionada = carpetaBase;
        exploradorPanel = new GUIArchivosPanel(this, carpetaSeleccionada, carpetaBase);
        add(exploradorPanel, BorderLayout.CENTER);
    }

    private void configurarBoton(JButton boton) {
        boton.setFont(new Font("Arial", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.BLACK);
        boton.setFocusPainted(false);
    }

    public void recargarCarpetas() {
        panelOpciones.removeAll();
        grupoCarpetas = new ButtonGroup();

        File[] archivos = carpetaBase.listFiles();

        if (archivos != null) {
            Arrays.sort(archivos, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    JToggleButton boton = new JToggleButton(archivo.getName());

                    boton.setFont(new Font("Arial", Font.BOLD, 11));
                    boton.setPreferredSize(new Dimension(200, 35));
                    boton.setMaximumSize(new Dimension(200, 35));
                    boton.setMinimumSize(new Dimension(200, 35));
                    boton.setForeground(Color.WHITE);
                    boton.setBackground(Color.BLACK);
                    boton.setFocusPainted(false);
                    boton.setHorizontalAlignment(SwingConstants.CENTER);

                    grupoCarpetas.add(boton);

                    boton.addActionListener(e -> {
                        if (boton.isSelected()) {
                            carpetaSeleccionada = archivo;
                            exploradorPanel.setCarpetaActual(carpetaSeleccionada);
                            mostrarMensaje("Carpeta seleccionada: " + archivo.getName(), false);
                        }
                    });

                    panelOpciones.add(boton);
                    panelOpciones.add(Box.createVerticalStrut(5));
                }
            }
        }

        panelOpciones.revalidate();
        panelOpciones.repaint();
    }

    private void pedirCrearCarpetaLateral() {
        mostrarEntrada("Nueva carpeta:", () -> {
            try {
                crearCarpetaLateral();
                recargarCarpetas();
                mostrarMensaje("Carpeta creada correctamente.", false);
                ocultarEntrada();
            } catch (BuscadorException ex) {
                mostrarMensaje(ex.getMessage(), true);
            }
        });
    }

    private void crearCarpetaLateral() throws BuscadorException {
        String nombre = getTextoEntrada();

        if (nombre.isEmpty()) {
            throw new BuscadorException("Debes escribir un nombre válido.");
        }

        File nueva = new File(carpetaBase, nombre);

        if (nueva.exists()) {
            throw new BuscadorException("Ya existe una carpeta con ese nombre.");
        }

        if (!nueva.mkdir()) {
            throw new BuscadorException("No se pudo crear la carpeta.");
        }
    }

    private void borrarCarpetaLateral() {
        try {
            eliminarCarpetaLateral();
            carpetaSeleccionada = carpetaBase;
            exploradorPanel.setCarpetaActual(carpetaBase);
            recargarCarpetas();
            mostrarMensaje("Carpeta eliminada correctamente.", false);
        } catch (BuscadorException ex) {
            mostrarMensaje(ex.getMessage(), true);
        }
    }

    private void eliminarCarpetaLateral() throws BuscadorException {
        if (carpetaSeleccionada == null || carpetaSeleccionada.equals(carpetaBase)) {
            throw new BuscadorException("Selecciona una carpeta válida para borrar.");
        }

        boolean eliminado = eliminarRecursivo(carpetaSeleccionada);

        if (!eliminado) {
            throw new BuscadorException("No se pudo eliminar la carpeta.");
        }
    }

    private boolean eliminarRecursivo(File archivo) {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!eliminarRecursivo(hijo)) {
                        return false;
                    }
                }
            }
        }
        return archivo.delete();
    }
}