package com.srain.binpack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A wrap for easy use.
 *
 * @author http://www.liaohuqiu.net
 */
@SuppressWarnings("rawtypes")
public class BinData {

	private static final String EMPTY_STRING = "";
	private Object listOrDict;

	public static BinData create(String str, String charsetName) {

		Object object = null;
		try {
			byte[] bs = str.getBytes(charsetName);
			object = BinPack.decode(bs, charsetName);
		} catch (Exception ex) {

		}
		return create(object);
	}

	public static BinData create(byte[] bs, String charsetName) {
		Object object = BinPack.decode(bs, charsetName);
		return create(object);
	}

	public static BinData create(Object o) {
		BinData binData = new BinData();
		if (o instanceof BinDict || o instanceof BinList) {
			binData.listOrDict = o;
		}
		if (o instanceof Map) {
			binData.listOrDict = new BinDict((Map) o);
		}
		if (o instanceof Collection) {
			binData.listOrDict = new BinList((Collection) o);
		}
		return binData;
	}

	public Object getRawData() {
		return listOrDict;
	}

	public BinData optBinData(String name) {

		Object ret = null;
		if (listOrDict instanceof BinDict) {
			ret = ((BinDict) listOrDict).opt(name);
		}
		return BinData.create(ret);
	}

	public BinData optBinData(int index) {
		Object ret = null;
		if (listOrDict instanceof BinList) {
			ret = ((BinList) listOrDict).opt(index);
		}
		return BinData.create(ret);
	}

	public String optString(String name) {
		return optMapOrNew().optString(name);
	}

	public String optString(int index) {
		return optArrayOrNew().optString(index);
	}

	public byte optByte(String name) {
		return optMapOrNew().optByte(name);
	}

	public byte optByte(int index) {
		return optArrayOrNew().optByte(index);
	}

	public short optShort(String name) {
		return optMapOrNew().optShort(name);
	}

	public short optShort(int index) {
		return optArrayOrNew().optShort(index);
	}

	public int optInt(String name) {
		return optMapOrNew().optInt(name);
	}

	public int optInt(int index) {
		return optArrayOrNew().optInt(index);
	}

	public long optLong(String name) {
		return optMapOrNew().optLong(name);
	}

	public long optLong(int index) {
		return optArrayOrNew().optLong(index);
	}

	public boolean optBoolean(String name) {
		return optMapOrNew().optBoolean(name);
	}

	public boolean optBoolean(int index) {
		return optArrayOrNew().optBoolean(index);
	}

	public double optDouble(String name) {
		return optMapOrNew().optDouble(name);
	}

	public double optDouble(int index) {
		return optArrayOrNew().optDouble(index);
	}

	public boolean has(String name) {
		return optMapOrNew().has(name);
	}

	public boolean has(int index) {
		return optArrayOrNew().length() > index;
	}

	public BinDict optMapOrNew() {
		if (listOrDict instanceof BinDict) {
			return (BinDict) listOrDict;
		}
		return new BinDict(null);
	}

	public BinList optArrayOrNew() {
		if (listOrDict instanceof BinList) {
			return (BinList) listOrDict;
		}
		return new BinList(null);
	}

	public int length() {
		if (listOrDict instanceof BinList) {
			return ((BinList) listOrDict).length();
		}
		if (listOrDict instanceof BinDict) {
			return ((BinDict) listOrDict).size();
		}
		return 0;
	}

	public String toString() {
		if (listOrDict instanceof BinList) {
			return ((BinList) listOrDict).toString();
		} else if (listOrDict instanceof BinDict) {
			return ((BinDict) listOrDict).toString();
		}
		return EMPTY_STRING;
	}

	public ArrayList<BinData> toArrayList() {
		ArrayList<BinData> arrayList = new ArrayList<BinData>();
		if (listOrDict instanceof BinList) {
			final BinList array = (BinList) listOrDict;
			for (int i = 0; i < array.length(); i++) {
				arrayList.add(i, BinData.create(array.opt(i)));
			}
		} else if (listOrDict instanceof BinDict) {
			final BinDict dict = (BinDict) listOrDict;

			Iterator it = dict.keys();

			while (it.hasNext()) {
				String key = (String) it.next();
				arrayList.add(BinData.create(dict.opt(key)));
			}
		}
		return arrayList;
	}
}
