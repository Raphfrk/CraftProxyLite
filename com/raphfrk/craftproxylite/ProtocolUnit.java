package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class ProtocolUnit {
	
	int timeout = 0;
	
	public Object read(DataInputStream in, PassthroughConnection connection, KillableThread thread) {
		return null;
	}
	
	public Object write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		return null;
	}
	
	public Object pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread) {
		return null;
	}
	
	public Object getValue() {
		throw new RuntimeException("attempted to set value to unextended ProtocolUnit");
	}
	
	public void setValue(Object value) {
		throw new RuntimeException("attempted to set value to unextended ProtocolUnit");
	}

	
	boolean timedOut(KillableThread thread) {
		if(thread == null || (!thread.killed)) {
			timeout++;
			if(timeout>=20) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
}
