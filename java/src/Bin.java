import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Bin {

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

    public static byte[] encode(Object obj, String charsetName) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            do_pack(out, obj, charsetName);
            return out.toByteArray();
        } catch (java.io.IOException ex) {
            byte[] vbs = {};
            return vbs;
        }
    }

    private static void do_pack(OutputStream out, Object obj, String charsetName)
        throws java.io.IOException {
        if (obj == null) {
            pack_null(out);
        } else if (obj instanceof Integer || obj instanceof Long || obj instanceof Short
            || obj instanceof Byte) {
            pack_integer(out, (Number) obj);
        } else if (obj instanceof String) {
            pack_string(out, obj.toString(), charsetName);
        } else if (obj instanceof Boolean) {
            pack_bool(out, ((Boolean) obj).booleanValue());
        } else if (obj instanceof byte[]) {
            pack_blob(out, (byte[]) obj);
        } else if (obj instanceof Double) {
            pack_double(out, ((Double) obj));
        } else if (obj instanceof Float) {
            pack_float(out, ((Float) obj));
        } else if (obj instanceof List) {
            pack_list(out, (List) obj, charsetName);
        } else if (obj instanceof Map) {
            pack_map(out, (Map) obj, charsetName);
        } else {
            pack_string(out, "XXX-not-pack-unsupported-type-" + obj.getClass().getName(), "UTF-8");
        }
    }

    public static Object decode(byte[] bs, String charsetName) {
        DecodeCtx ctx = new DecodeCtx();
        ctx.buf = bs;
        ctx.pos = 0;
        ctx.charsetName = charsetName;
        Object obj = do_decode(ctx);
        if (obj == SHUT_OBJECT) {
            return null;
        }
        return obj;
    }

    private static int _unpack_tag(DecodeCtx ctx, TagInfo info) {
        if (ctx.pos >= ctx.buf.length) {
            return -2;
        }
        long x = (long) ctx.buf[ctx.pos++];

        int shift = 0;
        long num = 0;
        while (x < 0) {
            x &= 0x7f;
            num |= x << shift;
            x = (long) ctx.buf[ctx.pos++];
            shift += 7;
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

    private static Object do_decode(DecodeCtx ctx) {
        TagInfo info = new TagInfo();
        if (_unpack_tag(ctx, info) < 0) {
            return null;
        }

        if (info.type >= BIN_TYPE_INTEGER) {
            return make_integer(info);
        }

        switch (info.type) {

            case BIN_TAG_SHUT:
                return SHUT_OBJECT;

            case BIN_TYPE_LIST: {
                ArrayList list = new ArrayList();
                if (make_list(ctx, list) < 0) {
                    return null;
                }
                return list;
            }

            case BIN_TYPE_DICT: {
                Map map = new HashMap();
                if (make_dict(ctx, map) < 0) {
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
                return make_double(ctx);
            }
            case BIN_TYPE_REAL_FLOAT: {
                return make_float(ctx);
            }
        }

        return null;
    }

    private static int make_list(DecodeCtx ctx, List list) {
        while (true) {
            Object value = do_decode(ctx);
            if (value == SHUT_OBJECT) {
                return 0;
            }
            list.add(value);
        }
    }

    private static int make_dict(DecodeCtx ctx, Map map) {
        while (true) {
            Object key = do_decode(ctx);
            if (key == SHUT_OBJECT) {
                return 0;
            }

            if (key == null) {
                return -1;
            }

            Object value = do_decode(ctx);
            if (value == SHUT_OBJECT) {
                return -1;
            }

            map.put(key, value);
        }
    }

    private static Object make_integer(TagInfo info) {
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

    private static Double make_double(DecodeCtx ctx) {

        long x = 0;
        byte shift = 0;
        long n = 0;
        while (shift < 64) {
            n = (long) ctx.buf[ctx.pos++];
            x |= (n & 0xff) << shift;
            shift += 8;
        }
        return Double.longBitsToDouble(x);
    }

    private static Float make_float(DecodeCtx ctx) {

        int x = 0;
        byte shift = 0;
        int n = 0;
        while (shift < 32) {
            n = (int) ctx.buf[ctx.pos++];
            x |= (n & 0xff) << shift;
            shift += 8;
        }
        return Float.intBitsToFloat(x);
    }

    private static void pack_double(OutputStream out, Double d) throws IOException {
        long x = Double.doubleToLongBits(d);
        out.write(BIN_TYPE_REAL_DOUBLE);

        byte shift = 64;

        while (shift > 0) {
            out.write((byte) (x & 0xff));
            x = x >> 8;
            shift -= 8;
        }
    }

    private static void pack_float(OutputStream out, Float f) throws IOException {
        int x = Float.floatToIntBits(f);
        out.write(BIN_TYPE_REAL_FLOAT);

        byte shift = 32;
        while (shift > 0) {
            out.write((byte) (x & 0xff));
            x = x >> 8;
            shift -= 8;
        }
    }

    private static void pack_list(OutputStream out, List list, String charsetName)
        throws IOException {
        out.write(BIN_TYPE_LIST);
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            do_pack(out, obj, charsetName);
        }
        out.write(BIN_TAG_SHUT);
    }

    private static void pack_map(OutputStream out, Map map, String charsetName) throws IOException {
        out.write(BIN_TYPE_DICT);
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            do_pack(out, entry.getKey(), charsetName);
            do_pack(out, entry.getValue(), charsetName);
        }
        out.write(BIN_TAG_SHUT);
    }

    private static void pack_string(OutputStream out, String s, String charsetName)
        throws IOException {
        byte[] bs = s.getBytes(charsetName);
        pack_num(out, bs.length, BIN_TYPE_STRING);
        out.write(bs);
    }

    private static void pack_num(OutputStream out, int len, byte type) throws IOException {
        // last number byte: 0000 xxxx
        while (len > BIN_TAG_PACK_NUM) {
            out.write((byte) (BIN_NUM_SIGN_BIT | (len & BIN_NUM_MASK)));
            len = len >>> 7;
        }
        out.write(type | len);
    }

    private static void pack_blob(OutputStream out, byte[] bs) throws IOException {
        pack_num(out, bs.length, BIN_TYPE_BLOB);
        out.write(bs);
    }

    private static void pack_null(OutputStream out) throws IOException {
        out.write(BIN_TYPE_NULL);
    }

    public static void pack_bool(OutputStream out, boolean v) throws IOException {
        out.write((v ? BIN_TYPE_BOOL : BIN_TYPE_BOOL_FALSE));
    }

    private static void pack_integer(OutputStream out, Number n) throws IOException {
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

    public static class DecodeCtx {
        byte[] buf;
        int pos; // start index
        String charsetName;
    }


    private static class TagInfo {
        byte type;
        long num;
    }

}
