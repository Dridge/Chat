package richard.eldridge.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {
    private static String DEFAULT_NAME = "(New Client)";
    private ChatServer server;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String name = DEFAULT_NAME;

    Connection(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            sendToClient(ActionCode.SUBMIT);
            boolean validName = false;
            boolean keepRunning = true;
            while (keepRunning) {
                String input = in.readLine();
                server.log("Input received from: " + name + ": " + input);
                if(null != input && !input.isEmpty()) {
                    String actionCode = String.valueOf(input.charAt(0));
                    String parameters = input.substring(1);
                    String submittedName = "";
                    switch (actionCode) {
                        case ActionCode.NAME:
                            submittedName = parameters;
                            boolean added = server.addConnection(this, name);
                            out.print(name);
                            if (added) {
                                validName = true;
                                name = submittedName;
                                sendToClient(ActionCode.ACCEPTED);
                                String message = ActionCode.CHAT + name + " has joined the chat";
                                server.broadcast(message);
                            } else {
                                sendToClient(ActionCode.REJECTED);
                            }
                            break;
                        case ActionCode.BROADCAST:
                            if(validName) {
                                server.broadcast(ActionCode.CHAT + parameters);
                            }
                            break;
                    }
                } else {
                    keepRunning = false;
                }
            }
        } catch (IOException e) {
            server.log("Error occurred when connecting to a new client or communicating with that client");
            server.log(e.getMessage());
        } finally{
            quit();
        }
    }

    private void quit() {
    	server.log("Connection ended for " + name + ".");
        if(!name.equals(DEFAULT_NAME)) {
            server.removeConnection(name);
            if (out != null) {
            	String s = ActionCode.CHAT;
            	server.broadcast(s + name + " has left the chat");
            	out = null;
            }
        }
        try {
            socket.close();
            server.broadcast(name + " just left the chat");
        } catch (IOException e) {
            //do nothing
        }
    }

    public void sendToClient(String s){
        out.println(s);
        if(null != s && !s.isEmpty()) {
            String actionCode = String.valueOf(s.charAt(0));
	        if(ActionCode.DISCONNECT == actionCode) {
	        	quit();
	        }
        }
        server.log("Sent: " + s + ", to " + name);
    }

    public String getName() {
        return name;
    }
}
