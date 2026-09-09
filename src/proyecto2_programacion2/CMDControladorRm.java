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
public class CMDControladorRm implements CMDComando {
    private CMDSistemaArchivo sistema;

    public CMDControladorRm(CMDSistemaArchivo sistema) {
        this.sistema = sistema;
    }

    @Override
    public String ejecutar(String[] args) {
        
    

        
        String nombre;
        if (args[args.length-1].endsWith("\"")){
            String textoCompleto= "";
            for (String texto: args){
                textoCompleto= textoCompleto+texto+" ";
            }
            nombre = textoCompleto;
            nombre = nombre.substring(1, nombre.length() - 1);
            nombre = nombre.substring(0, nombre.length() - 1);
            
        }else{
            nombre = args[0];
        }
        
  

        File archivo = sistema.buscar(nombre);

        if (!archivo.exists()) {
            return "Error: \"" + nombre + "\" no existe.";
        }

        if (sistema.eliminar(archivo)) {
            return "\"" + nombre + "\" eliminado correctamente.";
        }

        return "Error: No se pudo eliminar \"" + nombre + "\".";
    }
    
}
