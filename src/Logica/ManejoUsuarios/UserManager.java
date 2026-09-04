/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica.ManejoUsuarios;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *CLASE REFERENTE A ARCHIVOS DE MANEJO DE USUARIOS
 * @author David
 */
public class UserManager {
    //Parametros importantes de usuario Logged
    //User route
    //user dir
    
    private static String userRoute;//ruta principal para crear folders de usuarios
    //public static File loggedDir;
    private File userMRoute; //ruta donde crear archivo de manejo de usuarios
    private static RandomAccessFile userManage; //archivo que maneja los distintos usuarios
    
    
    //constructor inicializa dichos parametros
    public UserManager(){
        userRoute="src\\Z\\Usuarios";
        try{
           userMRoute= new File("src\\Z");
           userManage= new RandomAccessFile(userMRoute+"\\users.patOS", "rw" ); //ver si esto no crashea
        }catch(IOException e){
            
        }
        
    }
    
    //add new user
    /*
        FORMATO
        STRING name
        STRING password
        BOOLEAN status
        
    */
    public static RandomAccessFile getManagerRoute(){
        return userManage;
    }
    
    public static void addUser(String name, String password) throws IOException{
        
        File route = new File(userRoute,name);
        if(!route.exists()){
            //Escritura de datos en archivo manager
            userManage.seek(userManage.length());
            userManage.writeUTF(name);
            userManage.writeUTF(password);
            userManage.writeBoolean(true);

            initUserDir(name);
        }    
    }
    
    private static void initUserDir(String name) throws IOException{
        File newUDir = new File(userRoute, name); //me ubico en carpeta de usuario
        if(!newUDir.exists()){
            newUDir.mkdir();
            
            //Inicializacion de carpetas por defecto del usuario
            File myMusic= new File(newUDir,"Musica");
            File myDocs = new File(newUDir,"Mis Documentos");
            File myImages = new File(newUDir, "Mis Imagenes");
            
            if(!myMusic.exists()){
                myMusic.mkdir();
            }
            
            if(!myDocs.exists()){
                myDocs.mkdir();
            }
            
            if(!myImages.exists()){
                myImages.mkdir();
            }
        }else{
            System.out.println("YA ESTABA CREADA LA COSA XD");
        }
    }
    
    
    
    
      //getters
    
    //aparte de devolver el nombre, nos sirve para comprobar su existencia
     public static String getName(String username) throws IOException{
        userManage.seek(0);
        while(userManage.getFilePointer()<userManage.length()){
            String nameGetter=userManage.readUTF();
            if(nameGetter.equals(username)){
                return nameGetter;
            }
            userManage.readUTF();
            userManage.readBoolean();
        }
        
        return null;//caso que dio -1 y es que no encontro al usuario
        
    }
    
     
     //metodo usado para comprobar que el username ya existe
     public static boolean checkUsername(String username) throws IOException{
        userManage.seek(0);
        while(userManage.getFilePointer()<userManage.length()){
            String nameGetter=userManage.readUTF();
            if(nameGetter.equalsIgnoreCase(username)){
                return true;
            }
            userManage.readUTF();
            userManage.readBoolean();
        }
        
        return false;
     }
    
    public static  String getPass(String username) throws IOException{
        userManage.seek(0);
        while(userManage.getFilePointer()<userManage.length()){
            String nameGetter=userManage.readUTF();
            String passGetter= userManage.readUTF();
            if(nameGetter.equals(username)){
                return passGetter;
            }
            userManage.readBoolean();
        }
        
        return null;//caso que dio -1 y es que no encontro al usuario
    }
    
    
    public static boolean getStatus(String username) throws IOException{
        userManage.seek(0);
        boolean getterStatus;
        while(userManage.getFilePointer()<userManage.length()){
            String nameGetter=userManage.readUTF();
            userManage.readUTF();
            getterStatus=userManage.readBoolean();
            if(nameGetter.equals(username)){
                return getterStatus;
            }
            
        }
        return false;//simplemente identificar como usuario no existente
    }
    
    
  //Setters----
    //NOTA> VER SI ESTO NO NOS ARRUINA EL CODIGO DESPUES, YA QUE AL CAMBIAR ALGO UTF SE ALTERAN LOS ESPACIOS DE LOS BYTES
  public static boolean changeName(String name, String newName) throws IOException{
        userManage.seek(0);
        long postemp;
        while(userManage.getFilePointer()<userManage.length()){
            postemp=userManage.getFilePointer();//obtengo la posicion
            userManage.seek(postemp);//vuelvo a la posicion del nombre
            String nameGetter=userManage.readUTF();
            if(nameGetter.equals(name)){
                userManage.seek(postemp);//devulevo a la posicion del nombre;
                userManage.writeUTF(newName);//cambiamos el nombre
                return true;
            }
            userManage.readUTF();
            userManage.readBoolean();
        }
        
        
        return false; //en caso de no completarse, es porque no existe usuario bajo el nombre 
  }
  
  
  public static boolean changePass(String name, String newPass) throws IOException{
      userManage.seek(0);
        long postemp;
        while(userManage.getFilePointer()<userManage.length()){
            postemp=userManage.getFilePointer();//obtengo la posicion
            userManage.seek(postemp);//vuelvo a la posicion del nombre
            String nameGetter=userManage.readUTF();
            if(nameGetter.equals(name)){
                userManage.writeUTF(newPass);
                return true;
            }
            userManage.readUTF();
            userManage.readBoolean();
        }
        
        
        return false; //en caso de no completarse, es porque no existe usuario bajo el nombre 
  }
  
  
  public static boolean changeStatus(String name, boolean newStatus) throws IOException{
      userManage.seek(0);
        long postemp;
        while(userManage.getFilePointer()<userManage.length()){
            postemp=userManage.getFilePointer();//obtengo la posicion
            userManage.seek(postemp);//vuelvo a la posicion del nombre
            String nameGetter=userManage.readUTF();
            if(nameGetter.equals(name)){
                userManage.readUTF();
                userManage.writeBoolean(newStatus);
                return true;
            }
            userManage.readUTF();
            userManage.readBoolean();
        }
        
        
        return false; //en caso de no completarse, es porque no existe usuario bajo el nombre 

  }
  
  
  public static boolean borrarFilesUser(String Username) throws IOException {
      File userRute = new File(userRoute, Username);
      
      if(userRute.isDirectory()){
            for(File arc:userRute.listFiles()){
                borrarAux(arc);
            }
        }
        return userRute.delete();
        
        //Una vez limpia todo el directorio, se procede a borrar la carpeta en si
  }
  
  
  private static boolean borrarAux(File mf) throws IOException{
      if(mf.isDirectory()){
            for(File arc:mf.listFiles()){
                borrarAux(arc);
            }
        }
        return mf.delete();
  }
  
  
  
  
  
  
  
   
    
  
    
}
