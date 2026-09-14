package Instagram;

import Logica.Ventanas.InstaImages;
import Logica.Ventanas.InstaWindowLayout;

import Logica.Estructuras.ListaEnlazada;
import java.awt.*;
import javax.swing.*;

/** Carpetas personales dentro de la misma pantalla de INSTA+. */
final class InstaFoldersUI extends JPanel {
    private final String user;
    private final DefaultListModel<String> folders = new DefaultListModel<>();
    private final JList<String> list = new JList<>(folders);
    private final JPanel gallery = new JPanel(new GridLayout(0, 3, 4, 4));
    private final JLabel status = new JLabel("Selecciona una carpeta para ver sus imágenes.");
    private int request;

    InstaFoldersUI(String user, Runnable back) {
        this.user = user;
        putClientProperty("insta.manager", instaController.getInstance().getInsta(user));
        setLayout(new BorderLayout(8, 8)); setPreferredSize(new Dimension(400, 650));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel header = new JPanel(new BorderLayout(12, 0));
        JButton previous = new JButton("Volver"); previous.addActionListener(e -> back.run());
        header.add(previous, BorderLayout.WEST); header.add(new JLabel("Mis carpetas", SwingConstants.CENTER)); add(header, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(list), new JScrollPane(galleryContainer()));
        split.setResizeWeight(.3); split.setDividerLocation(140);
        list.setBackground(new Color(25, 25, 25)); list.setForeground(Color.WHITE);
        list.setSelectionBackground(new Color(150, 60, 10)); list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(32); add(split);
        JPanel actions = new JPanel(new BorderLayout(6, 6)); JTextField name = new JTextField();
        JPanel nameRow = new JPanel(new BorderLayout(0, 6));
        nameRow.add(new JLabel("Nombre de la nueva carpeta:"), BorderLayout.NORTH); nameRow.add(name);
        actions.add(nameRow, BorderLayout.NORTH);
        JButton create = new JButton("Crear carpeta"); create.addActionListener(e -> change(name.getText().trim(), true));
        JButton delete = new JButton("Eliminar carpeta vacía"); delete.addActionListener(e -> {
            if (list.getSelectedValue() != null) change(list.getSelectedValue(), false);
        });
        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0)); buttons.add(create); buttons.add(delete);
        actions.add(buttons); actions.add(status, BorderLayout.SOUTH); add(actions, BorderLayout.SOUTH);
        InstaPostComposer.style(this);
        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showImages(); });
        refresh();
        InstaWindowLayout.install(this);
    }

    private JPanel galleryContainer() {
        JPanel container = new JPanel(new BorderLayout()); container.add(gallery, BorderLayout.NORTH); return container;
    }

    private void refresh() {
        new SwingWorker<ListaEnlazada<String>, Void>() {
            protected ListaEnlazada<String> doInBackground() throws Exception { return instaController.getInstance().getInsta(user).getPersonalFolders(user); }
            protected void done() {
                try {
                    String selected = list.getSelectedValue(); folders.clear();
                    for (String name : get()) folders.addElement(name);
                    if (selected != null && folders.contains(selected)) list.setSelectedValue(selected, true);
                    else if (!folders.isEmpty()) list.setSelectedIndex(0);
                }
                catch (Exception ex) { status.setText("No se pudieron cargar las carpetas."); }
            }
        }.execute();
    }

    private void change(String name, boolean create) {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                instaManager manager = instaController.getInstance().getInsta(user);
                if (create) manager.createPersonalFolder(user, name); else manager.deletePersonalFolder(user, name);
                return null;
            }
            protected void done() {
                try { get(); status.setText(create ? "Carpeta creada" : "Carpeta eliminada"); refresh(); }
                catch (Exception ex) { status.setText(create ? "Nombre inválido o carpeta no disponible." : "Solo puedes eliminar carpetas vacías."); }
            }
        }.execute();
    }

    private void showImages() {
        int id = ++request; String folder = list.getSelectedValue(); gallery.removeAll();
        if (folder == null) { gallery.revalidate(); gallery.repaint(); return; }
        new SwingWorker<ListaEnlazada<String>, Void>() {
            protected ListaEnlazada<String> doInBackground() throws Exception { return instaController.getInstance().getInsta(user).getFolderImages(user, folder); }
            protected void done() {
                if (id != request) return;
                try { for (String path : get()) gallery.add(new JLabel(InstaImages.icon(InstaFoldersUI.this, path, 105, 105, true))); }
                catch (Exception ex) { status.setText("No se pudieron cargar las imágenes."); }
                gallery.revalidate(); gallery.repaint();
            }
        }.execute();
    }
}
