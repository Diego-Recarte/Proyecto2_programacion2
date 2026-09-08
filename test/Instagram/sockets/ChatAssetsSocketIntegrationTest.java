package Instagram.sockets;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;

/** Verifica que contactos y stickers no sean leídos directamente por el cliente. */
public final class ChatAssetsSocketIntegrationTest {

    private ChatAssetsSocketIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("instagram-chat-assets-");
        Path users = Files.createDirectories(root.resolve("users"));
        writeUser(root.resolve("users.ins"), "ana");

        int port = 15051;
        ChatServer server = new ChatServer(port, users);
        ChatClient client = new ChatClient("127.0.0.1", port, "ana");
        CountDownLatch contactsReady = new CountDownLatch(1);
        CountDownLatch stickersReady = new CountDownLatch(1);
        AtomicReference<List<ChatContact>> contacts = new AtomicReference<>();
        AtomicReference<List<ChatSticker>> stickers = new AtomicReference<>();
        client.addListener(new ChatClient.Listener() {
            @Override
            public void onContacts(List<ChatContact> value) {
                contacts.set(value);
                contactsReady.countDown();
            }

            @Override
            public void onStickers(List<ChatSticker> value) {
                stickers.set(value);
                if (!value.isEmpty()) {
                    stickersReady.countDown();
                }
            }
        });

        try {
            server.start();
            client.connect();
            client.requestContacts();
            require(contactsReady.await(3, TimeUnit.SECONDS), "No llegaron los contactos por socket.");
            require(contacts.get().size() == 1 && "ana".equals(contacts.get().getFirst().getUsername()),
                    "La lista de contactos es incorrecta.");

            client.importSticker("prueba.png", png());
            require(stickersReady.await(3, TimeUnit.SECONDS), "No se importó el sticker por socket.");
            require("prueba".equals(stickers.get().getFirst().getName()), "El sticker importado cambió de nombre.");
            require(Files.size(users.resolve("ana").resolve("stickers.ins")) > 0,
                    "El registro binario de stickers no se creó.");
            System.out.println("OK: contactos, avatares e importación de stickers viajan por sockets.");
        } finally {
            client.close();
            server.close();
        }
    }

    private static void writeUser(Path registry, String username) throws Exception {
        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(registry))) {
            output.writeUTF("Ana");
            output.writeChar('F');
            output.writeUTF(username);
            output.writeUTF("clave");
            output.writeLong(System.currentTimeMillis());
            output.writeInt(20);
            output.writeBoolean(true);
            output.writeUTF("");
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
