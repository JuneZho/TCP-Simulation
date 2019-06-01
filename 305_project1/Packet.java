import java.util.*;
import java.util.zip.Adler32;

/**
 * A class which represents a packet
 */
public class Packet
{
    
    private Message msg; //the enclosed message
    private int seqnum; //packets seq. number
    private int acknum; //packet ack. number
    private int checksum; //packet checksum

    Random ran; //random number generator
    
 

    public Packet(Message msg, int seqnum, int acknum, int checksum)
    {
        this.msg=msg;
        this.seqnum=seqnum;
        this.acknum=acknum;
        this.checksum=checksum;
        this.ran=new Random();
    }
    
    public Packet(Packet other)
    {
        this.msg=new Message(new String(other.msg.getMessage()));
        this.seqnum=other.seqnum;
        this.acknum=other.acknum;
        this.checksum=other.checksum;
        this.ran=other.ran;
        
    }

    public int getAcknum()
    {
        return acknum;
    }
    
    public int getSeqnum()
    {
        return seqnum;
    }

    public Message getMessage()
    {
        return msg;
    }
    /**
     * get all the data in the packet and return it as a string
     * @return
     */
    private String getData() {
    	int sum = acknum + seqnum;
    
  
    	return msg.getMessage()+""+sum;
    }
    /**
     * set the checksum number to be a valid checksum
     */
    
    public void setChecksum()
    {
    	
    	String data = getData();//get the data string
    	
    	Adler32 tool = new Adler32(); //use Adler32 to generate the checksum
    	
    	tool.update(data.getBytes()); //update the data
    	
    	
    	this.checksum = Integer.MAX_VALUE - (int)tool.getValue() ; //generated checksum + checksum should be the max value of int
    	
    }
    
    /**
     * 
     * @return if the packet is corrupted
     */
    public boolean isCorrupt()
    {
   
    	String data = getData(); //get the data string
    	
    	byte[] byteData = data.getBytes(); // the byte[] for the sting
    	
    	Adler32 tool = new Adler32();//get the Alder32
    	
    	tool.update(byteData); //update the data
    	
    	
    	if((int)tool.getValue() + checksum==Integer.MAX_VALUE) { //check if the generated checksum + checksum = max value of int
    		return false; //not corrupted
    	}
    	
        return true;
    }
    
    /**
     * This method curropts the packet the follwing way:
     * curropt the message with a 75% chance
     * curropt the seqnum with 12.5% chance
     * curropt the ackum with 12.5% chance
     */
    public void corrupt()
    {
        double num =ran.nextDouble();
        
        if(num<0.75)
        {this.msg.corruptMessage();}
        else if(num<0.875)
        {this.seqnum=this.seqnum+1;}
        else
        {this.acknum=this.acknum+1;}

    }
    public static void main(String args[]) {
    	Packet test = new Packet(new Message("asd123safasgfsagsdgsdhf"), 2, 2, 0);
    	test.setChecksum();
    	System.out.println(test.isCorrupt());
    	
    	
    }

}
