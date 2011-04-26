package com.raphfrk.craftproxylite;

public class ClientInfo {
	
	private int playerEntityId;
	private String username;
	private String ip;
	private int port;
	private boolean forward = false;
	private String hostname;
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setIP(String username) {
		this.ip = username;
	}
	
	public String getIP() {
		return ip;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setForward(boolean forward) {
		this.forward = forward;
	}
	
	public boolean getForward() {
		return forward;
	}
	
	public void setPlayerEntityId(int playerEntityId) {
		this.playerEntityId = playerEntityId;
	}
	
	public int getPlayerEntityId() {
		return playerEntityId;
	}
}


