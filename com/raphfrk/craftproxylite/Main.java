package com.raphfrk.craftproxylite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

	public static Object sleeper = new Object();
	static boolean serverEnabled = true;

	static boolean consoleInput = true;

	public static void main(String[] args, boolean consoleInput) {
		Main.consoleInput = consoleInput;
		main(args);
	}

	public static void main(String [] args) {

		Logging.log( "Starting Craftproxy-lite version " +  VersionNumbering.version );

		int listenPort;
		int defaultPort;
		String password = "";

		String usageString = "craftproxy <port to listen to> <default port> [hell] [quiet] [reconnectfile path_to_file] [verbose] [info] [auth] [clientversion num] [delay num] [local_alias alias] [debug] [banned banfile]";

		if( args.length < 2 ) {
			Logging.log( "Usage: " + usageString );
			if(consoleInput) {
				System.exit(0);
			}
			return;

		} else {
			try {
				listenPort = Integer.parseInt(args[0]);
				defaultPort = Integer.parseInt(args[1]);
				for( int pos=2;pos<args.length;pos++) {

					if( args[pos].equals("verbose"))        Globals.setVerbose(true);
					else if( args[pos].equals("hell"))           Globals.setHell(true);
					else if( args[pos].equals("info"))           Globals.setInfo(true);
					else if( args[pos].equals("auth"))           Globals.setAuth(true);
					else if( args[pos].equals("debug"))          Globals.setDebug(true);
					else if( args[pos].equals("clientversion")){ Globals.setClientVersion(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("password"))     { Globals.setPassword(args[pos+1]); pos++;}
					else if( args[pos].equals("local_alias"))  { Globals.setLocalAlias(args[pos+1]); pos++;}
					else if( args[pos].equals("quiet"))          Globals.setQuiet(true);
					else if( args[pos].equals("reconnectfile")){ ReconnectCache.init(args[pos+1]); pos++;}
					else if( args[pos].equals("banned"))       { BanList.init(args[pos+1]); pos++;}
					else if( args[pos].equals("limiter"))       { Globals.setLimiter(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("dimension"))       { Globals.setDimension(Byte.parseByte(args[pos+1])); pos++;}
					else if( args[pos].equals("fairness"))      { Globals.setFairness(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("delay"))		   { Globals.setDelay(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("window"))		   { Globals.setWindow(Long.parseLong(args[pos+1])); pos++;}
					else if( args[pos].equals("threshold"))		   { Globals.setThreshold(Long.parseLong(args[pos+1])); pos++;}
					else if( args[pos].equals("log"))              { Logging.setFilename(args[pos+1]) ; pos++;}
					else                                         password = new String(args[pos]); // game password - not used

				}

			} catch (NumberFormatException nfe) {
				Logging.log( "Unable to parse numbers");
				Logging.log( "Usage: " + usageString );
				System.exit(0);
				return;
			}
		}

		if( !Globals.isAuth() ) {
			Logging.log( "" );
			Logging.log( "WARNING: You have not enabled player name authentication");
			Logging.log( "WARNING: This means that player logins are not checked with the minecraft server");
			Logging.log( "" );
			Logging.log( "To enable name authentication, add auth to the command line" );
			Logging.log( "" );
		} else {
			Logging.log( "Name authentication enabled");
		}

		if( !ReconnectCache.isSet() ) {
			Logging.log( "WARNING: reconnectfile parameter not set");
			Logging.log( "WARNING: players will be connected to the default server regardless of last server connected to");
		}

		Logging.log( "Use \"end\" to stop the server");

		ProxyListener server = new ProxyListener( listenPort, defaultPort, password );

		server.start();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		if(consoleInput) {
			try {
				while( !in.readLine().equals("end") ) {
				}
			} catch (IOException e) {
			}

			try {
				in.close();
			} catch (IOException e) {
			}
			
			ReconnectCache.save();
			server.interrupt();
		} else {
			Logging.log("Server console disabled");
			while(true) {
				try {
					synchronized(sleeper) {
						while(serverEnabled) {
							sleeper.wait();
						}
					}
				} catch (InterruptedException ie) {
					ReconnectCache.save();
					server.interrupt();
				}
			}
		}
		
		Logging.log("Waiting for server to close");
		try {
			server.join();
		} catch (InterruptedException e) {
			Logging.log("Server interrupted while closing");
		}
		
		Logging.flush();

	}

	public static void killServer() {

		Logging.log("Killing server from Bukkit");

		synchronized(sleeper) {
			serverEnabled = false;
			sleeper.notify();
		}

	}


}
