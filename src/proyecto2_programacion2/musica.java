/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */


import java.io.*;
import java.nio.file.Files;
import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class musica implements Serializable {
    private String nombre;
    private String artista;
    private String album;
    private String descripcion;
    private byte[] audioMp3;
    private byte[] caratula;
    private String extensionCaratula;

    public musica(File archivoCancion) throws IOException {
        this.nombre = archivoCancion.getName();
        this.artista = "";
        this.album = "";
        this.descripcion = "";
        this.audioMp3 = Files.readAllBytes(archivoCancion.toPath());
        this.caratula = null;
        this.extensionCaratula = "";
    }

    public String getNombre() {
        return nombre;
    }

    public String getArtista() {
        return artista;
    }

    public String getAlbum() {
        return album;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public byte[] getAudioMp3() {
        return audioMp3;
    }

    public byte[] getCaratula() {
        return caratula;
    }

    public String getExtensionCaratula() {
        return extensionCaratula;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

   

    public void setExtensionCaratula(String extensionCaratula) {
        this.extensionCaratula = extensionCaratula;
    }
    
    public File guardarComoMP5(String rutaCarpeta) throws IOException {
        File archivo = new File(rutaCarpeta, nombre + ".mp5");

        try (FileOutputStream fos = new FileOutputStream(archivo);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);
        }

        return archivo;
    }
    public ImageIcon obtenerCaratulaComoIcono() {
        if (caratula == null || caratula.length == 0) {
            return null;
        }

        return new ImageIcon(caratula);
    }
    
    public void setCaratula(ImageIcon icon) throws IOException {
        if (icon == null) {
            return;
        }else{

        int ancho = icon.getIconWidth();
        int alto = icon.getIconHeight();

        BufferedImage imagen = new BufferedImage(ancho,alto,BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = imagen.createGraphics();

        graphics.drawImage( icon.getImage(),0, 0, null);

        graphics.dispose();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        
            
        ImageIO.write(imagen, "png", salida);
        
        caratula = salida.toByteArray();
        }
      
        
    }

}
