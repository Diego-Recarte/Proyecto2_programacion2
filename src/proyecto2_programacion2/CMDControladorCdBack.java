/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */
public class CMDControladorCdBack implements CMDComando  {
    
    private CMDSistemaArchivo sistema;

    public CMDControladorCdBack (CMDSistemaArchivo sistema) {
        this.sistema = sistema;
    }

    @Override
    public String ejecutar(String[] args) {

        if (args.length != 0) {
            return "Uso correcto: ..";
        }

        if (sistema.cambiarAnterior()) {
            return "";
        }

        return "Ya se encuentra en la carpeta raíz.";
    }
}
