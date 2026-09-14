package Instagram;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Ejecutar en un directorio temporal vacío para no modificar usuarios reales. */
public final class InstaProfilePathTest {

    public static void main(String[] args) throws Exception {
        require(!Files.exists(Path.of("Instagram")), "La prueba necesita un directorio vacío.");
        Path legacyPhoto = Path.of("Instagram/users/anterior/profile.png");
        Files.createDirectories(legacyPhoto.getParent());
        BufferedImage photo = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(photo, "png", legacyPhoto.toFile());
        try (RandomAccessFile users = new RandomAccessFile("Instagram/users.ins", "rw")) {
            users.writeUTF("Anterior");
            users.writeChar('M');
            users.writeUTF("anterior");
            users.writeUTF("Clave123");
            users.writeLong(0);
            users.writeInt(20);
            users.writeBoolean(true);
            users.writeUTF("C:\\otro-equipo\\proyecto\\Instagram\\users\\anterior\\profile.png");
        }

        instaManager manager = new instaManager();
        require(new File(manager.getProfilePic("anterior")).isFile(),
                "No se recuperó la foto de perfil guardada desde otro equipo.");
        Path source = Path.of("foto seleccionada.png");
        ImageIO.write(photo, "png", source.toFile());
        manager.addNewUser("Ana", 'F', "ana", "Clave123", 20, source.toAbsolutePath().toString());
        verifyStoredPhoto(manager, "Instagram/users/ana/profile.png");

        Path replacement = Path.of("otra foto.jpg");
        ImageIO.write(photo, "jpg", replacement.toFile());
        require(manager.updateProfile("ana", "Ana Editada", 'F', 21,
                replacement.toAbsolutePath().toString()), "No se actualizó el perfil.");
        verifyStoredPhoto(manager, "Instagram/users/ana/profile.jpg");
        require(manager.updateProfile("ana", "Ana", 'F', 21, null), "No se pudo editar sin cambiar foto.");
        verifyStoredPhoto(manager, "Instagram/users/ana/profile.jpg");
        System.out.println("OK: foto de perfil relativa al registrar y editar, y compatibilidad con rutas antiguas.");
    }

    private static void verifyStoredPhoto(instaManager manager, String expected) throws Exception {
        require(expected.equals(manager.getProfilePic("ana")), "La carga no conserva la ruta relativa.");
        require(ImageIO.read(new File(manager.getProfilePic("ana"))) != null, "La foto no se puede leer.");
        try (RandomAccessFile users = new RandomAccessFile("Instagram/users.ins", "r")) {
            while (users.getFilePointer() < users.length()) {
                users.readUTF();
                users.readChar();
                String username = users.readUTF();
                users.readUTF();
                users.readLong();
                users.readInt();
                users.readBoolean();
                String picture = users.readUTF();
                if (username.equals("ana")) {
                    require(expected.equals(picture), "Se guardó una ruta absoluta en users.ins.");
                    return;
                }
            }
        }
        throw new AssertionError("No se encontró el usuario guardado.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
