import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
 * This Browser class simulates a browser application 
 * Given an user instruction, a browser translates it into a http language and send it to the server.
 * Upon receiving a respond from the server, a browser also display the web page correctly for the user
 */
public class Browser {
	static final String PHTTPVersion = "HTTP /1.1";// persistent http version
	static final String NPHTTPVersion = "HTTP /1.0";// non persistent http version

	public boolean experiment; // experiment mode
	Map<String, byte[]> cache; // cache
	Map<String, String> map; //store the parse href information
	String requstedItem; // item to request. Store for the cache storage.
	public boolean persistent;// persistent connection
	private int count = 1;
	byte[] shelf;// temporary storage of requested cache content
	boolean allow_cache = false; // it is allow to use cache.

	ClientApp client; //client which use this browser

	
	/**
	 * contruction
	 * @param client
	 * @param experiment mode 
	 */
	public Browser(ClientApp client, boolean experiment) {
		this.client = client;
		this.experiment = experiment;
		cache = new HashMap<String, byte[]>(5); //create a cache
		map = new HashMap<String, String>();
		requstedItem = null;
	}

	/**
	 * 
	 * @return Allow_cache
	 */
	public boolean getAllow_cache() {
		return allow_cache;

	}

	/**
	 * 
	 * @return cache content to be print
	 */
	public byte[] get() {
		return shelf;
	}

	/**
	 * reset the counter for parse
	 */
	public void reset() {
		map.clear();
		count = 1;
	}

	/**
	 * 
	 * @param key
	 * @return if the cache contains the key
	 */
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}

	/**
	 * 
	 * @param key
	 * @return get data from the map
	 */
	public String get(String key) {
		return map.get(key);
	}
	
	/**
	 * parse one line in the requested file to see if there are other imgs to load or href to store
	 * @param line
	 * @return the parse result
	 */

	public String parse(String line) {
		String[] tokens = line.split(" ");

		if (tokens.length > 1) {

			if (tokens[1].equals("img")) { //if there is a img

				String item = tokens[tokens.length - 2]; //get rid of the img request sentence 
				//request for that img to the server
				if (!persistent) {//not persistent and should handshake
					client.handshakes(experiment);
				}
				sendHTTP(item, persistent);//send the HTTP to get that img 

				String img;
				if (cache.containsKey(item)) { //if the item is in the cache
					client.get_receive();
					HTTP http = new HTTP(cache.get(item));
					img = http.getEntity();
				} else {
					img = client.get_receive();
				}
				return img + "\n";
			}
			if (tokens[1].equals("href")) { //it is a href
				String item;
				if (tokens.length == 8) {
					item = count + ". Back to main page"; //number the href and its function
				} else {
					item = count + ". " + tokens[tokens.length - 2];
				}
				map.put(Integer.toString(count), tokens[2]);//put the href in the map
				count++;
				return item + "\n";
			}
		}
		return line + "\n";
	}

	/**
	 * display the data reveived
	 * @param data
	 * @return if it used cache
	 */
	public String display(byte[] data) {
		HTTP http = new HTTP(data);
		// System.out.println( http.getEntity() );
		if (http.getType() == 3) {
			if (!cache.containsKey(requstedItem)) {//cahce don't contains the item
				if (http.getState().equals("200 OK")) {//if it is a good item
					cache.put(requstedItem, http.toByte());//put the item name and http in the cache
					System.out.println("cache put in ");
				}
			} else {
				System.out.println(" found in cache ");
				// System.out.println(http.toString());
			}

			String stateLine = http.getState() + "\n"; //state
			String entity = http.getEntity(); //content in the http

			String temp = "";
			Scanner scan = new Scanner(entity);
			while (scan.hasNextLine()) { //get the next line of the content and parse it
				String line = scan.nextLine();
				temp += parse(line);
			}

			String result = stateLine + temp;

			return result;
		} else if (http.getType() == 4) { //if it is a last_modified confirm http
			System.out.println("304 Not_Modified");

			allow_cache = true;//go use the cahce
		}
		return null;

	}

	/**
	 * generate a HTTP request for the item
	 * 
	 * @param itemToRequest
	 * @param persistent connection or not
	 * @return if it used cache
	 */
	public boolean sendHTTP(String itemToRequest, boolean persistent) {
		this.persistent = persistent;
		if (cache.containsKey(itemToRequest)) { //item already in the cache
		    
			//send a if_modified_since http to see the data in the cache is out dated ort
			HTTP http = new HTTP(itemToRequest, PHTTPVersion, "www.interesting.com",
					new HTTP(cache.get(itemToRequest)).getDate()); //gerenerte the http
			client.send(http.toString());//send the http
			shelf = cache.get(itemToRequest);//temporary store the cache content for later usage
			return false;
		} else { //don't contain in the cache
			requstedItem = itemToRequest; 

			HTTP http;
			if (persistent) {//persistent
				http = new HTTP(itemToRequest, PHTTPVersion, "www.interesting.com", "open", "Console");
			} else {//non persistent
				http = new HTTP(itemToRequest, NPHTTPVersion, "www.interesting.com", "close", "Console");
			}
			client.send(http.toString());//send the http generated
			return true;
		}
	}

	
}
