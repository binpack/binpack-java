import in.srain.binpack.BinData;
import in.srain.binpack.BinPack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinPack.pack(out, out, "UTF-8");
        BinPack.pack(out, 1234.5f, "UTF-8");
        System.out.println(BinPack.decode(out.toByteArray(), "UTF-8"));

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("abc", 32);
        m.put("hello", "You jump, I jump");
        m.put("world", "All men are created equal".getBytes("UTF-8"));
        m.put("float", 12345.6789E123);

        for (int i = 0; i < 1024 * 1024 * 1; ++i) {
            byte[] tmp = BinPack.encode(m, "GBK");
        }

        byte[] v = BinPack.encode(m, "GBK");
        System.out.println(v.length);

        for (int i = 0; i < 1024 * 1024 * 1; ++i) {
            Object obj = BinPack.decode(v, "GBK");
        }

        Object obj = BinPack.decode(v, "GBK");
        System.out.println(obj);
    }
}