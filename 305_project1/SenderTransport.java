
import java.util.ArrayList;
import java.util.HashMap;
/**
 * A class which represents the receiver transport layer
 */
public class SenderTransport
{
    private NetworkLayer nl;//networklayer
    private Timeline tl;//timeline
    private int ws; //window size
    private int mss; //mss
    private boolean bufferingPackets;//buffer packet or not
    private int curSeq;//current sequence number
    private int sendCount;//count of packet send in one window
    private int packetAcked;//the number of packets acked
    private int n;
    private int curPk; //current packets sent
    private HashMap<Integer,Message> buffer;
    private int numOfAcks; //num of consecutive acks
    private int ackNum; //the ack num of that consecutive acks
    
    
    
    /**
     * 
     * @return the timeline
     */
    public Timeline getTL() {
    	return tl;
    }

    public SenderTransport(NetworkLayer nl){
        this.nl=nl;
        initialize();

    }
    
    /*
     * initialize the variable
     */

    public void initialize()
    {
    	numOfAcks=1;
    	ackNum =0;
    	curPk=0;
    	curSeq = 0;
    	sendCount = 0;
    	ws= 0;
    	mss =0;
    	buffer = new HashMap<Integer,Message>();
    	
    	
    }
    
    /**
     * get the packets sent
     * @return the packets sent
     */
    
    public int getPkNum() {
    	return curPk;
    }
    
    /**
     * create and send the next packet with msg given by the sender application with accurate paket number and checksum
     * @param msg given by sender application
     * @return true if it is the last packet allow to send in that windows size,   false if it is not
     */

    public boolean sendMessage(Message msg)
    {
    
    	
    	if(sendCount+1 == n || sendCount+1 > n) { //the last packet allow to send
    		sendCount =0; //reset the send count for next window
    		System.out.println("I'm here");
    		
    		

        	Packet pkt = new Packet(msg, curPk, -1, 0);  // generate the packet with ack =-1 //should be curseq
        	pkt.setChecksum(); //generate the checksum
        	nl.sendPacket(pkt, Event.RECEIVER); //send the packet using the networkLayer
        	curPk++; //increment the packet number
        	//System.out.println(curSeq%ws);
        	 
        	
            curSeq=curSeq-curSeq%ws+ws;//increment the current seqence num
        	
        	

    		tl.setSendSoFar(curPk+1);
    		//tell the tl which packet it should sent so far, so tl can decide whether to add the next sending events or not
    		// Since this Curpk is begin from 0 but sendsofar in timeline is begin from 1. We have to add 1.
        	
        	System.out.println(curPk+" send");
        	
        	System.out.println("reset the sendCount");
        	
        	tl.startTimer(100); //start a timer. timeout = 100
    		
    		return false;
    	}
    	
    	else { //not the last paket to send in that window
    
    	Packet pkt = new Packet(msg, curPk, -1, 0); // generate the packet with ack =-1 //should be curseq
    	pkt.setChecksum(); //generate the checksum
    	nl.sendPacket(pkt, 1); //1 means receiver
    	curPk++; //increment the packet number
    	
    	curSeq = curSeq+mss;
    		
    	//increment the current seqence num

		tl.setSendSoFar(curPk+1);
		//tell the tl which packet it should sent so far, so tl can decide whether to add the next sending events or not
		// Since this Curpk is begin from 0 but sendsofar in timeline is begin from 1. We have to add 1.
    	sendCount ++; //increment the send count 
    	
    	System.out.println(curPk+" send");
    	if(tl.getNextSendEvent()==null) { //if there is no more sending event, this means that the last packet has already get sent 
    		tl.startTimer(100); //directly add the timer and wait for the ack packet from the receiver
    	}
    	return true;
    	}
    	
    	
    }

    /**
     * receive a packet from the receiver
     * @param pkt
     * @return true if the sender has receive ack for the last packet send, which means that the sender can go ahead and sent the next pakcet
     * false otherwise
     */
    public boolean receiveMessage(Packet pkt)
    {
    	if(pkt.isCorrupt()) { // check if the packet is corrupted
    		System.out.println("receiver ack packet is corrupted"); 
    		//it is, just ignore it.
    	}
    	else {
    		
    	int ack = pkt.getAcknum(); //get the acknum from the packet
    	System.out.println("ack for "+ack+" received");
    	packetAcked = ack; //update the packetedAcked received
    	if(ack==curPk) { // if the ack we get if for the last packet (should be curSeq)
    		return false;
    	}
    	System.out.println(ackNum);
    	if(ack == ackNum) {
    		numOfAcks++; //increment the num of acks
    		if(numOfAcks == 3) {//if there are 3 duplicate acks
    			
    			System.out.println("fast retransmit \n");
    			Event e=tl.getTimeoutEvent(); 
    			if(e!=null) {
    			e.time = tl.getCurrentTime();//get the time out event and change the time to now
    			}
    		}
    	}else {
    		ackNum=ack;
    	}
    	
    	
    	}
    	return true;
    }

    /**
     * if the timer expired.
     * find the next sending event (which send the next packet) in the events queue. 
     * Change the events time to current time
     */
    public void timerExpired()
    { 
    	if(tl.getNextSendEvent()!=null) {  //get the next sending event and remove it
    		tl.getEvents().remove(tl.getNextSendEvent());}
    		System.out.println();
    		System.out.println("set the seq to resend the packet lost");
    		
    		/*curSeq = packetAcked;
    		curPk = packetAcked/mss+1;*/
    		curPk = packetAcked; //change the cur packet to send to the last packet acked
    		tl.setSendSoFar(curPk+1); //change the sendsofar to last packet acked
    		sendCount = 0;
    		tl.createSendEvent(); //create a new sending event
    		
    
    }

    public void setTimeLine(Timeline tl)
    {
        this.tl=tl;
        
    }

    public void setWindowSize(int n)
    {
        ws =n;
        if(mss!=0)
        this.n=ws/mss+1;
    }
    
    public void setMSS(int n)
    {
       mss=n;
       if(mss!=0)
       this.n=ws/mss+1;
    }
    

    public void setProtocol(int n)
    {
        if(n>0)
            bufferingPackets=true;
        else
            bufferingPackets=false;
    }
    
 
    

}
