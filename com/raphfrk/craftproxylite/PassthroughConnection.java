package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PassthroughConnection extends Thread {
	
	boolean enabled = true;
	
	private final Socket socketToClient;
	private final int defaultPort;
	private final String password;
	private final int listenPort;
	protected final ClientInfo clientInfo;
	
	protected DataInputStream inputFromClient = null;
	protected DataOutputStream outputToClient = null;
	
	protected DataInputStream inputFromServer = null;
	protected DataOutputStream outputToServer = null;
	
	protected boolean kickMessageSent = false;

	PassthroughConnection(Socket socketToClient, int defaultPort, String password , int listenPort) {
		this.socketToClient = socketToClient;
		this.defaultPort = defaultPort;
		this.password = password;
		this.listenPort = listenPort;
		this.clientInfo = new ClientInfo();
		this.clientInfo.setIP(socketToClient.getInetAddress().getHostAddress());
		this.clientInfo.setPort(socketToClient.getPort());
	}
	
	public void run() {
		
		LocalSocket clientSocket = new LocalSocket(socketToClient, this);
				
		if(!clientSocket.success) {
			printLogMessage("Unable to open data streams for client socket");
			return;
		}
		
		int portnum = defaultPort;
		
		Socket serverBasicSocket = LocalSocket.openSocket("localhost", portnum, this);
		if(serverBasicSocket == null) {
			printLogMessage("Unable to open connection to backend server");
			PacketFFKick.kick(clientSocket.out, this, "Unable to connect to backend server");
			LocalSocket.closeSocket(clientSocket.socket, this);
			return;
		}
		LocalSocket serverSocket = new LocalSocket(serverBasicSocket, this);
		if(!serverSocket.success) {
			printLogMessage("Unable to open data streams to backend server");
			PacketFFKick.kickAndClose(clientSocket, this, "Unable to connect to backend server");
			return;
		}
		
		DataStreamBridge serverToClient = new DataStreamBridge(serverSocket.in, clientSocket.out, this);
		DataStreamBridge clientToServer = new DataStreamBridge(clientSocket.in, serverSocket.out, this);
		
		serverToClient.start();
		clientToServer.start();
		
		try {
			clientToServer.join();
			serverToClient.join();
		} catch (InterruptedException ie) {
		}
		
		if(!getKickMessageSent()) {
			PacketFFKick.kick(clientSocket.out, this, "Connection closed");
		}
		
		printLogMessage("Closing connection to server");
		LocalSocket.closeSocket(serverSocket.socket, this);
		
		printLogMessage("Closing connection to client");
		LocalSocket.closeSocket(clientSocket.socket, this);
		
	}
	
	synchronized void printLogMessage(String message) {
		String username = clientInfo.getUsername();
		if(username == null) {
			System.out.println(clientInfo.getIP() + "/" + clientInfo.getPort() + ": " + message);
		} else {
			System.out.println(clientInfo.getIP() + "/" + clientInfo.getPort() + " (" + username + "): " + message);
		}
	}
	
	synchronized void setKickMessageSent(boolean kickMessageSent) {
		this.kickMessageSent = kickMessageSent;
	}
	
	synchronized boolean getKickMessageSent() {
		return kickMessageSent;
	}
	
	synchronized boolean testEnabled() {
		return enabled;
	}
	
	synchronized void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}
