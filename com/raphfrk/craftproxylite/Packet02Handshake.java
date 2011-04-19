package com.raphfrk.craftproxylite;

import java.io.DataInputStream;

public class Packet02Handshake extends Packet {

	final static Byte defaultPacketId = 0x02;
	
	Packet02Handshake(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	Packet02Handshake(DataInputStream in, PassthroughConnection ptc) {
		super(in, ptc);
	}
	
	String getUsername() {
		return (String)fields[0].getValue();
	}

	void setUsername(String value) {
		fields[0].setValue(value);
	}

}
