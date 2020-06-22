/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client_voice;

/**
 *
 * @author sylvia
 */
public class Client_voice {

    
    public static boolean calling = false;

    public static void main(String[] args) {
        
        client_fr fr = new client_fr();
        fr.setVisible(true);
        fr.setResizable(false);
    }
    
}
