package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DataStreamDownLinkBridge extends KillableThread {

	final DataInputStream in;
	final DataOutputStream out;
	final PassthroughConnection ptc;
	final DownlinkState linkState = new DownlinkState();

	DataStreamDownLinkBridge(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
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

			if(packetId == (byte)0xFF) {
				
				PacketFFKick kickPacket = new PacketFFKick((byte)0xFF);
				
				if(Globals.isVerbose()) {
					ptc.printLogMessage("Reading kick packet: " + packetId);
				}
				
				if(kickPacket.read(in, ptc, this, true, linkState) == null) {
					if(!Globals.isQuiet()) {
						ptc.printLogMessage("Unable to read kick packet");
					}
					eof = true;
					continue;
				}
				
				String reason = kickPacket.getMessage();
				String redirect = PacketFFKick.redirectDetected(reason, ptc);
				if(redirect != null) {
					ptc.printLogMessage("Redirect detected: " + redirect);
					ptc.setRedirect(redirect);
					if(!destroyEntities()) {
						ptc.printLogMessage("Unable to destroy entities correctly");
						ptc.setRedirect(null);
						eof = true;
						continue;
					} else {
						if(!Globals.isQuiet()) {
							ptc.printLogMessage("Destroyed entities");
						}
					}
					if(!unloadChunks()) {
						ptc.printLogMessage("Unable to unload chunks correctly");
						ptc.setRedirect(null);
						eof = true;
						continue;
					} else {
						if(!Globals.isQuiet()) {
							ptc.printLogMessage("Unloaded chunks");
						}
					}
					eof = true;
					continue;
				} else {
					ptc.printLogMessage("Player kicked: " + reason);
					UnitByte.writeByte(out, packetId, ptc, this);
					kickPacket.write(out, ptc, this, true);
				}
				
			} else {

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

				if(currentPacket.packetId == null || currentPacket.pass(in, out, ptc, this, true, buffer, linkState) == null) {
					if(!Globals.isQuiet()) {
						ptc.printLogMessage("Unable to transfer packet");
					}
					eof = true;
					continue;
				}
			}

		}
		ptc.interrupt();

	}
	
	boolean destroyEntities() {
		for( Integer entityId : linkState.entityIds ) {
			Packet1DDestroyEntity destroyEntity = new Packet1DDestroyEntity(out, ptc, this);
			
			destroyEntity.setEntityId(entityId);
			
			if(destroyEntity.packetId == null || destroyEntity.write(out, ptc, this, true) == null) {
				return false;
			}
		}
		return true;
	}
	
	boolean unloadChunks() {
		int[] buffer = new int[2];
		for( Long key : linkState.activeChunks ) {
			
			buffer = DownlinkState.convertFromKey(key, buffer);
			int x = buffer[0];
			int z = buffer[1];
			
			Packet32PreChunk preChunk = new Packet32PreChunk(out, ptc, this);
			
			preChunk.setX(x);
			preChunk.setZ(z);
			preChunk.setLoad(false);
			
			if(preChunk.packetId == null || preChunk.write(out, ptc, this, true) == null) {
				return false;
			}
		}
		return true;
	}

}
