package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PacketFFKick extends Packet {

	final static Byte defaultPacketId = 0x02;
	
	PacketFFKick(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	PacketFFKick(DataInputStream in, PassthroughConnection ptc) {
		super(in, ptc);
	}
	
	String getMessage() {
		return (String)fields[0].getValue();
	}

	void setMessage(String value) {
		fields[0].setValue(value);
	}
	
	public static String kick(DataOutputStream out, PassthroughConnection ptc, String message) {
		
		if(ptc.kickMessageSent) {
			return null;
		} else {
			ptc.kickMessageSent = true;
		}
		
		PacketFFKick kick = new PacketFFKick((byte)(0xFF));
		
		kick.setupFields();
		kick.setMessage(message);
		
		if(kick.write(out, ptc) == null) {
			return null;
		} else {
			return message;
		}
		
	}

}
