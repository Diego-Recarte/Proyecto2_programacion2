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

        if (args.length != 1) {
            return "Uso correcto: Rm <nombre>";
        }

        String nombre = args[0];

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
