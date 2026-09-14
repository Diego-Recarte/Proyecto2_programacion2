package Logica.Persistencia;

import Instagram.InstaPostMedia;
import Instagram.InstaSocialText;
import Logica.Excepciones.ArchivoCorruptoException;
import Logica.Excepciones.CuentaDesactivadaException;
import Logica.Excepciones.UsuarioDuplicadoException;
import Logica.Modelos.Publicacion;
import Logica.Modelos.Usuario;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import Logica.Estructuras.ListaEnlazada;
import java.util.Calendar;
import java.util.Date;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public final class InstaRepository implements AutoCloseable {

    private RandomAccessFile users;
    private final String mainRoot;
    private final String usersDir;
    private File loggedUserDir = null;
    private String loggedUser = null;
    private StickerRepository stickerRepository;

    public java.nio.file.Path root() { return java.nio.file.Path.of(mainRoot); }

    public static void validateUsername(String user) throws IOException {
        if (user == null || !user.matches("[A-Za-z0-9_]{1,40}")) {
            throw new IOException("El username admite letras, números y guion bajo (máximo 40).");
        }
    }

    private File personalFolder(String user, String name) throws IOException {
        validateUsername(user);
        if (name == null || !name.matches("[\\p{L}\\p{N} _-]{1,50}")) {
            throw new IOException("La carpeta admite letras, números, espacios, guion y guion bajo.");
        }
        return new File(new File(new File(usersDir, user), "folders_personales"), name);
    }

    public synchronized ListaEnlazada<String> getPersonalFolders(String user) throws IOException {
        validateUsername(user);
        ListaEnlazada<String> result = new ListaEnlazada<>();
        File[] folders = new File(new File(usersDir, user), "folders_personales").listFiles(File::isDirectory);
        if (folders != null) for (File folder : folders) result.add(folder.getName());
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    public synchronized void createPersonalFolder(String user, String name) throws IOException {
        Files.createDirectories(personalFolder(user, name).toPath());
    }

    public synchronized ListaEnlazada<String> getFolderImages(String user, String name) throws IOException {
        ListaEnlazada<String> result = new ListaEnlazada<>();
        File[] images = personalFolder(user, name).listFiles(File::isFile);
        if (images != null) for (File image : images) result.add(image.getPath());
        return result;
    }

    public synchronized void deletePersonalFolder(String user, String name) throws IOException {
        // Solo se eliminan carpetas vacias; no se borran fotos del usuario.
        Files.delete(personalFolder(user, name).toPath());
    }

    public synchronized String uploadImage(String user, String filename, byte[] bytes, String folder) throws IOException {
        validateUsername(user);
        if (!getStatusUser(user)) throw new CuentaDesactivadaException();
        if (bytes == null || bytes.length == 0 || bytes.length > 12 * 1024 * 1024) {
            throw new IOException("La imagen debe ocupar entre 1 byte y 12 MB.");
        }
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
        if (image == null) throw new IOException("El archivo no contiene una imagen válida.");
        File selectedFolder = folder == null || folder.isBlank() ? null : personalFolder(user, folder);
        if (selectedFolder != null && !selectedFolder.isDirectory()) throw new IOException("La carpeta personal no existe.");
        File images = new File(new File(usersDir, user), "imagenes");
        Files.createDirectories(images.toPath());
        File destination = new File(images, java.util.UUID.randomUUID() + ".png");
        javax.imageio.ImageIO.write(image, "png", destination);
        if (selectedFolder != null) Files.copy(destination.toPath(), new File(selectedFolder, destination.getName()).toPath());
        return destination.getPath().replace('\\', '/');
    }

    private void sortPosts(ListaEnlazada<String[]> posts) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        posts.sort((a, b) -> {
            try { return format.parse(b[2]).compareTo(format.parse(a[2])); }
            catch (java.text.ParseException ex) { return 0; }
        });
    }

    private void seedExamples() throws IOException {
        File marker = new File(mainRoot, "examples-v1.ins");
        if (marker.exists()) return;
        String[][] examples = {
            {"noticias", "Noticias", "Bienvenidos al boletín de INSTA+ #noticias #actualidad"},
            {"deporte", "Deporte", "Un día para entrenar y disfrutar #deporte #bienestar"},
            {"entretenimiento", "Entretenimiento", "Películas, música y buenos momentos #cine #musica"}
        };
        for (int i = 0; i < examples.length; i++) {
            String[] example = examples[i];
            if (checkUserExistance(example[0])) continue;
            addNewUser(example[1], 'F', example[0], "Demo123!", 25, null);
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(640, 640, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D graphics = image.createGraphics();
            graphics.setColor(new java.awt.Color(35 + i * 35, 55, 95 + i * 40));
            graphics.fillRect(0, 0, 640, 640);
            graphics.setColor(java.awt.Color.WHITE);
            graphics.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 44));
            graphics.drawString(example[1], 35, 320);
            graphics.dispose();
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", bytes);
            String path = uploadImage(example[0], "ejemplo.png", bytes.toByteArray(), "");
            setLoggedUser(example[0]);
            addPost(path, example[0], example[2]);
        }
        loggoutUser();
        try (java.io.DataOutputStream output = new java.io.DataOutputStream(new java.io.FileOutputStream(marker))) {
            output.writeInt(1);
        }
    }

    @Override public synchronized void close() throws IOException { if (users != null) users.close(); }

    public InstaRepository(java.nio.file.Path root, boolean demos) throws IOException {
        mainRoot = root.toString();
        usersDir = new File(mainRoot, "users").getPath();
        File instaFolder = new File(mainRoot);
        File usersDirF = new File(usersDir);

        if (!instaFolder.exists()) {
            instaFolder.mkdir();
        }
        if (!usersDirF.exists()) {
            usersDirF.mkdir();
        }

        try {
            stickerRepository = new StickerRepository(instaFolder, usersDirF);
            File[] existingUsers = usersDirF.listFiles(File::isDirectory);
            if (existingUsers != null) {
                for (File existingUser : existingUsers) {
                    initUserFiles(existingUser.getAbsolutePath());
                }
            }
            File f = new File(mainRoot + File.separator + "users.ins");
            if (!f.exists()) {
                f.createNewFile();
            }
            users = new RandomAccessFile(f, "rw");
            if (demos) seedExamples();
        } catch (IOException e) {
            throw e;
        }
    }

    public synchronized void addNewUser(String name, char genero, String username, String password, int edad, String profilePicPath) throws IOException {
        validateUsername(username);
        if (name == null || name.isBlank() || password == null || password.isBlank()
                || edad < 13 || edad > 120 || (genero != 'M' && genero != 'F')) {
            throw new IOException("Revisa nombre, contraseña, edad (13-120) y género (M/F).");
        }
        if (checkUserExistance(username)) {
            throw new UsuarioDuplicadoException(username);
        }

        users.seek(users.length());
        users.writeUTF(name);
        users.writeChar(genero);
        users.writeUTF(username);
        users.writeUTF(password);
        users.writeLong(Calendar.getInstance().getTimeInMillis());
        users.writeInt(edad);
        users.writeBoolean(true);

        String finalPath = "futura referencia de imagen aqui";
        File userDir = new File(usersDir, username);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        if (profilePicPath != null && !profilePicPath.isBlank()) {
            int dot = profilePicPath.lastIndexOf('.');
            String ext = (dot > 0) ? profilePicPath.substring(dot + 1) : "jpg";
            File destino = new File(userDir, "profile." + ext);
            Files.copy(new File(profilePicPath).toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            finalPath = destino.toPath().normalize().toString().replace('\\', '/');
        }

        users.writeUTF(finalPath);

        initUserFiles(userDir.getAbsolutePath());
    }

    private void initUserFiles(String userFolder) throws IOException {
        File fFollowers = new File(userFolder + File.separator + "followers.ins");
        File fFollowing = new File(userFolder + File.separator + "following.ins");
        File fPosts = new File(userFolder + File.separator + "insta.ins");
        File fComments = new File(userFolder + File.separator + "comments.ins");
        File fLikes = new File(userFolder + File.separator + "likes.ins");
        File fInbox = new File(userFolder + File.separator + "inbox.ins");

        if (!fFollowers.exists()) {
            fFollowers.createNewFile();
        }
        if (!fFollowing.exists()) {
            fFollowing.createNewFile();
        }
        if (!fPosts.exists()) {
            fPosts.createNewFile();
        }
        if (!fComments.exists()) {
            fComments.createNewFile();
        }
        if (!fLikes.exists()) {
            fLikes.createNewFile();
        }
        if (!fInbox.exists()) {
            fInbox.createNewFile();
        }
        if (stickerRepository != null) {
            stickerRepository.initializeUser(new File(userFolder));
        }
    }

    public synchronized ArrayList<String[]> getStickers(String username) throws IOException {
        if (stickerRepository == null) {
            throw new IOException("El repositorio de stickers no está disponible.");
        }
        return stickerRepository.list(username);
    }

    public synchronized String importSticker(String username, File source) throws IOException {
        if (stickerRepository == null) {
            throw new IOException("El repositorio de stickers no está disponible.");
        }
        return stickerRepository.importSticker(username, source);
    }

    /** Actualiza los datos editables sin alterar contraseña, fecha ni estado. */
    public synchronized boolean updateProfile(String username, String realName, char gender,
            int age, String newProfilePicPath) throws IOException {
        if (gender != 'M' && gender != 'F') throw new IOException("El género debe ser M o F.");
        if (realName == null || realName.isBlank()) {
            throw new IOException("El nombre no puede estar vacío.");
        }
        if (age < 13 || age > 120) {
            throw new IOException("La edad debe estar entre 13 y 120 años.");
        }

        ArrayList<Usuario> records = readUsuarios();
        Usuario selected = null;
        for (Usuario record : records) {
            if (record.getUsername().equals(username)) {
                selected = record;
                break;
            }
        }
        if (selected == null) {
            return false;
        }

        selected.setRealName(realName.trim());
        selected.setGender(gender);
        selected.setAge(age);

        if (newProfilePicPath != null && !newProfilePicPath.isBlank()) {
            File source = new File(newProfilePicPath);
            if (!source.isFile()) {
                throw new IOException("La nueva foto de perfil no existe.");
            }
            File userDirectory = new File(usersDir, username);
            Files.createDirectories(userDirectory.toPath());
            String fileName = source.getName();
            int dot = fileName.lastIndexOf('.');
            String extension = dot >= 0 ? fileName.substring(dot + 1).replaceAll("[^a-zA-Z0-9]", "") : "jpg";
            if (extension.isBlank()) {
                extension = "jpg";
            }
            File destination = new File(userDirectory, "profile." + extension.toLowerCase());
            if (!source.getCanonicalFile().equals(destination.getCanonicalFile())) {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            selected.setProfilePicture(destination.toPath().normalize().toString().replace('\\', '/'));
        }

        rewriteUsers(records);
        return true;
    }

    private ArrayList<Usuario> readUsuarios() throws IOException {
        ArrayList<Usuario> records = new ArrayList<>();
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            records.add(new Usuario(
                    users.readUTF(),
                    users.readChar(),
                    users.readUTF(),
                    users.readUTF(),
                    users.readLong(),
                    users.readInt(),
                    users.readBoolean(),
                    users.readUTF()
            ));
        }
        return records;
    }

    private void rewriteUsers(ArrayList<Usuario> records) throws IOException {
        File original = new File(mainRoot, "users.ins");
        File temporary = new File(mainRoot, "users.ins.tmp");
        try (RandomAccessFile output = new RandomAccessFile(temporary, "rw")) {
            output.setLength(0);
            for (Usuario record : records) {
                output.writeUTF(record.getRealName());
                output.writeChar(record.getGender());
                output.writeUTF(record.getUsername());
                output.writeUTF(record.getPassword());
                output.writeLong(record.getEntryDate());
                output.writeInt(record.getAge());
                output.writeBoolean(record.getActive());
                output.writeUTF(record.getProfilePicture());
            }
        }

        users.close();
        try {
            Files.move(temporary.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            users = new RandomAccessFile(original, "rw");
            Files.deleteIfExists(temporary.toPath());
        }
    }

    public synchronized void setLoggedUser(String username) throws IOException {
        File dir = new File(usersDir, username);
        if (!dir.exists()) {
            throw new IOException("Usuario no existe: " + username);
        }
        this.loggedUserDir = dir;
        this.loggedUser = username;
    }

    public synchronized void loggoutUser() {
        this.loggedUserDir = null;
        this.loggedUser = null;
    }

    public synchronized String getRealName(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            String rName = users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            if (uname.equals(username)) {
                return rName;
            }
            users.readUTF();
            users.readLong();
            users.readInt();
            users.readBoolean();
            users.readUTF();
        }
        return null;
    }

    public synchronized char getGender(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            char g = users.readChar();
            String uname = users.readUTF();
            if (uname.equals(username)) {
                return g;
            }
            users.readUTF();
            users.readLong();
            users.readInt();
            users.readBoolean();
            users.readUTF();
        }
        return 0;
    }

    public synchronized int getAge(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            if (uname.equals(username)) {
                users.readUTF();
                users.readLong();
                return users.readInt();
            }
            users.readUTF();
            users.readLong();
            users.readInt();
            users.readBoolean();
            users.readUTF();
        }
        return 0;
    }

    public synchronized String getEntryDate(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            String rName = users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            if (uname.equals(username)) {
                users.readUTF();
                long dateL = users.readLong();
                Date d = new Date(dateL);
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                return fmt.format(d);
            }
            users.readUTF();
            users.readLong();
            users.readInt();
            users.readBoolean();
            users.readUTF();
        }
        return null;
    }

    public synchronized boolean getStatusUser(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            users.readUTF();
            users.readLong();
            users.readInt();
            boolean status = users.readBoolean();
            users.readUTF();
            if (uname.equals(username)) {
                return status;
            }
        }
        return false;
    }

    public synchronized String getProfilePic(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            users.readUTF();
            users.readLong();
            users.readInt();
            users.readBoolean();
            String pic = users.readUTF();
            if (uname.equals(username)) {
                return InstaPostMedia.resolvePath(pic);
            }
        }
        return null;
    }

    public synchronized boolean checkUserExistance(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            if (uname.equals(username)) {
                return true;
            }
            users.readUTF();
            users.readLong();
            users.readInt();
            users.readBoolean();
            users.readUTF();
        }
        return false;
    }

    public synchronized boolean activateUser(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            long posBefore = users.getFilePointer();
            users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            users.readUTF();
            users.readLong();
            users.readInt();
            long boolPos = users.getFilePointer();
            boolean status = users.readBoolean();
            users.readUTF();
            if (uname.equals(username)) {
                users.seek(boolPos);
                users.writeBoolean(true);
                return true;
            }
        }
        return false;
    }

    public synchronized String getPassword(String username) throws IOException {
        users.seek(0);

        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            users.readChar();
            String nameUser = users.readUTF();

            if (nameUser.equals(username)) {
                String password = users.readUTF();
                return password;
            }

            users.readUTF();
            users.readLong();
            users.readInt();
            users.readBoolean();
            users.readUTF();
        }

        return null;
    }

    public synchronized boolean desactivateUser(String username) throws IOException {
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            users.readUTF();
            users.readLong();
            users.readInt();
            long boolPos = users.getFilePointer();
            boolean status = users.readBoolean();
            users.readUTF();
            if (uname.equals(username)) {
                users.seek(boolPos);
                users.writeBoolean(false);
                return true;
            }
        }
        return false;
    }

    /** Carga usernames del archivo binario en nodos propios, sin duplicados. */
    private ListaEnlazada<String> readUsernames(File file) throws IOException {
        ListaEnlazada<String> usernames = new ListaEnlazada<>();
        if (file.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                while (raf.getFilePointer() < raf.length()) {
                    String username = raf.readUTF();
                    if (!usernames.contains(username)) {
                        usernames.add(username);
                    }
                }
            }
        }
        return usernames;
    }

    private void writeUsernames(File file, ListaEnlazada<String> usernames) throws IOException {
        File temporary = new File(file.getParentFile(), file.getName() + ".temp");
        try (RandomAccessFile raf = new RandomAccessFile(temporary, "rw")) {
            raf.setLength(0);
            for (String username : usernames) {
                raf.writeUTF(username);
            }
        }
        Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public synchronized ListaEnlazada<String> getFollowers(String username) throws IOException {
        return readUsernames(new File(new File(usersDir, username), "followers.ins"));
    }

    public synchronized ListaEnlazada<String> getFollowing(String username) throws IOException {
        return readUsernames(new File(new File(usersDir, username), "following.ins"));
    }

    public synchronized boolean addFollow(String usernameToFollow) throws IOException {
        if (loggedUserDir == null || usernameToFollow == null
                || usernameToFollow.equals(loggedUser) || !getStatusUser(usernameToFollow)) {
            return false;
        }
        ListaEnlazada<String> following = getFollowing(loggedUser);
        if (following.contains(usernameToFollow)) {
            return false;
        }
        ListaEnlazada<String> followers = getFollowers(usernameToFollow);
        following.add(usernameToFollow);
        if (!followers.contains(loggedUser)) {
            followers.add(loggedUser);
        }
        writeUsernames(new File(loggedUserDir, "following.ins"), following);
        writeUsernames(new File(new File(usersDir, usernameToFollow), "followers.ins"), followers);
        return true;
    }

    public synchronized void quitarFollow(String usernameToUnfollow) throws IOException {
        if (loggedUserDir == null || usernameToUnfollow == null) {
            return;
        }
        ListaEnlazada<String> following = getFollowing(loggedUser);
        ListaEnlazada<String> followers = getFollowers(usernameToUnfollow);
        if (following.remove(usernameToUnfollow)) {
            writeUsernames(new File(loggedUserDir, "following.ins"), following);
        }
        if (followers.remove(loggedUser)) {
            writeUsernames(new File(new File(usersDir, usernameToUnfollow), "followers.ins"), followers);
        }
    }

    public synchronized String showFollowers(String username) throws IOException {
        StringBuilder text = new StringBuilder();
        for (String follower : getFollowers(username)) {
            text.append(follower).append("\n");
        }
        return text.toString();
    }

    public synchronized String showFollows(String username) throws IOException {
        StringBuilder text = new StringBuilder();
        for (String followed : getFollowing(username)) {
            text.append(followed).append("\n");
        }
        return text.toString();
    }

    public synchronized void addPost(String imagRef, String autor, String contenido) throws IOException {
        if (loggedUserDir == null) {
            throw new IOException("No hay usuario loggeado.");
        }
        int limit = imagRef == null || imagRef.isBlank() ? 140 : 220;
        if (contenido == null || contenido.isBlank()) {
            if (imagRef == null || imagRef.isBlank()) throw new IOException("Escribe una publicación o adjunta una imagen.");
        }
        if (contenido != null && contenido.length() > limit) {
            throw new IOException("El texto no puede superar " + limit + " caracteres.");
        }
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateFormat = formato.format(Calendar.getInstance().getTime());
        File postFile = new File(loggedUserDir, "insta.ins");
        if (!postFile.exists()) {
            postFile.createNewFile();
        }
        try (RandomAccessFile raf = new RandomAccessFile(postFile, "rw")) {
            raf.seek(raf.length());
            new Publicacion(imagRef == null || imagRef.isBlank() ? "text:v1:" + java.util.UUID.randomUUID() : imagRef, autor, dateFormat,
                    contenido == null ? "" : contenido).write(raf);
        }
    }

    public synchronized ListaEnlazada<String[]> getPosts(String username) throws IOException {
        ListaEnlazada<String[]> posts = new ListaEnlazada<>();
        try {
            if (!getStatusUser(username)) {
                return posts;
            }
        } catch (IOException e) {
            return posts;
        }

        File userPath = new File(usersDir, username);
        File fileOriginal = new File(userPath, "insta.ins");
        if (!fileOriginal.exists()) {
            return posts;
        }
        try (RandomAccessFile raf = new RandomAccessFile(fileOriginal, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                String[] post = Publicacion.read(raf).toArray();
                posts.add(post);
            }
        } catch (java.io.EOFException ex) {
            throw new ArchivoCorruptoException(fileOriginal.getName(), ex);
        }
        posts.reverse();
        sortPosts(posts);
        return posts;
    }

    /**
     * Construye el timeline con publicaciones propias y de cuentas seguidas.
     */
    public synchronized ListaEnlazada<String[]> getFeedPosts(String viewer) throws IOException {
        ListaEnlazada<String[]> feed = new ListaEnlazada<>();
        ListaEnlazada<String> visibleUsers = getFollowing(viewer);
        if (!visibleUsers.contains(viewer)) {
            visibleUsers.add(viewer);
        }

        File usersRoot = new File(usersDir);
        File[] userFolders = usersRoot.listFiles(File::isDirectory);
        if (userFolders == null) {
            return feed;
        }

        for (File userFolder : userFolders) {
            String owner = userFolder.getName();
            if (!visibleUsers.contains(owner) || !getStatusUser(owner)) {
                continue;
            }

            for (String[] post : getPosts(owner)) {
                feed.add(new String[]{
                    post.length > 0 ? post[0] : "",
                    post.length > 1 ? post[1] : owner,
                    post.length > 2 ? post[2] : "",
                    post.length > 3 ? post[3] : "",
                    owner
                });
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        format.setLenient(false);
        Comparator<String[]> newestFirst = (left, right) -> {
            try {
                long leftDate = format.parse(left[2]).getTime();
                long rightDate = format.parse(right[2]).getTime();
                return Long.compare(rightDate, leftDate);
            } catch (Exception ex) {
                return 0;
            }
        };
        feed.sort(newestFirst);
        return feed;
    }

    /** Alterna el corazón de un usuario y devuelve el nuevo total. */
    public synchronized int toggleLike(String postOwner, String imagePath, String username) throws IOException {
        File likesFile = getLikesFile(postOwner);
        ArrayList<String[]> likes = new ArrayList<>();
        boolean removed = false;
        try (RandomAccessFile input = new RandomAccessFile(likesFile, "r")) {
            while (input.getFilePointer() < input.length()) {
                String storedPath = input.readUTF();
                String storedUser = input.readUTF();
                if (storedPath.equals(imagePath) && storedUser.equals(username)) {
                    removed = true;
                } else {
                    likes.add(new String[]{storedPath, storedUser});
                }
            }
        }
        if (!removed) {
            likes.add(new String[]{imagePath, username});
        }
        rewritePairs(likesFile, likes);
        return getLikeCount(postOwner, imagePath);
    }

    public synchronized boolean hasLiked(String postOwner, String imagePath, String username) throws IOException {
        File likesFile = getLikesFile(postOwner);
        try (RandomAccessFile input = new RandomAccessFile(likesFile, "r")) {
            while (input.getFilePointer() < input.length()) {
                String storedPath = input.readUTF();
                String storedUser = input.readUTF();
                if (storedPath.equals(imagePath) && storedUser.equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized int getLikeCount(String postOwner, String imagePath) throws IOException {
        File likesFile = getLikesFile(postOwner);
        Set<String> usersWhoLiked = new HashSet<>();
        try (RandomAccessFile input = new RandomAccessFile(likesFile, "r")) {
            while (input.getFilePointer() < input.length()) {
                String storedPath = input.readUTF();
                String storedUser = input.readUTF();
                if (storedPath.equals(imagePath)) {
                    usersWhoLiked.add(storedUser);
                }
            }
        }
        return usersWhoLiked.size();
    }

    /** Borra el post y sus comentarios/corazones asociados. */
    public synchronized boolean deletePost(String postOwner, String imagePath) throws IOException {
        File userDirectory = new File(usersDir, postOwner);
        File postsFile = new File(userDirectory, "insta.ins");
        if (!postsFile.exists()) {
            return false;
        }

        ArrayList<String[]> remainingPosts = new ArrayList<>();
        boolean found = false;
        try (RandomAccessFile input = new RandomAccessFile(postsFile, "r")) {
            while (input.getFilePointer() < input.length()) {
                String image = input.readUTF();
                String author = input.readUTF();
                String date = input.readUTF();
                String content = input.readUTF();
                if (image.equals(imagePath)) {
                    found = true;
                } else {
                    remainingPosts.add(new String[]{image, author, date, content});
                }
            }
        }
        if (!found) {
            return false;
        }
        rewritePosts(postsFile, remainingPosts);
        removeCommentsForImage(new File(userDirectory, "comments.ins"), imagePath);
        removeLikesForImage(getLikesFile(postOwner), imagePath);
        return true;
    }

    private File getLikesFile(String postOwner) throws IOException {
        File userDirectory = new File(usersDir, postOwner);
        Files.createDirectories(userDirectory.toPath());
        File likesFile = new File(userDirectory, "likes.ins");
        if (!likesFile.exists()) {
            likesFile.createNewFile();
        }
        return likesFile;
    }

    private void rewritePairs(File original, ArrayList<String[]> values) throws IOException {
        File temporary = new File(original.getParentFile(), original.getName() + ".tmp");
        try (RandomAccessFile output = new RandomAccessFile(temporary, "rw")) {
            output.setLength(0);
            for (String[] value : values) {
                output.writeUTF(value[0]);
                output.writeUTF(value[1]);
            }
        }
        Files.move(temporary.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void rewritePosts(File original, ArrayList<String[]> posts) throws IOException {
        File temporary = new File(original.getParentFile(), original.getName() + ".tmp");
        try (RandomAccessFile output = new RandomAccessFile(temporary, "rw")) {
            output.setLength(0);
            for (String[] post : posts) {
                output.writeUTF(post[0]);
                output.writeUTF(post[1]);
                output.writeUTF(post[2]);
                output.writeUTF(post[3]);
            }
        }
        Files.move(temporary.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void removeCommentsForImage(File commentsFile, String imagePath) throws IOException {
        if (!commentsFile.exists()) {
            return;
        }
        File temporary = new File(commentsFile.getParentFile(), commentsFile.getName() + ".tmp");
        try (RandomAccessFile input = new RandomAccessFile(commentsFile, "r");
                RandomAccessFile output = new RandomAccessFile(temporary, "rw")) {
            output.setLength(0);
            while (input.getFilePointer() < input.length()) {
                String storedPath = input.readUTF();
                String author = input.readUTF();
                String comment = input.readUTF();
                long timestamp = input.readLong();
                if (!storedPath.equals(imagePath)) {
                    output.writeUTF(storedPath);
                    output.writeUTF(author);
                    output.writeUTF(comment);
                    output.writeLong(timestamp);
                }
            }
        }
        Files.move(temporary.toPath(), commentsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void removeLikesForImage(File likesFile, String imagePath) throws IOException {
        ArrayList<String[]> remaining = new ArrayList<>();
        try (RandomAccessFile input = new RandomAccessFile(likesFile, "r")) {
            while (input.getFilePointer() < input.length()) {
                String storedPath = input.readUTF();
                String storedUser = input.readUTF();
                if (!storedPath.equals(imagePath)) {
                    remaining.add(new String[]{storedPath, storedUser});
                }
            }
        }
        rewritePairs(likesFile, remaining);
    }

    public synchronized String getPostsfromUser(String imagReferencia, String username) throws IOException {
        StringBuilder lista = new StringBuilder();
        ListaEnlazada<String[]> misPosts = getPosts(username);
        for (String[] post : misPosts) {
            String imagURL = post[0];
            if (imagURL.equals(imagReferencia)) {
                lista.append(post[1]).append(" escribio:\n '").append(post[3]).append("' el [").append(post[2]).append("]\n\n");
            }
        }
        return lista.toString();
    }

    public synchronized void addComment(String postOwner, String imagePath, String author, String comment) throws IOException {
        File userDir = new File(usersDir, postOwner);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        File file = new File(userDir, "comments.ins");
        if (!file.exists()) {
            file.createNewFile();
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(raf.length());
            raf.writeUTF(imagePath != null ? imagePath : "");
            raf.writeUTF(author != null ? author : "");
            raf.writeUTF(comment != null ? comment : "");
            raf.writeLong(Calendar.getInstance().getTimeInMillis());
        }
    }

    public synchronized ArrayList<String[]> getComments(String postOwner, String imagePath) throws IOException {
        ArrayList<String[]> comments = new ArrayList<>();
        File userDir = new File(usersDir, postOwner);
        File file = new File(userDir, "comments.ins");
        if (!file.exists()) {
            return comments;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                String path = raf.readUTF();
                String author = raf.readUTF();
                String text = raf.readUTF();
                long date = raf.readLong();
                try {
                    if (!getStatusUser(author)) {
                        continue;
                    }
                } catch (IOException ioe) {
                    continue;
                }
                if (path.equals(imagePath)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String dateS = sdf.format(new Date(date));
                    comments.add(new String[]{author, text, dateS});
                }
            }
        }
        return comments;
    }

    public synchronized ListaEnlazada<String> searchUsers(String query) throws IOException {
        ListaEnlazada<String> encontrados = new ListaEnlazada<>();
        if (query == null) {
            return encontrados;
        }
        String lowQ = query.toLowerCase();
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            users.readUTF();
            users.readChar();
            String uname = users.readUTF();
            users.readUTF();
            users.readLong();
            users.readInt();
            boolean status = users.readBoolean();
            users.readUTF();
            if (status && uname.toLowerCase().contains(lowQ)) {
                encontrados.add(uname);
            }
        }
        return encontrados;
    }

    public synchronized int getFollowersCount(String username) throws IOException {
        return getFollowers(username).size();
    }

    public synchronized int getFollowingCount(String username) throws IOException {
        return getFollowing(username).size();
    }

    public synchronized boolean isFollowing(String targetUsername) throws IOException {
        return loggedUserDir != null && getFollowing(loggedUser).contains(targetUsername);
    }

    public synchronized ListaEnlazada<String[]> getMentions(String user) throws IOException {
        ListaEnlazada<String[]> result = new ListaEnlazada<>();
        if (user == null || user.isBlank()) return result;
        Set<String> seen = new HashSet<>();
        for (String owner : searchUsers("")) {
            if (owner.equalsIgnoreCase(user)) continue;
            for (String[] post : getPosts(owner)) {
                if (InstaSocialText.containsToken(post[3], '@', user)
                        && seen.add(owner + "\n" + String.join("\n", post))) {
                    result.add(new String[]{post[0], post[1], post[2], post[3], owner});
                }
            }
        }
        sortPosts(result);
        return result;
    }

    public synchronized ListaEnlazada<String[]> getPostsByHashtag(String tag) throws IOException {
        ListaEnlazada<String[]> res = new ListaEnlazada<>();
        if (tag == null || tag.isBlank()) {
            return res;
        }
        String normalizedTag = tag.trim();
        if (normalizedTag.startsWith("#")) {
            normalizedTag = normalizedTag.substring(1);
        }
        normalizedTag = normalizedTag.toLowerCase(Locale.ROOT);
        if (normalizedTag.isBlank()) {
            return res;
        }

        File base = new File(usersDir);
        File[] usersFolders = base.listFiles(File::isDirectory);
        if (usersFolders == null) {
            return res;
        }

        Set<String> seen = new HashSet<>();
        for (File udir : usersFolders) {
            String owner = udir.getName();
            try {
                if (!getStatusUser(owner)) {
                    continue;
                }
            } catch (IOException ioe) {
                continue;
            }

            File insta = new File(udir, "insta.ins");
            if (!insta.exists()) {
                continue;
            }
            try (RandomAccessFile raf = new RandomAccessFile(insta, "r")) {
                raf.seek(0);
                while (raf.getFilePointer() < raf.length()) {
                    String imag = raf.readUTF();
                    String autor = raf.readUTF();
                    String fecha = raf.readUTF();
                    String contenido = raf.readUTF();
                    if (contenido != null) {
                        String low = contenido.toLowerCase(Locale.ROOT);
                        if (containsHashtagVariant(low, normalizedTag)) {
                            String key = imag + "|" + autor + "|" + fecha + "|" + owner + "|" + contenido;
                            if (!seen.contains(key)) {
                                seen.add(key);
                                res.add(new String[]{imag, autor, fecha, contenido});
                            }
                        }
                    }
                }
            } catch (IOException ex) {
            }
        }
        res.reverse();
        return res;
    }

    /**
     * Devuelve hashtags realmente presentes en publicaciones activas. Los más
     * usados aparecen primero y el prefijo puede recibirse con o sin '#'.
     */
    public synchronized ListaEnlazada<String> getHashtagSuggestions(String prefix, int limit) throws IOException {
        ListaEnlazada<String> suggestions = new ListaEnlazada<>();
        if (limit <= 0) {
            return suggestions;
        }

        String normalizedPrefix = prefix == null ? "" : prefix.trim();
        if (normalizedPrefix.startsWith("#")) {
            normalizedPrefix = normalizedPrefix.substring(1);
        }
        normalizedPrefix = normalizedPrefix.toLowerCase(Locale.ROOT);

        Map<String, Integer> counts = new HashMap<>();
        File base = new File(usersDir);
        File[] usersFolders = base.listFiles(File::isDirectory);
        if (usersFolders == null) {
            return suggestions;
        }

        for (File userDirectory : usersFolders) {
            try {
                if (!getStatusUser(userDirectory.getName())) {
                    continue;
                }
            } catch (IOException ex) {
                continue;
            }

            File postsFile = new File(userDirectory, "insta.ins");
            if (!postsFile.isFile()) {
                continue;
            }
            try (RandomAccessFile raf = new RandomAccessFile(postsFile, "r")) {
                while (raf.getFilePointer() < raf.length()) {
                    raf.readUTF();
                    raf.readUTF();
                    raf.readUTF();
                    String content = raf.readUTF();
                    for (String hashtag : extractHashtags(content)) {
                        if (normalizedPrefix.isEmpty() || hashtag.startsWith(normalizedPrefix)) {
                            counts.merge(hashtag, 1, Integer::sum);
                        }
                    }
                }
            } catch (IOException ex) {
                // Un registro dañado no debe impedir sugerir etiquetas de otros usuarios.
            }
        }

        ListaEnlazada<Map.Entry<String, Integer>> ranked = new ListaEnlazada<>(counts.entrySet());
        ranked.sort((left, right) -> {
            int byFrequency = Integer.compare(right.getValue(), left.getValue());
            return byFrequency != 0 ? byFrequency : left.getKey().compareToIgnoreCase(right.getKey());
        });
        for (Map.Entry<String, Integer> entry : ranked) {
            suggestions.add("#" + entry.getKey());
            if (suggestions.size() == limit) {
                break;
            }
        }
        return suggestions;
    }

    private Set<String> extractHashtags(String text) {
        Set<String> hashtags = new HashSet<>();
        if (text == null || text.isBlank()) {
            return hashtags;
        }
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) != '#') {
                continue;
            }
            int end = index + 1;
            while (end < text.length()) {
                char current = text.charAt(end);
                if (!Character.isLetterOrDigit(current) && current != '_') {
                    break;
                }
                end++;
            }
            if (end > index + 1) {
                hashtags.add(text.substring(index + 1, end).toLowerCase(Locale.ROOT));
                index = end - 1;
            }
        }
        return hashtags;
    }

    public synchronized ListaEnlazada<String[]> findPostsMentioning(String username) throws IOException {
        return getMentions(username);
    }

    public synchronized ListaEnlazada<String[]> searchHashtag(String tag) throws IOException {
        return getPostsByHashtag(tag);
    }

    private boolean containsHashtagVariant(String text, String keyTag) {
        return InstaSocialText.containsToken(text, '#', keyTag);
    }

}
