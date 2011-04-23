package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class UnitFixed extends ProtocolUnit {

	private int length;
	private byte[] value = null;

	UnitFixed(int length) {
		this.length = length;
	}

	void setupBuffer(byte[] buffer) {
		if(value == null) {
			if(buffer == null || buffer.length < length) {
				value = new byte[length];
			} else {
				value = buffer;
			}
		}
	}

	@Override
	public byte[] read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		setupBuffer(null);

		int pos = 0;

		while(pos < length) {
			try {
				pos += in.read(value, pos, length - pos);
			} catch (SocketTimeoutException ste) {
				if(!thread.killed()) {
					timeout++;
					if(timeout > 20) {
						ptc.printLogMessage("Connection timed out");
						return null;
					}
					continue;
				}
				return null;
			} catch (IOException e) {
				ptc.printLogMessage("Unable to read from socket");
				return null;
			}
		}
		
		return value;

	}

	@Override
	public byte[] write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		while(true) {
			try {
				out.write(value, 0, length);
				out.flush();
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
	public byte[] pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		setupBuffer(buffer);
		read(in, ptc, thread, serverToClient, linkState);
		if(value != null) {
			return write(out, ptc, thread, serverToClient);
		} else {
			return null;
		}
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for(int cnt=0;cnt<length;cnt++) {
			if(first) {
				first = false;
				sb.append(value[cnt]);
			} else {
				sb.append(", " + value[cnt]);
			}
		}
		sb.append("]");
		return sb.toString();
	}

}

