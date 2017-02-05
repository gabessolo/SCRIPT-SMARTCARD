package Default;

/**
 * This class allows us to test
 * the {@link SoftCardServer} class.
 * @author Emmanuel Mocquet
 * @version 1.0 
 */
public class TestServer {

	public static void main(String[] args) {
		
		new SoftCardServer("0.0.0.0", 42425, 1);
		
	}
	
}
