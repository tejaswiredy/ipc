import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class IpcServer extends Thread {

    private static int port;
    static long time;
    static ArrayList<AcceptClient> threads =  new ArrayList<AcceptClient>();
    static ArrayList<AcceptClient> clientThreads=  new ArrayList<AcceptClient>();

    public static ArrayList<String>getLastOneHrsClientConnected(){
        ArrayList<String> lastOneHrConnectedClients = new ArrayList<String>();
        for(AcceptClient thread: clientThreads){
            if(System.currentTimeMillis()-thread.getTimeEntered()<3600000){
                if(!lastOneHrConnectedClients.contains(thread.getUserName())){
                    lastOneHrConnectedClients.add(thread.getUserName());
                }
            }
        }

        for(String name: lastOneHrConnectedClients){
            if(name==null){
                lastOneHrConnectedClients.remove(name);
            }
        }
        return lastOneHrConnectedClients;
    }

    public static ArrayList<AcceptClient> getConnectedClients(){
        return threads;
    }

    public static void removeConnectedClient(String name){
        int index=0,n=0;
        for(AcceptClient thread:threads){
            if(thread.getUserName().equals(name))
                index=n;
            n++;
        }
        threads.remove(index);
    }


    public static void broadcastMessageToAllConnectedClients(String m) {
		// Sending the broadcast message to all connected the clients
        for (AcceptClient ac : threads) {
            ac.message(m);
        }

    }


 
 public static void main(String[] args) throws Exception {
        int clientCount = 0;
        boolean serverCond = true;
		// Store port number 
        port=Integer.parseInt(args[0]);
		System.out.println("port number is======"+port);
		// Storing the logged in user details
        String[][] usersDetails=new String[0][0];
        ServerSocket serverSocket = null;
        Scanner input;

        try {
            String wholeLine;
            int noCredentialDetails = 0,row=0;
            String[] arr;
            File in = new File("clientcredential.txt");
            input = new Scanner(in);
			
			// Count number of users from the txt file
            while (input.hasNext()) {
                noCredentialDetails++;
                input.nextLine();
            }
            usersDetails = new String[noCredentialDetails][2];
			// Read the txt file as input stream
            input = new Scanner(in);
            System.out.println("USERNAME \t PASSWORD\n");
			
			// Read client credentials from the txt file and store in usersDetails
            while (input.hasNext()) {
                wholeLine = input.nextLine();
                arr = wholeLine.split("\\s+");
                usersDetails[row][0] = arr[0];
                usersDetails[row][1] = arr[1];
                System.out.println(arr[0]+" ===> "+arr[1]+"\n");
                row++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        try {
			// initialize socket with the port number passed e.g. localhost 4119
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error connecting via port " + port);
            System.exit(-1);
        }
		
		// Keeping the server alive
        while (serverCond) {
			// Accepting the client request
            AcceptClient client = new AcceptClient(serverSocket.accept(), usersDetails, time=System.currentTimeMillis());
			
			// Adding the client logged in to threads
            threads.add(client);
            clientThreads.add(client); // [new AcceptClient('', 'windows', 82397598275), new AcceptClient('', 'facebook', 823750275)]
            client.start();

        }
        System.out.println(clientCount);
        serverSocket.close();
    }





    

    
}
