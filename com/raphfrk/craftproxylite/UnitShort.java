package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashSet;

public class UnitShort extends ProtocolUnit {

		private Short value;

		@Override
		public Short read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

			while(true) {
				try {
					value = in.readShort();
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
		public Short write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

			while(true) {
				try {
					out.writeShort(value);
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
		public Short pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
			read(in, ptc, thread, serverToClient, linkState);
			if(value != null) {
				return write(out, ptc, thread, serverToClient);
			} else {
				return null;
			}
		}
		
		@Override
		public Short getValue() {
			return value;
		}

		public void setValue(Short value) {
			this.value = value;
		}
		
	}


