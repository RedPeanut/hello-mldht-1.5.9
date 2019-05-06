package hello.util;

public class Log {
	
	private static LogLevel logLevel = LogLevel.Verbose;
	
	public static enum LogLevel {
		Fatal, Error, Info, Debug, Verbose
	}
	
	public static void d(String tag, String msg) {
		print(String.format("[%s] %s", tag, msg), LogLevel.Debug);
	}
	
	public static void v(String tag, String msg) {
		print(String.format("[%s] %s", tag, msg), LogLevel.Verbose);
	}
	
	public static void print(String msg, LogLevel level) {
		if (level.compareTo(logLevel) < 1) { // <=
			System.out.println(msg);
		}
	}
}
