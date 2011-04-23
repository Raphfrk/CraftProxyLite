package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DataStreamUpLinkBridge extends KillableThread {

	final DataInputStream in;
	final DataOutputStream out;
	final PassthroughConnection ptc;
	
	DataStreamUpLinkBridge(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		this.in = in;
		this.out = out;
		this.ptc = ptc;
	}
	
	public void run() {
		
		boolean eof = false;
		
		byte[] buffer = new byte[131072]; // buffer used for passthrough temp storage
		
		while(!eof && !super.killed()) {
			
			Byte packetId = UnitByte.getByte(in, ptc, this);
			
			if(packetId == null) {
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Unable to read packet id");
				}
				eof = true;
				continue;
			}
			
			if(packetId == 0xFF) {
				ptc.printLogMessage("Kick packet received");
				eof = true;
			}
			
			if(UnitByte.writeByte(out, packetId, ptc, this) == null) {
				ptc.printLogMessage("Unable to write packet id");
				eof = true;
				continue;
			}
			
			Packet currentPacket = new Packet(packetId);
			
			if(Globals.isVerbose()) {
				ptc.printLogMessage("Transferring packet: " + Integer.toHexString(packetId & 0xFF));
			}
			
			if(currentPacket.packetId == null || currentPacket.pass(in, out, ptc, this, false, buffer, null) == null) {
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Unable to transfer packet");
				}
				eof = true;
				continue;
			}
			
			if(packetId == 0x10) {
				Short holding = (Short)((UnitShort)(currentPacket.fields[0])).getValue();
				ptc.setHolding(holding);
			}
			
			if(currentPacket.packetId == 0x47) {
				Packet47Weather w = new Packet47Weather(currentPacket);
				System.out.println(w);
				System.out.println("Weather packet");
			}
			
			if(currentPacket.packetId == 0x1B) {
				System.out.println("1b packet C->S");
			}
			
		}
		
		ptc.interrupt();
		
	}
	
}

