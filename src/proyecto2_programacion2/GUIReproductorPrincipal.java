package proyecto2_programacion2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;

/** Controles y reproducción de una canción seleccionada. */
public class GUIReproductorPrincipal extends JPanel {

    private final GUIReproductor padre;
    private final musica cancion;
    private final Object bloqueoAudio = new Object();
    private final boolean audioWav;

    private JButton botonReproducir;
    private JLabel estado;
    private JLabel tiempoActual;
    private JLabel tiempoTotal;
    private JSlider barraProgreso;
    private Timer temporizadorProgreso;

    private volatile AdvancedPlayer reproductorMp3;
    private volatile JavaSoundAudioDevice dispositivoMp3;
    private volatile Clip reproductorWav;
    private volatile boolean reproduciendo;
    private volatile long posicionMs;
    private volatile long posicionBaseMp3Ms;
    private volatile long generacionAudio;

    private long duracionMs;
    private long[] tiemposFramesMp3 = new long[0];
    private boolean actualizandoBarra;

    public GUIReproductorPrincipal(GUIReproductor padre, File archivo)
            throws IOException, ClassNotFoundException {
        this.padre = padre;
        this.cancion = cargarCancion(archivo);

        byte[] audio = cancion.getAudioMp3();
        if (audio == null || audio.length == 0) {
            throw new IOException("El archivo no contiene audio.");
        }

        audioWav = tieneFirmaWav(audio);
        analizarAudio(audio);

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        construirInterfaz();
        actualizarBarra(0);
    }

    private musica cargarCancion(File archivo) throws IOException, ClassNotFoundException {
        if (archivo == null || !archivo.isFile()) {
            throw new IOException("La canción seleccionada no existe.");
        }
        if (archivo.getName().toLowerCase().endsWith(".mp5")) {
            return abrirMP5(archivo);
        }
        return new musica(archivo);
    }

    private void analizarAudio(byte[] audio) throws IOException {
        if (audioWav) {
            analizarWav(audio);
        } else {
            analizarMp3(audio);
        }
        if (duracionMs <= 0) {
            throw new IOException("No fue posible determinar la duración del audio.");
        }
    }

    private void analizarWav(byte[] audio) throws IOException {
        try (AudioInputStream entrada = AudioSystem.getAudioInputStream(
                new BufferedInputStream(new ByteArrayInputStream(audio)))) {
            AudioFormat formato = entrada.getFormat();
            long frames = entrada.getFrameLength();
            float framesPorSegundo = formato.getFrameRate();
            if (frames <= 0 || framesPorSegundo <= 0) {
                throw new IOException("El WAV no contiene información de duración.");
            }
            duracionMs = Math.max(1L, Math.round(frames * 1000.0 / framesPorSegundo));
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("No se pudo analizar el WAV: " + mensaje(ex), ex);
        }
    }

    private void analizarMp3(byte[] audio) throws IOException {
        long[] tiempos = new long[4096];
        int cantidadFrames = 0;
        double tiempoAcumulado = 0;
        Bitstream bitstream = new Bitstream(new BufferedInputStream(new ByteArrayInputStream(audio)));

        try {
            Header header;
            while ((header = bitstream.readFrame()) != null) {
                if (cantidadFrames == tiempos.length) {
                    tiempos = Arrays.copyOf(tiempos, tiempos.length * 2);
                }
                tiempos[cantidadFrames++] = Math.round(tiempoAcumulado);
                tiempoAcumulado += header.ms_per_frame();
                bitstream.closeFrame();
            }
        } catch (JavaLayerException ex) {
            throw new IOException("No se pudo analizar el MP3: " + mensaje(ex), ex);
        } finally {
            try {
                bitstream.close();
            } catch (JavaLayerException ignored) {
                // El análisis ya terminó; no hay más recursos que recuperar.
            }
        }

        if (cantidadFrames == 0) {
            throw new IOException("El archivo no contiene cuadros MP3 válidos.");
        }
        tiemposFramesMp3 = Arrays.copyOf(tiempos, cantidadFrames);
        duracionMs = Math.max(1L, Math.round(tiempoAcumulado));
    }

    private boolean tieneFirmaWav(byte[] audio) {
        if (audio.length < 12) {
            return false;
        }
        String riff = new String(audio, 0, 4, StandardCharsets.US_ASCII);
        String wave = new String(audio, 8, 4, StandardCharsets.US_ASCII);
        return "RIFF".equals(riff) && "WAVE".equals(wave);
    }

    private void construirInterfaz() {
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        JLabel titulo = crearEtiqueta(cancion.getNombre(), 24, Font.BOLD, Color.WHITE);
        JLabel artista = crearEtiqueta(textoOAlternativa(cancion.getArtista(), "Artista desconocido"),
                14, Font.PLAIN, Color.LIGHT_GRAY);
        JLabel album = crearEtiqueta(textoOAlternativa(cancion.getAlbum(), "Álbum desconocido"),
                12, Font.PLAIN, Color.GRAY);

        JLabel caratula = new JLabel("♪", SwingConstants.CENTER);
        caratula.setAlignmentX(CENTER_ALIGNMENT);
        caratula.setPreferredSize(new Dimension(320, 270));
        caratula.setMinimumSize(new Dimension(320, 270));
        caratula.setMaximumSize(new Dimension(320, 270));
        caratula.setOpaque(true);
        caratula.setBackground(new Color(35, 35, 35));
        caratula.setForeground(new Color(220, 65, 45));
        caratula.setFont(new Font("SansSerif", Font.BOLD, 110));

        byte[] bytesCaratula = cancion.getCaratula();
        if (bytesCaratula != null && bytesCaratula.length > 0) {
            ImageIcon original = new ImageIcon(bytesCaratula);
            Image escalada = original.getImage().getScaledInstance(320, 270, Image.SCALE_SMOOTH);
            caratula.setText("");
            caratula.setIcon(new ImageIcon(escalada));
        }

        JTextArea descripcion = new JTextArea(textoOAlternativa(
                cancion.getDescripcion(), "Sin descripción"));
        descripcion.setEditable(false);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        descripcion.setRows(2);
        descripcion.setBackground(new Color(25, 25, 25));
        descripcion.setForeground(Color.WHITE);
        descripcion.setCaretColor(Color.WHITE);
        descripcion.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scrollDescripcion = new JScrollPane(descripcion);
        scrollDescripcion.setAlignmentX(CENTER_ALIGNMENT);
        scrollDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        scrollDescripcion.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55)));

        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(artista);
        contenido.add(album);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(caratula);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(scrollDescripcion);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(crearControles());

        add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearControles() {
        int maximoBarra = (int) Math.min(Integer.MAX_VALUE, duracionMs);
        barraProgreso = new JSlider(0, maximoBarra, 0);
        barraProgreso.setOpaque(false);
        barraProgreso.setFocusable(true);
        barraProgreso.setToolTipText("Arrastra para retroceder o adelantar");

        tiempoActual = crearEtiqueta("0:00", 12, Font.PLAIN, Color.LIGHT_GRAY);
        tiempoTotal = crearEtiqueta(formatearTiempo(duracionMs), 12, Font.PLAIN, Color.LIGHT_GRAY);

        JPanel progreso = new JPanel(new BorderLayout(8, 0));
        progreso.setOpaque(false);
        progreso.add(tiempoActual, BorderLayout.WEST);
        progreso.add(barraProgreso, BorderLayout.CENTER);
        progreso.add(tiempoTotal, BorderLayout.EAST);

        barraProgreso.addChangeListener(e -> {
            if (actualizandoBarra) {
                return;
            }
            long destino = valorBarraAMilisegundos(barraProgreso.getValue());
            tiempoActual.setText(formatearTiempo(destino));
            if (!barraProgreso.getValueIsAdjusting()) {
                posicionarEn(destino);
            }
        });

        MouseAdapter controlRaton = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    barraProgreso.setValueIsAdjusting(true);
                    actualizarBarraDesdeRaton(e.getX());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
                    actualizarBarraDesdeRaton(e.getX());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    actualizarBarraDesdeRaton(e.getX());
                    barraProgreso.setValueIsAdjusting(false);
                }
            }
        };
        barraProgreso.addMouseListener(controlRaton);
        barraProgreso.addMouseMotionListener(controlRaton);

        JPanel botones = new JPanel();
        botones.setOpaque(false);

        JButton anterior = crearBoton("⏮ Anterior");
        botonReproducir = crearBoton("▶ Reproducir");
        JButton siguiente = crearBoton("Siguiente ⏭");

        anterior.addActionListener(e -> padre.cancionAnterior());
        botonReproducir.addActionListener(e -> alternarReproduccion());
        siguiente.addActionListener(e -> padre.cancionSiguiente());

        botones.add(anterior);
        botones.add(botonReproducir);
        botones.add(siguiente);

        estado = new JLabel("Lista para reproducir", SwingConstants.CENTER);
        estado.setForeground(Color.LIGHT_GRAY);

        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setOpaque(false);
        contenedor.setAlignmentX(CENTER_ALIGNMENT);
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        progreso.setAlignmentX(CENTER_ALIGNMENT);
        botones.setAlignmentX(CENTER_ALIGNMENT);
        estado.setAlignmentX(CENTER_ALIGNMENT);
        contenedor.add(progreso);
        contenedor.add(Box.createVerticalStrut(4));
        contenedor.add(botones);
        contenedor.add(estado);

        temporizadorProgreso = new Timer(250, e -> actualizarProgresoDesdeMotor());
        temporizadorProgreso.setCoalesce(true);
        return contenedor;
    }

    private JLabel crearEtiqueta(String texto, int tamano, int estilo, Color color) {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        etiqueta.setFont(new Font("Arial", estilo, tamano));
        etiqueta.setForeground(color);
        etiqueta.setAlignmentX(CENTER_ALIGNMENT);
        return etiqueta;
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(135, 38));
        boton.setBackground(new Color(210, 50, 35));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        return boton;
    }

    private String textoOAlternativa(String texto, String alternativa) {
        return texto == null || texto.isBlank() ? alternativa : texto;
    }

    private void alternarReproduccion() {
        if (reproduciendo) {
            pausar();
        } else {
            reproducir();
        }
    }

    private void reproducir() {
        if (posicionMs >= duracionMs) {
            posicionMs = 0;
            actualizarBarra(0);
        }

        detenerMotor();
        try {
            if (audioWav) {
                iniciarWav();
            } else {
                iniciarMp3();
            }
            temporizadorProgreso.start();
            actualizarEstado("Reproduciendo", true);
        } catch (Exception ex) {
            detenerMotor();
            actualizarEstado("No se pudo reproducir: " + mensaje(ex), false);
        }
    }

    private void iniciarMp3() throws JavaLayerException {
        int frameInicial = buscarFrame(posicionMs);
        long baseMs = tiemposFramesMp3[frameInicial];
        BufferedInputStream entrada = new BufferedInputStream(
                new ByteArrayInputStream(cancion.getAudioMp3()));
        JavaSoundAudioDevice nuevoDispositivo = new JavaSoundAudioDevice();
        AdvancedPlayer nuevoReproductor = new AdvancedPlayer(entrada, nuevoDispositivo);
        long nuevaGeneracion;

        synchronized (bloqueoAudio) {
            nuevaGeneracion = ++generacionAudio;
            reproductorMp3 = nuevoReproductor;
            dispositivoMp3 = nuevoDispositivo;
            reproductorWav = null;
            posicionBaseMp3Ms = baseMs;
            posicionMs = baseMs;
            reproduciendo = true;
        }

        Thread hilo = new Thread(() -> {
            String error = null;
            try {
                nuevoReproductor.play(frameInicial, tiemposFramesMp3.length);
            } catch (JavaLayerException ex) {
                error = mensaje(ex);
            } finally {
                String errorFinal = error;
                SwingUtilities.invokeLater(() -> finalizarMp3(
                        nuevaGeneracion, nuevoReproductor, errorFinal));
            }
        }, "reproductor-mp3-" + cancion.getNombre());
        hilo.setDaemon(true);
        hilo.start();
    }

    private void finalizarMp3(long generacion, AdvancedPlayer reproductor, String error) {
        synchronized (bloqueoAudio) {
            if (generacion != generacionAudio || reproductorMp3 != reproductor) {
                return;
            }
            reproductorMp3 = null;
            dispositivoMp3 = null;
            reproduciendo = false;
            if (error == null) {
                posicionMs = duracionMs;
            }
        }
        temporizadorProgreso.stop();
        actualizarBarra(posicionMs);
        actualizarEstado(error == null ? "Reproducción finalizada" : "Error: " + error, false);
    }

    private void iniciarWav() throws Exception {
        Clip nuevo = AudioSystem.getClip();
        try (AudioInputStream entrada = AudioSystem.getAudioInputStream(
                new BufferedInputStream(new ByteArrayInputStream(cancion.getAudioMp3())))) {
            nuevo.open(entrada);
        }

        long nuevaGeneracion;
        synchronized (bloqueoAudio) {
            nuevaGeneracion = ++generacionAudio;
            reproductorWav = nuevo;
            reproductorMp3 = null;
            dispositivoMp3 = null;
            reproduciendo = true;
        }

        nuevo.addLineListener(evento -> {
            if (evento.getType() != LineEvent.Type.STOP
                    || nuevo.getMicrosecondPosition() + 10_000 < nuevo.getMicrosecondLength()) {
                return;
            }
            SwingUtilities.invokeLater(() -> finalizarWav(nuevaGeneracion, nuevo));
        });
        nuevo.setMicrosecondPosition(Math.min(posicionMs * 1000L, nuevo.getMicrosecondLength()));
        nuevo.start();
    }

    private void finalizarWav(long generacion, Clip reproductor) {
        synchronized (bloqueoAudio) {
            if (generacion != generacionAudio || reproductorWav != reproductor) {
                return;
            }
            reproductorWav = null;
            reproduciendo = false;
            posicionMs = duracionMs;
        }
        reproductor.close();
        temporizadorProgreso.stop();
        actualizarBarra(duracionMs);
        actualizarEstado("Reproducción finalizada", false);
    }

    private void pausar() {
        sincronizarPosicionDesdeMotor();
        detenerMotor();
        actualizarBarra(posicionMs);
        actualizarEstado("Pausada", false);
    }

    private void posicionarEn(long destinoMs) {
        long destino = limitar(destinoMs);
        boolean estabaReproduciendo = reproduciendo;
        detenerMotor();
        posicionMs = destino;
        actualizarBarra(destino);

        if (estabaReproduciendo && destino < duracionMs) {
            reproducir();
        } else {
            actualizarEstado(destino >= duracionMs ? "Fin de la canción" : "Posición seleccionada", false);
        }
    }

    private void actualizarProgresoDesdeMotor() {
        if (!reproduciendo || barraProgreso.getValueIsAdjusting()) {
            return;
        }
        sincronizarPosicionDesdeMotor();
        actualizarBarra(posicionMs);
    }

    private void sincronizarPosicionDesdeMotor() {
        long nuevaPosicion = posicionMs;
        Clip wav = reproductorWav;
        JavaSoundAudioDevice dispositivo = dispositivoMp3;

        if (wav != null && wav.isOpen()) {
            nuevaPosicion = wav.getMicrosecondPosition() / 1000L;
        } else if (dispositivo != null) {
            nuevaPosicion = posicionBaseMp3Ms + Math.max(0, dispositivo.getPosition());
        }
        posicionMs = limitar(nuevaPosicion);
    }

    public void detener() {
        detenerMotor();
        posicionMs = 0;
        if (barraProgreso != null) {
            actualizarBarra(0);
            actualizarEstado("Reproducción detenida", false);
        }
    }

    private void detenerMotor() {
        AdvancedPlayer mp3;
        Clip wav;
        synchronized (bloqueoAudio) {
            generacionAudio++;
            mp3 = reproductorMp3;
            wav = reproductorWav;
            reproductorMp3 = null;
            dispositivoMp3 = null;
            reproductorWav = null;
            reproduciendo = false;
        }
        if (temporizadorProgreso != null) {
            temporizadorProgreso.stop();
        }
        if (mp3 != null) {
            mp3.close();
        }
        if (wav != null) {
            wav.stop();
            wav.close();
        }
    }

    private int buscarFrame(long milisegundos) {
        int indice = Arrays.binarySearch(tiemposFramesMp3, limitar(milisegundos));
        if (indice < 0) {
            indice = -indice - 2;
        }
        return Math.max(0, Math.min(indice, tiemposFramesMp3.length - 1));
    }

    private long limitar(long milisegundos) {
        return Math.max(0L, Math.min(milisegundos, duracionMs));
    }

    private void actualizarBarra(long milisegundos) {
        long posicion = limitar(milisegundos);
        int valor = milisegundosAValorBarra(posicion);
        actualizandoBarra = true;
        try {
            barraProgreso.setValue(valor);
            tiempoActual.setText(formatearTiempo(posicion));
            tiempoTotal.setText(formatearTiempo(duracionMs));
        } finally {
            actualizandoBarra = false;
        }
    }

    private int milisegundosAValorBarra(long milisegundos) {
        if (duracionMs <= Integer.MAX_VALUE) {
            return (int) milisegundos;
        }
        return (int) Math.round(milisegundos * (double) Integer.MAX_VALUE / duracionMs);
    }

    private void actualizarBarraDesdeRaton(int x) {
        int anchoUtil = Math.max(1, barraProgreso.getWidth() - 1);
        double proporcion = Math.max(0.0, Math.min(1.0, x / (double) anchoUtil));
        int valor = (int) Math.round(proporcion * barraProgreso.getMaximum());
        barraProgreso.setValue(valor);
    }

    private long valorBarraAMilisegundos(int valor) {
        if (duracionMs <= Integer.MAX_VALUE) {
            return valor;
        }
        return Math.round(valor * (double) duracionMs / Integer.MAX_VALUE);
    }

    private String formatearTiempo(long milisegundos) {
        long segundosTotales = Math.max(0L, milisegundos / 1000L);
        long horas = segundosTotales / 3600L;
        long minutos = (segundosTotales % 3600L) / 60L;
        long segundos = segundosTotales % 60L;
        return horas > 0
                ? String.format("%d:%02d:%02d", horas, minutos, segundos)
                : String.format("%d:%02d", minutos, segundos);
    }

    private void actualizarEstado(String texto, boolean activo) {
        if (estado != null) {
            estado.setText(texto);
        }
        if (botonReproducir != null) {
            botonReproducir.setText(activo ? "⏸ Pausar" : "▶ Reproducir");
        }
    }

    private static String mensaje(Exception ex) {
        String texto = ex.getMessage();
        return texto == null || texto.isBlank() ? ex.getClass().getSimpleName() : texto;
    }

    public static musica abrirMP5(File archivoMP5) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(archivoMP5);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object contenido = ois.readObject();
            if (!(contenido instanceof musica musicaLeida)) {
                throw new IOException("El archivo MP5 no contiene una canción válida.");
            }
            return musicaLeida;
        }
    }
}
