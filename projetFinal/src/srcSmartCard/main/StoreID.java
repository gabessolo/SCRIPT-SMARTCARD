package main;

import javacard.framework.APDU;
import javacard.framework.APDUException;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.TransactionException;
import javacard.framework.Util;

public class StoreID extends Applet {
	/* Constantes */
	private static final byte CLA_STORE = (byte) 0xB0;
	private static final byte INS_STORE_LOGIN = 0x00;
	private static final byte INS_STORE_PWD = 0x01;
	private static final byte INS_VALIDATE_PWD = 0x02;
	private static final byte INS_GET = 0x03;
	private static final byte INS_GET_PWD = 0x04;		

	private byte[] login, delimiter, pwd, tmpPwd;
	private short[] dataLen;
	private boolean[] unlocked;

	/* Constructeur */
	private StoreID() {
		login = new byte[]{}; 
		// This delimiter represents a ' ' (space).
		delimiter = new byte[]{0x20};
		pwd =  new byte[]{};
		tmpPwd =  new byte[]{};

		// Will contain the length of the received data.
		dataLen = JCSystem.makeTransientShortArray((short) 1, JCSystem.CLEAR_ON_DESELECT);
		dataLen[0] = 0;

		unlocked = JCSystem.makeTransientBooleanArray((short) 1, JCSystem.CLEAR_ON_RESET);
		unlocked[0] = true;
	}

	public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
		new StoreID().register();
	}


	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();

		if (this.selectingApplet()) return;

		if (PIN.getState() == (short)0x9000) {
			if (buffer[ISO7816.OFFSET_CLA] != CLA_STORE) {
				ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
			}

			switch (buffer[ISO7816.OFFSET_INS]) {
			/**
			 * Store the provided login.
			 */
			case INS_STORE_LOGIN:
				try{
					dataLen[0] = apdu.setIncomingAndReceive();
					login = new byte[dataLen[0]];
					Util.arrayCopy(buffer, (short)ISO7816.OFFSET_CDATA, login, (short) 0, dataLen[0]);

					buffer[0] = 1;
					apdu.setOutgoingAndSend((short) 0, (short) 1);
				} catch(APDUException e) {
					ISOException.throwIt((short) 0x0001);
				} catch(NullPointerException e) {
					ISOException.throwIt((short) 0x0002);
				} catch(ArrayIndexOutOfBoundsException e) {
					ISOException.throwIt((short) 0x0003);
				} catch(TransactionException e) {
					ISOException.throwIt((short) 0x0004);
				}
				break;

				/**
				 * Store the provided password, temporary.
				 */
			case INS_STORE_PWD:
				try{
					dataLen[0] = apdu.setIncomingAndReceive();
					tmpPwd = new byte[dataLen[0]];
					Util.arrayCopy(buffer, (short)ISO7816.OFFSET_CDATA, tmpPwd, (short) 0, dataLen[0]);

					buffer[0] = 1;
					apdu.setOutgoingAndSend((short) 0, (short) 1);
				} catch(APDUException e) {
					ISOException.throwIt((short) 0x0001);
				} catch(NullPointerException e) {
					ISOException.throwIt((short) 0x0002);
				} catch(ArrayIndexOutOfBoundsException e) {
					ISOException.throwIt((short) 0x0003);
				} catch(TransactionException e) {
					ISOException.throwIt((short) 0x0004);
				}
				break;


				/**
				 * This block is called when the password has been successfully 
				 * modified/saved on the user side.
				 */
			case INS_VALIDATE_PWD:
				try{
					if (tmpPwd.length > 0) {
						pwd = new byte[tmpPwd.length];
						Util.arrayCopy(tmpPwd, (short)0, pwd, (short) 0, (short) tmpPwd.length);
						tmpPwd = new byte[]{};
						buffer[0] = 1;
						apdu.setOutgoingAndSend((short) 0, (short) 1);
					}
					else {
						buffer[0] = 0;
						apdu.setOutgoingAndSend((short) 0, (short) 1);
					}
				} catch(APDUException e) {
					ISOException.throwIt((short) 0x0001);
				} catch(NullPointerException e) {
					ISOException.throwIt((short) 0x0002);
				} catch(ArrayIndexOutOfBoundsException e) {
					ISOException.throwIt((short) 0x0003);
				} catch(TransactionException e) {
					ISOException.throwIt((short) 0x0004);
				}
				break;

				/**
				 * Copy login+delimiter+password+delimiter+newPassword into the buffer and send it
				 */
			case INS_GET:
				try{
					Util.arrayCopy(login, (short) 0, buffer, (short) 0, (short) login.length);
					Util.arrayCopy(delimiter, (short) 0, buffer, (short) login.length, (short) delimiter.length);
					Util.arrayCopy(tmpPwd, (short) 0, buffer, (short) (login.length + delimiter.length), (short) tmpPwd.length);
					Util.arrayCopy(delimiter, (short) 0, buffer, (short) (login.length + delimiter.length + tmpPwd.length), (short) delimiter.length);
					Util.arrayCopy(pwd, (short) 0, buffer, (short) (login.length + delimiter.length*2 + tmpPwd.length), (short) pwd.length);
					
					apdu.setOutgoing();
					apdu.setOutgoingLength((short)(login.length + delimiter.length*2 + tmpPwd.length + pwd.length));
					apdu.sendBytesLong(buffer, (short) 0, (short)(login.length + delimiter.length*2 + tmpPwd.length + pwd.length));
				} catch(APDUException e) {
					ISOException.throwIt((short) 0x0001);
				} catch(NullPointerException e) {
					ISOException.throwIt((short) 0x0002);
				} catch(ArrayIndexOutOfBoundsException e) {
					ISOException.throwIt((short) 0x0003);
				} catch(TransactionException e) {
					ISOException.throwIt((short) 0x0004);
				}
				break;

				/**
				 * Whenever the user wants to retrieve his password, this block is called.
				 */
			case INS_GET_PWD:
				try{
					Util.arrayCopy(pwd, (short) 0, buffer, (short) 0, (short) pwd.length);

					apdu.setOutgoing();
					apdu.setOutgoingLength((short)(pwd.length));
					apdu.sendBytesLong(buffer, (short) 0, (short)(pwd.length));
				} catch(APDUException e) {
					ISOException.throwIt((short) 0x0001);
				} catch(NullPointerException e) {
					ISOException.throwIt((short) 0x0002);
				} catch(ArrayIndexOutOfBoundsException e) {
					ISOException.throwIt((short) 0x0003);
				} catch(TransactionException e) {
					ISOException.throwIt((short) 0x0004);
				}
				break;

			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}
		}
		else {
			buffer[0] = -1;
			apdu.setOutgoingAndSend((short) 0, (short) 1);
		}
	}
}
