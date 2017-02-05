package cypher;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * This interface will permit users to encrypt and decrypt message
 * @author Dolby
 * @version 1.0
 */
public interface ICypher {
	
	/**
	 * This method encrypt a plaintext.
	 * @param plaintext
	 * @return The plaintext encrypted
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * <pre>plaintext can't be null</pre>
	 */
	byte[] encrypt(byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;
	
	/**
	 * This method decrypt a cyphertext.
	 * @param cyphertext
	 * @return The cypher decrypted
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * <pre>cyphertext can't be null</pre>
	 */
	byte[] decrypt(byte[] cyphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;
	
	/**
	 * This method return the algorithm which is use by 
	 * ICypher to encrypt or decrypt.
	 * @return the algorithim in string format
	 */
	String getAlgorithm();
	
}