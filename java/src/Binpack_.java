import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaofei.wxf
 */
public class Binpack_ {
    static final byte TYPE_CLOSE = 0x01;//0000 0001
    static final byte TYPE_NULL = 0x0f;//0000 1111
    static final byte TYPE_BOOL_TRUE = 0x04;//0000 0100
    static final byte TYPE_BOOL_FALSE = 0x05;//0000 0101
    static final byte TYPE_INT_POSITIVE = 0x40;//010x xxxx
    static final byte TYPE_INT_NEGATIVE = 0x60;//011x xxxx
    static final byte TYPE_BYTES = 0x10;//0001 xxxx
    static final byte TYPE_STRING = 0x20;//0010 xxxx
    static final byte TYPE_FLOAT = 0x07;//0000 0111
    static final byte TYPE_DOUBLE = 0x06;//0000 0110
    static final byte TYPE_LIST = 0x02;//0000 0010
    static final byte TYPE_MAP = 0x03;//0000 0011

    static final byte INT_BYTE_LENGTH = 0x01 << 3;//xxx0 1xxx
    static final byte INT_SHORT_LENGTH = 0x02 << 3;//xxx1 0xxx
    static final byte INT_INTEGER_LENGTH = 0x03 << 3;//xxx1 1xxx
    static final byte INT_LONG_LENGTH = 0x00;//xxx0 0xxx

    static final byte INT_3_BITS_VALUE = 0x07;//0000 0111

    static final byte INT_INT_MASK_1 = 0x7f;
    static final byte INT_INT_MASK_2 = (byte) 0x80;

    static final byte INT_4_BITS_VALUE = 0x0f;

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //pack
    /////////////////////////////////////////////////////////////////////////////////////////////////

    public static final void pack(OutputStream out, Object o) throws IOException {
        if (null == o) {
            packNull(out);
        }
        if (o instanceof Byte) {
            pack(out, (Byte) o);
        } else if (o instanceof Short) {
            pack(out, (Short) o);
        } else if (o instanceof Integer) {
            pack(out, (Integer) o);
        } else if (o instanceof Long) {
            pack(out, (Long) o);
        } else if (o instanceof Float) {
            pack(out, (Long) o);
        } else if (o instanceof Double) {
            pack(out, (Double) o);
        } else if (o instanceof List) {
            pack(out, (List) o);
        } else if (o instanceof Map) {
            pack(out, (Map) o);
        }


    }

    private static final boolean writeIfNull(OutputStream out, Object o) throws IOException {
        if (null == out) {
            throw new NullPointerException("output stream");
        }
        if (null == o) {
            out.write(TYPE_NULL);
            return true;
        }
        return false;
    }

    public static final void packNull(OutputStream out) throws IOException {
        writeIfNull(out, null);
    }

    public static final void pack(OutputStream out, Boolean b) throws IOException {
        if (writeIfNull(out, b)) {
            return;
        }
        if (b) {
            out.write(TYPE_BOOL_TRUE);
        } else {
            out.write(TYPE_BOOL_FALSE);
        }
    }

    public static final void pack(OutputStream out, Byte b) throws IOException {
        if (writeIfNull(out, b)) {
            return;
        }
        int value = b.intValue();
        byte type = TYPE_INT_POSITIVE;
        if (b < 0) {
            value = -value;
            type = TYPE_INT_NEGATIVE;
        }
        type |= INT_BYTE_LENGTH;
        while (value > INT_3_BITS_VALUE) {
            out.write(INT_INT_MASK_2 | INT_INT_MASK_1 & value);
            value = value >> 7;
        }
        out.write(type | value);
    }

    public static final void pack(OutputStream out, Short b) throws IOException {
        if (writeIfNull(out, b)) {
            return;
        }
        int value = b.intValue();
        byte type = TYPE_INT_POSITIVE;
        if (b < 0) {
            value = -value;
            type = TYPE_INT_NEGATIVE;
        }
        type |= INT_SHORT_LENGTH;
        while (value > INT_3_BITS_VALUE) {
            out.write(INT_INT_MASK_2 | INT_INT_MASK_1 & value);
            value = value >> 7;
        }
        out.write(type | value);
    }

    public static final void pack(OutputStream out, Integer i) throws IOException {
        if (writeIfNull(out, i)) {
            return;
        }
        int value = i.intValue();
        byte type = TYPE_INT_POSITIVE;
        if (i < 0) {
            value = -value;
            type = TYPE_INT_NEGATIVE;
        }
        type |= INT_INTEGER_LENGTH;
        while (value > INT_3_BITS_VALUE) {
            out.write(INT_INT_MASK_2 | INT_INT_MASK_1 & value);
            value = value >> 7;
        }
        out.write(type | value);
    }

    public static final void pack(OutputStream out, Long l) throws IOException {
        if (writeIfNull(out, l)) {
            return;
        }
        long value = l.longValue();
        byte type = TYPE_INT_POSITIVE;
        if (l < 0) {
            value = -value;
            type = TYPE_INT_NEGATIVE;
        }
        type |= INT_LONG_LENGTH;
        while (value > INT_3_BITS_VALUE) {
            byte dd = (byte) (INT_INT_MASK_2 | INT_INT_MASK_1 & value);
            out.write((int) (INT_INT_MASK_2 | INT_INT_MASK_1 & value));
            value = value >> 7;
        }
        out.write((int) (type | value));
    }

    public static final void pack(OutputStream out, byte[] bytes) throws IOException {
        if (writeIfNull(out, bytes)) {
            return;
        }
        //length
        int value = bytes.length;
        while (value > INT_4_BITS_VALUE) {
            out.write(INT_INT_MASK_2 | INT_INT_MASK_1 & value);
            value = value >> 7;
        }
        out.write(TYPE_BYTES | value);
        out.write(bytes);
    }

    public static final Charset charset = Charset.forName("utf-8");

    public static final void pack(OutputStream out, String str) throws IOException {
        if (writeIfNull(out, str)) {
            return;
        }
        byte[] bytes = str.getBytes(charset);
        //length
        int value = bytes.length;
        while (value > INT_4_BITS_VALUE) {
            out.write(INT_INT_MASK_2 | INT_INT_MASK_1 & value);
            value = value >> 7;
        }
        out.write(TYPE_STRING | value);
        out.write(bytes);
    }

    public static final void pack(OutputStream out, Float f) throws IOException {
        if (writeIfNull(out, f)) {
            return;
        }
        out.write(TYPE_FLOAT);
        int x = Float.floatToIntBits(f);
        byte shift = 32;
        while (shift > 0) {
            out.write((byte) (x & 0xff));
            x = x >> 8;
            shift -= 8;
        }
    }

    public static final void pack(OutputStream out, Double d) throws IOException {
        if (writeIfNull(out, d)) {
            return;
        }
        out.write(TYPE_DOUBLE);
        long x = Double.doubleToLongBits(d);
        byte shift = 64;
        while (shift > 0) {
            out.write((byte) (x & 0xff));
            x = x >> 8;
            shift -= 8;
        }
    }

    public static final void pack(OutputStream out, List list) throws IOException {
        if (writeIfNull(out, list)) {
            return;
        }
        out.write(TYPE_LIST);
        for (Object each : list) {
            pack(out, each);
        }
        out.write(TYPE_CLOSE);
    }

    public static final void pack(OutputStream out, Map map) throws IOException {
        if (writeIfNull(out, map)) {
            return;
        }
        out.write(TYPE_MAP);
        for (Object each : map.entrySet()) {
            Map.Entry<Object, Object> e = (Map.Entry<Object, Object>) each;
            pack(out, e.getKey());
            pack(out, e.getValue());
        }
        out.write(TYPE_CLOSE);
    }

    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pack(out, 232323);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        unpack(in);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //unpack
    /////////////////////////////////////////////////////////////////////////////////////////////////
    private static final Object CLOSE_MARK = new Object();

    public static final Object unpack(InputStream in) throws IOException {
        byte type = 0;
        long data = 0;
        int read = -1;
        int count = 0;
        while ((read = in.read()) != -1 && type == 0) {
            if (read < 128) {
                type = (byte) read;
            } else {
                System.out.println(Integer.toBinaryString(read));
                data |= ((read & INT_INT_MASK_1) << (count * 7));
            }
            count++;
        }
        if (count <= TYPE_NULL) {
            //单字节 或者 复杂结构
            switch (type) {
                case TYPE_NULL:
                    return null;
                case TYPE_BOOL_TRUE:
                    return Boolean.TRUE;
                case TYPE_BOOL_FALSE:
                    return Boolean.FALSE;
                case TYPE_CLOSE:
                    return CLOSE_MARK;
                case TYPE_LIST:
                    List l = new ArrayList();
                    while (true) {
                        Object o = unpack(in);
                        if (o == CLOSE_MARK) {
                            return l;
                        }
                        l.add(o);
                    }
                case TYPE_MAP:
                    Map m = new HashMap();
                    while (true) {
                        Object k = unpack(in);
                        if (k == CLOSE_MARK) {
                            return m;
                        }
                        Object v = unpack(in);
                        m.put(k, v);
                    }
                default:
                    return null;
            }
        } else {

        }
        return null;
    }

}
