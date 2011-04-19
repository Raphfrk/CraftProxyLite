package com.raphfrk.craftproxylite;

public class ReconnectCache {

	static MyPropertiesFile pf = null;

	static void init(String filename) {

		if( pf == null ) {
			pf = new MyPropertiesFile(filename);
			pf.load();
		}

	}

	static synchronized void store(String player, String hostname, int port) {
		if(pf==null) return;
		pf.setString(player,hostname + ":" + port);

	}
	
	static synchronized boolean isSet() {
		return pf != null;
	}

	static synchronized String get(String player) {
		if(pf==null) return "";
		return pf.getString(player, "");
	}

	static synchronized void save() {
		if(pf==null) return;
		pf.save();
	}

	static synchronized void remove(String player) {
		if(pf==null) return;
		pf.removeRecord(player);
	}

	static String getHost(String combined, String def) {

		if(pf==null) return def;
		
		String[] split = combined.split(":");

		if(combined.trim().equals("") || split.length<2 ) {
			return def;
		} else {
			return split[0];
		}

	}

	static int getPort(String combined, int def) {
		
		if(pf==null) return def;

		String[] split = combined.split(":");

		if(combined.trim().equals("") || split.length<2 ) {
			return def;
		} else {
			try {
				return Integer.parseInt(split[1]);
			} catch (NumberFormatException nfe) {
				return def;
			}
		}

	}

}
