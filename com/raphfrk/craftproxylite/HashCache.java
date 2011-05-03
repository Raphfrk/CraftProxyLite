package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HashCache {

	final static byte[] empty = new byte[0];

	// contains the file location for all known hashes
	static HashMap<Long,File> FAT = new HashMap<Long,File>();

	// contains full data for the current connection
	static HashMap<Long,Reference<byte[]>> cache = new HashMap<Long,Reference<byte[]>>();

	// Contains only blocks which has associated data linked
	LinkedHashMap<Long,long[]> blockHashList = new LinkedHashMap<Long,long[]>();

	LinkedList<byte[]> hardReferencesLoop = new LinkedList<byte[]>();

	Object fileIdSync = new Object();
	int fileId = 0;

	static FileCompare fileCompare = new FileCompare();

	// Need way to set arrays for current connection to hard links
	public byte[] addArray(long hash, byte[] a) {

		cache.put(hash, new HardReference<byte[]>(a));

		return a;
	}

	public byte[] getArray(long hash, int recursions, PassthroughConnection ptc) {
		if(recursions > 2) {
			return null;
		}
		Reference<byte[]> soft = cache.get(hash);
		byte[] array;
		if(soft != null) {
			array = soft.get();
			if(array == null) {
				cache.remove(hash);
			}
		} else {
			array = null;
		}
		File temp;
		if(array == null && (temp = FAT.get(hash)) != null && temp.exists()) {
			if(readSingleFile(temp, true, ptc)) {
				return getArray(hash, recursions + 1, ptc);
			} else {
				return null;
			}
		} else {
			return array;
		}
	}

	public boolean contains(long hash) {
		return FAT.containsKey(hash);
	}

	public HashCache(PassthroughConnection ptc) {
		
		ptc.hashQueue = new ConcurrentLinkedQueue<Long>();
		if(!Main.cacheDir.isDirectory()) {
			return;
		}
		File[] files = Main.cacheDir.listFiles();
		Arrays.sort(files, fileCompare);

		if(files.length > 0) {
			fileId = getIntFromName(files[files.length-1]) + 1;
			// Probably need code to limit number of files to 64?
		}

		for(File file : files) {
			readSingleFile(file, false, ptc);
		}
	}

	public Set<Long> getBlockHashList() {
		return blockHashList.keySet();
	}

	public long[] getHashesFromFile(long blockHash, PassthroughConnection ptc) {
		long[] hashes = blockHashList.get(blockHash);
		if(hashes != null) {
			for(long hash : hashes) {
				ptc.setHashes.add(hash);
			}
		}
		return null;
	}

	public boolean readSingleFile(File file, boolean all, PassthroughConnection ptc) {

		FileInputStream in;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return false;
		}

		int blockSize;
		long blockHash;
		long[] hashes;
		byte[][] hashData;

		int requiredUpdates = 0;
		int skippedUpdates = 0;

		try {
			GZIPInputStream gzIn;
			try {
				gzIn = new GZIPInputStream(in);
			} catch (IOException e1) {
				return false;
			}
			DataInputStream inData = new DataInputStream(gzIn);

			try {
				blockHash = inData.readLong();

				blockSize = inData.readInt();

				hashes = new long[blockSize];
				hashData = new byte[blockSize][];

				for(int cnt=0;cnt<blockSize;cnt++) {
					hashes[cnt] = inData.readLong();
					ptc.hashQueue.offer(hashes[cnt]);
				}

				if(all) {
					for(int cnt=0;cnt<blockSize;cnt++) {
						if(!cache.containsKey(hashes[cnt])) {
							int skip = skippedUpdates*2048;
							if(inData.skip(skip) != skip) {
								System.out.println("Error when skipping");
								return false;
							}
							hashData[cnt] = new byte[2048];
							inData.readFully(hashData[cnt]);
							requiredUpdates++;
							skippedUpdates = 0;
						} else {
							hashData[cnt] = null;
							skippedUpdates++;
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Unable to read file " + file.getName());
				return false;
			} 


		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}

		if(all) {
			for(int cnt=0;cnt<blockSize;cnt++) {
				if(hashData[cnt] != null) {
					cache.put(hashes[cnt], new SoftReference<byte[]>(hashData[cnt]));
					hardReferencesLoop.addLast(hashData[cnt]);
					if(hardReferencesLoop.size() > 32768) {
						hardReferencesLoop.removeFirst();
					}
				}
			}
		} else {
			blockHashList.put(blockHash, hashes);

			for(int cnt=0;cnt<blockSize;cnt++) {
				File current = FAT.get(hashes[cnt]);
				if(current == null) {
					FAT.put(hashes[cnt], file);
				}
			}
		}

		//if(all) {
		//	System.out.println("Read " + requiredUpdates + " hashes worth of data from: " + file.getName());
		//} else {
		//	System.out.println("Read header from: " + file.getName());
		//}

		return true;

	}

	private static class FileCompare implements Comparator<File> {

		public int compare(File f1, File f2) {			
			return getIntFromName(f1) - getIntFromName(f2);
		}

	}

	private class HardReference<T> extends SoftReference<T> {

		public T hard;

		T getHard() {
			return hard;
		}

		HardReference(T ref) {
			super(ref);
			hard = ref;
		}
	}

	public static int getIntFromName(File file) {
		try {
			String fileName = file.getName();
			if(fileName == null || fileName.length() <3) {
				return 0;
			}
			return Integer.parseInt(file.getName().substring(3));
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public void saveHashBlockToDisk(long[] hashes, byte[][] data, int blockSize, boolean prune) {

		long blockHash = getHashBlockHash(hashes);

		if(!Main.cacheDir.isDirectory()) {
			return;
		}
		File outFile = new File(Main.cacheDir, "CPL" + getNextId());
		
		//System.out.println("File name: " + outFile.getName());
		//System.out.println("File hash: " + blockHash);

		FileOutputStream out;
		try {
			out = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			return;
		}

		try {
			GZIPOutputStream gzOut = new GZIPOutputStream(out);
			DataOutputStream outData = new DataOutputStream(gzOut);
			outData.writeLong(blockHash);
			outData.writeInt(blockSize);

			for(int cnt=0;cnt<blockSize;cnt++) {
				outData.writeLong(hashes[cnt]);
			}

			if(data!=null) {
				for(int cnt=0;cnt<blockSize;cnt++) {
					outData.write(data[cnt]);
				}
			} 

			outData.close();
		} catch (IOException ioe) {
		} finally {
			try {
				out.close();
				addFile(outFile, prune);
			} catch (IOException e) {
			}
		}

		if(data != null) {
			for(int cnt=0;cnt<blockSize;cnt++) {
				long hash = hashes[cnt];
				Reference<byte[]> ref = cache.get(hash);
				if(ref != null && !(ref instanceof SoftReference)) {
					byte[] temp = ref.get();
					cache.put(hash, new SoftReference<byte[]>(temp));
				}
			}
		}

	}

	static int directorySize = 0;
	static LinkedList<File> fileList = new LinkedList<File>();
	static Object dirSyncObject = new Object();

	static {
		File[] files = Main.cacheDir.listFiles();
		Arrays.sort(files, fileCompare);
		for(File file : files) {
			synchronized(dirSyncObject) {
				addFile(file, true);
			}
		}
	}

	static void addFile(File file, boolean prune) {
		synchronized(dirSyncObject) {
			directorySize+=file.length();
			fileList.addLast(file);
			if(prune) {
				pruneCache();
			}
		}
	}

	public static void pruneCache() {
		synchronized(dirSyncObject) {
			int limit = Globals.getCacheLimit();
			while(!fileList.isEmpty() && limit > 0 && directorySize > limit) {
				File oldest = fileList.removeFirst();
				removeFile(oldest);
			}
		}
	}

	static void removeFile(File file) {
		synchronized(dirSyncObject) {
			directorySize-=file.length();
			file.delete();
		}
	}


	static long getHashBlockHash(long[] hashes) {

		long h = 1;
		int length = hashes.length;
		for(int cnt=0;cnt<length;cnt++) {
			h += (h<<5) + hashes[cnt];
		}

		return h;

	}

	int getNextId() {
		synchronized(fileIdSync) {
			return fileId++;
		}
	}

}
