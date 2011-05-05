package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitPreChunkPosition extends ProtocolUnit {

	private ChunkInfo value;
	
	@Override
	public ChunkInfo read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {
	
		while(true) {
			try {
				value.x = in.readInt();
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}
			
			try {
				value.z = in.readInt();
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}
			
			try {
				value.mode = in.readBoolean();
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}

			super.timeout = 0;

			return value;
		}

	}
	
	@Override
	public ChunkInfo write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		while(true) {
			try {
				out.writeInt(value.x);
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}
			
			
			try {
				out.writeInt(value.z);
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}
			
			try {
				out.writeBoolean(value.mode);
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}
			
			incrementCounter(serverToClient, 9, ptc);

			super.timeout = 0;

			return value;
		}

	}
	
	@Override
	public ChunkInfo pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		read(in, ptc, thread, serverToClient, linkState);
		if(value != null) {
			return write(out, ptc, thread, serverToClient);
		} else {
			return null;
		}
	}
	
	@Override
	public ChunkInfo getValue() {
		return value;
	}
	
	public void setValue(ChunkInfo value) {
		this.value = value;
	}

}
