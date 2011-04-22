package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class Packet extends ProtocolUnit {

	final static ProtocolUnit[][] packetInfo;

	final static UnitByte      unitByte   = new UnitByte();
	final static UnitCharacter unitChar   = new UnitCharacter();
	final static UnitInteger   unitInt    = new UnitInteger();
	final static UnitLong      unitLong   = new UnitLong();
	final static UnitShort     unitShort  = new UnitShort();
	final static UnitString    unitString = new UnitString();
	final static UnitEntityId  unitEntity = new UnitEntityId();
	final static UnitBoolean   unitBoolean= new UnitBoolean();

	static {

		packetInfo = new ProtocolUnit[256][];

		packetInfo[0x00] = new ProtocolUnit[] {};
		packetInfo[0x01] = new ProtocolUnit[] {unitInt, unitString, unitLong, unitByte};
		packetInfo[0x02] = new ProtocolUnit[] {unitString};
		packetInfo[0x03] = new ProtocolUnit[] {unitString};
		packetInfo[0x04] = new ProtocolUnit[] {new UnitFixed(8)};
		packetInfo[0x05] = new ProtocolUnit[] {unitEntity, new UnitFixed(6)};
		packetInfo[0x06] = new ProtocolUnit[] {new UnitFixed(12)};
		packetInfo[0x07] = new ProtocolUnit[] {unitEntity, unitEntity, unitBoolean};
		packetInfo[0x08] = new ProtocolUnit[] {unitShort};
		packetInfo[0x09] = new ProtocolUnit[] {};
		packetInfo[0x0A] = new ProtocolUnit[] {unitBoolean};
		packetInfo[0x0B] = new ProtocolUnit[] {new UnitFixed(33)};
		
		packetInfo[0x32] = new ProtocolUnit[] {unitInt, unitInt, unitBoolean};
		packetInfo[0x33] = new ProtocolUnit[] {unitInt, unitShort, unitInt, unitByte, unitByte, unitByte, unitIntSizedByteArray};

		packetInfo[0xFF] = new ProtocolUnit[] {unitString};

	}

	final Byte packetId;

	Packet(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		this(UnitByte.getByteSkipZeros(in, ptc, null));
	}

	Packet(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, Byte packetId) {
		this(UnitByte.writeByte(out, packetId, ptc, null));
	}

	Packet(Byte packetId) {
		if(packetId != null && packetInfo[packetId & 0xFF] == null) {
			throw new RuntimeException("Critical error unknown packet id: " + packetId);
		} else {
			this.packetId = packetId;
		}
	}

	ProtocolUnit[] fields = null;

	boolean setupFields() {
		if(fields != null) {
			return true;
		}
		ProtocolUnit[] fieldsSource = packetInfo[packetId&0xFF];
		int length = fieldsSource.length;
		fields = new ProtocolUnit[length];
		for(int cnt=0;cnt<length;cnt++) {
			fields[cnt] = fieldsSource[cnt].clone();
		}
		return true;
	}

	@Override
	public Packet read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {
		if(!setupFields()) {
			ptc.printLogMessage("Error creating field data storage for packet: " + packetId);
		}

		int length = fields.length;
		for(int cnt=0;cnt<length;cnt++) {
			if(fields[cnt].read(in, ptc, null, serverToClient, linkState)==null) {
				return null;
			}
		}
		entityDestroyCheck(linkState);
		return this;
	}

	@Override
	public Packet write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {
		if(!setupFields()) {
			ptc.printLogMessage("Error creating field data storage for packet: " + packetId);
		}

		int length = fields.length;
		for(int cnt=0;cnt<length;cnt++) {
			if(fields[cnt].write(out, ptc, null, serverToClient)==null) {
				return null;
			}
		}
		return this;
	}

	@Override
	public Packet pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		if(!setupFields()) {
			ptc.printLogMessage("Error creating field data storage for packet: " + packetId);
		}
		if(Globals.isVerbose()) {
			ptc.printLogMessage((serverToClient? "S->C" : "C->S") + " " + packetId);
		}
		int length = fields.length;
		for(int cnt=0;cnt<length;cnt++) {
			Object val = fields[cnt].pass(in, out, ptc, null, serverToClient, buffer, entityList);
			if(Globals.isVerbose()) {
				ptc.printLogMessage(cnt + " " + val);
			}
			if(val==null) {
				return null;
			}
		}
		entityDestroyCheck(linkState);
		return this;
	}

	void entityDestroyCheck(DownlinkState linkState) {
		if(linkState != null) {
			if(packetId == 0x1D && linkState.entityIds != null) {
				Integer entityId = (Integer)fields[0].getValue();
				linkState.entityIds.remove(entityId);
			} else if (packetId == 0x32) {
				Integer x = (Integer)fields[0].getValue();
				Integer z = (Integer)fields[1].getValue();
				Boolean mode = (Boolean)fields[2].getValue();
				if(mode) {
					linkState.addChunk(x, z);
				} else {
					linkState.removeChunk(x, z);
				}
			} else if (packetId == 0x33) {
				Integer x = ((Integer)fields[0].getValue()) >> 4;
				Integer z = ((Integer)fields[2].getValue()) >> 4;
				linkState.addChunk(x, z);
			}
		}
	}

	@Override
	public String toString() {
		int length = fields.length;
		StringBuilder sb = new StringBuilder();
		for(int cnt=0;cnt<length;cnt++) {
			sb.append(fields[cnt].getClass() + ") " + fields[cnt].getValue() + "\n");
		}
		return sb.toString();
	}

}
