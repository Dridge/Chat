package richard.eldridge.chat;

import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.UIManager.getCrossPlatformLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
		invokeAndWait(new Runnable() {
			@Override
			public void run() {
				new ChatServer();
			}
		});
	}

	public ChatServer() {
		initGui();
		setTitle("Chat Server");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
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

			private void startServer() {
				startButton.setEnabled(false);
				new Thread();
			}

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
					if(! serverSocket.isClosed()) {
						try {
							serverSocket.close();
						} catch (IOException e) {
							// do nothing
						}
					}
				}
			}
		});
		buttonPanel.add(startButton);
		getRootPane().setDefaultButton(startButton);
	}

	public void log(String message) {
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String timeStamp = dateFormat.format(time);
		logArea.append(timeStamp + ": " + message + "\n");
	}

	@Override
	public void run() {
		
	}

}
