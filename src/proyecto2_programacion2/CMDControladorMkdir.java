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
        if (args.length != 1) {
            return "Uso correcto: Mkdir <nombre>";
        }

        String nombre = args[0];

        if (nombre.trim().isEmpty()) {
            return "Error: Debe especificar el nombre de la carpeta.";
        }

        if (sistema.crearDir(nombre)) {
            return "Carpeta \"" + nombre + "\" creada correctamente.";
        }

        return "Error: No se pudo crear la carpeta \"" + nombre + "\". Puede que ya exista.";
    }
    
}
