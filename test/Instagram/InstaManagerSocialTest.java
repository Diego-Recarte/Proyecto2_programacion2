package Instagram;

/** Prueba ejecutable de edición, corazones y eliminación de publicaciones. */
public final class InstaManagerSocialTest {

    private InstaManagerSocialTest() {
    }

    public static void main(String[] args) throws Exception {
        instaManager manager = new instaManager();
        manager.addNewUser("Ana Inicial", 'F', "ana", "Clave123", 20, null);
        manager.addNewUser("Luis", 'M', "luis", "Clave456", 22, null);

        manager.setLoggedUser("ana");
        manager.addPost("imagen-prueba.png", "ana", "Mi publicación");
        require(manager.getPosts("ana").size() == 1, "No se creó la publicación.");
        require(manager.getFeedPosts("ana").size() == 1, "El timeline no incluyó la publicación propia.");

        manager.setLoggedUser("luis");
        manager.addPost("imagen-luis.png", "luis", "Publicación seguida");
        manager.setLoggedUser("ana");
        require(manager.getFeedPosts("ana").size() == 1, "El timeline mostró una cuenta no seguida.");
        require(manager.addFollow("luis"), "No se pudo seguir al usuario de prueba.");
        require(manager.getFeedPosts("ana").size() == 2, "El timeline no incluyó la cuenta seguida.");

        require(manager.getLikeCount("ana", "imagen-prueba.png") == 0, "El contador inicial no es cero.");
        require(manager.toggleLike("ana", "imagen-prueba.png", "luis") == 1, "No se agregó el corazón.");
        require(manager.hasLiked("ana", "imagen-prueba.png", "luis"), "El corazón no quedó registrado.");
        require(manager.toggleLike("ana", "imagen-prueba.png", "luis") == 0, "No se retiró el corazón.");

        require(manager.updateProfile("ana", "Ana Editada", 'O', 25, null), "No se actualizó el perfil.");
        require("Ana Editada".equals(manager.getRealName("ana")), "No cambió el nombre.");
        require(manager.getAge("ana") == 25 && manager.getGender("ana") == 'O', "No cambiaron edad/género.");
        require("Clave123".equals(manager.getPassword("ana")), "La edición alteró la contraseña.");

        manager.addComment("ana", "imagen-prueba.png", "luis", "Comentario");
        require(manager.deletePost("ana", "imagen-prueba.png"), "No se eliminó la publicación.");
        require(manager.getPosts("ana").isEmpty(), "La publicación sigue visible.");
        require(manager.getComments("ana", "imagen-prueba.png").isEmpty(), "Los comentarios huérfanos no se limpiaron.");

        System.out.println("OK: timeline, edición de perfil, corazones y eliminación verificados.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
