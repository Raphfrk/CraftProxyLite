package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.LinkedHashSet;

public abstract class ProtocolUnit implements Cloneable {
	
	int timeout = 0;
	
	public Object read(DataInputStream in, PassthroughConnection connection, KillableThread thread, boolean serverToClient, DownlinkState linkState) {
		return null;
	}
	
	public Object write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {
		return null;
	}
	
	public Object pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
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
	
	@Override
	public ProtocolUnit clone() {
		try {
			return (ProtocolUnit)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException( "Clone failure" , e);
		}
	}
	
}
