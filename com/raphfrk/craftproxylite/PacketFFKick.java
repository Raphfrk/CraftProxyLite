package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PacketFFKick extends Packet {

	final static Byte defaultPacketId = (byte)0xFF;
	
	PacketFFKick(Byte packetId) {
		super(packetId == defaultPacketId ? defaultPacketId : (Byte)null);
		if(packetId != defaultPacketId) {
			System.out.println("Unexpected packet Id, obtained " + packetId + " but expected " + defaultPacketId);
		}
	}
	
	PacketFFKick(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		super(in, ptc, thread);
	}
	
	PacketFFKick(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		super(out, ptc, thread, defaultPacketId);
	}
	
	String getMessage() {
		super.setupFields();
		return (String)fields[0].getValue();
	}

	void setMessage(String value) {
		super.setupFields();
		((UnitString)fields[0]).setValue(value);
	}
	
	public static String kick(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, String message) {
		
		if(Main.craftGUI != null) {
			Main.craftGUI.safeSetStatus(message);
		}
		
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
		
		if(Main.craftGUI != null) {
			Main.craftGUI.safeSetStatus(message);
		}
		
		boolean fail = false;
		if(PacketFFKick.kick(socket.out, ptc, thread, message) == null) {
			fail=true;
		}
		if(!LocalSocket.closeSocket(socket.socket, ptc)) {
			fail=true;
		}
		return fail?null:message;
	}
	
	public static String redirectDetected(String reason, PassthroughConnection ptc) {

		String hostName = null;
		int portNum = -1;
		
		if(ptc != null && (!Globals.isQuiet())) {
			ptc.printLogMessage( "Kicked with: " + reason ); 
		}

		if( reason.indexOf("[Serverport]") == 0 ) {
			String[] split = reason.split( ":" );
			if( split.length == 3 ) {
				hostName = split[1].trim();
				try { 
					portNum = Integer.parseInt( split[2].trim() );
				} catch (Exception e) { portNum = -1; };
			} else  if( split.length == 2 ) {
				hostName = split[1].trim();
				try {
					portNum = 25565;
				} catch (Exception e) { portNum = -1; };
			}
		}
		
		if(reason.startsWith("[Serverport] : ") && reason.indexOf(",")>=0) {
			return reason.substring(15).trim();
		}

		if( portNum != -1 ) {
			return hostName + ":" + portNum;
		} else {
			return null;

		}
	}

}
