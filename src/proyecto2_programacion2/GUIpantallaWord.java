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
import javax.swing.text.*;
public class GUIpantallaWord extends JDialog{
    private CardLayout cardLayout;
    private JPanel panelCards;
    private GUIWordEditor editor;
    private GUIWordNuevo nuevo;
    private GUIWordGuardarComo guardarComo;
    public GUIpantallaWord  (GUIPantallaPrincipal Perfil,File archivo, boolean isbuscador){
        super(Perfil, "Word", false);


        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
      setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        setLocationRelativeTo(Perfil);
        InitCardLayout();
        agregarCards();
        
        if (isbuscador){
            nuevo.archivo.setVisible(true);
             nuevo.Guardar.setVisible(true);
             nuevo.Guardarc.setVisible(true);
             
             guardarComo.archivo.setVisible(true);
             guardarComo.Guardar.setVisible(true);
             guardarComo.Guardarc.setVisible(true);
             
             try{
                WordArchivos.abrir(editor.label, editor.editor, archivo);
                mostrarCard("editor");
             }catch(IOException e){
                 
             }catch (BadLocationException e){
                 
             }

        }else{
            mostrarCard("nuevo");
        }
        
        
        setVisible(true);
        
       

    }
     public void agregarCards(){
       editor = new GUIWordEditor(this, cardLayout, panelCards);
        agregarCard(editor, "editor");
        
        nuevo = new GUIWordNuevo (this, cardLayout, panelCards, editor);
        agregarCard(nuevo, "nuevo");
        
         guardarComo= new GUIWordGuardarComo(this, cardLayout, panelCards,  editor);
         agregarCard(guardarComo, "guardarComo");
        
        
        
        
        

        
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
