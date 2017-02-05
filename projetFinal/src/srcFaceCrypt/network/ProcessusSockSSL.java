package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

import org.apache.commons.codec.binary.Base64;

import cache.CacheManager;
import cache.ICacheManager;
import data.DataProcess;

/**
 * This class represents a client thread. It as few methods for its management.
 * 
 * @author Baptiste Dolbeau
 * @version 1.0
 */
class ProcessusSockSSL extends Thread {

	private SSLSocket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private Client cli = null;
	ICacheManager cache = null;

	/**
	 * This constructor link a reader and a printer to the socket and start the
	 * thread.
	 * 
	 * @param socket
	 *            The client's socket.
	 * @throws IOException
	 */
	public ProcessusSockSSL(SSLSocket socket) throws IOException {
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			this.start();
		} catch (IOException e) {
			System.err.println("An error occured while receiving message 1");
			free();
		}
	}

	/**
	 * This method will be executed by the thread. It follows the rule : One
	 * message received, One message sent.
	 */
	public void run() {
		cli = new Client("127.0.0.1", 42425);
		JSONObject obj = null;
		boolean bool = true;
		cache = new CacheManager();
		System.out.println("Client connected");
		try {
			obj = receiveMessages();
		} catch (IOException e) {
			System.err.println("An error occured while receiving message 2");
			e.printStackTrace();
			return;
		}
		try {
			if (obj.getString("action").equals("quit")) {
				cli.proceedSocket();
				cli.requestQuit();
				bool = false;
			}
		} catch (JSONException e1) {
			System.err.println("Can't get field action");
		}
		while (bool) {
			doAction(obj, cli);
			try {
				if (bool) {
					obj = receiveMessages();
				}
			} catch (IOException e) {
				System.err
						.println("An error occured while receiving message 3");
				return;
			}
			try {
				if (obj.getString("action").equals("quit")) {
					cli.requestQuit();
					bool = false;
				}
			} catch (JSONException e) {
				System.err.println("Can't get field action");
			}
		}
		try {
			free();
		} catch (IOException e) {
			System.err.println("An error occured while closing I/O streamers");
		}
		System.out.println("Client disconnected");
	}

	/**
	 * This method manages the process of firefox extention's requests. Each
	 * request is described as a JSON object. It has to contain a field "action"
	 * which specifies what to do.
	 * 
	 * @param obj
	 *            The JSON Object which is received.
	 */
	private void doAction(JSONObject obj, Client cli) {
		String action;
		JSONObject rep = null;
		cli.proceedSocket();
		if (obj == null) {
			System.err.println("The JSON Object is null");
			return;
		}

		try {
			action = (String) obj.get("action");
		} catch (JSONException e) {
			System.err.println("No field \"action\" in the received object");
			return;
		}

		if (action.equals("getID")) {
			rep = cli.requestID();
			try {
				sendMessage(rep);
			} catch (IOException e) {
				System.err.println("An error occured while sending id");
			}
		} else if (action.equals("setID")) {
			String login = "", pass = "";
			try {
				login = obj.getString("login");
				/* To initialize the database */
				try {
					new DataProcess(login);
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				pass = obj.getString("pass");
				rep = cli.requestSetID(login, pass);
				try {
					sendMessage(rep);
				} catch (IOException e) {
					System.err
							.println("An error occured while sending the validating password message");
				}

			} catch (JSONException e) {
				System.err.println("An error occured while modifying IDs");
			}
		} else if (action.equals("modifPass")) {
			rep = cli.requestModifPass();
			try {
				sendMessage(rep);
			} catch (IOException e) {
				System.err.println("An error occured while sending password");
			}
		} else if (action.equals("validPass")) {
			rep = cli.requestValidPass();
			try {
				sendMessage(rep);
			} catch (IOException e) {
				System.err
						.println("An error occured while sending the validating password message");
			}
		} else if (action.equals("pubKey")) {
			rep = cli.requestPubKey();
			try {
				sendMessage(rep);
			} catch (IOException e) {
				System.err.println("An error occured while sending public key");
			}
		} else if (action.equals("encrypt")) {
			ArrayList<byte[]> pubkeys = null;
			ArrayList<String> names = null;
			JSONArray listsJSON = null;
			DataProcess dp = null;
			String s = null;
			boolean bool = false;

			try {
				listsJSON = (JSONArray) obj.get("list");
				bool = obj.getBoolean("ano");
				s = obj.getString("data");
			} catch (JSONException e1) {
				System.err
						.println("An error occured while extracting data (encrypt)");
				e1.printStackTrace();
			}
			try {
				dp = new DataProcess(getUserName());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			// Pour chaque liste, on extrait les noms
			try {
				names = extractNames(listsJSON, dp);
			} catch (SQLException e) {
				System.err.println("Error while extracting names");
				e.printStackTrace();
			} catch (JSONException e) {
				System.err
						.println("An error occured while extracting names (encrypt)");
				e.printStackTrace();
			}

			// Pour chaque nom, on extrait sa clef publique
			try {
				pubkeys = extractPubKeys(dp, names);
			} catch (SQLException e) {
				System.err.println("Error while extracting public keys");
				e.printStackTrace();
			} catch (IOException e) {
				System.err
						.println("An I/O error occured extracting public keys (encrypt) ");
				e.printStackTrace();
			}

			// On chiffre le message demandé
			rep = cli.requestEncrypt(names, pubkeys, s, bool);

			// On envoie la réponse
			try {
				sendMessage(rep);
			} catch (IOException e) {
				System.err
						.println("An error occured while sending data (encrypt)");
				e.printStackTrace();
			}
		} else if (action.equals("encryptCom")) {
			String id = null, message = null;

			// On extrait l'id et le commentaire
			try {
				message = obj.getString("message");
				id = obj.getString("id");
			} catch (JSONException e1) {
				System.err
						.println("An error occured while extracting data (encryptCom)");
				e1.printStackTrace();
			}

			// On chiffre le commentaire demandé
			rep = cli.requestEncryptCom(id, message, (CacheManager) cache);

			// On envoie la réponse
			try {
				sendMessage(rep);
			} catch (IOException e) {
				System.err
						.println("An error occured while sending data (encryptCom)");
				e.printStackTrace();
			}
		} else if (action.equals("decrypt")) {
			JSONArray commentaires = null;
			ArrayList<String> paquet = null;
			String packet = null;
			boolean bool = false;
			String id = null;

			// Récupération des infos du JSON
			try {
				id = obj.getString("id");
				bool = obj.getBoolean("ano");
				commentaires = obj.getJSONArray("comms");
				packet = obj.getString("message");
			} catch (JSONException e) {
				System.err
						.println("An error occured while extracting data (decrypt)");
				e.printStackTrace();
			}

			// Splittage du bloc message
			// Recupération liste de Strings
			// Attention dernier élément = message chiffré symétriquement
			try {
				paquet = splitMess(packet, bool);
			} catch (JSONException e) {
				System.err.println("An error occured while slitting message");
			}

			// On dechiffre le message demandé
			rep = cli.requestDecrypt(paquet, id, commentaires,
					(CacheManager) cache);

			// On envoie la réponse
			try {
				rep.put("id", id);
			} catch (JSONException e) {
				System.err
						.println("An error occured while inserting data (decrypt)");
				e.printStackTrace();
			}
			try {
				sendMessage(rep);
			} catch (IOException e) {
				System.err
						.println("An error occured while sending data (encrypt)");
				e.printStackTrace();
			}
		} else if (action.equals("quit")) {
			try {
				System.err.println("quitting");
				cli.requestQuit();
				free();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<String> splitMess(String packet, Boolean bool) throws JSONException {
		String[] tab = null, tab2 = null;
		ArrayList<String> l = new ArrayList<String>();

		tab = packet.split(",");
		if (bool) {
			for (int i = 0; i < tab.length - 1; i++) {
				l.add(tab[i]);
			}
		} else {
			JSONObject JSONlogin = cli.requestID();
			String login = JSONlogin.getString("login");
			for (int i = 0; i < tab.length - 1; i++) {
				tab2 = tab[i].split("-");
				if (tab2[0].equals(login)) {
				l.add(tab2[1]);
				}
			}

		}
		l.add(tab[tab.length - 1]);
		return l;
	}

	/**
	 * This method will retrieve a response from the bufferedReader object
	 * 
	 * @return The message received.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private JSONObject receiveMessages() throws IOException {
		String mess = in.readLine();
		JSONObject obj = null;
		if (mess != null) {
			try {
				obj = new JSONObject(mess);
			} catch (JSONException e) {
				System.err
						.println("An error occured while parsing string to JSON");
				e.printStackTrace();
			}
		} else {
			try {
				obj = new JSONObject("{\"action\" : \"error\";}");
			} catch (JSONException e) {
				System.err
						.println("An error occured while parsing string to JSON");
				e.printStackTrace();
			}
		}
		System.out.println("Réçu de Facecrypt : " + obj.toString());
		return obj;
	}

	/**
	 * This method will write a JSON object into the ObjectOutputStream to send
	 * on the socket. Then, it flush the buffer.
	 * 
	 * @param obj
	 *            Object to be sent
	 * @throws IOException
	 */
	private void sendMessage(JSONObject obj) throws IOException {
		String packet = "";
		packet = obj.toString();
		out.print(packet);
		out.flush();
		System.out.println("Envoyé à Facecrypt : " + packet);
	}

	/**
	 * This method closes buffers, socket and exit the program.
	 * 
	 * @throws IOException
	 */
	private void free() throws IOException {
		in.close();
		out.close();
		socket.close();
	}

	/**
	 * This method extracts all names of each list contained in the parameter .
	 * It deals with duplicate's name.
	 * 
	 * @param lists
	 *            The list of all list's name.
	 * @return The list of user's name.
	 * @throws JSONException
	 * @throws SQLException
	 */
	public ArrayList<String> extractNames(JSONArray lists, DataProcess dp)
			throws JSONException, SQLException {
		ArrayList<String> namesList = new ArrayList<String>();
		List<String> names = null;
		String bla;
		int i = 0;

		for (i = 0; i < lists.length(); i++) {
			bla = lists.getString(i);
			names = dp.getUsersFromList(bla);
			for (int j = 0; j < names.size(); j++) {
				if (!namesList.contains(names.get(j))) {
					namesList.add(names.get(j));
				}
			}
		}
		namesList.add(getUserName());
		return namesList;
	}

	/**
	 * This method extracts the public key in the database of each name in the
	 * list "names".
	 * 
	 * @param names
	 *            The list of friends.
	 * @return The list of public keys.
	 * @throws SQLException
	 * @throws IOException
	 */
	public ArrayList<byte[]> extractPubKeys(DataProcess dp,
			ArrayList<String> names) throws SQLException, IOException {
		ArrayList<byte[]> pubKeys = new ArrayList<byte[]>();
		String pubKeyString = null;
		byte[] getPubKey = { (byte) 0x42 };
		byte[] pubKeyByte;
		for (int i = 0; i < names.size() - 1; i++) {
			pubKeyString = dp.getPubKey(names.get(i));
			pubKeyByte = Base64.decodeBase64(pubKeyString);
			pubKeys.add(pubKeyByte);
		}
		// Demande + reception + ajout clef publique perso
		cli.sendRequest(getPubKey);
		byte[] test = cli.receiveMessage();
		pubKeys.add(test);
		return pubKeys;
	}

	/**
	 * This method is used to retrieved the user name of the actual user
	 * 
	 * @return the user login
	 * @throws JSONException
	 */
	public String getUserName() throws JSONException {
		JSONObject obj = null;
		String userName;

		obj = cli.requestID();
		userName = obj.getString("login");

		return userName;
	}
}
