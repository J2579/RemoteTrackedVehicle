package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;

public class Server {

	private ServerSocket server; //Server
	private Socket connection; //Socket connected to the client
	private int port;
	private BufferedReader input; //Read in data from client. Encapsulates the raw bytestream
	private OutputStream output; //lol
	private boolean stopped = false; //observe connection
	
	/**
	 * Given the command-line arguments, either attempts to parse a port from a JOptionPane (0 arg),
	 * or read directly from the command line (1 arg). If the user enters an invalid port in the JOptionPane,
	 * they are prompted to re-input; However, an invalid cmd arg causes the program to exit with an error message.
	 * 
	 * @param args The command-line arguments passed into main()
	 * @return The specified port number to listen for connections on
	 */
	public boolean setUpPortFromCMDArgs(String[] args) {
		
		int pport = -1; //parsed port
		
		if(args.length == 0) { //
			String portstr; //scoping
			do {
				portstr = JOptionPane.showInputDialog("Enter Port Number","3333");
				if(portstr == null) //user clicked cancel / x
					return false;
			} while((pport = validatePort(portstr)) == -1);	
		}
		else if(args.length == 1) {
			if((pport = validatePort(args[0])) == -1)
				return false;
		}
		else
			return false;
		
		this.port = pport;
		return true;
	}

	/**
	 * Validates a port number. If the string is a number n,
	 * where 0 <= n < 65536, the number is returned as an integer.
	 * Otherwise, returns -1.
	 * 
	 * @param portstr The port to validate
	 * @return The parsed port number, if valid. Otherwise, returns -1 on failure.
	 */
	private int validatePort(String portstr) {
		
		int port;
		
		try {
			port = Integer.parseInt(portstr); 
		}
		catch(NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Port must be an integer between 0 and 65535 (inclusive)!");
			return -1;
		}
		if(port < 0 || port > 65535) {
			JOptionPane.showMessageDialog(null, "Port must be an integer between 0 and 65535 (inclusive)!");
			port = -1;
		}
		return port;
	}
	
	/**
	 * Starts the server, and waits for a piece of Client code to make the connection.
	 * The InputStream is then wrapped in a BufferedReader because bytes are hard
	 * 
	 * @param port The port to listen for a connection on
	 * @throws IOException If an I/O ERROR occurs when setting up the Socket connection
	 * @return true If a connection was successfully made to a client, otherwise returns false
	 */
	public boolean startServerAndWaitForConnection() throws IOException {

		server = new ServerSocket(port);
		
		try {
			connection = server.accept(); //BLOCKING!!!
		}
		catch(SocketException e) {
			/*If this exception was thrown, the program was shut down while waiting
			for the client to connect. This just means that the server let us know
			an error occurred when trying to create the connection- nothing to worry 
			about! */
		}
		
		if(connection != null) {
			InputStream rawInput = connection.getInputStream();
			input = new BufferedReader(new InputStreamReader(rawInput));
			output = connection.getOutputStream();
			return true;
		}
		return false;
	}
	
	public String getNextLine() {
		
		String line;
		
		try {
			line = input.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		if(line.startsWith("!")) { //special information header
			if(line.substring(1).equals("END_CONNECTION")) //Client closed connection
				stop();
			return null; //We don't want to send information headers as movement commands!
		}
		
		return line;
	}	
	
	public void write(byte[] data, int len) {
		try {
			output.write(data, 0, len);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		stopped = true;
	}
	
	public boolean getIsStopped() {
		return stopped;
	}
	
	public boolean isConnected() {
		return input != null;
	}
	
	public void shutdown() {
		try {
			if(server != null)
				server.close();
			if(connection != null)
				connection.close();
		}
		catch(IOException e) { /*...*/ }
	}
}
