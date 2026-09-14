package Instagram;

import Instagram.sockets.InstaServer;
import Instagram.sockets.InstaWire;
import Logica.Excepciones.CuentaDesactivadaException;

import Logica.Estructuras.ListaEnlazada;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;

/** Fachada cliente: todas las operaciones persistentes se solicitan por TCP. */
@SuppressWarnings("unchecked")
public class instaManager {
    private final String host;
    private final int port;
    private final boolean embedded;
    private volatile String token = "";
    private volatile String user;
    private volatile int chatPort = 5050;
    private static InstaServer localServer;

    public instaManager() {
        this.host = System.getProperty("instagram.server.host", System.getProperty("instagram.chat.host", "127.0.0.1"));
        this.port = Integer.getInteger("instagram.server.port", InstaServer.DEFAULT_PORT);
        this.embedded = true;
    }

    public instaManager(String host, int port) {
        this.host = host; this.port = port; this.embedded = false;
    }

    private static synchronized void ensureLocal(int port) throws IOException {
        try (Socket probe = new Socket()) {
            probe.connect(new InetSocketAddress("127.0.0.1", port), 300);
            return;
        } catch (IOException absent) {
            if (localServer == null) {
                localServer = new InstaServer(port, Integer.getInteger("instagram.chat.port", 5050),
                        Path.of(System.getProperty("instagram.data.dir", "Instagram")), true);
                Runtime.getRuntime().addShutdownHook(new Thread(localServer::close));
            }
        }
    }

    Object call(String operation, Object... args) throws IOException {
        if (embedded && (host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost") || host.equals("::1"))) ensureLocal(port);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.setSoTimeout(15000);
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.writeInt(InstaWire.MAGIC); output.writeUTF(operation); output.writeUTF(token);
            InstaWire.write(output, args); output.flush();
            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            if (!input.readBoolean()) {
                String type = input.readUTF(); String message = input.readUTF();
                if (type.equals("CuentaDesactivadaException")) throw new CuentaDesactivadaException();
                throw new IOException(message);
            }
            return InstaWire.read(input);
        }
    }

    public boolean authenticate(String username, String password) throws IOException {
        String[] result = (String[]) call("LOGIN", username, password);
        token = result[0]; user = username; chatPort = Integer.parseInt(result[2]);
        instaController.getInstance().remember(username, this);
        return Boolean.parseBoolean(result[1]);
    }

    public void setLoggedUser(String username) throws IOException {
        if (user == null || !user.equals(username)) throw new IOException("Inicia sesión con esta cuenta primero.");
    }

    public void loggoutUser() {
        try { if (!token.isBlank()) call("LOGOUT"); } catch (IOException ignored) { }
        instaController.getInstance().forget(user, this);
        token = ""; user = null;
    }

    public String sessionToken() { return token; }
    public String serverHost() { return host; }
    public int chatPort() { return chatPort; }

    private static byte[] imageBytes(String path) throws IOException {
        if (path == null || path.isBlank()) return new byte[0];
        Path file = Path.of(path);
        if (Files.size(file) > 12 * 1024 * 1024) throw new IOException("La imagen supera 12 MB.");
        return Files.readAllBytes(file);
    }
    private static String filename(String path) { return path == null || path.isBlank() ? "" : Path.of(path).getFileName().toString(); }

    public void addNewUser(String name, char gender, String username, String password, int age, String picture) throws IOException {
        call("REGISTER", name, gender, username, password, age, filename(picture), imageBytes(picture));
    }
    public boolean updateProfile(String username, String name, char gender, int age, String picture) throws IOException {
        return (Boolean) call("updateProfile", username, name, gender, age, filename(picture), imageBytes(picture));
    }
    public String importSticker(String username, File source) throws IOException {
        return (String) call("importSticker", username, source.getName(), imageBytes(source.getPath()));
    }
    public byte[] readMedia(String reference) throws IOException { return (byte[]) call("readMedia", reference); }
    public String uploadImage(String username, File source, String folder) throws IOException {
        return (String) call("uploadImage", username, source.getName(), imageBytes(source.getPath()), folder);
    }
    public ListaEnlazada<String> getPersonalFolders(String username) throws IOException {
        return (ListaEnlazada<String>) call("getPersonalFolders", username);
    }
    public void createPersonalFolder(String username, String folder) throws IOException { call("createPersonalFolder", username, folder); }
    public void deletePersonalFolder(String username, String folder) throws IOException { call("deletePersonalFolder", username, folder); }
    public ListaEnlazada<String> getFolderImages(String username, String folder) throws IOException {
        return (ListaEnlazada<String>) call("getFolderImages", username, folder);
    }

    public ArrayList<String[]> getStickers(String username) throws IOException { return new ArrayList<>((ListaEnlazada<String[]>) call("getStickers" ,username)); }
    public String getRealName(String username) throws IOException { return (String) call("getRealName" ,username); }
    public char getGender(String username) throws IOException { return (char) call("getGender" ,username); }
    public int getAge(String username) throws IOException { return (int) call("getAge" ,username); }
    public String getEntryDate(String username) throws IOException { return (String) call("getEntryDate" ,username); }
    public boolean getStatusUser(String username) throws IOException { return (boolean) call("getStatusUser" ,username); }
    public String getProfilePic(String username) throws IOException { return (String) call("getProfilePic" ,username); }
    public boolean checkUserExistance(String username) throws IOException { return (boolean) call("checkUserExistance" ,username); }
    public boolean activateUser(String username) throws IOException { return (boolean) call("activateUser" ,username); }
    public boolean desactivateUser(String username) throws IOException { return (boolean) call("desactivateUser" ,username); }
    public ListaEnlazada<String> getFollowers(String username) throws IOException { return (ListaEnlazada<String>) call("getFollowers" ,username); }
    public ListaEnlazada<String> getFollowing(String username) throws IOException { return (ListaEnlazada<String>) call("getFollowing" ,username); }
    public boolean addFollow(String usernameToFollow) throws IOException { return (boolean) call("addFollow" ,usernameToFollow); }
    public void quitarFollow(String usernameToUnfollow) throws IOException { call("quitarFollow" ,usernameToUnfollow); }
    public String showFollowers(String username) throws IOException { return (String) call("showFollowers" ,username); }
    public String showFollows(String username) throws IOException { return (String) call("showFollows" ,username); }
    public void addPost(String imagRef, String autor, String contenido) throws IOException { call("addPost" ,imagRef, autor, contenido); }
    public ListaEnlazada<String[]> getPosts(String username) throws IOException { return (ListaEnlazada<String[]>) call("getPosts" ,username); }
    public ListaEnlazada<String[]> getFeedPosts(String viewer) throws IOException { return (ListaEnlazada<String[]>) call("getFeedPosts" ,viewer); }
    public int toggleLike(String postOwner, String imagePath, String username) throws IOException { return (int) call("toggleLike" ,postOwner, imagePath, username); }
    public boolean hasLiked(String postOwner, String imagePath, String username) throws IOException { return (boolean) call("hasLiked" ,postOwner, imagePath, username); }
    public int getLikeCount(String postOwner, String imagePath) throws IOException { return (int) call("getLikeCount" ,postOwner, imagePath); }
    public boolean deletePost(String postOwner, String imagePath) throws IOException { return (boolean) call("deletePost" ,postOwner, imagePath); }
    public String getPostsfromUser(String imagReferencia, String username) throws IOException { return (String) call("getPostsfromUser" ,imagReferencia, username); }
    public void addComment(String postOwner, String imagePath, String author, String comment) throws IOException { call("addComment" ,postOwner, imagePath, author, comment); }
    public ArrayList<String[]> getComments(String postOwner, String imagePath) throws IOException { return new ArrayList<>((ListaEnlazada<String[]>) call("getComments" ,postOwner, imagePath)); }
    public ListaEnlazada<String> searchUsers(String query) throws IOException { return (ListaEnlazada<String>) call("searchUsers" ,query); }
    public int getFollowersCount(String username) throws IOException { return (int) call("getFollowersCount" ,username); }
    public int getFollowingCount(String username) throws IOException { return (int) call("getFollowingCount" ,username); }
    public boolean isFollowing(String targetUsername) throws IOException { return (boolean) call("isFollowing" ,targetUsername); }
    public ListaEnlazada<String[]> getMentions(String user) throws IOException { return (ListaEnlazada<String[]>) call("getMentions" ,user); }
    public ListaEnlazada<String[]> getPostsByHashtag(String tag) throws IOException { return (ListaEnlazada<String[]>) call("getPostsByHashtag" ,tag); }
    public ListaEnlazada<String> getHashtagSuggestions(String prefix, int limit) throws IOException { return (ListaEnlazada<String>) call("getHashtagSuggestions" ,prefix, limit); }
    public ListaEnlazada<String[]> findPostsMentioning(String username) throws IOException { return (ListaEnlazada<String[]>) call("findPostsMentioning" ,username); }
    public ListaEnlazada<String[]> searchHashtag(String tag) throws IOException { return (ListaEnlazada<String[]>) call("searchHashtag" ,tag); }
}
