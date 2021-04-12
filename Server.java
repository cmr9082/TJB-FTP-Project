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
 * Server - a server for TFTP file transfer
 * @author  The Java Beans
 * @version 2205
 */

public class Server extends Application implements EventHandler<ActionEvent> {
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
      tfFolder.setText(initial.getAbsolutePath());
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
            break;
      }
   }
   
   /**
   *  startListen - starting the server
   */
   public void startListen() {
      btnStart.setText("Stop");
      btnStart.setStyle("-fx-background-color: FireBrick");
   }
   
   /**
   *  stopListen - stopping the server
   */
   public void stopListen() {
      btnStart.setText("Start");
      btnStart.setStyle("-fx-background-color: LawnGreen");
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
