package com.raphfrk.craftproxylite;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyListener extends KillableThread {
	
	private final int port;
	private final int defaultPort;
	private final String password;
	
	private Object enableSync = new Object();
	private boolean enabled = true;
	
	LinkedList<PassthroughConnection> connections = new LinkedList<PassthroughConnection>();
	
	ProxyListener(int port, int defaultPort, String password) {
		this.port = port;
		this.defaultPort = defaultPort;
		this.password = password;
	}
	
	ConcurrentHashMap<String,Long> lastLogin = new ConcurrentHashMap<String,Long>();
	
	@Override
	public void run() {
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(port);
			listener.setSoTimeout(1000);
		} catch (BindException be) {
			Logging.log( "Unable to bind to port");
			if( listener != null ) {
				try {
					listener.close();
				} catch (IOException e) {
					Logging.log( "Unable to close connection");
				}
			}
			return;
		} catch (IOException ioe) {
			Logging.log("Unknown error");	
			ioe.printStackTrace();
			if( listener != null ) {
				try {
					listener.close();
				} catch (IOException e) {
					Logging.log( "Unable to close connection");
				}
			}
			return;
		} 
		
		Logging.log("Server listening on port: " + port);
		
		while(!killed()) {
			
			Socket socket = null;
			try {
				socket = listener.accept();
			} catch (SocketTimeoutException ste ) {
				if( socket != null ) {
					Logging.log("Socket not null after timeout" );
				}
				continue;
			} catch (IOException e) {
				Logging.log("Error waiting for connection");
				e.printStackTrace();
				continue;
			}
			if(socket == null) {
				continue;
			}
			
			try {
				socket.setSoTimeout(1000);
			} catch (SocketException e) {
				Logging.log( "Unable to set timeout for socket");
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						Logging.log("Unable to close connection");
					}
					continue;
				}
				
			}
			
			String address = socket.getInetAddress().getHostAddress().toString();
			int port = socket.getPort();
			Logging.log("Connection from " + address + "/" + port);
			long currentTime = System.currentTimeMillis();
			Long lastConnect = lastLogin.get(address);
			boolean floodProtection = lastConnect != null && lastConnect + 5000 > currentTime;
			lastLogin.put(address, currentTime);
			if(floodProtection) {
				Logging.log("Disconnecting due to connect flood protection");
				try {
					DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
					PacketFFKick.kick(outputStream, null, null, "Only one connection is allowed per IP every 5 seconds");
					outputStream.flush();
					socket.close();
				} catch (IOException e) {
					Logging.log("Exception when closing connection");
				}
			} else {
				PassthroughConnection ptc = new PassthroughConnection(socket , defaultPort, password , port );
				ptc.start();
				addPassthroughConnection(ptc);
				
			}
			
			
		}
		
		interruptConnections();		
		
	}
	
	void addPassthroughConnection(PassthroughConnection ptc) {
		Iterator<PassthroughConnection> itr = connections.iterator();
		
		while(itr.hasNext()) {
			if(!itr.next().isAlive()) {
				itr.remove();
			}
		}
		
		connections.add(ptc);
	}
	
	void interruptConnections() {
		Iterator<PassthroughConnection> itr = connections.iterator();
		
		while(itr.hasNext()) {
			PassthroughConnection ptc = itr.next();
			ptc.interrupt();
			try {
				ptc.join();
			} catch (InterruptedException e) {
				ptc.printLogMessage("Unable to break connection");
				kill();
			}
		}
	}

}
