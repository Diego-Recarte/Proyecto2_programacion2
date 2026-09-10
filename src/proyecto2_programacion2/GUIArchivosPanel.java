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
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.tree.DefaultMutableTreeNode;

public class GUIArchivosPanel extends JPanel {

    private final Buscador buscador;

    private File carpetaActual;
    private File archivoSeleccionado;
    public File archivoCopiado;
    private final File carpetaBase;

    private JPanel panelListaArchivos;
    private JTextArea labelRuta;

    private Buscador.CriterioOrden criterioOrden = Buscador.CriterioOrden.NOMBRE;

    public GUIArchivosPanel(Buscador buscador, File carpetaInicial, File base) {
        this.buscador = buscador;
        this.carpetaActual = carpetaInicial;
        this.carpetaBase = base;

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(300, 600));
        setMaximumSize(new Dimension(300, 600));
        setMinimumSize(new Dimension(300, 600));
        setBackground(Color.GRAY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initBarra();
        initLista();

        mostrarPropiedades(carpetaActual);
    }

    public void setCarpetaActual(File carpeta) {
        if (carpeta != null && carpeta.exists()) {
            carpetaActual = carpeta;
            if (carpeta.isDirectory()) {
                archivoSeleccionado = null;
            }
            mostrarPropiedades(carpeta);
        }
    }

    public void mostrarArchivoSeleccionado(File archivo) {
        if (archivo != null && archivo.exists()) {
            archivoSeleccionado = archivo;
            if (archivo.isDirectory()) {
                carpetaActual = archivo;
            }
            mostrarPropiedades(archivo);
        }
    }

    public void setCriterioOrden(Buscador.CriterioOrden criterioOrden) {
        this.criterioOrden = criterioOrden;
    }

    public void recargarArchivos() {
        if (archivoSeleccionado != null && archivoSeleccionado.exists()) {
            mostrarPropiedades(archivoSeleccionado);
        } else if (carpetaActual != null && carpetaActual.exists()) {
            mostrarPropiedades(carpetaActual);
        } else {
            mostrarMensajeVacio();
        }
    }

    private void initBarra() {
        JPanel contenedorSuperior = new JPanel(new BorderLayout(10, 10));
        contenedorSuperior.setBackground(Color.BLACK);
        contenedorSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        labelRuta = new JTextArea("Ruta:");
        labelRuta.setForeground(Color.WHITE);
        labelRuta.setFont(new Font("Arial", Font.BOLD, 12));
        labelRuta.setLineWrap(true);
        labelRuta.setWrapStyleWord(true);
        labelRuta.setEditable(false);
        labelRuta.setOpaque(false);

        contenedorSuperior.add(labelRuta, BorderLayout.NORTH);

        add(contenedorSuperior, BorderLayout.NORTH);
    }

    private void initLista() {
        panelListaArchivos = new JPanel();
        panelListaArchivos.setLayout(new BoxLayout(panelListaArchivos, BoxLayout.Y_AXIS));
        panelListaArchivos.setBackground(Color.DARK_GRAY);

        JScrollPane scroll = new JScrollPane(panelListaArchivos);
        scroll.setBorder(BorderFactory.createTitledBorder("Propiedades"));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(scroll, BorderLayout.CENTER);
    }

    private void mostrarPropiedades(File archivo) {
        panelListaArchivos.removeAll();

        if (archivo == null || !archivo.exists()) {
            mostrarMensajeVacio();
            return;
        }

        labelRuta.setText("Ruta: " + obtenerRutaVisible(archivo));

        String nombre = archivo.getName().isEmpty() ? archivo.getPath() : archivo.getName();
        String padre = archivo.getName().isEmpty() ? archivo.getPath() : archivo.getParentFile().getName();
        String tipo = archivo.isDirectory() ? "Carpeta" : obtenerTipoArchivo(archivo);
        long tamanio = obtenerTamanio(archivo);
        String fecha = obtenerFechaModificacion(archivo);

        panelListaArchivos.add(crearCampoPropiedad("Nombre", nombre));
        panelListaArchivos.add(Box.createVerticalStrut(10));
        panelListaArchivos.add(crearCampoPropiedad("Tipo", tipo));
        panelListaArchivos.add(Box.createVerticalStrut(10));
        panelListaArchivos.add(crearCampoPropiedad("Tamaño", formatearTamanio(tamanio)));
        panelListaArchivos.add(Box.createVerticalStrut(10));
        panelListaArchivos.add(crearCampoPropiedad("Última modificación", fecha));
        panelListaArchivos.add(Box.createVerticalStrut(10));
        panelListaArchivos.add(crearCampoPropiedad("Padre", padre));

        refrescarLista();
    }

    private JTextArea crearCampoPropiedad(String titulo, String valor) {
        JTextArea area = new JTextArea(titulo + ":\n" + valor);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBackground(Color.BLACK);
        area.setForeground(Color.WHITE);
        area.setFont(new Font("Arial", Font.BOLD, 13));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        area.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);

        return area;
    }

    private void mostrarMensajeVacio() {
        panelListaArchivos.removeAll();

        JLabel vacio = new JLabel("No hay archivo o carpeta seleccionado");
        vacio.setForeground(Color.WHITE);
        vacio.setFont(new Font("Arial", Font.BOLD, 14));
        vacio.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelListaArchivos.add(vacio);
        refrescarLista();
    }

    private String obtenerFechaModificacion(File archivo) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formato.format(new Date(archivo.lastModified()));
    }

    private String obtenerRutaVisible(File archivo) {
        String ruta = archivo.getPath().replace("\\", "/");
        String prefijo = "./src/datos/windows/Z/";

        if (ruta.startsWith(prefijo)) {
            String visible = ruta.substring(prefijo.length());
            if (visible.equals("infoUsuarios")) {
                return "infoUsuarios/";
            }
            return visible;
        }

        return ruta;
    }

    private String formatearTamanio(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private long obtenerTamanio(File archivo) {
        if (archivo == null || !archivo.exists()) {
            return 0;
        }

        if (archivo.isFile()) {
            return archivo.length();
        }

        long total = 0;
        File[] hijos = archivo.listFiles();

        if (hijos != null) {
            for (File hijo : hijos) {
                total += obtenerTamanio(hijo);
            }
        }

        return total;
    }

    private String obtenerTipoArchivo(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');

        if (punto > 0 && punto < nombre.length() - 1) {
            return nombre.substring(punto + 1);
        }

        return "Sin extensión";
    }

    public void organizarCarpeta() throws BuscadorException {
        File carpetaOrganizar;

        if (archivoSeleccionado != null && archivoSeleccionado.isDirectory()) {
            carpetaOrganizar = archivoSeleccionado;
        } else if (carpetaActual != null && carpetaActual.isDirectory()) {
            carpetaOrganizar = carpetaActual;
        } else {
            throw new BuscadorException("La carpeta actual no es válida.");
        }

        File[] archivos = carpetaOrganizar.listFiles();

        if (archivos == null) {
            throw new BuscadorException("No se pudo leer la carpeta.");
        }

        File carpetaImagenes = new File(carpetaOrganizar, "misimagenes");
        File carpetaDocumentos = new File(carpetaOrganizar, "misdocumentos");
        File carpetaMusica = new File(carpetaOrganizar, "musica");

        for (File archivo : archivos) {
            if (!archivo.isFile()) continue;

            String extension = obtenerTipoArchivo(archivo).toLowerCase();
            File destino = null;

            if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
                carpetaImagenes.mkdirs();
                destino = new File(carpetaImagenes, archivo.getName());
            } else if (extension.equals("txt") || extension.equals("pdf") || extension.equals("doc")
                    || extension.equals("docx") || extension.equals("pwrd")) {
                carpetaDocumentos.mkdirs();
                destino = new File(carpetaDocumentos, archivo.getName());
            } else if (extension.equals("mp3") || extension.equals("wav") || extension.equals("mp5")) {
                carpetaMusica.mkdirs();
                destino = new File(carpetaMusica, archivo.getName());
            }

            if (destino != null && !destino.exists()) {
                try {
                    Files.move(archivo.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new BuscadorException("Error al organizar: " + archivo.getName());
                }
            }
        }

        eliminarSiEstaVacia(carpetaImagenes);
        eliminarSiEstaVacia(carpetaDocumentos);
        eliminarSiEstaVacia(carpetaMusica);

        mostrarPropiedades(carpetaOrganizar);
        buscador.recargarArbol();
    }

    private void eliminarSiEstaVacia(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] contenido = dir.listFiles();
        if (contenido == null || contenido.length == 0) {
            dir.delete();
        }
    }

    public void copiarArchivo() throws BuscadorException {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) buscador.arbolArchivos.getLastSelectedPathComponent();

        if (nodo == null) {
            throw new BuscadorException("Selecciona un archivo o carpeta para copiar.");
        }

        archivoSeleccionado = (File) nodo.getUserObject();
        archivoCopiado = archivoSeleccionado;
        mostrarPropiedades(archivoSeleccionado);
    }

    public void pegarArchivo() throws BuscadorException {
        if (archivoCopiado == null) {
            throw new BuscadorException("No hay ningún elemento copiado.");
        }

        File destino = new File(carpetaActual, archivoCopiado.getName());

        if (destino.exists()) {
            throw new BuscadorException("Ya existe un elemento con ese nombre en esta carpeta.");
        }

        try {
            if (archivoCopiado.isDirectory()) {
                copiarDirectorio(archivoCopiado, destino);
            } else {
                Files.copy(archivoCopiado.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            mostrarPropiedades(carpetaActual);
            buscador.recargarArbol();

        } catch (IOException ex) {
            throw new BuscadorException("Error al pegar: " + ex.getMessage());
        }
    }

    public void pedirRenombrarArchivo() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) buscador.arbolArchivos.getLastSelectedPathComponent();

        if (nodo == null) {
            buscador.mostrarMensaje("Selecciona un archivo o carpeta para renombrar.", true);
            return;
        }

        archivoSeleccionado = (File) nodo.getUserObject();

        buscador.mostrarEntrada("Nuevo nombre:", () -> {
            try {
                renombrarArchivo();
                buscador.mostrarMensaje("Elemento renombrado correctamente.", false);
                buscador.ocultarEntrada();
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });
    }

    public void renombrarArchivo() throws BuscadorException {
        String nuevoNombre = buscador.getTextoEntrada();

        if (nuevoNombre.isEmpty()) {
            throw new BuscadorException("El nuevo nombre no puede estar vacío.");
        }

        int punto = archivoSeleccionado.getName().lastIndexOf('.');
        String extension = "";

        if (punto != -1 && punto < archivoSeleccionado.getName().length() - 1) {
            extension = archivoSeleccionado.getName().substring(punto);
        }

        File nuevoArchivo = new File(archivoSeleccionado.getParentFile(), nuevoNombre + extension);

        if (nuevoArchivo.exists()) {
            throw new BuscadorException("Ya existe otro elemento con ese nombre.");
        }

        try {
            Files.move(archivoSeleccionado.toPath(), nuevoArchivo.toPath(), StandardCopyOption.REPLACE_EXISTING);
            archivoSeleccionado = nuevoArchivo;
            mostrarPropiedades(nuevoArchivo);
            buscador.recargarArbol();
        } catch (IOException e) {
            throw new BuscadorException("No se pudo renombrar el elemento.");
        }
    }

    public void cargarArchivo() throws BuscadorException {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) buscador.arbolArchivos.getLastSelectedPathComponent();

        if (nodo == null) {
            throw new BuscadorException("Selecciona un archivo para cargar.");
        }

        archivoSeleccionado = (File) nodo.getUserObject();
        if (archivoSeleccionado.isDirectory()) {
            throw new BuscadorException("Selecciona un archivo");
        }

        mostrarPropiedades(archivoSeleccionado);

        if (archivoSeleccionado.isDirectory()) {
            carpetaActual = archivoSeleccionado;
            archivoSeleccionado = null;
            mostrarPropiedades(carpetaActual);
            buscador.mostrarMensaje("Carpeta cargada: " + carpetaActual.getName(), false);
            return;
        }

        String nombre = archivoSeleccionado.getName().toLowerCase();

        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".png")) {
            buscador.abrirArchivoEnVisualizador(archivoSeleccionado);
            return;
        } else if (nombre.endsWith(".pwrd")) {
            buscador.abrirArchivoEnWord(archivoSeleccionado);
            return;
        } else if (nombre.endsWith(".mp5") || nombre.endsWith(".mp3") || nombre.endsWith(".wav")) {
            buscador.abrirArchivoEnReproductor(archivoSeleccionado);
            return;
        }

        throw new BuscadorException("No es valido para cargar");
    }

    public void eliminarArchivo(File dir) throws BuscadorException {
        File objetivo;

        if (dir != null) {
            objetivo = dir;
        } else {
            if (this.archivoSeleccionado == null) {
                throw new BuscadorException("Selecciona un archivo o carpeta para eliminar.");
            } else {
                objetivo = this.archivoSeleccionado;
            }
        }

        boolean eliminado = eliminarRecursivo(objetivo);

        if (eliminado) {
            archivoSeleccionado = null;
            mostrarPropiedades(carpetaActual);
            buscador.recargarArbol();
        } else {
            throw new BuscadorException("No se pudo eliminar el elemento.");
        }
    }

    public void pedirCrearCarpetaDentro(File archivo) {
        buscador.mostrarEntrada("Nueva carpeta:", () -> {
            try {
                if (archivo.isFile()) {
                    throw new BuscadorException("Seleccione una carpeta.");
                }
                crearCarpetaDentro(archivo);
                buscador.mostrarMensaje("Carpeta creada correctamente.", false);
                buscador.ocultarEntrada();
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });
    }

    public void crearCarpetaDentro(File carpetaActual) throws BuscadorException {
        String nombre = buscador.getTextoEntrada();

        if (nombre.isEmpty()) {
            throw new BuscadorException("Debes escribir un nombre válido.");
        }

        File nuevaCarpeta = new File(carpetaActual, nombre);

        if (nuevaCarpeta.exists()) {
            throw new BuscadorException("Ya existe una carpeta con ese nombre.");
        }

        if (nuevaCarpeta.mkdir()) {
            mostrarPropiedades(carpetaActual);
            buscador.recargarArbol();
        } else {
            throw new BuscadorException("No se pudo crear la carpeta.");
        }
    }

    public void importarArchivoDesdePC() throws BuscadorException {
        if (carpetaActual == null || !carpetaActual.exists() || !carpetaActual.isDirectory()) {
            throw new BuscadorException("La carpeta actual no es válida.");
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona un archivo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(true);

        int resultado = chooser.showOpenDialog(this);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            throw new BuscadorException("Importación cancelada.");
        }

        File archivoOrigen = chooser.getSelectedFile();

        if (archivoOrigen == null || !archivoOrigen.exists() || !archivoOrigen.isFile()) {
            throw new BuscadorException("El archivo seleccionado no es válido.");
        }

        String nombre = archivoOrigen.getName().toLowerCase();

        try {
            if (nombre.endsWith(".mp3") || nombre.endsWith(".wav")) {
                musica m = new musica(archivoOrigen);

                File archivoDestino = new File(carpetaActual, m.getNombre() + ".mp5");

                if (archivoDestino.exists()) {
                    throw new BuscadorException("Ya existe un archivo con ese nombre en la carpeta actual.");
                }

                m.guardarComoMP5(carpetaActual.getPath());

            } else {
                File archivoDestino = new File(carpetaActual, archivoOrigen.getName());

                if (archivoDestino.exists()) {
                    throw new BuscadorException("Ya existe un archivo con ese nombre en la carpeta actual.");
                }

                Files.copy(archivoOrigen.toPath(), archivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            mostrarPropiedades(carpetaActual);
            buscador.recargarArbol();

        } catch (IOException e) {
            throw new BuscadorException("Error al importar el archivo: " + e.getMessage());
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

    public void copiarDirectorio(File origen, File destino) throws IOException {
        if (!origen.exists()) {
            throw new IOException("La carpeta de origen no existe.");
        }

        if (origen.isDirectory()) {
            if (!destino.exists()) {
                if (!destino.mkdirs()) {
                    throw new IOException("No se pudo crear la carpeta destino.");
                }
            }

            File[] elementos = origen.listFiles();
            if (elementos != null) {
                for (File elemento : elementos) {
                    File nuevoDestino = new File(destino, elemento.getName());
                    copiarDirectorio(elemento, nuevoDestino);
                }
            }
        } else {
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void refrescarLista() {
        panelListaArchivos.revalidate();
        panelListaArchivos.repaint();
    }
}