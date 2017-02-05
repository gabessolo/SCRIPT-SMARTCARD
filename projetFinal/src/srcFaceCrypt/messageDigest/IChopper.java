package messageDigest;

import java.security.NoSuchAlgorithmException;

/**
 * This interface will permit users to hash plain text in using 
 * hash algorithm. 
 * @author Florian Guilbert
 * @version 1.0
 */

public interface IChopper {
	
	/**
	 * Hash the param with the algorithm given at the initialization.
	 * @param plainText
	 * @return a hexa-string of the plainText's hash
	 * @throws NoSuchAlgorithmException 
	 * <pre>plainText != null && plainText != ""</pre>
	 */
	String hash(String plainText) throws NoSuchAlgorithmException;
	
	/**
	 * @return the algorithm
	 */
	String getAlgorithm();
	
}
