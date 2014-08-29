package in.srain.binpack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * The core algorithm for binpack implementation.
 *
 * @author http://www.liaohuqiu.net
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BinPack {

    public static final byte BIN_TAG_SHUT = 0x01; // 0000 0001
    public static final byte BIN_TYPE_LIST = 0x02; // 0000 0010
    public static final byte BIN_TYPE_DICT = 0x03; // 0000 0011

    public static final byte BIN_TYPE_BOOL = 0x04; // 0000 0100
    public static final byte BIN_TYPE_BOOL_FALSE = 0x05; // 0000 0101

    public static final byte BIN_TYPE_REAL_DOUBLE = 0x06; // 0000 0110
    public static final byte BIN_TYPE_REAL_FLOAT = 0x07; // 0000 0111

    public static final byte BIN_TYPE_NULL = 0x0f; // 0000 1111

    public static final byte BIN_TYPE_BLOB = 0x10; // 0001 0000
    public static final byte BIN_TYPE_STRING = 0x20; // 0010 0000

    public static final byte BIN_TAG_PACK_NUM = 0x0f; // 0001 xxxx

    public static final byte BIN_TAG_PACK_INTEGER = 0x07; // 0000 0xxx

    public static final byte BIN_TYPE_INTEGER = 0x40; // 0100 0000
    public static final byte BIN_TYPE_INTEGER_NEGATIVE_MASK = 0x20; // 0010 0000

    public static final byte BIN_INTEGER_TYPE_Byte = 0x01 << 3; // xxx0 1xxx
    public static final byte BIN_INTEGER_TYPE_Short = 0x02 << 3; // xxx1 0xxx
    public static final byte BIN_INTEGER_TYPE_Int = 0x03 << 3; // xxx1 1xxx
    public static final byte BIN_INTEGER_TYPE_Long = 0x00 << 3; // xxx0 0xxx
    public static final byte BIN_INTEGER_TYPE_MASK = 0x18; // 0001 1000

    public static final byte BIN_NUM_SIGN_BIT = (byte) 0x80; // 1000 0000
    public static final byte BIN_NUM_MASK = (byte) 0x7f; // 1000 0000

    public static final Object SHUT_OBJECT = new Object();

    /**
     * Encode data into byte array
     *
     * @param obj
     * @param charsetName
     * @return byte[]
     */
    public static byte[] encode(Object obj, String charsetName) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pack(out, obj, charsetName);
            return out.toByteArray();
        } catch (java.io.IOException ex) {
            byte[] bs = {};
            return bs;
        }
    }

    public static void pack(OutputStream out, Object obj, String charsetName) throws java.io.IOException {
        if (obj == null) {
            packNull(out);
        } else if (obj instanceof Integer || obj instanceof Long || obj instanceof Short || obj instanceof Byte) {
            packInteger(out, (Number) obj);
        } else if (obj instanceof String) {
            packString(out, obj.toString(), charsetName);
        } else if (obj instanceof Boolean) {
            packBool(out, ((Boolean) obj).booleanValue());
        } else if (obj instanceof byte[]) {
            packBlob(out, (byte[]) obj);
        } else if (obj instanceof Double) {
            packDouble(out, ((Double) obj));
        } else if (obj instanceof Float) {
            packFloat(out, ((Float) obj));
        } else if (obj instanceof Collection) {
            packList(out, (Collection) obj, charsetName);
        } else if (obj.getClass().isArray()) {
            // packList(out, Arrays.asList(obj), charsetName);
        } else if (obj instanceof Map) {
            packMap(out, (Map) obj, charsetName);
        } else if (obj instanceof Enum) {
            packInteger(out, ((Enum) obj).ordinal());
        } else {
            packString(out, "unsupported-type-" + obj.getClass().getName(), "UTF-8");
        }
    }

    /**
     * Decode from byte array
     *
     * @param bs
     * @param charsetName
     * @return
     */
    public static Object decode(byte[] bs, String charsetName) {
        DecodeCtx ctx = new DecodeCtx();
        ctx.buf = bs;
        ctx.pos = 0;
        ctx.charsetName = charsetName;
        Object obj = doDecode(ctx);
        if (obj == SHUT_OBJECT) {
            return null;
        }
        return obj;
    }

    private static int _unpackTag(DecodeCtx ctx, TagInfo info) {
        if (ctx.pos >= ctx.buf.length) {
            return -2;
        }
        long x = (long) ctx.buf[ctx.pos++];

        int shift = 0;
        long num = 0;
        if (x < 0) {
            while (x < 0) {
                x &= 0x7f;
                num |= x << shift;
                x = (long) ctx.buf[ctx.pos++];
                shift += 7;
            }
        }

        // not length information
        byte type = (byte) x;
        if (type < 0x10) {

            info.type = type;
        } else {

            if (type < BIN_TYPE_INTEGER) {
                info.type = (byte) (type & 0x70);
                num |= (x & 0x0f) << shift;
            } else {
                info.type = type;
                num |= (x & 0x07) << shift;
            }

            info.num = num;
        }

        return 0;
    }

    private static Object doDecode(DecodeCtx ctx) {
        TagInfo info = new TagInfo();
        if (_unpackTag(ctx, info) < 0) {
            return null;
        }

        if (info.type >= BIN_TYPE_INTEGER) {
            return makeInteger(info);
        }

        switch (info.type) {

            case BIN_TAG_SHUT:
                return SHUT_OBJECT;

            case BIN_TYPE_LIST: {
                ArrayList list = new ArrayList();
                if (makeList(ctx, list) < 0) {
                    return null;
                }
                return list;
            }

            case BIN_TYPE_DICT: {
                Map map = new HashMap();
                if (makeDict(ctx, map) < 0) {
                    return null;
                }
                return map;
            }

            case BIN_TYPE_BOOL:
                return true;
            case BIN_TYPE_BOOL_FALSE:
                return false;

            case BIN_TYPE_NULL:
                return null;

            case BIN_TYPE_BLOB: {
                if (info.num > ctx.buf.length - ctx.pos) {
                    return null;
                }
                int start = ctx.pos;
                ctx.pos += info.num;
                return Arrays.copyOfRange(ctx.buf, start, ctx.pos);
            }

            case BIN_TYPE_STRING: {
                int start = ctx.pos;
                ctx.pos += info.num;
                try {
                    return new String(ctx.buf, start, (int) info.num, ctx.charsetName);
                } catch (Exception ex) {
                    return null;
                }
            }
            case BIN_TYPE_REAL_DOUBLE: {
                return makeDouble(ctx);
            }
            case BIN_TYPE_REAL_FLOAT: {
                return makeFloat(ctx);
            }
        }

        return null;
    }

    private static int makeList(DecodeCtx ctx, List list) {
        while (true) {
            Object value = doDecode(ctx);
            if (value == SHUT_OBJECT) {
                return 0;
            }
            list.add(value);
        }
    }

    private static int makeDict(DecodeCtx ctx, Map map) {
        while (true) {
            Object key = doDecode(ctx);
            if (key == SHUT_OBJECT) {
                return 0;
            }

            if (key == null) {
                return -1;
            }

            Object value = doDecode(ctx);
            if (value == SHUT_OBJECT) {
                return -1;
            }

            map.put(key, value);
        }
    }

    private static Object makeInteger(TagInfo info) {
        int type = info.type & BIN_INTEGER_TYPE_MASK;
        if ((info.type & BIN_TYPE_INTEGER_NEGATIVE_MASK) != 0) {
            info.num = -info.num;
        }
        switch (type) {
            case BIN_INTEGER_TYPE_Byte:
                return (byte) info.num;
            case BIN_INTEGER_TYPE_Short:
                return (short) info.num;
            case BIN_INTEGER_TYPE_Int:
                return (int) info.num;
            case BIN_INTEGER_TYPE_Long:
                return info.num;
            default:
                break;
        }
        return null;
    }

    private static Double makeDouble(DecodeCtx ctx) {

        long x = 0;
        byte shift = 0;
        long n;
        while (shift < 64) {
            n = (long) ctx.buf[ctx.pos++];
            x |= (n & 0xff) << shift;
            shift += 8;
        }
        return Double.longBitsToDouble(x);
    }

    private static Float makeFloat(DecodeCtx ctx) {

        int x = 0;
        byte shift = 0;
        int n;
        while (shift < 32) {
            n = (int) ctx.buf[ctx.pos++];
            x |= (n & 0xff) << shift;
            shift += 8;
        }
        return Float.intBitsToFloat(x);
    }

    private static void packDouble(OutputStream out, Double d) throws IOException {
        long x = Double.doubleToLongBits(d);
        out.write(BIN_TYPE_REAL_DOUBLE);

        byte shift = 64;

        while (shift > 0) {
            out.write((byte) (x & 0xff));
            x = x >> 8;
            shift -= 8;
        }
    }

    private static void packFloat(OutputStream out, Float f) throws IOException {
        int x = Float.floatToIntBits(f);
        out.write(BIN_TYPE_REAL_FLOAT);

        byte shift = 32;
        while (shift > 0) {
            out.write((byte) (x & 0xff));
            x = x >> 8;
            shift -= 8;
        }
    }

    private static void packList(OutputStream out, Collection list, String charsetName) throws IOException {
        out.write(BIN_TYPE_LIST);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            pack(out, obj, charsetName);
        }
        out.write(BIN_TAG_SHUT);
    }

    private static void packMap(OutputStream out, Map map, String charsetName) throws IOException {
        out.write(BIN_TYPE_DICT);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            pack(out, entry.getKey(), charsetName);
            pack(out, entry.getValue(), charsetName);
        }
        out.write(BIN_TAG_SHUT);
    }

    private static void packString(OutputStream out, String s, String charsetName) throws IOException {
        byte[] bs = s.getBytes(charsetName);
        packNum(out, bs.length, BIN_TYPE_STRING);
        out.write(bs);
    }

    private static void packNum(OutputStream out, int len, byte type) throws IOException {
        // last number byte: 0000 xxxx
        while (len > BIN_TAG_PACK_NUM) {
            out.write((byte) (BIN_NUM_SIGN_BIT | (len & BIN_NUM_MASK)));
            len = len >>> 7;
        }
        out.write(type | len);
    }

    private static void packBlob(OutputStream out, byte[] bs) throws IOException {
        packNum(out, bs.length, BIN_TYPE_BLOB);
        out.write(bs);
    }

    private static void packNull(OutputStream out) throws IOException {
        out.write(BIN_TYPE_NULL);
    }

    public static void packBool(OutputStream out, boolean v) throws IOException {
        out.write((v ? BIN_TYPE_BOOL : BIN_TYPE_BOOL_FALSE));
    }

    private static void packInteger(OutputStream out, Number n) throws IOException {
        byte tag = BIN_TYPE_INTEGER;
        if (n instanceof Byte) {
            tag |= BIN_INTEGER_TYPE_Byte;
        } else if (n instanceof Short) {
            tag |= BIN_INTEGER_TYPE_Short;
        } else if (n instanceof Integer) {
            tag |= BIN_INTEGER_TYPE_Int;
        } else if (n instanceof Long) {
            tag |= BIN_INTEGER_TYPE_Long;
        }
        long l = n.longValue();
        if (l < 0) {
            l = -l;
            tag |= BIN_TYPE_INTEGER_NEGATIVE_MASK;
        }

        // last number byte: 0000 0xxx
        while (l > BIN_TAG_PACK_INTEGER || l >>> 3 > 0) {
            out.write((byte) (BIN_NUM_SIGN_BIT | (l & BIN_NUM_MASK)));
            l = l >>> 7;
        }
        out.write((byte) (tag | l));
    }

    private static class DecodeCtx {
        byte[] buf;
        int pos;
        String charsetName;
    }

    private static class TagInfo {
        byte type;
        long num;
    }
}
