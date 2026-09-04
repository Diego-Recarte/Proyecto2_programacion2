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
import javax.swing.text.*;
public class GUIWordGuardarComo extends JPanel{
    public JButton archivo;
    private Timer timer;
    private JLabel labele;
    public JButton Guardarc;
    public JButton Guardar ;
    
    
    
   public GUIWordGuardarComo(GUIpantallaWord padre, CardLayout principal, JPanel cards,  GUIWordEditor campo){
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(1200, 800));
        setOpaque(false);
        initTimer();
        initComponentes(principal, cards, campo, padre);
    }
   
   private void initTimer(){
       timer = new Timer(2000, ev->{
           labele.setText(" ");
       });
       timer.setRepeats(false);
   }
    
    
    public void initComponentes(CardLayout principal, JPanel cards, GUIWordEditor campo, GUIpantallaWord padre){
        try{
        InitBarra(campo, principal,  cards, padre);
        }catch(IOException e){
                
        }
        Inicializarbotones(campo,principal,  cards);
        
        
    }
    
    
    private void InitBarra(GUIWordEditor campo,CardLayout principal, JPanel cards, GUIpantallaWord padre) throws IOException{
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
        Guardar.setOpaque(false);

        Guardar.setHorizontalAlignment(SwingConstants.LEFT);

        Guardar.addActionListener(e -> {

         
           
            boolean resultado= false;
            try{
              resultado =WordArchivos.guardarComo(campo.editor, new File ("src/datos/windows/Z/infoUsuarios/"+usuarioWinActivo.nombre+"/misDocumentos/"+campo.label.getText().trim()+".wrd"),campo.label.getText(), false);
            }catch (IOException er){
                
            };
            
            if (resultado==true){
                principal.show(cards, "editor");
            }else{
                labele.setText("No se ha guardado un archivo con ese nombre");
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
            try {
                    File carpetaBase;
                    if (usuarioWinActivo.isAdmin){
                        
                     carpetaBase = new File( "src/datos/windows/Z/infoUsuarios" );
                    }else{
                     carpetaBase = new File( "src/datos/windows/Z/infoUsuarios/" + usuarioWinActivo.nombre + "/misDocumentos" );
                    }

                    GUISelector selector = new GUISelector(SwingUtilities.getWindowAncestor(this),carpetaBase,"wrd" );

                    selector.setVisible(true);

                    File archivoSeleccionado = selector.getArchivoSeleccionado();

                    if (archivoSeleccionado != null) {
                        WordArchivos.abrir(campo.label, campo.editor, archivoSeleccionado);

                        archivo.setVisible(true);
                        Guardar.setVisible(true);
                        Guardarc.setVisible(true);

                        principal.show(cards, "editor");
                    }

                } catch (IOException | BadLocationException ex) {
                    
                    
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
    
   private void Inicializarbotones(GUIWordEditor campo, CardLayout principal, JPanel cards){
        JPanel Panelenvuelto =new JPanel(new GridBagLayout());
        Panelenvuelto.setOpaque(false);
        
        JLabel label = new JLabel("Confirma el nombre");

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

       
        
        
        JButton boton3 = new JButton("Guardar");

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
            boolean resultado= false;
            try{
              resultado =WordArchivos.guardarComo(campo.editor, new File ("src/datos/windows/Z/infoUsuarios/"+usuarioWinActivo.nombre+"/misDocumentos/"+nombre.getText().trim()+".wrd"),nombre.getText(), true);
            }catch (IOException er){
                
            }
            nombre.setText("");
            
            if (resultado){
                principal.show(cards, "editor");
            }else{
                labele.setText("Ya existe un archivo con ese nombre");
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
        
        add (Panelenvuelto);
        
    }
    
    

}
