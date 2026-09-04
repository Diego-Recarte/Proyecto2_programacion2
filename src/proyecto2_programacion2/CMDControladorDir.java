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
public class CMDControladorDir implements CMDComando {
    
    private CMDSistemaArchivo sistema;
    
    public CMDControladorDir(CMDSistemaArchivo sistema){
        this.sistema = sistema;
    }
    
    @Override
    public String ejecutar(String argumentos[]){
        File actual = sistema.getActual();
        File[] lista = actual.listFiles();
        
        if (lista == null || lista.length == 0){
            return "La carpeta esta vacia";
        }
        
        String resultado = "Directorio de '"+sistema.getRutaActual()+"': \n\n";
        
        int contDir = 0, contFile = 0;
        
        for(File f: lista){
            if(f.isDirectory()){
                resultado += "<DIR>     "+f.getName()+"\n";
                contDir++;
            } else{
                resultado += "          "+f.getName()+"\n";
                contFile++;
            }
        }
        
        resultado += "\n     "+contFile+" archivo(s)";
        resultado += "\n     "+contDir+" carpeta(s)";
        
        return resultado;
    }
    
}
