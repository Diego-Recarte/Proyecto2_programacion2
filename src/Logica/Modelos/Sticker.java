package Logica.Modelos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public record Sticker(String nombre, String imagen, boolean global) implements Serializable {
    private static final long serialVersionUID = 1L;
    public static Sticker read(DataInput in) throws IOException {
        return new Sticker(in.readUTF(), in.readUTF(), in.readBoolean());
    }
    public void write(DataOutput out) throws IOException {
        out.writeUTF(nombre); out.writeUTF(imagen); out.writeBoolean(global);
    }
    public String[] toArray() { return new String[]{nombre, imagen, String.valueOf(global)}; }
}
