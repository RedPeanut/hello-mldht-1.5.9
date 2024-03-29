/*
 *    This file is part of mlDHT. 
 * 
 *    mlDHT is free software: you can redistribute it and/or modify 
 *    it under the terms of the GNU General Public License as published by 
 *    the Free Software Foundation, either version 2 of the License, or 
 *    (at your option) any later version. 
 * 
 *    mlDHT is distributed in the hope that it will be useful, 
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *    GNU General Public License for more details. 
 * 
 *    You should have received a copy of the GNU General Public License 
 *    along with mlDHT.  If not, see <http://www.gnu.org/licenses/>. 
 */
package lbms.plugins.mldht.kad.messages;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;

import gudy.azureus2.core3.util.BEncoder;
import hello.util.Util;
import lbms.plugins.mldht.kad.DHT;
import lbms.plugins.mldht.kad.DHTConstants;
import lbms.plugins.mldht.kad.Key;
import lbms.plugins.mldht.kad.RPCServer;

/**
 * Base class for all RPC messages.
 *
 * @author Damokles
 */
public abstract class MessageBase {
	
	public static final String	VERSION_KEY = "v";
	public static final String	TRANSACTION_KEY = "t";

	protected byte[]			mtid;
	protected Method			method;
	protected Type				type;
	protected Key				id;
	protected InetSocketAddress	origin;
	protected String			version;
	protected RPCServer			srv;

	public MessageBase(byte[] mtid, Method m, Type type) {
		this.mtid = mtid;
		this.method = m;
		this.type = type;
	}

	/**
	 * When this message arrives this function will be called upon the DHT.
	 * The message should then call the appropriate DHT function (double dispatch)
	 * @param dht Pointer to DHT
	 */
	public void apply(DHT dht) {
	}

	/**
	 * BEncode the message.
	 * @return Data array
	 */
	public byte[] encode() throws IOException {
		return BEncoder.encode(getBase());
	}
	
	public Map<String, Object> getBase() {
		Map<String, Object> base = new TreeMap<String, Object>();
		Map<String, Object> inner = getInnerMap();
		if (inner != null)
			base.put(getType().innerKey(), inner);
		
		// transaction ID
		base.put(TRANSACTION_KEY, mtid);
		// version
		base.put(VERSION_KEY, DHTConstants.getVersion());
	
		// message type
		base.put(Type.TYPE_KEY, getType().getRPCTypeName());
		// message method if we're a request
		if (getType() == Type.REQ_MSG)
			base.put(getType().getRPCTypeName(), getMethod().getRPCName());

		return base;
	}
	
	public Map<String, Object> getInnerMap() {
		return null;
	}


	/// Set the origin (i.e. where the message came from)
	public void setOrigin(InetSocketAddress o) {
		origin = o;
	}

	/// Get the origin
	public InetSocketAddress getOrigin() {
		return origin;
	}

	/// Set the origin (i.e. where the message came from)
	public void setDestination(InetSocketAddress o) {
		origin = o;
	}

	/// Get the origin
	public InetSocketAddress getDestination() {
		return origin;
	}

	/// Get the MTID
	public byte[] getMTID() {
		return mtid;
	}

	/// Set the MTID
	public void setMTID(byte[] m) {
		mtid = m;
	}

	public void setMTID(short m) {
		mtid = new byte[] {(byte)(m>>8),(byte)(m&0xff)};
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setServer(RPCServer srv) {
		this.srv = srv;
	}

	public RPCServer getServer() {
		return srv;
	}

	public void setID(Key id) {
		this.id = id;
	}

	/// Get the id of the sender
	public Key getID() {
		return id;
	}

	/// Get the type of the message
	public Type getType() {
		return type;
	}

	/// Get the message it's method
	public Method getMethod() {
		return method;
	}
	
	@Override
	public String toString() {
		return " Method:" + method + " Type:" + type + " Key:" + id.toString(false) + " MessageID:" + new String(Util.toHexString(mtid))+(version != null ? " version:"+version : "")+"  ";
	}

	public void print() {
		Util.printMap("", getBase(), "", "", false);
	}
	
	public static enum Type {
		REQ_MSG {
			String innerKey() {	return "a";	}
			String getRPCTypeName() { return "q"; }
		}, RSP_MSG {
			String innerKey() {	return "r";	}
			String getRPCTypeName() { return "r"; }
		}, ERR_MSG {
			String getRPCTypeName() { return "e"; }
			String innerKey() {	return "e";	}
		}, INVALID;
		
		String innerKey() {
			return null;
		}
		
		String getRPCTypeName()	{
			return null;
		}
		
		public static final String TYPE_KEY = "y";
	};

	public static enum Method {
		PING, FIND_NODE, GET_PEERS, ANNOUNCE_PEER, NONE;
		
		String getRPCName()	{
			return name().toLowerCase();						
		}
	};
}
