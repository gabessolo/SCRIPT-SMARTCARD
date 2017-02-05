package cypher;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * This interface will permit users to encrypt and decrypt in using 
 * asymetric cryptosystem with a given public key. 
 * @author Florian Guilbert
 * @version 1.0
 */
public interface IAsymCypher extends ICypher {
	
	
	/**
	 * This method returns the encryption key.
	 * @return the cypher key in base64 string format
	 */
	byte[] getPublicKey();
	
	/**
	 * This method verify the signature
	 * @param signedText: the text signed which has to be verify
	 *        plainText: the reference text 
	 * @return true if the message is authenticated, else false.
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws SignatureException 
	 */
	boolean verify(String signedText, String plainText) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;

}