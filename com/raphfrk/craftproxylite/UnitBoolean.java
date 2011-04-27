package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashSet;

public class UnitBoolean extends ProtocolUnit {

		private Boolean value;

		@Override
		public Boolean read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

			while(true) {
				try {
					value = in.readBoolean();
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
		public Boolean write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {
			incrementCounter(serverToClient, 1, ptc);
			while(true) {
				try {
					out.writeBoolean(value);
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
		public Boolean pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
			read(in, ptc, thread, serverToClient, linkState);
			if(value != null) {
				return write(out, ptc, thread, serverToClient);
			} else {
				return null;
			}
		}
		
		@Override
		public Boolean getValue() {
			return value;
		}

		public void setValue(Boolean value) {
			this.value = value;
		}
		
	}


