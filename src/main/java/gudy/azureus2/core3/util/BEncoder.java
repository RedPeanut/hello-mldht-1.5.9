package gudy.azureus2.core3.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class BEncoder {

	private static final int BUFFER_DOUBLE_LIMIT	= 256*1024;
	private static final byte[] MINUS_1_BYTES = "-1".getBytes();
	
	private byte[]		currentBuffer		= new byte[256];
	private int			currentBufferPos	= 0;
	private byte[][]	oldBuffers;
	
	private final byte[]	intBuffer		= new byte[12];
	
	private final boolean	urlEncode;
	
	private BEncoder(boolean _urlEncode) {
		urlEncode	= _urlEncode;
	}
	
	public static byte[] encode(Map object) throws IOException {
		BEncoder encoder = new BEncoder(false);
		encoder.encodeObject(object);
		return encoder.toByteArray();
	}

	private byte[] toByteArray() {
		if (oldBuffers == null) {
			byte[]	res = new byte[currentBufferPos];
			System.arraycopy(currentBuffer, 0, res, 0, currentBufferPos);
			// System.out.println("-> " + current_buffer_pos);
			return (res);
		} else {
			int	total = currentBufferPos;
			for (int i=0;i<oldBuffers.length;i++) {
				total += oldBuffers[i].length;
			}
			byte[] res = new byte[total];
			int	pos = 0;
			//String str = "";
			for (int i=0;i<oldBuffers.length;i++) {
				byte[] buffer = oldBuffers[i];
				int	len = buffer.length;
				System.arraycopy(buffer, 0, res, pos, len);
				pos += len;
				//str += (str.length()==0?"":",") + len;
			}
			System.arraycopy(currentBuffer, 0, res, pos, currentBufferPos);
			//System.out.println("-> " + str + "," + current_buffer_pos);
			return (res);
		}
	}

	private boolean encodeObject(Object object) throws IOException {
		
		if (object instanceof String || object instanceof Float || object instanceof Double) {
			String tempString = (object instanceof String) ? (String)object : String.valueOf(object);
			// usually this is simpler to encode by hand as chars < 0x80 map directly in UTF-8
			boolean	simple = true;
			int	charCount = tempString.length();
			byte[]	encoded = new byte[charCount];
			for (int i=0;i<charCount;i++) {
				char c = tempString.charAt(i);
				if (c < 0x80) {
					encoded[i] = (byte)c;
				} else {
					simple = false;
					break;
				}
			}
			if (simple) {
			 	writeInt(charCount);
				writeChar(':');
				writeBytes(encoded);
			} else {
				ByteBuffer	bb 	= Constants.DEFAULT_CHARSET.encode(tempString);
				writeInt(bb.limit());
				writeChar(':');
				writeByteBuffer(bb);
			}
		} else if (object instanceof Map) {
			Map tempMap = (Map)object;
			SortedMap tempTree = null;
			
			// unfortunately there are some occasions where we want to ensure that
			// the 'key' of the map is not mangled by assuming its UTF-8 encodable.
			// In particular the response from a tracker scrape request uses the
			// torrent hash as the KEY. Hence the introduction of the type below
			// to allow the constructor of the Map to indicate that the keys should
			// be extracted using a BYTE_ENCODING
			boolean	byte_keys = object instanceof ByteEncodedKeyHashMap;
			//write the d
			writeChar('d');
			//are we sorted?
			if (tempMap instanceof TreeMap) {
				tempTree = (TreeMap)tempMap;
			} else {
				tempTree = new TreeMap(tempMap);
			}
			Iterator	it = tempTree.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry	entry = (Map.Entry)it.next();
				Object o_key = entry.getKey();
				Object value = entry.getValue();
				if (value != null) {
					if (o_key instanceof byte[]) {
						encodeObject(o_key);
						if (!encodeObject(value))
							encodeObject("");
					} else if (o_key instanceof String) {
						String key = (String) o_key;
						if (byte_keys) {
							try {
								encodeObject(Constants.BYTE_CHARSET.encode(key));
								if (!encodeObject(value))
									encodeObject("");
							} catch (UnsupportedEncodingException e) {
								throw (new IOException("BEncoder: unsupport encoding: " + e.getMessage()));
							}
						} else {
							encodeObject(key); // Key goes in as UTF-8
							if (!encodeObject(value))
								encodeObject("");
						}
					} else
						Debug.out("Attempt to encode an unsupported map key type: " + object.getClass() + ";value=" + object);
				}
			}
			writeChar('e');
		} else if (object instanceof List) {
			List tempList = (List)object;
			//write out the l
			writeChar('l');
			for (int i = 0; i<tempList.size(); i++) {
				encodeObject( tempList.get(i));
			}
			writeChar('e');
		} else if (object instanceof Long) {
			Long tempLong = (Long)object;
			//write out the l
			writeChar('i');
			writeLong(tempLong.longValue());
			writeChar('e');
		} else if (object instanceof byte[]) {
			byte[] tempByteArray = (byte[])object;
			writeInt(tempByteArray.length);
			writeChar(':');
			if (urlEncode) {
				writeBytes(URLEncoder.encode(new String(tempByteArray, Constants.BYTE_ENCODING), Constants.BYTE_ENCODING ).getBytes());
			} else {
				writeBytes(tempByteArray);
			}
		} else if (object instanceof Integer) {
			Integer tempInteger = (Integer)object;
			//write out the l
			writeChar('i');
			writeInt(tempInteger.intValue());
			writeChar('e');
		} else if (object instanceof Byte) {
			byte temp = (Byte)object;
			writeChar('i');
			writeInt(temp & 0x000000ff);
			writeChar('e');
		} else if (object instanceof ByteBuffer) {
				ByteBuffer  bb = (ByteBuffer)object;
				writeInt(bb.limit());
				writeChar(':');
			writeByteBuffer(bb);
		} else if (object == null) {
			// ideally we'd bork here but I don't want to run the risk of breaking existing stuff so just log
			Debug.out("Attempt to encode a null value: sofar=" + getEncodedSoFar());
			return false;
		} else {
			Debug.out("Attempt to encode an unsupported entry type: " + object.getClass() + ";value=" + object);
			return false;
		}
		return true;
	}
	
	private String getEncodedSoFar() {
		return (new String(toByteArray()));
	}
	
	private void writeChar(char c) {
		int rem = currentBuffer.length - currentBufferPos;
		if (rem > 0) {
			currentBuffer[currentBufferPos++] = (byte)c;
		} else {
	   		int	next_buffer_size = currentBuffer.length < BUFFER_DOUBLE_LIMIT?(currentBuffer.length << 1):(currentBuffer.length + BUFFER_DOUBLE_LIMIT);
			byte[]	new_buffer = new byte[ next_buffer_size ];
			new_buffer[ 0 ] = (byte)c;
			if (oldBuffers == null) {
				oldBuffers = new byte[][]{ currentBuffer };
			} else {
				byte[][] new_old_buffers = new byte[oldBuffers.length+1][];
				System.arraycopy(oldBuffers, 0, new_old_buffers, 0, oldBuffers.length);
				new_old_buffers[ oldBuffers.length ] = currentBuffer;
				oldBuffers = new_old_buffers;
			}
			currentBuffer		= new_buffer;
			currentBufferPos 	= 1;
	 	}
   	}
	
	private void writeInt(int i) {
		// we get a bunch of -1 values, optimise
		if (i == -1) {
			writeBytes(MINUS_1_BYTES);
			return;
		}
		int start = intToBytes(i);
		writeBytes(intBuffer, start, 12 - start);
	}

	private void writeLong(long l) {
		if (l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) {
			writeInt((int) l);
		} else {
			writeBytes(Long.toString(l).getBytes());
		}
	}

	private void writeBytes(byte[] bytes) {
		writeBytes(bytes, 0, bytes.length);
	}

	private void writeBytes(
		byte[]			bytes,
		int				offset,
		int				length )
	{
		int rem = currentBuffer.length - currentBufferPos;
		if (rem >= length) {
			System.arraycopy(bytes, offset, currentBuffer, currentBufferPos, length);
			currentBufferPos += length;
		} else {
			if (rem > 0) {
				System.arraycopy(bytes, offset, currentBuffer, currentBufferPos, rem);
				length -= rem;
			}
			int	next_buffer_size = currentBuffer.length < BUFFER_DOUBLE_LIMIT?(currentBuffer.length << 1):(currentBuffer.length + BUFFER_DOUBLE_LIMIT);
			byte[]	new_buffer = new byte[ Math.max(next_buffer_size, length + 512) ];
			System.arraycopy(bytes, offset + rem, new_buffer, 0, length);
			if (oldBuffers == null) {
				oldBuffers = new byte[][]{ currentBuffer };
			} else {
				byte[][] new_old_buffers = new byte[oldBuffers.length+1][];
				System.arraycopy(oldBuffers, 0, new_old_buffers, 0, oldBuffers.length);
				new_old_buffers[ oldBuffers.length ] = currentBuffer;
				oldBuffers = new_old_buffers;
			}
			currentBuffer		= new_buffer;
			currentBufferPos 	= length;
	 	}
	}

	private void writeByteBuffer(ByteBuffer bb) {
		writeBytes( bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
	}
	
	/*
	 * The following code is from Integer.java as we don't want to
	 */
	final static byte[] digits = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z'
		};

	final static byte [] DigitTens = {
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
		} ;

	final static byte [] DigitOnes = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		} ;

	/**
	 * writes to int_buffer and returns start position in buffer (always runs to end of buffer)
	 * @param i
	 * @return
	 */
	private int intToBytes(int i) {
		int q, r;
		int charPos = 12;
		byte sign = 0;

		if (i < 0) {
			sign = '-';
			i = -i;
		}

		// Generate two digits per iteration
		while (i >= 65536) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			intBuffer [--charPos] = DigitOnes[r];
			intBuffer [--charPos] = DigitTens[r];
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i <= 65536, i);
		for (;;) {
			q = (i * 52429) >>> (16+3);
			r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
			intBuffer [--charPos] = digits [r];
			i = q;
			if (i == 0) break;
		}
		if (sign != 0) {
			intBuffer [--charPos] = sign;
		}
		return charPos;
	}
	
	private static Object normaliseObject(Object o) {
		if (o instanceof Integer) {
	   		o = new Long(((Integer)o).longValue());
	  	} else if (o instanceof Boolean) {
	   		o = new Long(((Boolean)o).booleanValue()?1:0);
	 	} else if (o instanceof Float) {
	   		o = String.valueOf((Float)o);
	 	} else if (o instanceof Double) {
	   		o = String.valueOf((Double)o);
	   	} else if (o instanceof byte[]) {
	   		try {
	   			byte[] b = (byte[])o;
	   			String s = new String(b,"UTF-8");
	   			byte[] temp = s.getBytes("UTF-8");
	   				// if converting the raw byte array into a UTF string and back again doesn't result in
	   				// the same bytes then things ain't gonna work dump as a demarked hex string that
	   				// can be recognized and handled in BDecoder appropriately
	   			if (!Arrays.equals( b, temp)) {
	   				StringBuilder sb = new StringBuilder(b.length * 2 + 4);
					sb.append("\\x");
	   				for (byte x: b) {
	   					String ss=Integer.toHexString(x&0xff);
						for (int k=0;k<2-ss.length();k++) {
							sb.append('0');
						}
						sb.append(ss);
	   				}
					sb.append("\\x");
	   				o = sb.toString();
	   			} else {
	   				o = s;
	   			}
	   		} catch (Throwable e) {
	   		}
	   	}
		return (o);
	}
public static boolean objectsAreIdentical(Object o1, Object o2) {
		
		if (o1 == null && o2 == null) {
			return (true);
		} else if (o1 == null || o2 == null) {
			return (false);
		}
	  	if (o1.getClass() != o2.getClass()) {
			if (	( o1 instanceof Map && o2 instanceof Map) ||
					(o1 instanceof List && o2 instanceof List)) {
				// things actually OK
			} else {
				o1 = normaliseObject(o1);
				o2 = normaliseObject(o2);
				if (o1.getClass() != o2.getClass()) {
					Debug.out("Failed to normalise classes " + o1.getClass() + "/" + o2.getClass());
					return (false);
				}
	  		}
		}
		if (	o1 instanceof Long ||
				o1 instanceof String) {
			return (o1.equals( o2));
	 	} else if (o1 instanceof byte[]) {
	 		return (Arrays.equals((byte[])o1,(byte[])o2));
		} else if (o1 instanceof List) {
			return (listsAreIdentical((List)o1,(List)o2));
	   	} else if (o1 instanceof Map) {
			return (mapsAreIdentical((Map)o1,(Map)o2));
	   	} else if (	o1 instanceof Integer ||
					o1 instanceof Boolean ||
					o1 instanceof Float ||
					o1 instanceof ByteBuffer) {
			return (o1.equals( o2));
		} else {
			Debug.out("Invalid type: " + o1);
			return (false);
		}
	}

	public static boolean listsAreIdentical(List list1, List list2) {
		if (list1 == null && list2 == null) {
			return (true);
		} else if (list1 == null || list2 == null) {
			return (false);
		}
		if (list1.size() != list2.size()) {
			return (false);
		}
		for (int i = 0; i < list1.size(); i++) {
			if (!objectsAreIdentical(list1.get(i), list2.get(i))) {
				return (false);
			}
		}
		return (true);
	}

	public static boolean mapsAreIdentical(Map map1, Map map2) {
		if (map1 == null && map2 == null) {
			return (true);
		} else if (map1 == null || map2 == null) {
			return (false);
		}
		if (map1.size() != map2.size()) {
			return (false);
		}
		for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) map1).entrySet()) {
			Object key = entry.getKey();
			Object v1 = entry.getValue();
			Object v2 = map2.get(key);
			if (!objectsAreIdentical(v1, v2)) {
				return (false);
			}
		}
		return (true);
	}
}
