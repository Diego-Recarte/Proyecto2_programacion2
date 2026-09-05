package proyecto2_programacion2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javazoom.jl.player.Player;

/** Prueba manual corta del decodificador y la salida de audio MP3. */
public final class Mp3PlaybackSmokeTest {

    private Mp3PlaybackSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Indica la ruta de un archivo MP3.");
        }

        File archivo = new File(args[0]);
        if (!archivo.isFile()) {
            throw new IllegalArgumentException("No existe el MP3: " + archivo);
        }

        try (BufferedInputStream entrada = new BufferedInputStream(new FileInputStream(archivo))) {
            Player reproductor = new Player(entrada);
            try {
                reproductor.play(40);
                System.out.println("MP3 decodificado y enviado al dispositivo de audio correctamente.");
            } finally {
                reproductor.close();
            }
        }

        GUIReproductorPrincipal panel = new GUIReproductorPrincipal(null, archivo);
        Field campoBoton = GUIReproductorPrincipal.class.getDeclaredField("botonReproducir");
        campoBoton.setAccessible(true);
        JButton boton = (JButton) campoBoton.get(panel);
        Field campoBarra = GUIReproductorPrincipal.class.getDeclaredField("barraProgreso");
        campoBarra.setAccessible(true);
        JSlider barra = (JSlider) campoBarra.get(panel);

        SwingUtilities.invokeAndWait(boton::doClick);
        Thread.sleep(1000);
        if (!boton.getText().contains("Pausar") || barra.getValue() <= 0) {
            throw new AssertionError("El botón de la interfaz no inició la reproducción.");
        }

        int destino = barra.getMaximum() / 2;
        SwingUtilities.invokeAndWait(() -> barra.setValue(destino));
        Thread.sleep(750);
        if (barra.getValue() < destino - 1000 || !boton.getText().contains("Pausar")) {
            throw new AssertionError("La barra no adelantó ni continuó la reproducción.");
        }

        SwingUtilities.invokeAndWait(boton::doClick);
        int posicionPausada = barra.getValue();
        Thread.sleep(500);
        if (Math.abs(barra.getValue() - posicionPausada) > 500) {
            throw new AssertionError("La barra siguió avanzando después de pausar.");
        }

        int retroceso = barra.getMaximum() / 4;
        SwingUtilities.invokeAndWait(() -> barra.setValue(retroceso));
        SwingUtilities.invokeAndWait(boton::doClick);
        Thread.sleep(750);
        if (barra.getValue() < retroceso - 1000 || barra.getValue() >= posicionPausada) {
            throw new AssertionError("La barra no retrocedió y reanudó desde la nueva posición.");
        }

        panel.detener();
        System.out.println("La interfaz reprodujo, adelantó, retrocedió y pausó el MP3 correctamente.");
    }
}
