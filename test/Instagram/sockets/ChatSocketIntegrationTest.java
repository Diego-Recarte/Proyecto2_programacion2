package Instagram.sockets;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Prueba ejecutable sin dependencias externas para el transporte socket. */
public final class ChatSocketIntegrationTest {

    private ChatSocketIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        int port = 15050;
        Path history = Path.of(System.getProperty("java.io.tmpdir"), "instagram-chat-test-" + System.nanoTime());
        ChatServer server = new ChatServer(port, history);
        ChatClient ana = new ChatClient("127.0.0.1", port, "ana");
        ChatClient luis = new ChatClient("127.0.0.1", port, "luis");
        ChatClient luisReconnected = new ChatClient("127.0.0.1", port, "luis");
        CountDownLatch usersReady = new CountDownLatch(1);
        CountDownLatch delivered = new CountDownLatch(1);
        CountDownLatch unreadReceived = new CountDownLatch(1);
        AtomicReference<ChatMessage> received = new AtomicReference<>();

        luis.addListener(new ChatClient.Listener() {
            @Override
            public void onOnlineUsers(Set<String> users) {
                if (users.contains("ana") && users.contains("luis")) {
                    usersReady.countDown();
                }
            }

            @Override
            public void onMessage(ChatMessage message) {
                if ("ana".equals(message.getSender())) {
                    received.set(message);
                    delivered.countDown();
                }
            }

            @Override
            public void onUnreadCount(int count) {
                if (count == 1) {
                    unreadReceived.countDown();
                }
            }
        });

        try {
            server.start();
            ana.connect();
            luis.connect();
            require(usersReady.await(3, TimeUnit.SECONDS), "No se sincronizó la lista de usuarios.");

            ana.send(new ChatMessage("ana", "luis", ChatMessage.Type.TEXT, "Hola 👋"));
            require(delivered.await(3, TimeUnit.SECONDS), "El mensaje no llegó al destinatario.");
            require(unreadReceived.await(3, TimeUnit.SECONDS), "No se notificó el mensaje sin leer.");
            require("Hola 👋".equals(received.get().getContent()), "El contenido UTF-8 cambió durante el envío.");

            luis.close();
            CountDownLatch historyFinished = new CountDownLatch(1);
            CountDownLatch markedRead = new CountDownLatch(1);
            CountDownLatch deleted = new CountDownLatch(1);
            AtomicReference<ChatMessage> restored = new AtomicReference<>();
            luisReconnected.addListener(new ChatClient.Listener() {
                @Override
                public void onMessage(ChatMessage message) {
                    if ("ana".equals(message.getSender())) {
                        restored.set(message);
                    }
                }

                @Override
                public void onHistoryFinished(String peer) {
                    historyFinished.countDown();
                }

                @Override
                public void onUnreadCount(int count) {
                    if (count == 0) {
                        markedRead.countDown();
                    }
                }

                @Override
                public void onConversationDeleted(String peer) {
                    if ("ana".equals(peer)) {
                        deleted.countDown();
                    }
                }
            });
            luisReconnected.connect();
            luisReconnected.requestHistory("ana");
            require(historyFinished.await(3, TimeUnit.SECONDS), "El servidor no terminó de entregar el historial.");
            require(restored.get() != null && "Hola 👋".equals(restored.get().getContent()),
                    "El mensaje no se recuperó del historial persistente.");
            require(markedRead.await(3, TimeUnit.SECONDS), "El historial no se marcó como leído.");

            luisReconnected.deleteConversation("ana");
            require(deleted.await(3, TimeUnit.SECONDS), "No se confirmó la eliminación de la conversación.");
            require(new ChatHistoryStore(history).between("luis", "ana").isEmpty(),
                    "La conversación eliminada sigue en la bandeja del usuario.");

            System.out.println("OK: sockets, UTF-8, no leídos, historial binario y eliminación verificados.");
        } finally {
            ana.close();
            luis.close();
            luisReconnected.close();
            server.close();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
