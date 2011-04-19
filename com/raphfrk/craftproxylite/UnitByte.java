package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitByte extends ProtocolUnit {

	private Byte value;
	
	public static Byte getByte(DataInputStream in, PassthroughConnection ptc) {
		UnitByte temp = new UnitByte();
		return temp.read(in, ptc);
	}
	
	public static Byte writeByte(DataOutputStream out, PassthroughConnection ptc) {
		UnitByte temp = new UnitByte();
		return temp.write(out, ptc);
	}

	@Override
	public Byte read(DataInputStream in, PassthroughConnection ptc) {

		while(true) {
			try {
				value = in.readByte();
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(ptc)) {
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
	public Byte write(DataOutputStream out, PassthroughConnection ptc) {

		while(true) {
			try {
				out.writeByte(value);
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(ptc)) {
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
	public Byte pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		read(in, ptc);
		if(value != null) {
			return write(out, ptc);
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

