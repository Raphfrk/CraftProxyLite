package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;


public class UnitShortSizedItemStackArray extends ProtocolUnit {
	
	static UnitItemStack[] validStack = new UnitItemStack[0]; // not used during processing

	UnitShort lengthUnit;
	
	private Short length;
	private UnitItemStack[] value = null;

	void setupBuffer(byte[] buffer) {
		if(value == null || value.length < length) {
			value = new UnitItemStack[length];
		}
	}

	@Override
	public UnitItemStack[] read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		length = lengthUnit.read(in, ptc, thread, serverToClient, linkState);
		if(length == null) {
			return null;
		}
		
		if(length > 0xFFFF) {
			ptc.printLogMessage("To many item window updates in one packet - breaking connection");
			return null;
		}
		
		setupBuffer(null);
		
		ItemStack temp = null;

		for(int cnt=0;cnt<length;cnt++) {
			value[cnt] = new UnitItemStack();
			temp = value[cnt].read(in, ptc, thread, serverToClient, linkState);
			
			if(temp == null) {
				return null;
			}
		}
		
		return value;

	}

	@Override
	public UnitItemStack[] write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		if(length == null || value == null) {
			return null;
		}
		
		length = lengthUnit.write(out, ptc, thread, serverToClient);
		if(length == null) {
			return null;
		}
		
		if(length > 0xFFFF) {
			ptc.printLogMessage("To many item window updates in one packet - breaking connection");
			return null;
		}
		
		ItemStack temp = null;
		
		for(int cnt=0;cnt<length;cnt++) {
			
			if(value[cnt] == null) {
				return null;
			}
			
			temp = value[cnt].write(out, ptc, thread, serverToClient);
			
			if(temp == null) {
				return null;
			}
		}
		
		return value;
		

	}

	@Override
	public UnitItemStack[] pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		
		length = lengthUnit.read(in, ptc, thread, serverToClient, linkState);
		
		if(length == null) {
			return null;
		}
		
		if(length > 0xFFFF) {
			ptc.printLogMessage("To many item window updates in one packet - breaking connection");
			return null;
		}
		
		length = lengthUnit.write(out, ptc, thread, serverToClient);
		
		if(length == null) {
			return null;
		}
		
		ItemStack temp = null;
		
		UnitItemStack temp2 = new UnitItemStack();
		
		for(int cnt=0;cnt<length;cnt++) {
			
			temp = temp2.read(in, ptc, thread, serverToClient, linkState);
			
			if(temp == null) {
				return null;
			}
			
			temp = temp2.write(out, ptc, thread, serverToClient);
			
			if(temp == null) {
				return null;
			}
		}
		
		return validStack;
	
	}

	@Override
	public UnitItemStack[] getValue() {
		return value;
	}

	public void setValue(UnitItemStack[] value) {
		this.value = value;
	}
	
	@Override 
	public UnitShortSizedItemStackArray clone() {
		UnitShortSizedItemStackArray ussisa = (UnitShortSizedItemStackArray)super.clone();
		ussisa.value = null;
		ussisa.lengthUnit = new UnitShort();
		return ussisa;
	}

}

