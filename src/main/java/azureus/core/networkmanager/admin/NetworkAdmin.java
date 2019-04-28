package azureus.core.networkmanager.admin;

import java.net.InetAddress;

import azureus.core.networkmanager.admin.impl.NetworkAdminImpl;

public abstract class NetworkAdmin {
	private static NetworkAdmin	singleton;
	public static synchronized NetworkAdmin getSingleton() {
		if (singleton == null) {
			singleton = new NetworkAdminImpl();
		}
		return (singleton);
	}
	public abstract InetAddress[] getAllBindAddresses(boolean includeWildcard);
}
