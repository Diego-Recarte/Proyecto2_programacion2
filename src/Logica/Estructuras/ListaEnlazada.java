package Logica.Estructuras;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Lista enlazada simple propia. Los elementos se guardan exclusivamente en
 * nodos, nunca en ArrayList, LinkedList ni en un arreglo auxiliar.
 * AbstractList solo permite interoperar con las vistas que reciben List.
 * Agregar al final cuesta O(1); recorrer, buscar y acceder por indice, O(n).
 * No es thread-safe: cada carga debe construir su propia lista antes de
 * entregarla al hilo de Swing.
 */
public final class ListaEnlazada<T> extends AbstractList<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int cantidad;

    public ListaEnlazada() {
    }

    public ListaEnlazada(Iterable<? extends T> elementos) {
        for (T elemento : elementos) {
            add(elemento);
        }
    }

    @Override
    public int size() {
        return cantidad;
    }

    @Override
    public boolean add(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cola == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        cantidad++;
        modCount++;
        return true;
    }

    @Override
    public void add(int indice, T dato) {
        if (indice < 0 || indice > cantidad) {
            throw new IndexOutOfBoundsException(indice);
        }
        if (indice == cantidad) {
            add(dato);
            return;
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        if (indice == 0) {
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
        } else {
            Nodo<T> anterior = nodo(indice - 1);
            nuevo.siguiente = anterior.siguiente;
            anterior.siguiente = nuevo;
        }
        cantidad++;
        modCount++;
    }

    private Nodo<T> nodo(int indice) {
        Objects.checkIndex(indice, cantidad);
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual;
    }

    @Override
    public T get(int indice) {
        return nodo(indice).dato;
    }

    @Override
    public T set(int indice, T dato) {
        Nodo<T> actual = nodo(indice);
        T anterior = actual.dato;
        actual.dato = dato;
        return anterior;
    }

    private T desenlazar(Nodo<T> anterior, Nodo<T> actual) {
        if (anterior == null) {
            cabeza = actual.siguiente;
        } else {
            anterior.siguiente = actual.siguiente;
        }
        if (cola == actual) {
            cola = anterior;
        }
        cantidad--;
        modCount++;
        return actual.dato;
    }

    @Override
    public T remove(int indice) {
        Objects.checkIndex(indice, cantidad);
        Nodo<T> anterior = indice == 0 ? null : nodo(indice - 1);
        return desenlazar(anterior, anterior == null ? cabeza : anterior.siguiente);
    }

    @Override
    public boolean remove(Object dato) {
        Nodo<T> anterior = null;
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (Objects.equals(actual.dato, dato)) {
                desenlazar(anterior, actual);
                return true;
            }
            anterior = actual;
        }
        return false;
    }

    @Override
    public int indexOf(Object dato) {
        int indice = 0;
        for (T elemento : this) {
            if (Objects.equals(elemento, dato)) {
                return indice;
            }
            indice++;
        }
        return -1;
    }

    @Override
    public void clear() {
        cabeza = cola = null;
        cantidad = 0;
        modCount++;
    }

    /** Invierte los enlaces en O(n), sin copiar los datos. */
    public void reverse() {
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        cola = cabeza;
        while (actual != null) {
            Nodo<T> siguiente = actual.siguiente;
            actual.siguiente = anterior;
            anterior = actual;
            actual = siguiente;
        }
        cabeza = anterior;
        modCount++;
    }

    /** Merge sort estable sobre enlaces, O(n log n), sin arreglos auxiliares. */
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super T> comparador) {
        Comparator<? super T> orden = comparador != null ? comparador
                : (a, b) -> ((Comparable<? super T>) a).compareTo(b);
        cabeza = ordenar(cabeza, orden);
        cola = cabeza;
        while (cola != null && cola.siguiente != null) {
            cola = cola.siguiente;
        }
        modCount++;
    }

    private Nodo<T> ordenar(Nodo<T> inicio, Comparator<? super T> comparador) {
        if (inicio == null || inicio.siguiente == null) {
            return inicio;
        }
        Nodo<T> lento = inicio;
        Nodo<T> rapido = inicio.siguiente;
        while (rapido != null && rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente.siguiente;
        }
        Nodo<T> derecha = lento.siguiente;
        lento.siguiente = null;
        Nodo<T> izquierda = ordenar(inicio, comparador);
        derecha = ordenar(derecha, comparador);
        Nodo<T> centinela = new Nodo<>(null);
        Nodo<T> ultimo = centinela;
        while (izquierda != null && derecha != null) {
            if (comparador.compare(izquierda.dato, derecha.dato) <= 0) {
                ultimo.siguiente = izquierda;
                izquierda = izquierda.siguiente;
            } else {
                ultimo.siguiente = derecha;
                derecha = derecha.siguiente;
            }
            ultimo = ultimo.siguiente;
        }
        ultimo.siguiente = izquierda != null ? izquierda : derecha;
        return centinela.siguiente;
    }

    /** El for-each avanza nodo a nodo, sin repetir busquedas por indice. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> siguiente = cabeza;
            private Nodo<T> anterior;
            private Nodo<T> ultimo;
            private Nodo<T> anteriorUltimo;
            private int version = modCount;

            private void comprobarVersion() {
                if (version != modCount) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public boolean hasNext() {
                comprobarVersion();
                return siguiente != null;
            }

            @Override
            public T next() {
                comprobarVersion();
                if (siguiente == null) {
                    throw new NoSuchElementException();
                }
                anteriorUltimo = anterior;
                ultimo = siguiente;
                anterior = ultimo;
                siguiente = siguiente.siguiente;
                return ultimo.dato;
            }

            @Override
            public void remove() {
                comprobarVersion();
                if (ultimo == null) {
                    throw new IllegalStateException();
                }
                desenlazar(anteriorUltimo, ultimo);
                anterior = anteriorUltimo;
                ultimo = null;
                version = modCount;
            }
        };
    }
}
