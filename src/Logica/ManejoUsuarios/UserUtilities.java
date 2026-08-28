/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica.ManejoUsuarios;
import java.io.File;
import java.io.IOException;
/**
 *
 * @author David
 */
public class UserUtilities {
      private String name;
    private String password;
    private String typ;
    private boolean status;
    private String UserRoute;
    private File UserMainDir;
    
    public UserUtilities(String name, String password){
        this.name=name;
        this.password=password;
        this.status=status;
        UserRoute="src\\Z\\Usuarios\\"+name;
        UserMainDir = new File(UserRoute);
    }
    
    
    //setters
    public void setTyp(String typ){
        this.typ=typ;
    }
    
    public void setName(String name){
        this.name=name;
    }
    
    public void setPassword(String password){
        this.password=password;
    }
    
    public void changStatus(){
        if(status){
            status=false;
        }
        
    }
    
    
    //getters
    public String getName(){
        return name;
    }
    
    public String getPassword(){
        return password;
    }
    
    public String typ(){
        return typ;
    }
    
    public boolean getStatus(){
        return status;
    }
    
    public String getUserRoute(){
        return UserRoute;
    }
    
    public boolean isAdmin(){
        if(this.name.equalsIgnoreCase("ADMIN") || this.name.equalsIgnoreCase("ADMIN2")){
            return true;
        }
        return false;
    }
            
    
    
    public void createInitUserDir() throws IOException{
        File userFile = new File(UserRoute);
        if(userFile.exists()){
            System.out.println("No hay necesidad de crear archivos para "+name);
        }else{
            userFile.mkdir();
        };
    }
    
    public  void createInicialDirs(){
        File myMusic= new File(UserRoute,"Musica");
        File myDocs = new File(UserRoute,"Mis Documentos");
        File myImages = new File(UserRoute, "Mis Imagenes");
        
        if(myMusic.exists()){
            System.out.println("YA EXISTE MUSIC PARA "+name);
        }else{
            myMusic.mkdir();
        }
        
        if(myDocs.exists()){
            System.out.println("YA EXISTE DOCS PARA "+name);
        }else{
            myDocs.mkdir();
        }
        if(myImages.exists()){
            System.out.println("YA EXISTE IMAGES PARA "+name);
        }else{
            myImages.mkdir();
        }
        
    }
    
    
    
}
