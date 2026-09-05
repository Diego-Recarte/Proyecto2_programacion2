package proyecto2_programacion2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/** Ventana principal del reproductor de música de miniWindows. */
public class GUIReproductor extends JDialog {

    private final File carpetaBase;
    private final DefaultListModel<File> modeloCanciones = new DefaultListModel<>();
    private final JList<File> listaCanciones = new JList<>(modeloCanciones);
    private final JPanel panelReproductor = new JPanel(new BorderLayout());
    private GUIReproductorPrincipal reproductorActual;
    private File archivoActual;
    private boolean actualizandoSeleccion;

    public GUIReproductor(GUIPantallaPrincipal perfil) {
        this(perfil, null);
    }

    public GUIReproductor(GUIPantallaPrincipal perfil, File cancionInicial) {
        super(perfil, "Reproductor de música", false);

        if (usuarioWinActivo.isAdmin) {
            carpetaBase = new File("./src/datos/windows/Z/infoUsuarios");
        } else {
            carpetaBase = new File("./src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre);
        }
        if (!carpetaBase.exists()) {
            carpetaBase.mkdirs();
        }

        configurarVentana(perfil);
        construirInterfaz();
        recargarBiblioteca();

        if (cancionInicial != null) {
            cargarCancion(cancionInicial);
        } else if (!modeloCanciones.isEmpty()) {
            cargarCancion(modeloCanciones.get(0));
        } else {
            mostrarEstadoVacio();
        }

        setVisible(true);
    }

    private void configurarVentana(GUIPantallaPrincipal perfil) {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 720);
        setMinimumSize(new Dimension(760, 620));
        setLocationRelativeTo(perfil);
        getContentPane().setBackground(Color.BLACK);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                detenerReproduccionActual();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                detenerReproduccionActual();
            }
        });
    }

    private void construirInterfaz() {
        JPanel barra = new JPanel(new BorderLayout(10, 10));
        barra.setBackground(Color.BLACK);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Biblioteca de música");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        JButton abrir = crearBoton("Buscar canción");
        JButton actualizar = crearBoton("Actualizar");
        abrir.addActionListener(e -> abrirSelector());
        actualizar.addActionListener(e -> recargarBiblioteca());
        acciones.add(abrir);
        acciones.add(actualizar);

        barra.add(titulo, BorderLayout.WEST);
        barra.add(acciones, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);

        listaCanciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaCanciones.setBackground(new Color(24, 24, 24));
        listaCanciones.setForeground(Color.WHITE);
        listaCanciones.setFixedCellHeight(42);
        listaCanciones.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof File archivo) {
                    label.setText(archivo.getName());
                    label.setToolTipText(archivo.getAbsolutePath());
                }
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return label;
            }
        });
        listaCanciones.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || actualizandoSeleccion) {
                return;
            }
            File seleccionada = listaCanciones.getSelectedValue();
            if (seleccionada != null && !seleccionada.equals(archivoActual)) {
                cargarCancion(seleccionada);
            }
        });

        JPanel biblioteca = new JPanel(new BorderLayout(5, 5));
        biblioteca.setBackground(Color.BLACK);
        biblioteca.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY), "Canciones",0, 0, null, Color.WHITE));
        biblioteca.add(new JScrollPane(listaCanciones), BorderLayout.CENTER);

        JButton cargar = crearBoton("Cargar seleccionada");
        cargar.addActionListener(e -> {
            File seleccionada = listaCanciones.getSelectedValue();
            if (seleccionada == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una canción de la lista.",
                        "Reproductor", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            cargarCancion(seleccionada);
        });
        biblioteca.add(cargar, BorderLayout.SOUTH);

        panelReproductor.setBackground(Color.BLACK);
        JSplitPane divisor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, biblioteca, panelReproductor);
        divisor.setDividerLocation(260);
        divisor.setResizeWeight(0.28);
        divisor.setBorder(null);
        add(divisor, BorderLayout.CENTER);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(210, 50, 35));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        return boton;
    }

    private void mostrarEstadoVacio() {
        detenerReproduccionActual();
        panelReproductor.removeAll();
        JLabel mensaje = new JLabel(
                "<html><div style='text-align:center'>No hay canciones disponibles.<br>"
                + "Importa un MP3/WAV desde el Buscador o pulsa Buscar canción.</div></html>",
                SwingConstants.CENTER);
        mensaje.setForeground(Color.LIGHT_GRAY);
        mensaje.setFont(new Font("Arial", Font.PLAIN, 16));
        panelReproductor.add(mensaje, BorderLayout.CENTER);
        panelReproductor.revalidate();
        panelReproductor.repaint();
    }

    void abrirSelector() {
        try {
            GUISelector selector = new GUISelector(this, carpetaBase, "mp5", "mp3", "wav");
            selector.setVisible(true);
            File seleccionada = selector.getArchivoSeleccionado();
            if (seleccionada != null) {
                cargarCancion(seleccionada);
                recargarBiblioteca();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reproductor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recargarBiblioteca() {
        File seleccionAnterior = archivoActual;
        List<File> canciones = new ArrayList<>();
        recolectarCanciones(carpetaBase, canciones);
        canciones.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        modeloCanciones.clear();
        for (File archivo : canciones) {
            modeloCanciones.addElement(archivo);
        }

        if (seleccionAnterior != null) {
            listaCanciones.setSelectedValue(seleccionAnterior, true);
        }
    }

    private void recolectarCanciones(File carpeta, List<File> resultado) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return;
        }
        for (File archivo : archivos) {
            if (archivo.isDirectory()) {
                recolectarCanciones(archivo, resultado);
            } else if (esAudioCompatible(archivo)) {
                resultado.add(archivo);
            }
        }
    }

    private boolean esAudioCompatible(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        return nombre.endsWith(".mp5") || nombre.endsWith(".mp3") || nombre.endsWith(".wav");
    }

    private void cargarCancion(File archivo) {
        if (archivo == null || !archivo.isFile() || !esAudioCompatible(archivo)) {
            JOptionPane.showMessageDialog(this, "El archivo de audio no es válido.",
                    "Reproductor", JOptionPane.ERROR_MESSAGE);
            return;
        }

        detenerReproduccionActual();
        try {
            GUIReproductorPrincipal nuevo = new GUIReproductorPrincipal(this, archivo);
            reproductorActual = nuevo;
            archivoActual = archivo;
            panelReproductor.removeAll();
            panelReproductor.add(nuevo, BorderLayout.CENTER);
            panelReproductor.revalidate();
            panelReproductor.repaint();
            seleccionarEnLista(archivo);
            setTitle("Reproductor - " + archivo.getName());
        } catch (Exception ex) {
            archivoActual = null;
            panelReproductor.removeAll();
            mostrarEstadoVacio();
            JOptionPane.showMessageDialog(this, "No se pudo abrir la canción: " + ex.getMessage(),
                    "Reproductor", JOptionPane.ERROR_MESSAGE);
        }
    }

    void cancionAnterior() {
        cambiarCancion(-1);
    }

    void cancionSiguiente() {
        cambiarCancion(1);
    }

    private void cambiarCancion(int desplazamiento) {
        if (modeloCanciones.isEmpty()) {
            return;
        }
        int indice = listaCanciones.getSelectedIndex();
        if (indice < 0) {
            indice = 0;
        } else {
            indice = (indice + desplazamiento + modeloCanciones.size()) % modeloCanciones.size();
        }
        cargarCancion(modeloCanciones.get(indice));
    }

    private void detenerReproduccionActual() {
        if (reproductorActual != null) {
            reproductorActual.detener();
            reproductorActual = null;
        }
    }

    private void seleccionarEnLista(File archivo) {
        actualizandoSeleccion = true;
        try {
            listaCanciones.setSelectedValue(archivo, true);
        } finally {
            actualizandoSeleccion = false;
        }
    }
}
