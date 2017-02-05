package faceCrypt_extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;




/**
 * This class will create a multi-thread Server.
 * It is able to listen clients and send them an
 * hard-coded message 
 * @author Baptiste Dolbeau
 * @version 1.0
 */
public class Server{

	static ServerSocket servSocket;
	private InetAddress adresse = null;
	static ProcessusSock ps = null;
	
	
	/**
	 * This constructor initialize a Server.
	 * First it translates the string address
	 * into an InetAddress object.
	 * Then it set up the SSL Context (certificate,
	 * etc). 
	 * Finally it creates an SSL Socket listening
	 * on the port specified.
	 * @param adr Is the address of the server.
	 * @param port Is the port on which the server will listen.
	 * @param maxConn Specify the number of connection allowed.
	 */
	public Server(String adr, int port, int maxConn) {
		try {
			adresse = Inet4Address.getByName(adr);			
		} catch (UnknownHostException e) {
			System.out.println("Adresse non valide");
			e.printStackTrace();
			System.exit(1);
		}
		
		//setContext();
		//SSLServerSocketFactory sslSrvFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		
		try {
			//servSocket = (SSLServerSocket) sslSrvFact.createServerSocket(port, maxConn, adresse);
			servSocket = new ServerSocket(port, maxConn, adresse);
			this.listening();
		} catch (IOException e) {
			System.out.println("Problème Socket");
			System.exit(2);
		}
		
	}
	
	/**
	 * This method set up the SSL context. It specify the
	 * keystore which contains the server's certificate
	 * and its public key. 
	 */
	public void setContext() {
		System.setProperty("javax.net.ssl.keyStore", "/home/administrateur/faceCrypt/certTest/server.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "lolilol");
	}
	
	/**
	 * This method make the server in listening mode.
	 * For each client connection, it create an object
	 * which extends thread.
	 */
	public void listening() {
		Socket sock = null;
		
		System.out.println("En attente de clients !");
		while (true) {  	
			try {
				sock = servSocket.accept();
				ps = new ProcessusSock(sock);
			} catch (IOException e) {
				System.out.println("Problème Ecoute Socket");
				System.exit(3);
			}
		}
	}
}	
	

/**
 * This class represents a client thread.
 * It as few methods for its management.
 * 
 * @author Baptiste Dolbeau
 * @version 1.0
 */
class ProcessusSock extends Thread {

	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	
	/**
	 * This constructor link a reader and
	 * a printer to the socket and start
	 * the thread.
	 * 
	 * @param socket The client's socket.
	 * @throws IOException
	 */
	public ProcessusSock(Socket socket) throws IOException {
		this.socket = socket;
		try {
			in = new BufferedReader (new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());	
			this.start();
		} catch (IOException e) {
			free();
		}
	}

	/**
	 * This method will be executed by the
	 * thread. It follows the rule :
	 * One message received, One message sent.
	 */
	public void run() {
		String mess = "";
		System.out.println("Client connecté");
		try {
			mess = receiveMessages();
			System.out.println(mess);
		} catch (IOException e) {
			System.err.println("Problème réception message");
		}
		try {
		while(!mess.equals("quit")) {
			sendMessage("Ta mère aussi");
			try {
				mess = receiveMessages();
				System.out.println(mess);
			} catch (IOException e) {
				System.err.println("Problème réception message");
			}
		} 
		} catch (Exception e) {
			System.out.println("Plus de message");
		}
			
		sendMessage("Fin de connexion");
		try {
			free();
		} catch (IOException e) {
			System.out.println("Problème fermeture buffer + socket");
		}
	}

	/**
	 * This method will retrieve a response from
	 * the bufferedReader object
	 * @return The message received.
	 * @throws IOException
	 */
	private String receiveMessages() throws IOException {
		return in.readLine();
	}
	
	/**
	 * This method will write a message into
	 * the PrintWriter object to send on the socket.
	 * Then, it flush the buffer.
	 * @param s The message to send
	 */
	private void sendMessage(String s) {
		out.println(s);
		out.flush();
	}
	
	/**
	 * This method close the printer and reader
	 * and close the socket.
	 * @throws IOException
	 */
	private void free () throws IOException {
		in.close();
		out.close();
		
		socket.close();
	}
}


