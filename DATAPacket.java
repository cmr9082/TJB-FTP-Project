/**
*  DATAPacket - packet for sending file data between the client and the server
*  @author The Java Beans
*  @version 2205
*/

import java.net.*;
import java.io.*;

public class DATAPacket implements TFTPConstants {
   // Attributes 
   private int opcode;
   private InetAddress toAddress; // address of machine packet is being sent to
   private int port; // port to access on the machine
   private int blockNo; // the block number of the data
   private byte[] data; // the data being sent
   private int dataLen; // length of the data being sent
   
   // Constructors 
   
   /**
   *  Parameterized constructor - for creating a new packet to send
   *  @param _toAddress - address of machine packet is being sent to
   *  @param _port - port to access on the machine
   *  @param _blockNo - the block number of the data
   *  @param _data - the data being sent
   *  @param _dataLen - length of the data being sent
   */
   public DATAPacket(InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen) {
      opcode = DATA;
      toAddress = _toAddress;
      port = _port;
      blockNo = _blockNo;
      data = _data;
      dataLen = _dataLen;
   }
   
   /**
   *  Default constructor - for creating a new packet to later call dissect()
   */
   public DATAPacket() {
      // nothing needs to happen here; dissect() will take care of initialization of values
   }
   
   // Handling methods (building and dissecting)
   
   /**
   *  build - building a new DatagramPacket to send 
   *  @return the datagram packet to send
   */
   public DatagramPacket build() {
      // Output stream to make array 
      ByteArrayOutputStream baos = new ByteArrayOutputStream (2 /*opcode*/ + 2 /*blockNo*/ + dataLen /*data*/);
      DataOutputStream dos = new DataOutputStream(baos);
      
      // Writing all of the data parts to the packet
      try {
         dos.writeShort(opcode);
         dos.writeShort(blockNo);
         dos.write(data, 0, dataLen);
         
         // Closing output stream to flush
         dos.close();
      }
      catch (IOException ex) {
         System.out.println("ERROR while building DATAPacket: " + ex);
      }
      
      // Get the byte array
      byte[] holder = baos.toByteArray();
      
      // Make the datagram packet 
      DatagramPacket dataPkt = new DatagramPacket(holder, holder.length, toAddress, port);
      
      // Return the datagram packet
      return dataPkt;
   }
   
   /**
   *  dissect - dissecting a DatagramPacket into this class
   *  @param dataPkt - the packet we are dissecting
   */
   public void dissect(DatagramPacket dataPkt) {
      // Getting the packet's address and port
      toAddress = dataPkt.getAddress();
      port = dataPkt.getPort();
      
      // Creating input stream to read the packet data
      ByteArrayInputStream bias = new ByteArrayInputStream(dataPkt.getData(), dataPkt.getOffset(), dataPkt.getLength());
      DataInputStream dis = new DataInputStream(bias);
      
      // Get the opcode 
      try {
         opcode = dis.readShort();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting DATAPacket: " + ex);
      }
      
      // If the opcode says this isn't a DATAPacket, fill out the rest of the packet as nonvalues, close the input stream, and return
      if (opcode != DATA) {
         blockNo = 0;
         data = new byte[0];
         try {
            dis.close();
         }
         catch (Exception ex) {
            System.out.println("ERROR while dissecting DATAPacket: " + ex);
         }
         return;
      }
      
      // If this is the correct kind of packet, get the rest of the data and close the input stream
      try {
         blockNo = dis.readShort();
         dataLen = dataPkt.getLength() - 4;
         data = new byte[dataLen];
         int nread = dis.read(data, 0, dataLen);
         dis.close();
      }
      catch (IOException ex) {
         System.out.println("ERROR while dissecting DATAPacket: " + ex);
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
   
   /**
   *  getData - returns data
   *  @return data
   */
   public byte[] getData() {
      return data;
   }
   
   /**
   *  getLength - returns dataLen
   *  @return dataLen
   */
   public int getLength() {
      return dataLen;
   }
}