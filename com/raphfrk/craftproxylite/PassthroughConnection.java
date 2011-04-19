package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PassthroughConnection extends Thread {
	
	boolean enabled = true;
	
	private final Socket socketToClient;
	private final String defaultServer;
	private final int defaultPort;
	private final String password;
	private final int listenPort;
	protected final ClientInfo clientInfo;
	protected boolean kickMessageSent = false;

	PassthroughConnection(Socket socketToClient, String defaultServer, int defaultPort, String password , int listenPort) {
		this.socketToClient = socketToClient;
		this.defaultServer = defaultServer;
		this.defaultPort = defaultPort;
		this.password = password;
		this.listenPort = listenPort;
		this.clientInfo = new ClientInfo();
	}
	
	public void run() {
		DataInputStream inputFromClient = null;
		DataOutputStream outputToClient = null;
		
		try {
			inputFromClient = new DataInputStream( socketToClient.getInputStream() );
		} catch (IOException e) {
			System.out.println("Unable to open data stream to client");
			if( inputFromClient != null ) {
				try {
					inputFromClient.close();
				} catch (IOException e1) {
					System.out.println("Unable to close data stream to client");
				}
			}
			return;
		}

		try {
			outputToClient = new DataOutputStream( socketToClient.getOutputStream() );
		} catch (IOException e) {
			System.out.println("Unable to open data stream from client");
			if( outputToClient != null ) {
				try {
					outputToClient.close();
				} catch (IOException e1) {
					System.out.println("Unable to close data stream from client");
				}
			}
			return;
		}
		
		String kickMessage = Packet01Login.processLogin(inputFromClient, outputToClient, this);
		
		if(kickMessage != null) {
			
		}
		
		
	}
	
	
}
