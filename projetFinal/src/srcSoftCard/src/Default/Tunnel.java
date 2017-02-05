package Default;

/* Author : Romain Pignard */

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;






public class Tunnel {	
	/**
	 * This class represents the object by which all the communications with the card will be made	  
	 */

	/** tunnel AID on the card */
	public static byte[] APPLET_AID= {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x00, (byte)0x06};


	/** encryption object */
	private static Cipher encrypt;
	/** decryption object */
	private static Cipher decrypt;
	/** CBC-MAC object */
	private static Cipher CBC_MAC;

	/** Channel used for communication */
	private static CardChannel c;

	/** Session key used by the tunnel */
	private static SecretKey session_key;

	/**
	* The constructor builds a secure tunnel with the card
	* @param shared_key the secret key shared with the smartcard		  
	* 
	*/
	public Tunnel(SecretKey shared_key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CardException, IOException, InterruptedException
	{	
		
		
		// environment building
		TerminalFactory factory = TerminalFactory.getDefault();
		List<CardTerminal> terminals;
		
		terminals = factory.terminals().list();
		
		// selection of the first terminal
		CardTerminal terminal = terminals.get(0);

		// connection to the card
		Card card = terminal.connect("T=1");	
		
		// channel to the card		
		c =  card.getBasicChannel();		
		

		// APDU used to select the applet
		ResponseAPDU r = c.transmit(new CommandAPDU((byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, APPLET_AID));
		if (r.getSW() != 0x9000) {
			System.out.println("Error selecting the applet");
			System.out.println(r.getSW());
			System.exit(1);

		}

		/**
		 *  At this point, the system is connected to the tunnel applet on the card 
		 *  and ready to establish a secure connection
		 */
		

		// Initialization of the crypto objects
		
		encrypt = Cipher.getInstance("AES/CBC/NoPadding");		

		decrypt = Cipher.getInstance("AES/CBC/NoPadding");

		CBC_MAC = Cipher.getInstance("AES/CBC/NoPadding");

		// forcing of the CBC-MAC IV to 0,0,0,0,....0 with the shared_key
		CBC_MAC.init(Cipher.ENCRYPT_MODE, shared_key, new IvParameterSpec(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}));

		// Establisment of the session key		
		session_key = CryptoTools.EstablishSessionKey(c,encrypt,shared_key);

	}

	/**
	* 
	* Constructor with the not-so-secret key
	* 
	*/
	public Tunnel() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CardException, IOException, InterruptedException
	{	
		
		this(new SecretKeySpec(new byte[]{10,1,1,5,9,6,5,4,5,9,6,6,6,9,2,6}, "AES"));

	}

	/**
	* Sends a (possibly fragmented) packet inside the tunnel by
	* doing the encryption and the MAC
	*/
	public void sendRaw(byte[] input) throws Exception
	{

		
		
		// IV generation
		byte[] iv = ArrayTools.RandomArray((short) CryptoTools.IV_LENGTH);	

		// crypto object initialization
		encrypt.init(Cipher.ENCRYPT_MODE, session_key, new IvParameterSpec(iv));

	
		// padding of the data
		byte[] padded = ArrayTools.pad(input, CryptoTools.AES_BLOCK_LENGTH);			
		byte[] to_be_sent = encrypt.doFinal(padded);		

		

		// MAC computation with size appending at the beginning then CBC-MAC computation
		byte[] MAC = ArrayTools.ExtractLastBytes(CBC_MAC.doFinal(ArrayTools.add_size(ArrayTools.concat(iv,to_be_sent),(short) 16)), CryptoTools.MAC_LENGTH);
		// final message assembly : [IV|padded_message|MAC(size|IV|padded_message)]
		byte[] payload = ArrayTools.concat(ArrayTools.concat(iv,to_be_sent),MAC);		

		// transmission of the whole packet
		ResponseAPDU r = c.transmit(new CommandAPDU((byte)0xB0, 0x10, (byte) 0x00, (byte)0x00, payload));

		if (r.getSW() != 0x9000) {
			System.out.println(" Erreur d'envoi: Status word different from 0x9000 : "+r.getSW());
		}		

	}


	/**
	* Sends the execution signal to the card
	 * @return 
	*/
	public int execute() throws CardException
	{
		ResponseAPDU r = c.transmit(new CommandAPDU((byte)0xB0, 0x12, (byte) 0x00, (byte)0x00));
		return r.getSW();
	}



	/**
	* Gets the response from the card
	* @return returns the byte[] array containing the full response from the card after execution and fragment reassembly
	*/
	public byte[] getResponse() throws IllegalBlockSizeException, BadPaddingException, Exception
	{

		// array containing the full response
		byte[] response = new byte[]{};
		
		// temp array containing the received data
		byte[] received;
		// reception of the data 
		received = this.getData();	
		
		// we build the full response by concatening the received data
		
		while(received.length != 0)
		{    		
			
			response = ArrayTools.concat(response,received);			
			received = this.getData();  
			
			
		}		
		return response;

	}




	/**
	* Reception of a single packet
	*/
	public byte[] getData() throws IllegalBlockSizeException, BadPaddingException, Exception
	{
		
		
		// reception command
		ResponseAPDU r = c.transmit(new CommandAPDU((byte)0xB0, 0x11, (byte) 0x00, (byte)0x00));
		if ((r.getSW() != 0x9000) && (r.getSW() != 0x6666)) {
			System.out.println("Erreur de reception : status word different from 0x9000 : "+r.getSW());
		}
		
		
		// Specific SW to know if the packet is empty or if it the last packet 
		if (r.getSW() == 0x6666) {			
			return new byte[]{};
		}

		// MAC check
		if(!check_MAC(r.getData()))		
		{
			throw new Exception("CBC-MAC check failed ");
		}	

		// IV extraction
		byte[]  IV = ArrayTools.ExtractFirstBytes(r.getData(), CryptoTools.IV_LENGTH);	

		// message extraction		
		// "deletion" of the IV 
		byte[] msg = ArrayTools.ExtractLastBytes(r.getData(), (short) (r.getData().length - CryptoTools.IV_LENGTH));
		// "deletion" of the MAC 
		msg = ArrayTools.ExtractFirstBytes(msg, (short) (msg.length - CryptoTools.MAC_LENGTH));


		// crypto object initialization for decryption
		decrypt.init(Cipher.DECRYPT_MODE, session_key, new IvParameterSpec(IV));



		// message decryption	
		byte[] padded = decrypt.doFinal(msg);				
		//ArrayTools.printByteArray(padded);
		// unpadding
		byte[] unpadded = ArrayTools.unpad(padded, CryptoTools.AES_BLOCK_LENGTH);		
		return unpadded;	

	}


	


	/**
	* Sends the erase command to the card
	*/
	public void erase() throws CardException
	{
		
		ResponseAPDU r = c.transmit(new CommandAPDU((byte)0xB0, 0x13, (byte) 0x00, (byte)0x00));
		if (r.getSW() != 0x9000) {
			System.out.println("Status word different from 0x9000 : "+r.getSW());
		}
	}

	/**
	*  MAC checking with the built-in CBC-MAC object. 		
	*/
	public static boolean check_MAC(byte[] raw_message) throws IllegalBlockSizeException, BadPaddingException
	{
		

		// extraction of the MAC for comparison
		byte[] received_MAC = ArrayTools.ExtractLastBytes(raw_message, (short) CryptoTools.MAC_LENGTH);	

		// extraction of the IV + message for MAC computation
		// the add_size function adds a block with the payload size at the beginning to obtain a secure CBC-MAC.
		byte[] size_n_msg = ArrayTools.add_size(ArrayTools.ExtractFirstBytes(raw_message, (short) (raw_message.length - CryptoTools.MAC_LENGTH)),(short) 16);



		// MAC computation
		byte[] computed_MAC =  ArrayTools.ExtractLastBytes(CBC_MAC.doFinal(size_n_msg), (short) CryptoTools.MAC_LENGTH);

		// comparison
		return Arrays.equals(received_MAC, computed_MAC);		
	}

	/**
	* Sends a full APDU to the card through the tunnel
	* Emulates the transmit(new commandAPDU()) function that doesn't use the tunnel
	* Builds the whole packet by inserting the provided parameters
	* Fragments the resulting packet before using sendRaw to actually send the fragments through the wire 
	* 
	*/
	public void request(short applet_ID,short INS_ID, byte P1, byte P2, byte[] data) throws Exception
	{
		
		
		// full request 
		byte[] rq = new byte[data.length + 8];	

		// copy of the parameters inside the request
		rq[0] = (byte) applet_ID;
		rq[1] = (byte) INS_ID;
		rq[2] = (byte) P1;
		rq[3] = (byte) P2;
		rq[4] = (byte) data.length;

		// copy of the payload
		System.arraycopy(data, 0, rq, 5, data.length);

		// segmentation of the request	
		
		
		byte[][] segmented_rq = ArrayTools.split(rq,(short) ((short) 64));
		//ArrayTools.printByteArray(segmented_rq[0]);	
		for (int i = 0; i < segmented_rq.length; i++) 
		{
			// sending of the fragmented request
			// it will erassembled on the other side
			//System.out.println("iteration : "+ i);
			sendRaw(segmented_rq[i]);			
		}		
	}
	/**
	* Request without data
	*/
	public void request(short applet_ID,short INS_ID, byte P1, byte P2) throws Exception
	{
		
		request(applet_ID,INS_ID,P1,P2, new byte[]{} );
	}
}
