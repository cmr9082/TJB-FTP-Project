/**
*  ACKPacket - packet for sending acknowledgements of recieving data between the client and the server
*  @author The Java Beans
*  @version 2205
*/

import java.net.*;
import java.io.*;

public class ACKPacket implements TFTPConstants {
   // Attributes 
   private int opcode;
   private InetAddress toAddress; // address of machine packet is being sent to
   private int port; // port to access on the machine
   private int blockNo; // the block number of the data we are awknowleding
   
   // Constructors 
   
   /**
   *  Parameterized constructor - for creating a new packet to send
   *  @param _toAddress - address of machine packet is being sent to
   *  @param _port - port to access on the machine
   *  @param _blockNo - the block number of the data
   */
   public ACKPacket(InetAddress _toAddress, int _port, int _blockNo) {
      opcode = ACK;
      toAddress = _toAddress;
      port = _port;
      blockNo = _blockNo;
   }
   
   /**
   *  Default constructor - for creating a new packet to later call dissect()
   */
   public ACKPacket() {
      // nothing needs to happen here; dissect() will take care of initialization of values
   }
   
   // Handling methods (building and dissecting)
   
   /**
   *  build - building a new DatagramPacket to send 
   *  @return the datagram packet to send
   */
   public DatagramPacket build() {
      // Output stream to make array 
      ByteArrayOutputStream baos = new ByteArrayOutputStream (2 /*opcode*/ + 2 /*blockNo*/);
      DataOutputStream dos = new DataOutputStream(baos);
      
      // Writing all of the data parts to the packet
      try {
         dos.writeShort(opcode);
         dos.writeShort(blockNo);
         
         // Closing output stream to flush
         dos.close();
      }
      catch (IOException ex) {
         System.out.println("ERROR while building ACKPacket: " + ex);
      }
      
      // Get the byte array
      byte[] holder = baos.toByteArray();
      
      // Make the datagram packet 
      DatagramPacket ackPkt = new DatagramPacket(holder, holder.length, toAddress, port);
      
      // Return the datagram packet
      return ackPkt;
   }
   
   /**
   *  dissect - dissecting a DatagramPacket into this class
   *  @param ackPkt - the packet we are dissecting
   */
   public void dissect(DatagramPacket ackPkt) {
      // Getting the packet's address and port
      toAddress = ackPkt.getAddress();
      port = ackPkt.getPort();
      
      // Creating input stream to read the packet data
      ByteArrayInputStream bias = new ByteArrayInputStream(ackPkt.getData(), ackPkt.getOffset(), ackPkt.getLength());
      DataInputStream dis = new DataInputStream(bias);
      
      // Get the opcode 
      try {
         opcode = dis.readShort();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting ACKPacket: " + ex);
      }
      
      // If the opcode says this isn't a ACKPacket, fill out the rest of the packet as nonvalues, close the input stream, and return
      if (opcode != ACK) {
         blockNo = 0;
         try {
            dis.close();
         }
         catch (Exception ex) {
            System.out.println("ERROR while dissecting ACKPacket: " + ex);
         }
         return;
      }
      
      // If this is the correct kind of packet, get the rest of the data and close the input stream
      try {
         blockNo = dis.readShort();
         dis.close();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting ACKPacket: " + ex);
      }
      return;
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
   *  getBlockNo - returns blockNo
   *  @return blockNo
   */
   public int getBlockNo() {
      return blockNo;
   }
}