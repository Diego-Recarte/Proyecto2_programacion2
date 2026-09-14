package Instagram.sockets;

import Instagram.InstaPostMedia;
import Logica.Excepciones.CuentaDesactivadaException;
import Logica.Decodificacion.InstaRepository;
import Logica.Estructuras.ListaEnlazada;
import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Servidor central de INSTA+: el cliente nunca abre los archivos .ins. */
public final class InstaServer implements AutoCloseable {
    public static final int DEFAULT_PORT = 5051;
    private final InstaRepository repository;
    private final ServerSocket listener;
    private final ChatServer chat;
    private final FileChannel lockChannel;
    private final FileLock fileLock;
    private final ExecutorService workers = Executors.newCachedThreadPool(task -> {
        Thread thread = new Thread(task, "insta-request"); thread.setDaemon(true); return thread;
    });
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, Media> media = new ConcurrentHashMap<>();
    private volatile boolean running = true;
    private record Session(String user, long expires) { }
    private record Media(Path path, String owner, String original) { }

    public InstaServer(int port, int chatPort, Path root, boolean examples) throws IOException {
        Files.createDirectories(root);
        lockChannel = FileChannel.open(root.resolve("server.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock acquired;
        try { acquired = lockChannel.tryLock(); }
        catch (java.nio.channels.OverlappingFileLockException ex) { acquired = null; }
        if (acquired == null) { lockChannel.close(); throw new IOException("Otro servidor ya administra estos datos."); }
        fileLock = acquired;
        InstaRepository storage = null;
        ServerSocket socket = null;
        ChatServer messages = null;
        try {
            storage = new InstaRepository(root, examples);
            repository = storage;
            socket = new ServerSocket(port);
            listener = socket;
            messages = new ChatServer(chatPort, root.resolve("users"), this::validChatSession);
            chat = messages;
            chat.start();
        } catch (IOException | RuntimeException ex) {
            if (messages != null) messages.close();
            if (socket != null) socket.close();
            if (storage != null) storage.close();
            fileLock.release(); lockChannel.close(); throw ex;
        }
        Thread accept = new Thread(this::accept, "insta-server");
        accept.setDaemon(true); accept.start();
    }

    public int port() { return listener.getLocalPort(); }
    public int chatPort() { return chat.getPort(); }

    private void accept() {
        while (running) {
            try { Socket socket = listener.accept(); workers.submit(() -> handle(socket)); }
            catch (IOException ex) { if (running) System.err.println(ex.getMessage()); }
        }
    }

    private void handle(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            socket.setSoTimeout(15000);
            if (in.readInt() != InstaWire.MAGIC) return;
            String operation = in.readUTF();
            String token = in.readUTF();
            Object[] args = (Object[]) InstaWire.read(in);
            try {
                Object value;
                synchronized (repository) { value = dispatch(operation, token, args); }
                out.writeBoolean(true); InstaWire.write(out, value);
            } catch (Exception ex) {
                out.writeBoolean(false); out.writeUTF(ex.getClass().getSimpleName());
                out.writeUTF(ex.getMessage() == null ? "Solicitud inválida." : ex.getMessage());
            }
            out.flush();
        } catch (IOException | RuntimeException ignored) { }
    }

    private static String s(Object[] a, int i) { return (String) a[i]; }
    private static int n(Object[] a, int i) { return (Integer) a[i]; }
    private String authenticated(String token) throws IOException {
        Session session = sessions.get(token);
        if (session == null || session.expires < System.currentTimeMillis()) {
            sessions.remove(token); throw new IOException("La sesión venció. Inicia sesión otra vez.");
        }
        return session.user;
    }

    private boolean validChatSession(String user, String token) {
        synchronized (repository) {
            try { return user.equals(authenticated(token)) && repository.getStatusUser(user); }
            catch (IOException ex) { return false; }
        }
    }

    private void own(String user, String target) throws IOException {
        if (!user.equals(target)) throw new IOException("Esta acción corresponde a otra cuenta.");
    }

    private Object dispatch(String op, String token, Object[] a) throws IOException {
        if (op.equals("PING")) return "INSTA+";
        if (op.equals("LOGIN")) {
            InstaRepository.validateUsername(s(a, 0));
            String password = repository.getPassword(s(a, 0));
            if (password == null || !password.equals(s(a, 1))) throw new IOException("Usuario o contraseña incorrectos.");
            String id = UUID.randomUUID().toString();
            sessions.put(id, new Session(s(a, 0), System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12)));
            return new String[]{id, String.valueOf(repository.getStatusUser(s(a, 0))), String.valueOf(chatPort())};
        }
        if (op.equals("REGISTER")) {
            Path image = stage(s(a, 5), (byte[]) a[6]);
            try { repository.addNewUser(s(a, 0), (Character) a[1], s(a, 2), s(a, 3), n(a, 4), image == null ? null : image.toString()); }
            finally { if (image != null) Files.deleteIfExists(image); }
            return null;
        }
        if (op.equals("checkUserExistance")) {
            InstaRepository.validateUsername(s(a, 0)); return repository.checkUserExistance(s(a, 0));
        }
        if (Set.of("getRealName", "getGender", "getAge", "getEntryDate", "getStatusUser", "getProfilePic",
                "getFollowers", "getFollowing", "getFollowersCount", "getFollowingCount", "showFollowers", "showFollows",
                "getPosts", "getComments", "getLikeCount", "hasLiked", "toggleLike", "addComment", "deletePost",
                "activateUser", "desactivateUser", "updateProfile", "getStickers", "importSticker", "uploadImage",
                "getPersonalFolders", "getFolderImages", "createPersonalFolder", "deletePersonalFolder", "isFollowing",
                "addFollow", "quitarFollow", "getFeedPosts", "getMentions", "findPostsMentioning").contains(op)) {
            InstaRepository.validateUsername(s(a, 0));
        }
        String user = authenticated(token);
        if (op.equals("LOGOUT")) { sessions.remove(token); return null; }
        repository.setLoggedUser(user);
        if (op.equals("activateUser")) { own(user, s(a, 0)); return repository.activateUser(user); }
        boolean active = repository.getStatusUser(user);
        if (!active && !Set.of("getStatusUser", "getRealName", "getGender", "getAge", "getEntryDate", "getProfilePic").contains(op)) {
            throw new CuentaDesactivadaException();
        }
        if (Set.of("getRealName", "getGender", "getAge", "getEntryDate", "getProfilePic",
                "getFollowers", "getFollowing", "getFollowersCount", "getFollowingCount", "showFollowers", "showFollows",
                "getLikeCount", "hasLiked").contains(op) && !user.equals(s(a, 0))
                && !repository.getStatusUser(s(a, 0))) {
            throw new IOException("La cuenta no está disponible.");
        }
        return switch (op) {
            case "getRealName" -> repository.getRealName(s(a, 0));
            case "getGender" -> repository.getGender(s(a, 0));
            case "getAge" -> repository.getAge(s(a, 0));
            case "getEntryDate" -> repository.getEntryDate(s(a, 0));
            case "getStatusUser" -> repository.getStatusUser(s(a, 0));
            case "getProfilePic" -> export(repository.getProfilePic(s(a, 0)), s(a, 0));
            case "getFollowers" -> repository.getFollowers(s(a, 0));
            case "getFollowing" -> repository.getFollowing(s(a, 0));
            case "getFollowersCount" -> repository.getFollowersCount(s(a, 0));
            case "getFollowingCount" -> repository.getFollowingCount(s(a, 0));
            case "showFollowers" -> repository.showFollowers(s(a, 0));
            case "showFollows" -> repository.showFollows(s(a, 0));
            case "isFollowing" -> repository.isFollowing(s(a, 0));
            case "addFollow" -> repository.addFollow(s(a, 0));
            case "quitarFollow" -> { repository.quitarFollow(s(a, 0)); yield null; }
            case "searchUsers" -> repository.searchUsers(s(a, 0));
            case "getPosts" -> posts(repository.getPosts(s(a, 0)));
            case "getFeedPosts" -> { own(user, s(a, 0)); yield posts(repository.getFeedPosts(user)); }
            case "getPostsByHashtag", "searchHashtag" -> posts(repository.getPostsByHashtag(s(a, 0)));
            case "getMentions", "findPostsMentioning" -> posts(repository.getMentions(s(a, 0)));
            case "getHashtagSuggestions" -> repository.getHashtagSuggestions(s(a, 0), n(a, 1));
            case "getComments" -> repository.getStatusUser(s(a, 0))
                    ? repository.getComments(s(a, 0), resolve(s(a, 1))) : new ListaEnlazada<>();
            case "getLikeCount" -> repository.getLikeCount(s(a, 0), resolve(s(a, 1)));
            case "hasLiked" -> repository.hasLiked(s(a, 0), resolve(s(a, 1)), s(a, 2));
            case "toggleLike" -> {
                own(user, s(a, 2));
                if (!repository.getStatusUser(s(a, 0))) throw new CuentaDesactivadaException();
                yield repository.toggleLike(s(a, 0), resolve(s(a, 1)), user);
            }
            case "addComment" -> {
                own(user, s(a, 2));
                if (!repository.getStatusUser(s(a, 0))) throw new CuentaDesactivadaException();
                repository.addComment(s(a, 0), resolve(s(a, 1)), user, s(a, 3)); yield null;
            }
            case "deletePost" -> { own(user, s(a, 0)); yield repository.deletePost(user, resolve(s(a, 1))); }
            case "addPost" -> {
                own(user, s(a, 1));
                if (s(a, 0) != null && s(a, 0).startsWith("text:v1:")) throw new IOException("Crea una nueva publicación de texto.");
                if (InstaPostMedia.decode(s(a, 0)).size() > 20) throw new IOException("Máximo 20 adjuntos.");
                repository.addPost(resolve(s(a, 0)), user, s(a, 2)); yield null;
            }
            case "getPostsfromUser" -> repository.getPostsfromUser(resolve(s(a, 0)), s(a, 1));
            case "desactivateUser" -> { own(user, s(a, 0)); yield repository.desactivateUser(user); }
            case "updateProfile" -> {
                own(user, s(a, 0)); Path image = stage(s(a, 4), (byte[]) a[5]);
                try { yield repository.updateProfile(user, s(a, 1), (Character) a[2], n(a, 3), image == null ? null : image.toString()); }
                finally { if (image != null) Files.deleteIfExists(image); }
            }
            case "getStickers" -> {
                own(user, s(a, 0)); ArrayList<String[]> result = repository.getStickers(user);
                for (String[] sticker : result) sticker[1] = export(sticker[1], user);
                yield result;
            }
            case "importSticker" -> {
                own(user, s(a, 0)); Path image = stage(s(a, 1), (byte[]) a[2]);
                if (image == null) throw new IOException("Selecciona una imagen.");
                try { yield export(repository.importSticker(user, image.toFile()), user); }
                finally { Files.deleteIfExists(image); }
            }
            case "uploadImage" -> { own(user, s(a, 0)); yield export(repository.uploadImage(user, s(a, 1), (byte[]) a[2], s(a, 3)), user); }
            case "getPersonalFolders" -> { own(user, s(a, 0)); yield repository.getPersonalFolders(user); }
            case "createPersonalFolder" -> { own(user, s(a, 0)); repository.createPersonalFolder(user, s(a, 1)); yield null; }
            case "deletePersonalFolder" -> { own(user, s(a, 0)); repository.deletePersonalFolder(user, s(a, 1)); yield null; }
            case "getFolderImages" -> {
                own(user, s(a, 0)); ListaEnlazada<String> result = new ListaEnlazada<>();
                for (String path : repository.getFolderImages(user, s(a, 1))) result.add(export(path, user));
                yield result;
            }
            case "readMedia" -> {
                Media image = media.get(s(a, 0));
                if (image == null || !Files.isRegularFile(image.path)) throw new IOException("Imagen no disponible.");
                if (!repository.getStatusUser(image.owner)) throw new CuentaDesactivadaException();
                if (Files.size(image.path) > InstaWire.MAX_BYTES) throw new IOException("Imagen demasiado grande.");
                yield Files.readAllBytes(image.path);
            }
            default -> throw new IOException("Operación desconocida.");
        };
    }

    private Path stage(String filename, byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) return null;
        String name = filename == null ? "imagen.png" : filename.toLowerCase(Locale.ROOT);
        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg")) throw new IOException("Usa una imagen PNG o JPG.");
        if (bytes.length > 12 * 1024 * 1024 || javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes)) == null) throw new IOException("Imagen inválida o mayor a 12 MB.");
        Path folder = repository.root().resolve("uploads"); Files.createDirectories(folder);
        Path path = Files.createTempFile(folder, "upload-", name.endsWith(".png") ? ".png" : ".jpg");
        Files.write(path, bytes); return path;
    }

    private ListaEnlazada<String[]> posts(ListaEnlazada<String[]> rows) {
        for (String[] row : rows) row[0] = export(row[0], row[1]);
        return rows;
    }

    private String export(String raw, String owner) {
        if (raw != null && raw.startsWith("text:v1:")) return raw;
        if (raw == null || raw.isBlank() || raw.equals("futura referencia de imagen aqui")) return "";
        ListaEnlazada<String> refs = new ListaEnlazada<>();
        for (String path : InstaPostMedia.decode(raw)) {
            Path actual = Path.of(InstaPostMedia.resolvePath(path)).toAbsolutePath().normalize();
            String key = "insta://" + UUID.nameUUIDFromBytes((owner + "\n" + actual).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            media.put(key, new Media(actual, owner, path)); refs.add(key);
        }
        return InstaPostMedia.encode(refs);
    }

    private String resolve(String reference) throws IOException {
        if (reference != null && reference.matches("text:v1:[a-fA-F0-9-]{36}")) return reference;
        if (reference == null || reference.isBlank()) return "";
        ListaEnlazada<String> paths = new ListaEnlazada<>();
        for (String ref : InstaPostMedia.decode(reference)) {
            Media image = media.get(ref);
            if (image == null) throw new IOException("La referencia de imagen no pertenece al servidor.");
            // Conservar la identidad original, incluida la ruta relativa, en likes/comentarios.
            paths.add(image.original);
        }
        return InstaPostMedia.encode(paths);
    }

    @Override public void close() {
        running = false; sessions.clear(); chat.close(); workers.shutdownNow();
        try { listener.close(); repository.close(); fileLock.release(); lockChannel.close(); }
        catch (IOException ignored) { }
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("instagram.server.port", DEFAULT_PORT);
        int chatPort = Integer.getInteger("instagram.chat.port", 5050);
        InstaServer server = new InstaServer(port, chatPort, Path.of(System.getProperty("instagram.data.dir", "Instagram")), true);
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        System.out.println("INSTA+ servidor: datos " + server.port() + ", chat " + server.chatPort());
        Thread.currentThread().join();
    }
}
