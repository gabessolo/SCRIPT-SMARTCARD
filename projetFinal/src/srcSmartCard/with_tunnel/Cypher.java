package with_tunnel;

/**
 * @author Emmanuel Mocquet
 */

import javacard.framework.APDU;
import javacard.framework.APDUException;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.PrivateKey;
import javacard.security.PublicKey;
import javacard.security.RSAPublicKey;
import javacardx.crypto.Cipher;


public class Cypher extends Applet {

	/* 
	 * Class ID
	 */
	public static final byte CLA_MONAPPLET = (byte) 0xB0;
	/*
	 * ID for the encryption, decryption, retrieving of the exponent
	 * and the modulus.
	 */
	public static final byte INS_CIPHER = 0x00;
	public static final byte INS_DECRYPT = 0x01;
	private static final byte INS_GET_EXPONENT = 0x02;
	private static final byte INS_GET_MODULUS = 0x03;


	private static PrivateKey privKey;
	private static PublicKey pubKey;
	private static KeyPair kp;
	private static short dataLen;
	private static Cipher cipher;
	private static short cipherLen = 0;

	/* 
	 * Generating the key  
	 */
	private Cypher(){
		try{
			/* We use a RSA algorithm with a key of 1024 bits  */
			kp = new KeyPair(KeyPair.ALG_RSA_CRT, (short) KeyBuilder.LENGTH_RSA_1024);
			kp.genKeyPair();
			privKey = (PrivateKey) kp.getPrivate();
			pubKey = (PublicKey) kp.getPublic();
			cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);

		}
		catch(CryptoException e){
			ISOException.throwIt((short) 0x4242);
		}
	}

	/* Execute the action requested by the user  */
	public static void execute(byte[] data)
	{

		switch (data[ISO7816.OFFSET_INS]) {
		
		/* Returns the private exponent value of the key */
		case INS_GET_EXPONENT:
			try {
				dataLen = ((RSAPublicKey) pubKey).getExponent(data, (short) 0);
				datastore.eraseData();
				datastore.putData(data, dataLen);

			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4242);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4243);
			}
			break;			

		/* Returns the modulus value of the key */
		case INS_GET_MODULUS:
			try {				
				dataLen = ((RSAPublicKey) pubKey).getModulus(data, (short) 0);
				datastore.eraseData();
				datastore.putData(data, dataLen);
			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4242);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4243);
			}
			break;
			
		/* Generates encrypted output from input data */
		case INS_CIPHER:
			try {
				dataLen = data[ISO7816.OFFSET_LC];
				cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);

				cipher.init(pubKey, Cipher.MODE_ENCRYPT);
				cipherLen = cipher.doFinal(data, ISO7816.OFFSET_CDATA, dataLen, data, (short) 0);

				datastore.eraseData();
				datastore.putData(data, cipherLen);
			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4244);
			}
			catch(CryptoException ce){
				if (ce.getReason() == CryptoException.UNINITIALIZED_KEY)
					ISOException.throwIt((short) 0x0001);
				else if (ce.getReason() == CryptoException.INVALID_INIT)
					ISOException.throwIt((short) 0x0002);
				else if (ce.getReason() == CryptoException.ILLEGAL_USE)
					ISOException.throwIt((short) 0x0003);
				else if (ce.getReason() == CryptoException.ILLEGAL_VALUE)
					ISOException.throwIt((short) 0x0004);
				else if (ce.getReason() == CryptoException.NO_SUCH_ALGORITHM)
					ISOException.throwIt((short) 0x0005);
				else
					ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4246);
			}

			break;

		/* Generates decrypted output from input data  */	
		case INS_DECRYPT:
			try {
				if(PIN.getState() == (short) 0x9000)
				{				
					dataLen = (short) (data[ISO7816.OFFSET_LC] & 0xFF);

					cipher.init(privKey, Cipher.MODE_DECRYPT);
					cipherLen = cipher.doFinal(data, (short) ISO7816.OFFSET_CDATA, dataLen, data, (short) 0);
	
					datastore.eraseData();
					datastore.putData(data, cipherLen);
				}
				else
				{
					datastore.eraseData();
					datastore.putData(new byte[]{-1}, (short) 1);
				}	

			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4247);
			}

			catch(CryptoException ce){
				if (ce.getReason() == CryptoException.UNINITIALIZED_KEY)
					ISOException.throwIt((short) 0x0001);
				else if (ce.getReason() == CryptoException.INVALID_INIT)
					ISOException.throwIt((short) 0x0002);
				else if (ce.getReason() == CryptoException.ILLEGAL_USE)
					ISOException.throwIt((short) 0x0003);
				else if (ce.getReason() == CryptoException.ILLEGAL_VALUE)
					ISOException.throwIt((short) 0x0004);
				else if (ce.getReason() == CryptoException.NO_SUCH_ALGORITHM)
					ISOException.throwIt((short) 0x0005);
				else
					ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4249);
			}

			break;
		}
	}
	
	/**
	 * Method called when the applet is being installed on the card.
	 * @return void
	 * @throws ISOException if an error occured while installing.
	 */
	public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
		new Cypher().register();
	}

	/**	
	 * This method is called when the applet is being called from outside the tunnel.
	 * @return void
	 * @throws ISOException if an error occured while processing the request.
	 */
	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();
		short dataLen;
		short cipherLen = 0;

		if (this.selectingApplet()){
			return;
		}

		if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {

		// Ciphering request 
		case INS_CIPHER:
			try {
				dataLen = apdu.setIncomingAndReceive();

				cipher.init(pubKey, Cipher.MODE_ENCRYPT);
				cipherLen = cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, dataLen, buffer, (short) 0);

				apdu.setOutgoing();
				apdu.setOutgoingLength(cipherLen);
				apdu.sendBytesLong(buffer, (short) 0, cipherLen);
			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4244);
			}
			catch(CryptoException ce){
				if (ce.getReason() == CryptoException.UNINITIALIZED_KEY)
					ISOException.throwIt((short) 0x0001);
				else if (ce.getReason() == CryptoException.INVALID_INIT)
					ISOException.throwIt((short) 0x0002);
				else if (ce.getReason() == CryptoException.ILLEGAL_USE)
					ISOException.throwIt((short) 0x0003);
				else if (ce.getReason() == CryptoException.ILLEGAL_VALUE)
					ISOException.throwIt((short) 0x0004);
				else if (ce.getReason() == CryptoException.NO_SUCH_ALGORITHM)
					ISOException.throwIt((short) 0x0005);
				else
					ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4246);
			}

			break;

		// Decrypting request
		case INS_DECRYPT:
			try {
				dataLen = apdu.setIncomingAndReceive();
				
				cipher.init(privKey, Cipher.MODE_DECRYPT);
				cipherLen = cipher.doFinal(buffer, (short) ISO7816.OFFSET_CDATA, dataLen, buffer, (short) 0);

				apdu.setOutgoing();
				apdu.setOutgoingLength(cipherLen);
				apdu.sendBytesLong(buffer, (short) 0, cipherLen);

			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4247);
			}

			catch(CryptoException ce){
				if (ce.getReason() == CryptoException.UNINITIALIZED_KEY)
					ISOException.throwIt((short) 0x0001);
				else if (ce.getReason() == CryptoException.INVALID_INIT)
					ISOException.throwIt((short) 0x0002);
				else if (ce.getReason() == CryptoException.ILLEGAL_USE)
					ISOException.throwIt((short) 0x0003);
				else if (ce.getReason() == CryptoException.ILLEGAL_VALUE)
					ISOException.throwIt((short) 0x0004);
				else if (ce.getReason() == CryptoException.NO_SUCH_ALGORITHM)
					ISOException.throwIt((short) 0x0005);
				else
					ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4249);
			}

			break;

		// Request to obtain the exponent
		case INS_GET_EXPONENT:
			try {
				dataLen = ((RSAPublicKey) pubKey).getExponent(buffer, (short) 0);
				apdu.setOutgoingAndSend((short) 0, (short) dataLen);
			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4242);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4243);
			}
			break;

		// Request to obtain the modulus
		case INS_GET_MODULUS:
			try {
				RSAPublicKey rsaPubKey= (RSAPublicKey) pubKey;
				dataLen = rsaPubKey.getModulus(buffer, (short) 0);
				apdu.setOutgoing();
				apdu.setOutgoingLength((short) dataLen);
				apdu.sendBytesLong(buffer, (short) 0, (short) dataLen);
			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4242);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4243);
			}
			break;


		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}
