package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class UnitChunkData extends UnitIntSizedByteArray{
	
	@Override
	public byte[] write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {
		
		if(!Globals.isChunkCache()) {
			return super.write(out, ptc, thread, serverToClient);
		} else {
			// code to do compression
			return super.write(out, ptc, thread, serverToClient);
		}
		
	}
	
	@Override
	public byte[] pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {

		if(buffer == null || !Globals.isChunkCache()) {
			return super.pass(in, out, ptc, thread, serverToClient, buffer, linkState);
		} else {
			super.read(in, ptc, thread, serverToClient, linkState);
			if(super.getValue() != null) {
				return write(out, ptc, thread, serverToClient);
			} else {
				return null;
			}
		}
		
	}
	
}
