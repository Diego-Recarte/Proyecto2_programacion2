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
import javax.swing.*;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.text.*;
import java.io.File;
import java.util.Arrays;
import java.io.RandomAccessFile;

public class WordArchivos {
    private static final Object LOCK_ARCHIVOS = new Object();
    
    
    public static boolean guardarComo(JTextPane editor, File archivo, String nombre, boolean isGuardarComo) throws IOException{
        
        synchronized( LOCK_ARCHIVOS ){
            ArrayList<wordFragmento> fragmentos = extraerFragmentos(editor);

            if ((isGuardarComo && !archivo.exists()) ||(!isGuardarComo && archivo.exists()) ){



               try (RandomAccessFile raf =new RandomAccessFile(archivo, "rw")) {

                   raf.setLength(0);
                   raf.writeUTF(nombre);
                   raf.writeInt(fragmentos.size());

                   for (wordFragmento f : fragmentos) {
                       raf.writeUTF(f.getTexto());
                       raf.writeUTF(f.getFuente());
                       raf.writeInt(f.getTamano());
                       raf.writeInt(f.getColor().getRGB());

                   }

                   return true;
               }
            }else{
                return false;
            }
        }
     
        
        
    }
    private static ArrayList<wordFragmento> extraerFragmentos(JTextPane editor) {
        ArrayList<wordFragmento> lista = new ArrayList<>();

        try {
            StyledDocument doc = editor.getStyledDocument();
            int longitud = doc.getLength();
            int i = 0;

            while (i < longitud) {
                Element elemento = doc.getCharacterElement(i);
                int inicio = elemento.getStartOffset();
                int fin = Math.min(elemento.getEndOffset(), longitud);

                AttributeSet attr = elemento.getAttributes();

                String texto = doc.getText(inicio, fin - inicio);
                String fuente = StyleConstants.getFontFamily(attr);
                int tamano = StyleConstants.getFontSize(attr);
                Color color = StyleConstants.getForeground(attr);
              

                lista.add(new wordFragmento(texto, fuente, tamano, color  ));

                i = fin;
            }
        } catch (Exception e) {
         
        }

        return lista;
    }
    
    
    public static boolean guardar(JTextPane editor, File archivo, String nombre, boolean isGuardarComo) throws IOException{
         ArrayList<wordFragmento> fragmentos = extraerFragmentos(editor);
         
         if (archivo.exists()){
             

            try (RandomAccessFile raf =new RandomAccessFile(archivo, "rw")) {

                raf.setLength(0);
                raf.writeUTF(nombre);
                raf.writeInt(fragmentos.size());

                for (wordFragmento f : fragmentos) {
                    raf.writeUTF(f.getTexto());
                    raf.writeUTF(f.getFuente());
                    raf.writeInt(f.getTamano());
                    raf.writeInt(f.getColor().getRGB());

                }
                
                return true;
            }
         }else{
             return false;
         }
    }
    
  
    
        public static void abrir(JLabel titulo, JTextPane editor, File archivo)throws IOException, BadLocationException {

            if (archivo == null || !archivo.exists() || !archivo.isFile()) {
                throw new FileNotFoundException("Archivo no válido.");
            }

            try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {

                StyledDocument doc = editor.getStyledDocument();
                doc.remove(0, doc.getLength());

                raf.seek(0);

                String nombre = raf.readUTF();
                titulo.setText(nombre);

                int cantidadFragmentos = raf.readInt();

                for (int i = 0; i < cantidadFragmentos; i++) {
                    String texto = raf.readUTF();
                    String fuente = raf.readUTF();
                    int tamano = raf.readInt();
                    Color color = new Color(raf.readInt(), true);

                    SimpleAttributeSet atributos = new SimpleAttributeSet();
                    StyleConstants.setFontFamily(atributos, fuente);
                    StyleConstants.setFontSize(atributos, tamano);
                    StyleConstants.setForeground(atributos, color);

                    doc.insertString(doc.getLength(), texto, atributos);
                }
            }
        }
}

        
    
                     
              
            
        
    
    
    
        
        
        
        
        
        
        
    
            

    
    
    

