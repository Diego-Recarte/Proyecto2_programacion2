package Instagram;

import Instagram.sockets.ChatClient;
import Instagram.sockets.ChatMessage;
import Instagram.sockets.ChatServer;
import Instagram.sockets.LocalChatServer;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Una conexión y avisos por sesión; sobreviven a los cambios de pantalla. */
final class InstaSession implements ChatClient.Listener, AutoCloseable {
    private static final String KEY = InstaSession.class.getName();
    private final JFrame frame;
    private final String user;
    private final List<ChatClient.Listener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService worker;
    private final Set<String> seenPosts = new HashSet<>();
    private final InstaToast toasts;
    private final WindowAdapter windowListener;
    private volatile ChatClient client;
    private volatile boolean closed;
    private volatile boolean replaced;
    private volatile int unread;
    private volatile Set<String> online = Set.of();
    private boolean history;
    private boolean baseline;

    static InstaSession find(Component component) {
        if (SwingUtilities.getWindowAncestor(component) instanceof JFrame frame) {
            return (InstaSession) frame.getRootPane().getClientProperty(KEY);
        }
        return null;
    }

    static InstaSession ensure(Component component, String user) {
        if (!(SwingUtilities.getWindowAncestor(component) instanceof JFrame frame)) return null;
        InstaSession session = find(component);
        if (session == null || session.closed || !session.user.equals(user)) {
            if (session != null) session.close();
            session = new InstaSession(frame, user);
            frame.getRootPane().putClientProperty(KEY, session);
        }
        return session;
    }

    private InstaSession(JFrame frame, String user) {
        this.frame = frame;
        this.user = user;
        toasts = new InstaToast(frame);
        windowListener = new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { close(); }
        };
        frame.addWindowListener(windowListener);
        worker = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "insta-session-" + user);
            thread.setDaemon(true);
            return thread;
        });
        worker.scheduleWithFixedDelay(this::refresh, 0, 2, TimeUnit.SECONDS);
    }

    ChatClient client() { return client; }
    int unreadCount() { return unread; }

    void addListener(ChatClient.Listener listener) {
        listeners.add(listener);
        listener.onUnreadCount(unread);
        listener.onOnlineUsers(online);
        listener.onConnectionChanged(client != null && client.isConnected(), "Conectando…");
    }

    void removeListener(ChatClient.Listener listener) { listeners.remove(listener); }

    private void refresh() {
        if (closed) return;
        instaManager account = instaController.getInstance().getInsta(user);
        if (account == null) return;
        try {
            if (!account.getStatusUser(user)) {
                if (client != null) client.close();
                SwingUtilities.invokeLater(() -> {
                    if (!closed && !(frame.getContentPane() instanceof InstaProfileEditUI)) navigate(new InstaProfileEditUI(user));
                });
                return;
            }
        } catch (Exception unavailable) { return; }
        if (!replaced && (client == null || !client.isConnected())) {
            ChatClient next = null;
            try {
                instaManager manager = instaController.getInstance().getInsta(user);
                next = new ChatClient(manager.serverHost(), manager.chatPort(), user, manager.sessionToken());
                next.addListener(this);
                client = next;
                history = false;
                next.connect();
                if (closed) next.close();
            } catch (Exception ex) {
                if (next != null) next.close();
                onConnectionChanged(false, "Sin conexión · Reintentando…");
            }
        }
        try {
            instaManager manager = instaController.getInstance().getInsta(user);
            if (manager == null) return;
            Set<String> activeUsers = new HashSet<>(manager.searchUsers(""));
            SwingUtilities.invokeLater(() -> {
                if (closed) return;
                if (frame.getContentPane() instanceof InstaProfileUI profile) profile.refreshActiveState(activeUsers);
                if (frame.getContentPane() instanceof VisibilidadProfileUI profile) profile.refreshActiveState(activeUsers);
                if (frame.getContentPane() instanceof InstaPostUI detail) detail.refreshActiveState(activeUsers);
                if (frame.getContentPane() instanceof InstaFeedUI feed) feed.refreshActiveState(activeUsers);
                if (frame.getContentPane() instanceof HashtagSearchUI search) search.refreshActiveState(activeUsers);
                if (frame.getContentPane() instanceof InteractionsUI mentions) mentions.refreshActiveState(activeUsers);
                if (frame.getContentPane() instanceof InstaEditProfileUI search) search.refreshActiveState(activeUsers);
            });
            List<String[]> posts = manager.getFeedPosts(user);
            List<String[]> added = new Logica.Estructuras.ListaEnlazada<>();
            for (String[] post : posts) {
                String owner = post.length > 4 ? post[4] : post[1];
                String id = owner + "\n" + post[0];
                if (seenPosts.add(id) && baseline && !user.equals(owner)) added.add(post);
            }
            baseline = true;
            if (!added.isEmpty()) {
                String owner = added.get(0).length > 4 ? added.get(0)[4] : added.get(0)[1];
                String title = added.size() == 1 ? "Nueva publicación de @" + owner
                        : added.size() + " nuevas publicaciones";
                SwingUtilities.invokeLater(() -> {
                    if (!closed) toasts.show(title, "Hay contenido nuevo en tu inicio.",
                            () -> navigate(new InstaFeedUI(user)));
                });
            }
        } catch (Exception ex) {
            // Una lectura fallida se reintenta sin borrar la referencia anterior.
        }
    }

    private void navigate(JPanel panel) {
        if (closed) return;
        frame.setState(Frame.NORMAL);
        frame.setContentPane(panel);
        frame.pack();
        frame.toFront();
        frame.requestFocus();
    }

    @Override public void onMessage(ChatMessage message) {
        boolean historical = history;
        SwingUtilities.invokeLater(() -> {
            if (closed || historical || !user.equals(message.getRecipient())) return;
            boolean reading = frame.isFocused() && frame.getContentPane() instanceof InstaChatUI chat
                    && chat.isReading(message.getSender());
            if (!reading) {
                String preview = switch (message.getType()) {
                    case IMAGE -> "Te envió una imagen";
                    case STICKER -> "Te envió un sticker";
                    default -> message.getContent();
                };
                toasts.show("Mensaje de @" + message.getSender(), preview,
                        () -> navigate(new InstaChatUI(user, message.getSender())));
            }
        });
        for (ChatClient.Listener listener : listeners) listener.onMessage(message);
    }

    @Override public void onHistoryStarted(String peer) {
        history = true;
        for (ChatClient.Listener listener : listeners) listener.onHistoryStarted(peer);
    }
    @Override public void onHistoryFinished(String peer) {
        history = false;
        for (ChatClient.Listener listener : listeners) listener.onHistoryFinished(peer);
    }
    @Override public void onUnreadCount(int count) {
        unread = Math.max(0, count);
        for (ChatClient.Listener listener : listeners) listener.onUnreadCount(unread);
        SwingUtilities.invokeLater(() -> { if (!closed) frame.repaint(); });
    }
    @Override public void onOnlineUsers(Set<String> users) {
        online = Set.copyOf(users);
        for (ChatClient.Listener listener : listeners) listener.onOnlineUsers(users);
    }
    @Override public void onConnectionChanged(boolean connected, String detail) {
        if (detail != null && detail.contains("otra ventana")) replaced = true;
        for (ChatClient.Listener listener : listeners) listener.onConnectionChanged(connected, detail);
    }
    @Override public void onConversationDeleted(String peer) {
        for (ChatClient.Listener listener : listeners) listener.onConversationDeleted(peer);
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        worker.shutdownNow();
        if (client != null) client.close();
        listeners.clear();
        toasts.close();
        frame.removeWindowListener(windowListener);
        frame.getRootPane().putClientProperty(KEY, null);
    }
}
