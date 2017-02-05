package cache;

import java.util.LinkedList;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

public class CacheManager implements ICacheManager {
	
	private SortedMap<String, byte[]> map;
	private Queue<String> stack;
	
	public CacheManager() {
		this.map = new TreeMap<String,byte[]>();
		this.stack = new LinkedList<String>();
	}
	
	public void addPayload(String request, byte[] payload) {
		if (request == null || payload == null || request.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.map.put(request, payload);
		this.stack.add(request);
		if (map.size() > ICacheManager.MAX_SIZE) {
			map.remove(this.stack.remove());
		}
	}
	
	public byte[] getPayload(String request) {
		if (request == null || request.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return this.map.get(request);
	}
	
}
