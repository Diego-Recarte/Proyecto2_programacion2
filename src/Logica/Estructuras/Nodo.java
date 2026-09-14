package Logica.Estructuras;

/** Nodo simple: un dato y un unico enlace hacia el siguiente nodo. */
final class Nodo<T> {
    T dato;
    Nodo<T> siguiente;

    Nodo(T dato) {
        this.dato = dato;
    }
}
