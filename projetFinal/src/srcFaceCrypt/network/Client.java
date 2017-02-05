package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;


import json.JSONArray;
import json.JSONException;
import json.JSONObject;

import cypher.AsymCypher;
import cypher.IAsymCypher;
import cypher.ISymCypher;
import cypher.SymCypher;

import cache.*;




/**
 * This class will create a Client socket.
 * This one will be able to connect to a
 * server and sending some messages to it. 
 * @author Baptiste Dolbeau
 * @version 1.0
 */
public class Client {
	private DataInputStream in = null; 
	private DataOutputStream out = null;


	private InetAddress adresse = null;
	private SSLSocket socket = null;

	/**
	 * This constructor initializes the client connection.
	 * First it translates the string address
	 * into an InetAddress object.
	 * Then it set up the SSL Context (certificate,
	 * etc). 
	 * Finally it creates an SSL Socket which will
	 * connect on the specified port.
	 * @param adr The address of the server
	 * @param port The port on which the client
	 * will connect. 
	 */
	public Client(String adr, int port){
		try {
			adresse = Inet4Address.getByName(adr);			
		} catch (UnknownHostException e) {
			System.err.println("Non valid IP address");
			e.printStackTrace();
			System.exit(1);
		}

		setContext();
		SSLSocketFactory sslFact = (SSLSocketFactory)SSLSocketFactory.getDefault();

		try {
			socket = (SSLSocket) sslFact.createSocket(adresse, port);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("An error occured while creating socket (It may be because of certificats)");
			System.exit(2);
		}
	}
	
	
	/**
	 * This method will process the socket.
	 * It follows the rule : One message 
	 * sent, one message received.
	 */
	public void proceedSocket() {
		try {
			out = new DataOutputStream(socket.getOutputStream());	
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.err.println("An error occured while setting I/O streamers");
		}
	}
	
	
	/**
	 * This method set up the SSL context. It specifies the
	 * truststore which contains the authority the client
	 * will trust in.
	 */
	private void setContext() {
		// Certificats Facecrypt
		System.setProperty("javax.net.ssl.keyStore", "../certTest/facecrypt.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "lolilol");

		// Certificats de confiances
		System.setProperty("javax.net.ssl.trustStore", "../certTest/trusts.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "lolilol");
	}
	
	
	/**
	 * This method first send the length of the message
	 * which will follow (needed for the server).
	 * Then it concatenates the action and the body bytes
	 * array.
	 * 
	 * @param action An "one byte array" which specifies the action
	 * needed. For example : 0x43 to decode.
	 * @param body Data needed for the requested action.
	 */
	public void sendRequest(byte[] action, byte[] body) {
		try {
			byte[] packet = new byte[action.length + body.length];
			// Envoi de la taille des données qui vont suivre
			out.writeInt(action.length + body.length);
			// Creation du bytes array
			System.arraycopy(action, 0, packet, 0, action.length);
			System.arraycopy(body, 0, packet, action.length, body.length);
			// Envoi des données
			out.write(packet, 0, packet.length);
			out.flush();
			System.out.println("send to Softcard : "  + byteToHexString(packet));
			
		} catch (IOException e) {
			System.err.println("An error occured while sending bytes");
		}
	}
	
	
	/**
	 * This method send a one byte array. This byte specify
	 * the action needed.
	 * 
	 * 0x41 to quit
	 * 0x42 to ask the public key
	 * 0x43 to ask a random bytes array
	 * 0x44 to decrypt
	 * 0x45 to ask if the card is locked
	 * 0x46 to ask to unlock
	 * 0x47 to ask for the id
	 * 0x48 to ask for setting the id
	 * 0x49 to ask for resetting the passwd
	 * 0x50 to valid the passwd
	 *  
	 * @param action The "one byte array" specifiyng the action requested.
	 */
	public void sendRequest(byte[] action) throws IOException {
		out.writeInt(action.length);
		out.write(action, 0, action.length);
		System.out.println("Send to Softcard : "  + byteToHexString(action));
		out.flush();
	}
	
	
	/**
	 * This method is used to receive a message. Using the
	 * DataInputStream, it first read the length of the
	 * packet that will follows. 
	 * 
	 * @return Data read.
	 */
	public byte[] receiveMessage() {
		byte[] packet = null;
		try {
			// Lecture de la taille des données qui vont suivre
			int i = in.readInt();
			packet = new byte[i];
			// Lecture des données
			in.read(packet);
			System.out.println("Received from Softcard : "  + byteToHexString(packet));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("An error occured while receiving bytes");
		}
		return packet;
	}
	
	
	/**
	 * This method is used to create the list which contains
	 * the cipher of the symmetric key encrypted by each public
	 * key in the list.
	 * 
	 * @param listPubKey The list of public keys
	 * @param symKey The symmetric key
	 * @return The list of the symmetric key encrypted
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String encryptPubKeysAnon(ArrayList<byte[]> listPubKey, byte[] symKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
	byte[] getSeed = {(byte) 0x43, (byte) 0x20};
		byte [] seed, cypherKEY = null;
		String packet = "";
		// Pour chaque clef publique
		for (int i=0; i<listPubKey.size(); i++) {

			// Demande et réception d'un seed
			try {
				this.sendRequest(getSeed);
			} catch (IOException e) {
				System.err.println("An error occured while sending bytes");
			}
			seed = this.receiveMessage();

			// Chiffrement de la clef symetrique
			IAsymCypher ias = new AsymCypher("RSA", "", "", listPubKey.get(i), seed);
			cypherKEY = ias.encrypt(symKey);
			
			// Concaténation du chiffré dans la string
			packet += Base64.encodeBase64String(cypherKEY);
			if (i != (listPubKey.size()-1)) {
				packet += ",";
			}
			
		}
		packet += ",";
		return packet;
	}
	
	
	/**
	 * This method is used to create a String object
	 * which will contains each pseudo and the
	 * cipher of the symmetric key encrypted by
	 * each public key in the list
	 * 
	 * @param names The pseudo's list
	 * @param listPubKey The public keys' list
	 * @param symKey The symmetric key
	 * @return The JSON object
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String encryptPubKeys(ArrayList<String> names, ArrayList<byte[]> listPubKey, byte[] symKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		byte[] getSeed = {(byte) 0x43, (byte) 0x20};
		byte [] seed, cypherKEY;
		String packet = "";
		// Pour chaque clef publique
		for (int i=0; i<listPubKey.size(); i++) {

			// Demande et réception d'un seed
			try {
				this.sendRequest(getSeed);
			} catch (IOException e) {
				System.err.println("An error occured while sending bytes");
			}
			seed = this.receiveMessage();

			// Chiffrement de la clef symetrique
			IAsymCypher ias = new AsymCypher("RSA", "", "", listPubKey.get(i), seed);
			cypherKEY = ias.encrypt(symKey);
			
			// Concaténation du nom et du chiffré dans la string
			packet += names.get(i);
			packet += "-";
			packet += Base64.encodeBase64String(cypherKEY);
			packet += ",";
		}
		return packet;
	}
	
	
	/**
	 * This method extracts data from a tab before of after
	 * the SplitByte parameter. We specify this last information
	 * with the "direction" parameter. 
	 * It returns then the data
	 * 
	 * @param tab The byte array which have to be split
	 * @param splitByte The byte which specifies the separation
	 * @param direction The "before" or "after".
	 */
	private byte[] splitTab(byte[] tab, byte splitByte, String direction) {
		int i;
		byte[] tab2;
		// Recherche du byte de split
		for (i=0; i<tab.length; i++) {
			if (tab[i] == splitByte) {
				break;
			}
		}
		// Récupération de la partie précédent le splitbyte
		if (direction.equals("before")) {
			tab2 = new byte[i];
			System.arraycopy(tab, 0, tab2, 0, i);
		}
		// Récupération de la partie suivant le splitbyte
		else {
			tab2 = new byte[tab.length - (i+1)];
			System.arraycopy(tab, i+1, tab2, 0, tab.length - (i+1));			
		}
		return tab2;
	}
	
	
	/**
	 * This method verifies if the bytes array
	 * is "empty". In this case, empty means
	 * that the bytes array contains nothing 
	 * after the "split byte" which is 0x20.
	 * 
	 * @param id The bytes array
	 * @return True if the byte array is "empty" or false. 
	 */
	private boolean isEmpty(byte[] id) {
		byte splitByte = (byte) 0x20;
		// Récupération de la partie suivant le splitbyte
		byte [] bla = splitTab(id, splitByte, "after");
		// Test si le contenu est vide
		if (bla.length == 0) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * This method converts a bytes array into
	 * a string under its hexadecimal format.
	 * 
	 * @param bytes The byte array.
	 * @return The string decoded.
	 */
	public String byteToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return new String(sb);
	}
	
	
	/**
	 * This method requests the login and
	 * password's user to log on the social
	 * network.
	 * 
	 * @return A JSON Object which contains
	 * the login and the password 
	 */
	public JSONObject requestID() {
		JSONObject obj = new JSONObject();
		// Code pour demande d'id (carte)
		byte[] getID = {(byte) 0x47};
		byte[] login = null, password = null, id = null;
		byte splitByte = (byte) 0x20;
		
		// Envoi de la demande + reception
		try {
			this.sendRequest(getID);
		} catch (IOException e) {
			System.err.println("An error occured while sending bytes");
		}
		id = this.receiveMessage();
		
		// Splittage des données reçues
		login = this.splitTab(id, splitByte, "before");
		password = this.splitTab(id, splitByte, "after");
		// Création de l'objet JSON
		try {
			obj.put("action", "getID");
			obj.put("firstConnection", isEmpty(id));
			obj.put("login", new String(login));
			obj.put("pass", new String(password));
		} catch (JSONException e) {
			System.err.println("An error occured while creating JSON object (id)");
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * This method requests the login and
	 * password's user to log on the social
	 * network.
	 * 
	 * @param login The login's user.
	 * @param pass The password's user.
	 * @return A JSON Object which contains
	 * the login and the password.
	 */
	public JSONObject requestSetID(String login, String pass) {
		JSONObject obj = new JSONObject();
		// Code pour modif d'id (carte)
		byte[] setID = {(byte) 0x48};
		byte[] split = {(byte) 0x20};
		byte[] passwordByte = pass.getBytes();
		byte[] loginByte = login.getBytes();
		byte[] packet = null, id = null;
		
		// Création d'un bytes array pour envoyer à la carte
		packet = new byte [passwordByte.length + split.length + loginByte.length];
		// Insertion login
		System.arraycopy(loginByte, 0, packet,0, loginByte.length);
		// Insertion splitByte
		System.arraycopy(split, 0, packet, (loginByte.length), split.length);
		// Insertion password
		System.arraycopy(passwordByte, 0, packet, (loginByte.length + split.length), passwordByte.length);
		
		// Envoi de la demande + reception
		this.sendRequest(setID, packet);
		id = this.receiveMessage();
		
		// Creation de l'objet JSON
		try {
			obj.put("action", "setID");
			if (id[0] == 1) {
				obj.put("operation", "true");
			}
			else {
				obj.put("operation", "false");
			}
		} catch (JSONException e) {
			System.err.println("An error occured while creating JSON object (id)");
			e.printStackTrace();
		}
		return  obj;
	}
	
	
	/**
	 * This method requests the public key of
	 * the user.
	 * 
	 * @return A JSON Object which contains
	 * the public key.
	 */
	public JSONObject requestPubKey() {
		JSONObject obj = new JSONObject();
		// Code pour demande de clef publique (carte)
		byte[] getPubKey = {(byte) 0x42};
		byte[] pubKey = null;
		
		// Envoi de la demande + reception
		try {	
			this.sendRequest(getPubKey);
		} catch (IOException e) {
			System.err.println("An error occured while sending bytes");
		}
		pubKey = this.receiveMessage();
		
		// Création de l'objet JSON
		try {
			obj.put("action", "publicKey");
			obj.put("value", Base64.encodeBase64String(pubKey));
		} catch (JSONException e) {
			System.err.println("An error occured while creating JSON object (id)");
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * In this method, we ask softCard to generate
	 * a new password.
	 * 
	 * @return A JSON Object which contains the new
	 * and old password.
	 */
	public JSONObject requestModifPass() {
		JSONObject obj = new JSONObject();
		// Code pour demande de clef publique (carte)
		byte[] getNewPass = {(byte) 0x49};
		byte[] rep = null, oldPass = null, newPass = null;
		byte splitByte = 0x20;
		
		// Envoi de la demande + reception
		try {	
			this.sendRequest(getNewPass);
		} catch (IOException e) {
			System.err.println("An error occured while sending bytes");
		}
		rep = this.receiveMessage();
		
		// Splittage de la réponse
		oldPass = splitTab(rep, splitByte, "before");
		newPass = splitTab(rep, splitByte, "after");
		
		// Creation de l'objet JSON
		try {
			obj.put("action", "modifPass");
			obj.put("oldPass", new String(oldPass));
			obj.put("newPass", new String(newPass));
		} catch (JSONException e) {
			System.err.println("An error occured while creating JSON object (pass)");
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * In this method, we warn ask softCard togenerate
	 * a new password.
	 * 
	 * @return A JSON Object which contains the new
	 * and old password.
	 */
	public JSONObject requestValidPass() {
		JSONObject obj = new JSONObject();
		// Code pour demande de validation du passwd soumis (carte)
		byte[] validPass = {(byte) 0x50};
		byte[] rep = null;
		
		// Envoi de la demande + reception
		try {	
			this.sendRequest(validPass);
		} catch (IOException e) {
			System.err.println("An error occured while sending bytes");
		}
		rep = this.receiveMessage();

		// Creation de l'objet JSON
		try {
			obj.put("action", "validPass");
			if (rep.length == 1 && rep[0] == (byte) 0x01) {
				obj.put("operation", "true");
			}
			else {
				obj.put("operation", "false");	
			}
		} catch (JSONException e) {
			System.err.println("An error occured while creating JSON object (pass)");
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * This method is used when the user want to quit
	 * the extension.It forwards the "quit" demand
	 * to Softcard.
	 */
	public void requestQuit() {
		// Code pour demande de quit (carte)
		byte[] quit = {(byte) 0x41};
		
		// Envoi de la demande
		try {	
			this.sendRequest(quit);
		} catch (IOException e) {
			System.err.println("An error occured while sending bytes");
		}
	}
	
	
	/**
	 * This method encrypts the message with a symmetric 
	 * key and encrypts this key with the list of public
	 * keys. 
	 *	
	 * @return A JSON Object which contains the
	 * cipher text
	 */
	public JSONObject requestEncrypt(ArrayList<String> names, ArrayList<byte[]> pubKeys, String message, boolean bool) {
		JSONObject obj = new JSONObject();
		// Code pour demande d'alea (carte)
		byte[] getSymKey = {(byte) 0x43, (byte) 0x10};
		byte[] symKey = null;
		byte[] symCipher = null;
		ISymCypher is;
		String base64Cipher = "", base64Keys = "", packet = "";
		
		// Envoi de la demande + reception
		try {
			this.sendRequest(getSymKey);
		} catch (IOException e) {
			System.err.println("An error occured while requesting symmetric key");
		}
		symKey = this.receiveMessage();

		// Debut chiffrement partie donnée
		// Creation d'un objet pour chiffrer symetriquement
		try {
			is = new SymCypher("AES", "CBC", "PKCS5Padding", symKey, Base64.decodeBase64("T3qTt46SDstFQvGV5l5ltA=="));
			symCipher = is.encrypt(message.getBytes());
		} catch (NoSuchAlgorithmException e) {
			System.err.println("No such algorithm exists");
		} catch (NoSuchPaddingException e) {
			System.err.println("No such Padding exists");
		} catch (InvalidKeyException e) {
			System.err.println("Key error");
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("Invalid algorithm");
		} catch (IllegalBlockSizeException e) {
			System.err.println("Invalid block size");
		} catch (BadPaddingException e) {
			System.err.println("Invalid padding");
		}
		base64Cipher = Base64.encodeBase64String(symCipher);
		// Partie chiffrement donnée finie
		
		// Debut chiffrement partie clefs
		try {
			// Gestion chiffrement anonyme ou non
			if (bool) {
				base64Keys = this.encryptPubKeysAnon(pubKeys, symKey);
			}
			else {
				base64Keys = this.encryptPubKeys(names, pubKeys, symKey);
			}
		} catch (NoSuchAlgorithmException e) {
			System.err.println("No such algorithm exists");
		} catch (NoSuchPaddingException e) {
			System.err.println("No such Padding exists");
		} catch (InvalidKeyException e) {
			System.err.println("Key error");
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("Invalid algorithm");
		} catch (IllegalBlockSizeException e) {
			System.err.println("Invalid block size");
		} catch (BadPaddingException e) {
			System.err.println("Invalid padding");
		} catch (InvalidKeySpecException e) {
			System.err.println("Invalid key spec");
		}
		// Fin chiffrement partie clefs
		
		// Concaténation des deux parties
		packet += base64Keys;
		packet += base64Cipher;
		
		// Création de l'objet JSON final
		try {
			obj.put("action", "encrypt");
			obj.put("ano", bool);
			obj.put("post", packet);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	
	/**
	 * This method encrypts the message with a symmetric 
	 * key and encrypts this key with the list of public
	 * keys. 
	 *	
	 * @return A JSON Object which contains the
	 * cipher text
	 */
	public JSONObject requestEncryptCom(String id, String message, CacheManager cache) {
		JSONObject obj = new JSONObject();
		// Code pour demande d'alea (carte)
		byte[] comCipher = null;
		ISymCypher is;
		String commentaire = "";
		byte[] key = null;
		
		// On chercher d'abord si la clef est présente en cache
		key = cache.getPayload(id);
		
		
		// Debut chiffrement partie donnée
		// Creation d'un objet pour chiffrer symetriquement
		if (key != null) {
			try {
				is = new SymCypher("AES", "CBC", "PKCS5Padding", key, Base64.decodeBase64("T3qTt46SDstFQvGV5l5ltA=="));
				comCipher = is.encrypt(message.getBytes());
				commentaire = Base64.encodeBase64String(comCipher);
			} catch (NoSuchAlgorithmException e) {
				System.err.println("No such algorithm exists");
			} catch (NoSuchPaddingException e) {
				System.err.println("No such Padding exists");
			} catch (InvalidKeyException e) {
				System.err.println("Key error");
			} catch (InvalidAlgorithmParameterException e) {
				System.err.println("Invalid algorithm");
			} catch (IllegalBlockSizeException e) {
				System.err.println("Invalid block size");
			} catch (BadPaddingException e) {
				System.err.println("Invalid padding");
			}
		}
		else {
			commentaire = "Impossible de chiffrer un commmentaire d'un statut qui ne vous concerne pas";
		}
		// Partie chiffrement donnée finie
		
		// Création de l'objet JSON final
		try {
			obj.put("action", "encryptCom");
			obj.put("encryptedCom", commentaire);
			obj.put("id", id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * This method encrypts the message with a symmetric 
	 * key and encrypts this key with the list of public
	 * keys. 
	 *	
	 * @return A JSON Object which contains the
	 * cipher text
	 */
	public JSONObject requestDecrypt(ArrayList<String> list, String id, JSONArray comms, CacheManager cache) {
		byte[] key = null, cipher = null, message = null, comment = null, commentBase64 = null;
		JSONObject obj = new JSONObject();
		byte[] decrypt = {(byte) 0x44};
		boolean bool = false;
		ISymCypher is = null;
		
		// On chercher d'abord si la clef est présente en cache
		key = cache.getPayload(id);
		
		// Si elle est présente on change le boolean
		if (cache.getPayload(id) != null) {
			bool = true;
		}
		
		// Si la clef n'est pas présente
		if (!bool) {
			// On test un déchiffrement pour chaque clef chiffrée
			int i = 0;
			boolean notFound = true;
			while (i<(list.size()-1) && notFound) {
				this.sendRequest(decrypt, Base64.decodeBase64(list.get(i)));
				key = this.receiveMessage();
				if (key.length != 1) {
					notFound = false;
				}
				i++;
			}

			// Si tous les tests ont échoués
			if (notFound) {
				try {
					obj.put("action", "decrypt");
					obj.put("message", "Ce message ne vous concerne pas");
				} catch (JSONException e) {
					System.err.println("Error while inserting data in JSONObject (decrypt)");
					e.printStackTrace();
				}
				return obj;
			}

			// Sinon on lance la procédure du déchiffrement du message
			// avec la clef de message obtenue
			cipher = Base64.decodeBase64(list.get(list.size()-1));
		}
		
		try {
			is = new SymCypher("AES", "CBC", "PKCS5Padding", key, Base64.decodeBase64("T3qTt46SDstFQvGV5l5ltA=="));
			message = is.decrypt(cipher);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("No such algorithm exists");
		} catch (NoSuchPaddingException e) {
			System.err.println("No such Padding exists");
		} catch (InvalidKeyException e) {
			System.err.println("Key error");
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("Invalid algorithm");
		} catch (IllegalBlockSizeException e) {
			System.err.println("Invalid block size");
			try {
				obj.put("action", "decrypt");
				obj.put("message", "Ce message ne vous concerne pas");
			} catch (JSONException e1) {
				System.err.println("Error while inserting data in JSONObject (decrypt)");
			}
			return obj;
		} catch (BadPaddingException e) {
			System.err.println("Invalid padding");
		}

		ArrayList<String> commentsList = new ArrayList<String>();
		// Déchiffrement des commentaires
		if (comms.length() != 0) {
			int i=0;
			while (i<comms.length()) {
				try {
					commentBase64 = Base64.decodeBase64(comms.getString(i));
					comment = is.decrypt(commentBase64);
				} catch (JSONException e) {
					System.err.println("Error while extracting comment (decrypt)");
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					System.err.println("No such algorithm exists");
				} catch (NoSuchPaddingException e) {
					System.err.println("No such Padding exists");
				} catch (InvalidKeyException e) {
					System.err.println("Key error");
				} catch (InvalidAlgorithmParameterException e) {
					System.err.println("Invalid algorithm");
				} catch (IllegalBlockSizeException e) {
					System.err.println("Invalid block size");
					try {
						comment = comms.getString(i).getBytes();
					} catch (JSONException e1) {
						System.err.println("Can't retrieve JSON data (decrypt)");
					}
				} catch (BadPaddingException e) {
					System.err.println("Invalid padding");
					try {
						comment = comms.getString(i).getBytes();
					} catch (JSONException e1) {
						System.err.println("Can't retrieve JSON data (decrypt)");
					}
				}
				commentsList.add(new String(comment));
				i++;
			}
		}
		
		// Stockage de la clef de message
		// Si elle n'est pas contenue dans le cache
		if (!bool) {
			cache.addPayload(id, key);
		}
		
		// Création de l'objet JSON final
		try {
			obj.put("action", "decrypt");
			obj.put("message", new String(message));
			obj.put("comms", commentsList);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * This method close the printer and reader,
	 * close the socket and stop the program. 
	 * @throws IOException
	 */
	public void free() throws IOException {
		// Fermeture du buffer d'entrer
		in.close();
		// Fermeture du buffer de sortie
		out.close();
		// Fermeture de la socket (carte - facecrypt)
		socket.close();
	}

}