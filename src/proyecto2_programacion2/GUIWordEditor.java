/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;

public class GUIWordEditor extends JPanel {

    public boolean IsExistente;
    public JLabel label;
    public JTextPane editor;
    private JScrollPane scrollEditor;
    public File ruta;

    public GUIWordEditor(Component padre, CardLayout principal, JPanel cards) {
        IsExistente = false;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(800, 500));
        setOpaque(false);

        initComponentes(principal, cards);
    }

    private void initComponentes(CardLayout principal, JPanel cards) {
        InicializarBarra1(principal, cards);
        InicializarBarra2();
        InicializarEditor();
    }

    private void InicializarBarra1(CardLayout principal, JPanel cards) {

        JMenuBar barra;
        JButton botonb;

        barra = new JMenuBar();
        barra.setOpaque(true);
        barra.setBorderPainted(true);
        barra.setMargin(new Insets(4, 8, 4, 8));
        barra.setMaximumSize(new Dimension(1300, 42));
        barra.setPreferredSize(new Dimension(1300, 42));
        barra.setMinimumSize(new Dimension(1300, 42));
        barra.setBackground(Color.BLUE);
        barra.setForeground(Color.WHITE);
        barra.setFont(new Font("Arial", Font.BOLD, 14));

        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        botonb = new JButton("Archivo");
        botonb.setFont(new Font("Arial", Font.BOLD, 14));
        botonb.setOpaque(false);
        botonb.setForeground(Color.WHITE);
        botonb.setBackground(Color.BLUE);
        botonb.setFocusable(false);
        botonb.setBorderPainted(false);
        botonb.setContentAreaFilled(false);

        botonb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 90), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        botonb.addActionListener(e -> {
            principal.show(cards, "nuevo");
        });

        barra.add(botonb);

        label = new JLabel("Nombre del archivo");

        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setOpaque(false);

        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setPreferredSize(new Dimension(400, 30));

        barra.add(label);

        add(barra);
    }

    private void InicializarBarra2() {

        JMenuBar barra;
        barra = new JMenuBar();

        barra.setOpaque(true);
        barra.setBorderPainted(true);
        barra.setMargin(new Insets(6, 8, 6, 8));
        barra.setMaximumSize(new Dimension(1300, 82));
        barra.setPreferredSize(new Dimension(1300, 82));
        barra.setMinimumSize(new Dimension(1300, 82));
        barra.setBackground(new Color(245, 245, 245));
        barra.setForeground(Color.BLACK);
        barra.setFont(new Font("Arial", Font.BOLD, 14));

        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JMenu tamaño = new JMenu("Tamaño");

        tamaño.setForeground(new Color(35, 35, 35));
        tamaño.setFont(new Font("Arial", Font.BOLD, 14));
        tamaño.setOpaque(false);
        tamaño.setBorderPainted(false);
        tamaño.setHorizontalAlignment(SwingConstants.CENTER);
        tamaño.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JMenuItem tam9 = new JMenuItem("9");
        ajustesitem(tam9);
        tam9.addActionListener(e -> aplicarTamano(9));

        JMenuItem tam10 = new JMenuItem("10");
        ajustesitem(tam10);
        tam10.addActionListener(e -> aplicarTamano(10));

        JMenuItem tam11 = new JMenuItem("11");
        ajustesitem(tam11);
        tam11.addActionListener(e -> aplicarTamano(11));

        JMenuItem tam12 = new JMenuItem("12");
        ajustesitem(tam12);
        tam12.addActionListener(e -> aplicarTamano(12));

        JMenuItem tam13 = new JMenuItem("13");
        ajustesitem(tam13);
        tam13.addActionListener(e -> aplicarTamano(13));

        JMenuItem tam14 = new JMenuItem("14");
        ajustesitem(tam14);
        tam14.addActionListener(e -> aplicarTamano(14));

        JMenuItem tam15 = new JMenuItem("15");
        ajustesitem(tam15);
        tam15.addActionListener(e -> aplicarTamano(15));

        JMenuItem tam16 = new JMenuItem("16");
        ajustesitem(tam16);
        tam16.addActionListener(e -> aplicarTamano(16));

        JMenuItem tam17 = new JMenuItem("17");
        ajustesitem(tam17);
        tam17.addActionListener(e -> aplicarTamano(17));

        tamaño.add(tam9);
        tamaño.add(tam10);
        tamaño.add(tam11);
        tamaño.add(tam12);
        tamaño.add(tam13);
        tamaño.add(tam14);
        tamaño.add(tam15);
        tamaño.add(tam16);
        tamaño.add(tam17);

        barra.add(Box.createHorizontalStrut(200));
        barra.add(tamaño);
        barra.add(Box.createHorizontalStrut(30));

        JMenu fuente = new JMenu("Fuente");

        fuente.setForeground(new Color(35, 35, 35));
        fuente.setFont(new Font("Arial", Font.BOLD, 14));
        fuente.setOpaque(false);
        fuente.setBorderPainted(false);
        fuente.setHorizontalAlignment(SwingConstants.CENTER);
        fuente.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JMenuItem font1 = new JMenuItem("Arial");
        ajustesitem(font1);
        font1.addActionListener(e -> aplicarFuente("Arial"));

        JMenuItem font2 = new JMenuItem("Serif");
        ajustesitem(font2);
        font2.addActionListener(e -> aplicarFuente("Serif"));

        JMenuItem font3 = new JMenuItem("SansSerif");
        ajustesitem(font3);
        font3.addActionListener(e -> aplicarFuente("SansSerif"));

        JMenuItem font4 = new JMenuItem("Monospaced");
        ajustesitem(font4);
        font4.addActionListener(e -> aplicarFuente("Monospaced"));

        JMenuItem font5 = new JMenuItem("Dialog");
        ajustesitem(font5);
        font5.addActionListener(e -> aplicarFuente("Dialog"));

        JMenuItem font6 = new JMenuItem("DialogInput");
        ajustesitem(font6);
        font6.addActionListener(e -> aplicarFuente("DialogInput"));

        fuente.add(font1);
        fuente.add(font2);
        fuente.add(font3);
        fuente.add(font4);
        fuente.add(font5);
        fuente.add(font6);

        barra.add(fuente);
        barra.add(Box.createHorizontalStrut(30));

        JMenu estilos = new JMenu("Estilos");

        estilos.setForeground(new Color(35, 35, 35));
        estilos.setFont(new Font("Arial", Font.BOLD, 14));
        estilos.setOpaque(false);
        estilos.setBorderPainted(false);
        estilos.setHorizontalAlignment(SwingConstants.CENTER);
        estilos.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JMenuItem itemNegrita = new JMenuItem("Negrita");
        ajustesitem(itemNegrita);
        itemNegrita.addActionListener(e -> alternarEstilo(1));

        JMenuItem itemCursiva = new JMenuItem("Cursiva");
        ajustesitem(itemCursiva);
        itemCursiva.addActionListener(e -> alternarEstilo(2));

        JMenuItem itemSubrayado = new JMenuItem("Subrayado");
        ajustesitem(itemSubrayado);
        itemSubrayado.addActionListener(e -> alternarEstilo(3));

        JMenuItem itemTachado = new JMenuItem("Tachado");
        ajustesitem(itemTachado);
        itemTachado.addActionListener(e -> alternarEstilo(4));

        estilos.add(itemNegrita);
        estilos.add(itemCursiva);
        estilos.add(itemSubrayado);
        estilos.add(itemTachado);

        barra.add(estilos);
        barra.add(Box.createHorizontalStrut(30));

        JButton botonTabla = new JButton("Insertar tabla");

        botonTabla.setFont(new Font("Arial", Font.BOLD, 13));
        botonTabla.setForeground(new Color(35, 35, 35));
        botonTabla.setBackground(Color.WHITE);
        botonTabla.setFocusPainted(false);

        botonTabla.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        botonTabla.addActionListener(e -> mostrarDialogoInsertarTabla());

        barra.add(botonTabla);
        barra.add(Box.createHorizontalStrut(30));

        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(2, 4, 6, 6));
        panel.setPreferredSize(new Dimension(110, 50));
        panel.setMaximumSize(new Dimension(110, 50));
        panel.setOpaque(true);
        panel.setBackground(new Color(245, 245, 245));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        JButton black = crearBotonColor(Color.BLACK);
        JButton blue = crearBotonColor(Color.BLUE);
        JButton red = crearBotonColor(Color.RED);
        JButton yellow = crearBotonColor(Color.YELLOW);
        JButton green = crearBotonColor(Color.GREEN);
        JButton orange = crearBotonColor(Color.ORANGE);

        black.addActionListener(e -> aplicarColor(Color.BLACK));
        blue.addActionListener(e -> aplicarColor(Color.BLUE));
        red.addActionListener(e -> aplicarColor(Color.RED));
        yellow.addActionListener(e -> aplicarColor(Color.YELLOW));
        green.addActionListener(e -> aplicarColor(Color.GREEN));
        orange.addActionListener(e -> aplicarColor(Color.ORANGE));

        panel.add(black);
        panel.add(blue);
        panel.add(red);
        panel.add(yellow);
        panel.add(green);
        panel.add(orange);

        JButton btnMasColores = new JButton("+");

        btnMasColores.setMargin(new Insets(0, 0, 0, 0));
        btnMasColores.setBackground(Color.WHITE);
        btnMasColores.setFocusPainted(false);
        btnMasColores.setBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 120), 1)
        );

        btnMasColores.addActionListener(e -> {

            Color colorSeleccionado = JColorChooser.showDialog(
                    this,
                    "Selecciona un color",
                    Color.BLACK
            );

            if (colorSeleccionado != null) {
                aplicarColor(colorSeleccionado);
            }
        });

        panel.add(btnMasColores);

        barra.add(panel);

        add(barra);
    }

    private void InicializarEditor() {

        editor = new JTextPane() {

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }

            @Override
            public void setSize(Dimension d) {

                if (d.width < getParent().getSize().width) {
                    d.width = getParent().getSize().width;
                }

                super.setSize(d);
            }
        };

        editor.setFont(new Font("Arial", Font.PLAIN, 14));
        editor.setForeground(Color.BLACK);
        editor.setBackground(Color.WHITE);
        editor.setCaretColor(Color.BLACK);
        editor.setMargin(new Insets(20, 20, 20, 20));

        scrollEditor = new JScrollPane(editor);

        scrollEditor.setPreferredSize(new Dimension(700, 500));
        scrollEditor.setMinimumSize(new Dimension(700, 300));
        scrollEditor.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

        scrollEditor.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollEditor.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        scrollEditor.getViewport().setBackground(Color.WHITE);

        add(scrollEditor);
    }

    private JButton crearBotonColor(Color color) {

        JButton boton = new JButton();

        boton.setPreferredSize(new Dimension(22, 22));
        boton.setMinimumSize(new Dimension(22, 22));
        boton.setMaximumSize(new Dimension(22, 22));

        boton.setBackground(color);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setBorderPainted(true);
        boton.setContentAreaFilled(true);

        boton.setHorizontalAlignment(SwingConstants.CENTER);

        boton.setBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 120), 1)
        );

        return boton;
    }

    private void ajustesitem(JMenuItem item) {

        item.setFont(new Font("Arial", Font.BOLD, 13));
        item.setForeground(new Color(35, 35, 35));
        item.setBackground(Color.WHITE);
        item.setOpaque(true);

        item.setBorder(
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        );

        item.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void aplicarTamano(int tamano) {

        if (editor == null) {
            return;
        }

        int inicio = editor.getSelectionStart();
        int fin = editor.getSelectionEnd();

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontSize(atributos, tamano);

        if (inicio != fin) {
            editor.getStyledDocument().setCharacterAttributes(
                    inicio,
                    fin - inicio,
                    atributos,
                    false
            );
        } else {
            editor.setCharacterAttributes(atributos, false);
        }

        editor.requestFocusInWindow();
    }

    private void aplicarFuente(String fuente) {

        if (editor == null) {
            return;
        }

        int inicio = editor.getSelectionStart();
        int fin = editor.getSelectionEnd();

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos, fuente);

        if (inicio != fin) {
            editor.getStyledDocument().setCharacterAttributes(
                    inicio,
                    fin - inicio,
                    atributos,
                    false
            );
        } else {
            editor.setCharacterAttributes(atributos, false);
        }

        editor.requestFocusInWindow();
    }

    private void aplicarColor(Color color) {

        if (editor == null) {
            return;
        }

        int inicio = editor.getSelectionStart();
        int fin = editor.getSelectionEnd();

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos, color);

        if (inicio != fin) {
            editor.getStyledDocument().setCharacterAttributes(
                    inicio,
                    fin - inicio,
                    atributos,
                    false
            );
        } else {
            editor.setCharacterAttributes(atributos, false);
        }

        editor.requestFocusInWindow();
    }

    private void alternarEstilo(int tipo) {

        if (editor == null) {
            return;
        }

        int inicio = editor.getSelectionStart();
        int fin = editor.getSelectionEnd();

        StyledDocument documento = editor.getStyledDocument();

        AttributeSet atributosActuales;
        if (inicio != fin) {
            atributosActuales = documento.getCharacterElement(inicio).getAttributes();
        } else {
            atributosActuales = editor.getInputAttributes();
        }

        boolean activo = false;

        switch (tipo) {
            case 1:
                activo = StyleConstants.isBold(atributosActuales);
                break;
            case 2:
                activo = StyleConstants.isItalic(atributosActuales);
                break;
            case 3:
                activo = StyleConstants.isUnderline(atributosActuales);
                break;
            case 4:
                activo = StyleConstants.isStrikeThrough(atributosActuales);
                break;
        }

        SimpleAttributeSet atributos = new SimpleAttributeSet();

        switch (tipo) {
            case 1:
                StyleConstants.setBold(atributos, !activo);
                break;
            case 2:
                StyleConstants.setItalic(atributos, !activo);
                break;
            case 3:
                StyleConstants.setUnderline(atributos, !activo);
                break;
            case 4:
                StyleConstants.setStrikeThrough(atributos, !activo);
                break;
        }

        if (inicio != fin) {
            documento.setCharacterAttributes(
                    inicio,
                    fin - inicio,
                    atributos,
                    false
            );
        } else {
            editor.setCharacterAttributes(atributos, false);
        }

        editor.requestFocusInWindow();
    }

    public void ingresarContenido(String contenido, String nombre) {

        label.setText(nombre);

        if (contenido == null) {
            editor.setText("");
        } else {
            editor.setText(contenido);
        }
    }

    private void mostrarDialogoInsertarTabla() {

        JSpinner spinnerFilas =
                new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));

        JSpinner spinnerColumnas =
                new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));

        JPanel panel = new JPanel(
                new GridLayout(2, 2, 8, 8)
        );

        panel.add(new JLabel("Filas:"));
        panel.add(spinnerFilas);

        panel.add(new JLabel("Columnas:"));
        panel.add(spinnerColumnas);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Insertar tabla",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado == JOptionPane.OK_OPTION) {

            int filas = (Integer) spinnerFilas.getValue();
            int columnas = (Integer) spinnerColumnas.getValue();

            insertarTabla(new TablaEditor(filas, columnas));
        }
    }

    public void insertarTabla(TablaEditor tabla) {

        if (editor == null) {
            return;
        }

        editor.setCaretPosition(
                editor.getDocument().getLength()
        );

        editor.insertComponent(tabla);

        try {
            editor.getDocument().insertString(
                    editor.getDocument().getLength(),
                    "\n",
                    null
            );
        } catch (BadLocationException ex) {

        }

        editor.requestFocusInWindow();
    }
}