package messageDigest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Florian Guilbert
 *
 */
public class Chopper implements IChopper {
	
	private String algorithm = "";
	
	public Chopper(String algorithm) throws NoSuchAlgorithmException {
		if (algorithm == null || algorithm == "") {
			throw new IllegalArgumentException();
		}
		setAlgorithm(algorithm);
	}

	@Override
	public String hash(String plaintext) throws NoSuchAlgorithmException {
		MessageDigest axe = MessageDigest.getInstance(this.algorithm);
		byte[] digest = axe.digest((plaintext).getBytes());
		return byteToHexString(digest);
	}

	@Override
	public String getAlgorithm() {
		return this.algorithm;
	}
	
	// Private method
	private String byteToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return new String(sb);
	}
	
	private boolean setAlgorithm(String algorithm) throws NoSuchAlgorithmException {
		MessageDigest.getInstance(algorithm);
		this.algorithm = algorithm;
		return true;
	}

}
