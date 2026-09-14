package Instagram;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

/** Ejecutar desde un directorio temporal vacío. */
public final class InstaImagePathTest {

    public static void main(String[] args) throws Exception {
        Path relative = Path.of("Instagram/users/prueba/imagenes/foto con espacios.png");
        Files.createDirectories(relative.getParent());
        ImageIO.write(new BufferedImage(7, 5, BufferedImage.TYPE_INT_RGB), "png", relative.toFile());
        require(InstaPostMedia.readImage(relative.toString()).getWidth() == 7,
                "No se pudo cargar una referencia relativa.");
        String old = "C:\\otro-equipo\\proyecto\\Instagram\\users\\prueba\\imagenes\\foto con espacios.png";
        require(InstaPostMedia.readImage(old).getHeight() == 5,
                "No se recuperó la publicación de otro equipo.");
        String carousel = InstaPostMedia.encode(List.of(old, relative.toString()));
        require(new File(InstaPostMedia.coverPath(carousel)).isFile(),
                "La portada no usa la ubicación actual.");
        require(InstaPostMedia.decode(carousel).get(0).equals(old),
                "Se modificó la referencia que identifica los likes y comentarios.");
        Path legacy = Path.of("src/datos/windows/Z/infoUsuarios/prueba/misImagenes/foto.png");
        Files.createDirectories(legacy.getParent());
        Files.copy(relative, legacy);
        require(InstaPostMedia.readImage("src/Z/Usuarios/prueba/Mis Imagenes/foto.png").getWidth() == 7,
                "No se pudo cargar una foto de la antigua unidad Z.");
        String outside = "C:/otro/Instagram/users/../fuera.png";
        require(InstaPostMedia.resolvePath(outside).equals(outside),
                "Se resolvió una ruta fuera de la carpeta de usuarios.");
        expectFailure("ausente.png", "No se encontró");
        Path invalid = relative.resolveSibling("invalida.png");
        Files.writeString(invalid, "Esto no es una imagen");
        expectFailure(invalid.toString(), "Formato de imagen no compatible");
        System.out.println("OK: rutas relativas, publicaciones trasladadas y errores de imagen.");
    }

    private static void expectFailure(String path, String message) throws Exception {
        try {
            InstaPostMedia.readImage(path);
            throw new AssertionError("Se aceptó una imagen inválida: " + path);
        } catch (IOException ex) {
            require(ex.getMessage().contains(message), "No se distinguió la causa: " + ex.getMessage());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
