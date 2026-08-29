package Instagram.sockets;

import java.util.Objects;
import java.util.UUID;

/** Mensaje inmutable que viaja entre los clientes del chat. */
public final class ChatMessage {

    public enum Type {
        TEXT,
        EMOJI,
        STICKER,
        IMAGE,
        SYSTEM
    }

    private final String id;
    private final String sender;
    private final String recipient;
    private final Type type;
    private final String content;
    private final long timestamp;

    public ChatMessage(String sender, String recipient, Type type, String content) {
        this(UUID.randomUUID().toString(), sender, recipient, type, content, System.currentTimeMillis());
    }

    public ChatMessage(String id, String sender, String recipient, Type type, String content, long timestamp) {
        this.id = requireText(id, "id");
        this.sender = requireText(sender, "remitente");
        this.recipient = requireText(recipient, "destinatario");
        this.type = Objects.requireNonNull(type, "tipo");
        this.content = content != null ? content : "";
        this.timestamp = timestamp;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El " + name + " no puede estar vacío.");
        }
        return value;
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
