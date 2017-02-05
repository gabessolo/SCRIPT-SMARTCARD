package with_tunnel;

/**
 * @author Romain Pignard
 */

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;

public class PIN extends Applet {
	/* Constantes */
	public static final byte INS_VERIF_PIN = 0x00;
	public static final byte INS_PIN_REMAINING_TRIES = 0x01;
	public static final byte INS_PUK_REMAINING_TRIES = 0x02;
	public static final byte INS_UNLOCK_WITH_PUK = 0x03;
	private static final byte INS_GET_PIN = 0x04;
	private static final byte INS_GET_PUK = 0x05;

	private static final byte INS_IS_LOCKED = 0x06;

	private static OwnerPIN pin; 
	private static OwnerPIN puk;

	private static boolean pin_retrieved;
	private static boolean puk_retrieved;

	public static boolean test_PIN(byte[] PIN, short offset, short length)
	{
		return pin.check(PIN, offset, (byte) length);			
	}
	
	/* Execute the action erquested by the user  */
	public static void execute(byte[] buffer)
	{
		switch (buffer[ISO7816.OFFSET_INS]) {

		/* Return 1 if pin is locked, 0 otherwise */
		case INS_IS_LOCKED:
			if (pin.isValidated())
			{
				buffer[0] = 1;				
			}
			else
			{
				buffer[0] = 0;
			}
			datastore.eraseData();
			datastore.putData(buffer, (short) 1);
			break;

		/* Return the remaining tries to enter the PIN */
		case INS_PIN_REMAINING_TRIES:
			buffer[0] = pin.getTriesRemaining();
			datastore.eraseData();
			datastore.putData(buffer, (short) 1);
			
			break;

		/* Return the remaining tries to enter the PUK */
		case INS_PUK_REMAINING_TRIES:
			buffer[0] = puk.getTriesRemaining();
			datastore.eraseData();
			datastore.putData(buffer, (short) 1);
			break;

		/* Return 1 if the PIN received is valided, 0 otherwise */
		case INS_VERIF_PIN:
			if(pin.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) buffer[ISO7816.OFFSET_P1]))
			{
				buffer[0] = 1;
			}
			else
			{
				buffer[0] = 0;
			}
			datastore.eraseData();
			datastore.putData(buffer, (short) 1);
			break
;
		/* Unlock the PIN code with the PUK */
		case INS_UNLOCK_WITH_PUK:
			if(puk.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) buffer[ISO7816.OFFSET_P1]))
			{
				pin.resetAndUnblock();
				gen_random.genRandom(buffer, (short) 2);
				pin.update(buffer, (short) 0, (byte) 2);
				datastore.eraseData();
				datastore.putData(buffer, (short) 2);
			}
			else
			{
				datastore.eraseData();
			}

			break;

		/* Initialization of PIN */
		case INS_GET_PIN:
			if (!pin_retrieved) {
				try {
					pin.resetAndUnblock();
					gen_random.genRandom(buffer, (short) 2);
					pin.update(buffer, (short) 0, (byte) 2);
					datastore.eraseData();
					datastore.putData(buffer, (short) 2);
					pin_retrieved = true;
				} catch(Exception e) {
					ISOException.throwIt((short) 0x01);
				}
			}
			else {
				gen_random.genRandom(buffer, (short) 2);
				ISOException.throwIt((short) 0x02);
			}
			break;

		/* Initialization of PUK */
		case INS_GET_PUK:
			if (!puk_retrieved) {
				try {
					puk.resetAndUnblock();
					gen_random.genRandom(buffer, (short) 2);
					puk.update(buffer, (short) 0, (byte) 2);
					datastore.eraseData();
					datastore.putData(buffer, (short) 2);
					puk_retrieved = true;
				} catch(Exception e) {
					ISOException.throwIt((short) 0x01);
				}
			}
			else {
				gen_random.genRandom(buffer, (short) 2);
				ISOException.throwIt((short) 0x02);
			}
			break;

		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);

		}
	}

	/* Constructor */
	private PIN() {
		pin = new OwnerPIN((byte) 3, (byte) 2 );
		pin.update(new byte[]{15,12}, (short) 0, (byte) 2);
		puk = new OwnerPIN((byte) 3, (byte) 4);
		puk.update(new byte[]{15,12,45,124}, (short) 0, (byte) 4);
		pin_retrieved = false;
		puk_retrieved = false;
	}

	public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
		new PIN().register();
	}

	
	/**
	* Return 0x1235 if we are entered too many wrong PIN,
	* 0x1234 if PIN is locked, 0x9000 otherwise.
	*/
	public static short getState(){
		if(pin.getTriesRemaining() == 0) {
			return (short) 0x1235;
		} else if (!pin.isValidated()) {
			return (short) 0x1234;
		}
		return (short) 0x9000;

	}

	
	/**     
         * This method is called when the applet is being called from outside the tunnel.
         * @return void
         * @throws ISOException if an error occured while processing the request.
         */
	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();

		if (this.selectingApplet()) return;
		
		switch (buffer[ISO7816.OFFSET_INS]) {

		/* Return the remaining tries to enter the PIN */		
		case INS_PIN_REMAINING_TRIES:
			buffer[0] = pin.getTriesRemaining();
			apdu.setOutgoingAndSend((short) 0, (short) 1);
			break;

		/* Return the remaining tries to enter the PUK */		
		case INS_PUK_REMAINING_TRIES:
			buffer[0] = puk.getTriesRemaining();
			apdu.setOutgoingAndSend((short) 0, (short) 1);
			break;

		/* Return 1 if the PIN received is valided, 0 otherwise */
		case INS_VERIF_PIN:
			if(pin.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) buffer[ISO7816.OFFSET_P1]))
			{
				buffer[0] = 1;
			}
			else
			{
				buffer[0] = 0;
			}
			apdu.setOutgoingAndSend((short) 0, (short) 1);
			break;

		/* Unlock the PIN code with the PUK */
		case INS_UNLOCK_WITH_PUK:
			if(puk.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) buffer[ISO7816.OFFSET_P1]))
			{
				pin.resetAndUnblock();
				gen_random.genRandom(buffer, (short) 2);
				pin.update(buffer, (short) 0, (byte) 2);
				apdu.setOutgoingAndSend((short) 0, (short) 2);	
			}
			else
			{
				gen_random.genRandom(buffer, (short) 2);
				ISOException.throwIt((short) 0x0001);
			}

			break;

		/* Initialization of PIN */
		case INS_GET_PIN:
			if (!pin_retrieved) {
				try {
					pin.resetAndUnblock();
					gen_random.genRandom(buffer, (short) 2);
					pin.update(buffer, (short) 0, (byte) 2);
					apdu.setOutgoingAndSend((short) 0, (short) 2);
					pin_retrieved = true;
				} catch(Exception e) {
					ISOException.throwIt((short) 0x01);
				}
			}
			else {
				gen_random.genRandom(buffer, (short) 2);
				ISOException.throwIt((short) 0x02);
			}
			break;

		/* Initialization of PUK */
		case INS_GET_PUK:
			if (!puk_retrieved) {
				try {
					puk.resetAndUnblock();
					gen_random.genRandom(buffer, (short) 2);
					puk.update(buffer, (short) 0, (byte) 2);
					apdu.setOutgoingAndSend((short) 0, (short) 2);
					puk_retrieved = true;
				} catch(Exception e) {
					ISOException.throwIt((short) 0x01);
				}
			}
			else {
				gen_random.genRandom(buffer, (short) 2);
				ISOException.throwIt((short) 0x02);
			}
			break;

		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}
