package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitShortSizedQuadByteArray extends ProtocolUnit {

	UnitShort lengthUnit;
	
	private Short length;
	private byte[] value = null;

	void setupBuffer(byte[] buffer) {
		if(value == null || value.length < length) {
			if(buffer == null || buffer.length < length) {
				value = new byte[length];
			} else {
				value = buffer;
			}
		}
	}

	@Override
	public byte[] read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState) {

		length = lengthUnit.read(in, ptc, thread, serverToClient, linkState);
		if(length == null) {
			return null;
		}
		
		length = (short)(length * 4);
		
		if(length > 0xFFFF) {
			ptc.printLogMessage("To many block updates in one packet - breaking connection");
			return null;
		}
		
		setupBuffer(null);
		
		int pos = 0;

		while(pos < length) {
			try {
				pos += in.read(value, pos, length - pos);
			} catch (SocketTimeoutException ste) {
				if(!thread.killed()) {
					timeout++;
					if(timeout > 225) {
						ptc.printLogMessage("Connection timed out");
						return null;
					}
					continue;
				}
				return null;
			} catch (IOException e) {
				ptc.printLogMessage("Unable to read from socket");
				return null;
			}
		}
		
		return value;

	}

	@Override
	public byte[] write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		if(length == null) {
			return null;
		}
		
		Short lengthTemp = lengthUnit.write(out, ptc, thread, serverToClient);
		if(lengthTemp == null) {
			return null;
		}

		if((short)(lengthTemp * 4) != length) {
			ptc.printLogMessage("Error writing quad byte array, breaking connection");
			return null;
		}
		
		incrementCounter(serverToClient, length, ptc);
		
		while(true) {
			try {
				out.write(value, 0, length);
				out.flush();
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
	public byte[] pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, byte[] buffer, DownlinkState linkState) {
		
		length = lengthUnit.read(in, ptc, thread, serverToClient, linkState);
		if(length == null) {
			return null;
		}
		
		length = lengthUnit.write(out, ptc, thread, serverToClient);
		if(length == null) {
			return null;
		}
		
		length = (short)(length * 4);
		
		if(length > 0xFFFF) {
			ptc.printLogMessage("To many block updates in one packet - breaking connection");
			return null;
		}
		
		if(buffer == null || value == null) {
			setupBuffer(buffer);
		}
		
		buffer = value;
		
		int pos = 0;
		
		int bufLength = buffer.length;
		
		incrementCounter(serverToClient, length, ptc);

		while(pos < length) {
			int read;
			bufLength = Math.min(bufLength, length - pos);
			try {
				read = in.read(value, 0, bufLength);
			} catch (SocketTimeoutException ste) {
				if(!thread.killed()) {
					timeout++;
					if(timeout > 225) {
						ptc.printLogMessage("Connection timed out");
						return null;
					}
					continue;
				}
				return null;
			} catch (IOException e) {
				ptc.printLogMessage("Unable to read from socket");
				return null;
			}
			try {
				out.write(value, 0, read);
			} catch (SocketTimeoutException ste) {
				if(!thread.killed()) {
					timeout++;
					if(timeout > 225) {
						ptc.printLogMessage("Connection timed out");
						return null;
					}
					continue;
				}
				return null;
			} catch (IOException e) {
				ptc.printLogMessage("Unable to read from socket");
				return null;
			}
			pos += read;
		}
		
		return buffer;
		
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
	
	@Override 
	public UnitShortSizedQuadByteArray clone() {
		UnitShortSizedQuadByteArray ussqba = (UnitShortSizedQuadByteArray)super.clone();
		ussqba.value = null;
		ussqba.lengthUnit = new UnitShort();
		return ussqba;
	}

}

