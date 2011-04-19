package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitLong extends ProtocolUnit {

	private Long value;

	@Override
	public Long read(DataInputStream in, PassthroughConnection ptc) {

		while(true) {
			try {
				value = in.readLong();
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
	public Long write(DataOutputStream out, PassthroughConnection ptc) {

		while(true) {
			try {
				out.writeLong(value);
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
	public Long pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		read(in, ptc);
		if(value != null) {
			return write(out, ptc);
		} else {
			return null;
		}
	}
	
	@Override
	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}
	
}

