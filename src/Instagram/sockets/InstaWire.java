package Instagram.sockets;

import Logica.Estructuras.ListaEnlazada;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/** Protocolo binario con tipos y limites explicitos; no deserializa objetos Java. */
public final class InstaWire {
    public static final int MAGIC = 0x494E5331;
    public static final int MAX_BYTES = 16 * 1024 * 1024;
    private InstaWire() { }

    public static void write(DataOutput out, Object value) throws IOException {
        if (value == null) { out.writeByte(0); }
        else if (value instanceof String text) {
            out.writeByte(1); byte[] bytes = text.getBytes(StandardCharsets.UTF_8); bytes(out, bytes);
        } else if (value instanceof Boolean flag) { out.writeByte(2); out.writeBoolean(flag); }
        else if (value instanceof Integer number) { out.writeByte(3); out.writeInt(number); }
        else if (value instanceof Long number) { out.writeByte(4); out.writeLong(number); }
        else if (value instanceof Character character) { out.writeByte(5); out.writeChar(character); }
        else if (value instanceof byte[] bytes) { out.writeByte(6); bytes(out, bytes); }
        else if (value instanceof String[] row) {
            out.writeByte(7); out.writeInt(row.length); for (String text : row) write(out, text);
        } else if (value instanceof Collection<?> items) {
            out.writeByte(8); out.writeInt(items.size()); for (Object item : items) write(out, item);
        } else if (value instanceof Object[] items) {
            out.writeByte(9); out.writeInt(items.length); for (Object item : items) write(out, item);
        } else throw new IOException("Tipo no permitido en el protocolo.");
    }

    private static void bytes(DataOutput out, byte[] bytes) throws IOException {
        if (bytes.length > MAX_BYTES) throw new IOException("Archivo demasiado grande.");
        out.writeInt(bytes.length); out.write(bytes);
    }

    public static Object read(DataInput in) throws IOException { return read(in, 0, new int[]{MAX_BYTES * 2}); }

    private static int count(DataInput in, int maximum, int[] budget) throws IOException {
        int size = in.readInt();
        if (size < 0 || size > maximum || (budget[0] -= size) < 0) throw new IOException("Tamaño de mensaje inválido.");
        return size;
    }

    private static Object read(DataInput in, int depth, int[] budget) throws IOException {
        if (depth > 8) throw new IOException("Mensaje demasiado anidado.");
        int type = in.readUnsignedByte();
        return switch (type) {
            case 0 -> null;
            case 1, 6 -> {
                byte[] bytes = new byte[count(in, MAX_BYTES, budget)]; in.readFully(bytes);
                yield type == 1 ? new String(bytes, StandardCharsets.UTF_8) : bytes;
            }
            case 2 -> in.readBoolean();
            case 3 -> in.readInt();
            case 4 -> in.readLong();
            case 5 -> in.readChar();
            case 7 -> {
                String[] values = new String[count(in, 100, budget)];
                for (int i = 0; i < values.length; i++) values[i] = (String) read(in, depth + 1, budget);
                yield values;
            }
            case 8 -> {
                int count = count(in, 100000, budget);
                ListaEnlazada<Object> values = new ListaEnlazada<>();
                for (int i = 0; i < count; i++) values.add(read(in, depth + 1, budget));
                yield values;
            }
            case 9 -> {
                Object[] values = new Object[count(in, 20, budget)];
                for (int i = 0; i < values.length; i++) values[i] = read(in, depth + 1, budget);
                yield values;
            }
            default -> throw new IOException("Tipo de mensaje desconocido.");
        };
    }
}
