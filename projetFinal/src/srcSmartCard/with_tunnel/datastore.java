/* Author : Romain Pignard 
 * 
 * This applet is the the datastore 
 * 
 * The tunnel puts its data here chunk by chunk after decryption
 * When the reception is complete, the <applet>.execute(data) function
 * is called, <applet> depending on the first byte of the data. 
 * This emulates the applet selection process.
 * Instead of apdu.send(..), applets use datastore.putData() 
 * to send their response to the client
 * 
 * 
 * */

package with_tunnel;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class datastore extends Applet {

	private static byte[] data;	
	private static short[] tab;
	// offset AID : offset containing the applet AID
	private static final short OFF_AID = 0x00;	
	
	// offset AID values
	private static final short AID_PIN = 0x00; 
	private static final short AID_RNG = 0x01;
	private static final byte AID_CYPHER = 0x03;
	private static final byte AID_SIGN = 0x02;
	private static final byte AID_STORE = 0x04; 
	
	
	
	private datastore() {	
		// temp data storage, increase for larger capacity
		data = JCSystem.makeTransientByteArray((short) 512, JCSystem.CLEAR_ON_RESET);
		// loop variables and indexes
		tab = JCSystem.makeTransientShortArray((short) 7, JCSystem.CLEAR_ON_RESET);
		// initial length of the buffer
		tab[1] = 0;
		// initial current position 
		tab[2] = 0;
	}

	public static void putData(byte[] input, short length)
	{
		// store data in the datastore
		
		Util.arrayCopy(input, (short) 0,data ,(short) tab[1],length);	
		
		// increase the actual used memory
		tab[1] = (short) (tab[1] + length);		
	}
	public static void putData(byte[] input, short length, short off)	
	{
		// store data in the datastore
		
		Util.arrayCopy(input, (short) off,data ,(short) tab[1],length);
		
		
		// increase the actual used memory
		tab[1] = (short) (tab[1] + length);		
	}
	
	
	public static void getData(byte[] input, short length) 
	{
		// retrieve data from the datastore
		
		Util.arrayCopy(data, (short) tab[2],input ,(short) 0,length);
		
		
		// increase the current pointer
		tab[2] = (short) (tab[2] + length);
	} 
	public static short getRemainingData(short maximum)
	{
		// return the remaining data not read yet
		return (short) (maximum < (short)(tab[1] - tab[2])  ? maximum : (short)(tab[1] - tab[2]));
	}
	public static void eraseData()
	{
		// virtually erase the data by putting the pointers at the beginning
		tab[1] = 0;
		tab[2] = 0;
	}
	
	
	
	
	
	public static void install(byte bArray[], short bOffset, byte bLength)
			throws ISOException {
		new datastore().register();
	}

	
	public void process(APDU arg0) throws ISOException {
		

	}

	public static void execute() {
		switch(data[OFF_AID]){
		// we give the data "as-is" to the applets		
		case AID_PIN:			
			PIN.execute(data);			
			break;
		case AID_RNG:			
			gen_random.execute(data);
			break;
		case AID_SIGN:
			Sign.execute(data);
			break;
		case AID_CYPHER:
			Cypher.execute(data);
			break;
		case AID_STORE:
			StoreID.execute(data);
			break;
		default:
			ISOException.throwIt((short) 0x60);
		}
	}

}
