package Instagram;

import Logica.Decodificacion.Publicacion;
import Logica.Ventanas.InstaImages;
import Logica.Ventanas.InstaWindowLayout;

import Logica.Estructuras.ListaEnlazada;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import proyecto2_programacion2.GUISelector;

/** Editor integrado para texto, imagenes, stickers y carpetas personales. */
final class InstaPostComposer extends JPanel {
    static final int MAX_DESCRIPTION_LENGTH = Publicacion.MAX_TEXTO;
    static final int MAX_IMAGES_PER_POST = 20;
    private final String user;
    private final Runnable back;
    private final JTextArea description = new JTextArea(5, 25);
    private final JLabel status = new JLabel("Texto: 0/220");
    private final JPanel mediaPreview = new JPanel(new GridLayout(0, 3, 6, 6)) {
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Math.max(0, getPreferredSize().height)); }
    };
    private final JPanel stickersPanel = new JPanel(new GridLayout(0, 3, 6, 6)) {
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Math.max(0, getPreferredSize().height)); }
    };
    private final JComboBox<String> folders = new JComboBox<>();
    private final ListaEnlazada<File> images = new ListaEnlazada<>();
    private final ListaEnlazada<String> stickers = new ListaEnlazada<>();
    private final JButton publish = new JButton("Publicar");
    private final JPanel actions = new JPanel(new GridLayout(0, 2, 6, 6));
    private boolean publishing;

    static void open(Component parent, String user, Runnable afterPublish) {
        if (!(SwingUtilities.getWindowAncestor(parent) instanceof JFrame frame)) return;
        Container previous = frame.getContentPane();
        Runnable back = () -> { frame.setContentPane(previous); frame.revalidate(); frame.repaint(); };
        InstaPostComposer editor = new InstaPostComposer(user, back, () -> {
            back.run(); if (afterPublish != null) afterPublish.run();
        });
        frame.setContentPane(editor); frame.revalidate(); frame.repaint();
    }

    InstaPostComposer(String user, Runnable back, Runnable afterPublish) {
        this.user = user; this.back = back;
        putClientProperty("insta.manager", instaController.getInstance().getInsta(user));
        setLayout(new BorderLayout(10, 10)); setBackground(Color.BLACK);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(400, 650));
        JPanel header = new JPanel(new BorderLayout(12, 0));
        JButton cancel = new JButton("Volver"); cancel.addActionListener(e -> { if (!publishing) back.run(); });
        header.add(cancel, BorderLayout.WEST); header.add(new JLabel("Nueva publicación", SwingConstants.CENTER));
        add(header, BorderLayout.NORTH);
        JPanel content = new JPanel(); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.BLACK);
        description.setLineWrap(true); description.setWrapStyleWord(true);
        description.setBackground(new Color(30, 30, 30)); description.setForeground(Color.WHITE);
        description.setCaretColor(Color.WHITE); description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCount(); }
            public void removeUpdate(DocumentEvent e) { updateCount(); }
            public void changedUpdate(DocumentEvent e) { updateCount(); }
        });
        JScrollPane textScroll = new JScrollPane(description);
        textScroll.setPreferredSize(new Dimension(340, 140));
        textScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        content.add(textScroll); content.add(Box.createVerticalStrut(10));
        JButton photo = new JButton("Agregar imágenes"); photo.addActionListener(e -> selectImages()); actions.add(photo);
        JButton sticker = new JButton("Agregar sticker"); sticker.addActionListener(e -> loadStickers()); actions.add(sticker);
        JButton clear = new JButton("Quitar adjuntos"); clear.addActionListener(e -> {
            images.clear(); stickers.clear(); mediaPreview.removeAll(); mediaPreview.revalidate(); mediaPreview.repaint(); updateCount();
        }); actions.add(clear); actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        content.add(actions); content.add(Box.createVerticalStrut(10));
        mediaPreview.setBackground(Color.BLACK); content.add(mediaPreview);
        stickersPanel.setBackground(Color.BLACK); content.add(stickersPanel);
        JPanel folderRow = new JPanel(new BorderLayout(6, 6));
        folderRow.add(new JLabel("Carpeta de las imágenes:"), BorderLayout.NORTH);
        folders.addItem("Sin carpeta personal"); folderRow.add(folders, BorderLayout.CENTER);
        JButton manage = new JButton("Mis carpetas"); manage.addActionListener(e -> {
            if (SwingUtilities.getWindowAncestor(this) instanceof JFrame frame) {
                frame.setContentPane(new InstaFoldersUI(user, () -> { frame.setContentPane(this); loadFolders(); frame.revalidate(); }));
                frame.revalidate();
            }
        }); folderRow.add(manage, BorderLayout.SOUTH);
        folderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        content.add(Box.createVerticalStrut(10)); content.add(folderRow);
        content.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(content); scroll.setBorder(null); scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16); add(scroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(6, 6)); footer.add(status, BorderLayout.CENTER); footer.add(publish, BorderLayout.EAST);
        publish.addActionListener(e -> publish(afterPublish)); add(footer, BorderLayout.SOUTH);
        style(this);
        for (Component component : content.getComponents()) if (component instanceof JComponent jc) jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadFolders();
        InstaWindowLayout.install(this);
    }

    static void style(Component component) {
        if (component instanceof JPanel || component instanceof JViewport) component.setBackground(Color.BLACK);
        if (component instanceof JLabel) component.setForeground(Color.WHITE);
        if (component instanceof JButton button) {
            button.setBackground(new Color(220, 80, 0)); button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
        }
        if (component instanceof Container container) for (Component child : container.getComponents()) style(child);
    }

    private void updateCount() {
        int limit = MAX_DESCRIPTION_LENGTH;
        status.setText(Publicacion.textLength(description.getText()) + "/" + limit + " · #" + InstaSocialText.countHashtags(description.getText())
                + " · @" + InstaSocialText.countMentions(description.getText()));
        publish.setEnabled(!publishing && Publicacion.textLength(description.getText()) <= limit);
    }

    private void loadFolders() {
        new SwingWorker<ListaEnlazada<String>, Void>() {
            protected ListaEnlazada<String> doInBackground() throws Exception { return instaController.getInstance().getInsta(user).getPersonalFolders(user); }
            protected void done() {
                try { folders.removeAllItems(); folders.addItem("Sin carpeta personal"); for (String name : get()) folders.addItem(name); }
                catch (Exception ex) { status.setText("No se pudieron cargar las carpetas."); }
            }
        }.execute();
    }

    private void selectImages() {
        File[] selected = GUISelector.seleccionarArchivos(this, "Agregar imágenes", "png", "jpg", "jpeg");
        if (images.size() + stickers.size() + selected.length > MAX_IMAGES_PER_POST) { status.setText("Máximo 20 adjuntos."); return; }
        for (File file : selected) {
            images.add(file); mediaPreview.add(new JLabel(InstaImages.icon(this, file.getPath(), 105, 105, false)));
        }
        mediaPreview.revalidate(); updateCount();
    }

    private void loadStickers() {
        stickersPanel.removeAll();
        new SwingWorker<java.util.ArrayList<String[]>, Void>() {
            protected java.util.ArrayList<String[]> doInBackground() throws Exception { return instaController.getInstance().getInsta(user).getStickers(user); }
            protected void done() {
                try {
                    for (String[] item : get()) {
                        JButton button = new JButton(item[0], InstaImages.icon(InstaPostComposer.this, item[1], 60, 60, false));
                        button.setVerticalTextPosition(SwingConstants.BOTTOM); button.setHorizontalTextPosition(SwingConstants.CENTER);
                        button.addActionListener(e -> {
                            if (images.size() + stickers.size() >= 20) { status.setText("Máximo 20 adjuntos."); return; }
                            stickers.add(item[1]); mediaPreview.add(new JLabel(InstaImages.icon(InstaPostComposer.this, item[1], 105, 105, false)));
                            stickersPanel.removeAll(); mediaPreview.revalidate(); stickersPanel.revalidate(); repaint(); updateCount();
                        }); stickersPanel.add(button);
                    }
                    style(stickersPanel); stickersPanel.revalidate(); repaint();
                } catch (Exception ex) { status.setText("No se pudieron cargar los stickers."); }
            }
        }.execute();
    }

    private void publish(Runnable afterPublish) {
        String text = description.getText().trim();
        if (text.isBlank() && images.isEmpty() && stickers.isEmpty()) { status.setText("Escribe texto o agrega un adjunto."); return; }
        if (Publicacion.textLength(text) > MAX_DESCRIPTION_LENGTH) { updateCount(); return; }
        String folder = folders.getSelectedIndex() <= 0 ? "" : String.valueOf(folders.getSelectedItem());
        ListaEnlazada<File> selectedImages = new ListaEnlazada<>(images);
        ListaEnlazada<String> selectedStickers = new ListaEnlazada<>(stickers);
        publishing = true; publish.setEnabled(false); description.setEditable(false);
        for (Component component : actions.getComponents()) component.setEnabled(false);
        status.setText("Publicando...");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                instaManager manager = instaController.getInstance().getInsta(user);
                ListaEnlazada<String> references = new ListaEnlazada<>();
                for (File image : selectedImages) references.add(manager.uploadImage(user, image, folder));
                references.addAll(selectedStickers);
                manager.addPost(InstaPostMedia.encode(references), user, text); return null;
            }
            protected void done() {
                publishing = false; publish.setEnabled(true); description.setEditable(true);
                for (Component component : actions.getComponents()) component.setEnabled(true);
                try { get(); afterPublish.run(); }
                catch (Exception ex) { status.setText(ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()); }
            }
        }.execute();
    }
}
