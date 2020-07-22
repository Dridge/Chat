package richard.eldridge.chat;

import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.UIManager.getCrossPlatformLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import richard.eldridge.mycomponents.TitleLabel;

public class ChatServer extends JFrame implements Runnable {
	private static final int PORT_NUMBER = 63458;
	private static final long serialVersionUID = 1L;
	private JTextArea logArea = new JTextArea(10, 30);
	private JButton startButton = new JButton("Start");
	private ServerSocket serverSocket;
	

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		try {
			String className = getCrossPlatformLookAndFeelClassName();
			setLookAndFeel(className);
		} catch (Exception e) {
			// do nothing
		}
		invokeAndWait(new ChatServer());
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
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startServer();
			}
		});
		buttonPanel.add(startButton);
		getRootPane().setDefaultButton(startButton);
		
		//listeners
		new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stop();
				System.exit(0);
			}
		};
	}

	public void log(String message) {
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String timeStamp = dateFormat.format(time);
		logArea.append(timeStamp + ": " + message + "\n");
	}


	private void startServer() {
		if (startButton.getText().equals("Stop")) {
			//stop the server
			
		} else {
			startButton.setText("Stop");
			new Thread(this);
		}
	}
	
	private void stop() {
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
		log("Server is running...");
		try {
			serverSocket = new ServerSocket(PORT_NUMBER);
			while(true) {
				Socket socket = serverSocket.accept();
				log("Server is starting a new connection...");
			}
		} catch (IOException e) {
			log("An exception was caught while trying to listen on port: " + PORT_NUMBER);
		} finally {
			if(serverSocket != null && !serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					log("Unable to close the server connection.");
					log(e.getMessage());
				}
			}
		}
	}

}
