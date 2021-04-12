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

/**
 * Client - a client for TFTP file transfer
 * @author  The Java Beans
 * @version 2205
 */

public class Client extends Application implements EventHandler<ActionEvent> {
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
         case "Change Folder":
            break;
         case "Upload":
            break;
         case "Download":
            break;
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
}	
