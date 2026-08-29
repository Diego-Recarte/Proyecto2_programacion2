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
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Flujo común para publicar imágenes respetando carpetas y límites. */
final class InstaPostComposer {

    static final int MAX_DESCRIPTION_LENGTH = 220;

    private InstaPostComposer() {
    }

    static void open(Component parent, String username, Runnable afterPublish) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona una imagen");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imágenes", "jpg", "jpeg", "png", "gif", "bmp", "webp"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File userDirectory = new File(new File("Instagram", "users"), username);
        File imagesDirectory = new File(userDirectory, "imagenes");
        File personalRoot = new File(userDirectory, "folders_personales");
        imagesDirectory.mkdirs();
        personalRoot.mkdirs();

        JComboBox<String> folders = new JComboBox<>();
        folders.addItem("Imágenes");
        File[] personalFolders = personalRoot.listFiles(File::isDirectory);
        if (personalFolders != null) {
            java.util.Arrays.sort(personalFolders, java.util.Comparator.comparing(File::getName));
            for (File folder : personalFolders) {
                folders.addItem(folder.getName());
            }
        }

        JTextArea description = new JTextArea(5, 28);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBackground(new Color(30, 30, 30));
        description.setForeground(Color.WHITE);
        description.setCaretColor(Color.WHITE);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        ((AbstractDocument) description.getDocument()).setDocumentFilter(new LengthFilter(MAX_DESCRIPTION_LENGTH));

        JTextField newFolder = new JTextField();
        JPanel form = new JPanel(new BorderLayout(6, 6));
        JPanel fields = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        fields.add(new JLabel("Carpeta:"));
        fields.add(folders);
        fields.add(new JLabel("Nueva carpeta personal (opcional):"));
        fields.add(newFolder);
        form.add(fields, BorderLayout.NORTH);
        JScrollPane descriptionScroll = new JScrollPane(description);
        descriptionScroll.setPreferredSize(new Dimension(330, 120));
        form.add(descriptionScroll, BorderLayout.CENTER);
        form.add(new JLabel("Descripción, #hashtags y @menciones · máximo 220 caracteres"), BorderLayout.SOUTH);

        int answer = JOptionPane.showConfirmDialog(parent, form, "Nueva publicación",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            File destinationDirectory;
            String requestedFolder = cleanFolderName(newFolder.getText());
            if (!requestedFolder.isBlank()) {
                destinationDirectory = new File(personalRoot, requestedFolder);
            } else if (folders.getSelectedIndex() > 0) {
                destinationDirectory = new File(personalRoot, String.valueOf(folders.getSelectedItem()));
            } else {
                destinationDirectory = imagesDirectory;
            }
            Files.createDirectories(destinationDirectory.toPath());

            File selected = chooser.getSelectedFile();
            String cleanFileName = selected.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            File destination = new File(destinationDirectory, System.currentTimeMillis() + "_" + cleanFileName);
            Files.copy(selected.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            instaManager manager = instaController.getInstance().getInsta();
            manager.setLoggedUser(username);
            manager.addPost(destination.getAbsolutePath(), username, description.getText().trim());
            JOptionPane.showMessageDialog(parent, "Publicación creada.");
            if (afterPublish != null) {
                afterPublish.run();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo publicar: " + ex.getMessage(),
                    "Nueva publicación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String cleanFolderName(String value) {
        String clean = value == null ? "" : value.trim().replaceAll("[^a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ_-]", "_");
        return clean.equals(".") || clean.equals("..") ? "" : clean;
    }

    private static final class LengthFilter extends DocumentFilter {
        private final int maximum;

        private LengthFilter(int maximum) {
            this.maximum = maximum;
        }

        @Override
        public void insertString(FilterBypass bypass, int offset, String string, AttributeSet attributes)
                throws BadLocationException {
            if (string != null && bypass.getDocument().getLength() + string.length() <= maximum) {
                super.insertString(bypass, offset, string, attributes);
            }
        }

        @Override
        public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                throws BadLocationException {
            int replacementLength = text == null ? 0 : text.length();
            if (bypass.getDocument().getLength() - length + replacementLength <= maximum) {
                super.replace(bypass, offset, length, text, attributes);
            }
        }
    }
}
