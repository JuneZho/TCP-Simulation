import java.util.Scanner;
import java.lang.StringBuilder;
/*
 * This HTTP class simulates the HTTP protocol 
 * it provides a mutual understandable language to both the server and the client
 * both users are able to behave proporly accordingly
 */
public class HTTP {

    String item; // item to retrieve

    String host; // end host name
    String version;// http version number
    String date;// date of sending
    String lastModify;// date of last modify
    String connection; //connection states
    String user_agent; //agent of the user. in out lab is console
    String state; //state of the http  202 OK, 304 not_modified and 404 Not_Found
    String server; //server type    in out lab assume is "Apach"
    String length;//length of the content sending
    String If_Modified_Since;//when use cache, client send the original cache data get time to confirm if there are changes made
    int type; // 1= request 2 = if modified request 3 = response 4 = notmodify response
    byte[] entity;//content of the respond http
    
 
    /**
     * 
     * @return If_Modified_Since
     */

    public String getIf_Modified_Since() {
        return If_Modified_Since;

    }

    /**
     * 
     * @return item
     */
    public String getItem() {
        return item;
    }
    
    /**
     * 
     * @return host address
     */

    public String getHost() {
        return host;
    }

    /**
     * 
     * @return version of http
     */
    public String getVersion() {
        return version;
    }

    /**
     * 
     * @return date of sending
     */
    public String getDate() {
        return date;
    }

    /**
     * 
     * @return lastModified time
     */
    public String getLastModify() {
        return lastModify;
    }

    /**
     * 
     * @return connection state
     */
    public String getConnection() {
        return connection;
    }

    /**
     * 
     * @return user_agent
     */
    public String getUser_agent() {
        return user_agent;
    }
    
    /**
     * 
     * @return state of the http
     */

    public String getState() {
        return state;
    }
    
    /**
     * 
     * @return server type
     */

    public String getServer() {
        return server;
    }

    /**
     * 
     * @return length of the content
     */
    public String getLength() {
        return length;
    }
    
    /**
     * 
     * @return type of the http
     */
    public int getType(){
        return type;
    }
    
    /**
     * type 2 constructor
     * when use cache, client send this HTTP to confirm if there are changes made
     * @param item
     * @param version
     * @param host
     * @param since
     */

    public HTTP(String item, String version, String host, String since) {
        this.item = item;
        this.host = host;

        this.version = version;
        this.If_Modified_Since = since;

        type = 2;
    }

    /**
     * type 4 constructor
     *  when use cache,  after client send this HTTP to confirm if there are changes made(2)
     *  Server return this means there are no changes made
     * @param version
     * @param state
     * @param date
     */
    public HTTP(String version, String state, String date) {

        this.version = version;
        this.state = state;

        this.lastModify = date;
        type = 4;

    }
    /**
     * type 1 constructor
     * client request http
     * @param item
     * @param version
     * @param host
     * @param connection
     * @param user_agent
     */

    public HTTP(String item, String version, String host, String connection, String user_agent) {
        this.item = item;
        this.host = host;

        this.version = version;
        this.connection = connection;
        this.user_agent = user_agent;
        type = 1;
    }
    /**
     * server respond http
     * @param version
     * @param state
     * @param connection
     * @param date
     * @param server
     * @param last_modified
     * @param length
     * @param b
     */

    public HTTP(String version, String state, String connection, String date, String server, String last_modified,
    String length, byte[] b) {
        this.version = version;
        this.state = state;
        this.connection = connection;
        this.date = date;
        this.server = server;
        this.lastModify = last_modified;
        this.length = length;
        entity = b;
        type = 3;
    }
    /**
     * parse the byte[] data back to a HTTP
     * when server or client get the http(in byte[]) send from the other side, they have to use this to parse this method to parse the string data
     * @param data
     */

    public HTTP(byte[] data) {
        String http = new String(data);
        Scanner scan = new Scanner(http);

        String[] line1 = scan.nextLine().split(": ");
        if (line1[0].equals("GET")) {//type 1 or 2
            item = line1[1];

            String[] line2 = scan.nextLine().split(": ");
            version = line2[1];
            String[] line3 = scan.nextLine().split(": ");
            host = line3[1];
            String[] line4 = scan.nextLine().split(": ");
            if (line4[0].equals("If_Modified_Since")) { //type 2
                If_Modified_Since = line4[1];
                type = 2;

            } else { //type 1
                type = 1;
                connection = line4[1];
                String[] line5 = scan.nextLine().split(": ");
                user_agent = line5[1];
            }
        } else {
        	//type 3 or 4
            scan = new Scanner(http);
            String[] line_1 = scan.nextLine().split(": ");
            String[] info = line_1[1].split(" ");
            version = info[0] + " " + info[1];
            state = info[2]+" "+info[3];
            String[] line_3 = scan.nextLine().split(": ");

            if (line_3[0].equals("Last_Modified")) {//type 4
                
                type = 4;
                lastModify = line_3[1];

            } else {
            	//type 3
                type = 3;

                connection = line_3[1];
                String[] line_4 = scan.nextLine().split(": ");
                date = line_4[1];
                String[] line_5 = scan.nextLine().split(": ");
                server = line_5[1];
                String[] line_6 = scan.nextLine().split(": ");
                lastModify = line_6[1];
                String[] line_7 = scan.nextLine().split(": ");
                length = line_7[1];
                String[] line_8 = scan.nextLine().split(": ");

                if (!(line_8.length == 1)) {
                    String[] bytesString = line_8[1].split(" ");

                    byte[] bytes = new byte[bytesString.length];
                    for (int i = 0; i < bytes.length; ++i) {
                        bytes[i] = Byte.parseByte(bytesString[i]);
                    }

                    entity = bytes;
                } else {
                    entity = new byte[0];
                }
            }
        }

    }
    
    /**
     * parse the HTTP to a string
     * when server or client send http(in byte[]), they have to parse it into string. 
     * @return the string that this http parsed to
     */

    public String toString() {
        if (type == 1) {
            return "GET: " + item + "\n" + "version: " + version + "\n" + "Host: " + host + "\n" + "Connection: "
            + connection + "\n" + "User-agent: " + user_agent;
        }
        if(type==2) {

            return "GET: " + item + "\n" + "version: " + version + "\n" + "Host: " + host + "\n" + "If_Modified_Since: "+If_Modified_Since;
        }
        if (type == 3) {
            String entityString = null;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < entity.length; i++) {
                builder.append(entity[i] + " ");
            }
            entityString = builder.toString();
            return "Version: " + version + " " + state + "\n" + "Connection: " + connection + "\n" + "Date: " + date
            + "\n" + "Server: " + server + "\n" + "Last_Modified: " + lastModify + "\n" + "Content-Length: "
            + length + "\n" + "entity: " + entityString;
        }

        else {

            return "Version: " + version + " " + state + "\n" + "Last_Modified: " + lastModify; 
        }

    }

    

    /**
     * 
     * @return the byte[] that this http parse to
     */
    public byte[] toByte() {
        return this.toString().getBytes();

    }
    
    /**
     * 
     * @return the entity of this http
     */

    public String getEntity() {
        return new String(entity);
    }

   
}

