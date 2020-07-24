import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.io.BufferedWriter;


public class AcceptClient extends Thread {

    private Socket socket = null;
    private PrintWriter dout = null;
    private BufferedReader din = null;
    private BufferedWriter bw = null;
    private static int atUserName;
    private static int atPass;
    private static int atAuthentication;
    private static int atLoggedIn;
    private int state;
    private String userName = null;
    private String userPassword = null;
    int logInAttempts;
    String[][] allUsers;
    long timeEntered;
    String inputLine;
    String outputLine;

    public AcceptClient(Socket s, String[][] userlist, long t) throws IOException {
        socket = s;
        allUsers = userlist;
        atUserName = 0;
        atPass = 1;
        atAuthentication = 2;
        atLoggedIn = 3;
        state = atUserName;
        logInAttempts = 1;
        timeEntered = t;
    }

    public void message(String m) {
        dout.println(m);
    }

    public String getUserName() {
        return userName;
    }

    public long getTimeEntered() {
        return timeEntered;
    }

    public void run() {
        System.out.println("Client connected to socket: " + socket.toString());
        try {
			// Creating output stream for sending response to client request
            dout = new PrintWriter(socket.getOutputStream(), true);
			
			// gets client request as input stream
            din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
            outputLine = handleRequest(null);
            dout.println(outputLine);
			
			// Checking the input stream for further requests from client(s).
            while((inputLine=din.readLine())!=null) {
				// handles client request e.g. login, whoelse, wholasthr etc
                outputLine = handleRequest(inputLine);
				
				// handling the client exit request
                if(inputLine.equals("exit")){
					
					// Removing the client based on current username
                    IpcServer.removeConnectedClient(this.userName);
                    break;
                }
                if (outputLine != null) {
                    dout.println(outputLine);
                    if (outputLine.equals("exit")) {
                        System.out.println("Server is closing socket for client connected:" + socket.getLocalSocketAddress());
                        break;
                    }
                } else {
                    System.out.println("No output");
                    break;
                }
                }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dout.close();
                din.close();
                socket.close();
            } catch (Exception e) {
                System.out.println("Failed to close I/O");
            }
        }
    }

    public String handleRequest(String clientRequest) {
        String reply = null;
        String[] broadcastOperationDetails;
        try {
            if (clientRequest != null && clientRequest.equalsIgnoreCase("login")) {
                state = atPass;
            }
            if (clientRequest != null && clientRequest.equalsIgnoreCase("exit")) {
                IpcServer.removeConnectedClient(this.userName);
                return "exit";
            }

            switch(state){
                case 0:
                    reply = "Enter user name: ";
                    state = atPass;
                    break;
                case 1:
                    userName = clientRequest;
                    reply = "Enter password: ";
                    state = atAuthentication;
                    break;
                case 2:
                    userPassword = clientRequest;
                    int index = -1;
					
					// Find the client request is of from the existing user from txt file
                    for (int i = 0; i < allUsers.length; i++) {
                        if (userName.equalsIgnoreCase(allUsers[i][0])) {
                            index = i;
                        }
                    }
					
					// check if the username exist 
                    if (index == -1) {
                        logInAttempts++;
                        if (logInAttempts > 3) {
                            return "You failed too many times.";
                        }
                        reply = "Login Failed, try again. (Attempt #" + logInAttempts + ") Username: ";
                        state = atPass;
						
                    } else if (userName.equalsIgnoreCase(allUsers[index][0]) && userPassword.equals(allUsers[index][1])) {
						// After successfull login
                        reply = "Welcome to IPC!";

                        state = atLoggedIn;
                    } else {
                        logInAttempts++;
                        if (logInAttempts > 3) {
                            return "You failed too many times.  ";
                        }
                        reply = "Login Failed, try again. (Attempt #" + logInAttempts + ") Username: ";
                        state = atPass;

                    }
                    break;
                case 3:
                    int n = 0;
                    if (clientRequest != null && clientRequest.equalsIgnoreCase("whoelse")) {
                        String clientList = "";
                        ArrayList<AcceptClient> connectedClients = new ArrayList<AcceptClient>();
						
						// get list of all connected clients
                        connectedClients = IpcServer.getConnectedClients();
                        for (AcceptClient client : connectedClients) {
                            if (!client.getUserName().equals(this.userName)) {
                                if (n == 0) {
                                    clientList = client.getUserName();
                                } else {
                                    clientList = new StringBuilder(clientList).append(" ").append(client.getUserName()).toString();
                                }
                            }
                            n++;
                        }
                        reply = clientList;
                    }
                    else if (clientRequest != null && clientRequest.equalsIgnoreCase("wholasthr")) {
                        String clientList = "";
                        ArrayList<String> lastOneHrConnectedClients = new ArrayList<String>();
                        lastOneHrConnectedClients = IpcServer.getLastOneHrsClientConnected();
                        for (String s : lastOneHrConnectedClients) {
                            clientList =  new StringBuilder(clientList).append(" ").append(s).toString();
                        }
                        reply = clientList;
                    }
                    else if (clientRequest != null && clientRequest.startsWith("broadcast")) {
                        broadcastOperationDetails = clientRequest.split("\\s+");
                        String message = broadcastOperationDetails[1];
                        for(int i=2; i<broadcastOperationDetails.length;i++)
                            message=message+ " " +broadcastOperationDetails[i];
                        IpcServer.broadcastMessageToAllConnectedClients(message);
                        reply = "Broadcasted message to All Connected Clients";
                    } else {
                        reply = clientRequest;
                        state = atLoggedIn;
                    }
                    break;
                default:
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
            return "exit";
        }

        return reply;
    }


}
