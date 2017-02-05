package main;

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
	 * Identifiant de la classe
	 */
	public static final byte CLA_MONAPPLET = (byte) 0xB0;

	/*
	 * Identifiants pour un chiffrement, un déchiffrement, une récupération
	 * d'exposant public et une récupération du modulus
	 */
	public static final byte INS_CIPHER = 0x00;
	public static final byte INS_UNCIPHER = 0x01;
	private static final byte INS_GET_EXPONENT = 0x02;
	private static final byte INS_GET_MODULUS = 0x03;

	private PrivateKey privKey;
	private PublicKey pubKey;
	private KeyPair kp;

	/* 
	 * Génération de la paire de la clef 
	 */
	private Cypher(){
		try{
			kp = new KeyPair(KeyPair.ALG_RSA_CRT, (short) KeyBuilder.LENGTH_RSA_1024);
			kp.genKeyPair();
			privKey = (PrivateKey) kp.getPrivate();
			pubKey = (PublicKey) kp.getPublic();
		}
		catch(CryptoException e){
			ISOException.throwIt((short) 0x4242);
		}
	}

	/*
	 * Méthode appelée lors de l'installation de l'applet sur la carte
	 * 
	 */
	public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
		new Cypher().register();
	}

	/*
	 * Méthode appelée lors de l'appel de l'applet
	 */
	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();
		short dataLen;
		Cipher cipher;
		short cipherLen = 0;

		if (this.selectingApplet()){
			return;
		}

		if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {

		// Requête de chiffrement
		case INS_CIPHER:
			try {
				dataLen = apdu.setIncomingAndReceive();
				cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);

				cipher.init(pubKey, Cipher.MODE_ENCRYPT);
				cipherLen = cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, dataLen, buffer, (short) 0);

				// Besoin d'utiliser ces fonctions pour des réponses "longues"
				apdu.setOutgoing();
				apdu.setOutgoingLength(cipherLen);
				apdu.sendBytesLong(buffer, (short) 0, cipherLen);

				//apdu.setOutgoingAndSend((short) 0, (short) cipherLen);
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

			// Requête de déchiffrement
		case INS_UNCIPHER:
			try {
				if (PIN.getState() == (short)0x9000) {
					dataLen = apdu.setIncomingAndReceive();
					cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);

					cipher.init(privKey, Cipher.MODE_DECRYPT);
					cipherLen = cipher.doFinal(buffer, (short) ISO7816.OFFSET_CDATA, dataLen, buffer, (short) 0);

					// Besoin d'utiliser ces fonctions pour des réponses "longues"
					apdu.setOutgoing();
					apdu.setOutgoingLength(cipherLen);
					apdu.sendBytesLong(buffer, (short) 0, cipherLen);

				}
				else {
					buffer[0] = -1;
					apdu.setOutgoingAndSend((short) 0, (short) 1);
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

			// Requête d'obtention de l'exposant public
		case INS_GET_EXPONENT:
			try {
				RSAPublicKey rsaPubKey= (RSAPublicKey) pubKey;

				dataLen = rsaPubKey.getExponent(buffer, (short) 0);
				apdu.setOutgoingAndSend((short) 0, (short) dataLen);
			}
			catch(APDUException e){
				ISOException.throwIt((short) 0x4242);
			}
			catch(SecurityException e){
				ISOException.throwIt((short) 0x4243);
			}
			break;

			// Requête d'obtention du modulus
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
