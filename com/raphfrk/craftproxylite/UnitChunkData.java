package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class UnitChunkData extends UnitIntSizedByteArray{

	@Override
	public byte[] write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		if(!serverToClient || (!ptc.clientInfo.getLocalCache())) {
			return super.write(out, ptc, thread, serverToClient);
		} else {

			int length = super.lengthUnit.getValue();
			byte[] buffer = super.getValue();

			if(Globals.compressInfo()) {
				ptc.printLogMessage("Hashes stored at client:" + ptc.setHashes.size());
				ptc.printLogMessage("Initial size:" + length);
			}

			ChunkScan chunkScan = ptc.chunkScan;

			if(chunkScan.expandChunkData(buffer, length, buffer, ptc) == null) {
				if(Globals.compressInfo()) {
					ptc.printLogMessage("Expansion of chunk failed");
				}
				return super.write(out, ptc, thread, serverToClient);
			}

			chunkScan.generateHashes(ptc, buffer);

			boolean[] matches = new boolean[40];
			for(int cnt=0;cnt<40;cnt++) {
				Long hash = ptc.hashes[cnt];
				matches[cnt] = ptc.setHashes.contains(hash);
				ptc.setHashes.add(hash);
			}
			
			chunkScan.wipeBuffer(ptc, buffer, matches);

			Integer newLength = chunkScan.recompressChunkData(buffer, true, ptc.hashes, ptc);
			
			if(newLength == null) {
				if(Globals.compressInfo()) {
					ptc.printLogMessage("Expansion of chunk failed");
				}
				return super.write(out, ptc, thread, serverToClient);
			}
			
			super.lengthUnit.setValue(-newLength);
			
			if(Globals.compressInfo()) {
				ptc.printLogMessage("Recompressed size: " + newLength);
			}
			
			return super.write(out, ptc, thread, serverToClient);
		}

	}

	@Override
	public byte[] pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {

		if(!serverToClient || (!ptc.clientInfo.getLocalCache()) || buffer == null) {
			return super.pass(in, out, ptc, thread, serverToClient, buffer, linkState);
		} else {
			super.read(in, ptc, thread, serverToClient, linkState, buffer);
			if(super.getValue() != null) {
				return write(out, ptc, thread, serverToClient);
			} else {
				return null;
			}
		}

	}

}
