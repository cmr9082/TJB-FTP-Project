/**
*  TFTPConstants - an interface for all of our opcode and ecode constants
*  @author The Java Beans
*  @version 2205
*/

public interface TFTPConstants {
   // Server Port
   public static final int TFTP_PORT = 69;
   
   // Max size of a packet
   public static final int MAX_PACKET = 1500;

   // OPCodes - codes for different packets
   public static final int RRQ = 1;
   public static final int WRQ = 2;
   public static final int DATA = 3;
   public static final int ACK = 4;
   public static final int ERROR = 5;
   
   // ECodes - codes for different errors
   public static final int UNDEF = 0;
   public static final int NOTFD = 1;
   public static final int ACCESS = 2;
   public static final int DSKFUL = 3;
   public static final int ILLOP = 4;
   public static final int UNKID = 5;
   public static final int FILEX = 6;
   public static final int NOUSR = 7;
}