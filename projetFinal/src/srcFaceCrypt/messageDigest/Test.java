package messageDigest;

import java.security.NoSuchAlgorithmException;

public class Test {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException {
		IChopper chop = new Chopper("SHA-512");
		System.out.println(chop.hash("bazinga"));
		System.out.println(chop.getAlgorithm());
	}

}
