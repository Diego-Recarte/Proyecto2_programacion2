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
    public class TablaCelda {

    private String texto;
    private String fuente;
    private int tamano;
    private Color color;
    private boolean negrita;
    private boolean cursiva;
    private boolean subrayado;
    private boolean tachado;

    public TablaCelda() {
        this("", "Arial", 12, Color.BLACK, false, false, false, false);
    }

    public TablaCelda(String texto, String fuente, int tamano, Color color,
            boolean negrita, boolean cursiva, boolean subrayado, boolean tachado) {
        this.texto = texto == null ? "" : texto;
        this.fuente = fuente == null ? "Arial" : fuente;
        this.tamano = tamano <= 0 ? 12 : tamano;
        this.color = color == null ? Color.BLACK : color;
        this.negrita = negrita;
        this.cursiva = cursiva;
        this.subrayado = subrayado;
        this.tachado = tachado;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto == null ? "" : texto;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isNegrita() {
        return negrita;
    }

    public void setNegrita(boolean negrita) {
        this.negrita = negrita;
    }

    public boolean isCursiva() {
        return cursiva;
    }

    public void setCursiva(boolean cursiva) {
        this.cursiva = cursiva;
    }

    public boolean isSubrayado() {
        return subrayado;
    }

    public void setSubrayado(boolean subrayado) {
        this.subrayado = subrayado;
    }

    public boolean isTachado() {
        return tachado;
    }

    public void setTachado(boolean tachado) {
        this.tachado = tachado;
    }

}
