package cypher;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class will present each symmetric algorithm
 * available in our project
 * @author Dolby
 * @version 1.0
 */
public class SymCypher implements ISymCypher {

	private String algorithm = "", mode = "", padding = "";
	private IvParameterSpec iv = null;
	private Key key = null;

//	private static final List<String> algorithms =
//			Arrays.asList("AES", "Blowfish", "RC4", "DESede");
//	private static final List<String> modes =
//			Arrays.asList("None", "CBC", "CFB", "ECB", "OFB", "PCBC");
//	private static final List<String> paddings =
//			Arrays.asList("NoPadding", "PKCS5Padding", "SSL3Padding");


	/**
	 * This constructor set up a symcypher object which
	 * be used to encode and decode messages. It is only
	 * used by algorithm in ECB mode 
	 * 
	 * @param algo The symmetric algorithm.
	 * @param mode The mode of the algorithm.
	 * @param padding The padding option of the algorithm.
	 * @param randomKey The key for the algorithm.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public SymCypher(String algo, String mode, String padding, byte[] randomKey) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.algorithm = algo;
		this.mode = mode;
		this.padding = padding;
		checkAlgorithm(algo, mode, padding);
		generateKey(randomKey);
	}
	
	/**
	 * This constructor set up a symcypher object which
	 * be used to encode and decode messages.
	 * 
	 * @param algo The symmetric algorithm.
	 * @param mode The mode of the algorithm.
	 * @param padding The padding option of the algorithm.
	 * @param randomKey The key for the algorithm.
	 * @param randomIV The initialization vector.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public SymCypher(String algo, String mode, String padding, byte[] randomKey, byte[] randomIV) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.algorithm = algo;
		this.mode = mode;
		this.padding = padding;
		checkAlgorithm(algo, mode, padding);
		generateIV(randomIV);
		generateKey(randomKey);
	}
	
	/**
	 * 
	 */
	public byte[] encrypt(byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		if (plaintext == null || plaintext.length == 0) {
			throw new IllegalArgumentException();
		}
		Cipher c = Cipher.getInstance(this.getAlgorithm());
	    if (this.iv != null) {
			c.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
	    } else {
	    	c.init(Cipher.ENCRYPT_MODE, this.key);
	    }
		byte[] encVal = c.doFinal(plaintext);
		return encVal;
	}

	public byte[] decrypt(byte[] cyphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		if (cyphertext == null || cyphertext.length == 0) {
			throw new IllegalArgumentException();
		}
	    Cipher c = Cipher.getInstance(this.getAlgorithm());
	    if (this.iv != null) {
			c.init(Cipher.DECRYPT_MODE, this.key, this.iv);
	    } else {
	    	c.init(Cipher.DECRYPT_MODE, this.key);
	    }
        
        byte[] decValue = c.doFinal(cyphertext);
        return decValue;
	}

	// Getters
	
	public String getAlgorithm() {
		return "" + this.algorithm + "/" + this.mode + "/" + this.padding;
	}
	
	public byte[] getSecretKey() {
		return this.key.getEncoded();
	}
	
	
	// Private method
	
	private void generateKey(byte[] randomKey) {
        this.key = new SecretKeySpec(randomKey, this.algorithm);
	}
	
	private void generateIV(byte[] randomIV) {
		this.iv = new IvParameterSpec(randomIV);		
	}

	private boolean checkAlgorithm(String algo, String mode, String padding) throws NoSuchAlgorithmException, NoSuchPaddingException {
		String mix = this.getAlgorithm();
		Cipher.getInstance(mix);
		return true;
	}

	
}