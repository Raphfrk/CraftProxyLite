package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DataStreamDownLinkBridge extends KillableThread {

	final DataInputStream in;
	final DataOutputStream out;
	final PassthroughConnection ptc;
	
	DataStreamDownLinkBridge(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		this.in = in;
		this.out = out;
		this.ptc = ptc;
	}
	
	public void run() {
		
		boolean eof = false;
		
		byte[] buffer = new byte[131072]; // buffer used for passthrough temp storage
		
		DownlinkState linkState = new DownlinkState();
		
		while(!eof && !super.killed()) {
			
			Byte packetId = UnitByte.getByte(in, ptc, this);
			
			if(packetId == null) {
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Unable to read packet id");
				}
				eof = true;
				continue;
			}

			if(UnitByte.writeByte(out, packetId, ptc, this) == null) {
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Unable to write packet id");
				}
				eof = true;
				continue;
			}
			
			Packet currentPacket = new Packet(packetId);
			
			if(Globals.isVerbose()) {
				ptc.printLogMessage("Transferring packet: " + packetId);
			}
			
			if(currentPacket.pass(in, out, ptc, this, true, buffer, linkState) == null) {
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Unable to transfer packet");
				}
				eof = true;
				continue;
			}
			
		}
		ptc.interrupt();
		
	}
	
}
