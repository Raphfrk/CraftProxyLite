package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet1DDestroyEntity extends Packet {

	final static Byte defaultPacketId = 0x1D;
	
	Packet1DDestroyEntity(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
		if(packetId != defaultPacketId) {
			System.out.println("Unexpected packet Id, obtained " + packetId + " but expected " + defaultPacketId);
		}
	}
	
	Packet1DDestroyEntity(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	Packet1DDestroyEntity(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	Integer getEntityId() {
		super.setupFields();
		return (Integer)fields[0].getValue();
	}

	void setEntityId(Integer value) {
		super.setupFields();
		((UnitEntityId)fields[0]).setValue(value);
		
	}

}
