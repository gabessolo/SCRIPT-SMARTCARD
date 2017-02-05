package main;

import javacard.framework.APDU;
import javacard.framework.APDUException;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.SystemException;
import javacard.security.CryptoException;
import javacard.security.RandomData;


public class gen_random extends Applet {

	public static final byte CLA_MONAPPLET = (byte) 0xB0;

	public static final byte INS_NOUVEL_ALEA = 0x00;


	/* Attributs */

	private static RandomData rng;
	private short[] lg;


	private gen_random() throws SystemException, NegativeArraySizeException {
		rng =  RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
		lg = JCSystem.makeTransientShortArray((short) 8, JCSystem.CLEAR_ON_RESET);
		// longueur de la séquence d'octets désirée
		lg[0] = 0;

	}

	public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
		try {
			new gen_random().register();
		}
		catch (SystemException se) {
			ISOException.throwIt((short) 0x0042);
		}
		catch (NegativeArraySizeException se) {
			ISOException.throwIt((short) 0x0043);
		}
	}

	public static void genRandom(byte[] buff , short nb)
	{
		try{
			rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);	
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
			{ISOException.throwIt((short) 0x05);
			} else{
				ISOException.throwIt((short) 0x06);
			}

		}
	}

	public void process(APDU apdu) throws ISOException {
		//récupération du buffer
		byte[] buffer = apdu.getBuffer();

		if (this.selectingApplet()) return;

		if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {

		case INS_NOUVEL_ALEA:
			//récupération du nombre d'octets demandés
			lg[0] = buffer[ISO7816.OFFSET_P1];
			//génération de lg nombres et mise
			try {
				rng.generateData(buffer, (short) 0,(short) lg[0]);	
			}
			catch(CryptoException ce) {
				ISOException.throwIt((short) 0x0001);
			}

			try {
				apdu.setOutgoingAndSend((short) 0, (short) lg[0]);	
			}
			catch(APDUException ae) {
				ISOException.throwIt((short) 0x0002);
			}
			break;

		default:
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}	
	}

}
