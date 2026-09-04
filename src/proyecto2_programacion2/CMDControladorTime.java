/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import java.util.Calendar;
public class CMDControladorTime implements CMDComando{
    
    @Override
    public String ejecutar(String[] argumentos){
        Calendar actual = Calendar.getInstance();
        
        return String.format("Hora actual: %02d:%02d:%02d",
                actual.get(Calendar.HOUR_OF_DAY),
                actual.get(Calendar.MINUTE),
                actual.get(Calendar.SECOND));
        
    }
}
