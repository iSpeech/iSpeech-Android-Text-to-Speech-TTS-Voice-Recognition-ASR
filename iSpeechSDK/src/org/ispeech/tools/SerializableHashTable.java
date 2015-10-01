package org.ispeech.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class SerializableHashTable {
	private Hashtable<String, byte[]> _h = new Hashtable<String, byte[]>();

	public byte[] serialize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);

		Vector<byte[]> elements = new Vector<byte[]>();
		int totalSize = 0;
		for (String key : _h.keySet()) {
			byte[] value = this.getBytes(key);
			ByteArrayOutputStream e1 = new ByteArrayOutputStream();
			DataOutputStream e = new DataOutputStream(e1);

			e.writeShort(key.length());
			e.write(key.getBytes("utf8"));

			e.writeShort(value.length);
			e.write(value);

			byte[] ed = e1.toByteArray();
			elements.addElement(ed);
			e.close();
			e1.close();
			totalSize += ed.length;
		}
		out.writeInt(totalSize + 2);
		out.writeShort(this._h.size());
		for (byte[] e : elements)
			out.write(e);
		return baos.toByteArray();
	}

	public Object put(String key, String value) {
		try {
			return this._h.put(key, value.getBytes("utf8"));
		} catch (Exception e) {
		}
		return null;
	}

	public Object put(String key, byte[] value) {
		return this._h.put(key, value);
	}

	public byte[] getBytes(String key) {
		return (byte[]) this._h.get(key);
	}

	public int size() {
		return _h.size();
	}

	public String getString(String key) {
		try {
			return new String(this.getBytes(key), "utf8");
		} catch (Exception e) {
		}
		return null;
	}

	public static SerializableHashTable deserialize(byte data[]) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(bais);
		int totalSize = in.readInt();
		SerializableHashTable h = new SerializableHashTable();
		if (data.length - 4 == totalSize) {
			// size matches up, lets go
			int elementCount = in.readShort();
			for (int i = 0; i < elementCount; i++) {
				int keyLength = in.readShort();
				byte[] buffer = new byte[keyLength];
				in.read(buffer);
				String key = new String(buffer, "utf8");
				int valueLength = in.readShort();
				buffer = new byte[valueLength];
				in.read(buffer);
				h.put(key, buffer);
			}
			return h;
		}
		return null;
	}

	public boolean containsKey(String key) {
		return this._h.containsKey(key);
	}
}
