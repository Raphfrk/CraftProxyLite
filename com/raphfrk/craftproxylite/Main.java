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

		String listenHostname = null;
		String defaultHostname = null;

		String usageString = "craftproxy <listen hostname>:<listen port> <default hostname>:<default-port> .... parameters";

		if( args.length < 2 ) {
			Logging.log( "Usage: " + usageString );
			Logging.log("    auth                  Switches on authentication (not needed)");
			Logging.log("    auth_off              Switches off authentication");
			Logging.log("    staticlocalhost       Forces use of 127.0.0.1 for localhost");
			Logging.log("    rotatelocalhost       Uses a different 127.0.x.y for each connection to localhost");
			Logging.log("    clientversion  <num>  Allows manually setting of client version");
			Logging.log("    password <password>   Sets password for multi-LAN/global mode");
			Logging.log("    reconnectfile <file>  Sets the reconnect file");
			Logging.log("    banned <file>         Sets the banned list file");
			Logging.log("    log <file>            Redirects output to a log file");
			Logging.log("    dimension <num>       Sets the dimension (-1 = hell, 0=normal)");
			Logging.log("    seed <num>            Sets the world seed");
			Logging.log("    monitor <period ms>   Enables bandwidth use logging");
			Logging.log("    quiet:                Reduces logging");
			Logging.log("    disable_flood:        Disables flood protection");
			Logging.log("    info:                 Gives more information");
			Logging.log("    debug:                Gives debug info");
					
					
			if(consoleInput) {
				System.exit(0);
			}
			return;

		} else {
			try {
				listenHostname = args[0];
				defaultHostname = args[1];
				for( int pos=2;pos<args.length;pos++) {

					if( args[pos].equals("verbose"))        Globals.setVerbose(true);
					else if( args[pos].equals("info"))           Globals.setInfo(true);
					else if( args[pos].equals("auth"))           Globals.setAuth(true);
					else if( args[pos].equals("auth_off"))       Globals.setAuth(false);
					else if( args[pos].equals("staticlocalhost"))  Globals.setVaryLocalhost(false);
					else if( args[pos].equals("rotatelocalhost"))  Globals.setVaryLocalhost(true);
					else if( args[pos].equals("debug"))          Globals.setDebug(true);
					else if( args[pos].equals("clientversion")){ Globals.setClientVersion(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("password"))     { Globals.setPassword(args[pos+1]); pos++;}
					else if( args[pos].equals("quiet"))          Globals.setQuiet(true);
					else if( args[pos].equals("disable_flood")) Globals.setFlood(false);
					else if( args[pos].equals("reconnectfile")){ ReconnectCache.init(args[pos+1]); pos++;}
					else if( args[pos].equals("banned"))       { BanList.init(args[pos+1]); pos++;}
					else if( args[pos].equals("dimension"))       { Globals.setDimension(Byte.parseByte(args[pos+1])); pos++;}
					else if( args[pos].equals("monitor"))       { Globals.setMonitor(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("seed"))       { Globals.setSeed(Long.parseLong(args[pos+1])); pos++;}
					else if( args[pos].equals("log"))              { Logging.setFilename(args[pos+1]) ; pos++;}
					else                                        {System.out.println("Unknown field: " + args[pos]); System.exit(0);}

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

		ProxyListener server = new ProxyListener( listenHostname, defaultHostname );

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
			Logging.log("[CraftProxy-Lite] Server console disabled");
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
		ReconnectCache.save();
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
