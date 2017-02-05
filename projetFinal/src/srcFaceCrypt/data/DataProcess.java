package data;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import messageDigest.Chopper;

public class DataProcess implements IDataProcess {

	private Connection conn;
	private Statement stat;

	// CONSTRUCTORS
	public DataProcess(String dbname) throws NoSuchAlgorithmException, ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		this.conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.home") 
				+ "/.ssn/" + dbname + ".db");
		this.stat = this.conn.createStatement();
		initializeDatabase();
	}

	// REQUESTS
	public boolean hasLink(String friend, String listname) throws SQLException {
		if (friend == null || friend.isEmpty() || listname == null || 
				listname.isEmpty()) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement(
				"SELECT 1 FROM (FriendsLists INNER JOIN Friends ON " +
				"(FriendsLists.id_friend = Friends.id_friend)) t INNER JOIN Lists ON " +
				"(t.id_list = Lists.id_list) WHERE pseudo = ? AND list_name = ?;"
		);
		prep.setString(1, friend);
		prep.setString(2, listname);
		ResultSet rs = prep.executeQuery();
		return rs.next();
	}
	
	public List<String> getUsersFromList(String listname) throws SQLException {
		if (listname == null || listname.isEmpty()) {
			throw new IllegalArgumentException();
		}
		List<String> res = new ArrayList<String>();
		PreparedStatement prep = this.conn.prepareStatement(
				"SELECT pseudo FROM (FriendsLists fl INNER JOIN Friends f ON " +
				"(fl.id_friend = f.id_friend)) t INNER JOIN Lists l ON " +
				"(t.id_list = l.id_list) WHERE list_name = ?;"
		);
		prep.setString(1, listname);
		ResultSet rs = prep.executeQuery();
		while (rs.next()) {
			res.add(rs.getString(1));
		}
		rs.close();
		return res;
	}
	
	public List<String> getUsers() throws SQLException {
		List<String> res = new ArrayList<String>();
		ResultSet rs = this.stat.executeQuery("SELECT pseudo FROM Friends;");
		while (rs.next()) {
			res.add(rs.getString(1));
		}
		rs.close();
		return res;
	}
	
	public List<String> getLists() throws SQLException {
		List<String> res = new ArrayList<String>();
		ResultSet rs = this.stat.executeQuery("SELECT list_name FROM Lists;");
		while (rs.next()) {
			res.add(rs.getString(1));
		}
		rs.close();
		return res;
	}

	public String getPubKey(String friend) throws SQLException {
		if (friend == null || friend.isEmpty()) {
			throw new IllegalArgumentException();
		}
		String res = "";
		PreparedStatement prep = this.conn.prepareStatement(
				"SELECT pubKey FROM Friends WHERE pseudo = ?;"
		);
		prep.setString(1, friend);
		ResultSet rs = prep.executeQuery();
		if (rs.next()) {
			res = rs.getString(1);
			rs.close();
			return res;
		}
		return null;
	}
	
	// COMMANDS
	public void addFriend(String friend, String pubKey) throws NoSuchAlgorithmException, SQLException {
		if (friend == null || friend.isEmpty() || pubKey == null || pubKey.isEmpty()) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement(
				"INSERT INTO Friends ('pseudo', 'pubKey') VALUES (?, ?);"
		);
		prep.setString(1, friend);
		prep.setString(2, pubKey);
		prep.executeBatch();
	}
	
	public void addFriends(List<String[]> friends) throws NoSuchAlgorithmException, SQLException {
		if (friends == null) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement(
				"INSERT INTO Friends ('pseudo', 'pubKey') VALUES (?, ?)"
		);
		for (String[] t : friends) {
			prep.setString(1, t[0]);
			prep.setString(2, new Chopper(ALGO_HASH).hash(t[1]));
			prep.addBatch();
		}
		this.conn.setAutoCommit(false);
		prep.executeBatch();
		this.conn.setAutoCommit(true);
	}
	
	public void addList(String listname) throws SQLException {
		if (listname == null || listname.isEmpty()) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement("INSERT INTO Lists ('list_name') VALUES (?);");
		prep.setString(1, listname);
		prep.addBatch();
		prep.executeBatch();
	}
	
	public void addLink(String friend, String listname) throws SQLException {
		if (friend == null || friend.isEmpty() || listname == null || 
				listname.isEmpty()) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement(
				"insert into FriendsLists select id_friend, id_list from Friends," +
				" Lists where pseudo = ? and list_name = ?;" 
		);
		prep.setString(1, friend);
		prep.setString(2, listname);
		prep.addBatch();
		prep.executeBatch();
	}
	
	public void removeFriend(String friend) throws SQLException {
		if (friend == null || friend.isEmpty()) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement(
				"delete from Friends where pseudo = ?;" 
		);
		prep.setString(1, friend);
		prep.addBatch();
		prep.executeBatch();
	}
	
	public void removeList(String listname) throws SQLException {
		if (listname == null || listname.isEmpty()) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement(
				"delete from Lists where list_name = ?;" 
		);
		prep.setString(1, listname);
		prep.addBatch();
		prep.executeBatch();
	}
	
	public void removeLink(String friend, String listname) throws SQLException {
		if (friend == null || friend.isEmpty() || listname == null || 
				listname.isEmpty()) {
			throw new IllegalArgumentException();
		}
		PreparedStatement prep = this.conn.prepareStatement(
				"delete from FriendsLists where " +
				"id_friend = (select id_friend from Friends where pseudo = ?) " +
				"and id_list = (select id_list from Lists where list_name = ?);" 
		);
		prep.setString(1, friend);
		prep.setString(2, listname);
		prep.addBatch();
		prep.executeBatch();
	}
	
	public void close() throws SQLException {
		if (this.conn == null) {
			throw new IllegalStateException();
		}
		this.conn.close();
		this.conn = null;
	}

	// Private method
	private void initializeDatabase() throws SQLException {
		this.stat.executeUpdate("PRAGMA foreign_keys=ON");
		this.stat.executeUpdate("CREATE TABLE IF NOT EXISTS Friends " + 
				"(id_friend INTEGER PRIMARY KEY AUTOINCREMENT, pseudo UNIQUE, pubKey);"
				);
		this.stat.executeUpdate("CREATE TABLE IF NOT EXISTS Lists " + 
				"(id_list INTEGER PRIMARY KEY AUTOINCREMENT, list_name UNIQUE);"
				);
		this.stat.executeUpdate("CREATE TABLE IF NOT EXISTS FriendsLists " + 
				"(id_friend INTEGER REFERENCES Friends(id_friend) ON DELETE CASCADE, " + 
				"id_list INTEGER REFERENCES Lists(id_list) ON DELETE CASCADE," +
				"PRIMARY KEY(id_friend, id_list));"
				);
	}

}
