package in.srain.binpack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A Collection wrapper for data access.
 *
 * @author http://www.liaohuqiu.net
 */
public class BinList {

    private final List<Object> values;

    private BinList() {
        values = new ArrayList<Object>();
    }

    public BinList(Collection copyFrom) {
        this();
        if (copyFrom != null) {
            for (Iterator it = copyFrom.iterator(); it.hasNext(); ) {
                values.add(it.next());
            }
        }
    }

    public BinList(byte[] bs, String charsetName) throws BinPackException {
        Object object = BinPack.decode(bs, charsetName);
        if (object instanceof BinList) {
            values = ((BinList) object).values;
        } else {
            throw BinPackType.typeMismatch(object, "BinList");
        }
    }

    /**
     * Returns the number of values in this array.
     */
    public int length() {
        return values.size();
    }

    /**
     * Returns true if this array has no value at {@code index}, or if its value
     * is the {@code null} reference or {@link BinDict#NULL}.
     */
    public boolean isNull(int index) {
        Object value = opt(index);
        return value == null || value == BinDict.NULL;
    }

    /**
     * Returns the value at {@code index}.
     *
     * @throws BinPackException if this array has no value at {@code index}, or if
     *                          that value is the {@code null} reference. This method returns
     *                          normally if the value is {@code JSONObject#NULL}.
     */
    public Object get(int index) throws BinPackException {
        try {
            Object value = values.get(index);
            if (value == null) {
                throw new BinPackException("Value at " + index + " is null.");
            }
            return value;
        } catch (IndexOutOfBoundsException e) {
            throw new BinPackException("Index " + index + " out of range [0.." + values.size() + ")");
        }
    }

    /**
     * Returns the value at {@code index}, or null if the array has no value
     * at {@code index}.
     */
    public Object opt(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean.
     *
     * @throws BinPackException if the value at {@code index} doesn't exist or
     *                          cannot be coerced to a boolean.
     */
    public boolean getBoolean(int index) throws BinPackException {
        Object object = get(index);
        Boolean result = BinPackType.toBoolean(object);
        if (result == null) {
            throw BinPackType.typeMismatch(index, object, "boolean");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean. Returns false otherwise.
     */
    public boolean optBoolean(int index) {
        return optBoolean(index, false);
    }

    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean. Returns {@code fallback} otherwise.
     */
    public boolean optBoolean(int index, boolean fallback) {
        Object object = opt(index);
        Boolean result = BinPackType.toBoolean(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double.
     *
     * @throws BinPackException if the value at {@code index} doesn't exist or
     *                          cannot be coerced to a double.
     */
    public double getDouble(int index) throws BinPackException {
        Object object = get(index);
        Double result = BinPackType.toDouble(object);
        if (result == null) {
            throw BinPackType.typeMismatch(index, object, "double");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double. Returns {@code NaN} otherwise.
     */
    public double optDouble(int index) {
        return optDouble(index, Double.NaN);
    }

    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double. Returns {@code fallback} otherwise.
     */
    public double optDouble(int index, double fallback) {
        Object object = opt(index);
        Double result = BinPackType.toDouble(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a byte or
     * can be coerced to an byte.
     *
     * @throws BinPackException if the value at {@code index} doesn't exist or
     *                          cannot be coerced to a byte.
     */
    public byte getByte(int index) throws BinPackException {
        Object object = get(index);
        Byte result = BinPackType.toByte(object);
        if (result == null) {
            throw BinPackType.typeMismatch(index, object, "byte");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a byte or
     * can be coerced to a byte. Returns 0 otherwise.
     */
    public byte optByte(int index) {
        return optByte(index, (byte) 0);
    }

    /**
     * Returns the value at {@code index} if it exists and is a byte or
     * can be coerced to a byte. Returns {@code fallback} otherwise.
     */
    public byte optByte(int index, byte fallback) {
        Object object = opt(index);
        Byte result = BinPackType.toByte(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a short or
     * can be coerced to an byte.
     *
     * @throws BinPackException if the value at {@code index} doesn't exist or
     *                          cannot be coerced to a short.
     */
    public short getShort(int index) throws BinPackException {
        Object object = get(index);
        Short result = BinPackType.toShort(object);
        if (result == null) {
            throw BinPackType.typeMismatch(index, object, "short");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a short or
     * can be coerced to a short. Returns 0 otherwise.
     */
    public short optShort(int index) {
        return optShort(index, (short) 0);
    }

    /**
     * Returns the value at {@code index} if it exists and is a short or
     * can be coerced to a short. Returns {@code fallback} otherwise.
     */
    public short optShort(int index, short fallback) {
        Object object = opt(index);
        Short result = BinPackType.toShort(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int.
     *
     * @throws BinPackException if the value at {@code index} doesn't exist or
     *                          cannot be coerced to an int.
     */
    public int getInt(int index) throws BinPackException {
        Object object = get(index);
        Integer result = BinPackType.toInteger(object);
        if (result == null) {
            throw BinPackType.typeMismatch(index, object, "int");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int. Returns 0 otherwise.
     */
    public int optInt(int index) {
        return optInt(index, 0);
    }

    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int. Returns {@code fallback} otherwise.
     */
    public int optInt(int index, int fallback) {
        Object object = opt(index);
        Integer result = BinPackType.toInteger(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long.
     *
     * @throws BinPackException if the value at {@code index} doesn't exist or
     *                          cannot be coerced to a long.
     */
    public long getLong(int index) throws BinPackException {
        Object object = get(index);
        Long result = BinPackType.toLong(object);
        if (result == null) {
            throw BinPackType.typeMismatch(index, object, "long");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long. Returns 0 otherwise.
     */
    public long optLong(int index) {
        return optLong(index, 0L);
    }

    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long. Returns {@code fallback} otherwise.
     */
    public long optLong(int index, long fallback) {
        Object object = opt(index);
        Long result = BinPackType.toLong(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary.
     *
     * @throws BinPackException if no such value exists.
     */
    public String getString(int index) throws BinPackException {
        Object object = get(index);
        String result = BinPackType.toString(object);
        if (result == null) {
            throw BinPackType.typeMismatch(index, object, "String");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary. Returns the empty string if no such value exists.
     */
    public String optString(int index) {
        return optString(index, "");
    }

    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary. Returns {@code fallback} if no such value exists.
     */
    public String optString(int index, String fallback) {
        Object object = opt(index);
        String result = BinPackType.toString(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * BinList}.
     *
     * @throws BinPackException if the value doesn't exist or is not a {@code
     *                          BinList}.
     */
    public BinList getList(int index) throws BinPackException {
        Object object = get(index);
        if (object instanceof BinList) {
            return (BinList) object;
        } else {
            throw BinPackType.typeMismatch(index, object, "BinList");
        }
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * BinList}. Returns null otherwise.
     */
    public BinList optList(int index) {
        Object object = opt(index);
        return object instanceof BinList ? (BinList) object : null;
    }

    public BinDict getDict(int index) throws BinPackException {
        Object object = get(index);
        if (object instanceof Map<?, ?>) {
            return new BinDict((Map) object);
        } else {
            throw BinPackType.typeMismatch(index, object, "BinDict");
        }
    }

    public BinDict optDict(int index) {
        Object object = opt(index);
        return object instanceof BinDict ? (BinDict) object : null;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BinList && ((BinList) o).values.equals(values);
    }

    @Override
    public int hashCode() {
        // diverge from the original, which doesn't implement hashCode
        return values.hashCode();
    }
}
