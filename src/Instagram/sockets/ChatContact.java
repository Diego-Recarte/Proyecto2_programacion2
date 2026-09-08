package Instagram.sockets;

import java.util.Arrays;

/** Datos públicos de un contacto entregados por el servidor de chat. */
public final class ChatContact {

    private final String username;
    private final byte[] avatar;

    public ChatContact(String username, byte[] avatar) {
        this.username = username;
        this.avatar = avatar != null ? Arrays.copyOf(avatar, avatar.length) : new byte[0];
    }

    public String getUsername() {
        return username;
    }

    public byte[] getAvatar() {
        return Arrays.copyOf(avatar, avatar.length);
    }
}
