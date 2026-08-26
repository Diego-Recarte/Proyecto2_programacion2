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
public class Buscador extends JDialog {
    
    
    
    Buscador(GUIPantallaPrincipal Perfil){
        
    
    super(Perfil, "Buscador", false);


        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 800);
      setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.black);
        setLocationRelativeTo(Perfil);
        
        InitBarra();
        
        InitCardLayout();
        agregarCards();
        mostrarCard("nuevo");
        
        
        setVisible(true);
    }
    
    public void Initbarra(){
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(200, 750));
        panel.setMinimumSize(new Dimension(200, 750));
        panel.setMaximumSize(new Dimension(200, 750));
        panel.setOpaque(true);
        panel.setBackground(Color.black);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        
        
         
        
        add(panel);

        

    
    }
}
