//package Default;

/**
 * This class abstracts the errors sent to the client if necessary.
 * @author Emmanuel Mocquet
 *
 */
public enum NetworkException {
	
	ERROR_CONNECTION_CARD(new byte[]{(byte)0xff}),
	ERROR_PUBKEY(new byte[]{(byte)0xfe}),
	ERROR_RANDOM_NUMBER(new byte[]{(byte)0xfd}),
	ERROR_DECRYPT(new byte[]{(byte)0xfc}),
	ERROR_UNLOCK_CARD(new byte[]{(byte)0xfb}),
	ERROR_CHECK_LOCKED(new byte[]{(byte)0xfa}),
	ERROR_STORE_ID(new byte[]{(byte)0xef}),
	ERROR_GET_ID(new byte[]{(byte)0xee}),
	ERROR_RESET_PASSWORD(new byte[]{(byte)0xed}),
	ERROR_VALIDATE_PASSWORD(new byte[]{(byte)0xee});
	
	private byte[] value;

	private NetworkException(byte[] b) {
		value = new byte[b.length]; 
		System.arraycopy(b, 0, value, 0, b.length);
	}
	
	public byte[] getValue() {
		return this.value;
	}
}
