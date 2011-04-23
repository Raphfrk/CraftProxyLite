package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;

public class PassthroughConnection extends KillableThread {

	private final Socket socketToClient;
	private final int defaultPort;
	private final String password;
	private final int listenPort;
	protected final ClientInfo clientInfo;
	
	private Object holdingSync = new Object(); // not really required
	private short holding = 0;
	
	DateFormat shortTime = DateFormat.getTimeInstance(DateFormat.MEDIUM);

	private boolean forward = false;

	private Object enabledSync = new Object();
	private boolean enabled = true;

	private KillableThread serverToClient = null;
	private KillableThread clientToServer = null;

	protected DataInputStream inputFromClient = null;
	protected DataOutputStream outputToClient = null;

	protected DataInputStream inputFromServer = null;
	protected DataOutputStream outputToServer = null;

	protected boolean kickMessageSent = false;
	
	private Object redirectSync = new Object();
	private String redirect = null;

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

		String hostname = ReconnectCache.get(clientInfo.getUsername());
		int portnum = ReconnectCache.getPort(hostname, defaultPort);
		hostname = ReconnectCache.getHost(hostname, "localhost");

		boolean firstConnection = true;

		while(connected && !killed()) {
			
			String redirectLocal = getRedirect();
			setRedirect(null);
			
			if(redirectLocal != null) {
				String[] split = redirectLocal.split(":");
				if(split.length != 2) {
					printLogMessage("Error with redirect string");
					connected = false;
				} else {
					hostname = split[0];
					try {
						portnum = Integer.parseInt(split[1]);
					} catch (NumberFormatException nfe) {
						printLogMessage("Error with processing port number");
						connected = false;
					}
				}
			}
			
			printLogMessage("Connecting to: " + hostname + ":" + portnum);

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
				ReconnectCache.store(clientInfo.getUsername(), hostname, portnum);

				Packet01Login serverLoginPacket = new Packet01Login(serverSocket.in, this, this);

				if(serverLoginPacket.packetId == null || serverLoginPacket.read(serverSocket.in, this, this, true, null) == null) {
					printLogMessage("Server sent bad packet");
					connected = false;
				} else {

					clientInfo.setPlayerEntityId(serverLoginPacket.getVersion());

					if(firstConnection) {
						firstConnection = false;
						Packet01Login clientLoginPacket = new Packet01Login(clientSocket.out, this, this);
						if(!forward) {
							clientLoginPacket.setVersion(Globals.getDefaultPlayerId());
						} else {
							clientLoginPacket.setVersion(serverLoginPacket.getVersion());
						}
						if(Globals.getDimension() == null) {
							if(Globals.isVerbose()) {
								printLogMessage("Using dimension from server packet: " + serverLoginPacket.getDimension());
							}
							clientLoginPacket.setDimension(serverLoginPacket.getDimension());
						} else {
							if(Globals.isVerbose()) {
								printLogMessage("Using default dimension: " + Globals.getDimension());
							}
							clientLoginPacket.setDimension(Globals.getDimension());
						}
						clientLoginPacket.setMapSeed(serverLoginPacket.getMapSeed());
						clientLoginPacket.setUsername(serverLoginPacket.getUsername());
						if(clientLoginPacket.packetId == null || clientLoginPacket.write(clientSocket.out, this, this, true) == null) {
							printLogMessage("Failed to write login packet to client");
							connected = false;
						}
					} else {
						short holdingLocal = getHolding();
						if(holdingLocal != 0) {
							if(!Globals.isQuiet()) {
								printLogMessage("Updating holding slot to " + holding);
							}
							Packet10HoldingChange holdingChange = new Packet10HoldingChange(serverSocket.out, this, this);
							holdingChange.setSlot(holding);
							if(holdingChange.packetId == null || holdingChange.write(serverSocket.out, this, this, false) == null) {
								printLogMessage("Failed to write holding change update packet");
								connected = false;
							}
						}
					}

					if(connected) {
						if(forward) {
							serverToClient = new DataStreamBridge(serverSocket.in, clientSocket.out, this);
							clientToServer = new DataStreamBridge(clientSocket.in, serverSocket.out, this);
							connected = false;
						} else {
							serverToClient = new DataStreamDownLinkBridge(serverSocket.in, clientSocket.out, this);
							clientToServer = new DataStreamUpLinkBridge(clientSocket.in, serverSocket.out, this);
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
							connected = getRedirect() != null;
						}
					}
				}
			}

			printLogMessage("Closing connection to server");
			LocalSocket.closeSocket(serverSocket.socket, this);
			if(connected) {
				if(Globals.isVerbose()) {
					printLogMessage("Reviving thread");
				}
				revive();
			}
		}

		if(!getKickMessageSent()) {
			PacketFFKick.kick(clientSocket.out, this, this, "Connection closed by proxy");
		}

		printLogMessage("Closing connection to client");
		LocalSocket.closeSocket(clientSocket.socket, this);

	}

	synchronized void printLogMessage(String message) {
		String username = clientInfo.getUsername();
		if(username == null) {
			System.out.println("[" + shortTime.format(new Date()) + "] " + clientInfo.getIP() + "/" + clientInfo.getPort() + ": " + message);
		} else {
			System.out.println("[" + shortTime.format(new Date()) + "] " + clientInfo.getIP() + "/" + clientInfo.getPort() + " (" + username + "): " + message);
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

	@Override
	public void revive() {
		super.revive();
		synchronized(enabledSync) {
			enabled = true;
		}
		
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

	public void setRedirect(String redirect) {
		synchronized(redirectSync) {
			this.redirect = redirect;
		}
	}
	
	public String getRedirect() {
		synchronized(redirectSync) {
			return(redirect);
		}
	}
	
	public void setHolding(Short holding) {
		synchronized(holdingSync) {
			this.holding = holding;
		}
	}
	
	public Short getHolding() {
		synchronized(holdingSync) {
			return(holding);
		}
	}
	
	

}
