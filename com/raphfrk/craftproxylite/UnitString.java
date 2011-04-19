package com.raphfrk.craftproxylite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UnitString extends ProtocolUnit {

	private String value;

	@Override
	public String read(DataInputStream in, PassthroughConnection ptc) {

		Short length;

		while(true) {
			try {
				length = in.readShort();
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(ptc)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}

			super.timeout = 0;
			break;
		}

		if(length > 5000) {
			System.out.println("String length exceeded limit (>5000), breaking connection");
			return null;
		}

		StringBuilder s = new StringBuilder((int)length + 1);

		for(int cnt = 0; cnt < length; cnt++) {
			char c;
			while(true) {
				try {
					c = in.readChar();
				} catch ( SocketTimeoutException toe ) {
					if(timedOut(ptc)) {
						continue;
					}
					return null;
				} catch (IOException e) {
					return null;
				}

				super.timeout = 0;
				break;
			}
			s.append(c);
		}

		value = s.toString();
		return value;

	}

	@Override
	public String write(DataOutputStream out, PassthroughConnection ptc) {

		while(true) {
			try {
				out.writeShort(value.length());
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(ptc)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}

			super.timeout = 0;
			break;
		}
		
		int length = value.length();
		
		for(int cnt=0;cnt<length;cnt++) {
			while(true) {
				try {
					out.writeShort(value.charAt(cnt));
				} catch ( SocketTimeoutException toe ) {
					if(timedOut(ptc)) {
						continue;
					}
					return null;
				} catch (IOException e) {
					return null;
				}

				super.timeout = 0;
				break;
			}
		}

		return value;
	}

	@Override
	public String pass(DataInputStream in, DataOutputStream out, PassthroughConnection ptc) {
		Short length;

		while(true) {
			try {
				length = in.readShort();
				out.writeShort(length);
			} catch ( SocketTimeoutException toe ) {
				if(timedOut(ptc)) {
					continue;
				}
				return null;
			} catch (IOException e) {
				return null;
			}

			super.timeout = 0;
			break;
		}

		if(length > 5000) {
			ptc.printError("String length exceeded limit (>5000), breaking connection");
			return null;
		}

		for(int cnt = 0; cnt < length; cnt++) {
			char c;
			while(true) {
				try {
					c = in.readChar();
				} catch ( SocketTimeoutException toe ) {
					if(timedOut(ptc)) {
						continue;
					}
					return null;
				} catch (IOException e) {
					return null;
				}

				super.timeout = 0;
				break;
			}
			while(true) {
				try {
					out.writeChar(c);
				} catch ( SocketTimeoutException toe ) {
					if(timedOut(ptc)) {
						continue;
					}
					return null;
				} catch (IOException e) {
					return null;
				}

				super.timeout = 0;
				break;
			}
		}

		value = "";
		return value;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

}