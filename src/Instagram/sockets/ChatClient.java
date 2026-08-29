package Instagram.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/** Cliente reutilizable y sin dependencias de Swing. */
public final class ChatClient implements AutoCloseable {

    public interface Listener {
        default void onMessage(ChatMessage message) {
        }

        default void onOnlineUsers(Set<String> users) {
        }

        default void onHistoryStarted(String peer) {
        }

        default void onHistoryFinished(String peer) {
        }

        default void onConnectionChanged(boolean connected, String detail) {
        }

        default void onUnreadCount(int count) {
        }

        default void onConversationDeleted(String peer) {
        }
    }

    private final String host;
    private final int port;
    private final String username;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Object outputLock = new Object();
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Thread readerThread;

    public ChatClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public void addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public synchronized void connect() throws IOException {
        if (connected.get()) {
            return;
        }
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 2500);
        socket.setTcpNoDelay(true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        connected.set(true);
        sendLine(SocketProtocol.hello(username));
        readerThread = new Thread(this::readLoop, "instagram-chat-reader-" + username);
        readerThread.setDaemon(true);
        readerThread.start();
        fireConnection(true, "Conectado");
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void send(ChatMessage message) throws IOException {
        if (!connected.get()) {
            throw new IOException("El chat no está conectado.");
        }
        sendLine(SocketProtocol.message(SocketProtocol.SEND, message));
    }

    public void requestHistory(String peer) throws IOException {
        if (!connected.get()) {
            throw new IOException("El chat no está conectado.");
        }
        sendLine(SocketProtocol.history(peer));
    }

    public void markRead(String peer) throws IOException {
        requireConnection();
        sendLine(SocketProtocol.withArgument(SocketProtocol.MARK_READ, peer));
    }

    public void deleteConversation(String peer) throws IOException {
        requireConnection();
        sendLine(SocketProtocol.withArgument(SocketProtocol.DELETE_CONVERSATION, peer));
    }

    private void requireConnection() throws IOException {
        if (!connected.get()) {
            throw new IOException("El chat no está conectado.");
        }
    }

    private void readLoop() {
        String detail = "Conexión cerrada";
        try {
            String line;
            while (connected.get() && (line = input.readLine()) != null) {
                handle(line);
            }
        } catch (IOException | RuntimeException ex) {
            detail = ex.getMessage() != null ? ex.getMessage() : "Conexión interrumpida";
        } finally {
            closeResources();
            fireConnection(false, detail);
        }
    }

    private void handle(String line) {
        String command = SocketProtocol.command(line);
        switch (command) {
            case SocketProtocol.MESSAGE -> {
                ChatMessage message = SocketProtocol.parseMessage(line, SocketProtocol.MESSAGE);
                for (Listener listener : listeners) {
                    listener.onMessage(message);
                }
            }
            case SocketProtocol.USERS -> {
                Set<String> users = Collections.unmodifiableSet(new HashSet<>(SocketProtocol.parseUsers(line)));
                for (Listener listener : listeners) {
                    listener.onOnlineUsers(users);
                }
            }
            case SocketProtocol.HISTORY_START -> {
                String peer = SocketProtocol.argument(line);
                for (Listener listener : listeners) {
                    listener.onHistoryStarted(peer);
                }
            }
            case SocketProtocol.HISTORY_END -> {
                String peer = SocketProtocol.argument(line);
                for (Listener listener : listeners) {
                    listener.onHistoryFinished(peer);
                }
            }
            case SocketProtocol.UNREAD -> {
                int count = Integer.parseInt(SocketProtocol.argument(line));
                for (Listener listener : listeners) {
                    listener.onUnreadCount(count);
                }
            }
            case SocketProtocol.CONVERSATION_DELETED -> {
                String peer = SocketProtocol.argument(line);
                for (Listener listener : listeners) {
                    listener.onConversationDeleted(peer);
                }
            }
            case SocketProtocol.ERROR -> fireConnection(connected.get(), SocketProtocol.argument(line));
            default -> {
            }
        }
    }

    private void sendLine(String line) throws IOException {
        synchronized (outputLock) {
            if (output == null) {
                throw new IOException("No existe una salida de socket activa.");
            }
            output.println(line);
            if (output.checkError()) {
                throw new IOException("No se pudo enviar el mensaje.");
            }
        }
    }

    private void fireConnection(boolean state, String detail) {
        for (Listener listener : listeners) {
            listener.onConnectionChanged(state, detail);
        }
    }

    @Override
    public synchronized void close() {
        closeResources();
    }

    private void closeResources() {
        if (!connected.getAndSet(false) && socket == null) {
            return;
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        } finally {
            socket = null;
            input = null;
            output = null;
        }
    }
}
