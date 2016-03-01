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

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

public class ChatServer {
	
	
	private static ArrayList<Client> clients = new ArrayList<Client>();
	private static HashMap<String, Client> addresses = new HashMap<String, Client>();
	private static  DatagramSocket socket = null;
	private static final int PORT_NUMBER = 6700;
	
	public static void main (String[] args) {
		try {
			try {
				System.out.println("I am Server, running on " + NetworkInterface.getByName("wlan0").getInetAddresses().nextElement().toString() + ":" + PORT_NUMBER);
			} catch (NullPointerException e) {
				System.out.println("I am Server, I am running on port " + PORT_NUMBER);
			}
			socket = new DatagramSocket(PORT_NUMBER);
			
			while (true){
				byte [] buffer = new byte [1024];
				
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				
				socket.receive(request);
				System.out.println(new String(buffer).trim());
				try {
					DatagramPacket reply = handleRequest(request, buffer);
				
				if (reply != null)
					socket.send(reply);
				
				} catch (Exception e) {
					System.err.println("Could not handle request\n");
					e.printStackTrace();
					System.out.println();
				}
			}
			
		} catch(SocketException e) {
			System.out.print("Socket: " + e.getMessage());
		} catch(IOException e){
			System.out.print("IO: " + e.getMessage());
		}
		finally {
			if (socket != null) socket.close();
		}
	}
	
	@SuppressWarnings("unused")
	private static void wait(int milliseconds) {
		long 	t1 = System.currentTimeMillis(),
				t2 = System.currentTimeMillis();
		
		while (t2 - t1 < milliseconds) {
			t2 = System.currentTimeMillis();
		}
	}
	
	private static DatagramPacket handleRequest(DatagramPacket request, byte [] buffer) {
		// reserve 4 bytes for request command
		String 	requestString = new String(buffer).trim();
		String 	requestCommand = requestString.substring(0, 4),
				requestArgument = requestString.substring(4);

		String message = "";
		Client sender = addresses.get(request.getAddress().toString() + request.getPort());
		switch (requestCommand) {
		case "CHCK":
			String uncheckedUname = requestArgument.trim();
			message = checkUsernameAvailable(uncheckedUname)? "OK" : "NOTOK";
			break;
		case "JOIN":
			String username = requestArgument.trim();
			System.out.println(username + " joined chat.");
			Client client = new Client(request.getAddress(), request.getPort(), username.trim());
			clients.add(client);
			addresses.put(request.getAddress().toString() + request.getPort(), client);
			message = "Server Notice: " + "Welcome to the chatroom.";
			broadcastMessage("Server notice: " + username + " has joined the chatroom.", request.getAddress(), request.getPort());
			break;
			
		case "MESG":
			if (sender == null) return null;
			String user = sender.getUsername().trim(); // User name of the sender
			message = user + ": " + requestArgument;
			broadcastMessage(message, request.getAddress(), request.getPort());
			return null;
			
		case "UACT":
			if (sender == null) return null;
			broadcastMessage(" * " + sender.getUsername() + " " + requestArgument.trim() + " *", request.getAddress(), request.getPort());
			break;
		case "WHSP":
			if (sender == null) return null;
			String lengthStr = requestArgument.split("[a-zA-Z]")[0];
			int length = Integer.parseInt(lengthStr);
			String pack = requestArgument.substring(lengthStr.length());
			String whisperTo = pack.substring(0, length);
			String whisper = pack.substring(length);
			Client whisperClient = getClient(whisperTo);
			if (whisperClient == null) {
				message = "This user does not exist.";
				break;
			}
			sendMessage(" [Private] " + sender.getUsername() + ": " + whisper, 
					whisperClient.getAddress(), whisperClient.getPort());
			break;
			
		case "ENUM":
			if (sender == null) return null;
			message = "Names:";
			for (Client c : clients) message += "\n\t* " + c.getUsername();
			break;
			
		case "USER":
			if (sender == null) return null;
			String newUsername = requestArgument.trim();
			addresses.get(request.getAddress().toString() + request.getPort())
				.setUsername(newUsername);
			message = "Server Notice: You are now known as " + newUsername;
			break;

		case "HELP":
			if (sender == null) return null;
			message = "Commands:\n\n/help:\tDisplay help message.\n" +
					"/names:\tList users in chatroom.\n" +
					"/whisper:\tSend a private message to a user. /whisper user msg\n" +
					"/me:\tExecute an action. /me action\n" +
					"/username:\tChange your username. /username new_username\n" +
					"/quit:\tQuit the chatroom.";
			break;
			
		case "QUIT":
			if (sender == null) return null;
			message = "Server Notice: Goodbye.";
			String username1 = sender.getUsername().trim(); // User name of the sender
			System.out.println(username1 + " has quit the chatroom.");
			broadcastMessage("Server Notice: " + username1 + " has left the chatroom.", request.getAddress(), request.getPort());
			clients.remove(addresses.remove(request.getAddress().toString() + request.getPort()));
			break;
		
		default:
			break;
		}
		
		if (message.equals("")) return null;
		
		DatagramPacket reply = new DatagramPacket(message.getBytes(),
				message.getBytes().length, request.getAddress(), request.getPort());
		
		return reply;
	}
	
	private static boolean checkUsernameAvailable(String uncheckedUname) {
		for (Client c : clients) {
			if (c.getUsername().equals(uncheckedUname)) return false;
		}
		return true;
	}
	
	private static Client getClient(String username) {
		for (Client c : clients)
			if (c.getUsername().equals(username)) return c;
		return null;
	}
	
	private static void sendMessage(String message, InetAddress destinationAddress, int destinationPort) {
		Client client = addresses.get(destinationAddress.toString() + destinationPort);
		DatagramPacket reply = new DatagramPacket(message.getBytes(),
				message.getBytes().length, client.getAddress(), client.getPort());
		try { socket.send(reply); } catch(IOException e) { System.err.println(e.getMessage());};
	}

	private static void broadcastMessage(String message, InetAddress senderAddress, int senderPort) {
		for (Client client : clients) {
			if (client.getAddress().toString().equals(senderAddress.toString()) && client.getPort() == senderPort) continue;
			DatagramPacket reply = new DatagramPacket(message.getBytes(),
					message.getBytes().length, client.getAddress(), client.getPort());
			try { socket.send(reply); } catch(IOException e) { System.err.println(e.getMessage());};
		}
	}
}
