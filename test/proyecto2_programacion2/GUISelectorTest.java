package proyecto2_programacion2;

import Logica.RutasSistema;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/** Ejecutar en un directorio vacío; los diálogos se verifican sin mostrarlos. */
public final class GUISelectorTest {
    public static void main(String[] args) throws Exception {
        require(!RutasSistema.Z.exists(), "La prueba requiere un directorio vacío.");
        usuarioWinActivo.nombre = "ana";
        usuarioWinActivo.isAdmin = false;
        File base = GUISelector.carpetaDelPerfil();
        require(base.equals(RutasSistema.usuario("ana").getCanonicalFile()), "Raíz incorrecta para Ana.");
        File otra = RutasSistema.usuario("ana2");
        Files.createDirectories(otra.toPath());
        File ajena = new File(otra, "ajena.png");
        Files.write(ajena.toPath(), new byte[]{1});
        Files.write(new File(base, "uno.png").toPath(), new byte[]{1});
        Files.write(new File(base, "dos.PNG").toPath(), new byte[]{2});
        Files.write(new File(base, "documento.pwrd").toPath(), new byte[]{3});
        Files.write(new File(base, "sin-extension").toPath(), new byte[]{4});
        Files.createDirectories(new File(base, "imagenes").toPath());

        usuarioWinActivo.isAdmin = true;
        require(GUISelector.carpetaDelPerfil().equals(RutasSistema.USUARIOS.getCanonicalFile()),
                "El administrador no tiene la raíz de usuarios.");
        usuarioWinActivo.isAdmin = false;
        usuarioWinActivo.nombre = "../fuera";
        try {
            GUISelector.carpetaDelPerfil();
            throw new AssertionError("Se aceptó un perfil fuera de infoUsuarios.");
        } catch (IOException expected) {
        }
        usuarioWinActivo.nombre = null;
        try {
            GUISelector.carpetaDelPerfil();
            throw new AssertionError("Se aceptó un perfil sin sesión.");
        } catch (IOException expected) {
        }
        usuarioWinActivo.nombre = "ana";

        SwingUtilities.invokeAndWait(() -> {
            try {
                GUISelector selector = new GUISelector(null, base, "png");
                try {
                    require(fila(selector, "documento.pwrd") == null, "No se filtró el documento.");
                    require(fila(selector, "ajena.png") == null, "Se mostró una imagen ajena.");
                    fila(selector, "imagenes").doClick();
                    boton(selector, "botonAbrir").doClick();
                    boton(selector, "botonVolver").doClick();
                    require(base.equals(campo(selector, "carpetaActual")), "No regresó a la base.");
                    boton(selector, "botonVolver").doClick();
                    require(base.equals(campo(selector, "carpetaActual")), "Salió de la base.");
                    fila(selector, "uno.png").doClick();
                    require(selector.getArchivoSeleccionado() == null, "Aceptó antes de confirmar.");
                    boton(selector, "botonAceptar").doClick();
                    require(selector.getArchivoSeleccionado().getName().equals("uno.png"), "Selección incorrecta.");
                } finally {
                    selector.dispose();
                }

                for (boolean cerrar : new boolean[]{false, true}) {
                    selector = new GUISelector(null, base, "png");
                    try {
                        fila(selector, "uno.png").doClick();
                        if (cerrar) {
                            selector.dispatchEvent(new WindowEvent(selector, WindowEvent.WINDOW_CLOSING));
                        } else {
                            boton(selector, "botonCancelar").doClick();
                        }
                        require(selector.getArchivoSeleccionado() == null, "Cancelar o cerrar devolvió un archivo.");
                    } finally {
                        selector.dispose();
                    }
                }

                selector = new GUISelector(null, base, "png");
                try {
                    selector.setSeleccionMultiple(true);
                    fila(selector, "uno.png").doClick();
                    fila(selector, "dos.PNG").doClick();
                    fila(selector, "dos.PNG").doClick();
                    fila(selector, "dos.PNG").doClick();
                    boton(selector, "botonAceptar").doClick();
                    require(selector.getArchivosSeleccionados().length == 2, "Se perdió la selección múltiple.");
                } finally {
                    selector.dispose();
                }

                selector = new GUISelector(null, base, "*");
                try {
                    require(fila(selector, "sin-extension") != null, "El importador excluye archivos sin extensión.");
                    Field field = GUISelector.class.getDeclaredField("archivoSeleccionado");
                    field.setAccessible(true);
                    field.set(selector, ajena);
                    boton(selector, "botonAceptar").doClick();
                    require(selector.getArchivoSeleccionado() == null, "Aceptó un archivo de otro perfil.");
                } finally {
                    selector.dispose();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        System.out.println("OK: raíz por perfil, filtros, navegación, cancelación y selección múltiple.");
    }

    private static Object campo(GUISelector selector, String nombre) throws Exception {
        Field field = GUISelector.class.getDeclaredField(nombre);
        field.setAccessible(true);
        return field.get(selector);
    }

    private static JButton boton(GUISelector selector, String nombre) throws Exception {
        return (JButton) campo(selector, nombre);
    }

    private static JToggleButton fila(GUISelector selector, String nombre) throws Exception {
        for (Component component : ((JPanel) campo(selector, "panelLista")).getComponents()) {
            if (component instanceof JToggleButton button && button.getText().endsWith("] " + nombre)) {
                return button;
            }
        }
        return null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
