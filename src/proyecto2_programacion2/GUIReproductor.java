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

public class GUIReproductor extends JDialog{
     private CardLayout cardLayout;
    private JPanel panelCards;
    
    
    
    GUIReproductor(GUIPantallaPrincipal Perfil){
        
    
    super(Perfil, "Reproductor", false);


        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 800);
      setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.black);
        setLocationRelativeTo(Perfil);
        
        
        InitCardLayout();
        agregarCards();
        mostrarCard("nuevo");
        setVisible(true);
        
       

    }
     public void agregarCards(){
        GUIWordEditor editor = new GUIWordEditor(this, cardLayout, panelCards);
        agregarCard(editor, "editor");
        
        GUIWordNuevo nuevo = new GUIWordNuevo (this, cardLayout, panelCards, editor);
        agregarCard(nuevo, "nuevo");
        
        
        
        
        
        

        
    }
    
    public void InitCardLayout(){
    cardLayout = new CardLayout(); 
    panelCards = new JPanel(cardLayout); 
    
    
    panelCards.setOpaque(false);
     
 
    getContentPane().add(panelCards, BorderLayout.CENTER);
    
    



    }
    private void agregarCard(JPanel panel, String nombre) {
        panelCards.add(panel, nombre);
    }
    public void mostrarCard(String nombreCard) { 
        cardLayout.show(panelCards, nombreCard); 
        panelCards.revalidate();
        panelCards.repaint(); 
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
