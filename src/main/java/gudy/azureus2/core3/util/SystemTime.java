package gudy.azureus2.core3.util;

public class SystemTime {
	
	public static final long TIME_GRANULARITY_MILLIS	= 25; // internal update time ms
	
	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public static long getMonotonousTime() {
		return System.currentTimeMillis();
	}
}
