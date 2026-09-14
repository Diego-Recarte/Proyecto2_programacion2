package Logica.Excepciones;
public final class UsuarioDuplicadoException extends java.io.IOException {
    public UsuarioDuplicadoException(String user) { super("El username ya existe: " + user); }
}
