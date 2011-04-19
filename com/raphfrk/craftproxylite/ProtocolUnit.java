package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class ProtocolUnit {
	
	int timeout = 0;
	
	public Object read(DataInputStream in, PassthroughConnection connection) {
		return null;
	}
	
	public Object write(DataOutputStream out, PassthroughConnection ptc) {
		return null;
	}
	
	public Object pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		return null;
	}
	
	public Object getValue() {
		return null;
	}
	
	public void setValue(Object value) {
		throw new RuntimeException("attempted to set value to unextended ProtocolUnit");
	}

	
	boolean timedOut(PassthroughConnection ptc) {
		if(ptc.enabled) {
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
