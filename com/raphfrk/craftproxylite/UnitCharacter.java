package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitCharacter extends ProtocolUnit {

	private Character value;

	@Override
	public Character read(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {

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
	public Character write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {

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
	public Character pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		read(in, ptc, thread);
		if(value != null) {
			return write(out, ptc, thread);
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
