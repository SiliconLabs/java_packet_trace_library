package com.silabs.pti.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Various static network utilities.
 *
 * Created: long time ago.
 * @author Ezra, Timotej
 */
public class NetworkUtils {

	private static long networkInterfaceRefreshInterval = 5 * 1000; // ms
	private static long nextNetworkInterfaceFetch = 0;
	private static NetworkInterface[] lastNetworkInterfaces;

	/**
	 * It can be SLLOOOOOW to query these, so cache the results for a reasonable
	 * window of time
	 */
	private static NetworkInterface[] getRecentNetworkInterfaces() {
		long now = System.currentTimeMillis();
		if (now >= nextNetworkInterfaceFetch) {
			try {
				List<NetworkInterface> list = new ArrayList<>();
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
					list.add(en.nextElement());
				}
				lastNetworkInterfaces = list.toArray(new NetworkInterface[list.size()]);
			} catch (SocketException e) {
				if (lastNetworkInterfaces == null)
					lastNetworkInterfaces = new NetworkInterface[0];
			}
			nextNetworkInterfaceFetch = now + networkInterfaceRefreshInterval;
		}
		return lastNetworkInterfaces;
	}

	/**
	 * Set the network interface refresh interval. This class caches the results
	 * of a call to native "get all interfaces" call. This method sets
	 * the duration for how long before the cache expires.
	 *
	 * Default value is 5000 ms.
	 *
	 * It should be mostly OK, but there was a case discovered, where the
	 * call to get all interfaces takes over a second, essentially locking up
	 * network for a bit. It's an odd case (VPN + Virtual Machine, etc), but it
	 * exists, so we're moving that to the preference.
	 *
	 * Anyone who doesn't touch the preference will not be affected.
	 *
	 * see MCUDT-10888
	 *
	 * Preference will be implemented in the discovery preferences UI.
	 *
	 * @since 4.3
	 */
	public static void setNetworkInterfaceRefreshInterval(final long interval) {
	  networkInterfaceRefreshInterval = interval;
	}

	/**
	 * We need a getter, so that preference page can show it.
	 *
	 * @since 4.3
	 * @return long
	 */
	public static long networkInterfaceRefreshInterval() {
	  return networkInterfaceRefreshInterval;
	}

	/**
	 * Returns the mac addresses of this PC.
	 *
	 * @return {@link List}
	 */
	public static List<byte[]> getMacAddresses()
			throws SocketException {
		List<byte[]> list = new ArrayList<>();
		for (NetworkInterface ni : getRecentNetworkInterfaces()) {
			byte[] mac = ni.getHardwareAddress();
			if (mac == null)
				continue;
			if (!list.contains(mac))
				list.add(mac);
		}
		return list;
	}

	/**
	 * This method returns true if a given address is my own address.
	 * Method works with IPv4 addresses only and doesn't perform any hostname
	 * resolution whatsoever.
	 *
	 * If method returns true, answer is definite. If method returns false,
	 * it's either definite or some error happened and it inconclusive.
	 *
	 * @param ipAddress IP Address in textual form.
	 * @return boolean
	 */
	public static boolean isMyAddress(final String ipAddress) {
		if ( ipAddress == null )
			return false;
		for (NetworkInterface ni : getRecentNetworkInterfaces()) {
			Enumeration<InetAddress> addrs = ni.getInetAddresses();
			while ( addrs.hasMoreElements() ) {
				InetAddress ia = addrs.nextElement();
				if ( ipAddress.equals(ia.getHostAddress()))
					return true;
			}
		}
		return false;
	}

	/**
	 * This method returns a list of all non-loopback active local addresses.
	 * If you are looking for a "default" address, then the default address will
	 * be the first element in the list, if list is non-empty.
	 *
	 * @return list of IP addresses.
	 */
	public static List<InetAddress> getIpAddresses() {
		List<InetAddress> addresses = new ArrayList<>();
		try {
			for (NetworkInterface ni : getRecentNetworkInterfaces()) {
				if ( !ni.isUp() )
					continue;
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while ( addrs.hasMoreElements() ) {
					InetAddress ia = addrs.nextElement();
					if ( ia.isLoopbackAddress() )
						continue;
					if ( !addresses.contains(ia) )
						addresses.add(ia);
				}
			}
		} catch (SocketException se ) {
			// Whatever. We can't get addresess, we won't return them.
		}
		return addresses;
	}
}
