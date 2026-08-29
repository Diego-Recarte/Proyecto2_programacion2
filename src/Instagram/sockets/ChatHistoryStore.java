package Instagram.sockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Bandeja binaria privada: cada usuario conserva su propia copia de los mensajes. */
final class ChatHistoryStore {

    private static final int MAGIC = 0x494E5354; // INST
    private static final int VERSION = 1;
    private static final int MAX_FIELD_BYTES = 8 * 1024 * 1024;

    private final Path usersRoot;

    ChatHistoryStore(Path usersRoot) throws IOException {
        this.usersRoot = usersRoot;
        Files.createDirectories(usersRoot);
    }

    synchronized void append(ChatMessage message) throws IOException {
        appendForUser(message.getSender(), new StoredMessage(message, true));
        if (!message.getSender().equals(message.getRecipient())) {
            appendForUser(message.getRecipient(), new StoredMessage(message, false));
        }
    }

    synchronized List<ChatMessage> between(String viewer, String peer) throws IOException {
        List<ChatMessage> result = new ArrayList<>();
        for (StoredMessage stored : readAll(viewer)) {
            ChatMessage message = stored.message();
            boolean forward = viewer.equals(message.getSender()) && peer.equals(message.getRecipient());
            boolean backward = peer.equals(message.getSender()) && viewer.equals(message.getRecipient());
            if (forward || backward) {
                result.add(message);
            }
        }
        result.sort((first, second) -> Long.compare(first.getTimestamp(), second.getTimestamp()));
        return result;
    }

    synchronized int unreadCount(String viewer) throws IOException {
        int total = 0;
        for (StoredMessage stored : readAll(viewer)) {
            if (!stored.read() && viewer.equals(stored.message().getRecipient())) {
                total++;
            }
        }
        return total;
    }

    synchronized void markRead(String viewer, String peer) throws IOException {
        List<StoredMessage> messages = readAll(viewer);
        boolean changed = false;
        List<StoredMessage> updated = new ArrayList<>(messages.size());
        for (StoredMessage stored : messages) {
            ChatMessage message = stored.message();
            if (!stored.read() && peer.equals(message.getSender()) && viewer.equals(message.getRecipient())) {
                updated.add(new StoredMessage(message, true));
                changed = true;
            } else {
                updated.add(stored);
            }
        }
        if (changed) {
            rewrite(viewer, updated);
        }
    }

    synchronized void deleteConversation(String viewer, String peer) throws IOException {
        List<StoredMessage> remaining = new ArrayList<>();
        for (StoredMessage stored : readAll(viewer)) {
            ChatMessage message = stored.message();
            boolean belongs = (viewer.equals(message.getSender()) && peer.equals(message.getRecipient()))
                    || (peer.equals(message.getSender()) && viewer.equals(message.getRecipient()));
            if (!belongs) {
                remaining.add(stored);
            }
        }
        rewrite(viewer, remaining);
    }

    private void appendForUser(String username, StoredMessage stored) throws IOException {
        Path inbox = inbox(username);
        initialize(inbox);
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(
                Files.newOutputStream(inbox, StandardOpenOption.APPEND)))) {
            writeRecord(output, stored);
        }
    }

    private List<StoredMessage> readAll(String username) throws IOException {
        Path inbox = inbox(username);
        initialize(inbox);
        List<StoredMessage> result = new ArrayList<>();
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(inbox)))) {
            int magic = input.readInt();
            int version = input.readInt();
            if (magic != MAGIC || version != VERSION) {
                throw new IOException("El formato de inbox.ins no es compatible.");
            }
            while (true) {
                try {
                    result.add(readRecord(input));
                } catch (EOFException end) {
                    break;
                }
            }
        }
        return result;
    }

    private void rewrite(String username, List<StoredMessage> messages) throws IOException {
        Path inbox = inbox(username);
        Path temporary = inbox.resolveSibling("inbox.ins.tmp");
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(
                temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)))) {
            output.writeInt(MAGIC);
            output.writeInt(VERSION);
            for (StoredMessage message : messages) {
                writeRecord(output, message);
            }
        }
        try {
            Files.move(temporary, inbox, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporary, inbox, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path inbox(String username) throws IOException {
        if (username == null || username.isBlank() || username.contains("..")
                || username.contains("/") || username.contains("\\")) {
            throw new IOException("Nombre de usuario inválido para la bandeja.");
        }
        Path userDirectory = usersRoot.resolve(username);
        Files.createDirectories(userDirectory);
        return userDirectory.resolve("inbox.ins");
    }

    private static void initialize(Path inbox) throws IOException {
        if (!Files.exists(inbox) || Files.size(inbox) == 0) {
            try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(
                    inbox, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)))) {
                output.writeInt(MAGIC);
                output.writeInt(VERSION);
            }
        }
    }

    private static void writeRecord(DataOutputStream output, StoredMessage stored) throws IOException {
        ChatMessage message = stored.message();
        writeString(output, message.getId());
        writeString(output, message.getSender());
        writeString(output, message.getRecipient());
        writeString(output, message.getType().name());
        writeString(output, message.getContent());
        output.writeLong(message.getTimestamp());
        output.writeBoolean(stored.read());
    }

    private static StoredMessage readRecord(DataInputStream input) throws IOException {
        String id = readString(input);
        String sender = readString(input);
        String recipient = readString(input);
        ChatMessage.Type type = ChatMessage.Type.valueOf(readString(input));
        String content = readString(input);
        long timestamp = input.readLong();
        boolean read = input.readBoolean();
        return new StoredMessage(new ChatMessage(id, sender, recipient, type, content, timestamp), read);
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_FIELD_BYTES) {
            throw new IOException("El contenido del mensaje excede el tamaño permitido.");
        }
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    private static String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0 || length > MAX_FIELD_BYTES) {
            throw new IOException("Longitud inválida en inbox.ins.");
        }
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("Registro incompleto en inbox.ins.");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private record StoredMessage(ChatMessage message, boolean read) {
    }
}
