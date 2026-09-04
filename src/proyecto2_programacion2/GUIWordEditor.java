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
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class GUIWordEditor extends JPanel{
    public JLabel label;
    public JTextPane editor;
    private JScrollPane scrollEditor;
    
    public GUIWordEditor(Component padre, CardLayout principal, JPanel cards){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(800, 500));
        setOpaque(false);

        initComponentes(principal, cards);
    }

    private void initComponentes(CardLayout principal, JPanel cards){
        InicializarBarra1( principal,  cards);
        InicializarBarra2();
        InicializarEditor();
    }

    private void InicializarBarra1( CardLayout principal, JPanel cards){
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
        
        label = new JLabel("Nombre del archivo");//***********************************agregar nombre

        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.white);
        label.setOpaque(false);

        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setPreferredSize(new Dimension(400, 30));
        
        barra.add(label);

        
        
        
        add(barra);
    }
    
    private void InicializarBarra2(){
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
       
        barra.add(Box.createHorizontalStrut(400));
        barra.add(tamaño);
        barra.add(Box.createHorizontalStrut(30));
        
        
        JMenu fuente = new JMenu("Fuente");
        fuente.setForeground(new Color(35, 35, 35));
        fuente.setFont(new Font("Arial", Font.BOLD, 14));
        fuente.setOpaque(false);
        fuente.setBorderPainted(false);
        fuente.setHorizontalAlignment(SwingConstants.CENTER);
        fuente.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        JMenuItem font1 = new JMenuItem("BOLD");
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
       
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 3, 6, 6));
        panel.setPreferredSize(new Dimension(80, 50));
        panel.setMaximumSize(new Dimension(80, 50));
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
            scrollEditor.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollEditor.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
        boton.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120), 1));
        return boton;
    }
    
    private void ajustesitem(JMenuItem item){
        item.setFont(new Font("Arial", Font.BOLD, 13));
        item.setForeground(new Color(35, 35, 35));
        item.setBackground(Color.WHITE);
        item.setOpaque(true);
        item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        item.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void aplicarTamano(int tamano) {
        if (editor == null) return;

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontSize(atributos, tamano);
        editor.setCharacterAttributes(atributos, false);
        editor.requestFocusInWindow();
    }

    private void aplicarFuente(String fuente) {
        if (editor == null) return;

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos, fuente);
        editor.setCharacterAttributes(atributos, false);
        editor.requestFocusInWindow();
    }

    private void aplicarColor(Color color) {
        if (editor == null) return;

        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos, color);
        editor.setCharacterAttributes(atributos, false);
        editor.requestFocusInWindow();
    }
    
    
    public void ingresarContenido(String contenido, String nombre){
        label.setText(nombre);
        if (contenido == null){
            editor.setText("");
        }
        
        
    }
}
