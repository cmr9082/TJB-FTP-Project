/**
*  ERRORPacket - packet for sending error messages between the client and the server
*  @author The Java Beans
*  @version 2205
*/

import java.net.*;
import java.io.*;

public class ERRORPacket implements TFTPConstants {
   // Attributes 
   private int opcode;
   private InetAddress toAddress; // address of machine packet is being sent to
   private int port; // port to access on the machine
   private int errorNo; // the code for the error
   private String errorMsg; // the message for the error
   
   // Constructors 
   
   /**
   *  Parameterized constructor - for creating a new packet to send
   *  @param _toAddress - address of machine packet is being sent to
   *  @param _port - port to access on the machine
   *  @param _errorNo - the code for the error
   *  @param _errorMsg - the message for the error
   */
   public ERRORPacket(InetAddress _toAddress, int _port, int _errorNo, String _errorMsg) {
      opcode = ERROR;
      toAddress = _toAddress;
      port = _port;
      errorNo = _errorNo;
      errorMsg = _errorMsg;
   }
   
   /**
   *  Default constructor - for creating a new packet to later call dissect()
   */
   public ERRORPacket() {
      // nothing needs to happen here; dissect() will take care of initialization of values
   }
   
   // Handling methods (building and dissecting)
   
   /**
   *  build - building a new DatagramPacket to send 
   *  @return the datagram packet to send
   */
   public DatagramPacket build() {
      // Output stream to make array 
      ByteArrayOutputStream baos = new ByteArrayOutputStream (2 /*opcode*/ + 2 /*errorNo*/ + errorMsg.length() + 1 /*0*/);
      DataOutputStream dos = new DataOutputStream(baos);
      
      // Writing all of the data parts to the packet
      try {
         dos.writeShort(opcode);
         dos.writeShort(errorNo);
         dos.writeBytes(errorMsg);
         dos.writeByte(0);
         
         // Closing output stream to flush
         dos.close();
      }
      catch (IOException ex) {
         System.out.println("ERROR while building ERRORPacket: " + ex);
      }
      
      // Get the byte array
      byte[] holder = baos.toByteArray();
      
      // Make the datagram packet 
      DatagramPacket errorPkt = new DatagramPacket(holder, holder.length, toAddress, port);
      
      // Return the datagram packet
      return errorPkt;
   }
   
   /**
   *  dissect - dissecting a DatagramPacket into this class
   *  @param errorPkt - the packet we are dissecting
   */
   public void dissect(DatagramPacket errorPkt) {
      // Getting the packet's address and port
      toAddress = errorPkt.getAddress();
      port = errorPkt.getPort();
      
      // Creating input stream to read the packet data
      ByteArrayInputStream bias = new ByteArrayInputStream(errorPkt.getData(), errorPkt.getOffset(), errorPkt.getLength());
      DataInputStream dis = new DataInputStream(bias);
      
      // Get the opcode 
      try {
         opcode = dis.readShort();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting ERRORPacket: " + ex);
      }
      
      // If the opcode says this isn't a ACKPacket, fill out the rest of the packet as nonvalues, close the input stream, and return
      if (opcode != ERROR) {
         errorNo = 8; // outside the range of the error codes
         errorMsg = "";
         try {
            dis.close();
         }
         catch (Exception ex) {
            System.out.println("ERROR while dissecting ERRORPacket: " + ex);
         }
         return;
      }
      
      // If this is the correct kind of packet, get the rest of the data and close the input stream
      try {
         errorNo = dis.readShort();
         errorMsg = readToZ(dis);
         dis.close();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting ERRORPacket: " + ex);
      }
      return;
   }
   
   /**
   *  readToZ() - reading a 0-terminated String out of the packet
   *  @param dis - the DataInputStream we are collecting data from 
   */
   public static String readToZ(DataInputStream dis) {
      String value = "";
      while (true) {
         try {
            byte b = dis.readByte();
            
            if (b == 0) {
               return value;
            }
            value += (char) b;
         }
         catch (IOException ex) {
            System.out.println("ERROR while dissecting ERRORPacket (reading String): " + ex);
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
   *  getPort - returns port
   *  @return port
   */
   public int getPort() {
      return port;
   }
   
   /**
   *  getErrorNo - returns errorNo
   *  @return errorNo
   */
   public int getErrorNo() {
      return errorNo;
   }
   
   /**
   *  getErrorMsg - returns errorMsg
   *  @return errorMsg
   */
   public String getErrorMsg() {
      return errorMsg;
   }
}