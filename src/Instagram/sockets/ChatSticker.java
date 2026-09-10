package Instagram.sockets;

import java.util.Arrays;

/** Sticker transportado como datos binarios, sin compartir rutas del servidor. */
public final class ChatSticker {

    private final String name;
    private final byte[] image;

    public ChatSticker(String name, byte[] image) {
        this.name = name;
        this.image = image != null ? Arrays.copyOf(image, image.length) : new byte[0];
    }

    public String getName() {
        return name;
    }

    public byte[] getImage() {
        return Arrays.copyOf(image, image.length);
    }
}
