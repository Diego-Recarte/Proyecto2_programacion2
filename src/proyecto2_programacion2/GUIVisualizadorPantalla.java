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

public class GUIVisualizadorPantalla extends JDialog {

    private CardLayout cardLayout;
    private JPanel panelCards;

    private GUIVisualizadorPrincipal principalPanel;
    private GUIVisualizadorMenu menuPanel;

    public GUIVisualizadorPantalla(GUIPantallaPrincipal Perfil, File imagen) {
        super(Perfil, "Visualizador", false);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);
        setLocationRelativeTo(Perfil);

        initCardLayout();
        agregarCards(imagen);
        mostrarCard("principal");

        setVisible(true);
    }

    public void agregarCards(File imagen) {
        principalPanel = new GUIVisualizadorPrincipal(this, cardLayout, panelCards, imagen);
        agregarCard(principalPanel, "principal");

        menuPanel = new GUIVisualizadorMenu(this, cardLayout, panelCards, principalPanel.getCarpetaActual(), principalPanel.getArchivos(), principalPanel);
        agregarCard(menuPanel, "menu");
        
         
    }

    public void initCardLayout() {
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

    public void actualizarMenu(File carpeta, File[] archivos) {
        menuPanel.actualizarContenido(carpeta, archivos);
    }

    public GUIVisualizadorPrincipal getPrincipalPanel() {
        return principalPanel;
    }

    public GUIVisualizadorMenu getMenuPanel() {
        return menuPanel;
    }
}
