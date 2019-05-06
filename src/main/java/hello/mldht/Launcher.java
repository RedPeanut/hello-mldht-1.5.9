package hello.mldht;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;

import gudy.azureus2.core3.util.AESemaphore;
import hello.util.Log;
import lbms.plugins.mldht.DHTConfiguration;
import lbms.plugins.mldht.kad.DHT;
import lbms.plugins.mldht.kad.DHT.DHTtype;
import lbms.plugins.mldht.kad.DHT.LogLevel;
import lbms.plugins.mldht.kad.DHTLogger;
import lbms.plugins.mldht.kad.RPCServerListener;

public class Launcher {
	
	private static String TAG = Launcher.class.getSimpleName();
	
	/* - mldht Launcher
	 * noRouterBootstrap: false
	 * persistId: true
	 * routingTableCachePath: "./dht.cache"
	 * port: 49001
	 * multihoming: true
	 * 
	 * - MlDHTPlugin
	 * onlyPeerBootstrap: 	false
	 * alwaysRestoreID: 	true
	 * dht.cache:			?
	 * port: 				49001
	 * multihoming: 		false
	 */

	// noRouterBootstrap = onlyPeerBootstrap
	// persistId = alwaysRestoreID
	// multihoming = multihoming
	
	private Map<DHTtype, DHT> dhts;
	
	//public void startDHT() {}
	//public void stopDHT() {}
	
	protected void start() {
		
		// create dht
		dhts = DHT.createDHTs();
		DHT.setLogLevel(LogLevel.Verbose);
		/*DHT.setLogger(new DHTLogger() {
			public void log(String message) {
				System.out.println(message);
			}
			public void log(Throwable e) {
				e.printStackTrace();
				//System.err.println(e);
			}
		});*/
		
		startDHT();
		
		/*// need 1 non-daemon-thread to keep VM alive
		while (true) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}*/
		
		//
		try {
			new UI(this, dhts).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startDHT() {

		// config
		DHTConfiguration config = new DHTConfiguration() {
			@Override public boolean noRouterBootstrap() 	{ return false; 					}
			@Override public boolean isPersistingID() 		{ return true; 						}
			@Override public File getNodeCachePath() 		{ return new File("./dht.cache"); 	}
			@Override public int getListeningPort() 		{ return 49001; 					}
			@Override public boolean allowMultiHoming() 	{ return false; 					}
		};
		
		RPCServerListener serverListener = new RPCServerListener() {
			
			//private String TAG = "Launcher$RPCServerListener";
			private String TAG = this.getClass().getName();
			
			@Override
			public void replyReceived(InetSocketAddress fromNode) {
				//Log.d(TAG, "replyReceived() is called...");
				//Log.d(TAG, "fromNode = " + fromNode);
			}
		};
		
		// start dht
		try {
			for (Map.Entry<DHTtype, DHT> e: dhts.entrySet()) {
				e.getValue().start(config, serverListener);
				e.getValue().bootstrap();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void stopDHT() {
		
		final AESemaphore sem = new AESemaphore("MLDHT:Stopper");
		
		/*display.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					if (dhts != null) {
						for (DHT dht : dhts.values()) {
							dht.stop();
						}
					}
				} finally {
					sem.release();
				}
			}
		});*/
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (dhts != null) {
						for (DHT dht : dhts.values()) {
							dht.stop();
						}
					}
				} finally {
					Log.d(TAG, "sem.release() is called...");
					sem.release();
				}
			}
		}).start();
		
		if (sem.reserve(30*1000) != 1) {
			Log.d(TAG, "Timeout waiting for DHT to stop");
		}
	}

	public static void main(String[] args) throws Exception {
		new Launcher().start();
	}
	
}
