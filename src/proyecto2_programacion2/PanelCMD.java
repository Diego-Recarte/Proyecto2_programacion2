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
    private CMDControlador controlador = new CMDControlador ();
    private JPanel panelEditor;
    private JTextArea areaEditor;
    private JLabel editor;
    private JPanel contenedorScroll;
    private JPanel panelEntrada;
  
   
    
    



    public PanelCMD(GUIPantallaPrincipal Perfil){
    super(Perfil, "CMD", false);

    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setSize(1000, 500);
    setLayout(new BorderLayout(10, 10));
    getContentPane().setBackground(Color.BLACK);
    setLocationRelativeTo(Perfil);
     crearPanelEditor();
    InitPanel();
    setVisible(true);
    }

    
    private void crearPanelEditor() {

        panelEditor = new JPanel(new BorderLayout());
        panelEditor.setBackground(Color.BLACK);
        panelEditor.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        editor = new JLabel(" EDITOR ");
        editor.setForeground(Color.GREEN);
        editor.setBackground(Color.BLACK);
        editor.setOpaque(true);
        editor.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

        panelEditor.add(editor, BorderLayout.NORTH);

        areaEditor = new JTextArea();

        areaEditor.setBackground(Color.BLACK);
        areaEditor.setForeground(Color.GREEN);
        areaEditor.setCaretColor(Color.WHITE);
        areaEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        areaEditor.setLineWrap(true);
        areaEditor.setWrapStyleWord(false);

        areaEditor.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JScrollPane scrollEditor = new JScrollPane(areaEditor);

        scrollEditor.setBorder(null);
        scrollEditor.setBackground(Color.BLACK);
        scrollEditor.getViewport().setBackground(Color.BLACK);

        panelEditor.add(scrollEditor,BorderLayout.CENTER);

        JLabel lblAyuda =new JLabel(" ESC para cancelar | EXIT para guardar ");
        lblAyuda.setForeground(Color.WHITE);
        lblAyuda.setBackground(Color.BLACK);
        lblAyuda.setOpaque(true);
        lblAyuda.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panelEditor.add(lblAyuda,BorderLayout.SOUTH);
        panelEditor.setPreferredSize(new Dimension(1000, 450));
        panelEditor.setMinimumSize(new Dimension(1000, 450));
        panelEditor.setMaximumSize(new Dimension(1000, 450));
    }
    
    public void InitPanel(){
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(1000, 500));
        panel.setMinimumSize(new Dimension(1000, 500));
        panel.setMaximumSize(new Dimension(1000, 500));
        panel.setBackground(Color.BLACK);
        
        
        consola = new JTextArea();
        consola.setEditable(false);
        consola.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        consola.setForeground(Color.GREEN);
        consola.setBackground(Color.BLACK);
        consola.setCaretColor(Color.WHITE);
        
        consola.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        consola.append(usuarioWinActivo.nombre+ "~ "+  desdePalabra(controlador.getRutaActual(),"Z"));// ////////////////////////////////////////////marca de agua

        
        
        JScrollPane scroll = new JScrollPane(consola);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.BLACK);    
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBackground(Color.BLACK);
        scroll.getViewport().setBackground(Color.BLACK);
        scroll.getVerticalScrollBar().setBackground(Color.BLACK);
        scroll.getHorizontalScrollBar().setBackground(Color.BLACK);

        contenedorScroll = new JPanel(new BorderLayout()){
            @Override
            public Dimension getMaximumSize(){
                return getPreferredSize();
            }
        };
        contenedorScroll.setBackground(Color.BLACK);
        contenedorScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedorScroll.setPreferredSize(new Dimension(1000, 20));
        contenedorScroll.setMinimumSize(new Dimension(1000, 20));
      
        contenedorScroll.add(scroll, BorderLayout.CENTER);

        
        
        panel.add(contenedorScroll);
        
        
        panelEntrada = new JPanel(new BorderLayout()){
            @Override
            public Dimension getMaximumSize(){
                return getPreferredSize();
            }
        };
        panelEntrada.setBackground(Color.BLACK);
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        panelEntrada.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelEntrada.setPreferredSize(new Dimension(1000, 20));
        panelEntrada.setMinimumSize(new Dimension(1000, 20));
        panelEntrada.setMaximumSize(new Dimension(1000, 20));
        
        
        JLabel lblUsuario = new JLabel("$ ");
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        
        entrada = new JTextField();
        
        entrada.setBackground(Color.BLACK);
        entrada.setForeground(Color.WHITE);
        entrada.setCaretColor(Color.WHITE);
        entrada.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
         
        
        entrada.addActionListener(e -> {
            String comando = entrada.getText().trim();
            
            if (!comando.isEmpty()) {
                
                consola.append("\n$ " + comando + "\n"); 
                //llamar a logica//////////////////////////////////////////////////////////////////////////////
                boolean continuar = realizarAccion();
                
                entrada.setText("");
                if(!continuar){
                    return;
                }
                consola.setCaretPosition(consola.getDocument().getLength());
                consola.append(" " + "\n");
                consola.append( usuarioWinActivo.nombre+"~ "+ desdePalabra(controlador.getRutaActual(),"Z"));
                AjustarTamaño(contenedorScroll);
                entrada.requestFocusInWindow();


                
               
                
                
            }
        });
        
        panelEntrada.add(lblUsuario, BorderLayout.WEST);
        panelEntrada.add(entrada, BorderLayout.CENTER);
        
        panel.add(panelEntrada);

        
        add(panel, BorderLayout.CENTER);

        panel.revalidate();
        panel.repaint();
    }
    
    
    private void AjustarTamaño(JPanel scroll){
        int lineas = consola.getDocument().getDefaultRootElement().getElementCount();
        
        int tamañoPixeles= lineas *20;
        
        if (tamañoPixeles> 430){
            tamañoPixeles=430;
            
        }
        
        scroll.setPreferredSize(new Dimension(1000, tamañoPixeles));
         scroll.setMinimumSize(new Dimension(1000, tamañoPixeles));
        
        panel.repaint();
        panel.revalidate();
        
    }
    
    public void agregartTexto(String texto){
        
        
        
        
        consola.append( texto + "\n");
        consola.setCaretPosition(consola.getDocument().getLength());
        consola.append(" ");
      
       
       
    }
    
   
    
   
    private boolean realizarAccion(){
        String texto = controlador.getInterprete().ejecutar(entrada.getText());
        String comando = entrada.getText().trim();
       
        if(!texto.isEmpty()){
            consola.append(texto + "\n");
        }
        return true;
    }
    public static String desdePalabra(String texto, String palabra) {
        int posicion = texto.indexOf(palabra);

        if (posicion == -1) {
            return texto; 
        }

        return texto.substring(posicion);
    }
    
}

