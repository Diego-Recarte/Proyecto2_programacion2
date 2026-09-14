/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

import Logica.RutasSistema;

/**
 *
 * @author denam
 */

import javazoom.jl.player.advanced.AdvancedPlayer;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;

public class Buscador extends JDialog {

    public JTree arbolArchivos;
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
        setSize(900, 600);
        setLocationRelativeTo(perfil);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);

        if (usuarioWinActivo.isAdmin) {
            carpetaBase = RutasSistema.USUARIOS;
        } else {
            carpetaBase = RutasSistema.usuario(usuarioWinActivo.nombre);
        }

        if (!carpetaBase.exists()) {
            carpetaBase.mkdirs();
        }

        initExplorador();
        initPanelSuperior();
        initArbolIzquierdo();

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

        JPanel panelOrden = new JPanel();
        panelOrden.setLayout(new BoxLayout(panelOrden, BoxLayout.X_AXIS));
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

        initBotones(panelOrden);

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

    private void initBotones(JPanel panel) {

        JButton copiar = new JButton("Copiar");
        JButton pegar = new JButton("Pegar");
        JButton renombrar = new JButton("Renombrar");
        JButton cargar = new JButton("Cargar");
        JButton eliminar = new JButton("Eliminar");
        JButton crearCarpeta = new JButton("Crear carpeta");
        JButton importarArchivo = new JButton("Importar");
        JButton organizar = new JButton("Organizar");

        configurarBoton(copiar);
        configurarBoton(pegar);
        configurarBoton(renombrar);
        configurarBoton(cargar);
        configurarBoton(eliminar);
        configurarBoton(crearCarpeta);
        configurarBoton(importarArchivo);
        configurarBoton(organizar);

        crearCarpeta.setFont(new Font("Arial", Font.BOLD, 9));

        copiar.addActionListener(e -> {
            try {
                exploradorPanel.copiarArchivo();
                mostrarMensaje("Elemento copiado: " + exploradorPanel.archivoCopiado.getName(), false);
            } catch (BuscadorException ex) {
                mostrarMensaje(ex.getMessage(), true);
            }
        });

        pegar.addActionListener(e -> {
            try {
                exploradorPanel.pegarArchivo();
                mostrarMensaje("Elemento pegado correctamente.", false);
            } catch (BuscadorException ex) {
                mostrarMensaje(ex.getMessage(), true);
            }
        });

        renombrar.addActionListener(e -> exploradorPanel.pedirRenombrarArchivo());

        cargar.addActionListener(e -> {
            try {
                exploradorPanel.cargarArchivo();
            } catch (BuscadorException ex) {
                mostrarMensaje(ex.getMessage(), true);
            }
        });

        eliminar.addActionListener(e -> {
            try {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();

                if (nodo == null) {
                    return;
                }

                File archivo = (File) nodo.getUserObject();
                exploradorPanel.eliminarArchivo(archivo);
                mostrarMensaje("Elemento eliminado correctamente.", false);
            } catch (BuscadorException ex) {
                mostrarMensaje(ex.getMessage(), true);
            }
        });

        crearCarpeta.addActionListener(e -> {

            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();

            if (nodo == null) {
                return;
            }

            File archivo = (File) nodo.getUserObject();
            exploradorPanel.pedirCrearCarpetaDentro(archivo);

        });

        importarArchivo.addActionListener(e -> {
            try {
                exploradorPanel.importarArchivoDesdePC();
                mostrarMensaje("Archivo importado correctamente.", false);
            } catch (BuscadorException ex) {
                mostrarMensaje(ex.getMessage(), true);
            }
        });

        organizar.addActionListener(e -> {
            
            
            organizar.setEnabled(false);
            mostrarMensaje("Ordenando archivos...",false);
            Thread hiloOrganizador = new Thread (()->{
                try {
                exploradorPanel.organizarCarpeta();
                SwingUtilities.invokeLater(() ->{
                 mostrarMensaje("Archivos organizados correctamente.", false);
                 organizar.setEnabled(true);
                
                });
               
                
                
                
                
                } catch (BuscadorException ex) {
                   SwingUtilities.invokeLater(() ->{
                    mostrarMensaje(ex.getMessage(), true);
                    organizar.setEnabled(true);

                   });
                    
                }
            });
             hiloOrganizador.start();
            
        });

        panel.add(copiar);
        panel.add(pegar);
        panel.add(renombrar);
        panel.add(cargar);
        panel.add(eliminar);
        panel.add(crearCarpeta);
        panel.add(importarArchivo);
        panel.add(organizar);
    }

    private void configurarBotonBarra(JButton boton) {
        boton.setFont(new Font("Arial", Font.BOLD, 6));
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.BLACK);
        boton.setFocusPainted(false);
    }

    private void initArbolIzquierdo() {
        nodoRaiz = new DefaultMutableTreeNode(carpetaBase);
        modeloArbol = new DefaultTreeModel(nodoRaiz);

        arbolArchivos = new JTree(modeloArbol) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {

                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                Object obj = nodo.getUserObject();

                if (obj instanceof File archivo) {
                    String ruta = archivo.getPath().replace("\\", "/");

                    int prefijo = ruta.lastIndexOf("/");

                    return ruta.substring(prefijo + 1);
                }

                return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
            }

        };

        arbolArchivos.setRootVisible(true);
        arbolArchivos.setShowsRootHandles(true);
        arbolArchivos.setBackground(Color.BLACK);
        arbolArchivos.setForeground(Color.WHITE);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                Object obj = nodo.getUserObject();

                if (obj instanceof File archivo) {
                    if (archivo.isDirectory()) {
                        setIcon(expanded ? getDefaultOpenIcon() : getDefaultClosedIcon());
                    } else {
                        setIcon(getDefaultLeafIcon());
                    }
                }

                setBackgroundNonSelectionColor(Color.BLACK);
                setTextNonSelectionColor(Color.WHITE);
                setTextSelectionColor(Color.WHITE);

                return this;
            }
        };

        arbolArchivos.setCellRenderer(renderer);

        arbolArchivos.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
            if (nodo == null) return;

            File archivo = (File) nodo.getUserObject();
            carpetaSeleccionada = archivo;

            exploradorPanel.setCriterioOrden(criterioActual);
            exploradorPanel.mostrarArchivoSeleccionado(archivo);

            if (archivo.isDirectory()) {
                mostrarMensaje("Carpeta seleccionada: " + archivo.getName(), false);
            } else {
                mostrarMensaje("Archivo seleccionado: " + archivo.getName(), false);
            }
        });

        JScrollPane scrollArbol = new JScrollPane(arbolArchivos);
        scrollArbol.setPreferredSize(new Dimension(600, 800));
        scrollArbol.setMinimumSize(new Dimension(600, 800));
        scrollArbol.setMaximumSize(new Dimension(600, 800));
        scrollArbol.setBorder(BorderFactory.createTitledBorder("Explorador"));

        add(scrollArbol, BorderLayout.CENTER);

        cargarArbolEnThread(false);
    }

    private void cargarArbolEnThread(boolean restaurarExpansion) {
        mostrarMensaje("Cargando árbol de archivos...", false);

        List<String> rutasExpandidas = restaurarExpansion ? obtenerRutasExpandidas() : new ArrayList<>();

        Thread hiloArbol = new Thread(() -> {
            DefaultMutableTreeNode nuevaRaiz = crearNodoArchivo(carpetaBase);

            SwingUtilities.invokeLater(() -> {
                nodoRaiz = nuevaRaiz;
                modeloArbol.setRoot(nodoRaiz);
                modeloArbol.reload();

                if (restaurarExpansion) {
                    restaurarRutasExpandidas(rutasExpandidas);
                }

               
            });
        });

        hiloArbol.setName("Hilo-Arbol-Buscador");
        hiloArbol.start();
    }

    private List<String> obtenerRutasExpandidas() {
        List<String> rutas = new ArrayList<>();

        TreePath raizPath = new TreePath(modeloArbol.getRoot());
        Enumeration<TreePath> expandidas = arbolArchivos.getExpandedDescendants(raizPath);

        if (expandidas != null) {
            while (expandidas.hasMoreElements()) {
                TreePath path = expandidas.nextElement();
                String ruta = convertirTreePathARuta(path);
                if (ruta != null) {
                    rutas.add(ruta);
                }
            }
        }

        return rutas;
    }

    private String convertirTreePathARuta(TreePath path) {
        Object[] nodos = path.getPath();
        StringBuilder sb = new StringBuilder();

        for (Object obj : nodos) {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) obj;
            Object userObject = nodo.getUserObject();

            if (userObject instanceof File archivo) {
                sb.append(archivo.getAbsolutePath()).append("||");
            }
        }

        return sb.toString();
    }

    private void restaurarRutasExpandidas(List<String> rutasExpandidas) {
        if (rutasExpandidas == null || rutasExpandidas.isEmpty()) {
            return;
        }

        for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
            arbolArchivos.collapseRow(i);
        }

        expandirNodosGuardados(nodoRaiz, new TreePath(nodoRaiz), rutasExpandidas);
    }

    private void expandirNodosGuardados(DefaultMutableTreeNode nodo, TreePath pathActual, List<String> rutasExpandidas) {
        String rutaActual = convertirTreePathARuta(pathActual);

        if (rutasExpandidas.contains(rutaActual)) {
            arbolArchivos.expandPath(pathActual);
        }

        Enumeration<?> hijos = nodo.children();
        while (hijos.hasMoreElements()) {
            DefaultMutableTreeNode hijo = (DefaultMutableTreeNode) hijos.nextElement();
            expandirNodosGuardados(hijo, pathActual.pathByAddingChild(hijo), rutasExpandidas);
        }
    }

    private DefaultMutableTreeNode crearNodoArchivo(File archivo) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(archivo);

        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();

            if (hijos != null) {

                if (carpetaSeleccionada != null && archivo.equals(carpetaSeleccionada)) {
                    java.util.Arrays.sort(hijos, (a, b) -> {
                        switch (criterioActual) {
                            case NOMBRE:
                                return a.getName().compareToIgnoreCase(b.getName());

                            case FECHA:
                                return Long.compare(a.lastModified(), b.lastModified());

                            case TIPO:
                                String tipoA = a.isDirectory() ? "Carpeta" : obtenerTipoArchivo(a);
                                String tipoB = b.isDirectory() ? "Carpeta" : obtenerTipoArchivo(b);
                                return tipoA.compareToIgnoreCase(tipoB);

                            case TAMANIO:
                                long tamA = a.isFile() ? a.length() : 0;
                                long tamB = b.isFile() ? b.length() : 0;
                                return Long.compare(tamA, tamB);

                            default:
                                return a.getName().compareToIgnoreCase(b.getName());
                        }
                    });
                }

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
        add(exploradorPanel, BorderLayout.EAST);
    }

    private void cambiarOrden(){
        String seleccion = (String) comboOrden.getSelectedItem();
        if (seleccion == null) {
            try{
                throw new BuscadorException("El archivo seleccionado no es un documento válido.");
            }catch (BuscadorException e){
                
            }
            
            
        }
     
        

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
            recargarArbol();
        }

        mostrarMensaje("Orden aplicado: " + seleccion, false);
    }

    public void recargarArbol() {
        cargarArbolEnThread(true);
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

        if (!nombre.endsWith(".pwrd")) {
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

    private String obtenerTipoArchivo(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');

        if (punto > 0 && punto < nombre.length() - 1) {
            return nombre.substring(punto + 1);
        }

        return "Sin extensión";
    }
}