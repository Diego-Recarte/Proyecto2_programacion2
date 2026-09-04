/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */
import java.util.HashMap;
public class CMDInterprete {
    private HashMap<String, CMDComando> comandos;
    private CMDSistemaArchivo sistema;

    public CMDInterprete(CMDSistemaArchivo sistema) {
        this.sistema = sistema;
        comandos = new HashMap<>();
        comandos.put("cd",new CMDControladorCd(sistema));
        comandos.put("rm",new CMDControladorRm(sistema));
        comandos.put("cd..",new CMDControladorCdBack(sistema));
        comandos.put("date",new CMDControladorDate(sistema));
        comandos.put("dir",new CMDControladorDir(sistema));
        comandos.put("mkdir",new  CMDControladorMkdir(sistema));
        comandos.put("time", new CMDControladorTime());
    }

    public String ejecutar(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            return "";
        }
        String[] partes = entrada.trim().split("\\s+");
        String nombreComando = partes[0];
        CMDComando comando = comandos.get(nombreComando);
        if (comando == null) {
            return "'" + nombreComando + "' no se reconoce como un comando interno o externo.";
        }
        String[] argumentos = new String[partes.length - 1];
        for (int i = 1; i < partes.length; i++) {
            argumentos[i - 1] = partes[i];
        }
        return comando.ejecutar(argumentos);
    }

    public String getRutaActual() {
        return sistema.getRutaActual();
    }
}
