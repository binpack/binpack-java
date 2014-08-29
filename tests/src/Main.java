import in.srain.binpack.BinPack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

enum A {
    JAVA, FLASH,
}

public class Main {
    public static void main(String[] args) throws IOException {

        double[] d = new double[]{1, 2, 3};
        byte[] str = BinPack.encode(d, "UTF-8");
        Object o = BinPack.decode(str, "UTF-8");

        String strToBs = "All men are created equal.";
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("abc", 32);
        m.put("hello", "You jump, I jump");
        m.put("world", strToBs.getBytes("UTF-8"));
        m.put("float", 12345.6789E123);

        byte[] tmp = BinPack.encode(m, "UTF-8");
        Map<String, Object> obj = (Map<String, Object>) BinPack.decode(tmp, "UTF-8");
        byte[] bs = (byte[]) obj.get("world");
        System.out.println(m);
        System.out.println(obj);
        System.out.println(new String(bs, "UTF-8").equals(strToBs));
    }
}