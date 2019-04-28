package hello.mldht.utils;

public class Formatters {

	public String formatByteCountToKiBEtc(long bytes) {
		return String.format("%.1f", (float) bytes/1000)+" kB";
	}

	public String formatByteCountToKiBEtcPerSec(long bytes) {
		if (bytes >= 1000)
			return String.format("%.1f", (float) bytes/1000)+" kB/s";
		else
			return String.format("%.1f", (float) bytes)+" B/s";
	}

	public String formatTimeFromSeconds(long time) {
		
		if (time < 0) return "";

		int secs = (int) time % 60;
		int mins = (int) (time / 60) % 60;
		int hours = (int) (time /3600) % 24;
		int days = (int) (time / 86400) % 365;
		int years = (int) (time / 31536000);

		String result = "";
		if (years > 0) result += years + "y ";
		if (years > 0 || days > 0) result += days + "d ";
		result += twoDigits(hours) + ":" + twoDigits(mins) + ":" + twoDigits(secs);

		return result;
	}
	
	private static String twoDigits(int i) {
		return (i < 10) ? "0" + i : String.valueOf(i);
	}
}
