/*
 * Simple Chat Application build on UDP; meant as a test for Distributed Systems, USEK
 Copyright 2016 zshulu
 
 Licensed under the "THE BEER-WARE LICENSE" (Revision 42):
 zshulu wrote this file. As long as you retain this notice you
 can do whatever you want with this stuff. If we meet some day, and you think
 this stuff is worth it, you can buy me a beer or coffee in return
 *
 *
 */

import java.net.InetAddress;


public class Client {

	private InetAddress address;
	private int port;
	private String username;
	private boolean admin;
	
	
	public Client(InetAddress address, int port, String username) {
		this.address = address;
		this.port = port;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public boolean isAdmin() {
		return admin;
	}


	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "Client [address=" + address + ", port=" + port + ", username="
				+ username + ", admin=" + admin + "]";
	}
	
	
	
}
