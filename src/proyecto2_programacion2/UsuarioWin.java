/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_programacion2;

import Logica.RutasSistema;

/**
 *
 * @author denam
 */
import java.io.File;
import java.util.*;
import java.io.IOException;
import java.io.Serializable;

public class UsuarioWin implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private char [] password;
    private File misDocumentos ;
    private File musica ;
    private File misImagenes ;
    private File ubicacion;
    private boolean isActivo;
    private boolean isAdmin;
 
            
            
    
    public UsuarioWin(String nombre, char [] password, boolean isAdmin){
        this.nombre= nombre;
        this.password= String.valueOf(password).trim().toCharArray();
        isActivo = true;
        this.isAdmin=isAdmin;
        
        
        ubicacion= RutasSistema.usuario(this.nombre);
        misDocumentos = RutasSistema.documentos(this.nombre);
        musica = RutasSistema.musica(this.nombre);
        misImagenes = RutasSistema.imagenes(this.nombre);
        
        
        
        ubicacion.mkdirs();
        misDocumentos.mkdirs();
        musica.mkdirs();
         misImagenes.mkdirs();
        
        
       
        
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isIsActivo() {
        return isActivo;
    }

    public boolean isIsAdmin() {
        return isAdmin;
    }

    public char[] getPassword() {
        return password;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
