/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client_voice;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.text.DefaultCaret;
import sun.audio.AudioPlayer;

/**
 *
 * @author Sylvia
 */
public class client_fr extends javax.swing.JFrame implements WritableGUI {

    // chat application start
    MessageListener listener;
    int chat_recievePort = 8824;
    int chat_targetPort = 8823;

    public void chatListen() {
        listener = new MessageListener(this, (chat_recievePort));
        listener.start();
    }
    // ./chat application ends

    // file operations starts
    int file_recievePort = 9432;
    int file_targetPort = 9431;
    int file_name_recievePort = 4441;
    int file_name_targetPort = 4441;

    void sendFile(String path, String filename) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = null;
                    Socket file_name_socket = null;
                    sendMessage("has sent you a file.");
                    
                    socket = new Socket(txtIpAddress.getText(), file_targetPort);
                    file_name_socket = new Socket(txtIpAddress.getText(), file_name_targetPort);
                    
                    File file = new File(path);
                    // Get the size of the file
                    long length = file.length();
                    byte[] bytes = new byte[16 * 1024];
                    InputStream in = new FileInputStream(file);
                    OutputStream out = socket.getOutputStream();
                    OutputStream file_out = file_name_socket.getOutputStream();
                    
                    file_out.write(filename.getBytes());
                    file_out.flush();

                    int count;
                    while ((count = in.read(bytes)) > 0) {
                        out.write(bytes, 0, count);
                    }

                    out.close();
                    in.close();
                    file_out.close();
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        t.start();

    }

    void recieveFile() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                ServerSocket file_name_serverSocket = null;
                
                Socket socket = null;
                Socket file_socket = null;
                InputStream in = null;
                InputStream file_in = null;
                
                OutputStream out = null;
                try {
                    serverSocket = new ServerSocket(file_recievePort);
                    file_name_serverSocket = new ServerSocket(file_name_recievePort);

                    while((socket = serverSocket.accept()) != null) {
                        file_socket = file_name_serverSocket.accept();
                        in = socket.getInputStream();
                        file_in = file_socket.getInputStream();
                        
                        BufferedReader br = new BufferedReader(new InputStreamReader(file_in));
                        String fileName = br.readLine();
                        System.out.println("recieved file name: " + fileName);
                        
                        out = new FileOutputStream("H:\\" + fileName);
                        System.out.println("final path: " + "H:\\" + fileName);
                        byte[] bytes = new byte[16 * 1024];

                        int count;
                        while ((count = in.read(bytes)) > 0) {
                            out.write(bytes, 0, count);
                        }

                        out.close();
                        in.close();
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        });
        t.start();


    }
    
//    void sendFile(String path)  {
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ServerSocket ss;
//                Socket socket;
//                try {
//                    ss = new ServerSocket(file_recievePort);
//                    while ((socket = ss.accept()) != null) {
//                        File file = new File(path);
//                        if (!file.exists()) {
//                            write("file location not valid!");
//                        } else {
//                            FileInputStream fis = new FileInputStream(path);
//                            System.out.println("path: " + path);
//                            byte b[] = new byte[2000];
//                            fis.read(b, 0, b.length);
//                            OutputStream os = socket.getOutputStream();
//                            os.write(b);
//                        }
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
//        t.start();
//    }
//    
//    void recieveFile () {
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    byte b[] = new byte [2000];
//                    Socket sr = new Socket(add_server, file_targetPort);
//                    InputStream is = sr.getInputStream();
//                    FileOutputStream fos = new FileOutputStream("H:\\hh.txt");
//                    is.read(b, 0, b.length);
//                    fos.write(b, 0, b.length);
//                } catch (IOException ex) {
//                    Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                
//            }
//        });
//    }
    
    // ./ file operation starts
    
    public int recieve_port_client = 8888;
    
    player_thread p;
    public SourceDataLine audio_out;
    void recieveVoice() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info_out = new DataLine.Info(SourceDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info_out)) {
                System.out.println("not supported!");
                System.exit(0);
            }
            
            audio_out = (SourceDataLine) AudioSystem.getLine(info_out);
            audio_out.open(format);
            audio_out.start();
            
            p = new player_thread();
            p.din = new DatagramSocket(recieve_port_client);
            p.audio_out = audio_out;
            
            Client_voice.calling = true;
            p.start();
            btn_start.setEnabled(false);
            
        } catch (LineUnavailableException | SocketException ex) {
            Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    public int send_port_server = 8888;
    public String add_server = "127.0.0.1";
//    public String add_server = "192.168.0.102";
    TargetDataLine audio_in;
    RecorderThread r;
    
    /**
     * This method is responsible for recording and then sending audio from the device.
     * It receives audio from the TargetDataLine and then sends the audio
     * to the server with the help of RecorderThread.
     */
    void sendVoice() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Not supported");
                System.exit(0);
            }
            
            audio_in = (TargetDataLine) AudioSystem.getLine(info);
            audio_in.open(format);
            
            audio_in.start();
            
            r = new RecorderThread();
            InetAddress inet = InetAddress.getByName(add_server);
            
            r.audio_in = audio_in;
            r.dout = new DatagramSocket();
            r.server_ip = inet  ;
            r.server_port = send_port_server;
            
            Client_voice.calling = true;
            r.start();
            btn_start.setEnabled(false);
            btn_stop.setEnabled(true);
        } catch (LineUnavailableException | UnknownHostException ex) {
            Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
            chat.setText("UNKNOWN HOST!\n Check your Friend's IP Address and try again!");
            
        } catch (SocketException ex) {
            Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    *   This function returns the way we want to format our raw input from the
    *   computer input device.
    */
    public static AudioFormat getAudioFormat () {
        float sampleRate = 8000.0F;
        int sampleSizeInbits = 16;
        int channel1 = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInbits, channel1, signed, bigEndian);
    }
    
    public String getMyIpAddress() {
        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
            return (localhost.getHostAddress()).trim();
        } catch (UnknownHostException ex) {
            Logger.getLogger(client_fr.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "unable to check your ip address!";
    }

    public client_fr() {
        initComponents();
        
        recieveFile();
        
        DefaultCaret caret = (DefaultCaret) chat.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        chatListen();
        labelIpAddress.setText("Your ip address is: " + getMyIpAddress());
        
        Action action = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                writeOnMyScreen();
                sendMessage();
            }
        };
        message.addActionListener(action);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btn_start = new javax.swing.JButton();
        btn_stop = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        sendButton = new javax.swing.JButton();
        message = new javax.swing.JTextField();
        txtIpAddress = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        chat = new javax.swing.JTextArea();
        labelIpAddress = new javax.swing.JLabel();
        txt_username = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnChooseFile = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Client Side");

        btn_start.setIcon(new javax.swing.ImageIcon(getClass().getResource("/client_voice/images/phone-call-button.png"))); // NOI18N
        btn_start.setText("Start Voice Chat");
        btn_start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_startActionPerformed(evt);
            }
        });

        btn_stop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/client_voice/images/end-call.png"))); // NOI18N
        btn_stop.setText("Stop Voice Chat");
        btn_stop.setEnabled(false);
        btn_stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_stopActionPerformed(evt);
            }
        });

        sendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/client_voice/images/send 24px.png"))); // NOI18N
        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        txtIpAddress.setText("192.168.0.");
        txtIpAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIpAddressActionPerformed(evt);
            }
        });

        chat.setEditable(false);
        chat.setColumns(20);
        chat.setRows(5);
        jScrollPane1.setViewportView(chat);

        labelIpAddress.setText("Your IP Address:");

        jLabel1.setText("Enter your Friend's IP Address");

        jLabel2.setText("Enter your name");

        btnChooseFile.setText("Send File");
        btnChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelIpAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(message, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btn_start, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_stop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtIpAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                            .addComponent(txt_username))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelIpAddress)
                    .addComponent(btnChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtIpAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(sendButton)
                                .addGap(9, 9, 9)
                                .addComponent(btn_start)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_stop))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(message))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txt_username, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * this method is used for handling the stop button inside the client interface 
     */
    private void btn_stopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_stopActionPerformed
        // TODO add your handling code here:
        Client_voice.calling = false;
        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);
        
        p.din.close();
        sendMessage("is disconnected");
    }//GEN-LAST:event_btn_stopActionPerformed

     /**
     * this method is used for handling the start button inside the client interface 
     */
    
    void setIpAddress() {
        add_server = txtIpAddress.getText();
    }

    private void btn_startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_startActionPerformed
        // TODO add your handling code here:
        setIpAddress();
        initAudio();
        recieveVoice();
        sendMessage("is ready to connect with you.");
    }//GEN-LAST:event_btn_startActionPerformed

    private void txtIpAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIpAddressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIpAddressActionPerformed

    void writeOnMyScreen() {
        String username = new String(txt_username.getText());
        String msg;
        if (username.length() > 0) {
            msg = username + ": " + message.getText();
        } else {
            msg = "You: " + message.getText();
        }
        write(msg);
    }
    
    void sendMessage(String msg) {
        String username = new String(txt_username.getText());
        if (username.length() > 0) {
            msg = username + "(" + getMyIpAddress() + ")" + ": " + msg;
        } else {
            msg = getMyIpAddress() + ": " + msg;
        }
        MessageTransmitter transmitter = new MessageTransmitter(this, msg, txtIpAddress.getText(), chat_targetPort);
        transmitter.start();
    }
    
    void sendMessage(String msg, int i) {
        if (i == 1) {
            String username = new String(txt_username.getText());
            if (username.length() > 0) {
                msg = username + "(" + getMyIpAddress() + ")" + ": " + msg;
            } else {
                msg = getMyIpAddress() + ": " + msg;
            }
            MessageTransmitter transmitter = new MessageTransmitter(this, msg, txtIpAddress.getText(), chat_targetPort);
            transmitter.start();
        }
    }
    
    void sendMessage() {
        String username = new String(txt_username.getText());
        String msg;
        if (username.length() > 0) {
            msg = username + ": " + message.getText();
        } else {
            msg = getMyIpAddress() + ": " + message.getText();
        }
        MessageTransmitter transmitter = new MessageTransmitter(this, msg, txtIpAddress.getText(), chat_targetPort);
        transmitter.start();
        
        message.setText("");
    }
    
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        // TODO add your handling code here:
        writeOnMyScreen();
        sendMessage();
    }//GEN-LAST:event_sendButtonActionPerformed

    private void btnChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseFileActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println(chooser.getSelectedFile().toString() + " name: " + chooser.getSelectedFile().getName());
            sendFile(chooser.getSelectedFile().toString(), chooser.getSelectedFile().getName());
        }
    }//GEN-LAST:event_btnChooseFileActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(client_fr.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(client_fr.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(client_fr.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(client_fr.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new client_fr().setVisible(true);
            }
        });
    }
    
    
    
    
    
    public void initAudio () {
        
        sendVoice(); 
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseFile;
    private javax.swing.JButton btn_start;
    private javax.swing.JButton btn_stop;
    private javax.swing.JTextArea chat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelIpAddress;
    private javax.swing.JTextField message;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField txtIpAddress;
    private javax.swing.JTextField txt_username;
    // End of variables declaration//GEN-END:variables

    @Override
    public void write(String s) {
        chat.append(s + System.lineSeparator());
    }
}
