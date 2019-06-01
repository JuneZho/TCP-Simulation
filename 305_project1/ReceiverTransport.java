import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport
{
    private ReceiverApplication ra;
    private NetworkLayer nl;
    private boolean bufferingPackets;//buffered packets
    private int expectedSeqNum; //add

    private HashMap<Integer,Message> bufferedmsg;

    public ReceiverTransport(NetworkLayer nl){
        ra = new ReceiverApplication();
        this.nl=nl;
        initialize();
    }
    
    /**
     * initialized the variables
     */

    public void initialize()
    {
    	expectedSeqNum = 0 ;
    	bufferedmsg = new HashMap<Integer,Message>();
    }
    /**
     * check if the buffer has the next pkt in it. If it has , just use it and send the ack for the next pkt.
     * @param pktnum
     * @return the new ack should be sent
     */

    public int checkBuffer(int pktnum) {
    	 if(bufferedmsg.containsKey(pktnum+1)) { //if the buffer contains the next pkt
    		 bufferedmsg.remove(pktnum+1); //remove the entry

    			return checkBuffer(pktnum+1); //recursive find the next pkt of the next pkt and update the pktnum
    	 }

 		return pktnum;
    	
    }
    /**
     * receive message from the sender
     * @param pkt
     */
    public void receiveMessage(Packet pkt)
    {
    	
    	if(pkt.isCorrupt()) {//if the pkt is corrupted
    		Packet ackPkt = new Packet(new Message(""), -1,expectedSeqNum , 0); //always ack the next bite ,seq num = -1
    		ackPkt.setChecksum(); //set the checksum
    	    nl.sendPacket(ackPkt, Event.SENDER); //send an ack pkt to (sender)
    		System.out.println("sender packet is corrupted");
    	}
    	else { //not corrupted
    	Message mes = pkt.getMessage(); ///get the message
    	int seqNum = pkt.getSeqnum(); //get the sequence number
    	if(seqNum == expectedSeqNum ) { //if the packet is the packet we expect
    		expectedSeqNum++;  //should add the size of the message (the window size?)
    		 if(bufferingPackets && bufferedmsg.containsKey(seqNum+1)) { //check if the next packet is buffered, if it is, send the ack for the next packet
    	    	 System.out.println( " in the buffer");
    	    	expectedSeqNum = checkBuffer(seqNum); 
    	    	System.out.println( " send ack "+ (expectedSeqNum));
    	    	
    	    } 
    		Packet ackPkt = new Packet(new Message(""), -1,expectedSeqNum , 0); //always ack the next bite
    		ackPkt.setChecksum();
    	    nl.sendPacket(ackPkt, 0); //send an ack pck to 0 (sender)
    	    System.out.println("packet "+  (expectedSeqNum)+ " received");
    	    
    	}
    	else if(seqNum > expectedSeqNum){ //if the seq is larger that the expected sueqence num, this means that the packet is out of order
    		Packet ackPkt = new Packet(new Message(""), -1, expectedSeqNum, 0); //send for the expected seq num
    		ackPkt.setChecksum();
    	    nl.sendPacket(ackPkt, 0); //send an ack pck to 0 (sender)
    	    System.out.println("out of order packet, send ack "+(expectedSeqNum));
    	    if(bufferingPackets) {// if buffering packet is allowed, put this packet into the buffer
      	    	 System.out.println("buffer packets " + (seqNum+1));
      	    	 bufferedmsg.put((seqNum+1), pkt.getMessage());
       	    }
    		
    	}
    	else{ //if the seq is smaller that the expected sueqence num, this means that the packet already received
    		//packeted received
    		Packet ackPkt = new Packet(new Message(""), -1,expectedSeqNum , 0); //ack for the expected packet
    		ackPkt.setChecksum();
    	    nl.sendPacket(ackPkt, 0); //send an ack pck to 0 (sender)
    	    System.out.println("packet "+  (expectedSeqNum)+ " received, It is received");
    		}
    	}
    }
    

    public void setProtocol(int n)
    {
        if(n>0)
            bufferingPackets=true;
        else
            bufferingPackets=false;
    }

}
