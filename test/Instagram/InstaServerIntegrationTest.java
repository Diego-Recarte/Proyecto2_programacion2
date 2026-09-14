package Instagram;

import Instagram.sockets.InstaServer;

import Instagram.sockets.ChatClient;
import Instagram.sockets.ChatMessage;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

/** Dos clientes TCP, datos aislados, autenticacion, medios y persistencia. */
public final class InstaServerIntegrationTest {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("insta-central-");
        Path picture = root.resolve("seleccion.png");
        ImageIO.write(new BufferedImage(12, 8, BufferedImage.TYPE_INT_RGB), "png", picture.toFile());
        try (InstaServer server = new InstaServer(0, 0, root.resolve("servidor"), true)) {
            instaManager ana = new instaManager("127.0.0.1", server.port());
            instaManager luis = new instaManager("127.0.0.1", server.port());
            expectFailure(() -> ana.searchUsers(""));
            ana.addNewUser("Ana", 'F', "ana", "Clave123", 21, picture.toString());
            luis.addNewUser("Luis", 'M', "luis", "Clave456", 23, null);
            expectFailure(() -> ana.authenticate("ana", "incorrecta"));
            require(ana.authenticate("ana", "Clave123"), "Login activo");
            luis.authenticate("luis", "Clave456");
            require(ana.searchUsers("").size() == 5, "Tres cuentas de ejemplo mas dos nuevas");
            require(ana.getPosts("noticias").size() == 1 && ana.getPosts("deporte").size() == 1
                    && ana.getPosts("entretenimiento").size() == 1, "Contenido de ejemplo");
            require(ana.readMedia(ana.getProfilePic("ana")).length > 0, "Foto de perfil desde servidor");
            ana.createPersonalFolder("ana", "Viajes");
            String image = ana.uploadImage("ana", picture.toFile(), "Viajes");
            ana.addPost(image, "ana", "Hola @luis #viajes");
            require(ana.getFolderImages("ana", "Viajes").size() == 1, "Imagen asignada a carpeta");
            expectFailure(() -> ana.createPersonalFolder("ana", "../fuera"));
            expectFailure(() -> luis.deletePersonalFolder("ana", "Viajes"));
            expectFailure(() -> ana.deletePersonalFolder("ana", "Viajes"));
            luis.addFollow("ana");
            require(ana.getFollowers("ana").contains("luis"), "Seguimiento compartido");
            String[] post = luis.getFeedPosts("luis").get(0);
            require(post[0].startsWith("insta://") && luis.readMedia(post[0]).length > 0, "Publicacion e imagen remotas");
            luis.addComment("ana", post[0], "luis", "Comentario remoto");
            require(ana.getComments("ana", image).size() == 1, "Identidad de imagen conserva comentarios");
            luis.toggleLike("ana", post[0], "luis");
            require(ana.getLikeCount("ana", image) == 1, "Likes compartidos");
            expectFailure(() -> luis.deletePost("ana", post[0]));
            expectFailure(() -> luis.addComment("ana", post[0], "ana", "Suplantacion"));
            require(luis.getMentions("luis").size() == 1, "Mencion exacta");
            ana.addPost("", "ana", "No mencionar a @luisito #viajeslargos");
            luis.addPost("", "luis", "Me menciono @luis");
            require(luis.findPostsMentioning("luis").size() == 1, "Sin prefijos ni publicaciones propias");
            require(luis.searchHashtag("viajes").size() == 1, "Busqueda alternativa exacta");
            expectFailure(() -> ana.addPost("", "ana", "x".repeat(221)));
            expectFailure(() -> ana.addPost(image, "ana", "x".repeat(221)));
            ana.addPost(image, "ana", "x".repeat(220));
            String sticker = ana.getStickers("ana").get(0)[1];
            ana.addPost(sticker, "ana", "Sticker en el timeline #feliz");
            require(luis.readMedia(luis.searchHashtag("feliz").get(0)[0]).length > 0, "Sticker publicado");

            ana.addPost("", "ana", "Primer texto independiente");
            ana.addPost("", "ana", "Segundo texto independiente");
            String firstText = "", secondText = "";
            for (String[] row : ana.getPosts("ana")) {
                if (row[3].equals("Primer texto independiente")) firstText = row[0];
                if (row[3].equals("Segundo texto independiente")) secondText = row[0];
            }
            require(!firstText.isEmpty() && !secondText.isEmpty() && !firstText.equals(secondText), "Identidades independientes para textos");
            luis.addComment("ana", firstText, "luis", "Solo al primero");
            require(ana.getComments("ana", secondText).isEmpty(), "Comentarios de textos no se mezclan");
            ana.deletePost("ana", firstText);
            require(ana.getPosts("ana").stream().anyMatch(row -> row[3].equals("Segundo texto independiente")), "Borrar texto conserva otros textos");
            ana.deletePost("ana", secondText);

            ExecutorService workers = Executors.newFixedThreadPool(4);
            try {
                java.util.List<Future<?>> tasks = new java.util.ArrayList<>();
                for (int i = 0; i < 16; i++) tasks.add(workers.submit(() -> {
                    try { require(ana.getRealName("luis").equals("Luis"), "Registro concurrente"); ana.searchUsers("a"); }
                    catch (Exception ex) { throw new RuntimeException(ex); }
                }));
                for (Future<?> task : tasks) task.get(10, TimeUnit.SECONDS);
            } finally { workers.shutdownNow(); }

            CountDownLatch received = new CountDownLatch(1);
            CountDownLatch connected = new CountDownLatch(1);
            try (ChatClient sender = new ChatClient("127.0.0.1", server.chatPort(), "ana", ana.sessionToken());
                 ChatClient recipient = new ChatClient("127.0.0.1", server.chatPort(), "luis", luis.sessionToken())) {
                recipient.addListener(new ChatClient.Listener() {
                    public void onMessage(ChatMessage m) { if (m.getContent().equals("Hola por TCP")) received.countDown(); }
                    public void onOnlineUsers(java.util.Set<String> users) { if (users.contains("ana") && users.contains("luis")) connected.countDown(); }
                });
                recipient.connect(); sender.connect();
                require(connected.await(5, TimeUnit.SECONDS), "Chat autenticado");
                sender.send(new ChatMessage("ana", "luis", ChatMessage.Type.TEXT, "Hola por TCP"));
                require(received.await(5, TimeUnit.SECONDS), "Mensaje entregado");
                require(Files.size(root.resolve("servidor/users/luis/inbox.ins")) > 8, "Inbox binario central");
                ana.desactivateUser("ana");
                require(luis.searchUsers("ana").isEmpty() && luis.getPosts("ana").isEmpty(), "Desactivacion oculta datos");
                expectFailure(() -> luis.readMedia(image));
                expectFailure(() -> luis.toggleLike("ana", image, "luis"));
                ana.activateUser("ana");
                require(luis.searchUsers("ana").size() == 1, "Reactivacion");
            }
            luis.loggoutUser(); expectFailure(() -> luis.searchUsers(""));
            System.out.println("OK: servidor central, sesiones, 2 clientes, carpetas, medios, stickers, busquedas, concurrencia e Inbox.");
        }
        try (InstaServer restarted = new InstaServer(0, 0, root.resolve("servidor"), true)) {
            instaManager client = new instaManager("127.0.0.1", restarted.port());
            client.authenticate("ana", "Clave123");
            require(client.getFollowers("ana").contains("luis"), "Persistencia tras reiniciar");
            require(client.getPosts("noticias").size() == 1 && client.searchUsers("").size() == 5, "Ejemplos sin duplicados");
            require(client.readMedia(client.getPosts("ana").get(0)[0]).length > 0, "Imagen tras reiniciar");
        }
        System.out.println("OK: reinicio y compatibilidad de los binarios persistentes.");
    }
    private interface Operation { void run() throws Exception; }
    private static void expectFailure(Operation op) throws Exception {
        try { op.run(); throw new AssertionError("Se acepto una operacion invalida"); }
        catch (java.io.IOException expected) { }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
