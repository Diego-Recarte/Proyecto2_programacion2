package Instagram;

import Instagram.sockets.InstaServer;

import Logica.Estructuras.ListaEnlazada;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingUtilities;

/** Comprueba la paginacion real de Swing sin abrir ventanas ni conectar sockets. */
public final class InstaFeedListsTest {
    public static void main(String[] args) throws Exception {
        require(!Files.exists(Path.of("Instagram")), "La prueba requiere un directorio vacio");
        try (InstaServer server = new InstaServer(0, 0, Path.of("Instagram"), false)) {
        instaManager manager = new instaManager("127.0.0.1", server.port());
        manager.addNewUser("Ana", 'F', "ana", "Clave123", 20, null);
        manager.authenticate("ana", "Clave123");
        instaController.getInstance().setInsta(manager);
        SwingUtilities.invokeAndWait(() -> {
            try {
                InstaFeedUI feed = new InstaFeedUI("ana");
                Method render = method("renderFeed", ListaEnlazada.class);
                Method append = method("appendPosts", int.class);
                Method same = method("samePosts", ListaEnlazada.class, ListaEnlazada.class);
                ListaEnlazada<String[]> posts = new ListaEnlazada<>();
                for (int i = 0; i < 13; i++) {
                    posts.add(new String[]{"", "ana", "01/01/2026 10:00", "Post " + i, "ana"});
                }
                render.invoke(feed, posts);
                require((int) field(feed, "renderedPosts") == 5, "Primera pagina");
                require(field(feed, "feedPosts") instanceof ListaEnlazada, "La vista debe conservar nodos");
                append.invoke(feed, 5);
                require((int) field(feed, "renderedPosts") == 10, "Segunda pagina");
                append.invoke(feed, 5);
                require((int) field(feed, "renderedPosts") == 13, "Ultima pagina incompleta");
                append.invoke(feed, 5);
                require((int) field(feed, "renderedPosts") == 13, "No duplicar al terminar");
                require((boolean) same.invoke(feed, posts, new ListaEnlazada<>(posts)), "Comparacion por recorrido");
                ListaEnlazada<String[]> updated = new ListaEnlazada<>();
                updated.add(new String[]{"", "ana", "02/01/2026 10:00", "Nueva", "ana"});
                require(!(boolean) same.invoke(feed, posts, updated), "Detectar nuevos posts");
                render.invoke(feed, updated);
                require((int) field(feed, "renderedPosts") == 1, "Reiniciar el iterador al refrescar");
                render.invoke(feed, new ListaEnlazada<String[]>());
                require((int) field(feed, "renderedPosts") == 0, "Refresco vacio");
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(ex);
            }
        });
        System.out.println("OK: feed Swing enlazado, paginacion 5/5/3, comparacion y refresco.");
        }
    }

    private static Method method(String name, Class<?>... parameters) throws NoSuchMethodException {
        Method method = InstaFeedUI.class.getDeclaredMethod(name, parameters);
        method.setAccessible(true);
        return method;
    }

    private static Object field(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
