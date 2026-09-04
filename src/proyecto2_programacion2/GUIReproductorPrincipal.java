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
import java.io.*;
// "src/datos/windows/Z/imagenes/windows/noImagen.jpg"

public class GUIReproductorPrincipal extends JPanel {
    private musica cancion;
    private JLabel foto;
    private JTextArea areaDescripcion;
    private JLabel autor;
    
    private JLabel titulo ;
    public GUIReproductorPrincipal(GUIReproductor padre,File archivoMP5 ){
        try{
            cancion = abrirMP5(archivoMP5);
        }catch(IOException e){
            
        }catch (ClassNotFoundException e){
            
        }
                
                
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(400, 800));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initBarraedit();
        initMusica();
        

        
        
        
        


        
        
    }
    private void initBarraedit(){
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setPreferredSize(new Dimension(400, 100));
        panel.setBackground (Color.GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton boton = new JButton("Editar");
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setPreferredSize(new Dimension(120, 35));
        boton.setMaximumSize(new Dimension(120, 35));

        boton.setForeground(Color.BLACK);
        boton.setOpaque(false);

        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);

        boton.setHorizontalAlignment(SwingConstants.CENTER);

        boton.addActionListener(e -> {

        });
        
        add(panel);

        
        

    }
    
    
    private void initMusica(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(400, 100));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        titulo = new JLabel(cancion.getNombre());
        titulo.setFont(new Font("Arial", Font.BOLD, 15));
        titulo.setForeground(Color.BLACK);
        titulo.setOpaque(false);

        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setPreferredSize(new Dimension(150, 30));
        
        autor= new JLabel(cancion.getArtista());
        autor.setFont(new Font("Arial", Font.BOLD, 9));
        autor.setForeground(Color.BLACK);
        autor.setOpaque(false);

        autor.setHorizontalAlignment(SwingConstants.CENTER);
        autor.setPreferredSize(new Dimension(150, 30));
        
        
        
        foto = new JLabel();
        foto.setFont(new Font("Arial", Font.BOLD, 14));
        foto.setOpaque(false);
        foto.setForeground(Color.white);
        foto.setHorizontalAlignment(SwingConstants.CENTER);
        foto.setPreferredSize(new Dimension(350, 450));
        foto.setMinimumSize(new Dimension(350, 450));
        foto.setMaximumSize(new Dimension(350, 450));
        if (cancion.getCaratula().length==0){
            ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("src/datos/windows/Z/imagenes/windows/noImagen.jpg"));
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(350, 450, Image.SCALE_SMOOTH);
            ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);
            foto.setIcon(iconoEscalado);
        }else{
            ImageIcon iconoOriginal = cancion.obtenerCaratulaComoIcono();
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(350, 450, Image.SCALE_SMOOTH);
            ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);
            foto.setIcon(iconoEscalado);
        }
        
        
        areaDescripcion = new JTextArea();
        areaDescripcion.setFont(new Font("Arial", Font.BOLD, 9));
        areaDescripcion.setText(cancion.getDescripcion());
        areaDescripcion.setPreferredSize(new Dimension (330,200));
        areaDescripcion.setMinimumSize(new Dimension (330,200));
        areaDescripcion.setMaximumSize(new Dimension (330,200));
        areaDescripcion.setEditable(false);
        areaDescripcion.setLineWrap(true);
        areaDescripcion.setWrapStyleWord(true);
        areaDescripcion.setBackground(Color.BLACK);
        areaDescripcion.setForeground(Color.WHITE);
        
        
        
        JPanel panelStart = new JPanel();
        panelStart.setPreferredSize(new Dimension(380, 100));
        panelStart.setMinimumSize(new Dimension(380, 100));
        panelStart.setMaximumSize(new Dimension(380, 100));
        panelStart.setOpaque(false);
        
        
        JButton atras = new JButton("<");

        atras.setFont(new Font("Arial", Font.BOLD, 14));
        atras.setPreferredSize(new Dimension(100, 100));
        atras.setMaximumSize(new Dimension(100, 100));
        atras.setMinimumSize(new Dimension(100, 100));
        atras.setForeground(Color.WHITE);
        atras.setFocusPainted(false);
        atras.setBorderPainted(false);
        atras.setContentAreaFilled(false);
        atras.setOpaque(false);

        atras.setHorizontalAlignment(SwingConstants.CENTER);

        atras.addActionListener(e -> {

        });
        
        JButton adelante = new JButton(">");

        adelante .setFont(new Font("Arial", Font.BOLD, 14));
        adelante.setForeground(Color.WHITE);
        adelante .setPreferredSize(new Dimension(100, 100));
        adelante .setMaximumSize(new Dimension(100, 100));
        adelante .setMinimumSize(new Dimension(100, 100));
        adelante .setFocusPainted(false);
        adelante .setBorderPainted(false);
        adelante .setContentAreaFilled(false);
        adelante .setOpaque(false);

        adelante .setHorizontalAlignment(SwingConstants.CENTER);

        adelante .addActionListener(e -> {

        });
        
        JButton start = new JButton();

        start .setFont(new Font("Arial", Font.BOLD, 14));
        
        start .setPreferredSize(new Dimension(100, 100));
        start .setMaximumSize(new Dimension(100, 100));
        start .setMinimumSize(new Dimension(100, 100));
        start .setFocusPainted(false);
        start .setBorderPainted(false);
        start.setContentAreaFilled(false);
        start .setOpaque(false);

        start .setHorizontalAlignment(SwingConstants.CENTER);

        start .addActionListener(e -> {

        });
        
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("src/datos/windows/Z/imagenes/windows/start.png"));
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);
        start.setIcon(iconoEscalado);

        panelStart.add(atras, BorderLayout.WEST);
        panelStart.add(start, BorderLayout.CENTER);
        panelStart.add(adelante, BorderLayout.EAST);
        
        JPanel panelBarra = new JPanel();
        panelBarra.setPreferredSize(new Dimension (350,100));
        panelBarra.setMinimumSize(new Dimension (350,100));
        panelBarra.setMaximumSize(new Dimension (350,100));
        
        panel.add(titulo);
        panel.add(autor);
        panel.add(foto);
        panel.add(areaDescripcion);
        panel.add(panelStart);
        panel.add(panelBarra);
        
        add(panel);
        
        
        
           
        
        

    }
    
    
    
    
    public static musica abrirMP5(File archivoMP5) throws IOException, ClassNotFoundException {

        try (FileInputStream fis = new FileInputStream(archivoMP5);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (musica) ois.readObject();
        }
    }
    
}
