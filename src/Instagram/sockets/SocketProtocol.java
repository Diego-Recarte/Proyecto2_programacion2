package Instagram.sockets;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Protocolo de texto seguro para transportar UTF-8, emojis e imágenes. */
final class SocketProtocol {

    static final String HELLO = "HELLO";
    static final String SEND = "SEND";
    static final String MESSAGE = "MESSAGE";
    static final String HISTORY = "HISTORY";
    static final String HISTORY_START = "HISTORY_START";
    static final String HISTORY_END = "HISTORY_END";
    static final String MARK_READ = "MARK_READ";
    static final String DELETE_CONVERSATION = "DELETE_CONVERSATION";
    static final String CONVERSATION_DELETED = "CONVERSATION_DELETED";
    static final String UNREAD = "UNREAD";
    static final String USERS = "USERS";
    static final String ERROR = "ERROR";
    static final String PING = "PING";
    static final String PONG = "PONG";

    private SocketProtocol() {
    }

    static String hello(String username) {
        return withArgument(HELLO, username);
    }

    static String history(String peer) {
        return withArgument(HISTORY, peer);
    }

    static String unread(int count) {
        return withArgument(UNREAD, Integer.toString(count));
    }

    static String withArgument(String command, String argument) {
        return command + "\t" + encode(argument);
    }

    static String message(String command, ChatMessage message) {
        return command + "\t"
                + encode(message.getId()) + "\t"
                + encode(message.getSender()) + "\t"
                + encode(message.getRecipient()) + "\t"
                + message.getType().name() + "\t"
                + message.getTimestamp() + "\t"
                + encode(message.getContent());
    }

    static ChatMessage parseMessage(String line, String expectedCommand) {
        String[] parts = line.split("\t", -1);
        if (parts.length != 7 || !expectedCommand.equals(parts[0])) {
            throw new IllegalArgumentException("Mensaje de socket inválido.");
        }
        return new ChatMessage(
                decode(parts[1]),
                decode(parts[2]),
                decode(parts[3]),
                ChatMessage.Type.valueOf(parts[4]),
                decode(parts[6]),
                Long.parseLong(parts[5])
        );
    }

    static String users(Iterable<String> usernames) {
        StringBuilder result = new StringBuilder(USERS);
        for (String username : usernames) {
            result.append('\t').append(encode(username));
        }
        return result.toString();
    }

    static List<String> parseUsers(String line) {
        String[] parts = line.split("\t", -1);
        List<String> result = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            result.add(decode(parts[i]));
        }
        return result;
    }

    static String error(String message) {
        return withArgument(ERROR, message);
    }

    static String argument(String line) {
        String[] parts = line.split("\t", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Comando incompleto.");
        }
        return decode(parts[1]);
    }

    static String command(String line) {
        int separator = line.indexOf('\t');
        return separator >= 0 ? line.substring(0, separator) : line;
    }

    static String encodeStored(ChatMessage message) {
        return message(MESSAGE, message);
    }

    static ChatMessage decodeStored(String line) {
        return parseMessage(line, MESSAGE);
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
