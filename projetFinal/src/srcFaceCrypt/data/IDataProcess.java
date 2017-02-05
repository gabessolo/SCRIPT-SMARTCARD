package data;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

/**
 * This interface specifies the operations which can be use 
 * in order to manipulate data.
 * @author flgy
 * 
 * @cons
 * Test if a database exists for the dbname and if this is the
 * case, initialize the the processor with the corresponding 
 * database. If the database doesn't exist, it's created.
 * @param user, the FaceCrypt user
 * @pre <pre>user != null && dbname != ""</pre>
 * 
 */
public interface IDataProcess {
	
	public final static String ALGO_HASH = "SHA-1"; 
	
	/**
	 * Return <code>true</code> if it exists a link between
	 * <code>friend</code> and <code>listname</code> and false otherwise.
	 * @param friend
	 * @param listname
	 * @return
	 * @throws SQLException
	 */
	public boolean hasLink(String friend, String listname) throws SQLException;
	
	/**
	 * @param listname
	 * @return all users whom belonged to <code>listname</code>.
	 * @throws SQLException 
	 * @pre
	 * 	<pre>listname != null && listname != ""</pre>
	 */
	public List<String> getUsersFromList(String listname) throws SQLException;

	/**
	 * @param pseudo
	 * @return The <code>friend</code>'s <code>pubKey</code>
	 * or null if the friend doesn't exist in the database.
	 * @throws SQLException 
	 * @pre
	 * 	<pre>friend != null && friend != ""</pre>
	 */
	public String getPubKey(String friend) throws SQLException;
	
	/**
	 * @return all users in the database.
	 * @throws SQLException 
	 */
	public List<String> getUsers() throws SQLException;
	
	/**
	 * @return all lists in the database.
	 * @throws SQLException 
	 */
	public List<String> getLists() throws SQLException;
	
	/**
	 * Add a friend in the database with a hash of the
	 * given public key.
	 * @param friend
	 * @param pubKey
	 * @throws SQLException 
	 * @throws NoSuchAlgorithmException
	 * @pre <pre>friend != null && friend != "" 
	 * 		&& pubKey != null && pubKey != ""
	 * </pre> 
	 */
	public void addFriend(String friend, String pubKey) throws NoSuchAlgorithmException, SQLException;
	
	/**
	 * Add friends in the database with for each his pubKey
	 * @param friends
	 * @throws NoSuchAlgorithmException
	 * @throws SQLException
	 * @pre <pre>friends != null</pre>
	 */
	public void addFriends(List<String[]> friends) throws NoSuchAlgorithmException, SQLException;
	
	/***
	 * Add a list in the database.
	 * @param listname
	 * @pre <pre> listname != null && listname != "" </pre>
	 * @throws SQLException 
	 */
	public void addList(String listname) throws SQLException;
	
	/**
	 * Add a <code>friend</code> to the list <code>listname</code>.
	 * @param friend
	 * @param listname 
	 * @pre <pre> friend != null && friend != "" 
	 * 	&& listanme != null && listname != ""
	 * </pre>
	 * @throws SQLException
	 */
	public void addLink(String friend, String listname) throws SQLException;
	
	/**
	 * Remove a friend from the database.
	 * @param friend
	 * @throws SQLException
	 * @pre <pre> friend != null && friend != "" </pre>
	 */
	public void removeFriend(String friend) throws SQLException;
	
	/**
	 * Remove a list from the database.
	 * @param listname
	 * @throws SQLException
	 * @pre <pre> listanme != null && listname != "" </pre>
	 */
	public void removeList(String listname) throws SQLException;
	
	/**
	 * Remove a link between a friend and a list
	 * @param friend
	 * @param listname
	 * @throws SQLException
	 * @pre <pre> listanme != null && listname != "" </pre>
	 */
	public void removeLink(String friend, String listname) throws SQLException;

	/**
	 * Terminate properly the database connection.
	 * @throws SQLException
	 * @pre <pre>this.conn != null</pre>
	 * @post <pre>this.conn == null </pre> 
	 */
	public void close() throws SQLException;
}
