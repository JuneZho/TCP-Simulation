import java.util.Random;
/*
 * This TransportLayer class simulates a Transport Layer
 * it receives message from the application layer and sends it to the network layer with an additional TransportLayer header
 * it also receives message from the network layer and sends it to the application layer after decoding the TransportLayer header
 */
public class TransportLayer
{
    int seq;
    int expect;
    private NetworkLayer networkLayer;
    /**
     * server is true if the application is a server (should listen) or false if it is a client (should try and connect)
     * @param transmission_delay sets the # of seconds of transmission_delay for each byte
     * @param propogation_delay sets the # of seconds of propogation_delay for each packet
     */
    public TransportLayer(boolean server, double transmission_delay, long propogation_delay)
    {
        networkLayer = new NetworkLayer(server, transmission_delay, propogation_delay);
    }
    
    /**
     * Set the transport layer into experiment mode
     */
    public void set_experiment(){
        networkLayer.set_experiment();
    }
    
    /**
     * TransportLayer Constructor
     *
     * server is true if the application is a server (should listen) or false 
     * if it is a client (should try and connect)@param server A parameter
     */
    public TransportLayer(boolean server){
        networkLayer = new NetworkLayer(server);
        
    }
    
    /**
     * Method set_transmission_delay
     *
     * @param transmission_delay A parameter represents the # of second delay for each byte
     */
    public void set_transmission_delay(double transmission_delay){
        networkLayer.set_transmission_delay(transmission_delay);
    }
    
    /**
     * Method set_propogation_delay
     *
     * @param propogation_delay A parameter represents the # of seconds delay for each packet
     */
    public void set_propogation_delay(long propogation_delay){
        networkLayer.set_propogation_delay(propogation_delay);
    }

    /**
     * Method send
     * sends a transport layer datagram with the given payload with additional transport layer header
     * @param payload A parameter represents the data given by the application layer
     * @return The return value represents the time it takes for the packet to arrive to the other side
     */
    public double send(byte[] payload)
    {
        String header = "SYN = 0,seq =" + seq + ",ack = " + expect + ",";
        String data = header + new String(payload);
        byte[] packet = data.getBytes();

        double delay = networkLayer.send( packet );
        
        seq = seq + payload.length*8;
        
        return delay;
    }

    /**
     * Method receive
     * Receives a packet from the network layer
     * deals with the different header information accordingly
     * gives the application layer the data received from the other side
     * @return The return value represents the data received from the other side
     */
    public byte[] receive()
    {
        byte[] payload = networkLayer.receive();  
        String data = new String(payload);
        String[] token = data.split(",");
        if(token[0].equals("SYN = 0")&&token.length == 4){
            if(token[1].equals("seq ="+expect)){
                if(token[2].equals("ack = "+String.valueOf(seq))){
                    byte[] packet = token[3].getBytes();
                    expect = expect + packet.length * 8;
                    return packet;
                }else{
                    System.out.println("ack number wrong, packet lost");
                    System.out.println("expect: " + seq);
                    System.out.println("got: "+ token[2]);
                }
            }else{
                System.out.println("sequence number wrong, packet lost");
                System.out.println("expect: " + expect);
                System.out.println("got: "+ token[1]);
            }
        }else if(token[0].equals("SYN = 1")&&(token.length == 3||token.length == 4)){
            expect++;
            //System.out.println(new String(payload));
            return payload;
        }
        else{
            System.out.println("unrecognized format");
        }

        System.out.println("interesting");
        return payload;
    }

    /**
     * Method handShake_1
     * The first step for a TCP connection 
     * @param experiment A parameter tells the server whether the client is doing an experiment
     * @return The return value represents the time it toke to send this message
     */
    public double handShake_1(boolean experiment){
        System.out.println("Requesting Connection");
        seq = new Random().nextInt(100)+1;
        String SYN = "SYN = 1,seq =" + seq + ",experiment =" + experiment;
        byte[] syn_request = SYN.getBytes();
        double time = networkLayer.send(syn_request);
        seq++;
        return time;
    }

    /**
     * Method handShake_2
     * The second step for a TCP connection, acknowledging the request from the client 
     * @param ack A parameter represents the sequence number of next bit server is expecting
     * @return The return value represents the time it toke to send this message
     */
    public double handShake_2(int ack){  
        ack= ack+1;
        expect = ack;
        System.out.println("Accepting Connection");
        seq = new Random().nextInt(100)+1;
        String SYNACK = "SYN = 1,seq =" + seq + ",ack = " + expect;
        byte[] syn_ack = SYNACK.getBytes();
        double time = networkLayer.send(syn_ack);
        seq++;
        return time;
    }

    /**
     * Method getAck
     * @return The return value the acknowledge number of this transport layer
     */
    public int getAck(){
        return expect;
    }

    public void setAck(int x){
        expect = x;
    }

}

