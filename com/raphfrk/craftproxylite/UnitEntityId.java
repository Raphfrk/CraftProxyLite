package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashSet;

public class UnitEntityId extends ProtocolUnit {

	private Integer value;

	@Override
	public Integer read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		while(true) {
			try {
				value = in.readInt();
				if(serverToClient) {
					if(value == ptc.clientInfo.getPlayerEntityId()) {
						value = Globals.getDefaultPlayerId();
					} else if (value == Globals.getDefaultPlayerId()) {
						ptc.printLogMessage("Player entity id collision (server to client) - breaking connection");
						ptc.printLogMessage("Value: " + value);
						ptc.printLogMessage("Default: " + Globals.getDefaultPlayerId());
						ptc.printLogMessage("Active: " +  ptc.clientInfo.getPlayerEntityId());
						return null;
					} else {
						linkState.entityIds.add(value);
					}
				} else {
					if(value == Globals.getDefaultPlayerId()) {
						value = ptc.clientInfo.getPlayerEntityId();
					} else if (value == ptc.clientInfo.getPlayerEntityId()) {
						ptc.printLogMessage("Player entity id collision (client to server)- breaking connection");
						ptc.printLogMessage("Value: " + value);
						ptc.printLogMessage("Default: " + Globals.getDefaultPlayerId());
						ptc.printLogMessage("Active: " +  ptc.clientInfo.getPlayerEntityId());
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

			return value;
		}

	}
	
	@Override
	public Integer write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		incrementCounter(serverToClient, 4, ptc);
		
		while(true) {
			try {
				out.writeInt(value);
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
	public Integer pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		read(in, ptc, thread, serverToClient, linkState);
		if(value != null) {
			return write(out, ptc, thread, serverToClient);
		} else {
			return null;
		}
	}
	
	@Override
	public Integer getValue() {
		return value;
	}
	
	public void setValue(Integer value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return (String)value.toString();
	}

}
