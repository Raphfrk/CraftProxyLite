package com.raphfrk.craftproxylite;

public class KillableThread extends Thread {
	
	boolean killed = false;

	public boolean killed() {
		if(Thread.interrupted()) {
			killed = true;
		}
		
		return killed;
	}
	
	protected void kill() {
		killed = true;
	}
	
}
