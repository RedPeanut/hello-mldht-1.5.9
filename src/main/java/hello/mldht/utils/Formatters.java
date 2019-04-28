package hello.mldht.utils;

public class Formatters {

	public String formatByteCountToKiBEtc(long bytes) {
		return String.format("%.1f", (float) bytes/1000);
	}

	public String formatByteCountToKiBEtcPerSec(long bytes) {
		return String.format("%.1f", (float) bytes/1000);
	}
}
