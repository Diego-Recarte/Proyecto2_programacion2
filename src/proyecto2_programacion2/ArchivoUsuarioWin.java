/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

/**
 *
 * @author denam
 */
import java.io.*;
import java.util.ArrayList;
import java.util.*;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
public class ArchivoUsuarioWin {
    private File archivo;
    
    public ArchivoUsuarioWin(){
        archivo = new File("src/datos/windows/Z/usuarios.sop");
        try{
         inicializarArchivo();
        }catch(IOException e){
            //***********************************************************mensaje de error
        }
    }
    public void inicializarArchivo() throws IOException {
        if (!archivo.exists()) {
            archivo.getParentFile().mkdirs();
      
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(archivo));
             out.writeObject(new ArrayList<UsuarioWin>());
             out.close();

           
        }
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<UsuarioWin> leerUsuarios() throws IOException, ClassNotFoundException {
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }

        ObjectInputStream in =new ObjectInputStream(new FileInputStream(archivo));

        ArrayList<UsuarioWin> lista = (ArrayList<UsuarioWin>) in.readObject();
        in.close();
      

        return lista;
    }
    
    private void guardarTodos(ArrayList<UsuarioWin> lista) throws IOException{
        
       try{ 
           ObjectOutputStream escribir = new ObjectOutputStream(new FileOutputStream(archivo));
           escribir.writeObject(lista);
           escribir.close();
       
               
        }catch(IOException e){
            
        }
    }
    
    
    public void agregarUsuario(UsuarioWin usuario) throws IOException, ClassNotFoundException {
        ArrayList<UsuarioWin> usuarios = leerUsuarios();
        usuarios.add(usuario);
        guardarTodos(usuarios);
    }
    
    public boolean UsuarioExiste (String usuario) throws IOException, ClassNotFoundException{
        
       
        ArrayList<UsuarioWin> lista = leerUsuarios();
        for (UsuarioWin u : lista){
            if (usuario.equals(u.getNombre())){
                return true;
            }
        }
        return false;

        
    }
    public int login (String nombre, JPasswordField contra){
        try{
            ArrayList<UsuarioWin> lista = leerUsuarios();

           for (UsuarioWin u : lista){
               if (nombre.equals(u.getNombre())){
                   
                   if (Arrays.equals(u.getPassword(), contra.getPassword())){
                    usuarioWinActivo.isActivo= u.isIsActivo();
                    usuarioWinActivo.nombre= u.getNombre();
                    usuarioWinActivo.isAdmin= u.isIsAdmin();
                    return 1;
                   }else{
                       return 2;
                   }
                   
                   
                   
               }
           }
        }catch(IOException e){
            
        }catch (ClassNotFoundException e){
            
        }
            
            
        return 3;
        
    }
    
    public boolean existeAdmin(){
        try{
            ArrayList<UsuarioWin> lista = leerUsuarios();

           for (UsuarioWin u : lista){
               if (u.isIsAdmin()){
                   
                 return true;
                   
                   
                   
               }
           }
        }catch(IOException e){
            
        }catch (ClassNotFoundException e){
            
        }
            
            
        return false;

    }
    
    
    
    
}
