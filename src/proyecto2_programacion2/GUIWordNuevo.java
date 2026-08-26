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
public class GUIWordNuevo extends JPanel{
    private JButton archivo;
    
    public GUIWordNuevo(GUIpantallaWord padre, CardLayout principal, JPanel cards,  GUIWordEditor campo){
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(1200, 800));
        setOpaque(false);
        initComponentes(principal, cards, campo);
    }
    
    
    public void initComponentes(CardLayout principal, JPanel cards, GUIWordEditor campo){
        InitBarra(principal,  cards);
        Inicializarbotones(campo,principal,  cards);
        
        
    }
    
    
    private void InitBarra(CardLayout principal, JPanel cards){
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 1200));
        panel.setMinimumSize(new Dimension(300, 1200));
        panel.setMaximumSize(new Dimension(300, 1200));
        panel.setOpaque(true);
        panel.setBackground(Color.blue);
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
        archivo.setOpaque(false);
        archivo.setVisible(false);

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
        Nuevo.setOpaque(false);

        Nuevo.setHorizontalAlignment(SwingConstants.LEFT);

        Nuevo.addActionListener(e -> {

        });
        
        JButton Guardar = new JButton("Guardar");

        Guardar.setFont(new Font("Arial", Font.BOLD, 14));
        Guardar.setPreferredSize(new Dimension(200, 35));
        Guardar.setMinimumSize(new Dimension(200, 35));
        Guardar.setMaximumSize(new Dimension(200, 35));
        

        Guardar.setForeground(Color.white);
        Guardar.setOpaque(false);

        Guardar.setFocusPainted(false);
        Guardar.setBorderPainted(false);
        Guardar.setContentAreaFilled(false);
        Guardar.setOpaque(false);

        Guardar.setHorizontalAlignment(SwingConstants.LEFT);

        Guardar.addActionListener(e -> {

        });
        JButton Guardarc = new JButton("Guardar como");

        Guardarc.setFont(new Font("Arial", Font.BOLD, 14));
        Guardarc.setPreferredSize(new Dimension(200, 35));
        Guardarc.setMinimumSize(new Dimension(200, 35));
        Guardarc.setMaximumSize(new Dimension(200, 35));
        

        Guardarc.setForeground(Color.white);
        Guardarc.setOpaque(false);

        Guardarc.setFocusPainted(false);
        Guardarc.setBorderPainted(false);
        Guardarc.setContentAreaFilled(false);
        Guardarc.setOpaque(false);

        Guardarc.setHorizontalAlignment(SwingConstants.LEFT);

        Guardarc.addActionListener(e -> {

        });
        
        JButton Cargar = new JButton("Cargar");

        Cargar .setFont(new Font("Arial", Font.BOLD, 14));
        Cargar .setPreferredSize(new Dimension(200, 35));
        Cargar .setMinimumSize(new Dimension(200, 35));
        Cargar .setMaximumSize(new Dimension(200, 35));
        

        Cargar .setForeground(Color.white);
        Cargar .setOpaque(false);

        Cargar .setFocusPainted(false);
        Cargar .setBorderPainted(false);
        Cargar .setContentAreaFilled(false);
        Cargar .setOpaque(false);

        Cargar .setHorizontalAlignment(SwingConstants.LEFT);

        Cargar .addActionListener(e -> {

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
    
   private void Inicializarbotones(GUIWordEditor campo, CardLayout principal, JPanel cards){
        JPanel Panelenvuelto =new JPanel(new GridBagLayout());
        Panelenvuelto.setOpaque(false);
        
        JLabel label = new JLabel("Ingresa Nuevo Nombre");

        label.setFont(new Font("Arial", Font.BOLD, 35));
        label.setPreferredSize(new Dimension(400, 100));
        label.setMaximumSize(new Dimension(400, 100));
        label.setMinimumSize(new Dimension(400, 100));
        
        label.setForeground(Color.blue);
        label.setOpaque(false);

       

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);


        JTextField nombre = new JTextField(" ");

        nombre .setFont(new Font("Arial", Font.BOLD, 14));
        nombre .setPreferredSize(new Dimension(500, 50));
        nombre .setMaximumSize(new Dimension(500, 50));
         nombre .setMinimumSize(new Dimension(500, 50));

        nombre .setForeground(Color.black);
  

      
        nombre .setOpaque(false);
        nombre.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        nombre .setHorizontalAlignment(SwingConstants.CENTER);
        nombre .setAlignmentX(Component.CENTER_ALIGNMENT);

       
        
        
        JButton boton3 = new JButton("Crear");

        boton3.setFont(new Font("Arial", Font.BOLD, 14));
        boton3.setPreferredSize(new Dimension(500, 50));
        boton3.setMaximumSize(new Dimension(500, 50));
        boton3.setMinimumSize(new Dimension(500, 50));
        boton3.setForeground(Color.WHITE);
        boton3.setBackground(Color.blue);

        boton3.setFocusPainted(false);
        boton3.setBorderPainted(false);
        boton3.setContentAreaFilled(false);
        boton3.setOpaque(true);

        boton3.setHorizontalAlignment(SwingConstants.CENTER);
        boton3.setAlignmentX(Component.CENTER_ALIGNMENT);

        boton3.addActionListener(e -> {
            
            if (!nombre.getText().trim().equals("")){
            campo.ingresarContenido(null, nombre.getText().trim());
            nombre.setText("");
            archivo.setVisible(true);
            
             principal.show(cards, "editor");
            }
        });
        
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
        Panelenvuelto.add(panel);
        add (Panelenvuelto);
        
    }
    
    
}
