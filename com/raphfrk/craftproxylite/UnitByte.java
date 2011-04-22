package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashSet;

public class UnitByte extends ProtocolUnit {

	private Byte value;
	
	public static Byte getByte(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		UnitByte temp = new UnitByte();
		return temp.read(in, ptc, thread, false, null);
	}
	
	public static Byte getByteSkipZeros(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		UnitByte temp = new UnitByte();
		do {
			temp.read(in, ptc, thread, false, null);
		} while(temp.getValue() == 0 && temp.getValue() != null);
		return temp.getValue();
	}
	
	public static Byte writeByte(DataOutputStream out, Byte value, PassthroughConnection ptc, KillableThread thread) {
		UnitByte temp = new UnitByte();
		temp.value = value;
		return temp.write(out, ptc, thread, false);
	}

	@Override
	public Byte read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		while(true) {
			try {
				value = in.readByte();
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
	public Byte write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		while(true) {
			try {
				out.writeByte(value);
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
	public Byte pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		read(in, ptc, thread, serverToClient, linkState);
		if(value != null) {
			return write(out, ptc, thread, serverToClient);
		} else {
			return null;
		}
	}
	
	@Override
	public Byte getValue() {
		return value;
	}
	
	public void setValue(Byte value) {
		this.value = value;
	}

}

