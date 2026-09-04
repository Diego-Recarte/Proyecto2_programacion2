package Instagram;

import java.util.List;

/** Prueba ejecutable del formato retrocompatible usado por los carruseles. */
public final class InstaPostMediaTest {

    private InstaPostMediaTest() {
    }

    public static void main(String[] args) {
        String legacyPath = "C:\\imagenes\\foto antigua.png";
        require(legacyPath.equals(InstaPostMedia.encode(List.of(legacyPath))),
                "Una publicación de una imagen debe conservar el formato anterior.");
        require(InstaPostMedia.decode(legacyPath).equals(List.of(legacyPath)),
                "No se pudo leer una publicación anterior.");

        List<String> carousel = List.of(
                "C:\\imagenes\\uno.jpg",
                "C:\\imágenes\\dos.con.puntos.png",
                "C:\\imagenes\\tres.webp");
        String encoded = InstaPostMedia.encode(carousel);
        require(InstaPostMedia.decode(encoded).equals(carousel),
                "El carrusel no conservó todas las rutas en orden.");
        require(InstaPostMedia.coverPath(encoded).equals(carousel.get(0)),
                "La portada del carrusel no es la primera imagen.");
        InstaMediaCarousel carouselView = new InstaMediaCarousel(carousel, 360, 360, null);
        require(carouselView.getImageCount() == 3,
                "El componente visual no recibió todas las imágenes.");

        System.out.println("OK: publicaciones individuales y carruseles son compatibles.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
