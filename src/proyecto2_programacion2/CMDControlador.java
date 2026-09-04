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

public class CMDControlador {
    
    private CMDSistemaArchivo sistema;
    private CMDInterprete interprete;
    private boolean modoEscritura;
    private boolean modoAppend;
    private File archivoEscritura;
    private StringBuilder contenido;
    

    public CMDControlador(){
        sistema = new CMDSistemaArchivo();
        interprete = new CMDInterprete(sistema);
        modoEscritura = false;
        modoAppend = false;
    }
    

    public String procesarEntrada(String entrada) {
        if (modoEscritura) {
            
            contenido.append(entrada);
            contenido.append(System.lineSeparator());
            return "";
        }
        String resultado = interprete.ejecutar(entrada);
        
        return resultado;
    }

    private void iniciarEscritura(String entrada, boolean append) {
        String[] partes = entrada.split(" ");
        String nombreArchivo = partes[1];
        archivoEscritura = sistema.buscar(nombreArchivo);
        contenido = new StringBuilder();
        modoEscritura = true;
        modoAppend = append;
    }

   
    public String getRutaActual() {
        return sistema.getRutaActual();
    }

    public CMDInterprete getInterprete() {
        return interprete;
    }
    
    
    
}
   
    
    


