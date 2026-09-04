/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica.ManejoUsuarios;

/**
 *
 * @author David
 */
public class UserLogged {
    private static UserLogged instance;
    private User userLogged;
    
    private UserLogged() {};
    
    
    
    public static UserLogged getInstance(){
        if(instance==null){
            instance= new UserLogged();
        }
        
        return instance;
    }
    
    public void setUserLogged(User usuario){
        this.userLogged= usuario;
    }
    
    
    public User getUserLogged(){
        return this.userLogged;
    }
}
