/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica.Ventanas;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;

/**
 *
 * @author David
 */
public class genFondos extends JDesktopPane{
    private Image imagenFondo;
    
    public genFondos(String rutaImagen){
        try{
            this.imagenFondo= new ImageIcon(rutaImagen).getImage();
        }catch(Exception e){
            System.out.println("Error al cargar la imagen de fondo");
        }
    }
    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        
        if(imagenFondo!= null){
            g.drawImage(imagenFondo, 0,0,getWidth(), getHeight(), this);
        }
        
    }
}
