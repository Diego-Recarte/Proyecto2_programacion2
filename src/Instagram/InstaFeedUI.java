package Instagram;

import Logica.Ventanas.InstaImages;
import Logica.Ventanas.InstaWindowLayout;

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
import Logica.Estructuras.ListaEnlazada;
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
    private volatile ListaEnlazada<String[]> feedPosts = new ListaEnlazada<>();
    private volatile ScheduledExecutorService refreshExecutor;
    private volatile boolean feedRendered;
    private static final int PAGE_SIZE = 5;
    private int renderedPosts;
    private boolean MODO_MOBILE = true;
    private java.util.Iterator<String[]> feedIterator = feedPosts.iterator();
    private boolean appending;
    private final JLabel feedEnd = new JLabel("", SwingConstants.CENTER);
    private final JButton newPosts = new JButton("Nuevas publicaciones · Ver");
    private ListaEnlazada<String[]> pendingPosts;

    public InstaFeedUI(String currentUser) {
        this.currentUser = currentUser;
        putClientProperty("insta.manager", instaController.getInstance().getInsta(currentUser));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 650));
        setBackground(BACKGROUND);

        JPanel top = new JPanel(new BorderLayout());
        top.add(createHeader(), BorderLayout.NORTH);
        newPosts.setVisible(false);
        newPosts.addActionListener(e -> showPendingPosts());
        top.add(newPosts, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(BACKGROUND);
        feedScroll = new JScrollPane(feedPanel);
        feedScroll.setBorder(null);
        feedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feedScroll.getVerticalScrollBar().setUnitIncrement(18);
        feedScroll.getViewport().setBackground(BACKGROUND);
        feedEnd.setForeground(Color.GRAY);
        feedEnd.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedEnd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        feedScroll.getVerticalScrollBar().addAdjustmentListener(e -> appendNearEnd());
        add(feedScroll, BorderLayout.CENTER);

        add(createNavigation(), BorderLayout.SOUTH);
        showLoadingState();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent event) {
                boolean mobile = getWidth() < 650;
                if (mobile != MODO_MOBILE) {
                    MODO_MOBILE = mobile;
                    if (feedRendered) renderFeed(feedPosts);
                }
            }
        });
        InstaWindowLayout.install(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        InstaSession.ensure(this, currentUser);
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
        InstaMessageBadge.install(chat);
        chat.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 23));
        chat.addActionListener(e -> show(new InstaChatUI(currentUser)));
        JButton mode = iconButton("↔", "Cambiar vista móvil / escritorio");
        mode.addActionListener(e -> InstaWindowLayout.toggle(this));
        actions.add(mode);
        actions.add(people);
        actions.add(interactions);
        actions.add(chat);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void loadFeed() {
        requestRefresh(true);
    }

    private void showPendingPosts() {
        if (pendingPosts != null) {
            feedScroll.getVerticalScrollBar().setValue(0);
            renderFeed(pendingPosts);
        }
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
            instaManager manager = instaController.getInstance().getInsta(currentUser);
            ListaEnlazada<String[]> latest = manager != null ? manager.getFeedPosts(currentUser) : new ListaEnlazada<>();
            if (generation != refreshGeneration.get()) {
                return;
            }
            if (forceRender || !feedRendered || !samePosts(feedPosts, latest)) {
                SwingUtilities.invokeLater(() -> {
                    if (generation == refreshGeneration.get() && isDisplayable()) {
                        if (!forceRender && feedRendered && !feedPosts.isEmpty()
                                && feedScroll.getVerticalScrollBar().getValue() > 100) {
                            pendingPosts = latest;
                            newPosts.setVisible(true);
                        } else {
                            renderFeed(latest);
                        }
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

    void refreshActiveState(java.util.Set<String> activeUsers) {
        if (feedPosts.stream().anyMatch(post -> !activeUsers.contains(post[1]))) {
            ListaEnlazada<String[]> visible = new ListaEnlazada<>();
            for (String[] post : feedPosts) if (activeUsers.contains(post[1])) visible.add(post);
            renderFeed(visible);
        }
    }

    private void renderFeed(ListaEnlazada<String[]> latest) {
        int previousScroll = feedScroll.getVerticalScrollBar().getValue();
        int previousCount = renderedPosts;
        pendingPosts = null;
        newPosts.setVisible(false);
        feedPosts = new ListaEnlazada<>(latest);
        feedIterator = feedPosts.iterator();
        feedRendered = true;
        renderedPosts = 0;
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
            appendPosts(Math.max(PAGE_SIZE, previousScroll > 0 ? previousCount : PAGE_SIZE));
        }
        feedPanel.revalidate();
        feedPanel.repaint();
        SwingUtilities.invokeLater(() -> feedScroll.getVerticalScrollBar().setValue(previousScroll));
    }

    private void appendNearEnd() {
        javax.swing.JScrollBar bar = feedScroll.getVerticalScrollBar();
        if (feedRendered && !appending && renderedPosts < feedPosts.size()
                && bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - 450) {
            appendPosts(PAGE_SIZE);
        }
    }

    private void appendPosts(int count) {
        appending = true;
        feedPanel.remove(feedEnd);
        int end = Math.min(feedPosts.size(), renderedPosts + count);
        while (renderedPosts < end) {
            int index = renderedPosts++;
            JPanel card = createPostCard(feedIterator.next(), index);
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setBackground(BACKGROUND);
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(card);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
            feedPanel.add(wrapper);
            feedPanel.add(Box.createVerticalStrut(8));
        }
        feedEnd.setText(renderedPosts < feedPosts.size() ? "Desliza para ver más"
                : "Estás al día · No hay más publicaciones");
        feedPanel.add(feedEnd);
        feedPanel.revalidate();
        feedPanel.repaint();
        SwingUtilities.invokeLater(() -> appending = false);
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

    private boolean samePosts(ListaEnlazada<String[]> current, ListaEnlazada<String[]> latest) {
        if (current == latest) {
            return true;
        }
        if (current == null || latest == null || current.size() != latest.size()) {
            return false;
        }
        java.util.Iterator<String[]> rightPosts = latest.iterator();
        for (String[] left : current) {
            String[] right = rightPosts.next();
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
        int cardWidth = MODO_MOBILE ? 380 : 620;
        int mediaWidth = cardWidth - 20;
        String imagePath = value(post, 0);
        String author = value(post, 1);
        String date = value(post, 2);
        String caption = value(post, 3);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BACKGROUND);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(cardWidth, Integer.MAX_VALUE));
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 45)));

        JPanel userHeader = new JPanel(new BorderLayout());
        userHeader.setBackground(BACKGROUND);
        userHeader.setBorder(new EmptyBorder(8, 12, 8, 12));
        userHeader.setPreferredSize(new Dimension(cardWidth, 52));
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
            card.add(new InstaMediaCarousel(mediaPaths, mediaWidth, mediaWidth, () -> openPost(index)));
        } else if (!mediaPaths.isEmpty()) {
            JLabel image = new JLabel("Imagen no disponible", SwingConstants.CENTER);
            image.setForeground(Color.GRAY);
            image.setAlignmentX(Component.CENTER_ALIGNMENT);
            int imageWidth = mediaWidth;
            int imageHeight = 240;
            try {
                String singlePath = mediaPaths.isEmpty() ? "" : mediaPaths.get(0);
                ImageIcon icon = fitFeedImage(singlePath, imageWidth, 450);
                image.setIcon(icon);
                image.setText("");
                imageWidth = icon.getIconWidth();
                imageHeight = icon.getIconHeight();
            } catch (ImageLoadException ex) {
                image.setToolTipText(ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage());
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
        actions.setPreferredSize(new Dimension(cardWidth, 42));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boolean initiallyLiked = false;
        int initialLikeCount = 0;
        try {
            instaManager manager = instaController.getInstance().getInsta(currentUser);
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
                    instaManager manager = instaController.getInstance().getInsta(currentUser);
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
            captionPanel.setPreferredSize(new Dimension(cardWidth, captionHeight));
            captionPanel.setMaximumSize(new Dimension(cardWidth, captionHeight));
            card.add(captionPanel);
        }
        Dimension preferred = card.getPreferredSize();
        card.setPreferredSize(new Dimension(cardWidth, preferred.height));
        card.setMaximumSize(new Dimension(cardWidth, preferred.height));
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
        InstaMessageBadge.install(messages);
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
            instaManager manager = instaController.getInstance().getInsta(currentUser);
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
        return InstaImages.icon(this, path, size, size, true);
    }

    private ImageIcon fitFeedImage(String path, int maxWidth, int maxHeight) throws ImageLoadException {
        return InstaImages.icon(this, path, maxWidth, maxHeight, false);
    }

    private ImageIcon createAvatar(String username, int size) {
        return InstaImages.avatar(this, username, size);
    }

    private static String value(String[] values, int index) {
        return values != null && index < values.length && values[index] != null ? values[index] : "";
    }

}
