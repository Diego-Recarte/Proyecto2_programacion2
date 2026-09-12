package Instagram;

import Logica.ManejoUsuarios.UserManager;
import Logica.ManejoUsuarios.UserUtilities;
import Logica.RutasSistema;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import proyecto2_programacion2.ArchivoUsuarioWin;
import proyecto2_programacion2.UsuarioWin;

/** Ejecutar en un directorio temporal para no modificar los usuarios reales. */
public final class RutasCompartidasTest {

    public static void main(String[] args) throws Exception {
        require(!RutasSistema.Z.exists(), "La prueba necesita un directorio vacío.");
        new UserManager();
        try {
            UsuarioWin usuario = new UsuarioWin("compartido", "prueba".toCharArray(), false, 19,"Masculino");
            UserManager.addUser("compartido", "prueba");
            ArchivoUsuarioWin archivo = new ArchivoUsuarioWin();
            archivo.agregarUsuario(usuario);
            require(archivo.UsuarioExiste("compartido"), "No se guardó el usuario del sistema.");
            require("compartido".equals(UserManager.getName("compartido")), "No se guardó el usuario anterior.");

            UserUtilities utilidades = new UserUtilities("compartido", "prueba");
            utilidades.createInicialDirs();
            require(new File(utilidades.getUserRoute()).equals(RutasSistema.usuario("compartido")),
                    "Los módulos usan carpetas distintas.");
            require(RutasSistema.imagenes("compartido").isDirectory(), "Falta misImagenes.");
            require(RutasSistema.documentos("compartido").isDirectory(), "Falta misDocumentos.");
            require(RutasSistema.musica("compartido").isDirectory(), "Falta musica.");
            require(!new File("src/Z").exists(), "Se recreó la segunda Z.");
            require(!new File(RutasSistema.usuario("compartido"), "Mis Imagenes").exists(),
                    "Se creó una segunda carpeta de imágenes.");

            File foto = new File(RutasSistema.imagenes("compartido"), "foto.png");
            Files.write(foto.toPath(), new byte[]{1, 2, 3});
            String anterior = "C:\\equipo-anterior\\proyecto\\src\\Z\\Usuarios\\compartido\\Mis Imagenes\\foto.png";
            require(new File(InstaPostMedia.coverPath(anterior)).equals(foto.getAbsoluteFile()),
                    "No se resolvió la foto de una publicación anterior.");
            String carrusel = InstaPostMedia.encode(List.of(anterior, "otra.png"));
            require(InstaPostMedia.coverPath(carrusel).equals(foto.getAbsolutePath()),
                    "No se resolvió la portada del carrusel anterior.");
            require(InstaPostMedia.decode(carrusel).get(0).equals(anterior),
                    "Cambió la referencia que identifica los likes y comentarios.");
            require("otra.png".equals(RutasSistema.resolverRutaAnterior("otra.png")),
                    "Cambió una ruta que no pertenece a Z anterior.");
            System.out.println("OK: una sola Z, carpetas compartidas y fotos anteriores compatibles.");
        } finally {
            UserManager.getManagerRoute().close();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
