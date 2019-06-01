import java.io.BufferedReader;
import java.io.InputStreamReader;
/*
 * This ClientApp class represents the client application layer
 * After receiving an user input, it delievers the request to the server
 * Then, it displays the result for the user after receiving an respond from the server
 */
public class ClientApp
{
    //create a new transport layer for client (hence false) (connect to server), and read in first line from keyboard
    TransportLayer transportLayer;
    Browser browser;//brower
    boolean experiment;//experiment mode or not
    double transmission_delay = 1; //delays
    long propogation_delay = 1;

    public static void main(String[] args) throws Exception
    {
        ClientApp client = new ClientApp();
        client.run(0.1, 1);
    }

    public void run(double transmission_delay, long propogation_delay){
        experiment = true;
        //store the data
        this.transmission_delay = transmission_delay;
        this.propogation_delay = propogation_delay;
        if(experiment){//Experiment mode
            transportLayer = new TransportLayer(false, transmission_delay, propogation_delay);
            transportLayer.set_experiment();
        }else{
            transportLayer = new TransportLayer(false, transmission_delay, propogation_delay);
        }
        browser = new Browser(this, experiment); //Browsers that used
        establishConnection(true);  
    }

    public void print(String data){//print of the content in the console
        System.out.println(data);   
    }
    /**
     * establishConnection between client and server
     * @param persistent or non persistent http
     */

    public void establishConnection(boolean persistent){
        handshakes(experiment);//3 handshakes for establish connection

        System.out.println("Please give instructions: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));//get the intruction
        String line = read(reader);
        int index = 0;
        //while line is not empty
        while( line != null && !line.equals("") )
        {

            //convert lines into byte array, send to transoport layer and wait for response

        	//if the map contains it. Which means it is a href number
            if(browser.containsKey(line)){
                line = browser.get(line);//get it from the map
            }

            if((!persistent) && index > 0){
                System.out.println("non persistent");
                handshakes(experiment); //handshake for each request if it is non persistent
            }

            boolean cache = browser.sendHTTP(line,persistent);
            receive();

            if(!cache){  //if the cache contains the http to send
                if(browser.getAllow_cache()){ //if allow to use cache (already confirmed with server with the last_modified date)
                    useCache(browser.get()); //use the cache
                    browser.allow_cache=false; //not allow to use now
                }}

            index ++;
            System.out.println("Please give instructions: ");
            line = read(reader);
        }
    }
    
    /**
     * handshake 1 and 2 
     * @param experiment mode or not
     */

    public void handshakes(boolean experiment){

        double time = transportLayer.handShake_1(experiment);//get the handshake 1 time
        if(experiment){
            System.out.println("Hand Shake 1 delay: "+time + " seconds");
        }
        byte[] synack = transportLayer.receive(); //get the syn and ack string
        String syn_ack = new String ( synack );
        //System.out.println( "im here" );
        String[] token = syn_ack.split(",");
        if(token[0].equals("SYN = 1")){ //confirm and chanhe the SYN and ack number
            String[] parse = token[1].split("=");
            int server_isn = Integer.parseInt(parse[1]);
            transportLayer.setAck(server_isn+1);
        }else{
            System.out.println("unexpected reply");
        }

    }

    /**
     * read the line using bufferedreader
     * @param reader
     * @return
     */
    public String read(BufferedReader reader){
        try{
            String line = reader.readLine();
            return line;
        }catch(Exception e){
            System.out.println("read line error");
            return null;
        } 
    }

    /**
     * send the http string data to the transportlayer
     * @param data
     */
    public void send(String data){
        //convert lines into byte array, send to transoport layer and wait for response
        byte[] byteArray = data.getBytes();

        // send the data
        double delay = transportLayer.send( byteArray );

        if(experiment){
            System.out.println("delay is: " + delay + " seconds");
        }
    }
    
    /**
     * use cache to print content
     * @param byteArray
     */

    public void useCache(byte[] byteArray){
        browser.reset();//reset the browser map for parse href
        String data = browser.display(byteArray);
        if(data != null){ // if there is data, print it
        System.out.println(data);}
    }

    /**
     * recerive the http from the server and print it
     */
    public void receive(){

        byte[] byteArray = transportLayer.receive(); //receive from the transportLayer
        //System.out.println(new String(byteArray));
        browser.reset(); //reset the browser map for parse href
        String data = browser.display(byteArray);//display it

        if(data != null){
        System.out.println(data);}
    }
    /**
     * get the receive data
     * @return the data received
     */

    public String get_receive(){
        byte[] byteArray = transportLayer.receive();//receive from the transportLayer
        String data = browser.display(byteArray);
        return data;
    }
}

