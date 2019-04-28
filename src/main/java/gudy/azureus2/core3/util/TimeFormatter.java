package gudy.azureus2.core3.util;

public class TimeFormatter {
	
	// XXX should be i18n'd
	static final String[] TIME_SUFFIXES 	= { "s", "m", "h", "d", "y" };
		
	/**
	 * Format time into two time sections, the first chunk trimmed, the second
	 * with always with 2 digits.  Sections are *d, **h, **m, **s.  Section
	 * will be skipped if 0.
	 *
	 * @param time time in seconds
	 * @return Formatted time string
	 */
	public static String format(long timeSecs) {
		
		if (timeSecs == Constants.CRAPPY_INFINITY_AS_INT || timeSecs >= Constants.CRAPPY_INFINITE_AS_LONG)
			return Constants.INFINITY_STRING;

		if (timeSecs < 0)
			return "";

		// secs, mins, hours, days
		int[] vals = {
			(int) timeSecs % 60,
			(int) (timeSecs / 60) % 60,
			(int) (timeSecs / 3600) % 24,
			(int) (timeSecs / 86400) % 365,
			(int) (timeSecs / 31536000)
		};

		int end = vals.length - 1;
		while (vals[end] == 0 && end > 0) {
			end--;
		}

		String result = vals[end] + TIME_SUFFIXES[end];

		/* old logic removed to prefer showing consecutive units
		// skip until we have a non-zero time section
		do {
			end--;
		} while (end >= 0 && vals[end] == 0);
		*/

		end--;

		if (end >= 0)
			result += " " + twoDigits(vals[end]) + TIME_SUFFIXES[end];

		return result;
	}
	
	private static String twoDigits(int i) {
		return (i < 10) ? "0" + i : String.valueOf(i);
    }
}
