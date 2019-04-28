package gudy.azureus2.core3.util;

public class StringInterner {
	
	private static final LightHashSet managedInterningSet = new LightHashSet(800);
	
	private static final String[] COMMON_KEYS = {
		"src","port","prot","ip","udpport","azver","httpport","downloaded",
		"Content","Refresh On","path.utf-8","uploaded","completed","persistent","attributes","encoding",
		"azureus_properties","stats.download.added.time","networks","p1","resume data","dndflags","blocks","resume",
		"primaryfile","resumecomplete","data","peersources","name.utf-8","valid","torrent filename","parameters",
		"secrets","timesincedl","tracker_cache","filedownloaded","timesinceul","tracker_peers","trackerclientextensions","GlobalRating",
		"comment.utf-8","Count","String","stats.counted","Thumbnail","Plugin.<internal>.DDBaseTTTorrent::sha1","type","Title",
		"displayname","Publisher","Creation Date","Revision Date","Content Hash","flags","stats.download.completed.time","Description",
		"Progressive","Content Type","QOS Class","DRM","hash","ver","id",
		"body","seed","eip","rid","iip","dp2","tp","orig",
		"dp","Quality","private","dht_backup_enable","max.uploads","filelinks","Speed Bps","cdn_properties",
		"sha1","ed2k","DRM Key","Plugin.aeseedingengine.attributes","initial_seed","dht_backup_requested","ta","size",
		"DIRECTOR PUBLISH","Plugin.azdirector.ContentMap","dateadded","bytesin","announces","status","bytesout","scrapes",
		"passive",
	};

	private static final ByteArrayHashMap byteMap = new ByteArrayHashMap(COMMON_KEYS.length);
	
	static {
		try {
			for (int i=0;i<COMMON_KEYS.length;i++) {
				byteMap.put(COMMON_KEYS[i].getBytes(Constants.BYTE_ENCODING), COMMON_KEYS[i]);
				managedInterningSet.add(COMMON_KEYS[i]);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static String intern(byte[] bytes) {
		String res = (String) byteMap.get(bytes);
		// System.out.println(new String( bytes ) + " -> " + res);
		return (res);
	}
	
	public static String intern(String toIntern) {
		
		if (toIntern == null)
			return null;
		
		String internedString;
		String internedEntry = null;
		boolean hit = false;
		
		internedEntry = (String) managedInterningSet.get(toIntern);
		if (internedEntry != null && (internedString = internedEntry) != null)
			hit = true;
		else {
			toIntern = new String(toIntern);	// this trims any baggage that might be included in the original string due to char[] sharing for substrings etc
			//checkEntry = new WeakStringEntry(toIntern);
			managedInterningSet.add(toIntern);
			internedString = toIntern;
		}
		
		return internedString;
	}
}
