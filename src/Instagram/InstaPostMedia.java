package Instagram;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/** Codifica varias imágenes dentro del campo de imagen existente del post. */
final class InstaPostMedia {

    private static final String CAROUSEL_PREFIX = "carousel:v1:";

    private InstaPostMedia() {
    }

    static String encode(List<String> imagePaths) {
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

    static List<String> decode(String mediaReference) {
        if (mediaReference == null || mediaReference.isBlank()) {
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
        return paths.isEmpty() ? "" : paths.get(0);
    }

    static boolean isCarousel(String mediaReference) {
        return decode(mediaReference).size() > 1;
    }
}
