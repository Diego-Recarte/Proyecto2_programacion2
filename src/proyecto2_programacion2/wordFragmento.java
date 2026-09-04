/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import javax.swing.*;
import java.io.*;
import java.awt.*;
public class wordFragmento {
    
    private String texto;
    private String fuente;
    private int tamano;
    private Color color;
 
    
    public wordFragmento (String texto, String fuente, int tamano, Color color ){
        this.texto= texto;
        this.fuente= fuente;
        this.tamano= tamano;
        this.color= color;
        
    }

    public String getTexto() {
        return texto;
    }

    public String getFuente() {
        return fuente;
    }

    public int getTamano() {
        return tamano;
    }

    public Color getColor() {
        return color;
    }
    
    
    
    
}
