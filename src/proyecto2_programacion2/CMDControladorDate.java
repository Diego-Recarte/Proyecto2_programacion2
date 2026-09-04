/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */
import java.text.SimpleDateFormat;
import java.util.Date;

public class CMDControladorDate implements CMDComando {
    public CMDControladorDate(CMDSistemaArchivo sistema) {
       
    }

    @Override
    public String ejecutar(String[] args) {

        if (args.length != 0) {
            return "Uso correcto: Date";
        }

        Date fecha = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        return "Fecha actual: " + formato.format(fecha);
    }
    
}
