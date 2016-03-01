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

import java.io.IOException;
import java.net.*;

public class ChatClient {

	private static DatagramSocket socket;
	private static String SERVER_HOST = "localhost";
	private static InetAddress host;
	private final static int SERVER_PORT = 6700;
	private static ChatInterface ci;
	public static void main (String[] args) {

		try {SERVER_HOST = args[1];}catch(Exception e) {System.out.println("ArgError");};

		try {
			socket = new DatagramSocket();
			host = InetAddress.getByName(SERVER_HOST);
			ci = new ChatInterface();
			String username = ci.getUsername();

			DatagramPacket request = new DatagramPacket(("JOIN" + username).getBytes(),
					("JOIN" + username).getBytes().length, host, SERVER_PORT);
			socket.send(request);

			new Runnable() {
				@Override
				public void run() {
					while(true) {
						byte [] buffer = new byte [1024];

						DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
						try { socket.receive(reply); } 
						catch (IOException e) { System.err.println(e.getMessage()); }
						String message = new String(buffer).trim();
						ci.appendMessage(message);
						if (message.equals("Server Notice: Goodbye.")) System.exit(0);
					}
				}
			}.run();
			System.exit(0);

		} catch(Exception e) {
			System.out.print("Error: " + e.getMessage());
		} finally {
			if ( socket != null ) socket.close();
		}
	}

	public static void sendRequest(String request) {
		request = request.trim();
		String requestString = "";
		if (request.startsWith("/")) {
			// It must be a command
			String args [] = request.split(" ");

			switch (args[0].trim()) {
			case "/help":
				requestString = "HELP";
				break;
			case "/me":
				requestString = "UACT" + request.substring(3);
				break;
			case "/names":
				requestString = "ENUM";
				break;
			case "/username":
				requestString = "USER" + args[1];
				ci.setUsername(args[1]);
				break;
			case "/whisper":
				if (args.length > 2) {
					String message = "";
					for (int i = 2; i < args.length; i++)
						message += args[i] + " ";
					requestString = "WHSP" + args[1].length() + args[1] + message.trim();
				}
				break;
			case "/quit":
				requestString = "QUIT";
				break;
			default:
				break;
			}
		}
		else	// must be a broadcast message
			requestString = "MESG" + request;

		// requestString = command + argument;
		DatagramPacket requestPkt = new DatagramPacket(requestString.getBytes(),
				requestString.getBytes().length,  host, SERVER_PORT);
		try { socket.send(requestPkt); }
		catch (IOException e) { System.err.println(e.getMessage()); }
	}

	public static boolean availableUsername (String username) {
		DatagramPacket request = new DatagramPacket(("CHCK" + username).getBytes(),
				username.getBytes().length + 4, host, SERVER_PORT);
		try {
			socket.send(request);

			byte [] buffer = new byte [1024];

			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			socket.setSoTimeout(10000);
			socket.receive(reply);
			socket.setSoTimeout(10 * 60 * 1000);
			
			String message = new String(buffer).trim();
			return message.equals("OK");
		} catch (Exception e) { System.out.print(e.getMessage()); }
		return false;
	}
}
