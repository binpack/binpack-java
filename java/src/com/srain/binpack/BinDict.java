package com.srain.binpack;

import java.util.*;

/**
 * A map wrapper to access data.
 *
 * @author http://www.liaohuqiu.net
 */
public class BinDict {


	/**
	 * A sentinel value used to explicitly define a name with no value.
	 */
	public static final Object NULL = new Object() {
		@Override
		public boolean equals(Object o) {
			return o == this || o == null; // API specifies this broken equals implementation
		}

		@Override
		public String toString() {
			return "null";
		}
	};

	private final Map<String, Object> nameValuePairs;

	private BinDict() {
		nameValuePairs = new HashMap<String, Object>();
	}

	public BinDict(Map copyFrom) {
		this();
		Map<?, ?> contentsTyped = (Map<?, ?>) copyFrom;
		for (Map.Entry<?, ?> entry : contentsTyped.entrySet()) {
			String key = (String) entry.getKey();
			if (key == null) {
				throw new NullPointerException("key == null");
			}
			nameValuePairs.put(key, entry.getValue());
		}
	}

	public BinDict(byte[] bs, String charsetName) throws BinPackException {
		Object object = BinPack.decode(bs, charsetName);
		if (object instanceof Map) {
			this.nameValuePairs = (Map) object;
		} else {
			throw BinPackType.typeMismatch(object, "BinDict");
		}
	}

	public boolean isNull(String name) {
		Object value = nameValuePairs.get(name);
		return value == null || value == NULL;
	}

	public boolean has(String name) {
		return nameValuePairs.containsKey(name);
	}

	public Object get(String name) throws BinPackException {
		Object result = nameValuePairs.get(name);
		if (result == null) {
			throw new BinPackException("No value for " + name);
		}
		return result;
	}

	public Object opt(String name) {
		return nameValuePairs.get(name);
	}

	public boolean getBoolean(String name) throws BinPackException {
		Object object = get(name);
		Boolean result = BinPackType.toBoolean(object);
		if (result == null) {
			throw BinPackType.typeMismatch(name, object, "boolean");
		}
		return result;
	}

	public boolean optBoolean(String name) {
		return optBoolean(name, false);
	}

	public boolean optBoolean(String name, boolean fallback) {
		Object object = opt(name);
		Boolean result = BinPackType.toBoolean(object);
		return result != null ? result : fallback;
	}


	public double getDouble(String name) throws BinPackException {
		Object object = get(name);
		Double result = BinPackType.toDouble(object);
		if (result == null) {
			throw BinPackType.typeMismatch(name, object, "double");
		}
		return result;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a double or
	 * can be coerced to a double. Returns {@code NaN} otherwise.
	 */
	public double optDouble(String name) {
		return optDouble(name, Double.NaN);
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a double or
	 * can be coerced to a double. Returns {@code fallback} otherwise.
	 */
	public double optDouble(String name, double fallback) {
		Object object = opt(name);
		Double result = BinPackType.toDouble(object);
		return result != null ? result : fallback;
	}


	/**
	 * Returns the value mapped by {@code name} if it exists and is a byte or
	 * can be coerced to a byte.
	 *
	 * @throws BinPackException if the mapping doesn't exist or cannot be coerced
	 *                          to a byte.
	 */
	public byte getByte(String name) throws BinPackException {
		Object object = get(name);
		Byte result = BinPackType.toByte(object);
		if (result == null) {
			throw BinPackType.typeMismatch(name, object, "byte");
		}
		return result;
	}


	/**
	 * Returns the value at {@code index} if it exists and is a byte or
	 * can be coerced to a byte. Returns 0 otherwise.
	 */
	public byte optByte(String index) {
		return optByte(index, (byte) 0);
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a byte or
	 * can be coerced to a byte. Returns {@code fallback} otherwise.
	 */
	public byte optByte(String name, byte fallback) {
		Object object = opt(name);
		Byte result = BinPackType.toByte(object);
		return result != null ? result : fallback;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a short or
	 * can be coerced to a short.
	 *
	 * @throws BinPackException if the mapping doesn't exist or cannot be coerced
	 *                          to a short.
	 */
	public short getShort(String name) throws BinPackException {
		Object object = get(name);
		Short result = BinPackType.toShort(object);
		if (result == null) {
			throw BinPackType.typeMismatch(name, object, "short");
		}
		return result;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a short or
	 * can be coerced to a short. Returns 0 otherwise.
	 */
	public short optShort(String name) {
		return optShort(name, (short) 0);
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a short or
	 * can be coerced to a short Returns {@code fallback} otherwise.
	 */
	public short optShort(String name, short fallback) {
		Object object = opt(name);
		Short result = BinPackType.toShort(object);
		return result != null ? result : fallback;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is an int or
	 * can be coerced to an int.
	 *
	 * @throws BinPackException if the mapping doesn't exist or cannot be coerced
	 *                          to an int.
	 */
	public int getInt(String name) throws BinPackException {
		Object object = get(name);
		Integer result = BinPackType.toInteger(object);
		if (result == null) {
			throw BinPackType.typeMismatch(name, object, "int");
		}
		return result;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is an int or
	 * can be coerced to an int. Returns 0 otherwise.
	 */
	public int optInt(String name) {
		return optInt(name, 0);
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is an int or
	 * can be coerced to an int. Returns {@code fallback} otherwise.
	 */
	public int optInt(String name, int fallback) {
		Object object = opt(name);
		Integer result = BinPackType.toInteger(object);
		return result != null ? result : fallback;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a long or
	 * can be coerced to a long.
	 *
	 * @throws BinPackException if the mapping doesn't exist or cannot be coerced
	 *                          to a long.
	 */
	public long getLong(String name) throws BinPackException {
		Object object = get(name);
		Long result = BinPackType.toLong(object);
		if (result == null) {
			throw BinPackType.typeMismatch(name, object, "long");
		}
		return result;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a long or
	 * can be coerced to a long. Returns 0 otherwise.
	 */
	public long optLong(String name) {
		return optLong(name, 0L);
	}

	/**
	 * Returns the value mapped by {@code name} if it exists and is a long or
	 * can be coerced to a long. Returns {@code fallback} otherwise.
	 */
	public long optLong(String name, long fallback) {
		Object object = opt(name);
		Long result = BinPackType.toLong(object);
		return result != null ? result : fallback;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists, coercing it if
	 * necessary.
	 *
	 * @throws BinPackException if no such mapping exists.
	 */
	public String getString(String name) throws BinPackException {
		Object object = get(name);
		String result = BinPackType.toString(object);
		if (result == null) {
			throw BinPackType.typeMismatch(name, object, "String");
		}
		return result;
	}

	/**
	 * Returns the value mapped by {@code name} if it exists, coercing it if
	 * necessary. Returns the empty string if no such mapping exists.
	 */
	public String optString(String name) {
		return optString(name, "");
	}

	/**
	 * Returns the value mapped by {@code name} if it exists, coercing it if
	 * necessary. Returns {@code fallback} if no such mapping exists.
	 */
	public String optString(String name, String fallback) {
		Object object = opt(name);
		String result = BinPackType.toString(object);
		return result != null ? result : fallback;
	}

	public BinList getList(String name) throws BinPackException {
		Object object = get(name);
		if (object instanceof Collection) {
			return new BinList((Collection) object);
		} else {
			throw BinPackType.typeMismatch(name, object, "BinList");
		}
	}

	public BinList optList(String name) {
		Object object = opt(name);
		return object instanceof Collection ? new BinList((Collection) object) : null;
	}

	public BinDict getDict(String name) throws BinPackException {
		Object object = get(name);
		if (object instanceof Map) {
			return new BinDict((Map) object);
		} else {
			throw BinPackType.typeMismatch(name, object, "BinDict");
		}
	}

	public BinDict optDict(String name) {
		Object object = opt(name);
		return object instanceof Map ? new BinDict((Map) object) : null;
	}

	public Iterator keys() {
		return nameValuePairs.keySet().iterator();
	}

	public int size() {
		return nameValuePairs.size();
	}

	/**
	 * Returns an array containing the string names in this object. This method
	 * returns null if this object contains no mappings.
	 */
	public ArrayList<String> names() {
		return nameValuePairs.isEmpty()
				? null
				: new ArrayList<String>(nameValuePairs.keySet());
	}

	@Override
	public String toString() {
		return nameValuePairs.toString();
	}
}

