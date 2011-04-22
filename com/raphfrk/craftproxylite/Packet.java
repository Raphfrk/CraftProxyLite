package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class Packet extends ProtocolUnit {

	final static ProtocolUnit[][] packetInfo;

	final static UnitByte                     unitByte                     = new UnitByte();
	final static UnitCharacter                unitChar                     = new UnitCharacter();
	final static UnitInteger                  unitInt                      = new UnitInteger();
	final static UnitLong                     unitLong                     = new UnitLong();
	final static UnitShort                    unitShort                    = new UnitShort();
	final static UnitFloat                    unitFloat                    = new UnitFloat();
	final static UnitString                   unitString                   = new UnitString();
	final static UnitEntityId                 unitEntity                   = new UnitEntityId();
	final static UnitBoolean                  unitBoolean                  = new UnitBoolean();
	final static UnitItemStack                unitItemStack                = new UnitItemStack();
	final static UnitMetaStream               unitMetaStream               = new UnitMetaStream();
	final static UnitIntSizedByteArray        unitIntSizedByteArray        = new UnitIntSizedByteArray();
	final static UnitIntSizedTripleByteArray  unitIntSizedTripleByteArray  = new UnitIntSizedTripleByteArray();
	final static UnitShortSizedQuadByteArray  unitShortSizedQuadByteArray  = new UnitShortSizedQuadByteArray();
	final static UnitShortSizedItemStackArray unitShortSizedItemStackArray = new UnitShortSizedItemStackArray();

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
		packetInfo[0x0C] = new ProtocolUnit[] {new UnitFixed(9)};
		packetInfo[0x0D] = new ProtocolUnit[] {new UnitFixed(41)};
		packetInfo[0x0E] = new ProtocolUnit[] {new UnitFixed(11)};
		packetInfo[0x0F] = new ProtocolUnit[] {new UnitFixed(10), unitIntSizedByteArray, unitItemStack};
		packetInfo[0x10] = new ProtocolUnit[] {unitShort};
		packetInfo[0x11] = new ProtocolUnit[] {unitEntity, new UnitFixed(10)};
		packetInfo[0x12] = new ProtocolUnit[] {unitEntity, unitByte};
		packetInfo[0x13] = new ProtocolUnit[] {unitEntity, unitByte};
		packetInfo[0x14] = new ProtocolUnit[] {unitEntity, unitString, new UnitFixed(16)};
		packetInfo[0x15] = new ProtocolUnit[] {unitEntity, new UnitFixed(20)};
		packetInfo[0x16] = new ProtocolUnit[] {unitEntity, unitEntity};
		packetInfo[0x17] = new ProtocolUnit[] {unitEntity, new UnitFixed(13)};
		packetInfo[0x18] = new ProtocolUnit[] {unitEntity, new UnitFixed(15), unitMetaStream};
		packetInfo[0x19] = new ProtocolUnit[] {unitEntity, unitString, new UnitFixed(16)};
		packetInfo[0x1B] = new ProtocolUnit[] {new UnitFixed(18)};
		packetInfo[0x1C] = new ProtocolUnit[] {unitEntity, new UnitFixed(6)};
		packetInfo[0x1D] = new ProtocolUnit[] {unitEntity};
		packetInfo[0x1E] = new ProtocolUnit[] {unitEntity};
		packetInfo[0x1F] = new ProtocolUnit[] {unitEntity, new UnitFixed(3)};
		packetInfo[0x20] = new ProtocolUnit[] {unitEntity, new UnitFixed(2)};
		packetInfo[0x21] = new ProtocolUnit[] {unitEntity, new UnitFixed(5)};
		packetInfo[0x22] = new ProtocolUnit[] {unitEntity, new UnitFixed(14)};
		packetInfo[0x26] = new ProtocolUnit[] {unitEntity, unitByte};
		packetInfo[0x27] = new ProtocolUnit[] {unitEntity, unitEntity};
		packetInfo[0x28] = new ProtocolUnit[] {unitEntity, unitMetaStream};
				
		packetInfo[0x32] = new ProtocolUnit[] {unitInt, unitInt, unitBoolean};
		packetInfo[0x33] = new ProtocolUnit[] {unitInt, unitShort, unitInt, unitByte, unitByte, unitByte, unitIntSizedByteArray};
		packetInfo[0x34] = new ProtocolUnit[] {new UnitFixed(8), unitShortSizedQuadByteArray};
		packetInfo[0x35] = new ProtocolUnit[] {new UnitFixed(11)};
		packetInfo[0x36] = new ProtocolUnit[] {new UnitFixed(12)};
		packetInfo[0x3C] = new ProtocolUnit[] {new UnitFixed(12), unitIntSizedTripleByteArray};
		
		packetInfo[0x46] = new ProtocolUnit[] {unitByte};
		
		packetInfo[0x47] = new ProtocolUnit[] {unitEntity, new UnitFixed(13)};
		
		packetInfo[0x64] = new ProtocolUnit[] {unitByte, unitByte, unitString, unitByte};
		packetInfo[0x65] = new ProtocolUnit[] {unitByte};
		packetInfo[0x66] = new ProtocolUnit[] {new UnitFixed(7), unitItemStack};
		packetInfo[0x67] = new ProtocolUnit[] {new UnitFixed(3), unitItemStack};
		packetInfo[0x68] = new ProtocolUnit[] {unitByte, unitShortSizedItemStackArray};
		packetInfo[0x69] = new ProtocolUnit[] {new UnitFixed(5)};
		packetInfo[0x6A] = new ProtocolUnit[] {new UnitFixed(4)};
		
		packetInfo[0x82] = new ProtocolUnit[] {new UnitFixed(10), unitString, unitString, unitString, unitString};
		
		packetInfo[0xC8] = new ProtocolUnit[] {new UnitFixed(5)};
		
		packetInfo[0xFF] = new ProtocolUnit[] {unitString};

		if(true) {
			
			packetInfo[0x0C] = new ProtocolUnit[] {unitFloat, unitFloat, unitBoolean};
			
		}
		
	}

	final Byte packetId;

	Packet(DataInputStream in, PassthroughConnection ptc, KillableThread thread) {
		this(UnitByte.getByteSkipZeros(in, ptc, null));
	}

	Packet(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, Byte packetId) {
		this(UnitByte.writeByte(out, packetId, ptc, null));
	}

	Packet(Byte packetId) {
		if(packetId != null && packetInfo[((int)packetId) & 0xFF] == null) {
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
			if(Globals.isVerbose()) {
				ptc.printLogMessage((serverToClient? "S->C" : "C->S") + "       " + fields[cnt].getClass().toString());
			}
			Object val = fields[cnt].pass(in, out, ptc, null, serverToClient, buffer, linkState);
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
