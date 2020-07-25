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
        new Thread(this).run();
    }
    //TODO 'trusted authorisers log in style'
        // a key word, password, code is sent to a named 'trusted' person who has logged into the chat
        // whatsapp, but secure
        // whatsapp, but invite only
        // free speech, free expression
        // one rule: don't repeatedly call for or insight other to commit acts of violence against another user
            // three strikes and your out
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
                if(null == input) {
                    keepRunning = false;
                }

                if(!input.isEmpty()) {
                    String actionCode = String.valueOf(input.charAt(0));
                    String parameters = input.substring(1);
                    String submittedName;
                    boolean added = false;
                    switch(actionCode){
                        case ActionCode.NAME: submittedName = parameters;
                            added = server.addConnection(this, name);

                    }
                    if(added) {
                        validName = true;
                        submittedName = name;
                        sendToClient(ActionCode.ACCEPTED);
                    } else {
                        sendToClient(ActionCode.REJECTED);
                    }
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
        try {
            socket.close();
        } catch (IOException e) {
            //do nothing
        }
    }

    public void sendToClient(String s){
        out.println(s);
        server.log("Sent: " + s + ", to: " + name);
    }

    public String getName() {
        return name;
    }
}
