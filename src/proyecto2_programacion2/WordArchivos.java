/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */



import java.awt.Color;
import java.awt.Component;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 Elementos del archivo
  1. Firma
     Método: writeUTF()
     Tamaño: indefinido
 
  2. Versión
     Método: writeInt()
     Tamaño: 4 bytes
 
  3. Nombre del documento
     Método: writeUTF()
     Tamaño: indefinido
 
  4. Cantidad de elementos
     Método: writeInt()
     Tamaño: 4 bytes
 
  5. Elementos del documento
     Método: variable
     Tamaño: variable

 */

public class WordArchivos {

    private static final Object LOCK_ARCHIVOS = new Object();

    private static final String FIRMA = "WRD1";
    private static final int VERSION = 3;
    private static final String EXTENSION = ".pwrd";




    private static final int TIPO_TEXTO = 0;
    private static final int TIPO_TABLA = 1;

    public static File archivoDocumento(String nombre) {
        
        return new File("src/datos/windows/Z/infoUsuarios/"+usuarioWinActivo.nombre+"/misDocumentos", nombre.trim() + EXTENSION);
    }
    
 /**
 Elementos del texto
  1. Tipo de elemento
     Método: writeInt()
     Tamaño: 4 bytes
 
  2. Texto
     Método: writeUTF()
     Tamaño: indefinido
 
  3. Fuente
     Método: writeUTF()
     Tamaño: indefinido

 4. Tamaño de fuente
    Método: writeInt()
     Tamaño: 4 bytes
 
  5. Color
     Método: writeInt()
     Tamaño: 4 bytes
 
  6. Negrita
     Método: writeBoolean()
    Tamaño: 1 byte

  7. Cursiva
     Método: writeBoolean()
     Tamaño: 1 byte
 
  8. Subrayado
     Método: writeBoolean()
     Tamaño: 1 byte
 
  9. Tachado
     Método: writeBoolean()
     Tamaño: 1 byte
 
 */

    public static boolean guardarComo(JTextPane editor, File archivo, String nombre, boolean isGuardarComo)
            throws WordException {

        synchronized (LOCK_ARCHIVOS) {

            if (nombre == null || nombre.trim().isEmpty()) {
                throw new WordException.DatosInvalidosException( "El nombre del documento no puede estar vacío.");
            }

            if (isGuardarComo && archivo.exists()) {
                throw new WordException.ArchivoYaExisteException(
                        "Ya existe un archivo con ese nombre.");
            }

            if (!isGuardarComo && !archivo.exists()) {
                throw new WordException.ArchivoNoExisteParaGuardarException("El documento aún no existe; use \"Guardar como\" primero.");
            }

            ArrayList<Object> elementos = extraerElementos(editor);

            File carpeta = archivo.getParentFile();
            if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
                throw new WordException.ErrorEscrituraException( "No se pudo crear la carpeta de destino: " + carpeta, null);
            }

            try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
                raf.setLength(0);
                raf.writeUTF(FIRMA);
                raf.writeInt(VERSION);
                raf.writeUTF(nombre);
                escribirElementos(raf, elementos);
                return true;
            } catch (IOException e) {
                throw new WordException.ErrorEscrituraException("No se pudo escribir el archivo: " + e.getMessage(), e);
            }
        }
    }

    public static boolean guardar(JTextPane editor, File archivo, String nombre, boolean isGuardarComo)
            throws WordException {

        synchronized (LOCK_ARCHIVOS) {

            if (archivo == null || !archivo.exists()) {
                throw new WordException.ArchivoNoExisteParaGuardarException( "El archivo no existe; use \"Guardar como\" primero.");
            }

            if (nombre == null || nombre.trim().isEmpty()) {
                throw new WordException.DatosInvalidosException("El nombre del documento no puede estar vacío.");
            }

            ArrayList<Object> elementos = extraerElementos(editor);

            try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
                raf.setLength(0);
                raf.writeUTF(FIRMA);
                raf.writeInt(VERSION);
                raf.writeUTF(nombre);
                escribirElementos(raf, elementos);
                return true;
            } catch (IOException e) {
                throw new WordException.ErrorEscrituraException( "No se pudo escribir el archivo: " + e.getMessage(), e);
            }
        }
    }

    private static ArrayList<Object> extraerElementos(JTextPane editor) throws WordException {
        ArrayList<Object> lista = new ArrayList<>();

        try {
            StyledDocument doc = editor.getStyledDocument();
            int longitud = doc.getLength();
            int i = 0;

            while (i < longitud) {
                Element elemento = doc.getCharacterElement(i);
                int inicio = elemento.getStartOffset();
                int fin = Math.min(elemento.getEndOffset(), longitud);

                AttributeSet atributos = elemento.getAttributes();
                Component componente = StyleConstants.getComponent(atributos);

                if (componente instanceof TablaEditor) {
                    lista.add(componente);
                } else {
                    String texto = doc.getText(inicio, fin - inicio);
                    String fuente = StyleConstants.getFontFamily(atributos);
                    int tamano = StyleConstants.getFontSize(atributos);
                    Color color = StyleConstants.getForeground(atributos);
                    boolean negrita = StyleConstants.isBold(atributos);
                    boolean cursiva = StyleConstants.isItalic(atributos);
                    boolean subrayado = StyleConstants.isUnderline(atributos);
                    boolean tachado = StyleConstants.isStrikeThrough(atributos);

                    lista.add(new wordFragmento( texto, fuente,tamano,color,negrita,cursiva,subrayado, tachado));
                }

                i = fin;
            }
        } catch (BadLocationException e) {
            throw new WordException.ErrorEditorException(
                    "No se pudo leer el contenido del editor.", e);
        }

        return lista;
    }

    private static void escribirElementos(RandomAccessFile raf, ArrayList<Object> elementos)
            throws IOException, WordException {

        raf.writeInt(elementos.size());

        for (Object elemento : elementos) {
            if (elemento instanceof TablaEditor) {
                escribirTabla(raf, (TablaEditor) elemento);
            } else if (elemento instanceof wordFragmento) {
                escribirTexto(raf, (wordFragmento) elemento);
            } else {
                throw new WordException.DatosInvalidosException(
                        "Elemento desconocido dentro del documento.");
            }
        }
    }

    private static void escribirTexto(RandomAccessFile raf, wordFragmento fragmento) throws IOException {
        raf.writeInt(TIPO_TEXTO);
        raf.writeUTF(fragmento.getTexto());
        raf.writeUTF(fragmento.getFuente());
        raf.writeInt(fragmento.getTamano());
        raf.writeInt(fragmento.getColor().getRGB());
        raf.writeBoolean(fragmento.isNegrita());
        raf.writeBoolean(fragmento.isCursiva());
        raf.writeBoolean(fragmento.isSubrayado());
        raf.writeBoolean(fragmento.isTachado());
    }
/**
 tabla
 
  1. Tipo de elemento
     Método: writeInt()
     Tamaño: 4 bytes
 
  2. Filas
     Método: writeInt()
     Tamaño: 4 bytes
 
  3. Columnas
     Método: writeInt()
     Tamaño: 4 bytes
 
  4. Celdas de la tabla
     Método: variable
     Tamaño: variable
 
 */
    private static void escribirTabla(RandomAccessFile raf, TablaEditor tabla)
            throws IOException, WordException {

        TablaCelda[][] celdas = tabla.getCeldas();
        int filas = tabla.getFilas();
        int columnas = tabla.getColumnas();

        if (filas <= 0 || columnas <= 0) {
            throw new WordException.DatosInvalidosException("La tabla tiene dimensiones inválidas.");
        }

        if (celdas == null || celdas.length != filas) {
            throw new WordException.DatosInvalidosException("Las filas de la tabla son inválidas.");
        }

        raf.writeInt(TIPO_TABLA);
        raf.writeInt(filas);
        raf.writeInt(columnas);

        for (int fila = 0; fila < filas; fila++) {
            if (celdas[fila] == null || celdas[fila].length != columnas) {
                throw new WordException.DatosInvalidosException("Las columnas de la tabla son inválidas.");
            }

            for (int columna = 0; columna < columnas; columna++) {
                TablaCelda celda = celdas[fila][columna];

                raf.writeUTF(celda.getTexto());
                raf.writeUTF(celda.getFuente());
                raf.writeInt(celda.getTamano());
                raf.writeInt(celda.getColor().getRGB());
                raf.writeBoolean(celda.isNegrita());
                raf.writeBoolean(celda.isCursiva());
                raf.writeBoolean(celda.isSubrayado());
                raf.writeBoolean(celda.isTachado());
            }
        }
    }

    public static void abrir(JLabel titulo, JTextPane editor, File archivo) throws WordException {

        if (archivo == null || !archivo.exists()) {
            throw new WordException.ArchivoNoEncontradoException( "El archivo no existe.");
        }

        if (!archivo.isFile()) {
            throw new WordException.ArchivoInvalidoException("La ruta indicada no es un archivo válido.");
        }

        String nombreArchivo = archivo.getName().toLowerCase();
        if (!nombreArchivo.endsWith(EXTENSION)) {
            throw new WordException.ExtensionInvalidaException("La extensión debe ser " + EXTENSION + ".");
        }

        StyledDocument temporal = new DefaultStyledDocument();
        String nombre;

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {

            String firmaLeida = raf.readUTF();
            if (!FIRMA.equals(firmaLeida)) {
                throw new WordException.FormatoDesconocidoException( "El archivo no pertenece al formato propio del editor.");
            }

            int versionLeida = raf.readInt();
            if (versionLeida != VERSION) {
                throw new WordException.VersionNoCompatibleException( "Versión de archivo no compatible (se encontró v" + versionLeida  + ", se esperaba v" + VERSION + ").");
            }

            nombre = raf.readUTF();
            if (nombre.trim().isEmpty()) {
                throw new WordException.DatosInvalidosException( "El nombre del documento está vacío.");
            }

            int cantidadElementos = raf.readInt();
            if (cantidadElementos < 0) {
                throw new WordException.DatosInvalidosException( "Cantidad de elementos inválida.");
            }

            for (int i = 0; i < cantidadElementos; i++) {
                leerElemento(raf, temporal);
            }

        } catch (EOFException e) {
            throw new WordException.ArchivoCorruptoException("El archivo está truncado o corrupto.", e);
        } catch (IOException e) {
            throw new WordException.ErrorEscrituraException( "No se pudo leer el archivo: " + e.getMessage(), e);
        } catch (BadLocationException e) {
            throw new WordException.ErrorEditorException(  "No se pudo reconstruir el contenido del documento.", e);
        }

        try {
            StyledDocument destino = editor.getStyledDocument();
            destino.remove(0, destino.getLength());
            copiarDocumento(temporal, destino);
        } catch (BadLocationException e) {
            throw new WordException.ErrorEditorException(
                    "No se pudo mostrar el documento en el editor.", e);
        }

        titulo.setText(nombre);
    }

    private static void leerElemento(RandomAccessFile raf, StyledDocument documento)
            throws IOException, BadLocationException, WordException {

        int tipo = raf.readInt();

        if (tipo == TIPO_TEXTO) {
            leerTexto(raf, documento);
        } else if (tipo == TIPO_TABLA) {
            leerTabla(raf, documento);
        } else {
            throw new WordException.ArchivoCorruptoException(
                    "Tipo de elemento desconocido en el archivo: " + tipo);
        }
    }

    private static void leerTexto(RandomAccessFile raf, StyledDocument documento)
            throws IOException, BadLocationException, WordException {

        String texto = raf.readUTF();
        String fuente = raf.readUTF();
        int tamano = raf.readInt();
        Color color = new Color(raf.readInt(), true);

        boolean negrita = raf.readBoolean();
        boolean cursiva = raf.readBoolean();
        boolean subrayado = raf.readBoolean();
        boolean tachado = raf.readBoolean();

        if (tamano <= 0) {
            throw new WordException.DatosInvalidosException("Tamaño de texto inválido.");
        }

        SimpleAttributeSet atributos = crearAtributos(
                fuente, tamano, color, negrita, cursiva, subrayado, tachado
        );

        documento.insertString(documento.getLength(), texto, atributos);
    }
    
/**
  Celdas de tabla
  1. Texto
     Método: writeUTF()
     Tamaño: indefinido
 
  2. Fuente
     Método: writeUTF()
     Tamaño: indefinido
 
  3. Tamaño de fuente
     Método: writeInt()
     Tamaño: 4 bytes
 
  4. Color
     Método: writeInt()
     Tamaño: 4 bytes
 
  5. Negrita
     Método: writeBoolean()
     Tamaño: 1 byte
 
  6. Cursiva
     Método: writeBoolean()
     Tamaño: 1 byte
 
  7. Subrayado
     Método: writeBoolean()
     Tamaño: 1 byte
 
  8. Tachado
     Método: writeBoolean()
    Tamaño: 1 byte

 */

    private static void leerTabla(RandomAccessFile raf, StyledDocument documento)
            throws IOException, WordException {

        int filas = raf.readInt();
        int columnas = raf.readInt();

        if (filas <= 0 || filas > 1000 || columnas <= 0 || columnas > 1000) {
            throw new WordException.DatosInvalidosException("Dimensiones de tabla inválidas.");
        }

        TablaCelda[][] celdas = new TablaCelda[filas][columnas];

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                String texto = raf.readUTF();
                String fuente = raf.readUTF();
                int tamano = raf.readInt();
                Color color = new Color(raf.readInt(), true);

                boolean negrita = raf.readBoolean();
                boolean cursiva = raf.readBoolean();
                boolean subrayado = raf.readBoolean();
                boolean tachado = raf.readBoolean();

                if (tamano <= 0) {
                    throw new WordException.DatosInvalidosException("Tamaño de celda inválido.");
                }

                celdas[fila][columna] = new TablaCelda(
                        texto, fuente, tamano, color,
                        negrita, cursiva, subrayado, tachado
                );
            }
        }

        TablaEditor tabla = new TablaEditor(filas, columnas, celdas);

        try {
            documento.insertString(documento.getLength(), "\n", null);
        } catch (BadLocationException ev) {
        }

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setComponent(atributos, tabla);

        try {
            documento.insertString(documento.getLength(), " ", atributos);
        } catch (BadLocationException ev) {
        }

        try {
            documento.insertString(documento.getLength(), "\n", null);
        } catch (BadLocationException ev) {
        }
    }

    private static SimpleAttributeSet crearAtributos(String fuente, int tamano, Color color,
            boolean negrita, boolean cursiva, boolean subrayado, boolean tachado) {

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos, fuente);
        StyleConstants.setFontSize(atributos, tamano);
        StyleConstants.setForeground(atributos, color);
        StyleConstants.setBold(atributos, negrita);
        StyleConstants.setItalic(atributos, cursiva);
        StyleConstants.setUnderline(atributos, subrayado);
        StyleConstants.setStrikeThrough(atributos, tachado);
        return atributos;
    }

    private static void copiarDocumento(StyledDocument origen, StyledDocument destino)
            throws BadLocationException {

        for (int i = 0; i < origen.getLength();) {
            Element elemento = origen.getCharacterElement(i);
            int inicio = elemento.getStartOffset();
            int fin = Math.min(elemento.getEndOffset(), origen.getLength());

            String texto = origen.getText(inicio, fin - inicio);
            AttributeSet atributos = elemento.getAttributes();

            destino.insertString(destino.getLength(), texto, atributos);
            i = fin;
        }
    }
}

        
    
                     
              
            
        
    
    
    
        
        
        
        
        
        
        
    
            

    
    
    

