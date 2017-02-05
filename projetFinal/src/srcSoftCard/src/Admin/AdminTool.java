package Admin;

import Default.SoftCard;

/**
 * This class allow the administrator to get (only once) the code PIN and PUK
 * stored on the smartcard. An error may be thrown if the PIN or the PUK has already
 * been requested. 
 * @param an argument (PIN or PUK) is expected to obtain the concerned one.
 * @author Emmanuel Mocquet
 *
 */
public class AdminTool {
	public static void main(String[] args) {
		try {
			byte[] b;
			int value1, value2;
			/*
			 * XX & 0xFF : cast to unsigned int
			 */
			if (args.length == 1) {
				SoftCard soft = SoftCard.getInstance();
				if (args[0].equals("PIN")) {
					b = soft.getPIN();
					value1 = (int) (b[0] & 0xFF);
					value2 = (int) (b[1] & 0xFF);
					System.out.println(String.format("%05d", (int) (value1 << 8 | value2)));
				}
				else if (args[0].equals("PUK")) {
					b = soft.getPUK();
					value1 = (int) (b[0] & 0xFF);
					value2 = (int) (b[1] & 0xFF);
					System.out.println(String.format("%05d", (int) (value1 << 8 | value2)));
				}
				else {
					System.err.println("Usage : getCode <PIN|PUK> ");
					System.exit(1);
				}
			}
			else {
				System.err.println("Usage : getCode <PIN|PUK> ");
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println("Error " + e.getMessage());
		} 
	}

}
