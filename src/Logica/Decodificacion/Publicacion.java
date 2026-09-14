package Logica.Decodificacion;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/** Los cuatro campos de una publicacion, compatibles con insta.ins anterior. */
public record Publicacion(String imagen, String autor, String fecha, String contenido) implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MAX_TEXTO = 220;

    public static int textLength(String text) {
        return text == null ? 0 : text.codePointCount(0, text.length());
    }

    private static final java.time.format.DateTimeFormatter FECHA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm[:ss[.SSS]]");

    public static long timestamp(String fecha) {
        try {
            return java.time.LocalDateTime.parse(fecha, FECHA).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (java.time.format.DateTimeParseException | NullPointerException ex) {
            return 0; // Los registros antiguos sin fecha válida permanecen al final.
        }
    }

    /** Identifica la fila exacta, incluso si comparte imagen o minuto con otra. */
    public static boolean sameRow(String[] first, String[] second) {
        if (first == null || second == null || first.length < 4 || second.length < 4) return false;
        for (int i = 0; i < 4; i++) if (!java.util.Objects.equals(first[i], second[i])) return false;
        return true;
    }

    public static Publicacion read(DataInput input) throws IOException {
        return new Publicacion(input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF());
    }

    public void write(DataOutput output) throws IOException {
        output.writeUTF(imagen); output.writeUTF(autor);
        output.writeUTF(fecha); output.writeUTF(contenido);
    }

    public String[] toArray() { return new String[]{imagen, autor, fecha, contenido}; }
}
