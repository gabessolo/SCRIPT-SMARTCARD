package network; 

import java.io.IOException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


/**
 * This class will create a multi-thread Server.
 * It is able to listen clients and send them an
 * hard-coded message 
 * @author Baptiste Dolbeau
 * @version 1.0
 */
public class ServerSSL{

	static SSLServerSocket servSocket;
	private InetAddress adresse = null;
	static ProcessusSockSSL ps = null;
	
	
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
	public ServerSSL(String adr, int port, int maxConn) {
		try {
			adresse = Inet4Address.getByName(adr);			
		} catch (UnknownHostException e) {
			System.err.println("Address invalid");
			e.printStackTrace();
			System.exit(1);
		}
		
		setContext();
		SSLServerSocketFactory sslSrvFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		
		try {
			servSocket = (SSLServerSocket) sslSrvFact.createServerSocket(port, maxConn, adresse);
			this.listening();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error socket");
			System.exit(2);
		}
		
	}
	
	/**
	 * This method set up the SSL context. It specify the
	 * keystore which contains the server's certificate
	 * and its public key. 
	 */
	public void setContext() {
		System.setProperty("javax.net.ssl.keyStore", "cert/facecryptSERVER.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "lolilol");
		
		System.setProperty("javax.net.ssl.trustStore", "cert/trusts.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "lolilol");
	}
	
	/**
	 * This method make the server in listening mode.
	 * For each client connection, it create an object
	 * which extends thread.
	 */
	public void listening() {
		SSLSocket sock = null;
		
		System.out.println("Waiting for connections...");
		while (true) {  	
			try {
				sock = (SSLSocket) servSocket.accept();
				ps = new ProcessusSockSSL(sock);
			} catch (IOException e) {
				System.err.println("Error establishing socket");
				e.printStackTrace();
				System.exit(3);
			}
		}
	}
}	
