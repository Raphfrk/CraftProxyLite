package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class UnitMetaStream extends ProtocolUnit {

	private ArrayList<ProtocolUnit> value;

	@Override
	public ArrayList<ProtocolUnit> read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		UnitByte temp;
		value = new ArrayList<ProtocolUnit>();

		while(true) {

			temp = new UnitByte();

			if(temp.read(in, ptc, thread, serverToClient, linkState) == null) {
				return null;
			} else {
				value.add(temp);
				if(temp.getValue() == 0x7F) {
					return value;
				}
				Byte select = (byte)(( temp.getValue() >> 5 ) & 0x7);
				ProtocolUnit element = null;
				switch(select) {
				case 0: element = new UnitByte(); break;
				case 1: element = new UnitShort(); break;
				case 2: element = new UnitInteger(); break;
				case 3: element = new UnitFloat(); break;
				case 4: element = new UnitString(); break;
				default: {
					ptc.printLogMessage("Unknown meta data code (" + temp + ") - breaking connection");
					return null;
				}
				}
				if(element.read(in, ptc, thread, serverToClient, linkState) == null) {
					return null;
				}
				value.add(element);
			}


		}

	}

	@Override
	public ArrayList<ProtocolUnit>  write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		if(value == null) {
			return value;
		} 
		
		for(ProtocolUnit current : value) {
			if(current.write(out, ptc, thread, serverToClient) == null) {
				return null;
			}
		}
		
		return value;

	}

	@Override
	public ArrayList<ProtocolUnit>  pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		value = read(in, ptc, thread, serverToClient, linkState);
		if(value != null) {
			return write(out, ptc, thread, serverToClient);
		} else {
			return null;
		}
	}

	@Override
	public ArrayList<ProtocolUnit> getValue() {
		return value;
	}

	public void setValue(ArrayList<ProtocolUnit> value) {
		this.value = value;
	}

}

