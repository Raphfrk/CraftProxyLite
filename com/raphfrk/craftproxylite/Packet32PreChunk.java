package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet32PreChunk extends Packet {

	final static Byte defaultPacketId = 0x32;
	
	Packet32PreChunk(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	Packet32PreChunk(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	Packet32PreChunk(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	Integer getX() {
		super.setupFields();
		return (Integer)fields[0].getValue();
	}

	Integer getZ() {
		super.setupFields();
		return (Integer)fields[1].getValue();
	}

	void setX(Integer value) {
		super.setupFields();
		((UnitInteger)fields[0]).setValue(value);
	}
	
	void setZ(Integer value) {
		super.setupFields();
		((UnitInteger)fields[1]).setValue(value);
	}
	
	Boolean getLoad() {
		super.setupFields();
		return (Boolean)fields[2].getValue();
	}
	
	void setLoad(Boolean value) {
		super.setupFields();
		((UnitBoolean)fields[2]).setValue(value);
	}

}