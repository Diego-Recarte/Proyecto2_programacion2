package Instagram;

import Logica.Ventanas.InstaWindowLayout;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import Logica.Estructuras.ListaEnlazada;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class InstaEditProfileUI extends JPanel {

    private final String currentUser;

    private final Color COLOR_BG = Color.BLACK;
    // Tu color rojo original
    private final Color COLOR_BTN = new Color(255, 69, 0); 
    // Un rojo un poco más oscuro para cuando pasas el mouse
    private final Color COLOR_BTN_HOVER = new Color(200, 50, 0); 
    
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_BORDER = new Color(100, 100, 100);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONT_CAOS = new Font("Segoe UI", Font.PLAIN, 12);

    private JPanel panelCentral;
    private JTextField txtBuscar;
    private DefaultListModel<String> listModel;
    private JList<String> resultList;

    private JTextField txtEntrar;

    public InstaEditProfileUI(String currentUser) {
        this.currentUser = currentUser;

        putClientProperty("insta.manager", instaController.getInstance().getInsta(currentUser));
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(400, 650));

        add(crearHeader(), BorderLayout.NORTH);

        panelCentral = new JPanel(new CardLayout());
        panelCentral.setBackground(COLOR_BG);

        panelCentral.add(crearPanelBuscar(), "BUSCAR");
        panelCentral.add(crearPanelEntrar(), "ENTRAR");

        add(panelCentral, BorderLayout.CENTER);
        add(crearBarraInferior(), BorderLayout.SOUTH);

        InstaWindowLayout.install(this);
    }

    void refreshActiveState(java.util.Set<String> activeUsers) {
        for (int i = listModel.size() - 1; i >= 0; i--) {
            String line = listModel.get(i); int separator = line.indexOf(" - ");
            if (separator > 0 && !activeUsers.contains(line.substring(0, separator))) listModel.remove(i);
        }
    }

    private JPanel crearHeader() {
        JPanel p = new JPanel(null);
        p.setPreferredSize(new Dimension(400, 60));
        p.setBackground(COLOR_BG);

        JLabel lblBack = new JLabel("←");
        lblBack.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBack.setForeground(COLOR_BTN);
        lblBack.setBounds(10, 12, 30, 30);
        lblBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                volverAlPerfilPropio();
            }
        });
        p.add(lblBack);

        JLabel title = new JLabel("Buscar usuarios");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_TEXT);
        title.setBounds(50, 12, 180, 30);
        p.add(title);

        return p;
    }

    private JPanel crearPanelBuscar() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(COLOR_BG);

        JPanel arriba = new JPanel(null);
        arriba.setPreferredSize(new Dimension(380, 70));
        arriba.setBackground(COLOR_BG);

        txtBuscar = new JTextField();
        txtBuscar.setBounds(10, 10, 260, 40);
        estilizarCampo(txtBuscar);
        txtBuscar.addActionListener(e -> ejecutarBusqueda());
        arriba.add(txtBuscar);

        // USAMOS EL NUEVO BOTÓN ROJO
        JButton btnBuscar = new BotonRojo("Buscar");
        btnBuscar.setBounds(280, 10, 90, 40);
        btnBuscar.addActionListener(e -> ejecutarBusqueda());
        arriba.add(btnBuscar);

        p.add(arriba, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setBackground(new Color(20, 20, 20));
        resultList.setForeground(COLOR_TEXT);
        resultList.setFont(FONT_CAOS);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setBorder(new LineBorder(COLOR_BORDER));
        JScrollPane sp = new JScrollPane(resultList);
        sp.setBorder(null);

        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = resultList.getSelectedValue();
                    if (sel != null && !sel.equals("No se encontraron usuarios")) {
                        String username = sel.split(" - ")[0];
                        abrirPerfilExternoconRetroceso(username);
                    }
                }
            }
        });

        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPanelEntrar() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(COLOR_BG);

        JPanel top = new JPanel(null);
        top.setPreferredSize(new Dimension(380, 70));
        top.setBackground(COLOR_BG);

        txtEntrar = new JTextField();
        txtEntrar.setBounds(10, 10, 360, 40);
        estilizarCampo(txtEntrar);
        txtEntrar.addActionListener(e -> {
            String target = txtEntrar.getText().trim();
            if (!target.isEmpty()) {
                abrirPerfilExternoconRetroceso(target);
            }
        });
        top.add(txtEntrar);

        p.add(top, BorderLayout.NORTH);

        JPanel infoHolder = new JPanel();
        infoHolder.setLayout(new BoxLayout(infoHolder, BoxLayout.Y_AXIS));
        infoHolder.setBackground(COLOR_BG);
        infoHolder.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel hint = new JLabel("Escribe un username y presiona Enter para ir al perfil.");
        hint.setForeground(Color.LIGHT_GRAY);
        hint.setFont(FONT_CAOS);
        infoHolder.add(hint);

        p.add(infoHolder, BorderLayout.CENTER);

        return p;
    }

    private JPanel crearBarraInferior() {
        JPanel bar = new JPanel(new GridLayout(1, 1));
        bar.setPreferredSize(new Dimension(400, 50));
        bar.setBackground(new Color(20, 20, 20));
        bar.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, COLOR_BTN));

        JLabel hint = new JLabel("Doble clic en la lista para entrar al perfil", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        hint.setFont(FONT_CAOS);
        bar.add(hint);

        return bar;
    }

    // =========================================================================
    // LÓGICA DE NEGOCIO (Igual que antes)
    // =========================================================================

    private void ejecutarBusqueda() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim();

        if (q.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa texto para buscar (no dejes el campo vacío).",
                    "Búsqueda vacía",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        listModel.clear();
        try {
            instaManager manager = instaController.getInstance().getInsta(currentUser);
            if (manager == null) {
                return;
            }

            ListaEnlazada<String> res = manager.searchUsers(q);
            if (res == null || res.isEmpty()) {
                listModel.addElement("No se encontraron usuarios");
                return;
            }

            for (String u : res) {
                if (u.equalsIgnoreCase(currentUser)) {
                    continue;
                }

                manager.setLoggedUser(currentUser);
                boolean sigo = manager.isFollowing(u);
                String linea = u + " - " + (sigo ? "LO SIGO" : "NO LO SIGO");
                listModel.addElement(linea);
            }

            if (listModel.isEmpty()) {
                listModel.addElement("No se encontraron usuarios");
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error buscando: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirPerfilExternoconRetroceso(String username) {
        try {
            instaManager manager = instaController.getInstance().getInsta(currentUser);
            if (manager == null) {
                return;
            }

            if (!manager.getStatusUser(username)) {
                JOptionPane.showMessageDialog(this, "Ese usuario no existe o está desactivado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Window window = SwingUtilities.getWindowAncestor(this);
            if (!(window instanceof JFrame)) {
                return;
            }
            JFrame frame = (JFrame) window;

            VisibilidadProfileUI vp = new VisibilidadProfileUI(username, currentUser);
            frame.setContentPane(vp);
            frame.pack();
            frame.revalidate();
            frame.repaint();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al abrir perfil: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volverAlPerfilPropio() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.setContentPane(new InstaProfileUI(currentUser));
            frame.pack();
            frame.revalidate();
            frame.repaint();
        }
    }

    private void estilizarCampo(JTextField txt) {
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(COLOR_TEXT);
        txt.setCaretColor(COLOR_TEXT);
        txt.setFont(FONT_CAOS);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 5)));
    }


    private class BotonRojo extends JButton {

        public BotonRojo(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);      
            setBorderPainted(false);     
            
            setBackground(COLOR_BTN);
            setForeground(Color.WHITE);
            setFont(FONT_CAOS);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(COLOR_BTN_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(COLOR_BTN);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
