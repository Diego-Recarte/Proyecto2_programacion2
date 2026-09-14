/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */
public class BuscadorLista {

    private BuscadorNodo inicio;
    private int size;

    public BuscadorLista() {
        inicio = null;
        size = 0;
    }

    public boolean isEmpty() {
        return inicio == null;
    }

    public int size() {
        return size;
    }

    public void add(BuscadorNodo obj) {
        if (obj == null) {
            return;
        }

        obj.setSiguiente(null);

        if (isEmpty()) {
            inicio = obj;
        } else {
            BuscadorNodo aux = inicio;

            while (aux.getSiguiente() != null) {
                aux = aux.getSiguiente();
            }

            aux.setSiguiente(obj);
        }

        size++;
    }

    public BuscadorNodo getInicio() {
        return inicio;
    }

    public void clear() {
        inicio = null;
        size = 0;
    }
}
