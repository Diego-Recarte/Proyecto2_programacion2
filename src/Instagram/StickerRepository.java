package Instagram;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/** Repositorio binario de stickers globales y personales. */
final class StickerRepository {

    private static final String[][] DEFAULT_STICKERS = {
        {"Feliz", "☺"},
        {"Triste", "☹"},
        {"Corazón", "♥"},
        {"Risa", "😂"},
        {"Aplauso", "👏"}
    };

    private final File usersDirectory;
    private final File globalDirectory;

    StickerRepository(File root, File usersDirectory) throws IOException {
        this.usersDirectory = usersDirectory;
        this.globalDirectory = new File(root, "stickers_globales");
        Files.createDirectories(globalDirectory.toPath());
        createGlobalStickers();
    }

    void initializeUser(File userDirectory) throws IOException {
        Files.createDirectories(new File(userDirectory, "imagenes").toPath());
        Files.createDirectories(new File(userDirectory, "stickers_personales").toPath());

        File stickersFile = new File(userDirectory, "stickers.ins");
        if (!stickersFile.exists()) {
            stickersFile.createNewFile();
        }
        if (stickersFile.length() == 0) {
            try (RandomAccessFile output = new RandomAccessFile(stickersFile, "rw")) {
                for (String[] sticker : DEFAULT_STICKERS) {
                    output.writeUTF(sticker[0]);
                    output.writeUTF(new File(globalDirectory, safeName(sticker[0]) + ".png").getAbsolutePath());
                    output.writeBoolean(true);
                }
            }
        }
    }

    ArrayList<String[]> list(String username) throws IOException {
        File userDirectory = new File(usersDirectory, username);
        initializeUser(userDirectory);
        ArrayList<String[]> stickers = new ArrayList<>();
        try (RandomAccessFile input = new RandomAccessFile(new File(userDirectory, "stickers.ins"), "r")) {
            while (input.getFilePointer() < input.length()) {
                stickers.add(new String[]{input.readUTF(), input.readUTF(), String.valueOf(input.readBoolean())});
            }
        }
        return stickers;
    }

    String importSticker(String username, File source) throws IOException {
        if (source == null || !source.isFile()) {
            throw new IOException("Selecciona una imagen válida.");
        }
        String lower = source.getName().toLowerCase();
        if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) {
            throw new IOException("El sticker debe tener formato PNG o JPG.");
        }

        File userDirectory = new File(usersDirectory, username);
        initializeUser(userDirectory);
        File personalDirectory = new File(userDirectory, "stickers_personales");
        String cleanName = source.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        File destination = new File(personalDirectory, System.currentTimeMillis() + "_" + cleanName);
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

        try (RandomAccessFile output = new RandomAccessFile(new File(userDirectory, "stickers.ins"), "rw")) {
            output.seek(output.length());
            output.writeUTF(stripExtension(source.getName()));
            output.writeUTF(destination.getAbsolutePath());
            output.writeBoolean(false);
        }
        return destination.getAbsolutePath();
    }

    private void createGlobalStickers() throws IOException {
        Color[] colors = {
            new Color(255, 193, 7), new Color(80, 150, 245), new Color(238, 60, 85),
            new Color(255, 152, 0), new Color(120, 200, 120)
        };
        for (int index = 0; index < DEFAULT_STICKERS.length; index++) {
            String[] sticker = DEFAULT_STICKERS[index];
            File file = new File(globalDirectory, safeName(sticker[0]) + ".png");
            if (file.exists()) {
                continue;
            }
            BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(colors[index]);
            graphics.fillOval(4, 4, 120, 120);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 58));
            FontMetrics metrics = graphics.getFontMetrics();
            String symbol = sticker[1];
            int x = (128 - metrics.stringWidth(symbol)) / 2;
            int y = (128 - metrics.getHeight()) / 2 + metrics.getAscent();
            graphics.drawString(symbol, x, y);
            graphics.dispose();
            ImageIO.write(image, "png", file);
        }
    }

    private static String safeName(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    private static String stripExtension(String value) {
        int dot = value.lastIndexOf('.');
        return dot > 0 ? value.substring(0, dot) : value;
    }
}
