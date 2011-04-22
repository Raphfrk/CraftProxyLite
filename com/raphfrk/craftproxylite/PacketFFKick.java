package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PacketFFKick extends Packet {

	final static Byte defaultPacketId = (byte)0xFF;
	
	PacketFFKick(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
	}
	
	PacketFFKick(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	String getMessage() {
		return (String)fields[0].getValue();
	}

	void setMessage(String value) {
		((UnitString)fields[0]).setValue(value);
	}
	
	public static String kick(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, String message) {
		
		if(ptc != null && (!ptc.sendingKickMessage())) {
			return null;
		}
		
		PacketFFKick kick = new PacketFFKick((byte)(0xFF));
		
		if(UnitByte.writeByte(out, (byte)0xFF, ptc, thread) == null) {
			return null;
		}
		
		kick.setupFields();
		kick.setMessage(message);
		
		if(kick.write(out, ptc, thread, true) == null) {
			return null;
		} else {
			return message;
		}
		
	}
	
	public static String kickAndClose(LocalSocket socket, PassthroughConnection ptc, KillableThread thread, String message) {
		boolean fail = false;
		if(PacketFFKick.kick(socket.out, ptc, thread, message) == null) {
			fail=true;
		}
		if(!LocalSocket.closeSocket(socket.socket, ptc)) {
			fail=true;
		}
		return fail?null:message;
	}

}
