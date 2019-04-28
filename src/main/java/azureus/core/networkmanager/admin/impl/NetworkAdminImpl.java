package azureus.core.networkmanager.admin.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import azureus.core.networkmanager.admin.NetworkAdmin;

public class NetworkAdminImpl extends NetworkAdmin {

	private static InetAddress anyLocalAddress;
	private static InetAddress anyLocalAddressIPv4;
	private static InetAddress anyLocalAddressIPv6;
	private static InetAddress localhostV4;
	private static InetAddress localhostV6;

	static {
		try {
			anyLocalAddressIPv4 	= InetAddress.getByAddress(new byte[] { 0,0,0,0 });
			anyLocalAddressIPv6  	= InetAddress.getByAddress(new byte[] {0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0});
			anyLocalAddress			= new InetSocketAddress(0).getAddress();
			localhostV4 = InetAddress.getByAddress(new byte[] {127,0,0,1});
			localhostV6 = InetAddress.getByAddress(new byte[] {0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,1});
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	private InetAddress[]	currentBindIPs			= new InetAddress[] { null };
	private boolean			forceBind				= false;
	private boolean			supportsIPv6withNIO		= true;
	private boolean			supportsIPv6 = true;
	private boolean			supportsIPv4 = true;
	
	@Override
	public InetAddress[] getAllBindAddresses(boolean includeWildcard) {
		return new InetAddress[] {anyLocalAddressIPv4};
	}

}
