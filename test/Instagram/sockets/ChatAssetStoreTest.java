package Instagram.sockets;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/** Verifica el almacenamiento de contactos, avatares y stickers; no su transporte por sockets. */
public final class ChatAssetStoreTest {

    private ChatAssetStoreTest() {
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("instagram-chat-assets-");
        try {
            Path users = Files.createDirectories(root.resolve("users"));
            writeUsers(root.resolve("users.ins"));
            byte[] image = png();
            Path ana = Files.createDirectories(users.resolve("ana"));
            Files.write(ana.resolve("profile.png"), image);
            ChatAssetStore store = new ChatAssetStore(users);

            List<ChatContact> contacts = store.contacts();
            require(contacts.size() == 1 && "ana".equals(contacts.getFirst().getUsername()),
                    "La lista de contactos es incorrecta.");
            require(Arrays.equals(image, contacts.getFirst().getAvatar()), "No se recuperó el avatar.");
            require(store.isActiveUser("ana") && !store.isActiveUser("inactivo"),
                    "No se respetó el estado de las cuentas.");
            require(store.stickers("ana").isEmpty(), "La cuenta nueva ya contiene stickers.");

            store.importSticker("ana", "prueba.png", image);
            List<ChatSticker> stickers = new ChatAssetStore(users).stickers("ana");
            require(stickers.size() == 1, "No se recuperó el sticker después de reabrir el almacén.");
            require("prueba".equals(stickers.getFirst().getName()), "El sticker importado cambió de nombre.");
            require(Arrays.equals(image, stickers.getFirst().getImage()), "La imagen del sticker cambió.");
            require(Files.size(ana.resolve("stickers.ins")) > 0,
                    "El registro binario de stickers no se creó.");
            try {
                store.importSticker("ana", "invalido.png", new byte[]{1, 2, 3});
                throw new AssertionError("Se aceptó un sticker que no contiene una imagen.");
            } catch (java.io.IOException expected) {
                require(store.stickers("ana").size() == 1, "El sticker inválido modificó el registro.");
            }
            System.out.println("OK: contactos activos, avatares y persistencia de stickers verificados.");
        } finally {
            // Solo se retiran los archivos del directorio temporal creado por esta prueba.
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void writeUsers(Path registry) throws Exception {
        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(registry))) {
            for (String username : List.of("ana", "inactivo")) {
                output.writeUTF(username);
                output.writeChar('F');
                output.writeUTF(username);
                output.writeUTF("clave");
                output.writeLong(System.currentTimeMillis());
                output.writeInt(20);
                output.writeBoolean(username.equals("ana"));
                output.writeUTF("");
            }
        }
    }

    private static byte[] png() throws Exception {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        image.setRGB(2, 2, Color.ORANGE.getRGB());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
