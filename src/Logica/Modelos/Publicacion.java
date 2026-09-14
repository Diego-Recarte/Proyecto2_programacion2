package Logica.Modelos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/** Los cuatro campos de una publicacion, compatibles con insta.ins anterior. */
public record Publicacion(String imagen, String autor, String fecha, String contenido) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static Publicacion read(DataInput input) throws IOException {
        return new Publicacion(input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF());
    }

    public void write(DataOutput output) throws IOException {
        output.writeUTF(imagen); output.writeUTF(autor);
        output.writeUTF(fecha); output.writeUTF(contenido);
    }

    public String[] toArray() { return new String[]{imagen, autor, fecha, contenido}; }
}
