package gudy.azureus2.core3.util;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.Date;

public class Debug {

	public static void out(String string) {
		// TODO Auto-generated method stub
	}
	
	public static void out(Exception e) {
		// TODO Auto-generated method stub
	}
	
	public static void out(Throwable e) {
		// TODO Auto-generated method stub
	}

	public static void out(String msg, Throwable exception) {
		
		String header = "DEBUG::";
		header = header + new Date(SystemTime.getCurrentTime()).toString() + "::";
		String className;
		String methodName;
		int lineNumber;
		String	traceTraceTail = null;
		
		Throwable t = new Throwable();
		StackTraceElement[]	st = t.getStackTrace();
		StackTraceElement firstLine = st[2];
		className = firstLine.getClassName() + "::";
		methodName = firstLine.getMethodName() + "::";
		lineNumber = firstLine.getLineNumber();
		
		traceTraceTail = getCompressedStackTrace(t, 3, 200, false);
		
		System.err.println(header+className+(methodName)+lineNumber+":");
		if (msg.length() > 0) {
			System.err.println("	" + msg);
		}
		if (traceTraceTail != null) {
			System.err.println("		" + traceTraceTail);
		}
		if (exception != null) {
			exception.printStackTrace();
		}
		
	}
	
	private static String getCompressedStackTrace(
		Throwable t,
		int framesToSkip) {
		return getCompressedStackTrace(t, framesToSkip, 200);
	}


	public static String getCompressedStackTrace(
		Throwable t,
		int framesToSkip,
		int iMaxLines) {
		return getCompressedStackTrace(t, framesToSkip, iMaxLines, true);
	}
	
	public static String getCompressedStackTrace(
			Throwable t,
			int framesToSkip,
			int iMaxLines,
			boolean showErrString) {
			
			StringBuilder sbStackTrace = new StringBuilder(showErrString ? (t.toString() + "; ") : "");
			StackTraceElement[]	st = t.getStackTrace();

			if (iMaxLines < 0) {
				iMaxLines = st.length + iMaxLines;
				if (iMaxLines < 0) {
					iMaxLines = 1;
				}
			}
			int iMax = Math.min(st.length, iMaxLines + framesToSkip);
			for (int i = framesToSkip; i < iMax; i++) {

				if (i > framesToSkip) {
					sbStackTrace.append(", ");
				}

				String classname = st[i].getClassName();
				String cnShort = classname.substring(classname.lastIndexOf(".")+1);

				sbStackTrace.append(cnShort);
				sbStackTrace.append("::");
				sbStackTrace.append(st[i].getMethodName());
				sbStackTrace.append("::");
				sbStackTrace.append(st[i].getLineNumber());
			}

			Throwable cause = t.getCause();

			if (cause != null) {
				sbStackTrace.append("\n\tCaused By: ");
				sbStackTrace.append(getCompressedStackTrace(cause, 0));
			}

			return sbStackTrace.toString();
		}
	
	public static void printStackTrace(Throwable e) {
		// TODO Auto-generated method stub
	}

	public static String getNestedExceptionMessage(Throwable e) {
		
		String	lastMessage	= "";
		while (e != null) {
			String	thisMessage;
			if (e instanceof UnknownHostException) {
				thisMessage = "Unknown host " + e.getMessage();
			} else if (e instanceof FileNotFoundException) {
				thisMessage = "File not found: " + e.getMessage();
			} else {
				thisMessage = e.getMessage();
			}
			// if no exception message then pick up class name. if we have a deliberate
			// zero length string then we assume that the exception can be ignored for
			// logging purposes as it is just delegating
			if (thisMessage == null) {
				thisMessage = e.getClass().getName();
				int	pos = thisMessage.lastIndexOf(".");
				thisMessage = thisMessage.substring(pos+1).trim();
			}
			if (thisMessage.length() > 0 && !lastMessage.contains(thisMessage)) {
				lastMessage	+= (lastMessage.length()==0?"":", " ) + thisMessage;
			}
			e	= e.getCause();
		}
		return (lastMessage);
	}

	public static String getCompressedStackTrace() {
		return (getCompressedStackTrace(new Throwable(), 1, 200, false));
	}

}
