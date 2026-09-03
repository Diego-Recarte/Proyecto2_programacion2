package Instagram;

import Logica.Excepciones.ImageLoadException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Pantalla inicial de Instagram: publicaciones de los demás usuarios. */
public final class InstaFeedUI extends JPanel {

    private static final Color BACKGROUND = Color.BLACK;
    private static final Color SURFACE = new Color(20, 20, 20);
    private static final Color ACCENT = new Color(255, 69, 0);
    private static final Color TEXT = Color.WHITE;
    private static final long REFRESH_INTERVAL_SECONDS = 2L;

    private final String currentUser;
    private final JPanel feedPanel = new JPanel();
    private final JScrollPane feedScroll;
    private final AtomicBoolean refreshInProgress = new AtomicBoolean();
    private final AtomicLong refreshGeneration = new AtomicLong();
    private volatile ArrayList<String[]> feedPosts = new ArrayList<>();
    private volatile ScheduledExecutorService refreshExecutor;
    private volatile boolean feedRendered;

    public InstaFeedUI(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 650));
        setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);

        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(BACKGROUND);
        feedScroll = new JScrollPane(feedPanel);
        feedScroll.setBorder(null);
        feedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feedScroll.getVerticalScrollBar().setUnitIncrement(18);
        feedScroll.getViewport().setBackground(BACKGROUND);
        add(feedScroll, BorderLayout.CENTER);

        add(createNavigation(), BorderLayout.SOUTH);
        showLoadingState();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startRealtimeRefresh();
    }

    @Override
    public void removeNotify() {
        stopRealtimeRefresh();
        super.removeNotify();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(400, 58));
        header.setBackground(BACKGROUND);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 45)));

        JLabel title = new JLabel("  Instagram");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 8));
        actions.setOpaque(false);
        JButton people = iconButton("⌕", "Buscar perfiles");
        people.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 23));
        people.addActionListener(e -> show(new InstaEditProfileUI(currentUser)));
        JButton interactions = iconButton("♡", "Interacciones");
        interactions.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 25));
        interactions.addActionListener(e -> show(new InteractionsUI(currentUser)));
        JButton chat = iconButton("✉", "Mensajes");
        chat.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 23));
        chat.addActionListener(e -> show(new InstaChatUI(currentUser)));
        actions.add(people);
        actions.add(interactions);
        actions.add(chat);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void loadFeed() {
        requestRefresh(true);
    }

    private synchronized void startRealtimeRefresh() {
        if (refreshExecutor != null && !refreshExecutor.isShutdown()) {
            return;
        }
        long generation = refreshGeneration.incrementAndGet();
        refreshExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "instagram-feed-refresh-" + currentUser);
            thread.setDaemon(true);
            return thread;
        });
        refreshExecutor.scheduleWithFixedDelay(
                () -> refreshFromDisk(false, generation), 0, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private synchronized void stopRealtimeRefresh() {
        refreshGeneration.incrementAndGet();
        ScheduledExecutorService executor = refreshExecutor;
        refreshExecutor = null;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void requestRefresh(boolean forceRender) {
        ScheduledExecutorService executor = refreshExecutor;
        if (executor == null || executor.isShutdown()) {
            if (isDisplayable()) {
                startRealtimeRefresh();
            }
            return;
        }
        long generation = refreshGeneration.get();
        executor.execute(() -> refreshFromDisk(forceRender, generation));
    }

    private void refreshFromDisk(boolean forceRender, long generation) {
        if (!refreshInProgress.compareAndSet(false, true)) {
            return;
        }
        try {
            instaManager manager = instaController.getInstance().getInsta();
            ArrayList<String[]> latest = manager != null ? manager.getFeedPosts(currentUser) : new ArrayList<>();
            if (generation != refreshGeneration.get()) {
                return;
            }
            if (forceRender || !feedRendered || !samePosts(feedPosts, latest)) {
                SwingUtilities.invokeLater(() -> {
                    if (generation == refreshGeneration.get() && isDisplayable()) {
                        renderFeed(latest);
                    }
                });
            }
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                if (generation == refreshGeneration.get() && feedPosts.isEmpty() && isDisplayable()) {
                    showFeedError(ex.getMessage());
                }
            });
        } finally {
            refreshInProgress.set(false);
        }
    }

    private void renderFeed(ArrayList<String[]> latest) {
        int previousScroll = feedScroll.getVerticalScrollBar().getValue();
        feedPosts = new ArrayList<>(latest);
        feedRendered = true;
        feedPanel.removeAll();
        if (feedPosts.isEmpty()) {
            feedPanel.add(Box.createVerticalStrut(150));
            JLabel empty = new JLabel("<html><div style='text-align:center'>"
                    + "Aún no hay publicaciones en tu timeline.<br>"
                    + "Publica algo o sigue a otras personas.</div></html>", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedPanel.add(empty);
        } else {
            for (int index = 0; index < feedPosts.size(); index++) {
                JPanel card = createPostCard(feedPosts.get(index), index);
                JPanel wrapper = new JPanel(new GridBagLayout());
                wrapper.setBackground(BACKGROUND);
                wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
                wrapper.add(card);
                wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
                feedPanel.add(wrapper);
                feedPanel.add(Box.createVerticalStrut(8));
            }
        }
        feedPanel.revalidate();
        feedPanel.repaint();
        SwingUtilities.invokeLater(() -> feedScroll.getVerticalScrollBar().setValue(previousScroll));
    }

    private void showLoadingState() {
        feedPanel.removeAll();
        feedPanel.add(Box.createVerticalStrut(180));
        JLabel loading = new JLabel("Actualizando feed...", SwingConstants.CENTER);
        loading.setForeground(Color.GRAY);
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);
        feedPanel.add(loading);
    }

    private void showFeedError(String message) {
        feedPanel.removeAll();
        JLabel error = new JLabel("No se pudo cargar el feed: " + message, SwingConstants.CENTER);
        error.setForeground(ACCENT);
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        feedPanel.add(error);
        feedPanel.revalidate();
        feedPanel.repaint();
    }

    private boolean samePosts(ArrayList<String[]> current, ArrayList<String[]> latest) {
        if (current == latest) {
            return true;
        }
        if (current == null || latest == null || current.size() != latest.size()) {
            return false;
        }
        for (int row = 0; row < current.size(); row++) {
            String[] left = current.get(row);
            String[] right = latest.get(row);
            int maxLength = Math.max(left != null ? left.length : 0, right != null ? right.length : 0);
            for (int column = 0; column < maxLength; column++) {
                if (!value(left, column).equals(value(right, column))) {
                    return false;
                }
            }
        }
        return true;
    }

    private JPanel createPostCard(String[] post, int index) {
        String imagePath = value(post, 0);
        String author = value(post, 1);
        String date = value(post, 2);
        String caption = value(post, 3);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BACKGROUND);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 45)));

        JPanel userHeader = new JPanel(new BorderLayout());
        userHeader.setBackground(BACKGROUND);
        userHeader.setBorder(new EmptyBorder(8, 12, 8, 12));
        userHeader.setPreferredSize(new Dimension(380, 52));
        userHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        userHeader.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        identity.setOpaque(false);
        identity.add(new JLabel(createAvatar(author, 32)));
        JLabel username = new JLabel("@" + author);
        username.setForeground(TEXT);
        username.setFont(new Font("Segoe UI", Font.BOLD, 14));
        identity.add(username);
        userHeader.add(identity, BorderLayout.WEST);

        JLabel dateLabel = new JLabel(date);
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        userHeader.add(dateLabel, BorderLayout.EAST);
        userHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                show(new VisibilidadProfileUI(author, currentUser));
            }
        });
        card.add(userHeader);

        List<String> mediaPaths = InstaPostMedia.decode(imagePath);
        if (mediaPaths.size() > 1) {
            card.add(new InstaMediaCarousel(mediaPaths, 360, 360, () -> openPost(index)));
        } else {
            JLabel image = new JLabel("Imagen no disponible", SwingConstants.CENTER);
            image.setForeground(Color.GRAY);
            image.setAlignmentX(Component.CENTER_ALIGNMENT);
            int imageWidth = 360;
            int imageHeight = 240;
            try {
                String singlePath = mediaPaths.isEmpty() ? "" : mediaPaths.get(0);
                ImageIcon icon = fitFeedImage(singlePath, imageWidth, 450);
                image.setIcon(icon);
                image.setText("");
                imageWidth = icon.getIconWidth();
                imageHeight = icon.getIconHeight();
            } catch (ImageLoadException ignored) {
            }
            Dimension imageSize = new Dimension(imageWidth, imageHeight);
            image.setPreferredSize(imageSize);
            image.setMinimumSize(imageSize);
            image.setMaximumSize(imageSize);
            image.setCursor(new Cursor(Cursor.HAND_CURSOR));
            image.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openPost(index);
                }
            });
            card.add(image);
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(380, 42));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boolean initiallyLiked = false;
        int initialLikeCount = 0;
        try {
            instaManager manager = instaController.getInstance().getInsta();
            initiallyLiked = manager.hasLiked(author, imagePath, currentUser);
            initialLikeCount = manager.getLikeCount(author, imagePath);
        } catch (IOException ignored) {
        }
        JPanel likeGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        likeGroup.setOpaque(false);
        JLabel like = new JLabel(initiallyLiked ? "♥" : "♡");
        like.setForeground(initiallyLiked ? new Color(255, 48, 64) : TEXT);
        like.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 27));
        like.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel likeCount = new JLabel(String.valueOf(initialLikeCount));
        likeCount.setForeground(TEXT);
        likeCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        like.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    instaManager manager = instaController.getInstance().getInsta();
                    int count = manager.toggleLike(author, imagePath, currentUser);
                    boolean liked = manager.hasLiked(author, imagePath, currentUser);
                    like.setText(liked ? "♥" : "♡");
                    like.setForeground(liked ? new Color(255, 48, 64) : TEXT);
                    likeCount.setText(String.valueOf(count));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(InstaFeedUI.this, "No se pudo guardar el corazón.",
                            "Publicación", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        likeGroup.add(like);
        likeGroup.add(likeCount);
        JLabel comment = new JLabel("◯");
        comment.setForeground(TEXT);
        comment.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
        comment.setToolTipText("Ver comentarios");
        comment.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPost(index);
            }
        });
        actions.add(likeGroup);
        actions.add(comment);
        card.add(actions);

        if (!caption.isBlank()) {
            JEditorPane captionText = InstaSocialText.createCaption(author, caption, 350,
                    this::openHashtag, this::openMention);
            JPanel captionPanel = new JPanel(new BorderLayout());
            captionPanel.setBackground(BACKGROUND);
            captionPanel.setBorder(new EmptyBorder(0, 14, 12, 14));
            captionPanel.add(captionText, BorderLayout.CENTER);
            int captionHeight = Math.max(30, captionText.getPreferredSize().height + 12);
            captionPanel.setPreferredSize(new Dimension(380, captionHeight));
            captionPanel.setMaximumSize(new Dimension(380, captionHeight));
            card.add(captionPanel);
        }
        Dimension preferred = card.getPreferredSize();
        card.setPreferredSize(new Dimension(380, preferred.height));
        card.setMaximumSize(new Dimension(380, preferred.height));
        return card;
    }

    private JPanel createNavigation() {
        JPanel bar = new JPanel(new GridLayout(1, 5));
        bar.setPreferredSize(new Dimension(400, 58));
        bar.setBackground(SURFACE);
        bar.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, ACCENT));

        JButton home = navButton(InstaNavIcon.Type.HOME, "Inicio");
        home.setForeground(ACCENT);
        home.addActionListener(e -> loadFeed());
        JButton search = navButton(InstaNavIcon.Type.SEARCH, "Buscar hashtags");
        search.addActionListener(e -> show(new HashtagSearchUI(currentUser)));
        JButton add = navButton(InstaNavIcon.Type.ADD, "Nueva publicación");
        add.addActionListener(e -> composePost());
        JButton messages = navButton(InstaNavIcon.Type.MESSAGE, "Mensajes");
        messages.addActionListener(e -> show(new InstaChatUI(currentUser)));
        JButton profile = navButton(InstaNavIcon.Type.PROFILE, "Perfil");
        profile.addActionListener(e -> show(new InstaProfileUI(currentUser)));

        bar.add(home);
        bar.add(search);
        bar.add(add);
        bar.add(messages);
        bar.add(profile);
        return bar;
    }

    private void composePost() {
        InstaPostComposer.open(this, currentUser, this::loadFeed);
    }

    private void openHashtag(String hashtag) {
        if (hashtag != null && !hashtag.isBlank()) {
            show(new HashtagSearchUI(currentUser, hashtag));
        }
    }

    private void openMention(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String resolved = resolveUsername(username);
        if (resolved == null) {
            JOptionPane.showMessageDialog(this, "La mención @" + username
                    + " está guardada, pero ese usuario no existe o está desactivado.",
                    "Mención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        show(resolved.equalsIgnoreCase(currentUser)
                ? new InstaProfileUI(currentUser)
                : new VisibilidadProfileUI(resolved, currentUser));
    }

    private String resolveUsername(String username) {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager != null) {
                for (String candidate : manager.searchUsers(username)) {
                    if (candidate.equalsIgnoreCase(username)) {
                        return candidate;
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private void openPost(int index) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame frame)) {
            return;
        }
        Runnable back = () -> replace(frame, this);
        replace(frame, new InstaPostUI(currentUser, feedPosts, index, back));
    }

    private void show(JPanel panel) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            replace(frame, panel);
        }
    }

    private static void replace(JFrame frame, Component content) {
        frame.setContentPane((java.awt.Container) content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    private JButton navButton(InstaNavIcon.Type type, String tooltip) {
        JButton button = new JButton(new InstaNavIcon(type, 24));
        configureIconButton(button, tooltip);
        return button;
    }

    private JButton iconButton(String symbol, String tooltip) {
        JButton button = new JButton(symbol);
        configureIconButton(button, tooltip);
        return button;
    }

    private void configureIconButton(JButton button, String tooltip) {
        button.setToolTipText(tooltip);
        button.setForeground(TEXT);
        button.setBackground(SURFACE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private ImageIcon squareImage(String path, int size) throws ImageLoadException {
        try {
            BufferedImage source = ImageIO.read(new File(path));
            if (source == null) {
                throw new IOException("Formato no reconocido");
            }
            int square = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - square) / 2;
            int y = (source.getHeight() - square) / 2;
            BufferedImage crop = source.getSubimage(x, y, square, square);
            return new ImageIcon(crop.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (IOException ex) {
            throw new ImageLoadException("No se pudo leer " + path, ex);
        }
    }

    private ImageIcon fitFeedImage(String path, int maxWidth, int maxHeight) throws ImageLoadException {
        try {
            BufferedImage source = ImageIO.read(new File(path));
            if (source == null) {
                throw new IOException("Formato no reconocido");
            }
            double scale = Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight());
            int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
            return new ImageIcon(source.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (IOException ex) {
            throw new ImageLoadException("No se pudo leer " + path, ex);
        }
    }

    private ImageIcon createAvatar(String username, int size) {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            String profile = manager != null ? manager.getProfilePic(username) : null;
            if (profile != null && new File(profile).isFile()) {
                return squareImage(profile, size);
            }
        } catch (Exception ignored) {
        }

        BufferedImage avatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = avatar.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int hash = username.hashCode();
        Color color = new Color(80 + Math.abs(hash % 150), 50 + Math.abs((hash / 7) % 130), 40 + Math.abs((hash / 13) % 150));
        graphics.setColor(color);
        graphics.fill(new Ellipse2D.Double(0, 0, size, size));
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
        int x = (size - graphics.getFontMetrics().stringWidth(initial)) / 2;
        int y = (size - graphics.getFontMetrics().getHeight()) / 2 + graphics.getFontMetrics().getAscent();
        graphics.drawString(initial, x, y);
        graphics.dispose();
        return new ImageIcon(avatar);
    }

    private static String value(String[] values, int index) {
        return values != null && index < values.length && values[index] != null ? values[index] : "";
    }

}
