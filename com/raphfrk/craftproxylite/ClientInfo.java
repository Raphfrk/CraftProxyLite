package com.raphfrk.craftproxylite;

public class ClientInfo {
	
	private int playerEntityId;
	private String username;
	private String ip;
	private int port;
	
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
	
	public void setPlayerEntityId(int playerEntityId) {
		this.playerEntityId = playerEntityId;
	}
	
	public int getPlayerEntityId() {
		return playerEntityId;
	}
}


