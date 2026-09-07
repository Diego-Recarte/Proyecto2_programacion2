/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class TablaEditor extends JPanel {

    private final JTable tabla;
    private final DefaultTableModel modelo;
    private TablaCelda[][] formato;

    private static final String[] FUENTES = {
        "Arial", "Serif", "SansSerif", "Monospaced", "Dialog", "DialogInput"
    };
    private static final Integer[] TAMANOS = {9, 10, 11, 12, 13, 14, 16, 18, 20, 24};

    /**
     * Crea una tabla nueva y vacia con el numero de filas/columnas indicado.
     */
    public TablaEditor(int filas, int columnas) {
        this(filas, columnas, crearFormatoVacio(filas, columnas));
    }

    /**
     * Reconstruye una tabla ya existente (por ejemplo, al abrir un archivo)
     * a partir de su contenido y formato guardado.
     */
    public TablaEditor(int filas, int columnas, TablaCelda[][] celdas) {
        super(new BorderLayout(4, 4));
        this.formato = celdas;

        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1));

        String[] encabezados = new String[columnas];
        for (int c = 0; c < columnas; c++) {
            encabezados[c] = "Col " + (c + 1);
        }

        Object[][] datos = new Object[filas][columnas];
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                datos[f][c] = formato[f][c].getTexto();
            }
        }

        modelo = new DefaultTableModel(datos, encabezados) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setGridColor(new Color(200, 200, 200));
        tabla.setDefaultRenderer(Object.class, new CeldaRenderer());
        tabla.setPreferredScrollableViewportSize(new Dimension(Math.max(120, columnas * 120), filas * 28));

        
        JPanel contenedorTabla = new JPanel(new BorderLayout());
        contenedorTabla.setOpaque(false);
        contenedorTabla.add(tabla.getTableHeader(), BorderLayout.NORTH);
        contenedorTabla.add(tabla, BorderLayout.CENTER);

        add(construirBarraFormato(), BorderLayout.NORTH);
        add(contenedorTabla, BorderLayout.CENTER);
    }

    private static TablaCelda[][] crearFormatoVacio(int filas, int columnas) {
        TablaCelda[][] celdas = new TablaCelda[filas][columnas];
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                celdas[f][c] = new TablaCelda();
            }
        }
        return celdas;
    }

    private JToolBar construirBarraFormato() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setOpaque(true);
        barra.setBackground(new Color(235, 235, 235));

        JComboBox<String> comboFuente = new JComboBox<>(FUENTES);
        comboFuente.setMaximumSize(new Dimension(120, 26));
        comboFuente.addActionListener(e -> aplicarASeleccion(c -> c.setFuente((String) comboFuente.getSelectedItem())));

        JComboBox<Integer> comboTamano = new JComboBox<>(TAMANOS);
        comboTamano.setSelectedItem(12);
        comboTamano.setMaximumSize(new Dimension(70, 26));
        comboTamano.addActionListener(e -> aplicarASeleccion(c -> c.setTamano((Integer) comboTamano.getSelectedItem())));

        JToggleButton botonNegrita = new JToggleButton("N");
        botonNegrita.setFont(new Font("Arial", Font.BOLD, 12));
        botonNegrita.addActionListener(e -> aplicarASeleccion(c -> c.setNegrita(!c.isNegrita())));

        JToggleButton botonCursiva = new JToggleButton("C");
        botonCursiva.setFont(new Font("Arial", Font.ITALIC, 12));
        botonCursiva.addActionListener(e -> aplicarASeleccion(c -> c.setCursiva(!c.isCursiva())));

        JToggleButton botonSubrayado = new JToggleButton("S");
        botonSubrayado.addActionListener(e -> aplicarASeleccion(c -> c.setSubrayado(!c.isSubrayado())));

        JToggleButton botonTachado = new JToggleButton("T");
        botonTachado.addActionListener(e -> aplicarASeleccion(c -> c.setTachado(!c.isTachado())));

        JButton botonColor = new JButton("Color");
        botonColor.addActionListener(e -> {
            Color elegido = JColorChooser.showDialog(this, "Selecciona un color", Color.BLACK);
            if (elegido != null) {
                Color colorFinal = elegido;
                aplicarASeleccion(c -> c.setColor(colorFinal));
            }
        });

        barra.add(new JLabel(" Fuente: "));
        barra.add(comboFuente);
        barra.addSeparator();
        barra.add(new JLabel(" Tamano: "));
        barra.add(comboTamano);
        barra.addSeparator();
        barra.add(botonNegrita);
        barra.add(botonCursiva);
        barra.add(botonSubrayado);
        barra.add(botonTachado);
        barra.addSeparator();
        barra.add(botonColor);

        return barra;
    }

    private interface AccionCelda {
        void aplicar(TablaCelda celda);
    }

    private void aplicarASeleccion(AccionCelda accion) {
        int[] filas = tabla.getSelectedRows();
        int[] columnas = tabla.getSelectedColumns();

        if (filas.length == 0 || columnas.length == 0) {
            return;
        }

        for (int f : filas) {
            for (int c : columnas) {
                accion.aplicar(formato[f][c]);
            }
        }
        tabla.repaint();
    }

   
    private class CeldaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            TablaCelda celda = formato[row][column];
            String texto = value == null ? "" : value.toString();

            int rgb = celda.getColor().getRGB() & 0xFFFFFF;
            StringBuilder html = new StringBuilder("<html><span style='font-family:");
            html.append(celda.getFuente());
            html.append(";font-size:").append(celda.getTamano()).append("px;");
            html.append("color:#").append(String.format("%06X", rgb)).append(";'>");
            if (celda.isNegrita()) {
                html.append("<b>");
            }
            if (celda.isCursiva()) {
                html.append("<i>");
            }
            if (celda.isSubrayado()) {
                html.append("<u>");
            }
            if (celda.isTachado()) {
                html.append("<strike>");
            }
            html.append(escaparHtml(texto));
            if (celda.isTachado()) {
                html.append("</strike>");
            }
            if (celda.isSubrayado()) {
                html.append("</u>");
            }
            if (celda.isCursiva()) {
                html.append("</i>");
            }
            if (celda.isNegrita()) {
                html.append("</b>");
            }
            html.append("</span></html>");

            etiqueta.setText(html.toString());
            if (!isSelected) {
                etiqueta.setBackground(Color.WHITE);
            }
            return etiqueta;
        }

        private String escaparHtml(String texto) {
            return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }
    }

    public int getFilas() {
        return modelo.getRowCount();
    }

    public int getColumnas() {
        return modelo.getColumnCount();
    }

  
    public void setValorCelda(int fila, int columna, String texto) {
        modelo.setValueAt(texto, fila, columna);
    }

    public TablaCelda[][] getCeldas() {
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }
        int filas = getFilas();
        int columnas = getColumnas();
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                Object valor = modelo.getValueAt(f, c);
                formato[f][c].setTexto(valor == null ? "" : valor.toString());
            }
        }
        return formato;
    }
}
