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
	}
	
	public void run() {
		
		LocalSocket clientSocket = new LocalSocket(socketToClient, this);
				
		if(!clientSocket.success) {
			printError("Unable to open data streams for client socket");
			return;
		}
		
		int portnum = defaultPort;
		
		Socket serverBasicSocket = LocalSocket.openSocket("localhost", portnum, this);
		if(serverBasicSocket == null) {
			printError("Unable to open connection to backend server");
			PacketFFKick.kick(clientSocket.out, this, "Unable to connect to backend server");
			LocalSocket.closeSocket(clientSocket.socket, this);
			return;
		}
		LocalSocket serverSocket = new LocalSocket(serverBasicSocket, this);
		
				
	}
	
	void printError(String message) {
		String username = clientInfo.getUsername();
		if(username == null) {
			System.out.println(clientInfo.getIP() + ": " + message);
		} else {
			System.out.println(clientInfo.getIP() + " (" + username + "): " + message);
		}
	}
	
}
