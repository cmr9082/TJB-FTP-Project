import javafx.application.Application;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import java.io.*;
import java.net.*;
import javafx.scene.text.*;
import javafx.application.Platform;

/**
 * Server - a server for TFTP file transfer
 * @author  The Java Beans
 * @version 2205
 */

public class Server extends Application implements EventHandler<ActionEvent>, TFTPConstants {
   // GUI Components
   private Stage stage;
   private Scene scene;
   private VBox root = new VBox(10);
   
   // Text fields/areas
   private TextField tfFolder = new TextField();
   private TextArea taLog = new TextArea();
   
   // Buttons
   private Button btnChangeDir = new Button("Change Folder");
   private Button btnStart = new Button("Start");
   
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
   *  start - sets up and displays GUI
   *  @param _stage - the main GUI window
   */
   public void start(Stage _stage) throws Exception {
      // Saving stage in attribute and changing title
      stage = _stage;
      stage.setTitle("TFTP Server | Java Beans");
      
      // GUI Setup
      // Choose folder btn
      btnChangeDir.setOnAction(this);
      root.getChildren().add(btnChangeDir);
      
      // Current directory display
      // Disable tf editing
      tfFolder.setDisable(true);
      // Changing tf font and width
      tfFolder.setFont(Font.font("MONOSPACED", FontWeight.NORMAL, tfFolder.getFont().getSize()));
      // Getting initial directory and showing it
      File initial = new File(".");
      currentDir = initial.getAbsolutePath();
      tfFolder.setText(currentDir);
      // Changing tf width
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      // Make tf scrollable
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfFolder);
      // Finally adding tf
      root.getChildren().add(sp);
      
      // Start btn
      btnStart.setOnAction(this);
      btnStart.setStyle("-fx-background-color: LawnGreen");
      FlowPane fpStart = new FlowPane();
      fpStart.getChildren().addAll(new Label("Start the server:     "), btnStart);
      root.getChildren().add(fpStart);
      
      // Log
      taLog.setPrefHeight(300);
      taLog.setWrapText(true);
      root.getChildren().add(taLog);
      
      // Setting GUI scene and displaying the stage
      scene = new Scene(root, 300, 400);
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
         case "Start":
            startListen();
            break;
         case "Stop":
            stopListen();
            break;
         case "Change Folder":
            doChange();
            break;
      }
   }
   
   /**
   *  doChange - change the directory of the server
   */
   public void doChange() {
      // Have the user select the new starting directory
      DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle("Select Server Directory");
      File newDir = chooser.showDialog(stage);
      
      // Check that the choosing wasn't cancelled
      if (newDir == null) {
         return;
      }
      
      // Display/store the current directory
      currentDir = newDir.getAbsolutePath();
      tfFolder.setText(currentDir);
   }
   
   /**
   *  startListen - starting the server
   */
   public void startListen() {
      // Editing GUI
      btnStart.setText("Stop");
      btnStart.setStyle("-fx-background-color: Crimson");
      btnChangeDir.setDisable(true);
      
      // Starting server work thread
      UDPServerThread server = new UDPServerThread();
      server.start();
   }
   
   /**
   *  stopListen - stopping the server
   */
   public void stopListen() {
      // Editing GUI
      btnStart.setText("Start");
      btnStart.setStyle("-fx-background-color: LawnGreen");
      btnChangeDir.setDisable(false);
      
      // Stopping server work thread
      try {
         mainSocket.close();
      }
      catch (Exception ex) {
         log("Exception stopping server: " + ex);
      }
   }
   
   /**
   *  log - adding to the log text area
   *  @param message - the message to log
   */
   public void log(String message) {
      Platform.runLater(new Runnable() {
         public void run() {
           taLog.appendText(message + "\n");
         }
      });
   }
   
   /**
   *  UDPServerThread - inner class to accept clients
   *  @author The Java Beans
   *  @version 2205
   */
   public class UDPServerThread extends Thread {
      /**
      *  run - accepting clients
      */
      public void run() {
         // Logging that we started listening
         log("UDPServerThread.run -- Listen thread started");
         
         // Socket to listen for packets
         try {
            mainSocket = new DatagramSocket(TFTP_PORT);
         }
         catch (IOException ioe) {
            log("IO Exception (1): " + ioe);
            return;
         }
         
         // Waiting for new packets then making threads for them
         while (true) {
            // Array to hold new packet data
            byte[] holder = new byte[MAX_PACKET];
            // Packet to hold new client packet
            DatagramPacket pkt = new DatagramPacket(holder, MAX_PACKET);
            
            // Recieve a packet
            try {
               mainSocket.receive(pkt);
            }
            catch (IOException ioe) {
               // stops the server when the mainSocket is closed
               return;
            }
            
            // Log that we recieved the first packet
            log("UDPServerThread.run -- Client packet recieved!");
            
            // Creating thread for the packet
            UDPClientThread ct = new UDPClientThread(pkt);
            ct.start();
         }
      }
   }
   
   /**
   *  UDPClientThread - inner class to handle client requests
   *  @author The Java Beans
   *  @version 2205
   */
   public class UDPClientThread extends Thread {
      // Sockets for connecting with the client
      private DatagramSocket cSocket = null;
      private DatagramPacket firstPkt = null;
      private int port;
      private InetAddress toAddress;
      
      /**
      *  constructor - sets attributes for the connection and the first packet
      *  @param _pkt - the first datagram packet of the client-server conversation
      */
      public UDPClientThread(DatagramPacket _pkt) {
         // first packet
         firstPkt = _pkt;
         
         // new socket
         try {
            cSocket = new DatagramSocket();
         }
         catch (IOException ioe) {
            log("IO Exception (2): " + ioe);
            return;
         }
         
         // connection information
         port = cSocket.getPort();
         toAddress = firstPkt.getAddress();
      }
      
      /**
      *  run - handling clients, determining what was requested
      */
      public void run() {
         // Determining what the first packet requests
         try {
            // Dissect the packet to find the opcode
            // If the opcode is WRQ, call doWRQ()
            // If the opcode is WRQ, call doRRQ()
         }
         catch (IOException ioe) {
            log("IOException (3): " + ioe);
            ERRORPacket error = new ERRORPacket(toAddress, port, UNDEF, ioe.toString());
            DatagramPacket errorpkt = error.build();
            try { 
               cSocket.send(errorpkt); 
            } 
            catch (Exception ex) {
               log("ERROR (3): Can't send error to client."); 
            }
         }
      }
      
      /**
      *  doWRQ - handling an upload request
      */
      public void doWRQ() {
         // log the request info
         // awknowledge the request packet
         // open file for writing to
         // loop to recieve data packets (until one is < 512 bytes)
            // recieve data packet
            // write data to file
            // awknowledge with corresponding block number
         // log that the file has been uploaded
         // close socket and file output
      }
      
      /**
      *  doRRQ - handling a download request
      */
      public void doRRQ() {
         // log the request info
         // open the file for reading form
         // loop to send data packets (until eof)
            // read from file the max length of bytes
            // build data packet of max length
            // wait for awknowledgement of corresponding block no
         // log that the file has been downloaded
         // close the socket and file input
      }
   }
}	
