package Instagram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Highlighter;

/** Flujo común para publicar una imagen con descripción, hashtags y menciones. */
final class InstaPostComposer {

    static final int MAX_DESCRIPTION_LENGTH = 220;
    static final int MAX_IMAGES_PER_POST = 20;
    private static final Highlighter.HighlightPainter HASHTAG_PAINTER
            = new DefaultHighlighter.DefaultHighlightPainter(new Color(110, 48, 25));
    private static final Highlighter.HighlightPainter MENTION_PAINTER
            = new DefaultHighlighter.DefaultHighlightPainter(new Color(20, 70, 110));

    private InstaPostComposer() {
    }

    static void open(Component parent, String username, Runnable afterPublish) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona una o varias imágenes");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imágenes", "jpg", "jpeg", "png", "gif", "bmp", "webp"));
        chooser.setAccessory(new JLabel("<html><div style='width:150px'>"
                + "Usa Ctrl o Shift para seleccionar varias imágenes para el mismo post."
                + "</div></html>"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] selectedImages = chooser.getSelectedFiles();
        if (selectedImages == null || selectedImages.length == 0) {
            File selected = chooser.getSelectedFile();
            selectedImages = selected != null ? new File[]{selected} : new File[0];
        }
        if (selectedImages.length == 0) {
            JOptionPane.showMessageDialog(parent, "Selecciona al menos una imagen.",
                    "Nueva publicación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedImages.length > MAX_IMAGES_PER_POST) {
            JOptionPane.showMessageDialog(parent, "Puedes agregar hasta " + MAX_IMAGES_PER_POST
                    + " imágenes en una publicación.", "Nueva publicación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (File selectedImage : selectedImages) {
            if (selectedImage == null || !selectedImage.isFile()) {
                JOptionPane.showMessageDialog(parent, "Una de las imágenes seleccionadas no es válida.",
                        "Nueva publicación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        File userDirectory = new File(new File("Instagram", "users"), username);
        File imagesDirectory = new File(userDirectory, "imagenes");

        JTextPane description = new JTextPane();
        description.setBackground(new Color(30, 30, 30));
        description.setForeground(Color.WHITE);
        description.setCaretColor(Color.WHITE);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        ((AbstractDocument) description.getDocument()).setDocumentFilter(new LengthFilter(MAX_DESCRIPTION_LENGTH));

        JLabel recognitionStatus = new JLabel();
        recognitionStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        installSocialHighlighting(description, recognitionStatus);

        List<String> previewPaths = Arrays.stream(selectedImages)
                .map(File::getAbsolutePath).toList();
        InstaMediaCarousel previewCarousel = new InstaMediaCarousel(previewPaths, 360, 230, null);
        JPanel previewPanel = new JPanel(new BorderLayout(0, 4));
        previewPanel.add(new JLabel(selectedImages.length == 1
                ? "Vista previa de la imagen"
                : "Vista previa · " + selectedImages.length + " imágenes (usa las flechas)"), BorderLayout.NORTH);
        previewPanel.add(previewCarousel, BorderLayout.CENTER);

        JPanel form = new JPanel(new BorderLayout(0, 10));
        JScrollPane descriptionScroll = new JScrollPane(description);
        descriptionScroll.setPreferredSize(new Dimension(360, 150));
        JPanel descriptionPanel = new JPanel(new BorderLayout(4, 4));
        JPanel descriptionHeader = new JPanel(new BorderLayout());
        descriptionHeader.add(new JLabel("Descripción:"), BorderLayout.WEST);
        descriptionHeader.add(recognitionStatus, BorderLayout.EAST);
        descriptionPanel.add(descriptionHeader, BorderLayout.NORTH);
        descriptionPanel.add(descriptionScroll, BorderLayout.CENTER);
        descriptionPanel.add(new JLabel("Los #hashtags se marcan en naranja y las @menciones en azul."),
                BorderLayout.SOUTH);
        form.add(previewPanel, BorderLayout.NORTH);
        form.add(descriptionPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(description::requestFocusInWindow);
        int answer = JOptionPane.showConfirmDialog(parent, form, "Descripción de la publicación",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Files.createDirectories(imagesDirectory.toPath());

            ArrayList<String> storedPaths = new ArrayList<>();
            long postId = System.currentTimeMillis();
            for (int index = 0; index < selectedImages.length; index++) {
                File selected = selectedImages[index];
                String cleanFileName = selected.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
                File destination = new File(imagesDirectory,
                        postId + "_" + (index + 1) + "_" + cleanFileName);
                Files.copy(selected.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                storedPaths.add(destination.getAbsolutePath());
            }

            instaManager manager = instaController.getInstance().getInsta();
            manager.setLoggedUser(username);
            String finalDescription = description.getText().trim();
            manager.addPost(InstaPostMedia.encode(storedPaths), username, finalDescription);
            int hashtags = InstaSocialText.countHashtags(finalDescription);
            int mentions = InstaSocialText.countMentions(finalDescription);
            JOptionPane.showMessageDialog(parent, "Publicación creada con " + selectedImages.length
                    + " imagen" + (selectedImages.length == 1 ? "" : "es") + ".\nDescripción guardada · "
                    + hashtags + " hashtag" + (hashtags == 1 ? "" : "s") + " · "
                    + mentions + " mención" + (mentions == 1 ? "" : "es") + ".");
            if (afterPublish != null) {
                afterPublish.run();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo publicar: " + ex.getMessage(),
                    "Nueva publicación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void installSocialHighlighting(JTextPane description, JLabel status) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshSocialHighlighting(description, status);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshSocialHighlighting(description, status);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshSocialHighlighting(description, status);
            }
        };
        description.getDocument().addDocumentListener(listener);
        refreshSocialHighlighting(description, status);
    }

    private static void refreshSocialHighlighting(JTextPane description, JLabel status) {
        String text = description.getText();
        Highlighter highlighter = description.getHighlighter();
        highlighter.removeAllHighlights();
        int hashtags = 0;
        int mentions = 0;
        java.util.regex.Matcher matcher = InstaSocialText.TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            boolean isHashtag = "#".equals(matcher.group(1));
            try {
                highlighter.addHighlight(matcher.start(), matcher.end(),
                        isHashtag ? HASHTAG_PAINTER : MENTION_PAINTER);
            } catch (BadLocationException ignored) {
            }
            if (isHashtag) {
                hashtags++;
            } else {
                mentions++;
            }
        }
        status.setText(text.length() + "/" + MAX_DESCRIPTION_LENGTH + " · #" + hashtags + " · @" + mentions);
        status.setForeground((hashtags + mentions) > 0 ? new Color(190, 85, 45) : Color.DARK_GRAY);
    }

    private static final class LengthFilter extends DocumentFilter {
        private final int maximum;

        private LengthFilter(int maximum) {
            this.maximum = maximum;
        }

        @Override
        public void insertString(FilterBypass bypass, int offset, String string, AttributeSet attributes)
                throws BadLocationException {
            if (string == null) {
                return;
            }
            int available = maximum - bypass.getDocument().getLength();
            if (available > 0) {
                super.insertString(bypass, offset, string.substring(0, Math.min(available, string.length())), attributes);
            }
        }

        @Override
        public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                throws BadLocationException {
            int replacementLength = text == null ? 0 : text.length();
            int available = maximum - (bypass.getDocument().getLength() - length);
            if (replacementLength == 0) {
                super.replace(bypass, offset, length, "", attributes);
            } else if (available > 0) {
                super.replace(bypass, offset, length,
                        text.substring(0, Math.min(available, replacementLength)), attributes);
            }
        }
    }
}
