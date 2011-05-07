package com.raphfrk.craftproxylite;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ChunkScan {
	
	public static final int inputDataBufferSize = 128*1024;
	public static final int outputDataBufferSize = 128*1024;
	public static final int expandedBufferSize = 128*1024;
	
	public static final boolean[] dontWipe = new boolean[40];



	byte[] expandChunkData(byte[] input, int inputLength, byte[] buffer, PassthroughConnection ptc) {

		if(buffer.length < inputDataBufferSize + outputDataBufferSize + expandedBufferSize + 320 || inputLength > inputDataBufferSize) {
			return null;
		}

		ptc.inflate.reset();
		ptc.inflate.setInput(input, 0, inputLength);

		int outputOffset = inputDataBufferSize + outputDataBufferSize;
		int outputLength = expandedBufferSize;

		int expandedLength = -1;
		try {
			expandedLength = ptc.inflate.inflate(buffer, outputOffset, outputLength);
		} catch (DataFormatException e) {
			return null;
		}

		if(expandedLength != 81920 && expandedLength != 81920 + 320) {
			return null;
		}

		return buffer;
	}

	public Integer recompressChunkData(byte[] buffer, boolean overwriteInput, long[] hashes, PassthroughConnection ptc) {

		if(buffer.length < 163840) {
			return null;
		}

		int inputOffset = inputDataBufferSize + outputDataBufferSize;
		int inputSize = 81920;
		int outputOffset = overwriteInput?0:inputDataBufferSize;
		int outputSize = outputDataBufferSize;
		
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
		
		ptc.deflate.reset();
		ptc.deflate.setInput(buffer, inputOffset, inputSize);
		ptc.deflate.finish();
		int compressedLength = ptc.deflate.deflate(buffer, outputOffset, outputSize);

		if(!ptc.deflate.finished()) {
			return null;
		} else {
			return compressedLength;
		}

	}
	
	public long[] extractHashes(byte[] buffer, long[] hashes) {
		
		int inputOffset = inputDataBufferSize + outputDataBufferSize;
		int inputSize = 81920;
		
		int pos = inputSize + inputOffset;
		for(int a=0;a<40;a++) {
			long hash = 0;
			for(int b=0;b<8;b++) {
				byte value = buffer[pos++];
				hash = (hash >> 8) & 0x00FFFFFFFFFFFFFFL;
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
			hashThreads[cnt/10].init(buffer, inputDataBufferSize + outputDataBufferSize, cnt, cnt+10, ptc.hashes, dontWipe);
		}

		for(int cnt=0;cnt<4;cnt++) {
			hashThreads[cnt].doneJoin();
		}
		
		return ptc.hashes;
	}

	public void wipeBuffer(PassthroughConnection ptc, byte[] buffer, boolean[] wipe) {
		HashThread[] hashThreads = ptc.hashThreads;
		
		for(int cnt=0;cnt<40;cnt+=10) {
			hashThreads[cnt/10].init(buffer, inputDataBufferSize + outputDataBufferSize, cnt, cnt+10, ptc.hashes, wipe);
		}
		
		for(int cnt=0;cnt<4;cnt++) {
			hashThreads[cnt].doneJoin();
		}
	}


}
