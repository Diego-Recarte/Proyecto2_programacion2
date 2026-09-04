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
import java.io.*;
import java.util.*;

public class GUIReproductor extends JDialog{
     private CardLayout cardLayout;
    private JPanel panelCards;
    private JPanel panelcanciones ;
    private File carpetaBase;
    private File carpeta;
    private File cancion;
    private JLabel tituloCarpeta;
    private ButtonGroup grupoArchivos;
    
    
    GUIReproductor(GUIPantallaPrincipal Perfil, File cancion){
        
    
    super(Perfil, "Reproductor", false);
    
    
        if (usuarioWinActivo.isAdmin) {
            carpetaBase = new File("./src/datos/windows/Z/infoUsuarios");
        } else {
            carpetaBase = new File("./src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre);
        }
        
        this.cancion= cancion;
        this.carpeta= cancion.getParentFile();


        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 800);
      setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.black);
        setLocationRelativeTo(Perfil);
        
        
        InitCardLayout();
        agregarCards();
        mostrarCard("nuevo");
        setVisible(true);
        
       

    }
     public void agregarCards(){
        GUIWordEditor editor = new GUIWordEditor(this, cardLayout, panelCards);
        agregarCard(editor, "editor");
        
        GUIWordNuevo nuevo = new GUIWordNuevo (this, cardLayout, panelCards, editor);
        agregarCard(nuevo, "nuevo");
        
        
        
        
        
        

        
    }
    
    public void InitCardLayout(){
    cardLayout = new CardLayout(); 
    panelCards = new JPanel(cardLayout); 
    
    
    panelCards.setOpaque(false);
     
 
    getContentPane().add(panelCards, BorderLayout.CENTER);
    
    



    }
    private void agregarCard(JPanel panel, String nombre) {
        panelCards.add(panel, nombre);
    }
    public void mostrarCard(String nombreCard) { 
        cardLayout.show(panelCards, nombreCard); 
        panelCards.revalidate();
        panelCards.repaint(); 
    }
    
    private void initLista(){
        JPanel listaPanel = new JPanel();
        listaPanel.setPreferredSize(new Dimension(150, 800));
        listaPanel.setMinimumSize(new Dimension(150, 800));
        listaPanel.setMaximumSize(new Dimension(150, 800));
        listaPanel.setOpaque(false);
        
        
        JPanel paneleleccion = new JPanel ();
        paneleleccion .setPreferredSize(new Dimension(150, 100));
        paneleleccion .setMinimumSize(new Dimension(150, 100));
        paneleleccion .setMaximumSize(new Dimension(150, 100));
        paneleleccion.setBackground(Color.gray);
        paneleleccion.setLayout(new BoxLayout( paneleleccion, BoxLayout.Y_AXIS));
        paneleleccion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        panelcanciones = new JPanel ();
        panelcanciones .setPreferredSize(new Dimension(330, 100));
        panelcanciones .setMinimumSize(new Dimension(330, 100));
        panelcanciones .setMaximumSize(new Dimension(330, 100));
        panelcanciones.setBackground(Color.black);
        panelcanciones.setLayout(new BoxLayout( paneleleccion, BoxLayout.Y_AXIS));
        panelcanciones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        JScrollPane scroll = new JScrollPane(panelcanciones);
        scroll.setPreferredSize(new Dimension(330, 100));
        scroll.setBorder(BorderFactory.createTitledBorder("Explorador"));
        
        tituloCarpeta = new JLabel(carpeta.getName());
        tituloCarpeta.setFont(new Font("Arial", Font.BOLD, 14));
        tituloCarpeta.setForeground(Color.BLACK);
        tituloCarpeta.setOpaque(false);
        tituloCarpeta.setHorizontalAlignment(SwingConstants.CENTER);
        tituloCarpeta.setPreferredSize(new Dimension(50, 100));
        paneleleccion.add(tituloCarpeta);
        
        
        JButton abrirCarpeta = new JButton ("Buscar en otra carpeta");
        abrirCarpeta .setFont(new Font("Arial", Font.BOLD, 14));
        abrirCarpeta .setPreferredSize(new Dimension(50, 100));
        abrirCarpeta .setMaximumSize(new Dimension(50, 100));
        abrirCarpeta .setMinimumSize(new Dimension(50, 100));
        abrirCarpeta .setForeground(Color.WHITE);
        abrirCarpeta .setBackground(Color.RED);
        abrirCarpeta .setFocusPainted(false);
        abrirCarpeta .setBorderPainted(false);
        abrirCarpeta .setContentAreaFilled(false);
        abrirCarpeta .setHorizontalAlignment(SwingConstants.CENTER);
        abrirCarpeta .addActionListener(e -> {
            buscarOtraCarpeta();
            
            
        });
        paneleleccion.add(Box.createHorizontalStrut(40));
        paneleleccion.add(abrirCarpeta);


        
        listaPanel.add(paneleleccion);
        listaPanel.add(scroll);
        
        
        
        
        
        
        
        
        
        
        add(listaPanel, BorderLayout.WEST);
        
        
        
        

        
    }
    
      public void archivos(File file) {
        panelcanciones.removeAll();
        grupoArchivos = new ButtonGroup();
        file = carpeta;

        

        
        tituloCarpeta.setText( file.getName());

        File[] archivos = file.listFiles();

        if (archivos == null || archivos.length == 0) {
            JLabel vacio = new JLabel("Esta carpeta no contiene archivos.");
            vacio.setForeground(Color.WHITE);
            vacio.setFont(new Font("Arial", Font.BOLD, 14));
            panelcanciones.add(vacio);
            refrescarLista();
            return;
        }

        

        for (File archivo : archivos) {
            agregarBotonArchivo(archivo);
        }

        refrescarLista();
    }

    private void agregarBotonArchivo(File archivo) {
        String nombre = archivo.getName();

       
      
        long tamanio = archivo.length();

        String texto = nombre + " | Tamaño: " + tamanio + " bytes";

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
            
        });

        panelcanciones.add(boton);
        panelcanciones.add(Box.createVerticalStrut(8));
    }
     private void refrescarLista() {
        panelcanciones.revalidate();
        panelcanciones.repaint();
    }
     private void buscarOtraCarpeta() {
        File carpetaInicial;

        
        GUISelector selector = new GUISelector(this, carpetaBase, "mp5");

        selector.setVisible(true);

        File seleccionado = selector.getArchivoSeleccionado();

        if (seleccionado != null) {
            carpeta = seleccionado.getParentFile();
            abrirImagen(seleccionado);
        }
    }
     
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
