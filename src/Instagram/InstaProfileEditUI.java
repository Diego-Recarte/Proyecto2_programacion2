package Instagram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Formulario para editar los datos visibles del perfil. */
public final class InstaProfileEditUI extends JPanel {

    private static final Color BACKGROUND = Color.BLACK;
    private static final Color FIELD = new Color(28, 28, 28);
    private static final Color ACCENT = new Color(255, 69, 0);
    private static final Color BORDER = new Color(65, 65, 65);

    private final String username;
    private final JLabel avatar = new JLabel("Sin foto", SwingConstants.CENTER);
    private final JTextField nameField = new JTextField();
    private final JTextField ageField = new JTextField();
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Masculino", "Femenino", "Otro"});
    private final JButton accountButton = new JButton();
    private String selectedPicture;

    public InstaProfileEditUI(String username) {
        this.username = username;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 650));
        setBackground(BACKGROUND);
        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        loadCurrentData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(400, 58));
        header.setBackground(BACKGROUND);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel back = new JLabel("‹");
        back.setBounds(15, 5, 40, 45);
        back.setForeground(ACCENT);
        back.setFont(new Font("Segoe UI", Font.PLAIN, 34));
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProfile();
            }
        });
        header.add(back);

        JLabel title = new JLabel("Editar perfil", SwingConstants.CENTER);
        title.setBounds(65, 12, 270, 32);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title);
        return header;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(null);
        form.setBackground(BACKGROUND);

        avatar.setBounds(145, 25, 110, 110);
        avatar.setForeground(Color.GRAY);
        avatar.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
        form.add(avatar);

        JButton changePicture = textButton("Cambiar foto del perfil");
        changePicture.setBounds(100, 145, 200, 32);
        changePicture.addActionListener(e -> selectPicture());
        form.add(changePicture);

        addLabel(form, "Nombre", 35, 205);
        styleField(nameField);
        nameField.setBounds(35, 230, 330, 42);
        form.add(nameField);

        addLabel(form, "Nombre de usuario", 35, 292);
        JTextField usernameField = new JTextField("@" + username);
        styleField(usernameField);
        usernameField.setEnabled(false);
        usernameField.setDisabledTextColor(Color.GRAY);
        usernameField.setBounds(35, 317, 330, 42);
        form.add(usernameField);

        addLabel(form, "Edad", 35, 379);
        styleField(ageField);
        ageField.setBounds(35, 404, 155, 42);
        form.add(ageField);

        addLabel(form, "Género", 210, 379);
        genderBox.setBounds(210, 404, 155, 42);
        genderBox.setBackground(FIELD);
        genderBox.setForeground(Color.WHITE);
        genderBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(genderBox);

        JButton save = new JButton("Guardar cambios");
        save.setBounds(35, 490, 330, 43);
        save.setBackground(ACCENT);
        save.setForeground(Color.WHITE);
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.setFocusPainted(false);
        save.setBorderPainted(false);
        save.setCursor(new Cursor(Cursor.HAND_CURSOR));
        save.addActionListener(e -> saveChanges());
        form.add(save);

        accountButton.setBounds(35, 548, 330, 40);
        accountButton.setForeground(new Color(235, 90, 90));
        accountButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        accountButton.setFocusPainted(false);
        accountButton.setContentAreaFilled(false);
        accountButton.setBorder(BorderFactory.createLineBorder(new Color(120, 50, 50)));
        accountButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        accountButton.addActionListener(e -> toggleAccount());
        form.add(accountButton);
        return form;
    }

    private void loadCurrentData() {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            nameField.setText(manager.getRealName(username));
            ageField.setText(String.valueOf(manager.getAge(username)));
            char gender = manager.getGender(username);
            genderBox.setSelectedIndex(gender == 'M' ? 0 : gender == 'F' ? 1 : 2);
            String picture = manager.getProfilePic(username);
            if (picture != null && new File(picture).isFile()) {
                showPicture(picture);
            }
            updateAccountButton(manager.getStatusUser(username));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el perfil: " + ex.getMessage(),
                    "Editar perfil", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectPicture() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona una foto de perfil");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPicture = chooser.getSelectedFile().getAbsolutePath();
            showPicture(selectedPicture);
        }
    }

    private void showPicture(String path) {
        try {
            BufferedImage source = ImageIO.read(new File(path));
            if (source == null) {
                return;
            }
            int square = Math.min(source.getWidth(), source.getHeight());
            BufferedImage crop = source.getSubimage(
                    (source.getWidth() - square) / 2,
                    (source.getHeight() - square) / 2,
                    square,
                    square);
            avatar.setIcon(new ImageIcon(crop.getScaledInstance(106, 106, Image.SCALE_SMOOTH)));
            avatar.setText("");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la imagen.", "Imagen", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveChanges() {
        String realName = nameField.getText().trim();
        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La edad debe ser un número.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        char gender = genderBox.getSelectedIndex() == 0 ? 'M' : genderBox.getSelectedIndex() == 1 ? 'F' : 'O';
        try {
            instaManager manager = instaController.getInstance().getInsta();
            if (manager.updateProfile(username, realName, gender, age, selectedPicture)) {
                JOptionPane.showMessageDialog(this, "Perfil actualizado.");
                showProfile();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleAccount() {
        try {
            instaManager manager = instaController.getInstance().getInsta();
            boolean active = manager.getStatusUser(username);
            if (active) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "¿Desactivar tu cuenta? Tu perfil, publicaciones y comentarios dejarán de mostrarse.",
                        "Desactivar cuenta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
                if (manager.desactivateUser(username)) {
                    manager.loggoutUser();
                    JOptionPane.showMessageDialog(this, "Cuenta desactivada.");
                    showLogin();
                }
            } else if (manager.activateUser(username)) {
                updateAccountButton(true);
                JOptionPane.showMessageDialog(this, "Cuenta reactivada.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo cambiar el estado",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAccountButton(boolean active) {
        accountButton.setText(active ? "Desactivar cuenta" : "Reactivar cuenta");
        accountButton.setForeground(active ? new Color(235, 90, 90) : new Color(70, 200, 100));
    }

    private void showLogin() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            frame.setContentPane(new InstaLoginUI());
            frame.pack();
            frame.revalidate();
            frame.repaint();
        }
    }

    private void showProfile() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            frame.setContentPane(new InstaProfileUI(username));
            frame.pack();
            frame.revalidate();
            frame.repaint();
        }
    }

    private void addLabel(JPanel panel, String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 180, 20);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label);
    }

    private void styleField(JTextField field) {
        field.setBackground(FIELD);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(6, 10, 6, 10)));
    }

    private JButton textButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(ACCENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
