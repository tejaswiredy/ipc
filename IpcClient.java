
import java.io.*;
import java.net.*;

public class IpcClient implements Runnable {

    private static boolean closedClient = false;
    private static Socket clientSocket = null;
    private static BufferedReader sIn = null,in=null;
    private static PrintWriter output = null;
    private static String fromUser;

    @Override
    public void run() {
        String fromServer;
        try 
			
			// Client listens for any server messages
            while ((fromServer= in.readLine()) != null) {
                if(fromServer.equals("exit")){
                    break;
                }
				
				// Printout the broadcast message sent from server
                if (fromServer != null) {
                    System.out.println("Server: " + fromServer);
                }
                if (fromServer != null && fromServer.equals("You failed too many times.  Bye Bye!")) {
                    break;
                }

            }
            closedClient = true;
        } catch (IOException e) {
            System.out.println("");
        }
    }

    public static void main(String[] args) throws IOException {

        try {
            clientSocket = new Socket(args[0], Integer.parseInt(args[1]));
			
			// Open output stream reader for sending request to server
            output = new PrintWriter(clientSocket.getOutputStream(), true);
			
			// Open input stream reader for reading the messages sent by the server
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			// Read client input from cmd 
            sIn = new BufferedReader(new InputStreamReader(System.in));

        } catch (UnknownHostException e) {
            System.out.println("Unknown Host");
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        if (clientSocket != null && output != null & in != null) {
            try {
				// Start client thread
                new Thread(new IpcClient()).start();
                while (!closedClient) {
                    fromUser = sIn.readLine();
                    if(fromUser.equals("exit"))
                        break;
                    if (fromUser != null) {
                        System.out.println("Client: " + fromUser);
                        output.println(fromUser);
                    }

                }
                output.close();
                in.close();
                sIn.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("No I/O: " + e.getMessage());
                System.exit(1);
            }
        }

    }


}
