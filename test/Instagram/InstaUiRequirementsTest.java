package Instagram;

import Instagram.sockets.InstaServer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Verifica los paneles reales sin ventanas y genera capturas para revisar su diseño. */
public final class InstaUiRequirementsTest {
    public static void main(String[] args) throws Exception {
        Path root = Path.of("Instagram");
        if (Files.exists(root)) throw new IllegalStateException("Usa un directorio vacio.");
        try (InstaServer server = new InstaServer(0, 0, root, false)) {
            AtomicInteger imageRequests = new AtomicInteger();
            instaManager manager = new instaManager("127.0.0.1", server.port()) {
                @Override public byte[] readMedia(String reference) throws java.io.IOException {
                    if (SwingUtilities.isEventDispatchThread()) throw new AssertionError("Descarga de imagen en Swing");
                    imageRequests.incrementAndGet(); return super.readMedia(reference);
                }
            };
            manager.addNewUser("Ana de prueba", 'F', "ana", "Clave123", 23, null);
            manager.authenticate("ana", "Clave123");
            instaController.getInstance().setInsta(manager);
            manager.createPersonalFolder("ana", "Viajes");
            BufferedImage picture = new BufferedImage(420, 280, BufferedImage.TYPE_INT_RGB);
            Graphics2D paint = picture.createGraphics(); paint.setColor(new Color(30, 100, 135)); paint.fillRect(0, 0, 420, 280);
            paint.setColor(new Color(240, 175, 45)); paint.fillOval(235, 35, 80, 80); paint.dispose();
            Path photo = Path.of("prueba.png"); ImageIO.write(picture, "png", photo.toFile());
            String remote = manager.uploadImage("ana", photo.toFile(), "Viajes");
            manager.addPost(remote, "ana", "Una imagen de prueba #viajes");
            manager.addPost("", "ana", "Publicación solo de texto #noticias");
            instaManager luis = new instaManager("127.0.0.1", server.port());
            luis.addNewUser("Luis", 'M', "luis", "Clave123", 23, null); luis.authenticate("luis", "Clave123");
            luis.addComment("ana", remote, "luis", "Comentario visible antes de desactivar");
            JPanel[] panels = new JPanel[5];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    InstaFeedUI feed = new InstaFeedUI("ana");
                    Method render = InstaFeedUI.class.getDeclaredMethod("renderFeed", Logica.Estructuras.ListaEnlazada.class);
                    render.setAccessible(true); render.invoke(feed, manager.getFeedPosts("ana"));
                    panels[0] = feed;
                    panels[1] = new InstaPostComposer("ana", () -> {}, () -> {});
                    panels[2] = new InstaFoldersUI("ana", () -> {});
                    panels[3] = new InstaProfileUI("ana");
                    panels[4] = new InstaProfileEditUI("ana");
                    JTextArea description = (JTextArea) field(panels[1], "description");
                    description.setText("x".repeat(221));
                    require(!((JButton) field(panels[1], "publish")).isEnabled(), "Limite de texto sin adjuntos");
                    description.setText("Escribe aquí una publicación con #hashtags y @menciones.");
                    for (JPanel panel : panels) { panel.setSize(400, 650); layout(panel); }
                } catch (Exception ex) { throw new AssertionError(ex); }
            });
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
            while (imageRequests.get() < 2 && System.nanoTime() < deadline) Thread.sleep(25);
            require(imageRequests.get() >= 2, "Carga de imagenes remotas");
            // Dar paso a las entregas pendientes de los SwingWorker.
            Thread.sleep(250);
            SwingUtilities.invokeAndWait(() -> {
                try {
                    BufferedImage sheet = new BufferedImage(2000, 650, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = sheet.createGraphics();
                    for (int i = 0; i < panels.length; i++) {
                        layout(panels[i]); Graphics2D tile = (Graphics2D) graphics.create(i * 400, 0, 400, 650);
                        panels[i].printAll(tile); tile.dispose();
                    }
                    graphics.dispose(); ImageIO.write(sheet, "png", Path.of("insta-paneles.png").toFile());
                    panels[0].setSize(900, 650);
                } catch (Exception ex) { throw new AssertionError(ex); }
            });
            Thread.sleep(350); // Completar las imágenes que vuelve a pedir el modo escritorio.
            SwingUtilities.invokeAndWait(() -> {
                try {
                    require(!(boolean) field(panels[0], "MODO_MOBILE"), "Modo escritorio al ampliar");
                    layout(panels[0]); BufferedImage desktop = new BufferedImage(900, 650, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = desktop.createGraphics(); panels[0].printAll(graphics); graphics.dispose();
                    ImageIO.write(desktop, "png", Path.of("insta-escritorio.png").toFile());
                    require(Window.getWindows().length == 0, "Los formularios deben permanecer integrados");
                    InstaPostUI detail = new InstaPostUI("ana", manager.getPosts("ana"), 0, () -> {});
                    Method comments = InstaPostUI.class.getDeclaredMethod("abrirComentariosDe", String.class, String.class, JLabel.class);
                    comments.setAccessible(true); comments.invoke(detail, "ana", remote, new JLabel("0"));
                    DefaultListModel<?> model = (DefaultListModel<?>) field(detail, "listModelComentarios");
                    require(model.size() == 1, "Comentario inicial visible");
                    luis.desactivateUser("luis"); detail.refreshActiveState(java.util.Set.of("ana"));
                    require(model.isEmpty() && manager.getComments("ana", remote).isEmpty(), "Desactivar oculta comentarios ya abiertos y nuevas consultas");
                    ((InstaFeedUI) panels[0]).refreshActiveState(java.util.Set.of());
                    require(((java.util.List<?>) field(panels[0], "feedPosts")).isEmpty(), "Ocultar publicaciones ya abiertas");
                } catch (Exception ex) { throw new AssertionError(ex); }
            });
        }
        System.out.println("OK: paneles integrados, limite de texto, imagenes fuera de Swing y modo movil/escritorio.");
    }
    private static void layout(Container container) {
        container.doLayout(); for (Component child : container.getComponents()) if (child instanceof Container nested) layout(nested);
    }
    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(object);
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
