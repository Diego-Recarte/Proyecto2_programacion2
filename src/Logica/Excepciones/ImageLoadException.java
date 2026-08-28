/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica.Excepciones;

/**
 *
 * @author esteb
 */
public class ImageLoadException extends Exception {
    public ImageLoadException(String message) {
        super(message);
    }
    public ImageLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
