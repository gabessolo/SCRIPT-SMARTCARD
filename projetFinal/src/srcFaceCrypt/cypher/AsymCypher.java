package cypher;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

/**
 * This class will present each asymmetric algorithm
 * available in our project
 * @author Florian Guilbert
 * @version 1.0
 */
public class AsymCypher implements IAsymCypher {
	
	public static List<Integer> LENGTHS = Arrays.asList(512, 1024, 2048, 4096);

	private int length;
	private String algorithm = "", mode = "", padding = "";
	private SecureRandom random = null;
	private PublicKey pubKey = null;
	private PrivateKey privKey = null;

	// Constructeur
	public AsymCypher(String algo, String mode, String padding, byte[] pubKey, byte[] random) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
		this.algorithm = algo;
		this.mode = mode;
		this.padding = padding;
		checkAlgorithm();
		this.random = new SecureRandom(random);
		setPubKey(pubKey);
	}
	
	public AsymCypher(String algo, String mode, String padding, int length, byte[] random) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
		this.algorithm = algo;
		this.mode = mode;
		this.padding = padding;
		checkAlgorithm();
		setLength(length);
		this.random = new SecureRandom(random);
		generateKeys();
	}

	// Commands
	public byte[] encrypt(byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher c = Cipher.getInstance(this.getAlgorithm());
		c.init(Cipher.ENCRYPT_MODE, this.pubKey, this.random);
		byte[] encVal = c.doFinal(plaintext);
		return encVal;
	}

	public byte[] decrypt(byte[] cyphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
	    Cipher c = Cipher.getInstance(this.getAlgorithm());
	    c.init(Cipher.DECRYPT_MODE, this.privKey);
        byte[] decValue = c.doFinal(cyphertext);
        return decValue;
	}
	
	// TODO ToFinish!
	public boolean verify(String signedText, String plainText) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sign = Signature.getInstance("SHA1withRSA");
		sign.initVerify(this.pubKey);
		sign.update(plainText.getBytes());
		return sign.verify(Base64.decodeBase64(signedText));
	}

	// Getters
	public String getAlgorithm() {
		String res = this.algorithm;
		if (this.mode != null && this.mode != "") {
			res += "/" + this.mode;
			if (this.padding != null && this.padding != "") {
				res += "/" + this.padding;
			}
		}
		return res;
	}
	
	public byte[] getPublicKey() {
		return this.pubKey.getEncoded();
	}
	
	
	// Private method
	private void setLength(int length) {
		if (!LENGTHS.contains((Integer) length)) {
			throw new IllegalArgumentException();
		}
		this.length = length;
	}
	
	private void generateKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(this.algorithm);
		generator.initialize(this.length, this.random);
		KeyPair pair = generator.generateKeyPair();
		this.pubKey = pair.getPublic();
		this.privKey = pair.getPrivate();
	}
	
	private void setPubKey(byte[] pubKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.pubKey = KeyFactory.getInstance(this.algorithm).generatePublic(
        		new X509EncodedKeySpec(pubKey)
        );
	}

	private boolean checkAlgorithm() throws NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher.getInstance(this.getAlgorithm());
		return true;
	}

}