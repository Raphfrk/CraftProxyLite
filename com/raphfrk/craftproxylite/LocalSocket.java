package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class LocalSocket {

	public final boolean success;

	public final DataInputStream in;
	public final DataOutputStream out;
	public final Socket socket;
	public final PassthroughConnection ptc;

	public static Socket openSocket(String hostname, int port, PassthroughConnection ptc) {
		
		Socket socket = null;

		try {
			if(hostname.trim().startsWith("localhost") && Globals.varyLocalhost()) {
				String fakeLocalIP = LocalhostIPFactory.getNextIP();
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Connecting to: " + hostname + ":" + port + " from " + fakeLocalIP );
				}
				socket = new Socket(hostname, port, InetAddress.getByName(fakeLocalIP), 0);
			} else {
				socket = new Socket(hostname, port);
			}				
		} catch (UnknownHostException e) {
			ptc.printLogMessage("Unknown hostname: " + hostname);
			return null;
		} catch (IOException e) {
			ptc.printLogMessage("Unable to open socket to " + hostname + ":" + port);
			return null;
		}
		try {
			socket.setSoTimeout(1000);
		} catch (SocketException e) {
			ptc.printLogMessage("Unable to set socket timeout");
			return null;
		}

		return socket;

	}

	public static boolean closeSocket(Socket socket, PassthroughConnection ptc) {

		try {
			socket.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	LocalSocket(Socket socket, PassthroughConnection ptc) {
		this.ptc = ptc;
		this.socket = socket;
		DataInputStream inLocal = null;
		DataOutputStream outLocal = null;
		try {
			inLocal = new DataInputStream( socket.getInputStream() );
		} catch (IOException e) {
			ptc.printLogMessage("Unable to open data stream to client");
			if( inLocal != null ) {
				try {
					inLocal.close();
					socket.close();
				} catch (IOException e1) {
					ptc.printLogMessage("Unable to close data stream to client");
				}
			}
			in = null;
			out = null;
			success = false;
			return;
		}

		try {
			outLocal = new DataOutputStream( socket.getOutputStream() );
		} catch (IOException e) {
			ptc.printLogMessage("Unable to open data stream from client");
			if( outLocal != null ) {
				try {
					outLocal.close();
					socket.close();
				} catch (IOException e1) {
					ptc.printLogMessage("Unable to close data stream from client");
				}
			}
			in = null;
			out = null;
			success = false;
			return;
		}
		in = inLocal;
		out = outLocal;
		success = true;
	}

}
