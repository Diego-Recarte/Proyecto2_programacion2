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
import java.util.Arrays;
import java.util.Calendar;

public class GUIArchivosPanel extends JPanel {

    private final Buscador buscador;

    private File carpetaActual;
    private File archivoSeleccionado;
    private File archivoCopiado;
    private final File carpetaBase;

    private JPanel panelListaArchivos;
    private JLabel labelRuta;

    private ButtonGroup grupoArchivos;

    private Buscador.CriterioOrden criterioOrden = Buscador.CriterioOrden.NOMBRE;

    public GUIArchivosPanel(Buscador buscador, File carpetaInicial, File base) {
        this.buscador = buscador;
        this.carpetaActual = carpetaInicial;
        this.carpetaBase = base;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.GRAY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initBarra();
        initLista();

        archivos(carpetaActual);
    }

    public void setCarpetaActual(File carpeta) {
        if (carpeta != null && carpeta.exists() && carpeta.isDirectory()) {
            carpetaActual = carpeta;
            archivoSeleccionado = null;
            archivos(carpetaActual);
        }
    }

    public void setCriterioOrden(Buscador.CriterioOrden criterioOrden) {
        this.criterioOrden = criterioOrden;
    }

    public void recargarArchivos() {
        archivos(carpetaActual);
    }

    private void initBarra() {
        JPanel contenedorSuperior = new JPanel(new BorderLayout(10, 10));
        contenedorSuperior.setBackground(Color.BLACK);
        contenedorSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        labelRuta = new JLabel("Ruta:");
        labelRuta.setForeground(Color.WHITE);
        labelRuta.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel panelBotones = new JPanel(new GridLayout(1, 9, 8, 8));
        panelBotones.setBackground(Color.BLACK);

        JButton volver = new JButton("Volver");
        JButton copiar = new JButton("Copiar");
        JButton pegar = new JButton("Pegar");
        JButton renombrar = new JButton("Renombrar");
        JButton cargar = new JButton("Cargar");
        JButton eliminar = new JButton("Eliminar");
        JButton crearCarpeta = new JButton("Crear carpeta");
        JButton importarArchivo = new JButton("Importar");
        JButton organizar = new JButton("Organizar");

        configurarBoton(volver);
        configurarBoton(copiar);
        configurarBoton(pegar);
        configurarBoton(renombrar);
        configurarBoton(cargar);
        configurarBoton(eliminar);
        configurarBoton(crearCarpeta);
        configurarBoton(importarArchivo);
        configurarBoton(organizar);

        crearCarpeta.setFont(new Font("Arial", Font.BOLD, 9));

        volver.addActionListener(e -> {
            try {
                volverCarpeta();
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });

        copiar.addActionListener(e -> {
            try {
                copiarArchivo();
                buscador.mostrarMensaje("Elemento copiado: " + archivoCopiado.getName(), false);
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });

        pegar.addActionListener(e -> {
            try {
                pegarArchivo();
                buscador.mostrarMensaje("Elemento pegado correctamente.", false);
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });

        renombrar.addActionListener(e -> pedirRenombrarArchivo());

        cargar.addActionListener(e -> {
            try {
                cargarArchivo();
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });

        eliminar.addActionListener(e -> {
            try {
                eliminarArchivo();
                buscador.mostrarMensaje("Elemento eliminado correctamente.", false);
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });

        crearCarpeta.addActionListener(e -> pedirCrearCarpetaDentro());

        importarArchivo.addActionListener(e -> {
            try {
                importarArchivoDesdePC();
                buscador.mostrarMensaje("Archivo importado correctamente.", false);
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });

        organizar.addActionListener(e -> {
            try {
                organizarCarpeta();
                buscador.mostrarMensaje("Archivos organizados correctamente.", false);
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });

        panelBotones.add(volver);
        panelBotones.add(copiar);
        panelBotones.add(pegar);
        panelBotones.add(renombrar);
        panelBotones.add(cargar);
        panelBotones.add(eliminar);
        panelBotones.add(crearCarpeta);
        panelBotones.add(importarArchivo);
        panelBotones.add(organizar);

        contenedorSuperior.add(labelRuta, BorderLayout.NORTH);
        contenedorSuperior.add(panelBotones, BorderLayout.CENTER);

        add(contenedorSuperior, BorderLayout.NORTH);
    }

    private void initLista() {
        panelListaArchivos = new JPanel();
        panelListaArchivos.setLayout(new BoxLayout(panelListaArchivos, BoxLayout.Y_AXIS));
        panelListaArchivos.setBackground(Color.DARK_GRAY);

        JScrollPane scroll = new JScrollPane(panelListaArchivos);
        scroll.setBorder(BorderFactory.createTitledBorder("Contenido"));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(scroll, BorderLayout.CENTER);
    }

    private void configurarBoton(JButton boton) {
        boton.setFont(new Font("Arial", Font.BOLD, 19));
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.BLACK);
        boton.setFocusPainted(false);
    }

    public void archivos(File file) {
        panelListaArchivos.removeAll();
        grupoArchivos = new ButtonGroup();

        if (file == null || !file.exists() || !file.isDirectory()) {
            buscador.mostrarMensaje("La carpeta actual no es válida.", true);
            refrescarLista();
            return;
        }

        carpetaActual = file;
        labelRuta.setText("Ruta: " + carpetaActual.getPath());

        File[] archivos = file.listFiles();

        if (archivos == null || archivos.length == 0) {
            JLabel vacio = new JLabel("Esta carpeta no contiene archivos.");
            vacio.setForeground(Color.WHITE);
            vacio.setFont(new Font("Arial", Font.BOLD, 14));
            panelListaArchivos.add(vacio);
            refrescarLista();
            return;
        }

        Arrays.sort(archivos, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;

            switch (criterioOrden) {
                case FECHA:
                    return Long.compare(b.lastModified(), a.lastModified());
                case TIPO:
                    String tipoA = a.isDirectory() ? "" : obtenerTipoArchivo(a).toLowerCase();
                    String tipoB = b.isDirectory() ? "" : obtenerTipoArchivo(b).toLowerCase();
                    int cmpTipo = tipoA.compareToIgnoreCase(tipoB);
                    if (cmpTipo != 0) return cmpTipo;
                    return a.getName().compareToIgnoreCase(b.getName());
                case TAMANIO:
                    return Long.compare(b.length(), a.length());
                case NOMBRE:
                default:
                    return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        for (File archivo : archivos) {
            agregarBotonArchivo(archivo);
        }

        refrescarLista();
    }

    private void agregarBotonArchivo(File archivo) {
        String nombre = archivo.getName();

        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(archivo.lastModified());

        String fecha = String.format(
                "%02d/%02d/%04d %02d:%02d:%02d",
                calendario.get(Calendar.DAY_OF_MONTH),
                calendario.get(Calendar.MONTH) + 1,
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.HOUR_OF_DAY),
                calendario.get(Calendar.MINUTE),
                calendario.get(Calendar.SECOND)
        );

        String tipo = archivo.isDirectory() ? "Carpeta" : obtenerTipoArchivo(archivo);
        long tamanio = archivo.length();

        String texto = nombre + " | Fecha: " + fecha
                + " | Tipo: " + tipo
                + " | Tamaño: " + tamanio + " bytes";

        JToggleButton boton = new JToggleButton(texto);
        boton.setPreferredSize(new Dimension(700, 90));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        boton.setMinimumSize(new Dimension(700, 90));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setVerticalAlignment(SwingConstants.CENTER);
        boton.setBackground(Color.BLACK);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);

        grupoArchivos.add(boton);

        boton.addActionListener(e -> {
            if (boton.isSelected()) {
                archivoSeleccionado = archivo;
            }
        });

        panelListaArchivos.add(boton);
        panelListaArchivos.add(Box.createVerticalStrut(8));
    }

    private String obtenerTipoArchivo(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');

        if (punto > 0 && punto < nombre.length() - 1) {
            return nombre.substring(punto + 1);
        }

        return "Sin extensión";
    }

    private void organizarCarpeta() throws BuscadorException {
        if (carpetaActual == null || !carpetaActual.isDirectory()) {
            throw new BuscadorException("La carpeta actual no es válida.");
        }

        File[] archivos = carpetaActual.listFiles();
        if (archivos == null) {
            throw new BuscadorException("No se pudo leer la carpeta.");
        }

        File carpetaImagenes = new File(carpetaActual, "Imagenes");
        File carpetaDocumentos = new File(carpetaActual, "Documentos");
        File carpetaMusica = new File(carpetaActual, "Musica");

        carpetaImagenes.mkdirs();
        carpetaDocumentos.mkdirs();
        carpetaMusica.mkdirs();

        for (File archivo : archivos) {
            if (!archivo.isFile()) continue;

            String extension = obtenerTipoArchivo(archivo).toLowerCase();
            File destino = null;

            if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
                destino = new File(carpetaImagenes, archivo.getName());
            } else if (extension.equals("txt") || extension.equals("pdf") || extension.equals("doc") || extension.equals("docx")) {
                destino = new File(carpetaDocumentos, archivo.getName());
            } else if (extension.equals("mp3") || extension.equals("wav")) {
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

        archivos(carpetaActual);
        buscador.recargarArbol();
    }

    private void volverCarpeta() throws BuscadorException {
        if (carpetaActual == null) {
            throw new BuscadorException("No hay carpeta actual.");
        }

        File padre = carpetaActual.getParentFile();

        if (padre == null) {
            throw new BuscadorException("Ya estás en la carpeta superior disponible.");
        }

        try {
            Path padrePath = padre.getCanonicalFile().toPath();
            Path basePath = carpetaBase.getCanonicalFile().toPath();

            if (!padrePath.startsWith(basePath)) {
                throw new BuscadorException("Ya estás en la carpeta base.");
            }
        } catch (IOException e) {
            throw new BuscadorException("No es posible verificar la carpeta padre.");
        }

        carpetaActual = padre;
        archivoSeleccionado = null;
        archivos(carpetaActual);
    }

    private void copiarArchivo() throws BuscadorException {
        if (archivoSeleccionado == null) {
            throw new BuscadorException("Selecciona un archivo o carpeta para copiar.");
        }

        archivoCopiado = archivoSeleccionado;
    }

    private void pegarArchivo() throws BuscadorException {
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

            archivos(carpetaActual);
            buscador.recargarArbol();

        } catch (IOException ex) {
            throw new BuscadorException("Error al pegar: " + ex.getMessage());
        }
    }

    private void pedirRenombrarArchivo() {
        if (archivoSeleccionado == null) {
            buscador.mostrarMensaje("Selecciona un archivo o carpeta para renombrar.", true);
            return;
        }

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

    private void renombrarArchivo() throws BuscadorException {
        String nuevoNombre = buscador.getTextoEntrada();

        if (nuevoNombre.isEmpty()) {
            throw new BuscadorException("El nuevo nombre no puede estar vacío.");
        }

        File nuevoArchivo = new File(archivoSeleccionado.getParentFile(), nuevoNombre);

        if (nuevoArchivo.exists()) {
            throw new BuscadorException("Ya existe otro elemento con ese nombre.");
        }

        try {
            Files.move(archivoSeleccionado.toPath(), nuevoArchivo.toPath(), StandardCopyOption.REPLACE_EXISTING);
            archivoSeleccionado = nuevoArchivo;
            archivos(carpetaActual);
            buscador.recargarArbol();
        } catch (IOException e) {
            throw new BuscadorException("No se pudo renombrar el elemento.");
        }
    }

    private void cargarArchivo() throws BuscadorException {
        if (archivoSeleccionado == null) {
            throw new BuscadorException("Selecciona un elemento para cargar.");
        }

        if (archivoSeleccionado.isDirectory()) {
            carpetaActual = archivoSeleccionado;
            archivoSeleccionado = null;
            archivos(carpetaActual);
            buscador.mostrarMensaje("Carpeta cargada: " + carpetaActual.getName(), false);
            return;
        }

        String nombre = archivoSeleccionado.getName().toLowerCase();

        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".png")) {
            buscador.abrirArchivoEnVisualizador(archivoSeleccionado);
            return;
        }else if (nombre.endsWith(".wrd") ){
            buscador.abrirArchivoEnWord(archivoSeleccionado);
            return;
        }

        throw new BuscadorException("No es valido para cargar");
    }

    private void eliminarArchivo() throws BuscadorException {
        if (archivoSeleccionado == null) {
            throw new BuscadorException("Selecciona un archivo o carpeta para eliminar.");
        }

        boolean eliminado = eliminarRecursivo(archivoSeleccionado);

        if (eliminado) {
            archivoSeleccionado = null;
            archivos(carpetaActual);
            buscador.recargarArbol();
        } else {
            throw new BuscadorException("No se pudo eliminar el elemento.");
        }
    }

    private void pedirCrearCarpetaDentro() {
        buscador.mostrarEntrada("Nueva carpeta:", () -> {
            try {
                crearCarpetaDentro();
                buscador.mostrarMensaje("Carpeta creada correctamente.", false);
                buscador.ocultarEntrada();
            } catch (BuscadorException ex) {
                buscador.mostrarMensaje(ex.getMessage(), true);
            }
        });
    }

    private void crearCarpetaDentro() throws BuscadorException {
        String nombre = buscador.getTextoEntrada();

        if (nombre.isEmpty()) {
            throw new BuscadorException("Debes escribir un nombre válido.");
        }

        File nuevaCarpeta = new File(carpetaActual, nombre);

        if (nuevaCarpeta.exists()) {
            throw new BuscadorException("Ya existe una carpeta con ese nombre.");
        }

        if (nuevaCarpeta.mkdir()) {
            archivos(carpetaActual);
            buscador.recargarArbol();
        } else {
            throw new BuscadorException("No se pudo crear la carpeta.");
        }
    }

   private void importarArchivoDesdePC() throws BuscadorException {
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

            archivos(carpetaActual);
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

    private void copiarDirectorio(File origen, File destino) throws IOException {
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