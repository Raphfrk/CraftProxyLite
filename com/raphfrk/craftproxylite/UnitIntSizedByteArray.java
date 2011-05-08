package com.raphfrk.craftproxylite;

import java.awt.Container;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitIntSizedByteArray extends ProtocolUnit {

	UnitInteger lengthUnit;
	
	boolean compressed = false;
	
	private Integer length;
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
		return read(in, ptc, thread, serverToClient, linkState, null);
	}
		
	public byte[] read(DataInputStream in, PassthroughConnection ptc, KillableThread thread, boolean serverToClient, DownlinkState linkState, byte[] buffer) {

		length = lengthUnit.read(in, ptc, thread, serverToClient, linkState);
		if(length == null) {
			return null;
		}
		
		if(length > 262144 || length < -262144) {
			ptc.printLogMessage("Byte array out of allowed range (" + length + ") - breaking connection");
			return null;
		}
		
		return readStandard(in, ptc, thread, buffer);
		
	}
	
	private byte[] readStandard(DataInputStream in, PassthroughConnection ptc, KillableThread thread, byte[] buffer) {
		
		if(length == null) {
			return null;
		}
		
		if(length < 0) {
			compressed = true;
			length = -length;
		}
		
		setupBuffer(buffer);

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
		
		if(!compressed) {
			return value;
		} else if(value == null) {
			return null;
		}
		
		ChunkScan chunkScan = ptc.chunkScan;
		
		if(Globals.compressInfo()) {
			ptc.printLogMessage("Initial packet length: " + length);
		}
		if(chunkScan.expandChunkData(buffer, length, buffer, ptc) == null) {
			return null;
		}
		
		chunkScan.extractHashes(buffer, ptc.hashes);
		
		int miss = 0;
		int hit = 0;
		int blockSize = Globals.getBlockSize();
		for(int cnt=0;cnt<40;cnt++) {
			long hash = ptc.hashes[cnt];
			byte[] cachedHash = ptc.hashCache.getArray(hash, 0, ptc);
			byte[] hashArray = cachedHash;
			if(cachedHash == null) {
				miss++;
				hashArray = new byte[2048];
				HashThread.transferArray(buffer, ChunkScan.inputDataBufferSize + ChunkScan.outputDataBufferSize, cnt, hashArray, 0, false);
				ptc.hashCache.addArray(hash, hashArray);
			} else {
				hit++;
				HashThread.transferArray(buffer, ChunkScan.inputDataBufferSize + ChunkScan.outputDataBufferSize, cnt, cachedHash, 0, true);
			}
			if(!ptc.hashesReceivedThisConnection.containsKey(hash)) {
				int ptcPos = ptc.hashBlockReceivedPos;
				ptc.hashesReceivedThisConnection.put(hash, true);
				ptc.hashBlockReceived[ptcPos] = hash;
				ptc.hashBlockReceivedFull[ptcPos] = hashArray;
				ptcPos++;
				if(ptcPos == blockSize) {
					ptcPos = 0;
					ptc.hashCache.saveHashBlockToDisk(ptc.hashBlockReceived, ptc.hashBlockReceivedFull, blockSize, false);
				}
				ptc.hashBlockReceivedPos = ptcPos;
			}
			
			
			
		}
		
		if(Globals.compressInfo()) {
			ptc.printLogMessage("Hit-Miss = " + hit + "-" + miss);
		}
		
		Integer newLength = chunkScan.recompressChunkData(buffer, true, null, ptc);
		
		if(newLength == null) {
			return null;
		}
		
		lengthUnit.setValue(newLength);
		
		int saved = ptc.savedData.addAndGet(newLength - length);
		
		if(Main.craftGUI != null) {
			int percent = (100*saved) / (ptc.packetCounter); 
			Main.craftGUI.safeSetStatus("<html>" + ptc.clientInfo.getUsername() + " connected<br>Data saved: " + saved/1024 + "kB  (" + percent + " %reduction)<html>");
		}

		
		return value;
	}
	
	@Override
	public byte[] write(DataOutputStream out, PassthroughConnection ptc, KillableThread thread, boolean serverToClient) {

		if((length = lengthUnit.getValue()) < 0) {
			lengthUnit.setValue(length);
		}
		length = lengthUnit.write(out, ptc, thread, serverToClient);
		if(length == null) {
			return null;
		}

		incrementCounter(serverToClient, Math.abs(length), ptc);
		
		while(true) {
			try {
				out.write(value, 0, Math.abs(length));
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
		
		if(length < 0) {
			readStandard(in, ptc, thread, buffer);
			if(getValue() != null) {
				return write(out, ptc, thread, serverToClient);
			} else {
				return null;
			}
			
		}
		
		if(length > 262144) {
			ptc.printLogMessage("Byte array out of allowed range (" + length + ") - breaking connection");
			return null;
		}
		
		if(Globals.localCache() && length < 0) {
			length = -length;
		}
		
		length = lengthUnit.write(out, ptc, thread, serverToClient);
		if(length == null) {
			return null;
		}
		
		if(buffer == null || buffer.length < length) {
			setupBuffer(buffer);
		} else if(value == null) {
			value = buffer;
		}
		
		setupBuffer(null);
		
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
	public UnitIntSizedByteArray clone() {
		UnitIntSizedByteArray uisba = (UnitIntSizedByteArray)super.clone();
		uisba.value = null;
		uisba.lengthUnit = new UnitInteger();
		return uisba;
	}

}

