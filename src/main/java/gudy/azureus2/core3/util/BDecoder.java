package gudy.azureus2.core3.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Map;

public class BDecoder {

	private static final int MAX_MAP_KEY_SIZE		= 64*1024;
	
	private static final boolean TRACE	= false;
	
	// reuseable objects for key decoding
	private ByteBuffer keyBytesBuffer = ByteBuffer.allocate(32);
	private CharBuffer keyCharsBuffer = CharBuffer.allocate(32);
	private final CharsetDecoder keyDecoder = Constants.BYTE_CHARSET.newDecoder();
	
	public Map decodeStream(BufferedInputStream data) throws Exception {
		return (Map<String, Object>) decodeInputStream(data, "", 0);
	}

	private Object decodeInputStream(InputStream is, String context, int nesting) throws Exception {
		is.mark(1);
		int read = is.read();
		switch (read) {
			case 'd': {
				//create a new dictionary object
				LightHashMap map = new LightHashMap();

				byte[] prevKey = null;
				//get the key
				while (true) {
					is.mark(1);
					read = is.read();
					if (read == 'e' || read == -1)
						break; // end of map
					is.reset();
					// decode key strings manually so we can reuse the bytebuffer
					int keyLength = (int)getPositiveNumberFromStream(is, ':');
					int skipBytes = 0;
					if (keyLength > MAX_MAP_KEY_SIZE) {
						skipBytes = keyLength - MAX_MAP_KEY_SIZE;
						keyLength = MAX_MAP_KEY_SIZE;
						//new Exception().printStackTrace();
						//throw (new IOException( msg));
					}
					if (keyLength < keyBytesBuffer.capacity()) {
						keyBytesBuffer.position(0).limit(keyLength);
						keyCharsBuffer.position(0).limit(keyLength);
					} else {
						keyBytesBuffer = ByteBuffer.allocate(keyLength);
						keyCharsBuffer = CharBuffer.allocate(keyLength);
					}
					getByteArrayFromStream(is, keyLength, keyBytesBuffer.array());
					if (skipBytes > 0) {
						is.skip(skipBytes);
					}
					
					keyDecoder.reset();
					keyDecoder.decode(keyBytesBuffer,keyCharsBuffer,true);
					keyDecoder.flush(keyCharsBuffer);
					String key = new String(keyCharsBuffer.array(),0,keyCharsBuffer.limit());

					//decode value
					Object value = decodeInputStream(is,key,nesting+1);
					
					// recover from some borked encodings that I have seen whereby the value has
					// not been encoded. This results in, for example,
					// 18:azureus_propertiesd0:e
					// we only get null back here if decoding has hit an 'e' or end-of-file
					// that is, there is no valid way for us to get a null 'value' here
					if (value == null) {
						System.err.println("Invalid encoding - value not serialsied for '" + key + "' - ignoring: map so far=" + map + ",loc=" + Debug.getCompressedStackTrace());
						break;
					}
					if (skipBytes > 0) {
						String msg = "dictionary key is too large - "
								+ (keyLength + skipBytes) + ":, max=" + MAX_MAP_KEY_SIZE
								+ ": skipping key starting with " + new String(key.substring(0, 128));
						System.err.println(msg);
					} else {
						if (map.put(key, value) != null) {
							Debug.out("BDecoder: key '" + key + "' already exists!");
						}
					}
				}
				
				is.mark(1);
				read = is.read();
				is.reset();
				if (nesting > 0 && read == -1) {
					throw (new BEncodingException("BDecoder: invalid input data, 'e' missing from end of dictionary"));
				}
				//map.compactify(-0.9f);
				//return the map
				return map;
			}
			case 'l':
				//create the list
				ArrayList list = new ArrayList();

				Object tempElement = null;
				while ((tempElement = decodeInputStream(is, context, nesting+1)) != null) {
					//add the element
					list.add(tempElement);
				}
				list.trimToSize();
				is.mark(1);
				read = is.read();
				is.reset();
				if (nesting > 0 && read == -1) {
					throw (new BEncodingException("BDecoder: invalid input data, 'e' missing from end of list"));
				}
				
				//return the list
				return list;
			case 'e':
			case -1:
				return null;
			case 'i':
				return Long.valueOf(getNumberFromStream(is, 'e'));
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				//move back one
				is.reset();
				//get the string
				return getByteArrayFromStream(is, context);
			default: {
				int	remLen = is.available();
				if (remLen > 256) {
					remLen = 256;
				}
				byte[] remData = new byte[remLen];
				is.read(remData);
				throw (new BEncodingException(
						"BDecoder: unknown command '" + read + ", remainder = " + new String(remData)));
			}
		}
	}

	private Object getByteArrayFromStream(InputStream is, String context) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getNumberFromStream(InputStream is, char c) {
		// TODO Auto-generated method stub
		return null;
	}

	private void getByteArrayFromStream(InputStream is, int keyLength, byte[] array) {
		// TODO Auto-generated method stub
		
	}

	private int getPositiveNumberFromStream(InputStream is, char c) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Object decodeInputStream(
			InputStream dbis,
			String		context,
			int			nesting,
			boolean internKeys)
			throws IOException
		{
			if (nesting == 0 && !dbis.markSupported()) {
				throw new IOException("InputStream must support the mark() method");
			}
			//set a mark
			dbis.mark(1);
			//read a byte
			int tempByte = dbis.read();
			//decide what to do
			switch (tempByte) {
				case 'd' :
					//create a new dictionary object
					LightHashMap tempMap = new LightHashMap();
					try {
						byte[]	prev_key = null;
						//get the key
						while (true) {
							dbis.mark(1);
							tempByte = dbis.read();
							if (tempByte == 'e' || tempByte == -1)
								break; // end of map
							dbis.reset();
							// decode key strings manually so we can reuse the bytebuffer
							int keyLength = (int)getPositiveNumberFromStream(dbis, ':');
							int skipBytes = 0;
							if (keyLength > MAX_MAP_KEY_SIZE) {
								skipBytes = keyLength - MAX_MAP_KEY_SIZE;
								keyLength = MAX_MAP_KEY_SIZE;
								//new Exception().printStackTrace();
								//throw (new IOException( msg));
							}
							if (keyLength < keyBytesBuffer.capacity()) {
								keyBytesBuffer.position(0).limit(keyLength);
								keyCharsBuffer.position(0).limit(keyLength);
							} else {
								keyBytesBuffer = ByteBuffer.allocate(keyLength);
								keyCharsBuffer = CharBuffer.allocate(keyLength);
							}
							getByteArrayFromStream(dbis, keyLength, keyBytesBuffer.array());
							if (skipBytes > 0) {
								dbis.skip(skipBytes);
							}
							
							keyDecoder.reset();
							keyDecoder.decode(keyBytesBuffer,keyCharsBuffer,true);
							keyDecoder.flush(keyCharsBuffer);
							String key = new String(keyCharsBuffer.array(),0,keyCharsBuffer.limit());
							// keys often repeat a lot - intern to save space
							if (internKeys)
								key = StringInterner.intern(key);
		
							//decode value
							Object value = decodeInputStream(dbis,key,nesting+1,internKeys);
							// value interning is too CPU-intensive, let's skip that for now
							/*if (value instanceof byte[] && ((byte[])value).length < 17)
							value = StringInterner.internBytes((byte[])value);*/
							if (TRACE) {
								System.out.println(key + "->" + value + ";");
							}
							
							// recover from some borked encodings that I have seen whereby the value has
							// not been encoded. This results in, for example,
							// 18:azureus_propertiesd0:e
							// we only get null back here if decoding has hit an 'e' or end-of-file
							// that is, there is no valid way for us to get a null 'value' here
							if (value == null) {
								System.err.println("Invalid encoding - value not serialsied for '" + key + "' - ignoring: map so far=" + tempMap + ",loc=" + Debug.getCompressedStackTrace());
								break;
							}
							if (skipBytes > 0) {
								String msg = "dictionary key is too large - "
										+ (keyLength + skipBytes) + ":, max=" + MAX_MAP_KEY_SIZE
										+ ": skipping key starting with " + new String(key.substring(0, 128));
								System.err.println(msg);
							} else {
			  					if (tempMap.put( key, value) != null) {
			  						Debug.out("BDecoder: key '" + key + "' already exists!");
			  					}
							}
						}
						/*
						if (tempMap.size() < 8) {
							tempMap = new CompactMap(tempMap);
						}*/
						dbis.mark(1);
						tempByte = dbis.read();
						dbis.reset();
						if (nesting > 0 && tempByte == -1) {
							throw (new BEncodingException("BDecoder: invalid input data, 'e' missing from end of dictionary"));
						}
					} catch (Throwable e) {
						throw (new IOException(Debug.getNestedExceptionMessage(e)));
					}
					tempMap.compactify(-0.9f);
					//return the map
					return tempMap;
				case 'l' :
					//create the list
					ArrayList tempList = new ArrayList();
					try {
						//create the key
						//String context2 = PORTABLE_ROOT==null?context:(context+"[]");
						String context2 = context;
						Object tempElement = null;
						while ((tempElement = decodeInputStream(dbis, context2, nesting+1, internKeys)) != null) {
							//add the element
							tempList.add(tempElement);
						}
						tempList.trimToSize();
						dbis.mark(1);
						tempByte = dbis.read();
						dbis.reset();
						if (nesting > 0 && tempByte == -1) {
							throw (new BEncodingException("BDecoder: invalid input data, 'e' missing from end of list"));
						}
					} catch (Throwable e) {
						throw (new IOException(Debug.getNestedExceptionMessage(e)));
					}
					//return the list
					return tempList;
				case 'e' :
				case -1 :
					return null;
				case 'i' :
					return Long.valueOf(getNumberFromStream(dbis, 'e'));
				case '0' :
				case '1' :
				case '2' :
				case '3' :
				case '4' :
				case '5' :
				case '6' :
				case '7' :
				case '8' :
				case '9' :
					//move back one
					dbis.reset();
					//get the string
					return getByteArrayFromStream(dbis, context);
				default :{
					int	rem_len = dbis.available();
					if (rem_len > 256) {
						rem_len	= 256;
					}
					byte[] rem_data = new byte[rem_len];
					dbis.read(rem_data);
					throw (new BEncodingException(
							"BDecoder: unknown command '" + tempByte + ", remainder = " + new String(rem_data)));
				}
			}
		}
	
	private Map<String, Object> decode(InputStream data, boolean internKeys) throws IOException {

		Object res = decodeInputStream(data, "", 0, internKeys);

		if (res == null) {
			throw new BEncodingException("BDecoder: zero length file");
		} else if (!(res instanceof Map)) {
			throw (new BEncodingException("BDecoder: top level isn't a Map"));
		}

		return (Map<String, Object>) res;
	}
	
	public Map<String, Object> decodeByteArray(
		byte[] 	data,
		int		offset,
		int		length,
		boolean internKeys)
		throws IOException {
		return (decode(new BDecoderInputStreamArray(data, offset, length),internKeys));
	}
	
	private static class BDecoderInputStreamArray
	extends InputStream {
	final private byte[] bytes;
	private int pos = 0;
	private int markPos;
	private final int overPos;

	public BDecoderInputStreamArray(ByteBuffer buffer) {
		bytes = buffer.array();
		pos = buffer.arrayOffset() + buffer.position();
		overPos = pos + buffer.remaining();
	}


	private BDecoderInputStreamArray(
		byte[]		_buffer) {
		bytes = _buffer;
		overPos = bytes.length;
	}

	private BDecoderInputStreamArray(
		byte[]		_buffer,
		int			_offset,
		int			_length) {
		if (_offset == 0) {
			bytes = _buffer;
			overPos = _length;
		} else {
			bytes = _buffer;
			pos = _offset;
			overPos = Math.min(_offset + _length, bytes.length);
		}
	}

	public int read() throws IOException {
		if (pos < overPos) {
			return bytes[pos++] & 0xFF;
		}
		return -1;
	}

	public int read(byte[] buffer) throws IOException {
		return (read(buffer, 0, buffer.length));
	}

	public int read(
		byte[] 	b,
		int		offset,
		int		length )
		throws IOException {
		if (pos < overPos) {
			int toRead = Math.min(length, overPos - pos);
			System.arraycopy(bytes, pos, b, offset, toRead);
			pos += toRead;
			return toRead;
		}
		return -1;
	}

	public int available() throws IOException {
		return overPos - pos;
	}

	public boolean markSupported() {
		return (true);
	}

	public void mark(int limit) {
		markPos = pos;
	}

	public void reset() throws IOException {
		pos = markPos;
	}
}
}
