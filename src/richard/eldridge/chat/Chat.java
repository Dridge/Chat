package richard.eldridge.chat;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import richard.eldridge.mycomponents.TitleLabel;

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

	public static void main(String[] args) {
		try {
			String className = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(className);
		} catch (Exception e) {
			// do nothing
		}
		SwingUtilities.invokeLater(new Chat());
	}

	public Chat() {
		initGUI();
		setTitle("Chat");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		new Thread(this).run();
	}

	private void initGUI() {
		TitleLabel titleLabel = new TitleLabel("Chat");
		add(titleLabel, BorderLayout.PAGE_START);
		// listeners
		new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		};

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
			System.out.print(message);
			inputArea.setText("");
		}
	}

	@Override
	public void run() {
		try {
			socket = new Socket(host, PORT_NUMBER);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(this, "The server is not running!");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Connection to the server has been lost!");
		} finally {
			close();
		}
	}

	private void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// do nothing
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
}
