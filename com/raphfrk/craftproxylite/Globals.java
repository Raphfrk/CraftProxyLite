package com.raphfrk.craftproxylite;

public class Globals {
	
	static private String password = null;
	
	public synchronized static String getPassword() {
		return password;
	}
	
	public synchronized static void setPassword(String password) {
		Globals.password = password;
	}
	
	static private String localAlias = "";
	
	public synchronized static String getLocalAlias() {
		return localAlias;
	}
	
	public synchronized static void setLocalAlias(String localAlias) {
		Globals.localAlias = localAlias;
	}
	
	static private boolean quiet = false;

	public synchronized static boolean isQuiet() {
		return quiet;
	}
	
	public synchronized static void setQuiet( boolean newQuiet ) {
		quiet = newQuiet;
	}
	
	static private boolean debug = false;

	public synchronized static boolean isDebug() {
		return debug;
	}
	
	public synchronized static void setDebug( boolean newDebug) {
		debug = newDebug;
	}
	
	static private boolean hell = false;

	public synchronized static boolean isHell() {
		return hell;
	}
	
	public synchronized static void setHell( boolean newHell ) {
		hell = newHell;
	}
	
	static private boolean verbose = false;

	public synchronized static boolean isVerbose() {
		return verbose;
	}
	
	public synchronized static void setVerbose( boolean newVerbose ) {
		verbose = newVerbose;
	}
	
	static private boolean info = false;

	public synchronized static boolean isInfo() {
		return info;
	}
	
	public synchronized static void setInfo( boolean newInfo ) {
		info = newInfo;
	}
	
	static private boolean authenticate = false;
	
	public synchronized static boolean isAuth() {
		return authenticate;
	}
	
	public synchronized static void setAuth( boolean newAuth ) {
		authenticate = newAuth;
	}
	
	static private boolean varyLocalhost = true;
	
	public synchronized static boolean varyLocalhost() {
		return varyLocalhost;
	}
	
	public synchronized static void setVaryLocalhost( boolean varyLocalhost ) {
		Globals.varyLocalhost = varyLocalhost;
	}
	
	static private int defaultPlayerId = 456789012;
		
	public synchronized static int getDefaultPlayerId() {
		
		return defaultPlayerId;
		
	}
	
	static private int fakeVersion = 111111;
	
	public synchronized static int getFakeVersion() {
		return fakeVersion;
	}
	
	public synchronized static void setFakeVersion( int newFakeVersion ) {
		fakeVersion = newFakeVersion;
	}
	
	static private int clientVersion = 11;
	
	public synchronized static int getClientVersion() {
		return clientVersion;
	}
	
	public synchronized static void setClientVersion( int newClientVersion ) {
		clientVersion = newClientVersion;
	}
	
	static private int delay = 5500;
	
	public synchronized static int getDelay() {
		return delay;
	}
	
	public synchronized static void setDelay( int delay ) {
		Globals.delay = delay;
	}
	
	static private int limiter = 20;
	
	public synchronized static int getLimiter() {
		return limiter;
	}
	
	public synchronized static void setLimiter( int limiter ) {
		Globals.limiter = limiter;
	}
	
	static private int fairness = 0;
	
	public synchronized static int getFairness() {
		return fairness;
	}
	
	public synchronized static void setFairness( int fairness ) {
		Globals.fairness = fairness;
	}
	
	static private long window = 0;
	
	public synchronized static long getWindow() {
		return window;
	}
	
	public synchronized static void setWindow( long window ) {
		Globals.window = window;
	}
	
	static private long threshold = 0;
	
	public synchronized static long getThreshold() {
		return threshold;
	}
	
	public synchronized static void setThreshold( long threshold ) {
		Globals.threshold = threshold;
	}
	
	static private Byte dimension = null;
	
	public synchronized static Byte getDimension() {
		return dimension;
	}
	
	public synchronized static void setDimension( Byte dimension ) {
		Globals.dimension = dimension;
	}
}
