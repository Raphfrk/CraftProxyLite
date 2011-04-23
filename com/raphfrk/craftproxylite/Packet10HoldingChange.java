package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet10HoldingChange extends Packet {

	final static Byte defaultPacketId = 0x10;
	
	Packet10HoldingChange(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	Packet10HoldingChange(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	Packet10HoldingChange(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	Short getSlot() {
		super.setupFields();
		return (Short)fields[0].getValue();
	}

	void setSlot(Short value) {
		super.setupFields();
		((UnitShort)fields[0]).setValue(value);
	}

}