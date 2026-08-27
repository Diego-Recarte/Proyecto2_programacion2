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

public class PanelCMD extends JDialog {
    
    private JPanel panel;
    private JTextArea consola;
    private JTextField entrada;
    
    public PanelCMD(GUIPantallaPrincipal Perfil){
        super(Perfil, "CMD", false);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);
        setLocationRelativeTo(Perfil);

        InitPanel();
        setVisible(true);
    }
    
    public void InitPanel(){
        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 100));
        panel.setBackground(Color.BLACK);
        
        
        consola = new JTextArea();
        consola.setEditable(false);
        consola.setFont(new Font("Consolas", Font.PLAIN, 14));
        consola.setForeground(Color.GREEN);
        consola.setBackground(Color.BLACK);
        consola.setCaretColor(Color.WHITE);
        consola.setLineWrap(true);
        consola.setWrapStyleWord(true);
        consola.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        consola.append(usuarioWinActivo.nombre + " Win64 ~" + "\n");// poner el usuario de la persona
        
        
        JScrollPane scroll = new JScrollPane(consola);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBackground(Color.BLACK);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.BLACK);    

        
        
        panel.add(scroll, BorderLayout.CENTER);
        
        
        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBackground(Color.BLACK);
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        
        JLabel lblUsuario = new JLabel("$ ");
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        entrada = new JTextField();
        
        entrada.setBackground(Color.BLACK);
        entrada.setForeground(Color.WHITE);
        entrada.setCaretColor(Color.WHITE);
        entrada.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        
        entrada.addActionListener(e -> {
            String comando = entrada.getText().trim();
            
            if (!comando.isEmpty()) {
                
                consola.append("$ " + comando + "\n");
                entrada.setText("");
                consola.setCaretPosition(consola.getDocument().getLength());
                
                consola.append(usuarioWinActivo.nombre + " Win64 ~" + "\n");// poner el usuario de la persona
                
            }
        });
        
        panelEntrada.add(lblUsuario, BorderLayout.WEST);
        panelEntrada.add(entrada, BorderLayout.CENTER);
        
        panel.add(panelEntrada, BorderLayout.SOUTH);
        
        add(panel, BorderLayout.CENTER);
    }
    
    public void agregatTexto(String texto){
        consola.append("CMD: " + texto + "\n");
        consola.setCaretPosition(consola.getDocument().getLength());
        consola.append(" ");
       
    }
    
   
}