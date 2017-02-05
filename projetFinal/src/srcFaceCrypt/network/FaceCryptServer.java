package network;

/**
 * This class allows us to test
 * the Server class.
 * @author Baptiste Dolbeau, Florian Guilbert
 * @version 1.0 
 */
public class FaceCryptServer {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		ServerSSL serv = new ServerSSL("127.0.0.1", 4242, 15);

	}
}
