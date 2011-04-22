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
						return null;
					} else {
						linkState.entityIds.add(value);
					}
				} else {
					if(value == Globals.getDefaultPlayerId()) {
						value = ptc.clientInfo.getPlayerEntityId();
					} else if (value == ptc.clientInfo.getPlayerEntityId()) {
						ptc.printLogMessage("Player entity id collision (client to server)- breaking connection");
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

}
