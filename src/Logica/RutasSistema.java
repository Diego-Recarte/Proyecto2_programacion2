package Logica;

import java.io.File;
import java.nio.file.Path;

/** Ubicación compartida de la unidad Z y de los archivos de sus usuarios. */
public final class RutasSistema {

    public static final File Z = new File("src/datos/windows/Z");
    public static final File USUARIOS = new File(Z, "infoUsuarios");
    public static final String DOCUMENTOS = "misDocumentos";
    public static final String IMAGENES = "misImagenes";
    public static final String MUSICA = "musica";

    private RutasSistema() {
    }

    public static File usuario(String nombre) {
        return new File(USUARIOS, nombre);
    }

    public static File documentos(String nombre) {
        return new File(usuario(nombre), DOCUMENTOS);
    }

    public static File imagenes(String nombre) {
        return new File(usuario(nombre), IMAGENES);
    }

    public static File musica(String nombre) {
        return new File(usuario(nombre), MUSICA);
    }

    /** Conserva las referencias de publicaciones anteriores al traslado de src/Z. */
    public static String resolverRutaAnterior(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return ruta;
        }
        String normalizada = ruta.replace('\\', '/');
        String prefijo = "src/Z/Usuarios/";
        int inicio = normalizada.indexOf(prefijo);
        if (inicio < 0 || (inicio > 0 && normalizada.charAt(inicio - 1) != '/')) {
            return ruta;
        }
        String[] partes = normalizada.substring(inicio + prefijo.length()).split("/", 3);
        if (partes.length < 2) {
            return ruta;
        }
        String carpeta = switch (partes[1]) {
            case "Mis Imagenes" -> IMAGENES;
            case "Mis Documentos" -> DOCUMENTOS;
            case "Musica" -> MUSICA;
            default -> partes[1];
        };
        Path destino = usuario(partes[0]).toPath().resolve(carpeta);
        if (partes.length == 3) {
            destino = destino.resolve(partes[2]);
        }
        destino = destino.toAbsolutePath().normalize();
        return destino.startsWith(USUARIOS.toPath().toAbsolutePath().normalize())
                ? destino.toString() : ruta;
    }
}
