import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

//This class represents the server application
public class ServerApp
{
    //create a new transport layer for server (hence true) (wait for client)
    TransportLayer transportLayer;
    String state;

    static final String PHTTPVersion = "HTTP /1.1"; //persistent http version
    static final String NPHTTPVersion = "HTTP /1.0";

    double transmission_delay = 1; //delays
    long propogation_delay = 1;
    boolean experiment; //experiment mode or not 
    public static void main(String[] args) throws Exception
    {
        ServerApp server = new ServerApp();
        server.run(0.01, 1);
    }

    public void run(double transmission_delay, long propogation_delay){
        experiment = false;
        transportLayer = new TransportLayer(true, transmission_delay, propogation_delay);
        work();
    }

    /**
    public void run(){
    transportLayer = new TransportLayer(true, transmission_delay, propogation_delay);
    work();
    }
     */
    public void work(){
        while( true ) //always try to get the message that the client try to send through transportlayer
        {
            //receive message from client, and send the "received" message back.
            byte[] byteArray = transportLayer.receive();
            //if client disconnected
            if(byteArray==null) //if there is a message
                break;
            String str = new String ( byteArray );
            String[] token = str.split(",");
            if(token[0].equals("SYN = 1")){ //get and change SYN number
                //System.out.println( str );
                String[] parse = token[1].split("=");
                int client_isn = Integer.parseInt(parse[1]);
                String[] parse_2 = token[2].split("=");
                experiment = Boolean.parseBoolean(parse_2[1]);
                if(experiment){
                    transportLayer.set_experiment();
                    System.out.println("Hand Shake 2 delay: " + handShake_2(client_isn) + " seconds");
                }else{
                    handShake_2(client_isn); //handShake_2
                }
            }else{
                
                respond(byteArray); 
                //respond to this data
            }
        }  
    }

   
    public double handShake_2(int client_isn){
        return transportLayer.handShake_2(client_isn);
    }

    /**
     * form a http with given data
     * @param data
     * @param version
     * @param state
     * @param connection
     * @param date
     * @param server
     * @param last_modified
     * @param length
     * @return
     */
    public HTTP formHTTP(String data, String version, String state, String connection, String date, String server, String last_modified,
    String length){
        HTTP respond = new HTTP(version, state, connection, date, server, last_modified, length, data.getBytes());
        //HTTP respond = new HTTP("HTTP 1.1", "200 OK", connection, date, server, last_modified, length, data.getBytes());
        //System.out.println(respond.getState());
        return respond;
    }
    
    /**
     * load the file that client requested from the database
     * @param title
     * @return
     */

    public String loadFile(String title){
        String file = "";
        String temp = null;
        try{
            String fileName = "../website_example/" + title;//try to get the file
            FileReader fileReader = new FileReader(fileName);
            BufferedReader pageReader = new BufferedReader(fileReader);
            while((temp = pageReader.readLine()) != null){              
                file += temp + "\n";
            }
            pageReader.close();
            state = "200 OK"; //file found
        }catch(FileNotFoundException e){
            //System.out.println("404");
            state = "404 Not_Found";
        }catch(Exception e){
            System.out.println("I/O exception when reading file");
        }
        return file;
    }

    /**
     * respond to the http that client send
     * @param  request http that client send 
     */
    public void respond(byte[] byteArray){
        //receive
        HTTP request = new HTTP(byteArray);

        String version = request.getVersion();//get the version of the http
        String fileName = request.getItem();//get the item that client want to request
        HTTP result;
        if(request.getType()==1){// if it is a request http
            String connection;
            if(version.equals(PHTTPVersion)){
                connection = "connectioned";              
            }
            else{//non persitent, close the connection
                connection = "close";      
            }

            //respond
            String file = loadFile(fileName);//get the file

            result = formHTTP(file, version, state, connection, getDate(), "Apache", "2018/3/3 12:12:12",""+file.getBytes().length*8);//generate the respond http
        }

        else{ //if it is a if_modified_since check http
            String if_modified_since = request.getIf_Modified_Since(); //get the if_modified_since date
            String last_modified = getLastModified(fileName); // get the last_modifed time
            Date IfSince= null;
            Date Last = null;

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            try{
                IfSince =sdf.parse(if_modified_since);
                Last =sdf.parse(last_modified);

            }catch(Exception e){
                System.out.println("Exception in parse date");
            }
            //parse this two tring
            if(IfSince.compareTo(Last)==-1){// if not change after if_modified_since date
                
            	//send a http to confirm that the client can use cache
                result = new HTTP(version,state,last_modified);
            }else{
            	// client can not use cache since it is modified
            	//just send a standard respond file
                String connection;
                if(version.equals(PHTTPVersion)){
                    connection = "connectioned";              
                }
                else{
                    connection = "close";      
                }

                //respond
                String file = loadFile(fileName);
                

               result = formHTTP(file, version, state, connection, getDate(), "Apache", "2018/3/3 12:12:12",""+file.getBytes().length*8);
        
            }
        }

        
        //send it to the transport layer

        double delay = transportLayer.send(result.toByte() );

        if(experiment){
            System.out.println("delay is: "+ delay);
        }

    }
    /**
     * get last modified date of a given file
     * @param filename
     * @return the string of that date
     */

    public String getLastModified(String filename){

        try{

            File f = new File(filename);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            //System.out.println("last modified time:"+sdf.format(f.lastModified()));

            return sdf.format(f.lastModified());
        }catch(Exception e){
            System.out.println("File not found");

            return null;
        }
    }
    
    /**
     * get the sending date, which is the current date
     * @return the string of that date
     */

    public String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String dateOfSending = dateFormat.format(date);
        return dateOfSending;
    }
}


