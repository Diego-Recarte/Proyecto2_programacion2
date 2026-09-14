package Instagram;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

/** Codifica varias imágenes dentro del campo de imagen existente del post. */
public final class InstaPostMedia {

    private static final String CAROUSEL_PREFIX = "carousel:v1:";

    private InstaPostMedia() {
    }

    public static String encode(List<String> imagePaths) {
        ArrayList<String> validPaths = new ArrayList<>();
        if (imagePaths != null) {
            for (String path : imagePaths) {
                if (path != null && !path.isBlank()) {
                    validPaths.add(path);
                }
            }
        }
        if (validPaths.isEmpty()) {
            return "";
        }
        if (validPaths.size() == 1) {
            return validPaths.get(0);
        }

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        StringBuilder encoded = new StringBuilder(CAROUSEL_PREFIX);
        for (int index = 0; index < validPaths.size(); index++) {
            if (index > 0) {
                encoded.append('.');
            }
            encoded.append(encoder.encodeToString(validPaths.get(index).getBytes(StandardCharsets.UTF_8)));
        }
        return encoded.toString();
    }

    public static List<String> decode(String mediaReference) {
        if (mediaReference == null || mediaReference.isBlank() || mediaReference.startsWith("text:v1:")) {
            return Collections.emptyList();
        }
        if (!mediaReference.startsWith(CAROUSEL_PREFIX)) {
            return Collections.singletonList(mediaReference);
        }

        ArrayList<String> paths = new ArrayList<>();
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = mediaReference.substring(CAROUSEL_PREFIX.length());
        try {
            for (String encodedPath : payload.split("\\.")) {
                if (!encodedPath.isBlank()) {
                    paths.add(new String(decoder.decode(encodedPath), StandardCharsets.UTF_8));
                }
            }
        } catch (IllegalArgumentException ex) {
            return Collections.singletonList(mediaReference);
        }
        return paths.isEmpty() ? Collections.singletonList(mediaReference) : paths;
    }

    static String coverPath(String mediaReference) {
        List<String> paths = decode(mediaReference);
        return paths.isEmpty() ? "" : resolvePath(paths.get(0));
    }

    /** Recupera referencias guardadas desde otra ubicación del proyecto. */
    public static String resolvePath(String path) {
        if (path != null && path.startsWith("insta://")) return path;
        String resolved = Logica.RutasSistema.resolverRutaAnterior(path);
        if (resolved == null || resolved.isBlank() || new File(resolved).isFile()) {
            return resolved;
        }
        String normalized = resolved.replace('\\', '/');
        for (String root : List.of("Instagram/users/", "src/datos/windows/Z/infoUsuarios/")) {
            int start = normalized.lastIndexOf(root);
            if (start < 0 || (start > 0 && normalized.charAt(start - 1) != '/')) {
                continue;
            }
            Path localRoot = Path.of(root).toAbsolutePath().normalize();
            Path candidate = localRoot.resolve(normalized.substring(start + root.length())).normalize();
            if (candidate.startsWith(localRoot) && candidate.toFile().isFile()) {
                return candidate.toString();
            }
        }
        return resolved;
    }

    public static BufferedImage readImage(String path) throws IOException {
        return readImage(path, instaController.getInstance().getInsta());
    }

    public static BufferedImage readImage(String path, instaManager manager) throws IOException {
        if (path != null && path.startsWith("insta://")) {
            if (manager == null) throw new IOException("No hay conexión con INSTA+.");
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(manager.readMedia(path)));
            if (image == null) throw new IOException("Imagen remota inválida.");
            return image;
        }
        String resolved = resolvePath(path);
        if (resolved == null || resolved.isBlank() || !new File(resolved).isFile()) {
            throw new IOException("No se encontró el archivo de imagen: " + resolved);
        }
        BufferedImage image = ImageIO.read(new File(resolved));
        if (image == null) {
            throw new IOException("Formato de imagen no compatible: " + new File(resolved).getName());
        }
        return image;
    }

    static boolean isCarousel(String mediaReference) {
        return decode(mediaReference).size() > 1;
    }
}
