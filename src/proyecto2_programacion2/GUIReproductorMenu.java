/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;

public class GUIReproductorMenu extends JPanel {

    private final GUIReproductor padre;
    private musica cancion;
    private File archivo;

    private final Object bloqueoAudio = new Object();


    private JButton botonReproducir;
    private JLabel estado;
    private JLabel tiempoActual;
    private JLabel tiempoTotal;
    private JSlider barraProgreso;
    private Timer temporizadorProgreso;

    private volatile AdvancedPlayer reproductorMp3;
    private volatile JavaSoundAudioDevice dispositivoMp3;
    private volatile Clip reproductorWav;
    private volatile boolean reproduciendo;
    private volatile long posicionMs;
    private volatile long posicionBaseMp3Ms;
    private volatile long generacionAudio;

    private long duracionMs;
    private long[] tiemposFramesMp3 = new long[0];
    private boolean actualizandoBarra;

    public GUIReproductorMenu(GUIReproductor padre, File archivo) throws IOException, ClassNotFoundException {
        this.padre = padre;
        this.cancion= cargarCancion (archivo);
        this.archivo = archivo;

        


        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        construirInterfaz(padre);
       
    }
     private musica cargarCancion(File archivo) throws IOException, ClassNotFoundException {
        if (archivo == null || !archivo.isFile()) {
            throw new IOException("La canción seleccionada no existe.");
        }
        if (archivo.getName().toLowerCase().endsWith(".mp5")) {
            return abrirMP5(archivo);
        }
        return new musica(archivo);
    }
     
      public static musica abrirMP5(File archivoMP5) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(archivoMP5);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object contenido = ois.readObject();
            if (!(contenido instanceof musica musicaLeida)) {
                throw new IOException("El archivo MP5 no contiene una canción válida.");
            }
            return musicaLeida;
        }
    }

  
    


   
    private void construirInterfaz(GUIReproductor padre) throws FileNotFoundException,IOException {
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        JTextField titulo = crearEtiqueta(cancion.getNombre(), 24, Font.BOLD, Color.WHITE);
        JTextField artista = crearEtiqueta(textoOAlternativa(cancion.getArtista(), "Artista desconocido"),14, Font.PLAIN, Color.LIGHT_GRAY);
        JTextField album = crearEtiqueta(textoOAlternativa(cancion.getAlbum(), "Álbum desconocido"), 12, Font.PLAIN, Color.GRAY);

        JButton caratula = new JButton("♪");
        caratula.setAlignmentX(CENTER_ALIGNMENT);
        caratula.setPreferredSize(new Dimension(320, 270));
        caratula.setMinimumSize(new Dimension(320, 270));
        caratula.setMaximumSize(new Dimension(320, 270));
        caratula.setOpaque(true);
        caratula.setBackground(new Color(35, 35, 35));
        caratula.setForeground(new Color(220, 65, 45));
        caratula.setFont(new Font("SansSerif", Font.BOLD, 110));

        byte[] bytesCaratula = cancion.getCaratula();
        if (bytesCaratula != null && bytesCaratula.length > 0) {
            ImageIcon original = new ImageIcon(bytesCaratula);
            Image escalada = original.getImage().getScaledInstance(320, 270, Image.SCALE_SMOOTH);
            caratula.setText("");
            caratula.setIcon(new ImageIcon(escalada));
        }
        
        caratula.addActionListener(ev->{
        
            File carpetaBase;
                    if (usuarioWinActivo.isAdmin){
                        
                     carpetaBase = new File( "src/datos/windows/Z/infoUsuarios" );
                    }else{
                     carpetaBase = new File( "src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre  );
                    }

                    GUISelector selector = new GUISelector(SwingUtilities.getWindowAncestor(this),carpetaBase,"png", "jpg", "jpeg" );

                    selector.setVisible(true);

                    File archivoSeleccionado = selector.getArchivoSeleccionado();
                    
                    ImageIcon original = new ImageIcon((archivoSeleccionado.getAbsolutePath()));
                    Image escalada = original.getImage().getScaledInstance(320, 270, Image.SCALE_SMOOTH);
                    caratula.setText("");
                    caratula.setIcon(new ImageIcon(escalada));

        });
        
        
        
        
        
        

        JTextArea descripcion = new JTextArea(textoOAlternativa(cancion.getDescripcion(), "Sin descripción"));
        descripcion.setEditable(true);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        descripcion.setRows(2);
        descripcion.setOpaque(false);
        descripcion.setForeground(Color.red);
        descripcion.setBackground(Color.black);
        
        descripcion.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scrollDescripcion = new JScrollPane(descripcion);
        scrollDescripcion.setAlignmentX(CENTER_ALIGNMENT);
        scrollDescripcion.setBackground(Color.BLACK);
        scrollDescripcion.setOpaque(false);
        scrollDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        scrollDescripcion.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55)));
        
        JButton confirmar = new JButton("Confirmar Cambio");

        confirmar.setFont(new Font("Arial", Font.BOLD, 16));
        confirmar.setPreferredSize(new Dimension(300, 50));
        confirmar.setMaximumSize(new Dimension(300, 50));

        confirmar.setForeground(Color.WHITE);
        confirmar.setBackground(Color.RED);

        confirmar.setFocusPainted(false);
        confirmar.setBorderPainted(false);
        
        confirmar.setAlignmentX(CENTER_ALIGNMENT);

        confirmar.setHorizontalAlignment(SwingConstants.CENTER);

        confirmar.addActionListener(e -> {
            
            
            
            if (noExiste(titulo.getText())){
                
            
            
                ImageIcon imageIcon = (ImageIcon) caratula.getIcon();



              try{


               confirmar(titulo.getText().trim(), artista.getText().trim() , album.getText().trim(), descripcion.getText(), imageIcon);
              }catch (FileNotFoundException ex){

               }catch ( IOException ex){

               }
            }
            
            

        });
        
        JButton boton = new JButton("Volver");

        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setPreferredSize(new Dimension(500, 35));
        boton.setMaximumSize(new Dimension(500, 35));

        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.RED);

        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
       
      

        boton.setHorizontalAlignment(SwingConstants.CENTER);

        boton.addActionListener(e -> {
            padre.mostrarReproductor();
        });
        
        add( boton, BorderLayout.SOUTH);



        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(artista);
        contenido.add(album);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(caratula);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(scrollDescripcion);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(confirmar);
        
        
        
       

        add(contenido, BorderLayout.CENTER);
    }
    
    private boolean noExiste(String nombre){
        File[] archivos = archivo.getParentFile().listFiles();
        for (File arch : archivos){
            if (arch.getName().equals(nombre.trim()+".mp5")){
                if (!archivo.equals(arch)){
                    return false;
                }
                
            }
        }
        return true;
    }

   

    private JTextField crearEtiqueta(String texto, int tamano, int estilo, Color color) {
        JTextField etiqueta = new JTextField(texto, SwingConstants.CENTER);
        etiqueta.setFont(new Font("Arial", estilo, tamano));
        etiqueta.setForeground(color);
        etiqueta.setOpaque(false);
        etiqueta.setAlignmentX(CENTER_ALIGNMENT);
        return etiqueta;
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(135, 38));
        boton.setBackground(new Color(210, 50, 35));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        return boton;
    }

    private String textoOAlternativa(String texto, String alternativa) {
        return texto == null || texto.isBlank() ? alternativa : texto;
    }
    
    private void confirmar(String titulo, String autor, String album, String descripcion, ImageIcon imagen)throws IOException {

            cancion.setAlbum(album);
            cancion.setNombre(titulo);
            cancion.setArtista(autor);
            cancion.setDescripcion(descripcion);

            if (imagen != null) {
                cancion.setCaratula(imagen);
            }

            File nuevo = new File(
                    archivo.getParentFile().getAbsolutePath() + "/" + cancion.getNombre() + ".mp5"
            );

            if (!archivo.equals(nuevo)) {
                Files.move(archivo.toPath(), nuevo.toPath());
                archivo = nuevo;
            }

            try (FileOutputStream fos = new FileOutputStream(archivo, false);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                oos.writeObject(cancion);
                oos.flush();
            }

            padre.actualizarDespuesDeEditar(archivo);
    }
}
        

    

        
        
        
        
    
    

    
