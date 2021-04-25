/**
*  WRQPacket - packet for Write Request for between the Client and Server
*  @author The Java Beans
*  @version 2205
*/

import java.net.*;
import java.io.*;

public class RRQPacket implements TFTPConstants {
   // Attributes 
   private int opcode;
   private InetAddress toAddress; // address of machine packet is being sent to
   private String filename = null; // filename of the packet/ also the name of the file requested 
   private String mode = null; // information about the data transfer mode
   private int port; //port to access on the machine
   // Constructors 
   
   /**
   *  Parameterized constructor - for creating a new packet to send
   *  @param _toAddress - address of machine packet is being sent to
   *  @param _port - port to access on the machine
   *  @param _filename - file name of the packet 
   *  @param _mode - informmation about the data transfer
   */
   public RRQPacket(InetAddress _toAddress, int _opcode,  String _filename, String _mode, int _port ) {
      opcode = _opcode;
      toAddress = _toAddress;
      filename = _filename;
      mode = _mode;
      port = _port;
          
   }
   
   /**
   *  Default constructor 
   */
   public RRQPacket() {
      // nothing needs to happen here; 
   }
   
   // Handling methods (building)
   
   /**
   *  build - building a new DatagramPacket to send 
   *  format 2 bytes  - Opcode;  string Filename; 1 byte- 0 ; string  - mode; 1- byte 0;
   *  @return the datagram packet to send
   */
   public DatagramPacket build() {
   
      Exception except = null;
      int len = 2 + filename.length() + mode.length() + 2;
      ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
      DataOutputStream dos = new DataOutputStream(baos);
          
      try {
        
          //writing out  all the data 
         dos.writeShort(opcode);
         dos.writeBytes(filename);
         dos.writeByte(0);
         dos.writeBytes(mode);
         dos.writeByte(0);
          
          //closing the stream 
         dos.close();
       
      } catch (Exception e) {
         
         System.out.println(" Error while building WRQ/RRQ Packets" + e);
      }
        // Get the byte array
      byte[] holder = baos.toByteArray();
         
         // Make the datagram packet 
      DatagramPacket RRQpacket = new DatagramPacket(baos.toByteArray(), 0, (baos.toByteArray()).length, toAddress, port);
   
      return RRQpacket;
   }  
   
   /**
   *  dissect - dissecting a DatagramPacket into this class
   *  @param wrqRRQPacket - the packet we are dissecting
   */
   public void dissect(DatagramPacket RRQpacket) {
      // Getting the packet's address and port
      toAddress = RRQpacket.getAddress();
      port = RRQpacket.getPort();
      
      // Creating input stream to read the packet data
      ByteArrayInputStream bias = new ByteArrayInputStream(RRQpacket.getData(),RRQpacket.getOffset(), RRQpacket.getLength());
      DataInputStream dis = new DataInputStream(bias);
      
      // Get the opcode 
      try {
         opcode = dis.readShort();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting RRQ Packet " + ex);
      }
      
      // If the opcode says this isn't a ACKPacket, fill out the rest of the packet as nonvalues, close the input stream, and return
      if (opcode != RRQ) {
         filename = ""; // outside the range of the error codes
         mode = "";
         try {
            dis.close();
         }
         catch (Exception ex) {
            System.out.println("ERROR while dissecting RRQ Packet " + ex);
         }
         return;
      }
      
      // If this is the correct kind of packet, get the rest of the data and close the input stream
      try {
         filename = readToZ(dis);
         mode = readToZ(dis);
         dis.close();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting RRQ Packet " + ex);
      }
      return;
   }  
    
      
   /**
   *  readToZ() - reading a 0-terminated String out of the packet
   *  @param dis - the DataInputStream we are collecting data from 
   */
   public static String readToZ(DataInputStream dis)  {
      String value = "";
      while (true) {
         try {
            int byt = dis.readByte();
            
            if (byt == 0) {
               return value;
            }
            value += (char) byt;
         }
         catch (IOException ex) {
            System.out.println("ERROR while dissecting RRQ Packet " + ex);
         }
      }
   }
   
   // Accessors
   
   /**
   *  getOpcode - returns opcode
   *  @return opcode
   */
   public int getOpcode() {
      return opcode;
   }
   
   /**
   *  getAddress - returns toAddress
   *  @return toAddress
   */
   public InetAddress getAddress() {
      return toAddress;
   }
   
    /**
   *  getFilename - returns filename
   *  @return filename
   */
   public String getfilename() {
      return filename;
   }
 /**
   *  getmode - returns mode 
   *  @return port
   */
   public String getmode() {
      return mode;
   }

   /**
   *  getPort - returns port
   *  @return port
   */
   public int getPort() {
      return port;
   }
   
}