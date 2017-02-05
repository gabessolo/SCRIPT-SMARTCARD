package cypher;
/**
 * This interface will permit users to encrypt and decrypt in using 
 * symetric cryptosystem with a given key and a given IV if it's needed. 
 * @author Florian Guilbert
 * @version 1.0
 */
public interface ISymCypher extends ICypher {
	
	
	/**
	 * This method returns the encryption key.
	 * @return the cypher key in base64 string format
	 */
	byte[] getSecretKey();

}