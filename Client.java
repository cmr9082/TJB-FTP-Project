import javafx.application.Application;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import java.io.*;
import javafx.scene.text.*;
import javafx.application.Platform;
import java.net.*; 
import java.util.*;


/**
 * Client - a client for TFTP file transfer
 * @author  The Java Beans
 * @version 2205
 */

public class Client extends Application implements EventHandler<ActionEvent> , TFTPConstants {
   // GUI Components
   private Stage stage;
   private Scene scene;
   private VBox root = new VBox(10);
   
   // Text fields/areas
   private TextField tfServer = new TextField();
   private TextField tfFolder = new TextField();
   private TextArea taLog = new TextArea();
   
   // Buttons
   private Button btnChangeDir = new Button("Change Folder");
   private Button btnUpload = new Button("Upload");
   private Button btnDownload = new Button("Download");
   
    // Current directory storage
   private String currentDir;
   
   // Server Socket
   private DatagramSocket mainSocket = null;

   
   /**
   *  main - instantiates an instance of this GUI class
   *  @param args - any commandline arguments
   */
   public static void main(String[] args) {
      launch(args);
   }
   
   /**
   *  start - sets up and displays 
   *  @param _stage - the main GUI window
   */
   public void start(Stage _stage) throws Exception {
      // Saving stage in attribute and changing title
      stage = _stage;
      stage.setTitle("TFTP Client | Java Beans");
      
      // GUI Setup
      // Server Selection
      FlowPane fpServer = new FlowPane();
      fpServer.getChildren().addAll(new Label("Server:     "), tfServer);
      root.getChildren().add(fpServer);
      
      // Choose folder button
      btnChangeDir.setOnAction(this);
      root.getChildren().add(btnChangeDir);
      
      // Current directory display
      // Disable tf editing
      tfFolder.setDisable(true);
      // Changing tf font and width
      tfFolder.setFont(Font.font("MONOSPACED", FontWeight.NORMAL, tfFolder.getFont().getSize()));
      // Getting initial directory and showing it
      File initial = new File(".");
      tfFolder.setText(initial.getAbsolutePath());
      // Changing tf width
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      // Make tf scrollable
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfFolder);
      // Finally adding tf
      root.getChildren().add(sp);
      
      // Upload/Download buttons
      btnUpload.setOnAction(this);
      btnDownload.setOnAction(this);
      FlowPane fpButtons = new FlowPane();
      fpButtons.getChildren().addAll(btnUpload, new Label("     "), btnDownload);
      root.getChildren().add(fpButtons);
      
      // Log 
      taLog.setPrefHeight(375);
      taLog.setWrapText(true);
      root.getChildren().addAll(new Label("Log:"), taLog);
      
      // Setting GUI scene and displaying the stage
      scene = new Scene(root, 450, 550);
      stage.setScene(scene);
      stage.show();
   }
   
   /**
   * handle - handling button clicks
   * @param evt - the ActionEvent for the button click
   */
   public void handle(ActionEvent evt) {
      // Get the button that was clicked
      Button btn = (Button)evt.getSource();
      
      // Determining which button was clicked
      switch(btn.getText()) {
         // Not required so ignore it for now
         // case "Change Folder":
            // break;
         case "Upload":
         
         //call doUpload()
            doUpload();
            break;
         case "Download":
            // call doDownload()
            doDownload();
            break;
      }
   }   
   
   /**
   *  doUpload - uploading a file to the server
   */
   public void doUpload() {
      // prompt user for remote file name
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Remote Name"); 
      dialog.setHeaderText("Enter the name to the file on the server");
      dialog.setX(150);
      dialog.showAndWait();
      String remoteName = dialog.getEditor().getText();
            
      // filechooser to select local file
      FileChooser chooser = new FileChooser();
      chooser.setInitialDirectory(new File(tfFolder.getText()));
      chooser.setTitle("Select Local files to Uploads");
      File localFile = chooser.showOpenDialog((Window)stage); 
      if (localFile == null)
         return;
      String currentDir = localFile.getParent();
       log("Starting Upload " + remoteName + "-->" + remoteName);
       log("Client Sending... Opcode 2 (WRQ) Filename <" + remoteName +"> Mode <octect>");

      // send WRQ to server to port 69
      DatagramSocket cSocket = null;
      InetAddress iNet = null;
      try { 
         cSocket = new DatagramSocket();
         cSocket.setSoTimeout(1000); 
         iNet = InetAddress.getByName(tfServer.getText());
         WRQPacket wrqPkt = new WRQPacket(iNet, WRQ , remoteName, "octect", TFTP_PORT);
         DatagramPacket cPacket = wrqPkt.build();
         cSocket.send(cPacket);
      }
      catch (Exception ex) {
         log("ERROR: Can't send error to client."); 
      }
       
      // Recieve ack packet from server
       byte[] hold = new byte[MAX_PACKET];
            DatagramPacket pakt = new DatagramPacket(hold, MAX_PACKET);
            try {
               cSocket.receive(pakt);
            }
            catch(SocketTimeoutException ste){
            log("Upload timed out waiting for ACK!");
            return;
            }
            catch (IOException ioe) {
               ERRORPacket error = new ERRORPacket(iNet, TFTP_PORT , UNDEF, ioe.toString());
               log("Client sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }
            ACKPacket ackPakt = new ACKPacket();
            ackPakt.dissect(pakt);
            
            // Check that ackPkt is not an error
            if (ackPakt.getOpcode() == ERROR) {
               // Log Error
              
               ERRORPacket error = new ERRORPacket();
               error.dissect(pakt);
               log("Client recieved -- Opcode 5 (ERROR) Ecode " + error.getErrorNo() + " <" + error.getErrorMsg() + ">");
               return;
            }
            // Check that ackPkt is an acknowledgement packet
            else if (ackPakt.getOpcode() != ACK) {
               // Send error
               
               ERRORPacket error = new ERRORPacket(iNet, ackPakt.getPort(), ILLOP, "Illegal Opcode -- ACK expected --" + ackPakt.getOpcode() + " received.");
               log("Client sending -- Opcode 5 (ERROR) Ecode 4 (ILLOP) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }
   
            // log data packet
            log("Client recieved -- Opcode 4 (ACK) Blk# (" + ackPakt.getBlockNo() + ")");
               
      
        // open the file for reading from
         log("Opening ---- " + currentDir + "/" + localFile.getName());
         DataInputStream in = null;
         try {
            File inFile = new File(currentDir + "/" + localFile.getName());
            in = new DataInputStream(new FileInputStream(inFile));
         }
         catch (FileNotFoundException fnfe) {
            ERRORPacket error = new ERRORPacket(iNet, TFTP_PORT, NOTFD, fnfe.toString());
            log("Client sending -- Opcode 5 (ERROR) Ecode 1 (NOTFD) <" + error.getErrorMsg() + ">");
            DatagramPacket errorpkt = error.build();
            try { 
               cSocket.send(errorpkt); 
            } 
            catch (Exception ex) {
               log("ERROR: Can't send error to client."); 
            }
            return;
         }
      
      // loop to send data packets (until eof)
         boolean keepGoing = true;
         int blockNo = 1;
         while (keepGoing) {
            // read from file the max length of bytes
            byte[] data = new byte[512];
            int numRead = 0;
            try {
               numRead = in.read(data);
            }
            catch (IOException ioe) {
               ERRORPacket error = new ERRORPacket(iNet, TFTP_PORT, UNDEF, ioe.toString());
               log("Client sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }   
            // Determining if this will be the last data packet
            if (numRead < 512) {
               keepGoing = false;
            }
            // Handling an end of file -1 return 
            if (numRead == -1) {
               numRead = 0;
            }  
         
                             
        // build and send data packet
            try {
               DATAPacket dataPkt = new DATAPacket(iNet, ackPakt.getPort(), blockNo, data, numRead);
               log("Client sending -- Opcode 3 (DATA) Blk# (" + dataPkt.getBlockNo() + ") " + Arrays.toString(dataPkt.getData()));
               cSocket.send(dataPkt.build());
               System.out.println(dataPkt.getOpcode());
            }
            catch (IOException ioe) {
               ERRORPacket error = new ERRORPacket(iNet,ackPakt.getPort(), UNDEF, ioe.toString());
               log("Client sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
               DatagramPacket errorPkt = error.build();
               try { 
                  cSocket.send(errorPkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }
      // wait for awknowledgement
            byte[] holder = new byte[MAX_PACKET];
            DatagramPacket pkt = new DatagramPacket(holder, MAX_PACKET);
            try {
               cSocket.receive(pkt);
            }
            catch(SocketTimeoutException ste){
            log("Upload timed out waiting for ACK!");
            return;
            }
            catch (IOException ioe) {
               ERRORPacket error = new ERRORPacket(iNet, ackPakt.getPort() , UNDEF, ioe.toString());
               log("Client sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }
            ACKPacket ackPkt = new ACKPacket();
            ackPkt.dissect(pkt);
            
            // Check that ackPkt is not an error
            if (ackPkt.getOpcode() == ERROR) {
               // Log Error
               keepGoing = false;
               ERRORPacket error = new ERRORPacket();
               error.dissect(pkt);
               log("Client recieved -- Opcode 5 (ERROR) Ecode " + error.getErrorNo() + " <" + error.getErrorMsg() + ">");
               return;
            }
            // Check that ackPkt is an acknowledgement packet
            else if (ackPkt.getOpcode() != ACK) {
               // Send error
               keepGoing = false;
               ERRORPacket error = new ERRORPacket(iNet, ackPkt.getPort(), ILLOP, "Illegal Opcode -- ACK expected --" + ackPkt.getOpcode() + " received.");
               log("Client sending -- Opcode 5 (ERROR) Ecode 4 (ILLOP) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }
            
            // log data packet
            log("Client recieved -- Opcode 4 (ACK) Blk# (" + ackPkt.getBlockNo() + ")");
            
            // Update block number
            blockNo++;
         }
            
         // log that the file has been uploaded
         log("Uploaded" + remoteName + "-->" + remoteName);
         try {
            in.close();
         }
         catch (IOException ioe) {
            ERRORPacket error = new ERRORPacket(iNet, TFTP_PORT, UNDEF, ioe.toString());
            log("Server sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
            DatagramPacket errorPkt = error.build();
            try { 
               cSocket.send(errorPkt); 
            } 
            catch (Exception ex) {
               log("ERROR: Can't send error to client."); 
            }
            return;
         }
         cSocket.close();
      }
                 
     
   
   
   /**
   *  doDownload - downloading a file to the server 
  
   */
   public void doDownload() {
      // prompt user for remote file name
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Remote Name");
      dialog.setHeaderText("Enter the name of the remote file to download.");
      dialog.setX(75);
      dialog.showAndWait();
      String remoteName = dialog.getEditor().getText();
      // filechooser to select local save file
      FileChooser chooser = new FileChooser();
      chooser.setInitialDirectory(new File(this.tfFolder.getText()));
      chooser.setTitle("Enter the Name of the File for Download");
      File localFile = chooser.showSaveDialog((Window)this.stage);
      if (localFile == null) {
         log("Canceled!");
         return;
      } 
      String currentDir = localFile.getParent();
      // send RRQ to server to port 69
      DatagramSocket cSocket = null;
      InetAddress iNet = null;
      try { 
         cSocket = new DatagramSocket();
         cSocket.setSoTimeout(1000); 
         iNet = InetAddress.getByName(tfServer.getText());
         RRQPacket rrqPacket = new RRQPacket(iNet, RRQ , remoteName, "octect", TFTP_PORT);
         DatagramPacket cPacket = rrqPacket.build();
         cSocket.send(cPacket);
         log("Starting Download " + remoteName + "-->" + remoteName);
         log("Client Sending... Opcode 1 (RRQ) Filename <" + remoteName +"> Mode <octect>");
      }
      catch (Exception e) {
         log("ERROR: Can't send error to Server."); 
      }
                  
      
    // open file to recieve
         log("Opening-- " + currentDir + "/" + localFile.getName());
         DataOutputStream out = null;
         try {
            File outFile = new File(currentDir + "/" + localFile.getName());
            out = new DataOutputStream(new FileOutputStream(outFile));
         }
         catch (FileNotFoundException fnfe) {
            ERRORPacket error = new ERRORPacket(iNet, TFTP_PORT, ACCESS, fnfe.toString());
            log("Client sending -- Opcode 5 (ERROR) Ecode 2 (ACCESS) <" + error.getErrorMsg() + ">");
            DatagramPacket errorpkt = error.build();
            try { 
               cSocket.send(errorpkt); 
            } 
            catch (Exception ex) {
               log("ERROR: Can't send error to client."); 
            }
            return;
         }
      
      // loop to accept data packets
      boolean keepgoing = true; 
       while (keepgoing) { 
          byte[] hold = new byte[MAX_PACKET];
            DatagramPacket pakt = new DatagramPacket(hold, MAX_PACKET);
            try {
               cSocket.receive(pakt);
            }
            catch(SocketTimeoutException ste){
            log("Upload timed out waiting for ACK!");
            return;
            }
            catch (IOException ioe) {
               ERRORPacket error = new ERRORPacket(iNet, TFTP_PORT, UNDEF, ioe.toString());
               log("Client sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }
         // dissect data packet
           DATAPacket dataPkt = new DATAPacket();
           dataPkt.dissect(pakt);  
                          
            
         // save data into file
       if (dataPkt.getOpcode() == ERROR) {
               // Log Error
               keepgoing = false;
               ERRORPacket error = new ERRORPacket();
               error.dissect(pakt);
               log("Client recieved -- Opcode 5 (ERROR) Ecode " + error.getErrorNo() + " <" + error.getErrorMsg() + ">");
               return;
            }
            // Check that dataPkt is a data packet
            else if (dataPkt.getOpcode() != DATA) {
               // Send error
               keepgoing = false;
               ERRORPacket error = new ERRORPacket(iNet, dataPkt.getPort(), ILLOP, "Illegal Opcode -- DATA expected --" + dataPkt.getOpcode() + " received.");
               log("Client sending -- Opcode 5 (ERROR) Ecode 4 (ILLOP) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }
            
            // log data packet
            log("Client recieved -- Opcode 3 (DATA) Blk# (" + dataPkt.getBlockNo() + ") " + Arrays.toString(dataPkt.getData()));
         
         // write data to file
            try { 
               out.write(dataPkt.getData(), 0, dataPkt.getLength());
            }
            catch (IOException ioe) {
               ERRORPacket error = new ERRORPacket(iNet, dataPkt.getPort(), UNDEF, ioe.toString());
               log("Client sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
               DatagramPacket errorpkt = error.build();
               try { 
                  cSocket.send(errorpkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to server."); 
               }
               return;
            }
         
         // send ack packet with corresponding block #
         try { 
         ACKPacket ackDat = new ACKPacket(iNet, dataPkt.getPort(), dataPkt.getBlockNo()); 
         log("Client sending -- Opcode 4 (ACK) BLk# (" + ackDat.getBlockNo() + ")");
         cSocket.send(ackDat.build());
         }
          catch (IOException ioe) {
               ERRORPacket error = new ERRORPacket(iNet, dataPkt.getPort(), UNDEF, ioe.toString());
               log("Server sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
               DatagramPacket errorPkt = error.build();
               try { 
                  cSocket.send(errorPkt); 
               } 
               catch (Exception ex) {
                  log("ERROR: Can't send error to client."); 
               }
               return;
            }         
          // if packet is < 512 bytes, we are done looping
          if ( dataPkt.getLength() < 512) {
          keepgoing = false; 
             }  
             }   
             log("Downloaded" + remoteName + "-->" + remoteName);
      // done, close file and socket
      try {
            out.close();
         }
         catch (IOException ioe) {
            ERRORPacket error = new ERRORPacket(iNet, TFTP_PORT, UNDEF, ioe.toString());
            log("Client sending -- Opcode 5 (ERROR) Ecode 0 (UNDEF) <" + error.getErrorMsg() + ">");
            DatagramPacket errorPkt = error.build();
            try { 
               cSocket.send(errorPkt); 
            } 
            catch (Exception ex) {
               log("ERROR: Can't send error to client."); 
            }
            return;
         }
         cSocket.close();
      }
   
   /**
   *  log - adding to the log text area
   *  @param message - the message to log
   */
   public void log(String message) {
      taLog.appendText(message + "\n");
   }
}	
