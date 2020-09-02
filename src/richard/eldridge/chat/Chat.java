package richard.eldridge.chat;

import richard.eldridge.mycomponents.TitleLabel;
import richard.eldridge.networking.LogInDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class Chat extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	public static final int PORT_NUMBER = 63458;
	private String name = "someName";
	private String host = "localhost";
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	private JTextArea chatArea = new JTextArea(20, 20);
	private JTextArea inputArea = new JTextArea(3, 20);

	private LogInDialog logInDialog = new LogInDialog("Chat");

	public static void main(String[] args) {
		try {
			String className = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(className);
		} catch (Exception e) {
			// do nothing
		}
		new Chat();
	}

	public Chat() {
		initGUI();
		setTitle("Chat");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		login();
		new Thread(this).run();
	}

	private void login() {
		logInDialog.login();
		if(logInDialog.isCanceled()) {
			close();
		}
		host = logInDialog.getIpAddress();
		name = logInDialog.getUserName();
		System.out.println(host + ",  " + name);
	}

	private void initGUI() {
		TitleLabel titleLabel = new TitleLabel("Chat");
		add(titleLabel, BorderLayout.PAGE_START);
		// listeners
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		// main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel, BorderLayout.CENTER);

		//chat area
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		Insets marginInsets = new Insets(3, 3, 3, 3);
		chatArea.setMargin(marginInsets);
		JScrollPane chatScrollPane = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(chatScrollPane);
		
		//input area
		JLabel messageLabel = new JLabel("Type your message here:");
		mainPanel.add(messageLabel);
		inputArea.setLineWrap(true);
		inputArea.setMargin(marginInsets);
		//enter key gubbins
		inputArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e){
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					send();
				}
			}
		});
		JScrollPane inputScrollPanel = new JScrollPane(inputArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(inputScrollPanel);
		
		//button panel
		JPanel buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.PAGE_END);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		buttonPanel.add(sendButton);
	}

	protected void send() {
		String message = "";
		message = inputArea.getText().trim();
		if(message.length() > 0) {
			inputArea.setText("");
			out.println(ActionCode.BROADCAST + name + ": " +  message);
		}
		inputArea.grabFocus();
	}

	@Override
	public void run() {
		try {
			socket = new Socket(host, PORT_NUMBER);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			boolean keepRunning = true;
			while(keepRunning) {
				String input = in.readLine();
				if (null == input || input.isEmpty()) {
					keepRunning = false;
				} else {
					String actionCode = String.valueOf(input.charAt(0));
					String parameters = input.substring(1);
					switch (actionCode) {
						case ActionCode.SUBMIT:
							out.println(ActionCode.NAME + ": " + name);
							break;
						case ActionCode.ACCEPTED:
							setTitle(name);
							inputArea.requestFocus();
							break;
						case ActionCode.REJECTED:
							JOptionPane.showMessageDialog(this, "User name " + name + " is not available.");
							login();
							out.println(ActionCode.NAME + ": " + name);
							break;
						case ActionCode.QUIT:
							keepRunning = false;
							break;
						case ActionCode.CHAT:
							Toolkit.getDefaultToolkit().beep();
							chatArea.append(parameters + "\n\n");
							//scroll the chat area
							String text = chatArea.getText();
							Integer endOFText = text.length();
							chatArea.setCaretPosition(endOFText);
							break;
					}
				}
			}
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(this, "The server is not running!");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Connection to the server has been lost!");
		} finally {
			close();
		}
	}

	private void close() {
		try {
			if(out != null) {
				out.println(ActionCode.QUIT);
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// do nothing
			e.printStackTrace();
		}
		System.exit(0);
	}


}
