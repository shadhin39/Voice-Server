
package client_voice;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

public class MessageTransmitter extends Thread {
    
    String message, hostname;
    int port;
    WritableGUI gui;
    
    public MessageTransmitter() {}
    
    public MessageTransmitter (WritableGUI gui, String message, String hostname, int port) {
        this.gui = gui;
        this.message = message;
        this.hostname = hostname;
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("this hostname: " + hostname);
            Socket s = new Socket(hostname, port);
            s.getOutputStream().write(message.getBytes());
            s.close();
            
        } catch (IOException ex) {
            Logger.getLogger(MessageTransmitter.class.getName()).
                    log(Level.SEVERE, null, ex);
            gui.write("Your friend with ip: " + hostname + " "
                    + "is not online or maybe you've entered a wrong IP address!");
        }
    }
    
}
