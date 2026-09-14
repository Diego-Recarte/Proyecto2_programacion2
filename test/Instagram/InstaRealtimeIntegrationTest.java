package Instagram;

import Instagram.sockets.InstaServer;

import Instagram.sockets.ChatClient;
import Instagram.sockets.ChatMessage;
import Instagram.sockets.ChatServer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/** Ejecutar en un directorio temporal: crea exclusivamente usuarios de prueba. */
public final class InstaRealtimeIntegrationTest {
    private static JFrame frame;

    public static void main(String[] args) throws Exception {
        if (Files.exists(Path.of("Instagram"))) {
            throw new IllegalStateException("Ejecutar en una carpeta vacía para proteger los datos existentes.");
        }
        int port;
        try (ServerSocket available = new ServerSocket(0)) { port = available.getLocalPort(); }
        System.setProperty("instagram.chat.port", Integer.toString(port));
        InstaServer central = new InstaServer(0, port, Path.of("Instagram"), false);
        instaManager manager = new instaManager("127.0.0.1", central.port());
        instaManager anaManager = new instaManager("127.0.0.1", central.port());
        instaController.getInstance().setInsta(manager);
        manager.addNewUser("Ana", 'F', "ana", "Clave123", 20, null);
        manager.addNewUser("Luis", 'M', "luis", "Clave123", 20, null);
        manager.authenticate("luis", "Clave123");
        anaManager.authenticate("ana", "Clave123");
        manager.addFollow("ana");
        BufferedImage picture = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 13; i++) {
            File file = new File("post-" + i + ".png");
            ImageIO.write(picture, "png", file);
            manager.addPost(manager.uploadImage("luis", file, ""), "luis", "Publicación de prueba " + i);
        }
        InstaFeedUI[] feed = new InstaFeedUI[1];
        try (ChatClient ana = new ChatClient("127.0.0.1", port, "ana", anaManager.sessionToken())) {
            edt(() -> {
                frame = new JFrame("Prueba Insta+");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                feed[0] = new InstaFeedUI("luis");
                frame.setContentPane(feed[0]);
                frame.pack();
            });
            await(() -> number(feed[0], "renderedPosts") == 5, "El feed debe cargar solo la primera página.");
            InstaSession session = InstaSession.find(feed[0]);
            await(() -> session.client() != null && session.client().isConnected(), "Sesión sin conexión.");
            ana.connect();
            await(() -> online(session, "ana"), "Falta registrar al emisor.");
            ana.send(new ChatMessage("ana", "luis", ChatMessage.Type.TEXT, "Hola desde otra pestaña"));
            await(() -> session.unreadCount() == 1 && toastCount() == 1, "Falta aviso o contador en el feed.");
            edt(() -> {
                require(messageButtons(frame).stream().allMatch(b -> b.getIcon() instanceof InstaMessageBadge),
                        "El feed no usa el contador compartido.");
                saveComponent(feed[0], "feed-badge.png");
                for (Window window : Window.getWindows()) {
                    if (window instanceof JWindow toast && toast.isVisible()) saveComponent(toast.getContentPane(), "toast.png");
                }
                frame.setContentPane(new InstaProfileUI("luis"));
                frame.pack();
                require(InstaSession.find(frame.getContentPane()) == session, "Cambiar pestaña creó otra sesión.");
                require(!messageButtons(frame).isEmpty(), "Falta acceso a mensajes en el perfil.");
                frame.setContentPane(new InstaChatUI("luis"));
                frame.pack();
            });
            require(session.unreadCount() == 1, "Abrir la bandeja marcó mensajes sin leer.");
            edt(() -> { frame.setContentPane(new InstaChatUI("luis", "ana")); frame.pack(); });
            await(() -> number(frame.getContentPane(), "unreadCount") == 1, "El chat no recibió el contador inicial.");
            Thread.sleep(350);
            require(session.unreadCount() == 1, "Cargar historial sin foco marcó mensajes como leídos.");
            require(onEdt(() -> toastCount() == 1), "El historial produjo un aviso nuevo.");
            session.client().markRead("ana");
            await(() -> session.unreadCount() == 0, "El contador no se limpió tras leer.");
            edt(() -> { frame.setContentPane(feed[0]); frame.pack(); });
            JScrollPane scroll = (JScrollPane) field(feed[0], "feedScroll");
            edt(() -> scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum()));
            await(() -> number(feed[0], "renderedPosts") == 10, "No se cargó la segunda página.");
            edt(() -> scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum()));
            await(() -> number(feed[0], "renderedPosts") == 13, "No se cargó la última página.");
            anaManager.addPost(anaManager.uploadImage("ana", new File("post-0.png"), ""), "ana", "Nueva publicación seguida");
            await(() -> ((JButton) field(feed[0], "newPosts")).isVisible(), "Falta el aviso de nuevos posts al leer abajo.");
            require(number(feed[0], "renderedPosts") == 13, "Un post nuevo alteró la página que se estaba leyendo.");
            await(() -> toastCount() == 2, "Falta la notificación de publicación seguida.");
            edt(() -> ((JButton) field(feed[0], "newPosts")).doClick());
            await(() -> number(feed[0], "renderedPosts") == 5, "Ver nuevos posts no reinició la paginación.");
            ana.send(new ChatMessage("ana", "luis", ChatMessage.Type.TEXT, "La conexión sigue activa"));
            await(() -> session.unreadCount() == 1, "Se perdió la conexión al salir del chat.");
            edt(() -> frame.setContentPane(new InstaLoginUI()));
            await(() -> InstaSession.find(frame.getContentPane()) == null && !session.client().isConnected(),
                    "Cerrar sesión dejó la conexión activa.");
            require(onEdt(() -> toastCount() == 0), "Cerrar sesión dejó avisos abiertos.");
            System.out.println("OK: paginación, avisos, historial, contador entre pestañas y cierre de sesión.");
        } finally {
            edt(() -> { if (frame != null) frame.dispose(); });
            central.close();
        }
    }

    private static java.util.List<JButton> messageButtons(Container parent) {
        java.util.List<JButton> result = new java.util.ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (child instanceof JButton button && button.getToolTipText() != null
                    && button.getToolTipText().startsWith("Mensajes")) result.add(button);
            if (child instanceof Container container) result.addAll(messageButtons(container));
        }
        return result;
    }
    private static void saveComponent(Component component, String path) {
        try {
            BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D graphics = image.createGraphics();
            component.printAll(graphics);
            graphics.dispose();
            ImageIO.write(image, "png", new File(path));
        } catch (Exception ex) { throw new AssertionError(ex); }
    }
    private static boolean online(InstaSession session, String user) {
        return ((java.util.Set<?>) field(session, "online")).contains(user);
    }
    private static int toastCount() {
        int count = 0;
        for (Window window : Window.getWindows()) if (window instanceof JWindow && window.isVisible()) count++;
        return count;
    }
    private static int number(Object object, String name) { return (Integer) field(object, name); }
    private static Object field(Object object, String name) {
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception ex) { throw new AssertionError(ex); }
    }
    private static void edt(Runnable runnable) throws Exception { SwingUtilities.invokeAndWait(runnable); }
    private static boolean onEdt(BooleanSupplier condition) throws Exception {
        AtomicReference<Boolean> result = new AtomicReference<>();
        edt(() -> result.set(condition.getAsBoolean()));
        return result.get();
    }
    private static void await(BooleanSupplier condition, String message) throws Exception {
        long deadline = System.nanoTime() + 8_000_000_000L;
        while (!onEdt(condition)) {
            if (System.nanoTime() > deadline) throw new AssertionError(message);
            Thread.sleep(50);
        }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
