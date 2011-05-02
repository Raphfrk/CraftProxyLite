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

	long period = 0;

	public void run() {

		ByteCircleBuffer bcb = new ByteCircleBuffer(20);

		boolean eof = false;

		byte[] buffer = new byte[131072]; // buffer used for passthrough temp storage

		resetCounters();

		long startTime = System.currentTimeMillis();
		long lastTime = startTime;

		period = Globals.monitorBandwidth();
		boolean monitor = period > 0;

		while(!eof && !super.killed()) {

			long currentTime = System.currentTimeMillis();

			if(monitor && currentTime > lastTime + period) {
				lastTime = currentTime;
				printBandwidth(ptc, currentTime - startTime);
			}

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

					Packet46ServerState serverState = new Packet46ServerState(out, ptc, this);

					serverState.setState((byte)2);

					if(serverState.packetId == null || serverState.write(out, ptc, this, true) == null) {
						ptc.printLogMessage("Unable to send rain clearing packet");
						ptc.setRedirect(null);
						eof = true;
						continue;
					}

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
					UnitByte.writeByte(out, packetId, ptc, this);
					kickPacket.write(out, ptc, this, true);
				}

			} /*else if(packetId == 0x32){

				Packet currentPacket = new Packet(packetId);

				Packet value = currentPacket.read(in, ptc, this, true, linkState);
				if(value != null) {
					Integer x = (Integer)currentPacket.fields[0].getValue();
					Integer z = (Integer)currentPacket.fields[1].getValue();
					Boolean mode = (Boolean)currentPacket.fields[2].getValue();
					if(mode) {
						if(linkState.contains(x, z)) {
							System.out.println("Chunk " + x + " " + z + " added, but it already was loaded, skipping");
							continue;
						}
					} else {
						if(linkState.contains(x, z)) {
							System.out.println("Chunk " + x + " " + z + " removed, but it was not loaded, skipping");
							continue;
						}
					} 

					if(UnitByte.writeByte(out, packetId, ptc, this) == null) {
						if(!Globals.isQuiet()) {
							ptc.printLogMessage("Unable to write packet id");
						}
						eof = true;
						continue;
					}
					value = currentPacket.write(out, ptc, this, true);
				} else {
					ptc.printLogMessage("Unable to read 0x32 packet");
					eof = true;
					continue;
				}

			} else */{

				if(UnitByte.writeByte(out, packetId, ptc, this) == null) {
					if(!Globals.isQuiet()) {
						ptc.printLogMessage("Unable to write packet id");
					}
					eof = true;
					continue;
				}

				Packet currentPacket = new Packet(packetId);

				bcb.write(packetId);

				if(currentPacket.critical) {
					ptc.printLogMessage("Downlink Previous packets (Oldest -> Newest): " + bcb);
				}

				if(Globals.isVerbose()) {
					ptc.printLogMessage("Transferring packet: " + Integer.toHexString(packetId & 0xFF));
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

	private void clearCounters() {
		for(int cnt=0;cnt<256;cnt++) {
			ptc.packetLastCounters[cnt] = ptc.packetCounters[cnt];
		}
	}

	private void resetCounters() {
		for(int cnt=0;cnt<256;cnt++) {
			ptc.packetCounters[cnt] = 0;
		}
		clearCounters();
	}

	void printBandwidth(PassthroughConnection ptc, long time) {
		int total = 0;
		for(int cnt=0;cnt<256;cnt++) {
			total += ptc.packetCounters[cnt] - ptc.packetLastCounters[cnt];
		}

		ptc.printLogMessage("");
		ptc.printLogMessage("Bandwidth Stats - last " + (period/1000) + " seconds" );
		for(int cnt=0;cnt<256;cnt++) {
			int usage = ptc.packetCounters[cnt] - ptc.packetLastCounters[cnt];
			if(usage > total/100) {
				printBandwidthLine("0x" + Integer.toHexString(cnt), usage, total, time);			}
		}

		ptc.printLogMessage("");
		printBandwidthLine("Total", total, total, time);

		total = 0;
		for(int cnt=0;cnt<256;cnt++) {
			total += ptc.packetCounters[cnt];
		}

		ptc.printLogMessage("");
		ptc.printLogMessage("Bandwidth Stats - since login" );
		ptc.printLogMessage("");

		for(int cnt=0;cnt<256;cnt++) {
			int usage = ptc.packetCounters[cnt];
			if(usage > total/100) {
				printBandwidthLine("0x" + Integer.toHexString(cnt), usage, total, time);			}
		}

		ptc.printLogMessage("");
		printBandwidthLine("Total", total, total, time);
		ptc.printLogMessage("");

		clearCounters();
	}

	void printBandwidthLine(String prefix, int usage, int total, long time) {
		String kb = "" + (usage/1024) + "                            ";
		String kbs = "" + ((1000.0*(usage/1024.0))/time) + "                              ";
		String percent = "" + ((100.0*usage)/total) + "                           ";

		ptc.printLogMessage("     " + prefix + ": " + kbs.substring(0,5) + "kB/s (" + percent.substring(0,6) + "%)");

	}


}
