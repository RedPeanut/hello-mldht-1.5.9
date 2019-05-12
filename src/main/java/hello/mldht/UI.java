package hello.mldht;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import gudy.azureus2.core3.util.AESemaphore;
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
	
	Launcher launcher;
	Map<DHTtype, DHT> dhts;
	Display display;
	
	public UI(Launcher launcher, Map<DHTtype, DHT> dhts) {
		this.launcher = launcher;
		this.dhts = dhts;
	}
	
	public static boolean isMac() {
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
			return true;
		return false;
	}
	
	public void start() throws Exception {
		
		if (isMac()) {
			String platform = SWT.getPlatform();
			if (platform.equals("carbon")) {
				
			} else if (platform.equals("cocoa")) {
				//CocoaUIEnhancer enhancer = new CocoaUIEnhancer();
				//enhancer.hookApplicationMenu();
			}
		}
		
		formatters = new Formatters();
		
		/*Display*/ display = new Display();
		
		Shell shell = new Shell(display);
		shell.setText("Hello, world!");
		//shell.setLayout(new FormLayout());
		
		createMenus(shell);
		initUI(shell);
		addListener(shell);
		activate(/*display*/);
		
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
	
	public static final String KEY_MENU_ID = "key.menu.id";
	public static final String MENU_ID_FILE = "MainWindow.menu.file";
	
	private void createMenus(Shell shell) {
		
		Menu menuBar = new Menu(shell, SWT.BAR);
		
		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("파일(&F)");
		
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);
		
		MenuItem fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.addListener(SWT.Selection,
			new Listener() {
				@Override
				public void handleEvent(Event event) {
					System.out.println("handleEvent() is called...");
				}
			}
		);
		fileOpenItem.setText("열기(&O)");
		
		// 토렌트 파일...
		// URL, 마그넷 또는 해쉬...
		
		shell.setMenuBar(menuBar);
	}
	
	private void addListener(Composite shell) {
		Listener l = new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.Close) {
					Log.d(TAG, "SWT.Close is occured...");
					
				} else if (event.type == SWT.Deactivate) {
					Log.d(TAG, "SWT.Deactivate is occured...");
					
				} else if (event.type == SWT.Dispose) {
					Log.d(TAG, "SWT.Dispose is occured...");
					launcher.stopDHT();
					deactivate();
				}
			}
		};
		//shell.getShell().addListener(SWT.Activate, l);
		shell.getShell().addListener(SWT.Close, l);
		shell.getShell().addListener(SWT.Deactivate, l);
		shell.getShell().addListener(SWT.Dispose, l);
	}
	
	private Control getOne(TabFolder tabFolder) {
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		
		//panel = new Composite(parent, SWT.NULL);
		
		GridLayout gl = new GridLayout();
		composite.setLayout(gl);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		final ScrolledComposite scrollComposite = new ScrolledComposite(composite,
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
		
		return composite;
	}
	
	private Control getTwo(TabFolder tabFolder) {
		
		Composite comp = new Composite(tabFolder, SWT.NONE);
		
		GridLayout gridLayout = new GridLayout();
		comp.setLayout(gridLayout);
		comp.setLayoutData(new GridData());
		
		Group group = new Group(comp, SWT.None);
		group.setText("DHT Control");
		
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		group.setLayout(fillLayout);
		group.setLayoutData(new GridData());
		
		Button button0 = new Button(group, SWT.PUSH);
		button0.setText("button0");
		button0.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				
			}
		});
		
		Button button1 = new Button(group, SWT.PUSH);
		button1.setText("button1");

		Button button2 = new Button(group, SWT.PUSH);
		button2.setText("button2");

		Button button3 = new Button(group, SWT.PUSH);
		button3.setText("button3");
		
		/*GridLayout gl = new GridLayout(2, false);
		comp.setLayout(gl);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		Group grp = new Group(comp, SWT.None);
		grp.setText("1번째 그룹...");
		
		grp = new Group(comp, SWT.None);
		grp.setText("2번째 그룹...");
		
		grp = new Group(comp, SWT.None);
		grp.setText("3번째 그룹...");
		
		grp = new Group(comp, SWT.None);
		grp.setText("4번째 그룹...");*/
		
		return comp;
	}
	//Composite panel;
	
	private void initUI(Composite shell) {
		
		//shell.setLayout(new FillLayout());
		shell.setLayout(new FormLayout());
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);

	    TabItem one = new TabItem(tabFolder, SWT.NONE);
	    one.setText("DHTView");
	    one.setControl(getOne(tabFolder));
		
	    TabItem two = new TabItem(tabFolder, SWT.NONE);
	    two.setText("ControlView");
	    two.setControl(getTwo(tabFolder));
	    
	    //tabFolder.setSelection(0);
	    
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
	private Button				dhtStartStop;
	private Label				dhtUpdateScheduleStatus;
	private Button				dhtUpdateScheduleStartStop;
	private Label				expiredEntriesScheduleStatus;
	private Button				expiredEntriesScheduleStartStop;
	private Label				lookupScheduleStatus;
	private Button				lookupScheduleStartStop;
	
	private Label[][]			messageLabels;

	private Group				dhtStatsGroup;
	private Group				dhtControlGroup;
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
		//Group grp = new Group(comp, SWT.None);
		Group grp = dhtControlGroup = new Group(comp, SWT.None);
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
		dhtStartStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				DHTtype type = DHTtype.IPV4_DHT;
				
				if (dhts.get(type).isRunning()) {
					launcher.stopDHT();
					deactivate();
				} else {
					launcher.startDHT();
					activate();
				}
			}
		});
		
		Label dhtUpdateScheduleLabel = new Label(grp, SWT.None);
		dhtUpdateScheduleLabel.setText("DHT Update Schedule Status:");
		
		dhtUpdateScheduleStatus = new Label(grp, SWT.None);
		
		dhtUpdateScheduleStartStop = new Button(grp, SWT.PUSH);
		dhtUpdateScheduleStartStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				DHTtype type = DHTtype.IPV4_DHT;
				if (dhts.get(type).isDhtUpdateScheduleActive())
					dhts.get(type).stopDhtUpdateSchedule();
				else
					dhts.get(type).startDhtUpdateSchedule();
				updateDHTRunStatus();
			}
		});
		
		Label expiredEntriesScheduleLabel = new Label(grp, SWT.None);
		expiredEntriesScheduleLabel.setText("Expired Entries Schedule Status:");
		
		expiredEntriesScheduleStatus = new Label(grp, SWT.None);
		
		expiredEntriesScheduleStartStop = new Button(grp, SWT.PUSH);
		expiredEntriesScheduleStartStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				DHTtype type = DHTtype.IPV4_DHT;
				if (dhts.get(type).isExpiredEntriesScheduleActive())
					dhts.get(type).stopExpiredEntriesSchedule();
				else
					dhts.get(type).startExpiredEntriesSchedule();
				updateDHTRunStatus();
			}
		});
		
		Label lookupScheduleLabel = new Label(grp, SWT.None);
		lookupScheduleLabel.setText("Lookup Schedule Status:");
		
		lookupScheduleStatus = new Label(grp, SWT.None);
		
		lookupScheduleStartStop = new Button(grp, SWT.PUSH);
		lookupScheduleStartStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				DHTtype type = DHTtype.IPV4_DHT;
				if (dhts.get(type).isLookupScheduleActive())
					dhts.get(type).stopLookupSchedule();
				else
					dhts.get(type).startLookupSchedule();
				updateDHTRunStatus();
			}
		});
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
	
	private void activate(/*final Display display*/) {
		
		dhtStatsListener = new DHTStatsListener() {
			
			private String TAG = this.getClass().getName();
			
			@Override
			public void statsUpdated(final DHTStats stats) {
				//Log.d(TAG, "statsUpdated() is called...");
				if (display != null && !display.isDisposed()) {
					display.asyncExec(new Runnable() {

						@Override
						public void run() {
							
							peerCount.setText(String.valueOf(stats.getNumPeers()));
							taskCount.setText(String.valueOf(stats.getNumTasks()));
							keysCount.setText(String.valueOf(stats.getDbStats().getKeyCount()));
							itemsCount.setText(String.valueOf(stats.getDbStats().getItemCount()));
							sentPacketCount.setText(String.valueOf(stats.getNumSentPackets()));
							receivedPacketCount.setText(String.valueOf(stats.getNumReceivedPackets()));
							activeRPCCount.setText(String.valueOf(stats.getNumRpcCalls()));
							
							//dhtUpdateScheduleStatus.setText();
							
							RPCStats rpc = stats.getRpcStats();
							
							receivedBytesTotal.setText(formatters.formatByteCountToKiBEtc(rpc.getReceivedBytes()));
							sentBytesTotal.setText(formatters.formatByteCountToKiBEtc(rpc.getSentBytes()));
							receivedBytes.setText(formatters.formatByteCountToKiBEtcPerSec(rpc.getReceivedBytesPerSec()));
							sentBytes.setText(formatters.formatByteCountToKiBEtcPerSec(rpc.getSentBytesPerSec()));
							
							long uptimeSec = (System.currentTimeMillis() - stats.getStartedTimestamp()) / 1000;
							
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
							//dhtControlGroup.layout();
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
		
		if (ourID != null && !ourID.isDisposed()) {
			ourID.setText((dhts.get(type).isRunning()) ?
					dhts.get(type).getOurID().toString()
					: "XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX");
		}
		
		if (dhtRunStatus != null && !dhtRunStatus.isDisposed()) {
			dhtRunStatus.setText((dhts.get(type).isRunning()) ? "Running"
					: "Stopped");
		}

		if (dhtStartStop != null && !dhtStartStop.isDisposed()) {
			dhtStartStop.setText((dhts.get(type).isRunning()) ? "Stop"
					: "Start");
		}
		
		if (dhtUpdateScheduleStatus != null && !dhtUpdateScheduleStatus.isDisposed()) {
			dhtUpdateScheduleStatus.setText((dhts.get(type).isDhtUpdateScheduleActive()) ? "Active"
					: "Inactive");
		}
		
		if (dhtUpdateScheduleStartStop != null && !dhtUpdateScheduleStartStop.isDisposed()) {
			dhtUpdateScheduleStartStop.setText((dhts.get(type).isDhtUpdateScheduleActive()) ? "Stop"
					: "Start");
		}
		
		if (expiredEntriesScheduleStatus != null && !expiredEntriesScheduleStatus.isDisposed()) {
			expiredEntriesScheduleStatus.setText((dhts.get(type).isExpiredEntriesScheduleActive()) ? "Active"
					: "Inactive");
		}
		
		if (expiredEntriesScheduleStartStop != null && !expiredEntriesScheduleStartStop.isDisposed()) {
			expiredEntriesScheduleStartStop.setText((dhts.get(type).isExpiredEntriesScheduleActive()) ? "Stop"
					: "Start");
		}
		
		if (lookupScheduleStatus != null && !lookupScheduleStatus.isDisposed()) {
			lookupScheduleStatus.setText((dhts.get(type).isLookupScheduleActive()) ? "Active"
					: "Inactive");
		}
		
		if (lookupScheduleStartStop != null && !lookupScheduleStartStop.isDisposed()) {
			lookupScheduleStartStop.setText((dhts.get(type).isLookupScheduleActive()) ? "Stop"
					: "Start");
		}
		
		dhtControlGroup.layout();
		
	}
	
	private void deactivate() {
		DHTtype type = DHTtype.IPV4_DHT;
		dhts.get(type).removeStatsListener(dhtStatsListener);
		rtc.setNode(null);
	}
	
	private void refresh() {
		
	}
}
