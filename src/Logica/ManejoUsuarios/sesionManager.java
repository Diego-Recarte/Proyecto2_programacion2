/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica.ManejoUsuarios;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 *
 * @author David
 */
public class sesionManager {
    
    //Metodo para inicio de sesion
    public static boolean LogIn (String username, String password, JPasswordField passwordfield, JFrame screen){
        
        ArrayList<User> usuarios = UsuariosControlador.getInstance().getUsuarios();
        
        for(int i=0; i<usuarios.size(); i++){
            User user= usuarios.get(i);
            try{
              if(user.getStatus()==true){
                  //Check de username
                  if(user.getName().equals(username)){
                      //check de password
                      if(user.getPassword().equals(password)){
                          //JOptionPane.showMessageDialog(null, "INICIANDO SESION");
                          UserLogged.getInstance().setUserLogged(user);
                          System.out.println("Iniciando sesion como : "+user.getName());
                          return true;
                      }else if(password.equalsIgnoreCase("") || password.equalsIgnoreCase(" ")){
                          JOptionPane.showMessageDialog(screen, "CAMPO DE CONTRASEÑA VACIO");
                      }else{
                          JOptionPane.showMessageDialog(screen, "CONTRASEÑA INCORRECTA");
                      }
                  }else{
                      JOptionPane.showMessageDialog(screen, "USUARIO NO EXISTE");
                  }
              }
            }catch(NullPointerException e){
                
            }
        }
        
        return false;
    }
    
    
    public static boolean LogIn2(String name, String password, JPasswordField passwordfield, JFrame screen){
        
        try{
            boolean statusUser= UserManager.getStatus(name);
            //check existencia de usuario
            if(statusUser){
                //check si el nombre coincide
                String nameUser = UserManager.getName(name);
                
                //aparte de decirnos que si se encuentra ese nombre de usuario, nos enseña que en efecto si se encuentra en la lista
                if(nameUser!=null){
                    String pass= UserManager.getPass(nameUser);
                    
                    //check de constrasenia
                    if(pass.equals(password)){
                        
                        //Creacion de mecanica de clase auxiliar para mantener informacion de usuario logeado
                        User logged= new User(nameUser, pass);
                        UserLogged.getInstance().setUserLogged(logged);
                        System.out.println("Iniciando Sesion como: "+nameUser);
                        return true;
                    }else{
                        JOptionPane.showMessageDialog(screen, "Contraseña incorrecta");
                    }   
                }else{
                    JOptionPane.showMessageDialog(screen, "NOMBRE DE USUARIO NO ENCONTRADO");
                }   
            }else{
                JOptionPane.showMessageDialog(screen, "USUARIO NO EXISTE");
            }
        }catch(IOException e){
            System.out.println("Excepcion en login 2");
            e.printStackTrace();
        }
        
        return false;
    }
    
    
    
    //metodo llamado al iniciar programa
    public static void loadUsers() throws IOException{
        //Obtenemos referencia de ruta
        RandomAccessFile managerRoute = UserManager.getManagerRoute();
        
        
        //vamos a lo largo del archivo agregando usuarios como user al controlador de usuarios
        managerRoute.seek(0);
        while(managerRoute.getFilePointer()< managerRoute.length()){
            String name = managerRoute.readUTF();
            String pass = managerRoute.readUTF();
            boolean status = managerRoute.readBoolean();
            if(status){
                User newUser = new User(name, pass);
                UsuariosControlador.getInstance().getUsuarios().add(newUser);
                System.out.println("Usuario cargado...");
            }
            
        }        
    }
    
    
    public static boolean passwordCheck(JPasswordField passwordfield){
        //Verificadores 
        boolean ver1=false;//length
        boolean ver3=false;//charsespeciales
        boolean ver4=false;//no espacios
        boolean ver5=false;//verificacion de caracteres normales
        
        char[] contra= passwordfield.getPassword();
        int lengthpss = contra.length;
        
        
        //check de longitud
        if(lengthpss==5){
            ver1=true;   
        }
        
        //verificar que contenga caracteres especiales
        String specialChars ="!@#$%&*()'+,-./:;<=>?[]^_`{|}";
        for(int i=0; i<contra.length; i++){
            String stChar = Character.toString(contra[i]);
            if(specialChars.contains(stChar)){
                ver3=true;
                break;   
            }
        }
        
        
        
        //Verificar que no tiene espacios en blanco
        boolean checkSpaces=false;
        for(int i=0; i<contra.length; i++){
            char letra = contra[i];
            if(letra==' '){
                checkSpaces=true;
               // System.out.println("Se encontro un espacio en blanco");
                break;
            }
        }
        if(checkSpaces!=true){
            ver4=true;
        }
        
        
        
        //verificar que contenga caracter normal
        String abc="abcdefghijklmnñopqrstuvwxyz";
        String ABC="ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
        for(int i=0;i<contra.length; i++){
            String stChar = Character.toString(contra[i]);
            if(abc.contains(stChar) || ABC.contains(stChar)){
                ver5=true;
                break;
            }
        }
        
        
        //checkeo de cada una de las condiciones
        if(ver1==true && ver3==true && ver4==true && ver5==true){
            return true;
        }else{
             if(ver1==false){
                 JOptionPane.showMessageDialog(null, "La contraseña debe tener una longitud exact a de 5 caracteres");
             }
             
             if(ver3==false){
                 JOptionPane.showMessageDialog(null, "La contraseña debe contener por lo menos un caracter especial");
             }
             
             if(ver4==false){
                 JOptionPane.showMessageDialog(null, "La contraseña no puede contener espacios en blanco");
             }
             
             if(ver5==false){
                 JOptionPane.showMessageDialog(null, "La contraseña debe tener por lo menos una letra");
             }
            
        }
        
        return false;
    }
    
    
    //version que solo es con el string
    public static boolean passwordCheck(String password){
        //Verificadores 
        boolean ver1=false;//length
        boolean ver3=false;//charsespeciales
        boolean ver4=false;//no espacios
        boolean ver5=false;//verificacion de caracteres normales
        
        char[] contra= password.toCharArray();
        int lengthpss = contra.length;
        
        
        //check de longitud
        if(lengthpss==5){
            ver1=true;   
        }
        
        //verificar que contenga caracteres especiales
        String specialChars ="!@#$%&*()'+,-./:;<=>?[]^_`{|}";
        for(int i=0; i<contra.length; i++){
            String stChar = Character.toString(contra[i]);
            if(specialChars.contains(stChar)){
                ver3=true;
                break;   
            }
        }
        
        
        
        //Verificar que no tiene espacios en blanco
        boolean checkSpaces=false;
        for(int i=0; i<contra.length; i++){
            char letra = contra[i];
            if(letra==' '){
                checkSpaces=true;
               // System.out.println("Se encontro un espacio en blanco");
                break;
            }
        }
        if(checkSpaces!=true){
            ver4=true;
        }
        
        
        
        //verificar que contenga caracter normal
        String abc="abcdefghijklmnñopqrstuvwxyz";
        String ABC="ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
        for(int i=0;i<contra.length; i++){
            String stChar = Character.toString(contra[i]);
            if(abc.contains(stChar) || ABC.contains(stChar)){
                ver5=true;
                break;
            }
        }
        
        
        //checkeo de cada una de las condiciones
        if(ver1==true && ver3==true && ver4==true && ver5==true){
            return true;
        }else{
             if(ver1==false){
                 JOptionPane.showMessageDialog(null, "La contraseña debe tener una longitud exact a de 5 caracteres");
             }
             
             if(ver3==false){
                 JOptionPane.showMessageDialog(null, "La contraseña debe contener por lo menos un caracter especial");
             }
             
             if(ver4==false){
                 JOptionPane.showMessageDialog(null, "La contraseña no puede contener espacios en blanco");
             }
             
             if(ver5==false){
                 JOptionPane.showMessageDialog(null, "La contraseña debe tener por lo menos una letra");
             }
            
        }
        
        return false;
    }
    
    
    
    
    
    public static boolean userCheck(String username, int index, JFrame screen){
        if(index>UsuariosControlador.getInstance().getUsuarios().size()){
           return true;
       }
       
       User user = UsuariosControlador.getInstance().getUsuarios().get(index);
       
       if(user.getName().equalsIgnoreCase(username)){
           JOptionPane.showMessageDialog(screen, "El nombre de usuario ya existe, por favor utilice uno distinto");
           return false;
       }
       
       
       return userCheck(username, index+1, screen);
    }
    
    
    //version que solo es con el string
    public static boolean userCheck(String username){
        
        boolean comprobacion =false;
        
        for(User usuario: UsuariosControlador.getInstance().getUsuarios()){
            String nameUser = usuario.getName();
            if(nameUser.equalsIgnoreCase(username)){
                comprobacion=true;
            }
        }
        
        if(comprobacion){
            return true;
        }
       
       return false;
    }
    
    
    
    
    
}
