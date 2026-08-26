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
public class GUIReproductorMenu extends JPanel{
    
    
    private JButton archivo;
    public GUIReproductorMenu(GUIpantallaWord padre, CardLayout principal, JPanel cards,  GUIWordEditor campo){
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(500, 800));
        setOpaque(false);
        initComponentes(principal, cards, campo);
    }
    
    
    public void initComponentes(CardLayout principal, JPanel cards, GUIWordEditor campo){
        InitBarra(principal,  cards);
   
        
        
    }
    
    
    private void InitBarra(CardLayout principal, JPanel cards){
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
