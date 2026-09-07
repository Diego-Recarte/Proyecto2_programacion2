/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUIWordGuardarComo extends JPanel {

    public JButton archivo;
    private Timer timer;
    private JLabel labele;
    public JButton Guardarc;
    public JButton Guardar;

    public GUIWordGuardarComo(GUIpantallaWord padre, CardLayout principal, JPanel cards,  GUIWordEditor campo) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(1200, 800));
        setOpaque(false);
        initTimer();
        initComponentes(principal, cards, campo, padre);
    }

    private void initTimer() {
        timer = new Timer(2000, ev -> {
            labele.setText(" ");
        });
        timer.setRepeats(false);
    }

    public void initComponentes(CardLayout principal, JPanel cards, GUIWordEditor campo, GUIpantallaWord padre) {
        InitBarra(campo, principal, cards, padre);
        Inicializarbotones(campo,principal,  cards, padre);
    }

    private String mensajeDeError(WordException ex) {
        if (ex instanceof WordException.ArchivoYaExisteException) {
            return "Ya existe un archivo con ese nombre";
        } else if (ex instanceof WordException.ArchivoNoExisteParaGuardarException) {
            return "El documento aún no existe; use \"Guardar como\"";
        } else if (ex instanceof WordException.DatosInvalidosException) {
            return "El nombre del documento no es válido";
        } else if (ex instanceof WordException.ArchivoNoEncontradoException) {
            return "El archivo no existe";
        } else if (ex instanceof WordException.ExtensionInvalidaException) {
            return "La extensión del archivo no es válida";
        } else if (ex instanceof WordException.FormatoDesconocidoException) {
            return "El archivo no tiene el formato del editor";
        } else if (ex instanceof WordException.VersionNoCompatibleException) {
            return "El archivo fue guardado con otra versión del editor";
        } else if (ex instanceof WordException.ArchivoCorruptoException) {
            return "El archivo está corrupto o incompleto";
        }
        return "No se pudo completar la operación: " + ex.getMessage();
    }

    private void InitBarra (GUIWordEditor campo,CardLayout principal, JPanel cards, GUIpantallaWord padre) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 1200));
        panel.setMinimumSize(new Dimension(300, 1200));
        panel.setMaximumSize(new Dimension(300, 1200));
        panel.setOpaque(true);
        panel.setBackground(Color.BLUE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        archivo = new JButton("Archivo");
        archivo.setFont(new Font("Arial", Font.BOLD, 16));
        archivo.setPreferredSize(new Dimension(200, 35));
        archivo.setMinimumSize(new Dimension(200, 35));
        archivo.setMaximumSize(new Dimension(200, 35));
        archivo.setForeground(Color.white);
        archivo.setOpaque(false);
        archivo.setFocusPainted(false);
        archivo.setBorderPainted(false);
        archivo.setContentAreaFilled(false);
        archivo.setHorizontalAlignment(SwingConstants.CENTER);

        archivo.addActionListener(e -> {
            principal.show(cards, "editor");
        });

        JButton Nuevo = new JButton("Nuevo");
        Nuevo.setFont(new Font("Arial", Font.BOLD, 14));
        Nuevo.setPreferredSize(new Dimension(200, 35));
        Nuevo.setMinimumSize(new Dimension(200, 35));
        Nuevo.setMaximumSize(new Dimension(200, 35));
        Nuevo.setForeground(Color.white);
        Nuevo.setOpaque(false);
        Nuevo.setFocusPainted(false);
        Nuevo.setBorderPainted(false);
        Nuevo.setContentAreaFilled(false);
        Nuevo.setHorizontalAlignment(SwingConstants.LEFT);

        Nuevo.addActionListener(e -> {
            padre.mostrarCard("nuevo");
        });

        Guardar = new JButton("Guardar");
        Guardar.setFont(new Font("Arial", Font.BOLD, 14));
        Guardar.setPreferredSize(new Dimension(200, 35));
        Guardar.setMinimumSize(new Dimension(200, 35));
        Guardar.setMaximumSize(new Dimension(200, 35));
        Guardar.setForeground(Color.white);
        Guardar.setOpaque(false);
        Guardar.setFocusPainted(false);
        Guardar.setBorderPainted(false);
        Guardar.setContentAreaFilled(false);
        Guardar.setHorizontalAlignment(SwingConstants.LEFT);

        Guardar.addActionListener(e -> {
            try {
                if (campo.ruta == null) {
                    throw new WordException.ArchivoNoExisteParaGuardarException(
                            "El documento aún no existe; use \"Guardar como\" primero.");
                }

                WordArchivos.guardar(campo.editor, campo.ruta, campo.label.getText().trim(), false);
                principal.show(cards, "editor");
            } catch (WordException ex) {
                labele.setText(mensajeDeError(ex));
                timer.start();
            }
        });

        Guardarc = new JButton("Guardar como");
        Guardarc.setFont(new Font("Arial", Font.BOLD, 14));
        Guardarc.setPreferredSize(new Dimension(200, 35));
        Guardarc.setMinimumSize(new Dimension(200, 35));
        Guardarc.setMaximumSize(new Dimension(200, 35));
        Guardarc.setForeground(Color.white);
        Guardarc.setOpaque(false);
        Guardarc.setFocusPainted(false);
        Guardarc.setBorderPainted(false);
        Guardarc.setContentAreaFilled(false);
        Guardarc.setHorizontalAlignment(SwingConstants.LEFT);

        Guardarc.addActionListener(e -> {
            padre.mostrarCard("guardarComo");
        });

        JButton Cargar = new JButton("Cargar");
        Cargar.setFont(new Font("Arial", Font.BOLD, 14));
        Cargar.setPreferredSize(new Dimension(200, 35));
        Cargar.setMinimumSize(new Dimension(200, 35));
        Cargar.setMaximumSize(new Dimension(200, 35));
        Cargar.setForeground(Color.white);
        Cargar.setOpaque(false);
        Cargar.setFocusPainted(false);
        Cargar.setBorderPainted(false);
        Cargar.setContentAreaFilled(false);
        Cargar.setHorizontalAlignment(SwingConstants.LEFT);

        Cargar.addActionListener(e -> {
                    File carpetaBase;
                    if (usuarioWinActivo.isAdmin){
                        
                     carpetaBase = new File( "src/datos/windows/Z/infoUsuarios" );
                    }else{
                     carpetaBase = new File( "src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre  );
                    }

                    GUISelector selector = new GUISelector(SwingUtilities.getWindowAncestor(this),carpetaBase,"pwrd" );

                    selector.setVisible(true);

                    File archivoSeleccionado = selector.getArchivoSeleccionado();

                    if (archivoSeleccionado != null) {
                        try{
                        WordArchivos.abrir(campo.label, campo.editor, archivoSeleccionado);
                        }catch (WordException ex){
                            
                        }

                        archivo.setVisible(true);
                        Guardar.setVisible(true);
                        Guardarc.setVisible(true);

                        principal.show(cards, "editor");
                    }

        });

        panel.add(archivo);
        panel.add(Box.createVerticalStrut(200));
        panel.add(Nuevo);
        panel.add(Box.createVerticalStrut(70));
        panel.add(Guardar);
        panel.add(Box.createVerticalStrut(70));
        panel.add(Guardarc);
        panel.add(Box.createVerticalStrut(70));
        panel.add(Cargar);

        add(panel);
    }

    private void Inicializarbotones(GUIWordEditor campo, CardLayout principal, JPanel cards, GUIpantallaWord padre) {
        JPanel Panelenvuelto = new JPanel(new GridBagLayout());
        Panelenvuelto.setOpaque(false);

        JLabel label = new JLabel("Confirma el nombre");
        label.setFont(new Font("Arial", Font.BOLD, 35));
        label.setPreferredSize(new Dimension(400, 100));
        label.setMaximumSize(new Dimension(400, 100));
        label.setMinimumSize(new Dimension(400, 100));
        label.setForeground(Color.BLUE);
        label.setOpaque(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nombre = new JTextField(" ");
        nombre.setFont(new Font("Arial", Font.BOLD, 14));
        nombre.setPreferredSize(new Dimension(500, 50));
        nombre.setMaximumSize(new Dimension(500, 50));
        nombre.setMinimumSize(new Dimension(500, 50));
        nombre.setForeground(Color.black);
        nombre.setOpaque(false);
        nombre.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        nombre.setHorizontalAlignment(SwingConstants.CENTER);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton boton3 = new JButton("Guardar");
        boton3.setFont(new Font("Arial", Font.BOLD, 14));
        boton3.setPreferredSize(new Dimension(500, 50));
        boton3.setMaximumSize(new Dimension(500, 50));
        boton3.setMinimumSize(new Dimension(500, 50));
        boton3.setForeground(Color.WHITE);
        boton3.setBackground(Color.BLUE);
        boton3.setFocusPainted(false);
        boton3.setBorderPainted(false);
        boton3.setContentAreaFilled(false);
        boton3.setOpaque(true);
        boton3.setHorizontalAlignment(SwingConstants.CENTER);
        boton3.setAlignmentX(Component.CENTER_ALIGNMENT);

        boton3.addActionListener(e -> {
            String nombreTexto = nombre.getText().trim();

            if (nombreTexto.isEmpty()) {
                campo.ingresarContenido(null, nombreTexto);
            } else {
                try {
                    File acceso = WordArchivos.archivoDocumento(nombreTexto);

                    WordArchivos.guardarComo(campo.editor, acceso, nombreTexto, true);

                    nombre.setText("");
                    campo.IsExistente = true;
                    campo.ruta = acceso;
                    padre.cambiarGuardar();

                    principal.show(cards, "editor");
                } catch (WordException ex) {
                    
                    labele.setText(mensajeDeError(ex));
                    timer.start();
                } 
            }
        });

        labele = new JLabel("");
        labele.setFont(new Font("Arial", Font.BOLD, 14));
        labele.setPreferredSize(new Dimension(400, 100));
        labele.setMaximumSize(new Dimension(400, 100));
        labele.setMinimumSize(new Dimension(400, 100));
        labele.setForeground(Color.red);
        labele.setOpaque(false);
        labele.setHorizontalAlignment(SwingConstants.CENTER);
        labele.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(500, 400));
        panel.setMaximumSize(new Dimension(500, 400));
        panel.setMinimumSize(new Dimension(500, 400));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(nombre);
        panel.add(Box.createVerticalStrut(10));
        panel.add(boton3);
        panel.add(Box.createVerticalStrut(10));
        panel.add(labele);

        Panelenvuelto.add(panel);
        add(Panelenvuelto);
    }
}
