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
public class CMDControladorMkdir implements CMDComando{
    private CMDSistemaArchivo sistema;
    
    public CMDControladorMkdir(CMDSistemaArchivo sistema){
        this.sistema = sistema;
    }
    
    @Override
    public String ejecutar(String args[]){
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

       

        if (nombre.trim().isEmpty()) {
            return "Error: Debe especificar el nombre de la carpeta.";
        }

        if (sistema.crearDir(nombre)) {
            return "Carpeta \"" + nombre + "\" creada correctamente.";
        }

        return "Error: No se pudo crear la carpeta \"" + nombre + "\". Puede que ya exista.";
    }
    
}
