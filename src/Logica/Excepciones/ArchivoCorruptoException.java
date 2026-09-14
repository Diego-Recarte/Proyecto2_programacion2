package Logica.Excepciones;
public final class ArchivoCorruptoException extends java.io.IOException {
    public ArchivoCorruptoException(String file, Throwable cause) { super("Registro binario incompleto: " + file, cause); }
}
