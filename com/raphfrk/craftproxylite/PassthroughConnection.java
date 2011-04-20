package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PassthroughConnection extends KillableThread {

	private final Socket socketToClient;
	private final int defaultPort;
	private final String password;
	private final int listenPort;
	protected final ClientInfo clientInfo;

	private boolean forward = true;

	private Object enabledSync = new Object();
	private boolean enabled = true;

	private KillableThread serverToClient = null;
	private KillableThread clientToServer = null;

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

		boolean connected = true;

		LocalSocket clientSocket = new LocalSocket(socketToClient, this);

		if(!clientSocket.success) {
			printLogMessage("Unable to open data streams for client socket");
			return;
		}

		String kickMessage = Packet01Login.processLogin(clientSocket.in, clientSocket.out, this, this, Globals.isAuth());

		if(kickMessage != null) {
			printLogMessage(kickMessage);
			PacketFFKick.kick(clientSocket.out, this, this, kickMessage);
			connected = false;
		}

		String hostname = "localhost";
		int portnum = defaultPort;
		boolean firstConnection = true;

		while(connected && !killed()) {
			Socket serverBasicSocket = LocalSocket.openSocket(hostname, portnum, this);
			if(serverBasicSocket == null) {
				printLogMessage("Unable to open connection to backend server");
				PacketFFKick.kick(clientSocket.out, this, this, "Unable to connect to backend server");
				LocalSocket.closeSocket(clientSocket.socket, this);
				return;
			}
			LocalSocket serverSocket = new LocalSocket(serverBasicSocket, this);
			if(!serverSocket.success) {
				printLogMessage("Unable to open data streams to backend server");
				PacketFFKick.kickAndClose(clientSocket, this, this, "Unable to connect to backend server");
				return;
			}

			kickMessage = Packet01Login.serverLogin(serverSocket.in, serverSocket.out, this, this);
			if(kickMessage != null) {
				printLogMessage(kickMessage);
				PacketFFKick.kick(clientSocket.out, this, this, kickMessage);
				connected = false;
			}

			if(connected) {
				if(forward) {
					serverToClient = new DataStreamBridge(serverSocket.in, clientSocket.out, this);
					clientToServer = new DataStreamBridge(clientSocket.in, serverSocket.out, this);
				}

				boolean localEnabled;
				synchronized(enabledSync) {
					localEnabled = enabled;
					if(enabled) {
						serverToClient.start();
						clientToServer.start();
					}
				}

				if(localEnabled) {
					while((clientToServer.isAlive() || serverToClient.isAlive())) {
						try {
							clientToServer.join(500);
							serverToClient.join(500);
						} catch (InterruptedException ie) {
							kill();
						}
						if(killed() || (!(clientToServer.isAlive() && serverToClient.isAlive()))) {
							clientToServer.interrupt();
							serverToClient.interrupt();
						}
					}
				}
			}

			if(!getKickMessageSent()) {
				PacketFFKick.kick(clientSocket.out, this, this, "Connection closed");
			}


			printLogMessage("Closing connection to server");
			LocalSocket.closeSocket(serverSocket.socket, this);
		}

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

	synchronized boolean sendingKickMessage() {
		if(!kickMessageSent) {
			kickMessageSent = true;
			return true;
		} else {
			return false;
		}
	}

	synchronized boolean getKickMessageSent() {
		return kickMessageSent;
	}

	public void interrupt() {
		synchronized(enabledSync) {
			enabled = false;
		}
		if(clientToServer != null) {
			clientToServer.interrupt();
		}
		if(serverToClient != null) {
			serverToClient.interrupt();
		}
	}



}
