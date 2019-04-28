package hello.mldht;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import hello.mldht.utils.Formatters;
import hello.util.Log;
import lbms.plugins.mldht.kad.DHT;
import lbms.plugins.mldht.kad.DHT.DHTtype;
import lbms.plugins.mldht.kad.DHTStats;
import lbms.plugins.mldht.kad.DHTStatsListener;
import lbms.plugins.mldht.kad.RPCStats;
import lbms.plugins.mldht.kad.messages.MessageBase.Method;
import lbms.plugins.mldht.kad.messages.MessageBase.Type;

public class UI {

	private static String TAG = UI.class.getSimpleName();
	
	Map<DHTtype, DHT> dhts;
	
	public UI(Map<DHTtype, DHT> dhts) {
		this.dhts = dhts;
	}
	
	public void start() {
		
		formatters = new Formatters();
		
		Display display = new Display();
		
		Shell shell = new Shell(display);
		shell.setText("Hello, world!");
		//shell.setLayout(new FormLayout());
		
		initialize(shell);
		activate(display);
		
		shell.pack();
		refresh();
		
		shell.open();
		
		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in the event queue
				display.sleep();
			}
		}
		display.dispose();
	}
	
	//Composite panel;
	
	private void initialize(Composite shell) {
		//panel = new Composite(parent, SWT.NULL);
		
		GridLayout gl = new GridLayout();
		shell.setLayout(gl);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		shell.setLayoutData(gridData);

		final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
				SWT.V_SCROLL | SWT.H_SCROLL);

		final Composite compOnSC = new Composite(scrollComposite, SWT.None);

		/*GridLayout*/ gl = new GridLayout(2, false);
		compOnSC.setLayout(gl);
		
		gridData = new GridData(GridData.FILL_BOTH);
		compOnSC.setLayoutData(gridData);
		
		createDHTStatsGroup(compOnSC);
		createControlGroup(compOnSC);
		createRPCGroup(compOnSC);
		createMessageStatsGroup(compOnSC);
		
		createRoutingTableView(compOnSC);
		
		scrollComposite.setContent(compOnSC);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		
		Listener l = new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.Close) {
					Log.d(TAG, "SWT.Close is occured");
				} 
			}
		};
		//shell.getShell().addListener(SWT.Activate, l);
		shell.getShell().addListener(SWT.Close, l);
	}
	
	private Formatters			formatters;
	
	private DHTStatsListener	dhtStatsListener;
	
	private Label				peerCount;
	private Label				taskCount;
	private Label				keysCount;
	private Label				itemsCount;
	private Label				sentPacketCount;
	private Label				receivedPacketCount;
	private Label				activeRPCCount;
	private Label				ourID;
	private Label				receivedBytesTotal;
	private Label				sentBytesTotal;
	private Label				receivedBytes;
	private Label				sentBytes;
	private Label				uptime;
	private Label				avgSentBytes;
	private Label				avgReceivedBytes;

	private Label				dhtRunStatus;
	private Label[][]			messageLabels;
	private Button				dhtStartStop;

	private Group				dhtStatsGroup;
	private Group				serverStatsGroup;
	private Group				messageStatsGroup;
	private RoutingTableCanvas	rtc;
	
	private void createDHTStatsGroup(Composite comp) {
		dhtStatsGroup = new Group(comp, SWT.None);
		Group grp = dhtStatsGroup;
		grp.setText("DHT Stats");

		GridLayout gl = new GridLayout(2, true);
		grp.setLayout(gl);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		grp.setLayoutData(gd);

		Label peerLabel = new Label(grp, SWT.None);
		peerLabel.setText("Peers in routing table:");

		peerCount = new Label(grp, SWT.None);
		peerCount.setText("0");

		Label taskLabel = new Label(grp, SWT.None);
		taskLabel.setText("Active Task:");

		taskCount = new Label(grp, SWT.None);
		taskCount.setText("0");

		Label dbKeysLabel = new Label(grp, SWT.None);
		dbKeysLabel.setText("Stored Keys:");

		keysCount = new Label(grp, SWT.None);
		keysCount.setText("0");

		Label dbItemsLabel = new Label(grp, SWT.None);
		dbItemsLabel.setText("Stored Items:");

		itemsCount = new Label(grp, SWT.None);
		itemsCount.setText("0");

		Label sentPacketsLabel = new Label(grp, SWT.None);
		sentPacketsLabel.setText("Sent Packets:");

		sentPacketCount = new Label(grp, SWT.None);
		sentPacketCount.setText("0");

		Label receivedPacketsLabel = new Label(grp, SWT.None);
		receivedPacketsLabel.setText("Received Packets:");

		receivedPacketCount = new Label(grp, SWT.None);
		receivedPacketCount.setText("0");

		Label rpcCallsLabel = new Label(grp, SWT.None);
		rpcCallsLabel.setText("Active Calls:");

		activeRPCCount = new Label(grp, SWT.None);
		activeRPCCount.setText("0");
	}
	
	private void createControlGroup(Composite comp) {
		Group grp = new Group(comp, SWT.None);
		grp.setText("DHT Control");

		GridLayout gl = new GridLayout(3, false);
		grp.setLayout(gl);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		grp.setLayoutData(gd);

		Label ourIDLabel = new Label(grp, SWT.None);
		ourIDLabel.setText("Our ID:");

		ourID = new Label(grp, SWT.None);
		ourID.setText("XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX");

		gd = new GridData();
		gd.horizontalSpan = 2;
		ourID.setLayoutData(gd);

		Label dhtStatusLabel = new Label(grp, SWT.None);
		dhtStatusLabel.setText("DHT Status:");

		dhtRunStatus = new Label(grp, SWT.None);
		
		dhtStartStop = new Button(grp, SWT.PUSH);
	}
	
	private void createRPCGroup (Composite comp) {
		serverStatsGroup = new Group(comp, SWT.None);
		Group grp = serverStatsGroup;
		grp.setText("Server Stats");

		GridLayout gl = new GridLayout(2, true);
		grp.setLayout(gl);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		grp.setLayoutData(gd);

		Label rBytesLabel = new Label(grp, SWT.None);
		rBytesLabel.setText("Received Total:");

		receivedBytesTotal = new Label(grp, SWT.None);

		Label sBytesLabel = new Label(grp, SWT.None);
		sBytesLabel.setText("Sent Total:");

		sentBytesTotal = new Label(grp, SWT.None);

		Label rBytesPSLabel = new Label(grp, SWT.None);
		rBytesPSLabel.setText("Received:");

		receivedBytes = new Label(grp, SWT.None);

		Label sBytesPSLabel = new Label(grp, SWT.None);
		sBytesPSLabel.setText("Sent:");

		sentBytes = new Label(grp, SWT.None);

		Label runningSinceLabel = new Label(grp, SWT.None);
		runningSinceLabel.setText("Uptime:");

		uptime = new Label(grp, SWT.None);

		Label avgRecLabel = new Label(grp, SWT.None);
		avgRecLabel.setText("Avg. Received:");

		avgReceivedBytes = new Label(grp, SWT.None);

		Label avgSentLabel = new Label(grp, SWT.None);
		avgSentLabel.setText("Avg. Sent:");

		avgSentBytes = new Label(grp, SWT.None);
	}
	
	private void createMessageStatsGroup (Composite comp) {
		messageStatsGroup = new Group(comp, SWT.None);
		Group grp = messageStatsGroup;
		grp.setText("Message Stats");

		messageLabels = new Label[4][5];

		GridLayout gl = new GridLayout(6, false);
		grp.setLayout(gl);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		grp.setLayoutData(gd);

		//empty label
		new Label(grp, SWT.None);

		Label sentLabel = new Label(grp, SWT.None);
		sentLabel.setText("Sent");
		gd = new GridData();
		gd.horizontalSpan = 3;
		sentLabel.setLayoutData(gd);

		Label receivedLabel = new Label(grp, SWT.None);
		receivedLabel.setText("Received");
		gd = new GridData();
		gd.horizontalSpan = 2;
		receivedLabel.setLayoutData(gd);

		//empty label
		new Label(grp, SWT.None);

		Label sentRequestLabel = new Label(grp, SWT.None);
		sentRequestLabel.setText("Requests");
		Label sentResponseLabel = new Label(grp, SWT.None);
		sentResponseLabel.setText("Responses");
		Label sentTimeoutLabel = new Label(grp, SWT.None);
		sentTimeoutLabel.setText("Timeouts");

		Label recRequestLabel = new Label(grp, SWT.None);
		recRequestLabel.setText("Requests");
		Label recResponseLabel = new Label(grp, SWT.None);
		recResponseLabel.setText("Responses");

		Label pingLabel = new Label(grp, SWT.None);
		pingLabel.setText("Ping:");
		for (int i = 0; i < messageLabels[Method.PING.ordinal()].length; i++) {
			messageLabels[Method.PING.ordinal()][i] = new Label(grp, SWT.None);
		}

		Label findNodeLabel = new Label(grp, SWT.None);
		findNodeLabel.setText("Find Node:");
		for (int i = 0; i < messageLabels[Method.PING.ordinal()].length; i++) {
			messageLabels[Method.FIND_NODE.ordinal()][i] = new Label(grp, SWT.None);
		}

		Label getPeersLabel = new Label(grp, SWT.None);
		getPeersLabel.setText("Get Peers:");
		for (int i = 0; i < messageLabels[Method.PING.ordinal()].length; i++) {
			messageLabels[Method.GET_PEERS.ordinal()][i] = new Label(grp, SWT.None);
		}

		Label announceLabel = new Label(grp, SWT.None);
		announceLabel.setText("Announce");
		for (int i = 0; i < messageLabels[Method.PING.ordinal()].length; i++) {
			messageLabels[Method.ANNOUNCE_PEER.ordinal()][i] = new Label(grp, SWT.None);
		}
	}
	
	private void createRoutingTableView(Composite comp) {
		/*
		 * ScrolledComposite sc = new ScrolledComposite(comp, SWT.H_SCROLL |
		 * SWT.V_SCROLL | SWT.BORDER);
		 */

		Composite sc = new Composite(comp, SWT.None);

		GridData gd = new GridData();
		gd.horizontalSpan = 2;

		sc.setLayoutData(gd);

		rtc = new RoutingTableCanvas(sc);
	}
	
	private void activate(final Display display) {
		
		dhtStatsListener = new DHTStatsListener() {
			
			private String TAG = this.getClass().getName();
			
			@Override
			public void statsUpdated(final DHTStats stats) {
				//Log.d(TAG, "statsUpdated() is called...");
				if (display != null && !display.isDisposed()) {
					display.asyncExec(new Runnable() {

						@Override
						public void run() {
							int numPeers = stats.getNumPeers();
							int numTasks = stats.getNumTasks();
							int keyCount = stats.getDbStats().getKeyCount();
							int itemCount = stats.getDbStats().getItemCount();
							int numSentPackets = stats.getNumSentPackets();
							int numReceivedPackets = stats.getNumReceivedPackets();
							int numRpcCalls = stats.getNumRpcCalls();
							
							//Log.d(TAG, "numPeers = " + numPeers);
							
							peerCount.setText(String.valueOf(stats.getNumPeers()));
							taskCount.setText(String.valueOf(stats.getNumTasks()));
							keysCount.setText(String.valueOf(stats.getDbStats().getKeyCount()));
							itemsCount.setText(String.valueOf(stats.getDbStats().getItemCount()));
							sentPacketCount.setText(String.valueOf(stats.getNumSentPackets()));
							receivedPacketCount.setText(String.valueOf(stats.getNumReceivedPackets()));
							activeRPCCount.setText(String.valueOf(stats.getNumRpcCalls()));
							
							RPCStats rpc = stats.getRpcStats();
							/*long receivedBytes = rpc.getReceivedBytes();
							long sentBytes = rpc.getSentBytes();
							long receivedBytesPerSec = rpc.getReceivedBytesPerSec();
							long sentBytesPerSec = rpc.getSentBytesPerSec();*/
							
							receivedBytesTotal.setText(formatters.formatByteCountToKiBEtc(rpc.getReceivedBytes()));
							sentBytesTotal.setText(formatters.formatByteCountToKiBEtc(rpc.getSentBytes()));
							receivedBytes.setText(formatters.formatByteCountToKiBEtcPerSec(rpc.getReceivedBytesPerSec()));
							sentBytes.setText(formatters.formatByteCountToKiBEtcPerSec(rpc.getSentBytesPerSec()));
							
							/*Log.d(TAG, "receivedBytes = " + receivedBytes);
							Log.d(TAG, "sentBytes = " + sentBytes);
							Log.d(TAG, "receivedBytesPerSec = " + receivedBytesPerSec);
							Log.d(TAG, "sentBytesPerSec = " + sentBytesPerSec);*/
							
							long uptimeSec = (System.currentTimeMillis() - stats.getStartedTimestamp()) / 1000;
							/*long avgReceivedBytes = rpc.getReceivedBytes() / uptimeSec;
							long avgSentBytes = rpc.getSentBytes() / uptimeSec;*/
							
							uptime.setText(formatters.formatTimeFromSeconds(uptimeSec));
							avgReceivedBytes.setText(formatters.formatByteCountToKiBEtcPerSec(rpc.getReceivedBytes() / uptimeSec));
							avgSentBytes.setText(formatters.formatByteCountToKiBEtcPerSec(rpc.getSentBytes() / uptimeSec));
							
							for (int i = 0; i < 4; i++) {
								Method m = Method.values()[i];
								Label[] messages = messageLabels[i];
								messages[0].setText(String.valueOf(rpc.getSentMessageCount(m, Type.REQ_MSG)));
								messages[1].setText(String.valueOf(rpc.getSentMessageCount(m, Type.RSP_MSG)));
								messages[2].setText(String.valueOf(rpc.getTimeoutMessageCount(m)));
								messages[3].setText(String.valueOf(rpc.getReceivedMessageCount(m, Type.REQ_MSG)));
								messages[4].setText(String.valueOf(rpc.getReceivedMessageCount(m, Type.RSP_MSG)));
							}
							
							dhtStatsGroup.layout();
							serverStatsGroup.layout();
							messageStatsGroup.layout();
							
							rtc.fullRepaint();
						}
						
					});
				}
			}
		};
		
		DHTtype type = DHTtype.IPV4_DHT;
		boolean isRunning = dhts.get(type).isRunning();
		Log.d(TAG, "isRunning = " + isRunning);
		if (dhts.get(type).isRunning()) {
			dhts.get(type).addStatsListener(dhtStatsListener);
			rtc.setNode(dhts.get(type).getNode());
		}
		updateDHTRunStatus();
	}
	
	private void updateDHTRunStatus() {
		
		DHTtype type = DHTtype.IPV4_DHT;
		boolean isRunning = dhts.get(type).isRunning();
		
		if (dhtRunStatus != null && !dhtRunStatus.isDisposed()) {
			dhtRunStatus.setText((dhts.get(type).isRunning()) ? "Running"
					: "Stopped");
		}

		if (dhtStartStop != null && !dhtStartStop.isDisposed()) {
			dhtStartStop.setText((dhts.get(type).isRunning()) ? "Stop"
					: "Start");
		}
		if (ourID != null && !ourID.isDisposed()) {
			ourID.setText((dhts.get(type).isRunning()) ?
					dhts.get(type).getOurID().toString()
					: "XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX");
		}
	}
	
	private void deactivate() {
		DHTtype type = DHTtype.IPV4_DHT;
		dhts.get(type).removeStatsListener(dhtStatsListener);
		rtc.setNode(null);
	}
	
	private void refresh() {
		
	}
}
