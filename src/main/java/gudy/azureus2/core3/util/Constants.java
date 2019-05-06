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
	
	public static final String OSName = System.getProperty("os.name");
	
	public static final boolean isOSX			= OSName.toLowerCase().startsWith("mac os");
	public static final boolean isLinux			= OSName.equalsIgnoreCase("Linux");
	public static final boolean isSolaris		= OSName.equalsIgnoreCase("SunOS");
	public static final boolean isFreeBSD		= OSName.equalsIgnoreCase("FreeBSD");
	public static final boolean isWindowsXP		= OSName.equalsIgnoreCase("Windows XP");
	public static final boolean isWindows95		= OSName.equalsIgnoreCase("Windows 95");
	public static final boolean isWindows98		= OSName.equalsIgnoreCase("Windows 98");
	public static final boolean isWindows2000	= OSName.equalsIgnoreCase("Windows 2000");
	public static final boolean isWindowsME		= OSName.equalsIgnoreCase("Windows ME");
	public static final boolean isWindows9598ME	= isWindows95 || isWindows98 || isWindowsME;
	
	public static final boolean isWindows	= OSName.toLowerCase().startsWith("windows");
	// If it isn't windows or osx, it's most likely an unix flavor
	public static final boolean isUnix = !isWindows && !isOSX;
}
