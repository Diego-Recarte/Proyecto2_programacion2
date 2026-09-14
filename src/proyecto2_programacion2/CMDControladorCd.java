/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import java.io.File;

public class CMDControladorCd implements CMDComando{
    
    private CMDSistemaArchivo sistema;
    
    public CMDControladorCd(CMDSistemaArchivo sistema){
        this.sistema = sistema;
    }
    
    @Override
    public String ejecutar(String[] argumentos){
        if (argumentos == null || argumentos.length == 0) {
            return "Uso: cd <nombre_carpeta>";
        }
        String nombre;
            
        if (argumentos[argumentos.length-1].endsWith("\"")){
            String textoCompleto= "";
            for (String texto: argumentos){
                textoCompleto= textoCompleto+texto+" ";
            }
            nombre = textoCompleto;
            nombre = nombre.substring(1, nombre.length() - 1);
            nombre = nombre.substring(0, nombre.length() - 1);
            
        }else{
            nombre = argumentos[0];
        }
        if (nombre.equals("..") || nombre.equals(".")) {
            return "Carpeta '" + nombre + "' no encontrada";
        }
        
        
        
        File carpeta = sistema.buscar(nombre);
        
        if(!carpeta.exists()){
            return "Carpeta '"+nombre+"'no encontrada";
        }
        if(!carpeta.isDirectory()){
            return "La ruta indicada '"+nombre+"'no es de una carpeta.";
        }
        
        if(!sistema.cambiarDir(nombre)){
            return "No se pudo cambiar a la carpeta '"+nombre+"'";
        }
        
        return "Cambio a '"+nombre+"' exitoso.";
    }
    

}
