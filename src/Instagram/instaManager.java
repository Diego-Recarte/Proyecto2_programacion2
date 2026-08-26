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
import java.util.HashSet;
import java.util.Set;


public class instaManager {

    private RandomAccessFile users;
    private final String mainRoot = "Instagram";
    private final String usersDir = mainRoot + File.separator + "users";
    private File loggedUserDir = null;
    private String loggedUser = null;

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
}
