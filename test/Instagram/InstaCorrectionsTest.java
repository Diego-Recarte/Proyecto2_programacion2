package Instagram;

import Instagram.sockets.InstaServer;
import Logica.Decodificacion.Publicacion;
import Logica.Decodificacion.InstaRepository;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Regresiones reales de login por ventana, privacidad, fechas y límite de texto. */
public final class InstaCorrectionsTest {
    public static void main(String[] args) throws Exception {
        Path root = Path.of("Instagram");
        require(!Files.exists(root), "Ejecutar únicamente en una carpeta vacía");
        try (InstaRepository data = new InstaRepository(root, false)) {
            data.addNewUser("Ana privada", 'F', "ana", "Clave123", 22, null);
            data.addNewUser("Luis privado", 'M', "luis", "Clave123", 24, null);
        }
        // Datos previos: dos registros del mismo autor/minuto y un registro con milisegundos.
        String first = "text:v1:00000000-0000-0000-0000-000000000001";
        fixture(root, "ana", first, "01/01/2026 10:00", "Primera mención @luis");
        fixture(root, "ana", "text:v1:00000000-0000-0000-0000-000000000002", "01/01/2026 10:00", "Segunda mención @luis");
        fixture(root, "luis", "text:v1:00000000-0000-0000-0000-000000000003", "01/01/2026 10:00:00.900", "Más reciente dentro del minuto");
        JFrame[] windows = new JFrame[2];
        InstaLoginUI[] logins = new InstaLoginUI[2];
        try (InstaServer server = new InstaServer(0, 0, root, false)) {
            instaManager configured = new instaManager("127.0.0.1", server.port());
            instaController.getInstance().setInsta(configured);
            try {
                SwingUtilities.invokeAndWait(() -> {
                    for (int i = 0; i < windows.length; i++) {
                        windows[i] = new JFrame("Prueba aislada de INSTA");
                        logins[i] = new InstaLoginUI();
                        windows[i].setContentPane(logins[i]); windows[i].pack();
                    }
                });
                login(logins[0], "ana");
                await(() -> windows[0].getContentPane() instanceof InstaFeedUI, "Login de Ana");
                login(logins[1], "luis");
                await(() -> windows[1].getContentPane() instanceof InstaFeedUI, "Login de Luis");
                instaManager ana = instaController.getInstance().getInsta("ana");
                instaManager luis = instaController.getInstance().getInsta("luis");
                require(ana != luis && ana != configured && luis != configured, "Cada ventana debe tener su cliente");
                require(!ana.sessionToken().equals(luis.sessionToken()), "Tokens independientes");
                require(configured.sessionToken().isEmpty(), "La configuración global no recibe credenciales");
                expectFailure(() -> ana.authenticate("luis", "Clave123"));
                ana.setLoggedUser("ana"); luis.setLoggedUser("luis");
                ana.addFollow("luis");
                require(ana.getFeedPosts("ana").get(0)[3].equals("Más reciente dentro del minuto"), "Orden entre formato antiguo y preciso");
                require(Publicacion.timestamp("01/01/2026 10:00:00.901") > Publicacion.timestamp("01/01/2026 10:00:00.900"), "Se comparan milisegundos");

                Path photo = Path.of("imagen.png");
                ImageIO.write(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB), "png", photo.toFile());
                String remote = ana.uploadImage("ana", photo.toFile(), "");
                String caption = "#tema @luis " + "x".repeat(208);
                require(Publicacion.textLength(caption) == 220, "Fixture de 220 caracteres");
                ana.addPost("", "ana", caption);
                ana.addPost(remote, "ana", caption);
                String sticker = ana.getStickers("ana").get(0)[1];
                ana.addPost(sticker, "ana", caption);
                ana.addPost("", "ana", "😀".repeat(220));
                for (String media : List.of("", remote, sticker)) {
                    expectFailure(() -> ana.addPost(media, "ana", caption + "x"));
                }
                expectFailure(() -> ana.addPost("", "ana", "😀".repeat(221)));
                List<String[]> posts = ana.getPosts("ana");
                require(posts.get(0)[2].matches(".*:\\d{2}\\.\\d{3}"), "Fecha nueva conserva milisegundos");
                require(Publicacion.timestamp(posts.get(0)[2]) > Publicacion.timestamp(posts.get(1)[2]), "Orden de publicaciones consecutivas");

                SwingUtilities.invokeAndWait(() -> {
                    try {
                        InstaPostComposer composer = new InstaPostComposer("ana", () -> {}, () -> {});
                        JTextArea text = (JTextArea) field(composer, "description");
                        JButton publish = (JButton) field(composer, "publish");
                        text.setText(caption); require(publish.isEnabled(), "Editor acepta 220 con hashtags y menciones");
                        text.setText(caption + "x"); require(!publish.isEnabled(), "Editor rechaza 221");
                        text.setText("😀".repeat(220)); require(publish.isEnabled(), "Contador Unicode del editor");
                        InstaMediaCarousel carousel = new InstaMediaCarousel(List.of(remote, remote), 80, 80, null, ana);
                        require(carousel.getClientProperty("insta.manager") == ana, "Carrusel conserva el cliente de su ventana");
                        InteractionsUI mentions = new InteractionsUI("luis");
                        windows[1].setContentPane(mentions);
                        String[] requested = ana.getPosts("ana").stream().filter(row -> row[0].equals(first)).findFirst().orElseThrow();
                        Method open = InteractionsUI.class.getDeclaredMethod("abrirPost", String[].class); open.setAccessible(true);
                        open.invoke(mentions, (Object) requested);
                        InstaPostUI detail = (InstaPostUI) windows[1].getContentPane();
                        List<?> shown = (List<?>) field(detail, "feedPosts");
                        require(Publicacion.sameRow(requested, (String[]) shown.get((Integer) field(detail, "startIndex"))), "La mención abre exactamente la publicación elegida");
                    } catch (Exception ex) { throw new AssertionError(ex); }
                });

                luis.desactivateUser("luis");
                require(ana.searchUsers("luis").isEmpty() && ana.getPosts("luis").isEmpty(), "Inactivo fuera de búsquedas y publicaciones");
                require(ana.checkUserExistance("luis"), "El username inactivo permanece reservado para el registro");
                expectFailure(() -> ana.getRealName("luis"));
                expectFailure(() -> ana.getAge("luis"));
                expectFailure(() -> ana.getGender("luis"));
                expectFailure(() -> ana.getEntryDate("luis"));
                expectFailure(() -> ana.getProfilePic("luis"));
                expectFailure(() -> ana.getFollowers("luis"));
                require(luis.getRealName("luis").equals("Luis privado"), "Cuenta inactiva conserva acceso a su propia recuperación");
                SwingUtilities.invokeAndWait(() -> {
                    VisibilidadProfileUI profile = new VisibilidadProfileUI("luis", "ana");
                    require(((JLabel) field(profile, "lblName")).getText().equals("Cuenta no disponible"), "Acceso directo no muestra datos privados");
                    require(((JLabel) field(profile, "lblInfo")).getText().isEmpty(), "Sin edad, género o fecha de una cuenta inactiva");
                });
                luis.activateUser("luis");
                require(ana.getRealName("luis").equals("Luis privado"), "Reactivación restaura consultas");
                ana.loggoutUser();
                require(instaController.getInstance().getInsta("ana") == null, "Logout elimina solo la sesión correspondiente");
                require(luis.getRealName("luis").equals("Luis privado"), "Salir de Ana no cierra Luis");
                luis.addPost("", "luis", "La segunda ventana conserva su sesión");
                expectFailure(() -> ana.searchUsers(""));
            } finally {
                SwingUtilities.invokeAndWait(() -> { for (JFrame window : windows) if (window != null) window.dispose(); });
            }
        }
        System.out.println("OK: dos logins reales, sesiones independientes, privacidad de inactivos, fechas compatibles, selección exacta y 220 caracteres.");
    }

    private static void fixture(Path root, String author, String id, String date, String text) throws Exception {
        try (RandomAccessFile file = new RandomAccessFile(root.resolve("users").resolve(author).resolve("insta.ins").toFile(), "rw")) {
            file.seek(file.length()); new Publicacion(id, author, date, text).write(file);
        }
    }
    private static void login(InstaLoginUI view, String user) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) field(view, "txtUser")).setText(user);
            ((JPasswordField) field(view, "txtPass")).setText("Clave123");
            ((JButton) field(view, "btnLogin")).doClick();
        });
    }
    private static Object field(Object object, String name) {
        try { Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(object); }
        catch (Exception ex) { throw new AssertionError(ex); }
    }
    private static void await(BooleanSupplier condition, String message) throws Exception {
        long deadline = System.nanoTime() + 10_000_000_000L;
        AtomicBoolean result = new AtomicBoolean();
        while (System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait(() -> result.set(condition.getAsBoolean()));
            if (result.get()) return;
            Thread.sleep(25);
        }
        throw new AssertionError(message);
    }
    private interface Operation { void run() throws Exception; }
    private static void expectFailure(Operation operation) throws Exception {
        try { operation.run(); throw new AssertionError("Se aceptó una operación inválida"); }
        catch (IOException expected) { }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
