package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet46ServerState extends Packet {

	final static Byte defaultPacketId = 0x46;
	
	Packet46ServerState(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
		if(packetId != defaultPacketId) {
			System.out.println("Unexpected packet Id, obtained " + packetId + " but expected " + defaultPacketId);
		}
	}
	
	Packet46ServerState(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	Packet46ServerState(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	Byte getState() {
		super.setupFields();
		return (Byte)fields[0].getValue();
	}

	void setState(Byte value) {
		super.setupFields();
		((UnitByte)fields[0]).setValue(value);
	}

}