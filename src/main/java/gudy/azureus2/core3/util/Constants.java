package gudy.azureus2.core3.util;

import java.nio.charset.Charset;

public class Constants {
	
	public static final String DEFAULT_ENCODING 	= "UTF8";
	public static final String BYTE_ENCODING 		= "ISO-8859-1";
	public static final Charset	DEFAULT_CHARSET;
	public static final Charset	BYTE_CHARSET;
	
	static {
		DEFAULT_CHARSET	= Charset.forName(Constants.DEFAULT_ENCODING);;
	 	BYTE_CHARSET 	= Charset.forName(Constants.BYTE_ENCODING);;
	}
	
	public static final String	INFINITY_STRING	= "\u221E"; // "oo";pa
	public static final int		CRAPPY_INFINITY_AS_INT	= 365*24*3600; // seconds (365days)
	public static final long	CRAPPY_INFINITE_AS_LONG = 10000*365*24*3600; // seconds (10k years)
}
