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
public class GUIPantallaPrincipal extends JFrame {
    private CardLayout cardLayout;
    private JPanel panelCards;

    private GUILogin login;
    private GUIEscritorio escritorio;
    private GUICrearUsuarios crear;

    public GUIPantallaPrincipal() {
        super("Windows +");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        setLocationRelativeTo(null);
        setUndecorated(true);

        InitCardLayout();
        agregarCards();

        ArchivoUsuarioWin archivo = new ArchivoUsuarioWin();
        if (!archivo.existeAdmin()) {
            mostrarCard("crear");
        } else {
            mostrarCard("login");
        }

        setVisible(true);
    }

    public void agregarCards() {
        login = new GUILogin(this, cardLayout, panelCards);
        agregarCard(login, "login");

        escritorio = new GUIEscritorio(this, cardLayout, panelCards);
        agregarCard(escritorio, "escritorio");

        crear = new GUICrearUsuarios(this, cardLayout, panelCards);
        agregarCard(crear, "crear");
    }

    public void InitCardLayout() {
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

    public void mostrarEscritorio() {
        if (escritorio != null) {
            escritorio.actualizarUsuarioActivo();
        }
        mostrarCard("escritorio");
    }
}