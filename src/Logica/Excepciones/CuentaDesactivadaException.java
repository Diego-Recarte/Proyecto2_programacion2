package Logica.Excepciones;
public final class CuentaDesactivadaException extends java.io.IOException {
    public CuentaDesactivadaException() { super("La cuenta está desactivada. Reactívala desde Editar perfil."); }
}
