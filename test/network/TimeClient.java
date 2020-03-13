package network;

import java.net.*;
import java.io.*;

//I ripped this code from the internet, to test basic socket functionality over the RVC Network.
//Additionally, the concepts in this code assisted me in creation of test/model/HalfModelUIWithNetwork, as
//well as src/network/MoveClient

public class TimeClient {
 
	public static void usage() {
		System.err.println("usage: TimeClient <ip> <port>");
		System.exit(-1);
	}

    public static void main(String[] args) {
        if (args.length != 2) usage();
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        try (Socket socket = new Socket(hostname, port)) {
 
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            String time = reader.readLine();
 
            System.out.println(time);
 
 
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}