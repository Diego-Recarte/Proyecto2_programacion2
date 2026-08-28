/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica.ManejoUsuarios;

import java.util.ArrayList;

/**
 *
 * @author David
 */
public class UsuariosControlador {
     private static UsuariosControlador instance;
    
    private ArrayList<User> usuarios;
    
    private UsuariosControlador(){};
    
    
    public static UsuariosControlador getInstance(){
        if(instance == null){
            instance = new UsuariosControlador();
        }
        
        return instance;
    }
    
    
    public void setUsuarios(ArrayList<User> usuarios){
        this.usuarios=usuarios;
    }
    
    public ArrayList<User> getUsuarios(){
        return this.usuarios;
    }
}
