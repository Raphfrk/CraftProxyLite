package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitInteger extends ProtocolUnit {

	private Integer value;

	@Override
	public Integer read(DataInputStream in, PassthroughConnection ptc) {

		while(true) {
			try {
				value = in.readInt();
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
	public Integer write(DataOutputStream out, PassthroughConnection ptc) {

		while(true) {
			try {
				out.writeInt(value);
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
	public Integer pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		read(in, ptc);
		if(value != null) {
			return write(out, ptc);
		} else {
			return null;
		}
	}
	
	@Override
	public Integer getValue() {
		return value;
	}
	
	public void setValue(Integer value) {
		this.value = value;
	}

}
