package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet01Login extends Packet {
	
	final static Byte defaultPacketId = 0x01;
	
	Packet01Login(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	Packet01Login(DataInputStream in, PassthroughConnection ptc) {
		super(in, ptc);
	}

	static String processLogin(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		
		Packet02Handshake CtSHandshake = new Packet02Handshake(in, ptc);
		
		if(CtSHandshake.packetId == null || CtSHandshake.read(in, ptc) == null) {
			return "Failed to read initial C->S handshake packet";
		}
		
		ptc.clientInfo.setUsername(CtSHandshake.getUsername());
		
		Packet01Login login = new Packet01Login(in, ptc);
				
		if(login == null || login.read(in, ptc) == null) {
			return "Failed to read initial login packet";
		}
		
		return null;
		
	}
	
	int getVersion() {
		return (Integer)fields[0].getValue();
	}
	
	void setVersion(int value) {
		fields[0].setValue(value);
	}

	String getUsername() {
		return (String)fields[1].getValue();
	}
	
	void setUsername(String value) {
		fields[1].setValue(value);
	}

	String getPassword() {
		return (String)fields[2].getValue();
	}
	
	void setPassword(String value) {
		fields[2].setValue(value);
	}
	
	long getMapSeed() {
		return (Long)fields[3].getValue();
	}
	
	void setMapSeed(long value) {
		fields[3].setValue(value);
	}

	byte getDimension() {
		return (Byte)fields[4].getValue();
	}
	
	void setDimension(byte value) {
		fields[4].setValue(value);
	}
	
}
