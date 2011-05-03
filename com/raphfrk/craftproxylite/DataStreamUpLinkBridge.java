package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.LinkedList;

public class DataStreamUpLinkBridge extends KillableThread {

	final DataInputStream in;
	final DataOutputStream out;
	final PassthroughConnection ptc;

	DataStreamUpLinkBridge(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		this.in = in;
		this.out = out;
		this.ptc = ptc;
	}

	LinkedList<Long> synced = new LinkedList<Long>();

	public void run() {

		boolean sendHashUpdates = Globals.localCache();
		System.out.println("local cache enabled");

		ByteCircleBuffer bcb = new ByteCircleBuffer(20);

		boolean eof = false;

		byte[] buffer = new byte[131072]; // buffer used for passthrough temp storage

		while(!eof && !super.killed()) {

			if(sendHashUpdates && !ptc.hashQueue.isEmpty()) {

				Long hash;
				UnitByte fakeId = new UnitByte();

				fakeId.setValue((byte)0x50);

				if(fakeId.write(out, ptc, this, false)==null) {
					eof = true;
					continue;
				}

				while((hash = ptc.hashQueue.poll()) != null && synced.size() < 2048) {
					synced.add(hash);
				}

				UnitShort length = new UnitShort();
				length.setValue((short)synced.size());

				if(length.write(out, ptc, this, false) == null) {
					eof = true;
					continue;
				}

				UnitLong hashUnit = new UnitLong();
				
				while((hash = synced.poll()) != null) {
					hashUnit.setValue(hash);
					if(hashUnit.write(out, ptc, this, false) == null) {
						eof = true;
						continue;
					}
				}
			}

			Byte packetId = UnitByte.getByte(in, ptc, this);

			if(packetId == null) {
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Unable to read packet id");
				}
				eof = true;
				continue;
			}

			if(packetId == (byte)0x50) {

				UnitShort length = new UnitShort();

				if(length.read(in, ptc, this, false, null) == null) {
					eof = true;
					continue;
				}

				short lengthPrim = length.getValue();

				UnitLong hashUnit = new UnitLong();
				for(short cnt=0;cnt<lengthPrim && !eof;cnt++) {
					if(hashUnit.read(in, ptc, this, false, null) == null) {
						eof = true;
					} else if(ptc.setHashes.size() < 100000) {
						ptc.setHashes.add(hashUnit.getValue());
					}
				}
				if(eof) {
					continue;
				}

			} else {

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

				bcb.write(packetId);

				if(currentPacket.critical) {
					ptc.printLogMessage("Uplink Previous packets (Oldest -> Newest): " + bcb);
				}

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
			}
		}

		ptc.interrupt();

	}

}

