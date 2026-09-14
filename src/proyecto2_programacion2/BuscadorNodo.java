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

public class  BuscadorNodo {
    private final File ruta;
    private final File destino;
    private BuscadorNodo siguiente;

    public BuscadorNodo(File destino,File ruta ) {
        this.destino = destino;
        this.ruta= ruta;
    }

    public File getdestino() {
        return destino;
    }

    public BuscadorNodo getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(BuscadorNodo siguiente) {
        this.siguiente = siguiente;
    }

    public File getRuta() {
        return ruta;
    }
    
    

}
