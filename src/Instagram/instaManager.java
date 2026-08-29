package Instagram;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


public class instaManager {

    private RandomAccessFile users;
    private final String mainRoot = "Instagram";
    private final String usersDir = mainRoot + File.separator + "users";
    private File loggedUserDir = null;
    private String loggedUser = null;
    private StickerRepository stickerRepository;

    public instaManager() {
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
        } catch (IOException e) {
            System.err.println("Error inicializando users.ins: " + e.getMessage());
        }
    }

    public void addNewUser(String name, char genero, String username, String password, int edad, String profilePicPath) throws IOException {
        if (checkUserExistance(username)) {
            throw new IOException("Usuario ya existe: " + username);
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
            finalPath = destino.getAbsolutePath();
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

    public ArrayList<String[]> getStickers(String username) throws IOException {
        if (stickerRepository == null) {
            throw new IOException("El repositorio de stickers no está disponible.");
        }
        return stickerRepository.list(username);
    }

    public String importSticker(String username, File source) throws IOException {
        if (stickerRepository == null) {
            throw new IOException("El repositorio de stickers no está disponible.");
        }
        return stickerRepository.importSticker(username, source);
    }

    /** Actualiza los datos editables sin alterar contraseña, fecha ni estado. */
    public synchronized boolean updateProfile(String username, String realName, char gender,
            int age, String newProfilePicPath) throws IOException {
        if (realName == null || realName.isBlank()) {
            throw new IOException("El nombre no puede estar vacío.");
        }
        if (age < 13 || age > 120) {
            throw new IOException("La edad debe estar entre 13 y 120 años.");
        }

        ArrayList<UserRecord> records = readUserRecords();
        UserRecord selected = null;
        for (UserRecord record : records) {
            if (record.username.equals(username)) {
                selected = record;
                break;
            }
        }
        if (selected == null) {
            return false;
        }

        selected.realName = realName.trim();
        selected.gender = gender;
        selected.age = age;

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
            selected.profilePicture = destination.getAbsolutePath();
        }

        rewriteUsers(records);
        return true;
    }

    private ArrayList<UserRecord> readUserRecords() throws IOException {
        ArrayList<UserRecord> records = new ArrayList<>();
        users.seek(0);
        while (users.getFilePointer() < users.length()) {
            records.add(new UserRecord(
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

    private void rewriteUsers(ArrayList<UserRecord> records) throws IOException {
        File original = new File(mainRoot, "users.ins");
        File temporary = new File(mainRoot, "users.ins.tmp");
        try (RandomAccessFile output = new RandomAccessFile(temporary, "rw")) {
            output.setLength(0);
            for (UserRecord record : records) {
                output.writeUTF(record.realName);
                output.writeChar(record.gender);
                output.writeUTF(record.username);
                output.writeUTF(record.password);
                output.writeLong(record.entryDate);
                output.writeInt(record.age);
                output.writeBoolean(record.active);
                output.writeUTF(record.profilePicture);
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

    public void setLoggedUser(String username) throws IOException {
        File dir = new File(usersDir, username);
        if (!dir.exists()) {
            throw new IOException("Usuario no existe: " + username);
        }
        this.loggedUserDir = dir;
        this.loggedUser = username;
    }

    public void loggoutUser() {
        this.loggedUserDir = null;
        this.loggedUser = null;
    }

    public String getRealName(String username) throws IOException {
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

    public char getGender(String username) throws IOException {
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

    public int getAge(String username) throws IOException {
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

    public String getEntryDate(String username) throws IOException {
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

    public boolean getStatusUser(String username) throws IOException {
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

    public String getProfilePic(String username) throws IOException {
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
                return pic;
            }
        }
        return null;
    }

    public boolean checkUserExistance(String username) throws IOException {
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

    public boolean activateUser(String username) throws IOException {
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

    public String getPassword(String username) throws IOException {
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

    public boolean desactivateUser(String username) throws IOException {
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

    private void addFollowerInternal(String usernameTarget, String newFollower) throws IOException {
        File user2Dir = new File(usersDir, usernameTarget);
        if (!user2Dir.exists()) {
            user2Dir.mkdirs();
        }
        File followersFile = new File(user2Dir, "followers.ins");
        try (RandomAccessFile raf = new RandomAccessFile(followersFile, "rw")) {
            raf.seek(raf.length());
            raf.writeUTF(newFollower);
        }
    }

    public boolean addFollow(String usernameToFollow) throws IOException {
        if (loggedUserDir == null) {
            return false;
        }
        File followsFile = new File(loggedUserDir, "following.ins");
        if (!followsFile.exists()) {
            followsFile.createNewFile();
        }

        try (RandomAccessFile raf = new RandomAccessFile(followsFile, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                String read = raf.readUTF();
                if (read.equals(usernameToFollow)) {
                    return false;
                }
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(followsFile, "rw")) {
            raf.seek(raf.length());
            raf.writeUTF(usernameToFollow);
        }

        addFollowerInternal(usernameToFollow, loggedUser);
        return true;
    }

    public void quitarFollow(String usernameToUnfollow) throws IOException {
        if (loggedUserDir == null) {
            return;
        }
        File fileOriginal = new File(loggedUserDir, "following.ins");
        File fileTemp = new File(loggedUserDir, "following.temp");

        if (!fileOriginal.exists()) {
            return;
        }

        boolean encontrado = false;
        try (RandomAccessFile oFile = new RandomAccessFile(fileOriginal, "r"); RandomAccessFile tFile = new RandomAccessFile(fileTemp, "rw")) {
            oFile.seek(0);
            tFile.setLength(0);
            while (oFile.getFilePointer() < oFile.length()) {
                String read = oFile.readUTF();
                if (!read.equals(usernameToUnfollow)) {
                    tFile.writeUTF(read);
                } else {
                    encontrado = true;
                }
            }
        }

        if (encontrado) {
            if (!fileOriginal.delete()) {
                throw new IOException("No se pudo borrar following.ins");
            }
            if (!fileTemp.renameTo(fileOriginal)) {
                throw new IOException("No se pudo renombrar temporal following");
            }
        } else {
            if (fileTemp.exists()) {
                fileTemp.delete();
            }
        }

        quitarFollowerInternal(usernameToUnfollow, loggedUser);
    }

    private void quitarFollowerInternal(String username, String follower) throws IOException {
        File user2Dir = new File(usersDir, username);
        File fileOriginal = new File(user2Dir, "followers.ins");
        File fileTemp = new File(user2Dir, "followers.temp");

        if (!fileOriginal.exists()) {
            return;
        }

        boolean encontrado = false;
        try (RandomAccessFile raf = new RandomAccessFile(fileOriginal, "r"); RandomAccessFile tFile = new RandomAccessFile(fileTemp, "rw")) {
            raf.seek(0);
            tFile.setLength(0);
            while (raf.getFilePointer() < raf.length()) {
                String read = raf.readUTF();
                if (!read.equals(follower)) {
                    tFile.writeUTF(read);
                } else {
                    encontrado = true;
                }
            }
        }

        if (encontrado) {
            if (!fileOriginal.delete()) {
                throw new IOException("No se pudo borrar followers.ins");
            }
            if (!fileTemp.renameTo(fileOriginal)) {
                throw new IOException("No se pudo renombrar temporal followers");
            }
        } else {
            if (fileTemp.exists()) {
                fileTemp.delete();
            }
        }
    }

    public String showFollowers(String username) throws IOException {
        File userPath = new File(usersDir, username);
        File f = new File(userPath, "followers.ins");
        if (!f.exists()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                sb.append(raf.readUTF()).append("\n");
            }
        }
        return sb.toString();
    }

    public String showFollows(String username) throws IOException {
        File userPath = new File(usersDir, username);
        File f = new File(userPath, "following.ins");
        if (!f.exists()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                sb.append(raf.readUTF()).append("\n");
            }
        }
        return sb.toString();
    }

    public void addPost(String imagRef, String autor, String contenido) throws IOException {
        if (loggedUserDir == null) {
            throw new IOException("No hay usuario loggeado.");
        }
        if (contenido != null && contenido.length() > 220) {
            throw new IOException("La descripción no puede superar 220 caracteres.");
        }
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateFormat = formato.format(Calendar.getInstance().getTime());
        File postFile = new File(loggedUserDir, "insta.ins");
        if (!postFile.exists()) {
            postFile.createNewFile();
        }
        try (RandomAccessFile raf = new RandomAccessFile(postFile, "rw")) {
            raf.seek(raf.length());
            raf.writeUTF(imagRef != null ? imagRef : "");
            raf.writeUTF(autor != null ? autor : "");
            raf.writeUTF(dateFormat);
            raf.writeUTF(contenido != null ? contenido : "");
        }
    }

    public ArrayList<String[]> getPosts(String username) throws IOException {
        ArrayList<String[]> posts = new ArrayList<>();
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
                String[] post = new String[4];
                post[0] = raf.readUTF();
                post[1] = raf.readUTF();
                post[2] = raf.readUTF();
                post[3] = raf.readUTF();
                posts.add(post);
            }
        } catch (IOException ex) {
        }
        Collections.reverse(posts);
        return posts;
    }

    /**
     * Construye el timeline con publicaciones propias y de cuentas seguidas.
     */
    public ArrayList<String[]> getFeedPosts(String viewer) throws IOException {
        ArrayList<String[]> feed = new ArrayList<>();
        Set<String> visibleUsers = new HashSet<>();
        visibleUsers.add(viewer);
        File followingFile = new File(new File(usersDir, viewer), "following.ins");
        if (followingFile.exists()) {
            try (RandomAccessFile following = new RandomAccessFile(followingFile, "r")) {
                while (following.getFilePointer() < following.length()) {
                    visibleUsers.add(following.readUTF());
                }
            }
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

    public String getPostsfromUser(String imagReferencia, String username) throws IOException {
        StringBuilder lista = new StringBuilder();
        ArrayList<String[]> misPosts = getPosts(username);
        for (String[] post : misPosts) {
            String imagURL = post[0];
            if (imagURL.equals(imagReferencia)) {
                lista.append(post[1]).append(" escribio:\n '").append(post[3]).append("' el [").append(post[2]).append("]\n\n");
            }
        }
        return lista.toString();
    }

    public void addComment(String postOwner, String imagePath, String author, String comment) throws IOException {
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

    public ArrayList<String[]> getComments(String postOwner, String imagePath) throws IOException {
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

    public ArrayList<String> searchUsers(String query) throws IOException {
        ArrayList<String> encontrados = new ArrayList<>();
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

    public int getFollowersCount(String username) throws IOException {
        File userPath = new File(usersDir, username);
        File f = new File(userPath, "followers.ins");
        if (!f.exists()) {
            return 0;
        }
        int count = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                raf.readUTF();
                count++;
            }
        }
        return count;
    }

    public int getFollowingCount(String username) throws IOException {
        File userPath = new File(usersDir, username);
        File f = new File(userPath, "following.ins");
        if (!f.exists()) {
            return 0;
        }
        int count = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                raf.readUTF();
                count++;
            }
        }
        return count;
    }

    public boolean isFollowing(String targetUsername) throws IOException {
        if (loggedUserDir == null) {
            return false;
        }
        File file = new File(loggedUserDir, "following.ins");
        if (!file.exists()) {
            return false;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                String u = raf.readUTF();
                if (u.equals(targetUsername)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<String[]> getMentions(String user) throws IOException {
        ArrayList<String[]> res = new ArrayList<>();
        if (user == null || user.isEmpty()) {
            return res;
        }
        String needle = "@" + user.toLowerCase();

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
                    if (contenido != null && contenido.toLowerCase().contains(needle)) {
                        String key = imag + "|" + autor + "|" + fecha;
                        if (!seen.contains(key)) {
                            seen.add(key);
                            res.add(new String[]{imag, autor, fecha, contenido});
                        }
                    }
                }
            } catch (IOException ex) {
            }
        }
        Collections.reverse(res);
        return res;
    }

    public ArrayList<String[]> getPostsByHashtag(String tag) throws IOException {
        ArrayList<String[]> res = new ArrayList<>();
        if (tag == null || tag.isEmpty()) {
            return res;
        }
        String needle = "#" + tag.toLowerCase();

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
                        String low = contenido.toLowerCase();
                        if (low.contains(needle) || low.contains("#" + tag.toLowerCase())) {
                            String key = imag + "|" + autor + "|" + fecha + "|" + owner;
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
        Collections.reverse(res);
        return res;
    }

    public ArrayList<String[]> findPostsMentioning(String username) throws IOException {
        ArrayList<String[]> encontrados = new ArrayList<>();
        if (username == null || username.isBlank()) {
            return encontrados;
        }

        String needle = "@" + username.toLowerCase();

        HashSet<String> seen = new HashSet<>();

        File usersRoot = new File(usersDir);
        File[] userDirs = usersRoot.listFiles(File::isDirectory);
        if (userDirs == null) {
            return encontrados;
        }

        for (File udir : userDirs) {
            String owner = udir.getName();

            try {
                if (!getStatusUser(owner)) {
                    continue;
                }
            } catch (IOException ioe) {
                continue;
            }

            File postsFile = new File(udir, "insta.ins");
            if (postsFile.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(postsFile, "r")) {
                    raf.seek(0);
                    while (raf.getFilePointer() < raf.length()) {
                        String imagRef = raf.readUTF();
                        String autor = raf.readUTF();
                        String fecha = raf.readUTF();
                        String contenido = raf.readUTF();

                        String lowerContenido = (contenido != null) ? contenido.toLowerCase() : "";

                        if (lowerContenido.contains(needle)) {
                            String key = imagRef + "|" + autor + "|" + fecha + "|" + owner;
                            if (!seen.contains(key)) {
                                seen.add(key);
                                String[] post = new String[5];
                                post[0] = imagRef;
                                post[1] = autor;
                                post[2] = fecha;
                                post[3] = contenido;
                                post[4] = owner;
                                encontrados.add(post);
                            }
                        }
                    }
                } catch (IOException ex) {
                }
            }

            File commentsFile = new File(udir, "comments.ins");
            if (commentsFile.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(commentsFile, "r")) {
                    raf.seek(0);
                    while (raf.getFilePointer() < raf.length()) {
                        String imagePath = raf.readUTF();
                        String commentAuthor = raf.readUTF();
                        String commentText = raf.readUTF();
                        long ts = raf.readLong();

                        try {
                            if (!getStatusUser(commentAuthor)) {
                                continue;
                            }
                        } catch (IOException ioe) {
                            continue;
                        }

                        String lowerComment = commentText != null ? commentText.toLowerCase() : "";
                        if (lowerComment.contains(needle)) {
                            File ownerPosts = new File(udir, "insta.ins");
                            if (ownerPosts.exists()) {
                                try (RandomAccessFile raf2 = new RandomAccessFile(ownerPosts, "r")) {
                                    raf2.seek(0);
                                    while (raf2.getFilePointer() < raf2.length()) {
                                        String imagRef = raf2.readUTF();
                                        String autor = raf2.readUTF();
                                        String fecha = raf2.readUTF();
                                        String contenido = raf2.readUTF();

                                        if (imagRef != null && imagRef.equals(imagePath)) {
                                            String key = imagRef + "|" + autor + "|" + fecha + "|" + owner;
                                            if (!seen.contains(key)) {
                                                seen.add(key);
                                                String[] post = new String[5];
                                                post[0] = imagRef;
                                                post[1] = autor;
                                                post[2] = fecha;
                                                post[3] = contenido;
                                                post[4] = owner;
                                                encontrados.add(post);
                                            }
                                            break;
                                        }
                                    }
                                } catch (IOException ex2) {
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                }
            }
        }

        Collections.reverse(encontrados);
        return encontrados;
    }

    public ArrayList<String[]> searchHashtag(String tag) throws IOException {
        ArrayList<String[]> encontrados = new ArrayList<>();
        if (tag == null || tag.isBlank()) {
            return encontrados;
        }

        String keyTag = tag.startsWith("#") ? tag.substring(1).toLowerCase() : tag.toLowerCase();
        String needleHash = "#" + keyTag;

        HashSet<String> seen = new HashSet<>();

        File usersRoot = new File(usersDir);
        File[] userDirs = usersRoot.listFiles(File::isDirectory);
        if (userDirs == null) {
            return encontrados;
        }

        for (File udir : userDirs) {
            String owner = udir.getName();

            try {
                if (!getStatusUser(owner)) {
                    continue;
                }
            } catch (IOException ioe) {
                continue;
            }

            File postsFile = new File(udir, "insta.ins");
            if (postsFile.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(postsFile, "r")) {
                    raf.seek(0);
                    while (raf.getFilePointer() < raf.length()) {
                        String imagRef = raf.readUTF();
                        String autor = raf.readUTF();
                        String fecha = raf.readUTF();
                        String contenido = raf.readUTF();

                        String lowerContenido = (contenido != null) ? contenido.toLowerCase() : "";

                        if (lowerContenido.contains(needleHash) || containsHashtagVariant(lowerContenido, keyTag)) {
                            String key = imagRef + "|" + autor + "|" + fecha + "|" + owner;
                            if (!seen.contains(key)) {
                                seen.add(key);
                                String[] post = new String[5];
                                post[0] = imagRef;
                                post[1] = autor;
                                post[2] = fecha;
                                post[3] = contenido;
                                post[4] = owner;
                                encontrados.add(post);
                            }
                        }
                    }
                } catch (IOException ex) {
                }
            }

            File commentsFile = new File(udir, "comments.ins");
            if (commentsFile.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(commentsFile, "r")) {
                    raf.seek(0);
                    while (raf.getFilePointer() < raf.length()) {
                        String imagePath = raf.readUTF();
                        String commentAuthor = raf.readUTF();
                        String commentText = raf.readUTF();
                        long ts = raf.readLong();

                        try {
                            if (!getStatusUser(commentAuthor)) {
                                continue;
                            }
                        } catch (IOException ioe) {
                            continue;
                        }

                        String lowerComment = commentText != null ? commentText.toLowerCase() : "";
                        if (lowerComment.contains(needleHash) || containsHashtagVariant(lowerComment, keyTag)) {
                            File ownerPosts = new File(udir, "insta.ins");
                            if (ownerPosts.exists()) {
                                try (RandomAccessFile raf2 = new RandomAccessFile(ownerPosts, "r")) {
                                    raf2.seek(0);
                                    while (raf2.getFilePointer() < raf2.length()) {
                                        String imagRef = raf2.readUTF();
                                        String autor = raf2.readUTF();
                                        String fecha = raf2.readUTF();
                                        String contenido = raf2.readUTF();

                                        if (imagRef != null && imagRef.equals(imagePath)) {
                                            String key = imagRef + "|" + autor + "|" + fecha + "|" + owner;
                                            if (!seen.contains(key)) {
                                                seen.add(key);
                                                String[] post = new String[5];
                                                post[0] = imagRef;
                                                post[1] = autor;
                                                post[2] = fecha;
                                                post[3] = contenido;
                                                post[4] = owner;
                                                encontrados.add(post);
                                            }
                                            break;
                                        }
                                    }
                                } catch (IOException ex2) {
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                }
            }
        }

        Collections.reverse(encontrados);
        return encontrados;
    }

    private boolean containsHashtagVariant(String text, String keyTag) {
        int idx = 0;
        while (true) {
            idx = text.indexOf("#" + keyTag, idx);
            if (idx == -1) {
                return false;
            }
            int after = idx + 1 + keyTag.length();
            if (after == text.length()) {
                return true;
            }
            char c = text.charAt(after);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return true;
            }
            idx = after;
        }
    }

    private static final class UserRecord {
        private String realName;
        private char gender;
        private final String username;
        private final String password;
        private final long entryDate;
        private int age;
        private final boolean active;
        private String profilePicture;

        private UserRecord(String realName, char gender, String username, String password,
                long entryDate, int age, boolean active, String profilePicture) {
            this.realName = realName;
            this.gender = gender;
            this.username = username;
            this.password = password;
            this.entryDate = entryDate;
            this.age = age;
            this.active = active;
            this.profilePicture = profilePicture;
        }
    }
}
