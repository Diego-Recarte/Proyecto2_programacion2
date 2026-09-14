package Instagram;

import Logica.Decodificacion.InstaRepository;

import Logica.Estructuras.ListaEnlazada;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Ejecutar desde un directorio vacio para no tocar cuentas reales. */
public final class InstaLinkedListsTest {
    public static void main(String[] args) throws Exception {
        require(!Files.exists(Path.of("Instagram")), "La prueba requiere un directorio vacio");
        InstaRepository manager = new InstaRepository(java.nio.file.Path.of("Instagram"), false);
        for (String user : List.of("ana", "patito", "elpatitoloco", "oculto", "fuera")) {
            manager.addNewUser(user, 'F', user, "Clave123", 21, null);
        }
        manager.setLoggedUser("ana");
        require(manager.getFeedPosts("ana").isEmpty(), "Timeline vacio");
        require(manager.getFollowers("ana").isEmpty(), "Followers vacios");
        require(!manager.addFollow("ana") && !manager.addFollow("inexistente"), "Seguimiento invalido");
        require(manager.addFollow("patito"), "Agregar seguimiento");
        require(!manager.addFollow("patito"), "Evitar seguimiento duplicado");
        require(manager.addFollow("elpatitoloco") && manager.addFollow("oculto"), "Mas seguimientos");
        require(manager.getFollowing("ana").equals(List.of("patito", "elpatitoloco", "oculto")), "Lista following");
        require(manager.getFollowers("patito").equals(List.of("ana")), "Lista followers reciproca");
        require(manager.getFollowingCount("ana") == 3 && manager.getFollowersCount("patito") == 1,
                "Contadores desde listas");
        require(manager.isFollowing("patito") && "ana\n".equals(manager.showFollowers("patito")), "Consulta y listado");
        require(manager.showFollows("ana").equals("patito\nelpatitoloco\noculto\n"), "Texto following");

        // Archivos en el formato binario previo; las listas deben poder cargarlos.
        post("ana", "propia", "01/01/2026 10:00", "Mi #viajes");
        post("patito", "seguida", "03/01/2026 10:00", "#Viajes #viajes con @ana");
        post("elpatitoloco", "anterior", "02/01/2026 10:00", "#viajeslargos");
        post("oculto", "inactiva", "04/01/2026 10:00", "#viajes @ana");
        post("fuera", "ajena", "05/01/2026 10:00", "Otro tema");
        manager.desactivateUser("oculto");
        ListaEnlazada<String[]> feed = manager.getFeedPosts("ana");
        require(feed.size() == 3 && feed.get(0)[0].equals("seguida")
                && feed.get(1)[0].equals("anterior") && feed.get(2)[0].equals("propia"),
                "Timeline propio y seguido, descendente, sin inactivos ni ajenos");
        require(manager.getPosts("oculto").isEmpty(), "Publicaciones inactivas");
        ListaEnlazada<String> usuarios = manager.searchUsers("PATITO");
        require(usuarios.equals(List.of("patito", "elpatitoloco")), "Busqueda parcial de personas");
        require(manager.searchUsers("oculto").isEmpty() && manager.searchUsers(null).isEmpty(), "Busqueda inactiva/nula");

        // Repetir un registro y el hashtag no debe duplicar la publicacion encontrada.
        post("patito", "seguida", "03/01/2026 10:00", "#Viajes #viajes con @ana");
        ListaEnlazada<String[]> hashtags = manager.getPostsByHashtag("#VIAJES");
        require(hashtags.size() == 2, "Hashtag sin duplicados, prefijos ajenos ni cuentas inactivas");
        require(manager.getPostsByHashtag(null).isEmpty() && manager.getPostsByHashtag("#").isEmpty(), "Hashtag vacio");
        require(manager.getHashtagSuggestions("via", 1).equals(List.of("#viajes")), "Sugerencias enlazadas ordenadas");
        require(manager.searchHashtag("viajes") instanceof ListaEnlazada, "Busqueda alternativa enlazada");
        require(manager.getMentions("ana") instanceof ListaEnlazada
                && manager.findPostsMentioning("ana") instanceof ListaEnlazada, "Resultados de menciones enlazados");

        manager.quitarFollow("elpatitoloco");
        manager.quitarFollow("patito");
        manager.quitarFollow("oculto");
        manager.quitarFollow("patito");
        require(manager.getFollowing("ana").isEmpty() && manager.getFollowers("patito").isEmpty(),
                "Eliminar medio, cabeza, cola y seguimiento ausente");
        require(manager.addFollow("patito"), "Reinsertar despues de vaciar");
        InstaRepository recargado = new InstaRepository(java.nio.file.Path.of("Instagram"), false);
        recargado.setLoggedUser("ana");
        require(recargado.isFollowing("patito") && recargado.getFollowersCount("patito") == 1,
                "Persistencia de las dos listas al recargar");
        // Los registros UTF existentes se deduplican en memoria sin migrar el archivo.
        try (RandomAccessFile file = new RandomAccessFile("Instagram/users/patito/followers.ins", "rw")) {
            file.seek(file.length());
            file.writeUTF("ana");
        }
        require(recargado.getFollowersCount("patito") == 1, "No contar registros antiguos duplicados");
        recargado.quitarFollow("patito");
        require(Files.size(Path.of("Instagram/users/patito/followers.ins")) == 0, "Persistir eliminacion de duplicados");
        System.out.println("OK: listas reales en timeline, followers/following y busquedas; binarios compatibles.");
    }

    private static void post(String owner, String image, String date, String content) throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("Instagram/users/" + owner + "/insta.ins", "rw")) {
            file.seek(file.length());
            file.writeUTF(image);
            file.writeUTF(owner);
            file.writeUTF(date);
            file.writeUTF(content);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
