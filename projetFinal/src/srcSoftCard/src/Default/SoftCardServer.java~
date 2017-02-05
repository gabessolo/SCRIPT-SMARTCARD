//package Default;

import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.smartcardio.CardException;

import org.apache.commons.codec.binary.Hex;


/**
 * This class will create a multi-thread Server.
 * It is able to listen clients and send them an
 * hard-coded message 
 * @author Baptiste Dolbeau, Emmanuel Mocquet
 * @version 1.0
 */
public class SoftCardServer {

	static SSLServerSocket servSocket;
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
	 * @param the address of the server.
	 * @param the port on which the server will listen.
	 * @param the number of connection allowed.
	 */
	public SoftCardServer(String adr, int port, int maxConn) {
		try {
			adresse = Inet4Address.getByName(adr);			
		} catch (UnknownHostException e) {
			System.out.println("Adresse non valide");
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
		System.setProperty("javax.net.ssl.keyStore", "certs/carte.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "lolilol");
		System.setProperty("javax.net.ssl.trustStore", "certs/trustClientFaceCrypt.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "lolilol");
	}

	/**
	 * This method make the server in listening mode.
	 * For each client connection, it create an object
	 * which extends thread.
	 */
	public void listening() {
		SSLSocket sock = null;
		while (true) {
			try {
				sock = (SSLSocket) servSocket.accept();
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
 * It has few methods for its management.
 * 
 * @author Baptiste Dolbeau, Emmanuel Mocquet
 * @version 1.0
 */
class ProcessusSock extends Thread {

	private SSLSocket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;

	private final byte QUIT = (byte) 0x41;
	private final byte GET_KEY = (byte) 0x42;
	private final byte GET_RANDOM_NUMBER = (byte) 0x43;
	private final byte DECRYPT= (byte) 0x44;
	private final byte IS_UNLOCKED = (byte) 0x45;
	private final byte UNLOCK = (byte) 0x46;
	private final byte RETRIEVE_CRED = (byte) 0x47;
	private final byte STORE_CREDENTIALS = (byte) 0x48;
	private final byte RESET_PWD = (byte) 0x49;
	private final byte VALIDATE_PWD = (byte) 0x50;

	/**
	 * This constructor links a reader and
	 * a printer to the socket and start
	 * the thread.
	 * 
	 * @param the client's socket.
	 * @throws IOException
	 */
	public ProcessusSock(SSLSocket socket) throws IOException {
		this.socket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			this.start();
		} catch (IOException e) {
			free();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("exception");
			free();
		}
	}


	private static String bytesToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("0x%02x ", b));
		}
		return new String(sb);
	}

	/**
	 * This method will be executed by the
	 * thread. It follows the rule :
	 * One message received, One message sent.
	 * 
	 * An identifier is sent to the server (us), to inform it the task to do.
	 */
	public void run() {
		byte[] mess = {};
		boolean cont = true;
		System.out.println("Client connected.");
		do {
			try {
				mess = receiveMessages();
				if (mess.length > 0) {
					cont = doAction(mess);
				}
			} catch (Exception e) {
				System.err.println("Error while receiving/sending data.");
				cont = false;
			}
		} while (cont);

		try {
			this.disconnectCard();
			free();
		} catch (IOException e) {
			System.err.println("An error occured while closing connection.");
		} catch (Exception e) {
			System.err.println("An error occured while disconnecting the card.");
		}
	}

	/**
	 * This is the principal principal method the server : each time the client
	 * sends a request (to obtain the public key, get the user's ID, etc.) this
	 * method try to translate the first byte containing the action to do. 
	 * It then trigger the method associated and send a message containing the 
	 * result.
	 * @param the message received from the client, containing an ID and 
	 * the data, if necessary.
	 * @return the result of the execution of the associated methods (as a {@link boolean}) 
	 * @throws IOException with the associated messsage, if the client is not reachable.
	 */
	private boolean doAction(byte[] mess) throws IOException {
		boolean res = true;
		byte id = mess[0];
		byte[] data;
		
		switch(id) {
		// Send public key
		case GET_KEY:
			try {
				byte[] key = this.getPublicKey();
				sendMessage(key);
			} catch(CardException ce) {
				sendMessage(NetworkException.ERROR_CONNECTION_CARD);			
			} catch (Exception e) {
				sendMessage(NetworkException.ERROR_PUBKEY);
			}
			break;

			// Send random number
		case GET_RANDOM_NUMBER:
			byte nb = mess[1];
			try {
				sendMessage(this.getRandomNumber(nb));
			} catch(CardException ce) {
				sendMessage(NetworkException.ERROR_CONNECTION_CARD);			
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(NetworkException.ERROR_RANDOM_NUMBER);		
			}
			break;

			// retrieve the ciphered data and decrypt it.
		case DECRYPT:
			data = new byte[mess.length - 1];

			System.arraycopy(mess, 1, data, 0, mess.length - 1);
			try {
				sendMessage(this.decryptData(data));
			} catch (Exception e) {
				sendMessage(NetworkException.ERROR_DECRYPT);		
			}
			break;

			// check if the card is unlocked.
		case IS_UNLOCKED:
			try {
				sendMessage((isUnlocked())? new byte[]{1} : new byte[]{0});
			} catch (Exception e) {
				sendMessage(NetworkException.ERROR_CHECK_LOCKED);		
			}
			break;

			// client wants to manually unlock the card
		case UNLOCK:
			data = new byte[mess.length - 1];
			System.arraycopy(mess, 1, data, 0, mess.length - 1);
			try {
				sendMessage(unlock(data)? new byte[]{1} : new byte[]{0});
			} catch (Exception e) {
				sendMessage(NetworkException.ERROR_UNLOCK_CARD);
			}
			break;

			// client wants to store his credentials.
		case STORE_CREDENTIALS:
			data = new byte[mess.length - 1];
			System.arraycopy(mess, 1, data, 0, mess.length - 1);
			try {
				sendMessage(storeCredentials(data)? new byte[]{1} : new byte[]{0});
			} catch (Exception e) {
				System.err.println(e.getMessage());
				sendMessage(NetworkException.ERROR_STORE_ID);

			}
			break;

			// client wants to retrieve his credentials.
		case RETRIEVE_CRED:
			try {
				sendMessage(retrieveCredentials());
			} catch (Exception e) {
				sendMessage(NetworkException.ERROR_GET_ID);

			}
			break;

			// client wants to modify the password.
		case RESET_PWD:
			try {
				sendMessage(resetPassword());
			} catch (Exception e) {
				sendMessage(NetworkException.ERROR_RESET_PASSWORD);
			}
			break;

			// client tells the card that the password was validated.
		case VALIDATE_PWD:
			try {
				System.out.println(bytesToHexString(mess));
				sendMessage(validatePassword()? new byte[]{1} : new byte[]{0});
			} catch (Exception e) {
				sendMessage(NetworkException.ERROR_VALIDATE_PASSWORD);
			}
			break;

			// client wants to disconnect.
		case QUIT:
			res = false;
			try {
				disconnectCard();
			}
			catch(Exception e) {}
			System.out.println("Client disconnected.");
			break;
		}
		return res;

	}



	/**
	 * This method calls the eponymous method of {@link SoftCard} to
	 * reset the user's password
	 * @return the new password, as a bytes' array.
	 * @throws Exception with the reason message, if an error 
	 * occured on the smartcard's side.
	 */
	private byte[] resetPassword() throws CardException, Exception {
		System.out.println(1);
		return SoftCard.getInstance().resetPassword();
	}


	/**
	 * This method calls the eponymous method of {@link SoftCard} to
	 * tell the card to replace the actual password by the temporary one 
	 * @return <code>true</code> if no error occured.
	 * @throws Exception with the reason message, if an error 
	 * occured on the smartcard's side.
	 */
	private boolean validatePassword() throws CardException, Exception {
		return SoftCard.getInstance().validatePassword();
	}

	/**
	 * This method calls the eponymous method of {@link SoftCard} to
	 * store the user's login.
	 * @param the login to be stored.
	 * @return <code>true</code> if the operation succeeded.
	 * @throws Exception with the reason message, if an error 
	 * occured on the smartcard's side.
	 */
	private boolean storeLogin(byte[] login) throws CardException, Exception {
		return SoftCard.getInstance().storeLogin(login);
	}

	/**
	 * This method calls the eponymous method of {@link SoftCard} to 
	 * store the user's login and password. 
	 * @param data 
	 * @return <code>true</code> if the all went well.
	 * @throws Exception with the reason message, if an error
	 * occured on the smartcard's side.
	 */
	private boolean storeCredentials(byte[] data) throws CardException, Exception {
		return SoftCard.getInstance().storeCredentials(data);
	}

	/**
	 * This method calls the eponymous method of {@link SoftCard} to
	 * retrieve user's Facebook credentials.
	 * @return the user's credentials as a bytes' array.
	 * @throws Exception with the reason message, if an error 
	 * occured on the smartcard's side.
	 */
	private byte[] retrieveCredentials() throws CardException, Exception {
		return SoftCard.getInstance().retrieveCredentials();
	}

	/**
	 * This method calls the eponymous method of {@link SoftCard} to 
	 * verify the unlocking of the smartcard.
	 * @return <code>true</code> if the Card if the card is unlocked, false otherwise
	 * @throws CardException with the reason message, if an error
	 * occured on the smartcard's side.
	 */
	private boolean isUnlocked() throws CardException, Exception {
		return SoftCard.getInstance().isUnlocked();		
	}

	/**
	 * This method calls the eponymous method of {@link SoftCard} to 
	 * verify the PIN.
	 * @return <code>true</code> if the Card if the PIN is correct, false otherwise
	 * @throws Exception with the reason message, if an error
	 * occured on the smartcard's side.
	 */
	private boolean unlock(byte[] pin) throws CardException, Exception {
		return (SoftCard.getInstance().unlock(pin) == (byte)1) ? true : false;
	}

	/**
	 * This method calls disconnectCard from {@link SoftCard} to
	 * obtain a random number.
	 * @throws CardException with the reason message, if an error
	 * occured on the smartcard's side.
	 * @see SoftCard
	 */
	private void disconnectCard() throws Exception {
		SoftCard.getInstance().disconnect();
	}

	/**
	 * This method calls getRandomNumber from {@link SoftCard} to obtain
	 * a random number.
	 * @return an random number in a bytes' array
	 * @throws Exception with the reason message
	 * @see SoftCard
	 */
	private byte[] getRandomNumber(byte nb) throws CardException, Exception {
		return SoftCard.getInstance().getRandomNumber(nb);
	}

	/**
	 * This method calls decryptData from {@link SoftCard} to obtain
	 * the decrypted data.
	 * @return the decrypted data in a bytes' array
	 * @throws Exception with the reason message
	 * @see SoftCard
	 */
	private byte[] decryptData(byte[] data) throws CardException, Exception {
		SoftCard c = SoftCard.getInstance();
		return c.decryptData(data);
	}

	/**
	 * This method calls getPublicKey from {@link SoftCard} to obtain
	 * the public key stored in the smartcard.
	 * @return the public key in a bytes' array
	 * @throws Exception with the reason message
	 * @throws CardException if an error occured on the smartcard's side
	 * @see SoftCard
	 */
	private byte[] getPublicKey() throws CardException, Exception {
		SoftCard c = SoftCard.getInstance();
		byte[] publicKey = c.getPublicKey();
		return publicKey;
	}

	/**
	 * This method will retrieve a response from
	 * the DataOutputStream. It will first read the
	 * length of data to receive, then read it.
	 * @return The received data and its length
	 * @throws IOException
	 */
	private byte[] receiveMessages() throws IOException {
		int i = in.readInt();

		if (i > 2560 ) {
			throw new IOException("Data too big");
		}

		byte[] b = new byte[i];
		in.read(b, 0, i);
		return b;
	}

	/**
	 * This method will write data into
	 * the DataInputStream object to send on the socket.
	 * It will first write the length's data, then the data
	 * itself. 
	 * Finally, it flushes the buffer.
	 * @param The data to send
	 * @throws IOException if the data could not be sent.
	 */
	private void sendMessage(byte[] b) throws IOException {
		out.writeInt(b.length);
		out.write(b, 0, b.length);
		out.flush();
	}

	/**
	 * This method will write data into
	 * the DataInputStream object to send on the socket.
	 * It will first write the length's data, then the data
	 * itself. 
	 * Finally, it flushes the buffer.
	 * @param The data to send, an exception.
	 * @throws IOException if the data could not be sent.
	 */
	private void sendMessage(NetworkException ne) throws IOException {
		byte[] b = ne.getValue();
		out.writeInt(b.length);
		out.write(b, 0, b.length);
		out.flush();
	}


	/**
	 * This method closes the printer and reader
	 * and closes the socket.
	 * @throws IOException
	 */
	private void free () throws IOException {
		in.close();
		out.close();
		socket.close();
	}
}



