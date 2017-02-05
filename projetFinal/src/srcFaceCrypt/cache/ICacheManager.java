package cache;

/**
 * This interface specifies a custom cache object which will contains
 * some decrypted payload.
 * 
 * @author Florian Guilbert.
 * 
 * @cons
 * 	initialize the map and the stack.
 *
 */
public interface ICacheManager {

	public static final int MAX_SIZE = 1500;
	
	/**
	 * Add in the cache the following couple.
	 * @param request, the crypted string 
	 * @param payload, the decrypted string
	 * 
	 * <pre>
	 * 	request != null && request != ""
	 *  payload != null && payload != ""
	 * </pre>
	 */
	public void addPayload(String request, byte[] payload);
	
	/**
	 * Return the decrypted value matching the request.
	 * @param request, the crypted string 
	 * 
	 * <pre>
	 * 	request != null && request != ""
	 * </pre>
	 */
	public byte[] getPayload(String request);
	
}