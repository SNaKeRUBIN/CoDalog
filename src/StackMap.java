package src;

import java.util.HashMap;
import java.util.Map;

public class StackMap<K,V> {
		private Map<K,V> self;
		private StackMap<K,V> parent;

		public StackMap() {
			self = new HashMap<K, V>();
			this.parent = null;
		}

		public StackMap(StackMap<K,V> parent) {
			self = new HashMap<K, V>();
			this.parent = parent;
		}

		public StackMap(Map<K,V> map) {
			self = new HashMap<K, V>(map);
			this.parent = null;
		}

		Map<K,V> flatten() {
			Map<K,V> map;
			if(parent != null)
				map = parent.flatten();
			else
				map = new HashMap<K, V>();
			map.putAll(self);
			return map;
		}

		public V put(K key, V value) {
			return self.put(key, value);
		}

		public boolean containsKey(K key) {
			if(self.containsKey(key))
				return true;
			if(parent != null)
				return parent.containsKey(key);
			return false;
		}

		public V get(K key) {
			V value = self.get(key);
			if(value != null)
				return value;
			if(parent != null)
				return parent.get(key);
			return null;
		}
		
		public String toString(K key, V value) {
			return key + " : " + value;
		}
	}