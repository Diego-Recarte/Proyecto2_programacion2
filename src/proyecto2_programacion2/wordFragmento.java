/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */


import java.awt.*;
public class wordFragmento {

    private String texto;
    private String fuente;
    private int tamano;
    private Color color;
    private boolean negrita;
    private boolean cursiva;
    private boolean subrayado;
    private boolean tachado;

    public wordFragmento(String texto, String fuente, int tamano, Color color, boolean negrita, boolean cursiva, boolean subrayado, boolean tachado) {
        this.texto = texto;
        this.fuente = fuente;
        this.tamano = tamano;
        this.color = color;
        this.negrita = negrita;
        this.cursiva = cursiva;
        this.subrayado = subrayado;
        this.tachado = tachado;
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

    public boolean isNegrita() {
        return negrita;
    }

    public boolean isCursiva() {
        return cursiva;
    }

    public boolean isSubrayado() {
        return subrayado;
    }

    public boolean isTachado() {
        return tachado;
    }
}