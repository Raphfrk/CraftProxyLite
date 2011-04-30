package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet02Handshake extends Packet {

	final static Byte defaultPacketId = 0x02;
	
	Packet02Handshake(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
		if(packetId != defaultPacketId) {
			System.out.println("Unexpected packet Id, obtained " + packetId + " but expected " + defaultPacketId);
		}
	}
	
	Packet02Handshake(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	Packet02Handshake(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	String getUsername() {
		super.setupFields();
		return (String)fields[0].getValue();
	}

	void setUsername(String value) {
		super.setupFields();
		((UnitString)fields[0]).setValue(value);
		
	}

}
