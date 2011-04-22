package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class UnitMetaStream extends ProtocolUnit {

	private ArrayList<Byte> value;
	
	@Override
	public ArrayList<Byte> read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		Byte temp;
		value = new ArrayList<Byte>();
		
		while(true) {
			try {
				temp = in.readByte();

				if(temp == null) {
					return null;
				} else {
					value.add(temp);
					if(temp == 0x7F) {
						return value;
					}
					temp = (byte)(( temp >> 5 ) & 0x7);
					if(temp != 0) {
						ptc.printLogMessage("Non-zero meta data type - breaking connection");
						return null;
					}
				}
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}
			
			super.timeout = 0;
			
			try {
				temp = in.readByte();

				if(temp == null) {
					return null;
				} else {
					value.add(temp);
				}
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(thread)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}

			super.timeout = 0;

		}

	}
	
	@Override
	public ArrayList<Byte>  write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		while(true) {
			try {
				int length = value.size();
				for(int cnt=0;cnt<length;cnt++) {
					out.writeByte(value.get(cnt));
				}
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
	public ArrayList<Byte>  pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		value = read(in, ptc, thread, serverToClient, linkState);
		if(value != null) {
			return write(out, ptc, thread, serverToClient);
		} else {
			return null;
		}
	}
	
	@Override
	public ArrayList<Byte> getValue() {
		return value;
	}
	
	public void setValue(ArrayList<Byte> value) {
		this.value = value;
	}

}

