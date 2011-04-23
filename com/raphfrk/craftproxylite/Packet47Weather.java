package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet47Weather extends Packet {
	
	Packet47Weather(Packet packet) {
		super(packet.packetId);
		fields = packet.fields;
	}

	final static Byte defaultPacketId = 0x32;
	
	Packet47Weather(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	Packet47Weather(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	Packet47Weather(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	Double getX() {
		super.setupFields();
		return ((Integer)fields[2].getValue())/32.0;
	}

	Double getY() {
		super.setupFields();
		return ((Integer)fields[3].getValue())/32.0;
	}
	
	Double getZ() {
		super.setupFields();
		return ((Integer)fields[4].getValue())/32.0;
	}


	void setX(Double value) {
		super.setupFields();
		((UnitInteger)fields[2]).setValue((int)(value*32));
	}
	
	void setY(Double value) {
		super.setupFields();
		((UnitInteger)fields[3]).setValue((int)(value*32));
	}
	
	void setZ(Double value) {
		super.setupFields();
		((UnitInteger)fields[4]).setValue((int)(value*32));
	}
	
	Boolean getRain() {
		super.setupFields();
		return (Boolean)fields[1].getValue();
	}
	
	void setRain(Boolean value) {
		super.setupFields();
		((UnitBoolean)fields[1]).setValue(value);
	}
	
	Integer getId() {
		super.setupFields();
		return (Integer)fields[1].getValue();
	}
	
	void setId(Integer value) {
		super.setupFields();
		((UnitEntityId)fields[1]).setValue(value);
	}
	
	@Override
	public String toString() {
		return "[[" + getX() + ", " + getY() + ", " + getZ() + "] " + getRain() + " " + getId() + "]";
	}
	

}