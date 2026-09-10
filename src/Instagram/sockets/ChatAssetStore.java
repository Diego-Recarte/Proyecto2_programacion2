package Instagram.sockets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/** Acceso del servidor a contactos, avatares y stickers del chat. */
final class ChatAssetStore {

    private static final int MAX_IMAGE_BYTES = 3 * 1024 * 1024;

    private final Path usersRoot;
    private final Path instagramRoot;

    ChatAssetStore(Path usersRoot) throws IOException {
        this.usersRoot = usersRoot.toAbsolutePath().normalize();
        Path parent = this.usersRoot.getParent();
        if (parent == null) {
            throw new IOException("La carpeta de usuarios no tiene una raíz válida.");
        }
        instagramRoot = parent;
        Files.createDirectories(this.usersRoot);
    }

    synchronized List<ChatContact> contacts() throws IOException {
        List<ChatContact> contacts = new ArrayList<>();
        Path registry = instagramRoot.resolve("users.ins");
        if (!Files.isRegularFile(registry)) {
            return contacts;
        }

        try (DataInputStream input = new DataInputStream(Files.newInputStream(registry))) {
            while (true) {
                try {
                    input.readUTF();
                    input.readChar();
                    String username = input.readUTF();
                    input.readUTF();
                    input.readLong();
                    input.readInt();
                    boolean active = input.readBoolean();
                    String profilePath = input.readUTF();
                    if (active) {
                        contacts.add(new ChatContact(username, readAvatar(username, profilePath)));
                    }
                } catch (EOFException end) {
                    break;
                }
            }
        }
        return contacts;
    }

    synchronized boolean isActiveUser(String username) throws IOException {
        for (ChatContact contact : contacts()) {
            if (contact.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    synchronized List<ChatSticker> stickers(String username) throws IOException {
        Path userDirectory = safeUserDirectory(username);
        Path registry = userDirectory.resolve("stickers.ins");
        List<ChatSticker> result = new ArrayList<>();
        if (!Files.isRegularFile(registry)) {
            return result;
        }

        try (RandomAccessFile input = new RandomAccessFile(registry.toFile(), "r")) {
            while (input.getFilePointer() < input.length()) {
                String name = input.readUTF();
                String storedPath = input.readUTF();
                boolean global = input.readBoolean();
                Path image = resolveSticker(userDirectory, name, storedPath, global);
                byte[] bytes = readSmallImage(image);
                if (bytes.length > 0) {
                    result.add(new ChatSticker(name, bytes));
                }
            }
        }
        return result;
    }

    synchronized void importSticker(String username, String fileName, byte[] imageBytes) throws IOException {
        if (imageBytes == null || imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES) {
            throw new IOException("El sticker debe ser una imagen de hasta 3 MB.");
        }
        String cleanName = safeFileName(fileName);
        String lower = cleanName.toLowerCase();
        if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) {
            throw new IOException("El sticker debe tener formato PNG o JPG.");
        }
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (decoded == null) {
            throw new IOException("El archivo no contiene una imagen válida.");
        }

        Path userDirectory = safeUserDirectory(username);
        Path personal = userDirectory.resolve("stickers_personales");
        Files.createDirectories(personal);
        Path destination = personal.resolve(System.currentTimeMillis() + "_" + cleanName).normalize();
        Files.write(destination, imageBytes, StandardOpenOption.CREATE_NEW);

        Path registry = userDirectory.resolve("stickers.ins");
        try (RandomAccessFile output = new RandomAccessFile(registry.toFile(), "rw")) {
            output.seek(output.length());
            output.writeUTF(stripExtension(cleanName));
            output.writeUTF(destination.toString());
            output.writeBoolean(false);
        }
    }

    private byte[] readAvatar(String username, String storedPath) throws IOException {
        Path candidate = null;
        try {
            if (storedPath != null && !storedPath.isBlank()) {
                candidate = Path.of(storedPath);
            }
        } catch (RuntimeException ignored) {
        }
        byte[] bytes = readSmallImage(candidate);
        if (bytes.length > 0) {
            return bytes;
        }

        Path userDirectory = safeUserDirectory(username);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(userDirectory, "profile.*")) {
            for (Path file : files) {
                bytes = readSmallImage(file);
                if (bytes.length > 0) {
                    return bytes;
                }
            }
        }
        return new byte[0];
    }

    private Path resolveSticker(Path userDirectory, String name, String storedPath, boolean global) {
        try {
            Path stored = Path.of(storedPath);
            if (Files.isRegularFile(stored)) {
                return stored;
            }
        } catch (RuntimeException ignored) {
        }
        if (global) {
            return instagramRoot.resolve("stickers_globales").resolve(safeName(name) + ".png");
        }
        Path personal = userDirectory.resolve("stickers_personales");
        if (Files.isDirectory(personal)) {
            try (DirectoryStream<Path> files = Files.newDirectoryStream(personal)) {
                for (Path file : files) {
                    if (stripExtension(file.getFileName().toString()).endsWith(safeFileName(name))) {
                        return file;
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private byte[] readSmallImage(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            return new byte[0];
        }
        long size = Files.size(path);
        if (size <= 0 || size > MAX_IMAGE_BYTES) {
            return new byte[0];
        }
        return Files.readAllBytes(path);
    }

    private Path safeUserDirectory(String username) throws IOException {
        if (username == null || username.isBlank() || username.contains("..")
                || username.contains("/") || username.contains("\\")) {
            throw new IOException("Nombre de usuario inválido.");
        }
        Path result = usersRoot.resolve(username).normalize();
        if (!result.startsWith(usersRoot)) {
            throw new IOException("Nombre de usuario inválido.");
        }
        Files.createDirectories(result);
        return result;
    }

    private static String safeFileName(String value) {
        String clean = value == null ? "sticker.png" : Path.of(value).getFileName().toString();
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "_");
        return clean.isBlank() ? "sticker.png" : clean;
    }

    private static String safeName(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    private static String stripExtension(String value) {
        int dot = value.lastIndexOf('.');
        return dot > 0 ? value.substring(0, dot) : value;
    }
}
