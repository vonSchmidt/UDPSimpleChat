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

import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ChatInterface extends JFrame implements MouseListener, KeyListener{

	private static final long serialVersionUID = -7303751130863917820L;
	private JTextArea chatArea;
	private JTextField inputArea;
	private JButton sendButton;
	private String username;
	private JScrollPane chatContainer;

	public ChatInterface() {
		setLayout(new FlowLayout());
		setSize(500, 500);
		chatArea = new JTextArea(30, 35);
		chatArea.setSize(getWidth() - 10, getHeight() - 150);
		chatArea.setEditable(false);
		chatArea.setFocusable(false);
		inputArea = new JTextField(30);
		inputArea.setSize(this.getWidth() - 150, 30);
		sendButton = new JButton();
		sendButton.setSize(100, 30);
		sendButton.setText("Send Message");
		sendButton.addMouseListener(this);
		inputArea.addKeyListener(this);
		chatContainer = new JScrollPane(chatArea);
		add(chatContainer);
		add(inputArea);
		add(sendButton);

		do {
			username = JOptionPane.showInputDialog("Username: (You will be prompted again, if in use)", "");
			if (username == null) System.exit(0);
		} while(!ChatClient.availableUsername(username));

		setTitle("Chat -- " + username);
		setVisible(true);
		setResizable(true);
		inputArea.requestFocusInWindow();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				ChatClient.sendRequest("/quit");
			}
		});
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (inputArea.getText().equals("")) return;
		String message = inputArea.getText().trim();
		chatArea.append("\n" + this.username + ": "+ message);
		inputArea.setText("");
		inputArea.requestFocusInWindow();

		ChatClient.sendRequest(message);
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if (inputArea.getText().equals("")) return;
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			String message = inputArea.getText().trim();
			chatArea.append("\n" + this.username + ": "+ message);
			inputArea.setText("");
			inputArea.requestFocusInWindow();
			
			ChatClient.sendRequest(message);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}


	@Override
	public void keyTyped(KeyEvent e) { 
		if (e.getKeyChar() == '/') {
			//chatArea.append("HELP COMMAND");
		}
	}

	public String getUsername() { return username; }
	public void setUsername(String username) {
		this.username = username;
		setTitle("Chat -- " + username);
	}


	public void appendMessage(String message) {
		this.chatArea.append("\n" + message.trim());
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
	}

}
