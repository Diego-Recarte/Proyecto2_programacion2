package Instagram.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Servidor multicliente del chat de Instagram. */
public final class ChatServer implements AutoCloseable {

    public static final int DEFAULT_PORT = 5050;

    private final int port;
    private final ChatHistoryStore historyStore;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService clientPool = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "instagram-chat-client");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private Thread acceptThread;

    public ChatServer(int port) throws IOException {
        this(port, Path.of("Instagram", "users"));
    }

    public ChatServer(int port, Path historyDirectory) throws IOException {
        this.port = port;
        this.historyStore = new ChatHistoryStore(historyDirectory);
    }

    public synchronized void start() throws IOException {
        if (running.get()) {
            return;
        }
        serverSocket = new ServerSocket(port);
        running.set(true);
        acceptThread = new Thread(this::acceptLoop, "instagram-chat-server");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public boolean isRunning() {
        return running.get();
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                clientPool.submit(new ClientHandler(socket));
            } catch (IOException ex) {
                if (running.get()) {
                    System.err.println("Error aceptando cliente de chat: " + ex.getMessage());
                }
            }
        }
    }

    private void register(String username, ClientHandler handler) throws IOException {
        ClientHandler previous = clients.put(username, handler);
        if (previous != null && previous != handler) {
            previous.send(SocketProtocol.error("Tu sesión se abrió en otra ventana."));
            previous.close();
        }
        handler.send(SocketProtocol.unread(historyStore.unreadCount(username)));
        broadcastUsers();
    }

    private void remove(ClientHandler handler) {
        if (handler.username != null) {
            clients.remove(handler.username, handler);
            broadcastUsers();
        }
    }

    private void broadcastUsers() {
        String line = SocketProtocol.users(new ArrayList<>(clients.keySet()));
        for (ClientHandler client : clients.values()) {
            client.send(line);
        }
    }

    private void route(ChatMessage message) throws IOException {
        historyStore.append(message);
        ClientHandler sender = clients.get(message.getSender());
        ClientHandler recipient = clients.get(message.getRecipient());
        String line = SocketProtocol.message(SocketProtocol.MESSAGE, message);
        if (sender != null) {
            sender.send(line);
        }
        if (recipient != null && recipient != sender) {
            recipient.send(line);
            recipient.send(SocketProtocol.unread(historyStore.unreadCount(message.getRecipient())));
        }
    }

    @Override
    public synchronized void close() {
        running.set(false);
        for (ClientHandler handler : clients.values()) {
            handler.close();
        }
        clients.clear();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
        clientPool.shutdownNow();
    }

    private final class ClientHandler implements Runnable {

        private final Socket socket;
        private final Object outputLock = new Object();
        private BufferedReader input;
        private PrintWriter output;
        private String username;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (socket) {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                output = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

                String firstLine = input.readLine();
                if (firstLine == null || !SocketProtocol.HELLO.equals(SocketProtocol.command(firstLine))) {
                    send(SocketProtocol.error("Debes identificarte antes de usar el chat."));
                    return;
                }

                username = SocketProtocol.argument(firstLine).trim();
                if (username.isEmpty()) {
                    send(SocketProtocol.error("Usuario inválido."));
                    return;
                }
                register(username, this);

                String line;
                while ((line = input.readLine()) != null) {
                    handle(line);
                }
            } catch (IOException | RuntimeException ex) {
                if (running.get()) {
                    System.err.println("Cliente de chat desconectado: " + ex.getMessage());
                }
            } finally {
                remove(this);
            }
        }

        private void handle(String line) throws IOException {
            String command = SocketProtocol.command(line);
            switch (command) {
                case SocketProtocol.SEND -> {
                    ChatMessage message = SocketProtocol.parseMessage(line, SocketProtocol.SEND);
                    if (!username.equals(message.getSender())) {
                        send(SocketProtocol.error("El remitente no coincide con la sesión."));
                        return;
                    }
                    if (message.getType() == ChatMessage.Type.TEXT && message.getContent().length() > 300) {
                        send(SocketProtocol.error("El mensaje no puede superar 300 caracteres."));
                        return;
                    }
                    route(message);
                }
                case SocketProtocol.HISTORY -> sendHistory(SocketProtocol.argument(line));
                case SocketProtocol.MARK_READ -> markRead(SocketProtocol.argument(line));
                case SocketProtocol.DELETE_CONVERSATION -> deleteConversation(SocketProtocol.argument(line));
                case SocketProtocol.PING -> send(SocketProtocol.PONG);
                default -> send(SocketProtocol.error("Comando no reconocido."));
            }
        }

        private void sendHistory(String peer) throws IOException {
            send(SocketProtocol.withArgument(SocketProtocol.HISTORY_START, peer));
            for (ChatMessage message : historyStore.between(username, peer)) {
                send(SocketProtocol.message(SocketProtocol.MESSAGE, message));
            }
            send(SocketProtocol.withArgument(SocketProtocol.HISTORY_END, peer));
            markRead(peer);
        }

        private void markRead(String peer) throws IOException {
            historyStore.markRead(username, peer);
            send(SocketProtocol.unread(historyStore.unreadCount(username)));
        }

        private void deleteConversation(String peer) throws IOException {
            historyStore.deleteConversation(username, peer);
            send(SocketProtocol.withArgument(SocketProtocol.CONVERSATION_DELETED, peer));
            send(SocketProtocol.unread(historyStore.unreadCount(username)));
        }

        private void send(String line) {
            synchronized (outputLock) {
                if (output != null) {
                    output.println(line);
                }
            }
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
