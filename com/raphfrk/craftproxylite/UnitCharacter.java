package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashSet;

public class UnitCharacter extends ProtocolUnit {

	private Character value;

	@Override
	public Character read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		while(true) {
			try {
				value = in.readChar();
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
	public Character write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		while(true) {
			try {
				out.writeChar(value);
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
	public Character pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		read(in, ptc, thread, serverToClient, linkState);
		if(value != null) {
			return write(out, ptc, thread, serverToClient);
		} else {
			return null;
		}
	}
	
	@Override
	public Character getValue() {
		return value;
	}
	
	public void setValue(Character value) {
		this.value = value;
	}
}
