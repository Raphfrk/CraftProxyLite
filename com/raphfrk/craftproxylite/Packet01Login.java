package com.raphfrk.craftproxylite;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;

public class Packet01Login extends Packet {
	
	final static Byte defaultPacketId = 0x01;
	
	Packet01Login(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	Packet01Login(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	Packet01Login(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	static String processLogin(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean auth) {
		
		Packet02Handshake CtSHandshake = new Packet02Handshake(in, ptc, thread);
		
		if(CtSHandshake.packetId == null || CtSHandshake.read(in, ptc, thread, true, null) == null) {
			return "Client refused to send initial handshake";
		}
		
		ptc.clientInfo.setUsername(CtSHandshake.getUsername());
		ptc.printLogMessage("Attempting login");
		
		Packet02Handshake StCHandshake = new Packet02Handshake(out, ptc, thread);
		
		String hashString;
		if( auth ) {
			hashString = getHashString();
			if(!Globals.isQuiet()) {
				ptc.printLogMessage("Hash used: " + hashString);
			}
		} else {
			hashString = "-";
		}
		
		StCHandshake.setUsername(hashString);
		
		if(StCHandshake.packetId == null || StCHandshake.write(out, ptc, thread, false) == null) {
			return "Client rejected initial handshake";
		}
		
		Packet01Login clientLogin = new Packet01Login(in, ptc, thread);
		
		if(clientLogin.packetId == null || clientLogin.read(in, ptc, thread, true, null) == null) {
			return "Client sent bad login packet";
		}
		
		if(clientLogin.getVersion() != Globals.getClientVersion()) {
			return "Client attempted to login with incorrect version";
		}
		
		if(auth) {
			if(!authenticate(ptc.clientInfo.getUsername(), hashString,  ptc)) {
				return "Proxy is unable to authenticate";
			} 
		}
		
		return null;
		
	}
	
	static String serverLogin(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {

		ptc.printLogMessage("Attempting server login");
		
		Packet02Handshake CtSHandshake = new Packet02Handshake(out, ptc, thread);
		
		CtSHandshake.setUsername(ptc.clientInfo.getUsername());
		
		if(CtSHandshake.packetId == null || CtSHandshake.write(out, ptc, thread, false) == null) {
			return "Server rejected intial handshake packet";
		}
		
		Packet02Handshake StCHandshake = new Packet02Handshake(in, ptc, thread);
		
		if(StCHandshake.packetId == null || StCHandshake.read(in, ptc, thread, true, null) == null) {
			return "Server sent bad handshake packet";
		}
		
		Packet01Login clientLogin = new Packet01Login(out, ptc, thread);
		
		clientLogin.setVersion(Globals.getClientVersion());
		clientLogin.setUsername(ptc.clientInfo.getUsername());
		clientLogin.setDimension((byte)0);
		clientLogin.setMapSeed(0);
		
		if(clientLogin.packetId == null || clientLogin.write(out, ptc, thread, false) == null) {
			return "Server rejected client login packet";
		}
		
		return null;
	}

	
	static SecureRandom hashGenerator = new SecureRandom();
	
	static String getHashString() {
		long hashLong;
		synchronized( hashGenerator ) {
			hashLong = hashGenerator.nextLong();
		}

		return Long.toHexString(hashLong);
	}
	
	static boolean authenticate( String username , String hashString, PassthroughConnection ptc )  {

		try {
			String authURLString = new String( "http://www.minecraft.net/game/checkserver.jsp?user=" + username + "&serverId=" + hashString);
			if(!Globals.isQuiet()) {
				ptc.printLogMessage("Authing with " + authURLString);
			}
			URL minecraft = new URL(authURLString);
			URLConnection minecraftConnection = minecraft.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(minecraftConnection.getInputStream()));

			String reply = in.readLine();

			if( Globals.isInfo() ) {
				ptc.printLogMessage("Server Response: " + reply );
			}

			in.close();
			
			if( reply != null && reply.equals("YES")) {
				
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Auth successful");
				}
				return true;
			}
		} catch (MalformedURLException mue) {
			ptc.printLogMessage("Auth URL error");
		} catch (IOException ioe) {
			ptc.printLogMessage("Problem connecting to auth server");
		}

		return false;
	}
	
	int getVersion() {
		super.setupFields();
		return (Integer)fields[0].getValue();
	}
	
	void setVersion(int value) {
		super.setupFields();
		((UnitInteger)fields[0]).setValue(value);
	}

	String getUsername() {
		super.setupFields();
		return (String)fields[1].getValue();
	}
	
	void setUsername(String value) {
		super.setupFields();
		((UnitString)fields[1]).setValue(value);
	}
	
	long getMapSeed() {
		super.setupFields();
		return (Long)fields[2].getValue();
	}
	
	void setMapSeed(long value) {
		super.setupFields();
		((UnitLong)fields[2]).setValue(value);
	}

	byte getDimension() {
		super.setupFields();
		return (Byte)fields[3].getValue();
	}
	
	void setDimension(byte value) {
		super.setupFields();
		((UnitByte)fields[3]).setValue(value);
	}
	
}
