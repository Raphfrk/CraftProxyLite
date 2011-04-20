package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitInteger extends ProtocolUnit {

	private Integer value;

	@Override
	public Integer read(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {

		while(true) {
			try {
				value = in.readInt();
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
	public Integer write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {

		while(true) {
			try {
				out.writeInt(value);
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
	public Integer pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		read(in, ptc, thread);
		if(value != null) {
			return write(out, ptc, thread);
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
