package com.srain.binpack;

/**
 * An util tool for data conversion.
 *
 * @author http://www.liaohuqiu.net
 */
public class BinPackType {

	static Boolean toBoolean(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return null;
	}

	static Double toDouble(Object value) {
		if (value instanceof Double) {
			return (Double) value;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else if (value instanceof String) {
			try {
				return Double.valueOf((String) value);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	static Byte toByte(Object value) {
		if (value instanceof Byte) {
			return (Byte) value;
		} else if (value instanceof Number) {
			return ((Number) value).byteValue();
		} else if (value instanceof String) {
			try {
				return (byte) Double.parseDouble((String) value);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	static Short toShort(Object value) {
		if (value instanceof Short) {
			return (Short) value;
		} else if (value instanceof Number) {
			return ((Number) value).shortValue();
		} else if (value instanceof String) {
			try {
				return (short) Double.parseDouble((String) value);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	static Integer toInteger(Object value) {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof String) {
			try {
				return (int) Double.parseDouble((String) value);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	static Long toLong(Object value) {
		if (value instanceof Long) {
			return (Long) value;
		} else if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value instanceof String) {
			try {
				return (long) Double.parseDouble((String) value);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	static String toString(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value != null) {
			return String.valueOf(value);
		}
		return null;
	}

	public static BinPackException typeMismatch(Object indexOrName, Object actual, String requiredType) throws BinPackException {
		if (actual == null) {
			throw new BinPackException("Value at " + indexOrName + " is null.");
		} else {
			throw new BinPackException("Value " + actual + " at " + indexOrName
					+ " of type " + actual.getClass().getName()
					+ " cannot be converted to " + requiredType);
		}
	}

	public static BinPackException typeMismatch(Object actual, String requiredType) throws BinPackException {
		if (actual == null) {
			throw new BinPackException("Value is null.");
		} else {
			throw new BinPackException("Value " + actual + " of type " + actual.getClass().getName() + " cannot be converted to " + requiredType);
		}
	}
}

