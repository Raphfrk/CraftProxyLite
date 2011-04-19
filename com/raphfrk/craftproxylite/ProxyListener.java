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

public class ProxyListener extends Thread {
	
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
			System.out.println( "Unable to bind to port");
			if( listener != null ) {
				try {
					listener.close();
				} catch (IOException e) {
					System.out.println( "Unable to close connection");
				}
			}
			return;
		} catch (IOException ioe) {
			System.out.println("Unknown error");	
			ioe.printStackTrace();
			if( listener != null ) {
				try {
					listener.close();
				} catch (IOException e) {
					System.out.println( "Unable to close connection");
				}
			}
			return;
		} 
		
		System.out.println("Server listening on port: " + port);
		
		while(isEnabled()) {
			
			Socket socket = null;
			try {
				socket = listener.accept();
			} catch (SocketTimeoutException ste ) {
				if( socket != null ) {
					System.out.println("Socket not null after timeout" );
				}
				continue;
			} catch (IOException e) {
				System.out.println("Error waiting for connection");
				e.printStackTrace();
				continue;
			}
			if(socket == null) {
				continue;
			}
			
			try {
				socket.setSoTimeout(1000);
			} catch (SocketException e) {
				System.out.println( "Unable to set timeout for socket");
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						System.out.println("Unable to close connection");
					}
					continue;
				}
				
			}
			
			String address = socket.getInetAddress().getHostAddress().toString();
			System.out.println("Connection from " + address);
			long currentTime = System.currentTimeMillis();
			Long lastConnect = lastLogin.get(address);
			boolean floodProtection = lastConnect != null && lastConnect + 5000 > currentTime;
			lastLogin.put(address, currentTime);
			if(floodProtection) {
				System.out.println("Disconnecting due to connect flood protection");
				try {
					DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
					//Protocol.kick(outputStream, "Only one connection is allowed per IP every 5 seconds");
					outputStream.flush();
					socket.close();
				} catch (IOException e) {
					System.out.println("Exception when closing connection");
				}
			} else {
				PassthroughConnection ptc = new PassthroughConnection(socket , defaultPort, password , port );
				ptc.start();
				addPassthroughConnection(ptc);
				
			}
			
			
		}
		
		
	}
	
	private boolean isEnabled() {
		synchronized(enableSync) {
			return enabled;
		}
	}
	
	public void kill() {
		synchronized(enableSync) {
			enabled = false;
		}
		boolean selfCall = Thread.currentThread().equals(this);
		System.out.println("Sent server kill signal");
		try {
			if(!selfCall) {
				this.join();
			}
		} catch (InterruptedException e) {
		}
		if((!selfCall) && this.isAlive()) {
			System.out.println("Server shutdown failed to complete correctly");
		} else if(selfCall) {
			System.out.println("Server exit command sent from server thread");
		} else {
			System.out.println("Server shutdown complete");
		}
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
	
	

}
