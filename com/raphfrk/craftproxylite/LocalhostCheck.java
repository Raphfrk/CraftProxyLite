package com.raphfrk.craftproxylite;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class LocalhostCheck {

	// From: http://stackoverflow.com/questions/2406341/how-to-check-if-an-ip-address-is-the-local-host-on-a-multi-homed-system
	
	static boolean isThisMyIpAddress(String hostname) {
	try {
		InetAddress addr = InetAddress.getByName(hostname);
		return isThisMyIpAddress(addr);
	} catch (UnknownHostException e) {
		return false;
	}
	}
	
	public static boolean isThisMyIpAddress(InetAddress addr) {
	    // Check if the address is a valid special local or loop back
	    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
	        return true;

	    // Check if the address is defined on any interface
	    try {
	        return NetworkInterface.getByInetAddress(addr) != null;
	    } catch (SocketException e) {
	        return false;
	    }
	}
	
}
