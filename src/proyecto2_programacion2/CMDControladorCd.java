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
        if (argumentos.length != 1){
            return "Uso correcto: Cd <nombre carpeta>";
        }
        
        String nombre = argumentos[0];
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
