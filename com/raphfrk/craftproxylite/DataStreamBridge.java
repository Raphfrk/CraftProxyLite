package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class DataStreamBridge extends KillableThread {
	
	final DataInputStream in;
	final DataOutputStream out;
	final PassthroughConnection ptc;
	
	DataStreamBridge(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		this.in = in;
		this.out = out;
		this.ptc = ptc;
	}
	
	public void run() {
		
		boolean eof = false;
		
		byte[] buffer = new byte[65536];
		
		int timeout = 0;
		
		while(!eof && !super.killed()) {
			int read=0;
			try {
				read = in.read(buffer);
			} catch (SocketTimeoutException ste) {
				if(!super.killed()) {
					timeout++;
					if(timeout > 20) {
						ptc.printLogMessage("Connection timed out");
						return;
					}
					continue;
				}
			} catch (IOException e) {
				ptc.printLogMessage("Unable to read from socket");
				return;
			}
			timeout = 0;
			if(read == -1) {
				ptc.printLogMessage("Connection reached end");
				eof = true;
			} else {
				try {
					out.write(buffer, 0, read);
				} catch (IOException e) {
					ptc.printLogMessage("Error writing to socket");
					return;
				}
			}
		}
		
		ptc.interrupt();
		
	}
	
}
