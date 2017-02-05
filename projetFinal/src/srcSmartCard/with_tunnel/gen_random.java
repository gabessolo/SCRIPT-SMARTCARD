/* Author : Romain Pignard */

/* 
 * Applet generating random numbers 
 * 
 * 
 * 
 */



package with_tunnel;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.security.CryptoException;
import javacard.security.RandomData;


public class gen_random extends Applet {

	public static final byte CLA_MONAPPLET = (byte) 0xB0;

	public static final byte INS_NOUVEL_ALEA = 0x00;
	private static short[] tab; 


	/* Attributs */

	private static RandomData rng;
	
	/**
	 * Execute the action requested by the user
 	 */
	public static void execute(byte[] data)
	{
		switch (data[ISO7816.OFFSET_INS]) {
		/*   */	
		case INS_NOUVEL_ALEA:
			// the required number is in big endian
			datastore.eraseData();
			tab[0] = (short) (data[ISO7816.OFFSET_P1] + data[ISO7816.OFFSET_P2]*256);
			rng.generateData(data, (short) 0,tab[0]);			
			datastore.putData(data, (short) tab[0]);

			break;
		}

	}

	private gen_random() {
		// Secure random number generator
		rng =  RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
		tab =  JCSystem.makeTransientShortArray((short) 1, JCSystem.CLEAR_ON_RESET);


	}


	public static void install(byte bArray[], short bOffset, byte bLength)
			throws ISOException {
		new gen_random().register();
	}


	public static  void genRandom(byte[] buff , short nb)
	{
		// buff : output buffer
		// nb : required number of bytes


		try{
			rng =  RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);

			// we put the required number of random bytes into buff
			rng.generateData(buff,(short) 0,(short) nb);
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
	}

	public void process(APDU apdu) throws ISOException {
	}

}
