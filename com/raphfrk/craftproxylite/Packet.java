package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class Packet extends ProtocolUnit {
	
	final static ProtocolUnit[][] packetInfo;
	
	final static UnitByte      unitByte   = new UnitByte();
	final static UnitCharacter unitChar   = new UnitCharacter();
	final static UnitInteger   unitInt    = new UnitInteger();
	final static UnitLong      unitLong   = new UnitLong();
	final static UnitString    unitString = new UnitString();
	
	static {
		
		packetInfo = new ProtocolUnit[256][];
				
		packetInfo[1] = new ProtocolUnit[] {unitInt, unitString, unitString, unitLong, unitByte};
		
	}
	
	final Byte packetId;
	
	Packet(DataInputStream in, PassthroughConnection ptc) {
		this(UnitByte.getByte(in, ptc));
	}
	
	Packet(Byte packetId) {
		this.packetId = packetId;
	}
	
	ProtocolUnit[] fields = null;
	
	boolean setupFields() {
		if(fields != null) {
			return true;
		}
		ProtocolUnit[] fieldsSource = packetInfo[packetId];
		int length = fieldsSource.length;
		fields = new ProtocolUnit[length];
		for(int cnt=0;cnt<length;cnt++) {
			try {
				fields[cnt] = fieldsSource[cnt].getClass().newInstance();
			} catch (InstantiationException e) {
				return false;
			} catch (IllegalAccessException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Packet read(DataInputStream in, PassthroughConnection ptc) {
		if(!setupFields()) {
			System.out.println("Error creating field data storage for packet: " + packetId);
		}
		
		int length = fields.length;
		for(int cnt=0;cnt<length;cnt++) {
			if(fields[cnt].read(in, ptc)==null) {
				return null;
			}
		}
		return this;
	}
	
	@Override
	public Packet write(DataOutputStream out, PassthroughConnection ptc) {
		if(!setupFields()) {
			System.out.println("Error creating field data storage for packet: " + packetId);
		}
		
		int length = fields.length;
		for(int cnt=0;cnt<length;cnt++) {
			if(fields[cnt].write(out, ptc)==null) {
				return null;
			}
		}
		return this;
	}
	
	@Override
	public Packet pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		int length = fields.length;
		for(int cnt=0;cnt<length;cnt++) {
			if(fields[cnt].pass(in, out, ptc)==null) {
				return null;
			}
		}
		return this;
	}
	
}
