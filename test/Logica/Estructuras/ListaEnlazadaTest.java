package Logica.Estructuras;

import java.util.ConcurrentModificationException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Ejecutar con java Logica.Estructuras.ListaEnlazadaTest. */
public final class ListaEnlazadaTest {
    public static void main(String[] args) {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        require(lista.isEmpty(), "Lista inicial vacia");
        lista.reverse();
        lista.sort(null);
        lista.add("b");
        lista.add(0, "a");
        lista.add("d");
        lista.add(2, "c");
        require(lista.equals(List.of("a", "b", "c", "d")), "Inserciones");
        require(lista.remove("a") && lista.remove("c") && lista.remove("d"), "Eliminar cabeza, medio y cola");
        lista.add("e");
        require(lista.equals(List.of("b", "e")), "Cola despues de eliminar");
        require("b".equals(lista.remove(0)) && "e".equals(lista.remove(0)), "Eliminar hasta vaciar");
        lista.add(null);
        require(lista.contains(null) && lista.remove(null) && lista.isEmpty(), "Dato null");
        lista.addAll(List.of("c", "a", "b"));
        ListaEnlazada<String> copia = new ListaEnlazada<>(lista);
        lista.clear();
        require(copia.size() == 3, "Copia con nodos independientes");
        copia.reverse();
        require(copia.equals(List.of("b", "a", "c")), "Invertir enlaces");
        copia.sort(null);
        require(copia.equals(List.of("a", "b", "c")), "Orden natural");
        copia.add("d");
        require("d".equals(copia.get(3)), "Cola despues de ordenar");
        Iterator<String> iterator = copia.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        require(copia.isEmpty(), "Eliminar por iterador");
        copia.add("x");
        Iterator<String> stale = copia.iterator();
        copia.add("y");
        try {
            stale.next();
            throw new AssertionError("Falta detectar una modificacion durante el recorrido");
        } catch (ConcurrentModificationException expected) {
        }
        try {
            copia.get(-1);
            throw new AssertionError("Indice invalido aceptado");
        } catch (IndexOutOfBoundsException expected) {
        }

        record Dato(int orden, String id) { }
        ListaEnlazada<Dato> estable = new ListaEnlazada<>(List.of(
                new Dato(2, "a"), new Dato(1, "b"), new Dato(2, "c"), new Dato(1, "d")));
        estable.sort(Comparator.comparingInt(Dato::orden));
        require(estable.get(0).id().equals("b") && estable.get(1).id().equals("d")
                && estable.get(2).id().equals("a") && estable.get(3).id().equals("c"), "Orden estable");

        // Una lista grande detecta perdidas de enlaces, ciclos y recursion lineal.
        ListaEnlazada<Integer> grande = new ListaEnlazada<>();
        Random random = new Random(17);
        long suma = 0;
        for (int i = 0; i < 20000; i++) {
            int dato = random.nextInt(100000);
            grande.add(dato);
            suma += dato;
        }
        grande.sort(null);
        int anterior = -1;
        int cantidad = 0;
        long sumaOrdenada = 0;
        for (int dato : grande) {
            require(dato >= anterior, "Orden incorrecto");
            anterior = dato;
            sumaOrdenada += dato;
            require(++cantidad <= 20000, "Ciclo de nodos");
        }
        require(cantidad == 20000 && suma == sumaOrdenada, "Se perdieron datos al ordenar");
        System.out.println("OK: nodos, insercion, eliminacion, iterador, copia y ordenamiento estable (20000 datos).");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
