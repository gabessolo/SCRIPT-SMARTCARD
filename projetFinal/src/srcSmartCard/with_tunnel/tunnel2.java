/**
 * @author Romain Pignard
 */

package with_tunnel;

import javacard.framework.APDU;
import javacard.framework.APDUException;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.AESKey;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacardx.crypto.Cipher;



public class tunnel2 extends Applet {
		
	
	// shared key and crypto object	
	// shared key
	private static AESKey shared_key; 	
	// crypto object for the key establishment
	private static Cipher cipher_exchange; 
	
	// session parameters	
	// session key
	private static byte[] raw_session_key;
	private static  AESKey session_key;	
	// encryption and decryption objects
	private static Cipher cipher_crypt;
	private static Cipher cipher_decrypt;
	
	// MAC cipher object (improved CBC-MAC)
	private static Cipher MAC;
	
	
	// temp arrays for encryption/decryption
	private static byte[] padded;	
	private static byte[] padded2;
	
	
	// loop variables, length and indexes
	private static short[] tab;
	
	private static byte[] IV;
	public static final byte CLA_MONAPPLET = (byte) 0xB0;	
	public static final short AES_BLOCK_LENGTH = 16;
	public static final short LENGTH_BLOCK_SIZE = 16; 
	
	
	
	public static final byte INS_DECRYPT = 0x01;
	public static final byte INS_SET_TUNNEL = 0x02;
	public static final byte INS_ECHO_PLUS_ONE = 0x03;
	public static final byte INS_CHECK_TUNNEL = 0x04;
	public static final byte INS_GENERATE_IV = 0x05; 
	
	
	
	public static final byte IV_LENGTH = 16;
	public static final byte MAC_LENGTH = 16;
	private static final byte PUT_DATA = 0x10;
	private static final byte GET_DATA = 0x11;
	private static final byte EXECUTE = 0x12;
	private static final byte ERASE_DATA = 0x13;
	
	
	private tunnel2() {
		
		
		try{
			// shared key initialization
			shared_key = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
			shared_key.setKey(new byte[]{10,1,1,5,9,6,5,4,5,9,6,6,6,9,2,6},(short) 0);
			cipher_exchange = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
			
			// MAC initialization with the shared key and an all-zero IV
			MAC = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
			MAC.init(shared_key,Cipher.MODE_ENCRYPT, new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},(short) 0,IV_LENGTH);
			
			// session key memory allocation		
			session_key = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_RESET, KeyBuilder.LENGTH_AES_128, false);
			
			// session crypto objects creation
			cipher_crypt = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
			cipher_decrypt = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
			
		}
		catch(CryptoException ce)
		{
			if(ce.getReason() == CryptoException.ILLEGAL_USE)
			{ISOException.throwIt((short) 0x01);}
			else if (ce.getReason() == CryptoException.ILLEGAL_VALUE)
			{ISOException.throwIt((short) 0x02);}
			else if (ce.getReason() == CryptoException.INVALID_INIT)
			{ISOException.throwIt((short) 0x03);}
			else if (ce.getReason() == CryptoException.NO_SUCH_ALGORITHM)
			{ISOException.throwIt((short) 0x04);}
			else if (ce.getReason() == CryptoException.UNINITIALIZED_KEY)
			{ISOException.throwIt((short) 0x05);}
			
		}
		
		
		
		// Local variables in RAM		
		padded = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_RESET);
		padded2 = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_RESET);		
		tab = JCSystem.makeTransientShortArray((short) 8, JCSystem.CLEAR_ON_RESET);
		IV = JCSystem.makeTransientByteArray( (short) ((short) IV_LENGTH), JCSystem.CLEAR_ON_RESET);		
		raw_session_key = JCSystem.makeTransientByteArray((short) (KeyBuilder.LENGTH_AES_128/8), JCSystem.CLEAR_ON_RESET);
	}
	
	/**
	* Decrypts the 3rd part of the authentification process
	* Puts the decrypted data into buffer to check if 
	*/
	
	
	
	public static short pad(byte[] sortie, byte[] entree, short blockSize, short lg, byte bufOff)
	{
					
		
		// padding of the message according to pkcs7			
		
		if(lg % blockSize == 0)
		{	
			// if the last block is full, we create another full block 
			
			Util.arrayFillNonAtomic(entree, lg, (short) (  blockSize), (byte)  blockSize);
			
			
		}
		else
		{
			// we fill the last block with the required number of bytes
			
			Util.arrayFillNonAtomic(entree, lg, (short) (  blockSize  -  (lg % blockSize)), (byte) ((byte)  blockSize - lg  % blockSize));
			
			
		}
		tab[1] = (short) (lg + blockSize -  (lg % blockSize));
		//tab[1] = (short)( lg - entree[(short)(lg - 1 + bufOff)]);
		
		Util.arrayCopy(entree, (short)0,sortie ,(short) 0,tab[1] );		
		
		// length of the padded message
		return tab[1];
	}	
	
	
	
	private static void check_card_secret(byte[] buffer)
	{
				
		
		// crypto object initialization
		
		cipher_decrypt.init(session_key, Cipher.MODE_DECRYPT,buffer,ISO7816.OFFSET_CDATA, IV_LENGTH);							
			
		// we get the length
		tab[2] = buffer[ISO7816.OFFSET_P1];
		
		// decryption with the length
		cipher_decrypt.doFinal(buffer, (short)(ISO7816.OFFSET_CDATA+IV_LENGTH),tab[2] , padded,(short) 0);
		
		
		//brutal unpadding			
		Util.arrayCopy(padded, (short) 0,buffer ,(short)0,(short) (tab[2] - padded[(short)(tab[2] - 1)]));		
		tab[2] = (short)( tab[2] - padded[(short)(tab[2] - 1)]);
		
		for(tab[0] = 0 ; tab[0] < tab[2]; tab[0] ++ )
		{
			if (buffer[tab[0]] != padded2[tab[0]])
			{
				ISOException.throwIt((short) 0x50);
			}	
		}
		
		
	}
	
	/**
	* @input buffer data of the form [data|MAC(data.length.0..0|data)]
	* Computes a CBC-MAC "improved" with the size at the beginning to handle variable size packets		 * 
	*/
	private static void compute_MAC(byte[] buffer)
	{
		
		
		// copy of the data into padded2 for MAC computation with space left for the size
		Util.arrayCopy(buffer, (short) 0,padded2 ,(short)LENGTH_BLOCK_SIZE,(short) (tab[2] + IV_LENGTH));	
		
		// emptying the first block of the message for MAC computation
		Util.arrayFillNonAtomic(padded2, (short) 0,(short) LENGTH_BLOCK_SIZE,(byte) 0);
		
		// length insertion at the beginning
		padded2[0] =  (byte) (((short)(tab[2] + IV_LENGTH)) % 256);
		padded2[1] =  (byte) (((short)(tab[2] + IV_LENGTH)) / 256);
				
		
		
		
		// actual MAC computation
		MAC.doFinal(padded2,(short) 0 , (short)(tab[2]+ IV_LENGTH + LENGTH_BLOCK_SIZE), padded, (short) 0);	
		
		
		// MAC insertion
		Util.arrayCopy(padded, (short) (tab[2] - MAC_LENGTH  + IV_LENGTH + LENGTH_BLOCK_SIZE),buffer ,(short)(tab[2] + IV_LENGTH),(short) MAC_LENGTH);
			
		
	}
	
	/**
	* Checks the MAC in the supplied buffer		 
	* @input buffer data of the form [data|MAC(data.length.0..0|data)]
	* Computes a CBC-MAC "improved" with the size at the beginning to handle variable size packets
	* and checks it against the MAC at the enb of the buffer
	*/
	private static void check_MAC(byte[] buffer)	
	{
		
		
		// message length (w/o IV nor MAC)
		tab[2] = (short ) (buffer[ISO7816.OFFSET_LC] - IV_LENGTH - MAC_LENGTH);
		
		// whole message length
		tab[1] = buffer[ISO7816.OFFSET_LC];
				
		
		// copy of the original message into padded2 for MAC computation with space left for the size		
		Util.arrayCopy(buffer, (short) ISO7816.OFFSET_CDATA,padded2 ,(short)LENGTH_BLOCK_SIZE,(short) (tab[2] + IV_LENGTH));
	
		// emptying the first block of the message for MAC computation
		Util.arrayFillNonAtomic(padded2, (short)0, (short)LENGTH_BLOCK_SIZE,(byte) 0);
	
				
		// length insertion at the beginning (in the first block) 		
		padded2[0] =  (byte) ((short)(tab[2] + IV_LENGTH) % 256);
		padded2[1] =  (byte) ((short)(tab[2] + IV_LENGTH) / 256);				
		
		
		
		// actual MAC computation
		MAC.doFinal(padded2,(short) 0 , (short)(tab[2]+ IV_LENGTH + LENGTH_BLOCK_SIZE), padded, (short) 0);
		
		
		
		// MAC comparison
		for(tab[0] = 0;tab[0] < MAC_LENGTH;tab[0]++)
		{
			if(buffer[(short)(ISO7816.OFFSET_CDATA + tab[1] - MAC_LENGTH + tab[0])] != padded[(short)(tab[0] + tab[2]- MAC_LENGTH + IV_LENGTH + LENGTH_BLOCK_SIZE )])
			{
				ISOException.throwIt((short) 0x66);
			}	
		}	
		
	}
	
	/**
	* Performs the decryption of the data in the buffer using the tunnel parameters
	* Stores the length of the decrypted data in tab[2]
	* @param buffer the buffer to be decrypted
	* Puts the result back in buffer
	*/
	private static void decrypt_tunnel(byte[] buffer)
	{
		
		
		// decryption object initialization
		cipher_decrypt.init(session_key, Cipher.MODE_DECRYPT, buffer, (short) ISO7816.OFFSET_CDATA, IV_LENGTH);	
		
		
		tab[2] = (short) (buffer[ISO7816.OFFSET_LC] - MAC_LENGTH - IV_LENGTH);
		
		// decryption of the data
		cipher_decrypt.doFinal(buffer, (short) ((short)(ISO7816.OFFSET_CDATA) + IV_LENGTH),tab[2] , padded,(short) 0);
		
		// copy of the decrypted data into the output buffer
		Util.arrayCopy(padded, (short) 0,buffer ,(short)0,(short) tab[2]);
		
		
		
		
		tab[3] = tab[2];
		tab[2] = (short)( tab[2] - padded[(short)(tab[2] - 1)]);	
	}
	/**
	* @input buffer the data to be encrypted
	* @input length the length of the buffer
	* Encrypts the data of buffer of size length with the tunnel parameters
	* Puts it  back in the buffer with the IV at the beginning
	* Updates tab[2] with the total size (IV + encrypted data)
	*/
	private static void encrypt_tunnel(byte[] buffer, short length)
	{
		
		// IV generation
		gen_random.genRandom(IV, IV_LENGTH);
		
		// padding 
		tab[2] = pad(padded, buffer, AES_BLOCK_LENGTH, length, (byte) 0);
		
		
		// copy of the into the buffer 
		Util.arrayCopy(IV, (short) 0,buffer ,(short)0,(short) IV_LENGTH);
		
		
		
		
		// setting of the IV		
		cipher_crypt.init(session_key, Cipher.MODE_ENCRYPT, IV, (short) 0, IV_LENGTH);
		
		
		// encryption with the IV
		cipher_crypt.doFinal(padded, (short) 0, tab[2], padded2, (short) 0 );
		
		
		Util.arrayCopy(padded2, (short) 0,buffer ,(short)IV_LENGTH,(short) tab[2]);	
		
	}
	
	/**	
	* Second part of the mutual authentication exchange
	* Generates a session key and a card nonce 
	* Extract the received client nonce 
	* Builds this packet :			 	 
	* padded = [session_key|nonce_card|nonce_client]
	* Sends back this encrypted packet with the shared key
	* If the client finds its nonce, it proves the card knows the shared key
	*/
	public static void set_tunnel(byte[] buffer)
	{	
		
				
		
		// raw session key (byte[]) random generation 
		gen_random.genRandom(padded,  (short) (KeyBuilder.LENGTH_AES_128/8));	
		// padded = [session_key|.....]
		Util.arrayCopy(padded, (short) 0,raw_session_key ,(short)0,(short) (KeyBuilder.LENGTH_AES_128/8));
		
		
		// IV generation 
		gen_random.genRandom(IV, IV_LENGTH);
		// crypto object intialization with the shared key and the previously generated IV
		cipher_exchange.init(shared_key, Cipher.MODE_ENCRYPT,IV,(short) 0, IV_LENGTH);	
		
		
		
		
		// nonce_client extraction
		// padded = [session_key|nonce_client...]
		Util.arrayCopy(buffer, (short) ISO7816.OFFSET_CDATA,padded ,(short) (KeyBuilder.LENGTH_AES_128/8),AES_BLOCK_LENGTH);
		
		
		
		// nonce_card generation
		gen_random.genRandom(padded2, AES_BLOCK_LENGTH);
		
		// padded = [session_key|nonce_client|nonce_card]
		Util.arrayCopy(padded2, (short)0,padded ,(short) (KeyBuilder.LENGTH_AES_128/8 + AES_BLOCK_LENGTH),AES_BLOCK_LENGTH);
		
		
		
		tab[3] = (short) pad(padded2,padded , AES_BLOCK_LENGTH, (short) (( KeyBuilder.LENGTH_AES_128/8) + AES_BLOCK_LENGTH + AES_BLOCK_LENGTH), (byte) 0);
		
		// copy of the generated IV at the beginning of buffer
		Util.arrayCopy(IV, (short) 0,buffer ,(short) 0,IV_LENGTH);
		
		// encryption of the whole packet with the 
		cipher_exchange.doFinal(padded2, (short)0,
				(short) tab[3],buffer,(short) IV_LENGTH);
		
		
		
		
		
		// session key affectation						
		session_key.setKey(raw_session_key,(short) 0);	
		
	}
		
		
	public static void install(byte bArray[], short bOffset, byte bLength)
			throws ISOException {
		new tunnel2().register();
	}

	
	public void process(APDU apdu) throws ISOException {

		
		
		byte[] buffer = apdu.getBuffer();
		
		
		
		if (this.selectingApplet()) return;
		
		if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}
		
		switch (buffer[ISO7816.OFFSET_INS]) {				
		case INS_SET_TUNNEL: 
			try{
			// Second part of the mutual authentication exchange
				
			set_tunnel(buffer);				
			apdu.setOutgoingAndSend((short)0,(short) (tab[3]+ IV_LENGTH ));	
			}
			catch(CryptoException ce)
			{
				if(ce.getReason() == CryptoException.ILLEGAL_USE)
				{ISOException.throwIt((short) 0x01);}
				else if (ce.getReason() == CryptoException.ILLEGAL_VALUE)
				{ISOException.throwIt((short) 0x02);}
				else if (ce.getReason() == CryptoException.INVALID_INIT)
				{ISOException.throwIt((short) 0x19);}
				else if (ce.getReason() == CryptoException.NO_SUCH_ALGORITHM)
				{ISOException.throwIt((short) 0x04);}
				else if (ce.getReason() == CryptoException.UNINITIALIZED_KEY)
				{ISOException.throwIt((short) 0x05);}
				
			}
			catch(APDUException ae)
			{ISOException.throwIt((short) 0x11);}
							
			break;
			
		case INS_CHECK_TUNNEL:
			try{			
				check_card_secret(buffer);			
			}			
			catch(CryptoException ce)
			{
				if(ce.getReason() == CryptoException.ILLEGAL_USE)
				{ISOException.throwIt((short) 0x01);}
				else if (ce.getReason() == CryptoException.ILLEGAL_VALUE)
				{ISOException.throwIt((short) 0x02);}
				else if (ce.getReason() == CryptoException.INVALID_INIT)
				{ISOException.throwIt((short) 0x19);}
				else if (ce.getReason() == CryptoException.NO_SUCH_ALGORITHM)
				{ISOException.throwIt((short) 0x04);}
				else if (ce.getReason() == CryptoException.UNINITIALIZED_KEY)
				{ISOException.throwIt((short) 0x05);}
				
			}
			catch(APDUException ae)
			{ISOException.throwIt((short) 0x12);}
			
			break;				
				
		case PUT_DATA:
			tab[4] = buffer[ISO7816.OFFSET_LC];				
			check_MAC(buffer);	
			decrypt_tunnel(buffer);	
			datastore.putData(buffer, (short) tab[2]);
			
			break;
			
		case EXECUTE:	
			datastore.execute();
			break;
			
		case GET_DATA:			
			tab[2] = (short) (datastore.getRemainingData((short)208));
			if(tab[2] == 0)
			{
				ISOException.throwIt((short) 0x6666);
			}	
			datastore.getData(buffer, tab[2]);				
			encrypt_tunnel(buffer,(short)(tab[2]));
			compute_MAC(buffer);			
			apdu.setOutgoingAndSend((short)0,(short) ((short)tab[2]+ IV_LENGTH + MAC_LENGTH));
			break;	
		case ERASE_DATA:
			datastore.eraseData();
			break;
		
			
		
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	}

}
