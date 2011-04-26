package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;

public class PassthroughConnection extends KillableThread {

	private final Socket socketToClient;
	protected final ClientInfo clientInfo;
	
	private Object holdingSync = new Object(); // not really required
	private short holding = 0;
	
	DateFormat shortTime = DateFormat.getTimeInstance(DateFormat.MEDIUM);

	private Object enabledSync = new Object();
	private boolean enabled = true;
	
	private final String listenHostname;
	private final String defaultHostname;

	private KillableThread serverToClient = null;
	private KillableThread clientToServer = null;

	protected DataInputStream inputFromClient = null;
	protected DataOutputStream outputToClient = null;

	protected DataInputStream inputFromServer = null;
	protected DataOutputStream outputToServer = null;

	protected boolean kickMessageSent = false;
	
	private Object redirectSync = new Object();
	private String redirect = null;

	PassthroughConnection(Socket socketToClient, String defaultHostname, String listenHostname) {
		this.socketToClient = socketToClient;
		this.clientInfo = new ClientInfo();
		this.clientInfo.setIP(socketToClient.getInetAddress().getHostAddress());
		this.clientInfo.setPort(socketToClient.getPort());
		this.defaultHostname = defaultHostname;
		this.listenHostname = listenHostname;
	}

	public void run() {

		boolean connected = true;
		
		clientInfo.setForward(false);
		clientInfo.setHostname(null);

		LocalSocket clientSocket = new LocalSocket(socketToClient, this);

		if(!clientSocket.success) {
			printLogMessage("Unable to open data streams for client socket");
			return;
		}

		String kickMessage = Packet01Login.processLogin(clientSocket.in, clientSocket.out, this, this, Globals.isAuth(), clientInfo);

		if(kickMessage != null) {
			printLogMessage(kickMessage);
			PacketFFKick.kick(clientSocket.out, this, this, kickMessage);
			connected = false;
		}
		
		if(BanList.banned(clientInfo.getUsername())) {
			printLogMessage(clientInfo.getUsername() + " is banned");
			PacketFFKick.kick(clientSocket.out, this, this, "Your account name is on the proxy ban list");
			connected = false;
		}

		if(clientInfo.getHostname() == null) {
			clientInfo.setHostname(ReconnectCache.get(clientInfo.getUsername()));
			if(clientInfo.getHostname() == null || clientInfo.getHostname().equals("")) {
				clientInfo.setHostname(defaultHostname);
			}
		}

		boolean firstConnection = true;

		while(connected && !killed()) {
			
			String redirectLocal = getRedirect();
			setRedirect(null);
			
			if(redirectLocal != null) {
				clientInfo.setHostname(redirectLocal);
			}
			
			printLogMessage("Connecting to: " + clientInfo.getHostname());
			
			String nextHostname = RedirectManager.getNextHostname(listenHostname, clientInfo.getHostname());
			Integer nextPortnum = RedirectManager.getNextPort(listenHostname, clientInfo.getHostname());
			
			if(nextHostname == null || nextPortnum == null) {
				printLogMessage("Unable to parse hostname: " + clientInfo.getHostname());
				PacketFFKick.kick(clientSocket.out, this, this, "Unable to parse hostname: " + clientInfo.getHostname());
				LocalSocket.closeSocket(clientSocket.socket, this);
				return;
			}
			
			Socket serverBasicSocket = LocalSocket.openSocket(nextHostname, nextPortnum, this);
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
			
			Boolean proxyLogin = RedirectManager.isNextProxy(listenHostname, clientInfo.getHostname());
			
			if(proxyLogin == null) {
				printLogMessage("Unable to determine if next login is a proxy");
				PacketFFKick.kickAndClose(clientSocket, this, this, "Unable to determine if next login is a proxy");
				return;
			}
			
			kickMessage = Packet01Login.serverLogin(serverSocket.in, serverSocket.out, this, this, proxyLogin, clientInfo);
			if(kickMessage != null) {
				printLogMessage(kickMessage);
				PacketFFKick.kick(clientSocket.out, this, this, kickMessage);
				connected = false;
			}
			if(connected) {
				
				Packet01Login serverLoginPacket = new Packet01Login(serverSocket.in, this, this);

				if(serverLoginPacket.packetId == null || serverLoginPacket.read(serverSocket.in, this, this, true, null) == null) {
					printLogMessage("Server sent bad packet");
					connected = false;
				} else {

					if(serverLoginPacket.packetId == 0x01) {
						clientInfo.setPlayerEntityId(serverLoginPacket.getVersion());
					} else if (serverLoginPacket.packetId == (byte)0xFF){
						PacketFFKick.kickAndClose(clientSocket, this, this, ((UnitString)serverLoginPacket.fields[0]).getValue());
						return;
					} else {
						PacketFFKick.kickAndClose(clientSocket, this, this, "Server sent bad packet during login");
						return;
					}

					if(firstConnection) {
						firstConnection = false;
						Packet01Login clientLoginPacket = new Packet01Login(clientSocket.out, this, this);
						if(!clientInfo.getForward()) {
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
						
						if(!Globals.isQuiet()) {
							if(clientInfo.getForward()) {
								printLogMessage("Connection is in forwarding mode");
							} else {
								printLogMessage("Connection is in proxy/processing mode");
							}
						}
						
						ReconnectCache.store(clientInfo.getUsername(), clientInfo.getHostname() );
						
						if(clientInfo.getForward()) {
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
			Logging.log("[" + shortTime.format(new Date()) + "] " + clientInfo.getIP() + "/" + clientInfo.getPort() + ": " + message);
		} else {
			Logging.log("[" + shortTime.format(new Date()) + "] " + clientInfo.getIP() + "/" + clientInfo.getPort() + " (" + username + "): " + message);
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
