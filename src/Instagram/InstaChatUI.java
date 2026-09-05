package Instagram;

import Instagram.sockets.ChatClient;
import Instagram.sockets.ChatMessage;
import Instagram.sockets.ChatServer;
import Instagram.sockets.LocalChatServer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Bandeja y conversación privada conectada por sockets. */
public final class InstaChatUI extends JPanel implements ChatClient.Listener {

    private static final Color BACKGROUND = Color.BLACK;
    private static final Color SURFACE = new Color(22, 22, 22);
    private static final Color FIELD = new Color(35, 35, 35);
    private static final Color ACCENT = new Color(255, 69, 0);
    private static final int MAX_IMAGE_BYTES = 3 * 1024 * 1024;

    private final String currentUser;
    private final DefaultListModel<Contact> contactModel = new DefaultListModel<>();
    private final JList<Contact> contactList = new JList<>(contactModel);
    private final DefaultListModel<ChatMessage> messageModel = new DefaultListModel<>();
    private final JList<ChatMessage> messageList = new JList<>(messageModel);
    private final JTextField messageField = new JTextField();
    private final JLabel titleLabel = new JLabel("Mensajes", SwingConstants.CENTER);
    private final JLabel connectionLabel = new JLabel("Conectando…", SwingConstants.CENTER);
    private final JButton sendButton = new JButton("Enviar");
    private final JButton deleteButton = new JButton("×");
    private final java.awt.CardLayout cards = new java.awt.CardLayout();
    private final JPanel center = new JPanel(cards);
    private final Set<String> onlineUsers = new HashSet<>();
    private final Set<String> visibleMessageIds = new HashSet<>();

    private ChatClient client;
    private String currentPeer;
    private boolean closing;
    private int unreadCount;

    public InstaChatUI(String currentUser) {
        this(currentUser, null);
    }

    public InstaChatUI(String currentUser, String peer) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 650));
        setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        center.setBackground(BACKGROUND);
        center.add(createContactList(), "CONTACTS");
        center.add(createConversation(), "CONVERSATION");
        add(center, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        loadContacts();
        if (peer != null && !peer.isBlank()) {
            openConversation(peer);
        } else {
            cards.show(center, "CONTACTS");
        }
        connectInBackground();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(400, 58));
        header.setBackground(BACKGROUND);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        JLabel back = new JLabel("  ‹");
        back.setPreferredSize(new Dimension(55, 58));
        back.setForeground(ACCENT);
        back.setFont(new Font("Segoe UI", Font.BOLD, 30));
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentPeer != null) {
                    currentPeer = null;
                    updateTitle();
                    deleteButton.setVisible(false);
                    cards.show(center, "CONTACTS");
                } else {
                    goToFeed();
                }
            }
        });
        header.add(back, BorderLayout.WEST);

        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(titleLabel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(90, 58));

        deleteButton.setToolTipText("Eliminar esta conversación");
        deleteButton.setForeground(new Color(235, 90, 90));
        deleteButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setPreferredSize(new Dimension(38, 58));
        deleteButton.setVisible(false);
        deleteButton.addActionListener(e -> deleteCurrentConversation());
        actions.add(deleteButton);

        JButton profile = new JButton();
        profile.setToolTipText("Mi perfil");
        profile.setIcon(avatarFor(currentUser, 30));
        profile.setBorderPainted(false);
        profile.setContentAreaFilled(false);
        profile.setFocusPainted(false);
        profile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profile.setPreferredSize(new Dimension(48, 58));
        profile.addActionListener(e -> showPanel(new InstaProfileUI(currentUser)));
        actions.add(profile);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createContactList() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        JLabel hint = new JLabel("  Selecciona un usuario para conversar");
        hint.setForeground(Color.LIGHT_GRAY);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setBorder(new EmptyBorder(8, 6, 8, 6));
        panel.add(hint, BorderLayout.NORTH);

        contactList.setBackground(BACKGROUND);
        contactList.setForeground(Color.WHITE);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setFixedCellHeight(62);
        contactList.setCellRenderer(new ContactRenderer());
        contactList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    Contact contact = contactList.getSelectedValue();
                    if (contact != null) {
                        openConversation(contact.username);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(contactList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConversation() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        messageList.setBackground(BACKGROUND);
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setCellRenderer(new MessageRenderer());
        messageList.setBorder(new EmptyBorder(8, 5, 8, 5));
        JScrollPane messagesScroll = new JScrollPane(messageList);
        messagesScroll.setBorder(null);
        messagesScroll.getVerticalScrollBar().setUnitIncrement(16);
        messagesScroll.getViewport().setBackground(BACKGROUND);
        panel.add(messagesScroll, BorderLayout.CENTER);

        JPanel composer = new JPanel(new BorderLayout(5, 5));
        composer.setBackground(SURFACE);
        composer.setBorder(new EmptyBorder(7, 7, 7, 7));

        JPanel mediaButtons = new JPanel(new GridLayout(1, 3, 2, 0));
        mediaButtons.setOpaque(false);
        JButton emoji = smallButton("☺", "Emojis");
        emoji.addActionListener(e -> showEmojiMenu(emoji));
        JButton sticker = smallButton("★", "Stickers");
        sticker.addActionListener(e -> showStickerMenu(sticker));
        JButton image = smallButton("▧", "Enviar imagen");
        image.addActionListener(e -> chooseImage());
        mediaButtons.add(emoji);
        mediaButtons.add(sticker);
        mediaButtons.add(image);

        messageField.setBackground(FIELD);
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
        messageField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 65, 65)),
                new EmptyBorder(7, 9, 7, 9)));
        messageField.addActionListener(e -> sendText());
        ((AbstractDocument) messageField.getDocument()).setDocumentFilter(new MaxLengthFilter(300));

        sendButton.setBackground(ACCENT);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendText());

        composer.add(mediaButtons, BorderLayout.WEST);
        composer.add(messageField, BorderLayout.CENTER);
        composer.add(sendButton, BorderLayout.EAST);
        panel.add(composer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setPreferredSize(new Dimension(400, 24));
        status.setBackground(SURFACE);
        connectionLabel.setForeground(Color.GRAY);
        connectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        status.add(connectionLabel, BorderLayout.CENTER);
        return status;
    }

    private void loadContacts() {
        contactModel.clear();
        try {
            instaManager manager = instaController.getInstance().getInsta();
            ArrayList<String> users = manager != null ? manager.searchUsers("") : new ArrayList<>();
            users.stream()
                    .filter(user -> !user.equalsIgnoreCase(currentUser))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(user -> contactModel.addElement(new Contact(user, onlineUsers.contains(user))));
            if (contactModel.isEmpty()) {
                contactList.setEnabled(false);
            }
        } catch (IOException ex) {
            connectionLabel.setText("No se pudieron cargar los usuarios");
        }
    }

    private void connectInBackground() {
        String host = System.getProperty("instagram.chat.host", "127.0.0.1");
        int port = Integer.getInteger("instagram.chat.port", ChatServer.DEFAULT_PORT);
        new SwingWorker<ChatClient, Void>() {
            @Override
            protected ChatClient doInBackground() throws Exception {
                LocalChatServer.ensureAvailable(host, port);
                ChatClient connectedClient = new ChatClient(host, port, currentUser);
                connectedClient.addListener(InstaChatUI.this);
                connectedClient.connect();
                return connectedClient;
            }

            @Override
            protected void done() {
                try {
                    ChatClient connectedClient = get();
                    if (closing) {
                        connectedClient.close();
                        return;
                    }
                    client = connectedClient;
                    sendButton.setEnabled(true);
                    if (currentPeer != null) {
                        client.requestHistory(currentPeer);
                    }
                } catch (Exception ex) {
                    connectionLabel.setForeground(ACCENT);
                    connectionLabel.setText("Sin conexión: " + rootMessage(ex));
                    sendButton.setEnabled(false);
                }
            }
        }.execute();
    }

    private void openConversation(String peer) {
        currentPeer = peer;
        updateTitle();
        deleteButton.setVisible(true);
        messageModel.clear();
        visibleMessageIds.clear();
        cards.show(center, "CONVERSATION");
        messageField.requestFocusInWindow();
        if (client != null && client.isConnected()) {
            try {
                client.requestHistory(peer);
            } catch (IOException ex) {
                connectionLabel.setText(ex.getMessage());
            }
        }
    }

    private void sendText() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        if (text.length() > 300) {
            JOptionPane.showMessageDialog(this, "El mensaje no puede superar 300 caracteres.",
                    "Mensaje demasiado largo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ChatMessage.Type type = text.codePointCount(0, text.length()) <= 3
                && text.codePoints().anyMatch(codePoint -> codePoint > 0x2600)
                ? ChatMessage.Type.EMOJI : ChatMessage.Type.TEXT;
        if (send(type, text)) {
            messageField.setText("");
        }
    }

    private boolean send(ChatMessage.Type type, String content) {
        if (currentPeer == null || client == null || !client.isConnected()) {
            JOptionPane.showMessageDialog(this, "El chat aún no está conectado.", "Chat", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            client.send(new ChatMessage(currentUser, currentPeer, type, content));
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo enviar: " + ex.getMessage(), "Chat", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showEmojiMenu(Component source) {
        JPopupMenu menu = new JPopupMenu();
        menu.setLayout(new GridLayout(2, 5));
        String[] emojis = {"😀", "😂", "😍", "😎", "🥳", "❤️", "👍", "🔥", "👏", "🎉"};
        for (String value : emojis) {
            JButton button = new JButton(value);
            button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            button.addActionListener(e -> messageField.setText(messageField.getText() + value));
            menu.add(button);
        }
        menu.show(source, 0, -menu.getPreferredSize().height);
    }

    private void showStickerMenu(Component source) {
        JPopupMenu menu = new JPopupMenu();
        menu.setLayout(new GridLayout(0, 3, 4, 4));
        try {
            instaManager manager = instaController.getInstance().getInsta();
            for (String[] sticker : manager.getStickers(currentUser)) {
                JButton button = new JButton(stickerIcon(sticker[1], 54));
                button.setToolTipText(sticker[0]);
                button.setPreferredSize(new Dimension(68, 68));
                button.addActionListener(e -> sendSticker(new File(sticker[1])));
                menu.add(button);
            }
        } catch (IOException ex) {
            connectionLabel.setText("No se pudieron cargar los stickers");
        }

        JButton importButton = new JButton("+");
        importButton.setToolTipText("Importar sticker personal PNG o JPG");
        importButton.setFont(new Font("Segoe UI", Font.BOLD, 28));
        importButton.addActionListener(e -> importSticker(source));
        menu.add(importButton);
        menu.show(source, 0, -menu.getPreferredSize().height);
    }

    private void sendSticker(File file) {
        if (!file.isFile() || file.length() > MAX_IMAGE_BYTES) {
            JOptionPane.showMessageDialog(this, "El sticker no está disponible o supera 3 MB.",
                    "Sticker", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String payload = file.getName() + "\n"
                    + Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            send(ChatMessage.Type.STICKER, payload);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el sticker: " + ex.getMessage(),
                    "Sticker", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importSticker(Component source) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Importar sticker personal");
        chooser.setFileFilter(new FileNameExtensionFilter("Stickers PNG o JPG", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            instaController.getInstance().getInsta().importSticker(currentUser, chooser.getSelectedFile());
            showStickerMenu(source);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo importar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon stickerIcon(String path, int size) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image != null) {
                return new ImageIcon(image.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
        } catch (IOException ignored) {
        }
        return new ImageIcon();
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enviar imagen por chat");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif", "bmp"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.length() > MAX_IMAGE_BYTES) {
            JOptionPane.showMessageDialog(this, "La imagen no puede superar 3 MB.", "Imagen demasiado grande", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String payload = file.getName() + "\n" + Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            send(ChatMessage.Type.IMAGE, payload);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton smallButton(String symbol, String tooltip) {
        JButton button = new JButton(symbol);
        button.setToolTipText(tooltip);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    @Override
    public void onMessage(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (currentPeer != null && isCurrentConversation(message) && visibleMessageIds.add(message.getId())) {
                messageModel.addElement(message);
                messageList.ensureIndexIsVisible(messageModel.size() - 1);
                if (currentUser.equals(message.getRecipient()) && client != null) {
                    try {
                        client.markRead(currentPeer);
                    } catch (IOException ignored) {
                    }
                }
            } else if (currentUser.equals(message.getRecipient())) {
                connectionLabel.setForeground(ACCENT);
                connectionLabel.setText("Nuevo mensaje de @" + message.getSender());
            }
        });
    }

    @Override
    public void onOnlineUsers(Set<String> users) {
        SwingUtilities.invokeLater(() -> {
            onlineUsers.clear();
            onlineUsers.addAll(users);
            loadContacts();
            if (currentPeer != null) {
                connectionLabel.setText(onlineUsers.contains(currentPeer) ? "En línea" : "Desconectado · los mensajes quedarán guardados");
                connectionLabel.setForeground(onlineUsers.contains(currentPeer) ? new Color(70, 200, 100) : Color.GRAY);
            }
        });
    }

    @Override
    public void onHistoryStarted(String peer) {
        SwingUtilities.invokeLater(() -> {
            if (peer.equals(currentPeer)) {
                messageModel.clear();
                visibleMessageIds.clear();
            }
        });
    }

    @Override
    public void onHistoryFinished(String peer) {
        SwingUtilities.invokeLater(() -> {
            if (peer.equals(currentPeer) && !messageModel.isEmpty()) {
                messageList.ensureIndexIsVisible(messageModel.size() - 1);
            }
        });
    }

    @Override
    public void onConnectionChanged(boolean connected, String detail) {
        SwingUtilities.invokeLater(() -> {
            connectionLabel.setText(connected ? "Conectado al chat" : detail);
            connectionLabel.setForeground(connected ? new Color(70, 200, 100) : ACCENT);
            sendButton.setEnabled(connected);
        });
    }

    @Override
    public void onUnreadCount(int count) {
        SwingUtilities.invokeLater(() -> {
            unreadCount = Math.max(0, count);
            updateTitle();
        });
    }

    @Override
    public void onConversationDeleted(String peer) {
        SwingUtilities.invokeLater(() -> {
            if (peer.equals(currentPeer)) {
                messageModel.clear();
                visibleMessageIds.clear();
                connectionLabel.setText("Conversación eliminada de tu bandeja");
            }
        });
    }

    private void updateTitle() {
        if (currentPeer != null) {
            titleLabel.setText("@" + currentPeer);
        } else {
            titleLabel.setText(unreadCount > 0 ? "Mensajes (" + unreadCount + ")" : "Mensajes");
        }
    }

    private void deleteCurrentConversation() {
        if (currentPeer == null || client == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "¿Eliminar tu historial con @" + currentPeer + "?\nLa copia del otro usuario no se borrará.",
                "Eliminar conversación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                client.deleteConversation(currentPeer);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isCurrentConversation(ChatMessage message) {
        return (currentUser.equals(message.getSender()) && currentPeer.equals(message.getRecipient()))
                || (currentPeer.equals(message.getSender()) && currentUser.equals(message.getRecipient()));
    }

    private void goToFeed() {
        showPanel(new InstaFeedUI(currentUser));
    }

    private void showPanel(JPanel panel) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            frame.setContentPane(panel);
            frame.pack();
            frame.revalidate();
            frame.repaint();
        }
    }

    @Override
    public void removeNotify() {
        closing = true;
        if (client != null) {
            client.close();
        }
        super.removeNotify();
    }

    private ImageIcon avatarFor(String username, int size) {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            String path = manager != null ? manager.getProfilePic(username) : null;
            if (path != null && new File(path).isFile()) {
                BufferedImage source = ImageIO.read(new File(path));
                if (source != null) {
                    int square = Math.min(source.getWidth(), source.getHeight());
                    BufferedImage crop = source.getSubimage((source.getWidth() - square) / 2, (source.getHeight() - square) / 2, square, square);
                    return new ImageIcon(crop.getScaledInstance(size, size, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignored) {
        }
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int hash = username.hashCode();
        graphics.setColor(new Color(70 + Math.abs(hash % 160), 50 + Math.abs((hash / 5) % 140), 50 + Math.abs((hash / 11) % 140)));
        graphics.fill(new Ellipse2D.Double(0, 0, size, size));
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
        int x = (size - graphics.getFontMetrics().stringWidth(initial)) / 2;
        int y = (size - graphics.getFontMetrics().getHeight()) / 2 + graphics.getFontMetrics().getAscent();
        graphics.drawString(initial, x, y);
        graphics.dispose();
        return new ImageIcon(image);
    }

    private static String rootMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : "error de conexión";
    }

    private static String displayText(ChatMessage message) {
        if (message.getType() == ChatMessage.Type.IMAGE) {
            int separator = message.getContent().indexOf('\n');
            return separator >= 0 ? message.getContent().substring(0, separator) : "Imagen";
        }
        return message.getContent();
    }

    private static String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;").replace("\n", "<br>");
    }

    private final class ContactRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            Contact contact = (Contact) value;
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(selected ? new Color(45, 45, 45) : BACKGROUND);
            row.setBorder(new EmptyBorder(8, 12, 8, 12));
            row.add(new JLabel(avatarFor(contact.username, 42)), BorderLayout.WEST);

            JLabel name = new JLabel("<html><b style='color:white'>@" + html(contact.username) + "</b><br>"
                    + "<span style='color:" + (contact.online ? "#55cc77" : "#888888") + "'>"
                    + (contact.online ? "● En línea" : "○ Desconectado") + "</span></html>");
            name.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            row.add(name, BorderLayout.CENTER);
            return row;
        }
    }

    private final class MessageRenderer implements ListCellRenderer<ChatMessage> {
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        @Override
        public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message,
                int index, boolean selected, boolean focus) {
            boolean mine = currentUser.equals(message.getSender());
            JPanel row = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 3));
            row.setBackground(BACKGROUND);
            row.setComponentOrientation(mine ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

            JPanel bubble = new JPanel();
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
            bubble.setBackground(mine ? new Color(165, 50, 15) : FIELD);
            bubble.setBorder(new EmptyBorder(7, 10, 5, 10));

            if (message.getType() == ChatMessage.Type.IMAGE
                    || (message.getType() == ChatMessage.Type.STICKER && message.getContent().contains("\n"))) {
                ImageIcon image = decodeImage(message.getContent());
                JLabel label = new JLabel(image != null ? image : new ImageIcon());
                if (image == null) {
                    label.setText("Imagen no disponible");
                    label.setForeground(Color.LIGHT_GRAY);
                }
                bubble.add(label);
            } else {
                JLabel body = new JLabel("<html><div style='width:210px'>" + html(displayText(message)) + "</div></html>");
                body.setForeground(Color.WHITE);
                body.setFont(new Font("Segoe UI Emoji", Font.PLAIN,
                        message.getType() == ChatMessage.Type.STICKER ? 38 : 14));
                bubble.add(body);
            }

            JLabel time = new JLabel(timeFormat.format(new Date(message.getTimestamp())));
            time.setForeground(new Color(205, 205, 205));
            time.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            time.setAlignmentX(mine ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            bubble.add(Box.createVerticalStrut(2));
            bubble.add(time);
            row.add(bubble);
            return row;
        }

        private ImageIcon decodeImage(String payload) {
            try {
                int separator = payload.indexOf('\n');
                String encoded = separator >= 0 ? payload.substring(separator + 1) : payload;
                byte[] bytes = Base64.getDecoder().decode(encoded.getBytes(StandardCharsets.US_ASCII));
                BufferedImage source = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
                if (source == null) {
                    return null;
                }
                double scale = Math.min(180d / source.getWidth(), 180d / source.getHeight());
                scale = Math.min(1d, scale);
                int width = Math.max(1, (int) (source.getWidth() * scale));
                int height = Math.max(1, (int) (source.getHeight() * scale));
                return new ImageIcon(source.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static final class Contact {
        private final String username;
        private final boolean online;

        private Contact(String username, boolean online) {
            this.username = username;
            this.online = online;
        }
    }

    private static final class MaxLengthFilter extends DocumentFilter {
        private final int maxLength;

        private MaxLengthFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass bypass, int offset, String text, AttributeSet attributes)
                throws BadLocationException {
            if (text != null && bypass.getDocument().getLength() + text.length() <= maxLength) {
                super.insertString(bypass, offset, text, attributes);
            }
        }

        @Override
        public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                throws BadLocationException {
            int replacementLength = text == null ? 0 : text.length();
            if (bypass.getDocument().getLength() - length + replacementLength <= maxLength) {
                super.replace(bypass, offset, length, text, attributes);
            }
        }
    }
}
