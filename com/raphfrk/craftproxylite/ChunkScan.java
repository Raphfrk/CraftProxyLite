package com.raphfrk.craftproxylite;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ChunkScan {
	
	static final boolean[] dontWipe = new boolean[40];

	Inflater inflate = new Inflater();
	Deflater deflate = new Deflater(1);

	byte[] expandChunkData(byte[] input, int inputLength, byte[] buffer) {

		if(buffer.length < 131072 || inputLength > 16384) {
			return null;
		}

		inflate.reset();
		inflate.setInput(input, 0, inputLength);

		int outputOffset = 32768;
		int outputLength = 131072-outputOffset;

		int expandedLength = -1;
		try {
			expandedLength = inflate.inflate(buffer, outputOffset, outputLength);
		} catch (DataFormatException e) {
			return null;
		}

		if(expandedLength != 81920 && expandedLength != 81920 + 320) {
			return null;
		}

		return buffer;
	}

	public Integer recompressChunkData(byte[] buffer, boolean overwriteInput, long[] hashes) {

		if(buffer.length < 131072) {
			return null;
		}

		int inputOffset = 32768;
		int inputSize = 81920;
		int outputOffset = overwriteInput?0:16384;
		int outputSize = overwriteInput?32768:16384;;
		
		if(hashes != null) {
			int pos = inputSize + inputOffset;
			for(int a=0;a<40;a++) {
				long hash = hashes[a];

				for(int b=0;b<8;b++) {
					buffer[pos++] = (byte)hash;
					hash = hash >> 8;
				}
			}
			inputSize += 8*40;
		}
		
		deflate.reset();
		deflate.setInput(buffer, inputOffset, inputSize);
		deflate.finish();
		int compressedLength = deflate.deflate(buffer, outputOffset, outputSize);

		if(!deflate.finished()) {
			return null;
		} else {
			return compressedLength;
		}

	}
	
	public long[] extractHashes(byte[] buffer, long[] hashes) {
		
		int inputOffset = 32768;
		int inputSize = 81920;
		
		int pos = inputSize + inputOffset;
		for(int a=0;a<40;a++) {
			long hash = 0;
			for(int b=0;b<8;b++) {
				byte value = buffer[pos++];
				hash = (hash >> 8)&0x0FFFFFFFFFFFFFFFL;
				hash |= ((long)value)<<56;
			}
			hashes[a] = hash;
		}
		
		return hashes;
		
	}

	public long[] generateHashes(PassthroughConnection ptc, byte[] buffer) {

		synchronized(ptc.hashThreadSyncObject) {
			if(ptc.hashThreads == null) {
				ptc.hashThreads = new HashThread[4];
				for(int cnt=0;cnt<4;cnt++) {
					ptc.hashThreads[cnt] = new HashThread(ptc);
					ptc.hashThreads[cnt].start();
				}
			}
		}

		HashThread[] hashThreads = ptc.hashThreads;
		
		for(int cnt=0;cnt<40;cnt+=10) {
			hashThreads[cnt/10].init(buffer, 32768, cnt, cnt+10, ptc.hashes, dontWipe);
		}

		for(int cnt=0;cnt<4;cnt++) {
			hashThreads[cnt].doneJoin();
		}
		
		return ptc.hashes;
	}

	public void wipeBuffer(PassthroughConnection ptc, byte[] buffer, boolean[] wipe) {
		HashThread[] hashThreads = ptc.hashThreads;
		
		for(int cnt=0;cnt<40;cnt+=10) {
			hashThreads[cnt/10].init(buffer, 32768, cnt, cnt+10, ptc.hashes, wipe);
		}
		
		for(int cnt=0;cnt<4;cnt++) {
			hashThreads[cnt].doneJoin();
		}
	}


}
