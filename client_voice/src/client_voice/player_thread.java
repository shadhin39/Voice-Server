
package client_voice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * 
 */
public class player_thread extends Thread {
    public DatagramSocket din;
    public SourceDataLine audio_out;
    byte[] buffer = new byte [512];
    
    @Override
    public void run() {
        
        int i = 0;
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        while (Client_voice.calling) {
            try {
                din.receive(incoming);
                buffer = incoming.getData();
                audio_out.write(buffer, 0, buffer.length);
                System.out.println("#there" + (i++));
            } catch (IOException ex) {
                System.out.println("Your socket has been closed");
                break;
            }
        }
        audio_out.close();
        audio_out.drain();
        System.out.println("STOP");
    }
}
