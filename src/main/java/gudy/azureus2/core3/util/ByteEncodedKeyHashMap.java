package gudy.azureus2.core3.util;

import java.util.HashMap;
import java.util.Map;

public class ByteEncodedKeyHashMap<T, S> extends HashMap<T, S> {
	public ByteEncodedKeyHashMap() {
		super();
	}

	public ByteEncodedKeyHashMap(Map<T, S> map) {
		super(map);
	}
}
