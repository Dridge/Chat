package richard.eldridge.chat;

import richard.eldridge.mycomponents.TitleLabel;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.swing.UIManager.getCrossPlatformLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;

public class ChatServer extends JFrame implements Runnable {
	private static final int PORT_NUMBER = 63458;
	private static final long serialVersionUID = 1L;
	private JTextArea logArea = new JTextArea(10, 30);
	private JButton toggleButton = new JButton("Start");
	private ServerSocket serverSocket;
	private List<Connection> connections = new ArrayList<Connection>();

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		try {
			String className = getCrossPlatformLookAndFeelClassName();
			setLookAndFeel(className);
		} catch (Exception e) {
			// do nothing
		}
		new ChatServer();
	}

	public ChatServer() {
		initGui();
		setTitle("Chat Server");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	private void initGui() {
		Component titleLabel = new TitleLabel("Chat Server");
		add(titleLabel, BorderLayout.PAGE_START);

		// Main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel, BorderLayout.CENTER);

		// Log area
		logArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(logArea);
		mainPanel.add(scrollPane, BorderLayout.PAGE_END);
		DefaultCaret caret = (DefaultCaret) logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		// Button panel
		JPanel buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.PAGE_END);
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleServer();
			}
		});
		buttonPanel.add(toggleButton);
		getRootPane().setDefaultButton(toggleButton);

		//listeners
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stop();
				System.exit(0);
			}
		});
	}

	public void log(String message) {
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String timeStamp = dateFormat.format(time);
		logArea.append(timeStamp + ": " + message + "\n");
	}

	private void toggleServer() {
		if (toggleButton.getText().equals("Stop")) {
			//stop the server
			stop();
			toggleButton.setText("Start");
		} else {
			toggleButton.setText("Stop");
			new Thread(this).start();
		}
	}

	private void stop() {
		log("Attempting to stop server");
		if(serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				log("Unable to close the server connection.");
				log(e.getMessage());
			}
		}
	}

	@Override
	public void run() {
		log("Starting Chat server...");
		try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)){
			while(true) {
				Socket socket = serverSocket.accept();
				log("Server is starting a new connection...");
				new Connection(this, socket);
			}
		} catch (IOException e) {
			log("An exception was caught while trying to listen on port: " + PORT_NUMBER);
		}
	}

	public boolean addConnection(Connection newConnection, String newName) {
		boolean added = false;
		boolean found;
		synchronized (connections) {
			found = connections.stream().anyMatch(e -> e.getName().equals(newName));
		}
		if (!found) {
			connections.add(newConnection);
			added = true;
		}
		return added;
	}

	public void removeConnection(String removeName) {
		synchronized (connections) {
			connections.removeIf(e-> e.getName().equals(removeName));
		}
	}
	
	public void broadcast(String s) {
		synchronized (connections) {
			connections.forEach(e -> e.sendToClient(s));
		}
	}
}
