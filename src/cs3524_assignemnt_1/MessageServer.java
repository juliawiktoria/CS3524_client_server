package cs3524_assignemnt_1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;



public class MessageServer
{
    // create a new message server
    private ServerSocket serverSocket = null ;

    public static HashSet < PrintWriter > writers = new HashSet < PrintWriter > ();

    // counter for clients 
    static int id = 0; 

    // create a hashmap for client connections
    // public static Map< String, ClientConnection > clientList = new HashMap< String, ClientConnection >() ;

    public MessageServer ( int serverPort ) throws IOException, ClassNotFoundException
    {
        this.serverSocket = new ServerSocket ( serverPort );
        System.out.println("Server open! Listening for client connections!");
        // add further implementation
    }

    private void acceptClient( ) throws IOException
    {
        // add further implementation
        try 
        {
            // infinite loop listening for multiple client connections
            while (true) {

                // accepting incoming client request
                Socket client = serverSocket.accept();
                String clientIDname = "[ Client #" + Integer.toString(id) + " ]";
                System.out.println(">>> New client connection captured: " + clientIDname);

                // create input and output streams
                // BufferedReader in  = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
                // PrintWriter    out = new PrintWriter( new OutputStreamWriter( client.getOutputStream() ), true );

                // send welcome message to connected client
                // out.println( "Welcome to the socket-based ShoutServer" ) ;

                // create a new ClientConnection to handle the client
                ClientConnection clientConn = new ClientConnection(client, id);
                System.out.println(">>> Created new ClientConnection for the client " + clientIDname);

                // create a new thread for this client
                // ServerThread clientThread = new ServerThread(clientConn);
                clientConn.start();
                // add client to the ClientList
                // clientList.put(Integer.toString(id), clientConn);
                id++;
            }
        }
        catch (IOException e) {
            System.out.println("@@@@@ exception caught in MessageServer.acceptClient() @@@@@");
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    public static void main( String[] args ) throws NumberFormatException, ClassNotFoundException, IOException, SocketException
    {   
        // start the program
        System.out.println ( "Messenger> start");
        // try and catch block to ensure safety
        try
        {
            // create new message server and wait for the client
            MessageServer newServer = new MessageServer ( Integer.parseInt( args[0] )) ;
            newServer.acceptClient();
        }
        catch ( SocketException e )
        {
            System.out.println("socket exception");
            // System.out.println("Server exception: " + e.getMessage());
            // e.printStackTrace();
        }
        // possibly catch an exception and display the error message
        catch ( IOException e)
        {
            System.out.println("io exception");
            // System.out.println("Server exception: " + e.getMessage());
            // e.printStackTrace();
        }
        
        System.out.println ( "Messenger> finished") ;
    }

    private static class ClientConnection extends Thread
    {
        private Socket clientSocket;
        private String name;
        private BufferedReader reader;
        private PrintWriter writer;
        // clientID is a String, so it can be interchangeable with clientName in HashMaps
        private String clientID;
        // create hash maps for storing client connections and client messages (output streams)
        public static Map< String, ClientConnection > clientList = new HashMap < String, ClientConnection > () ;
        public static Map< String, PrintWriter > clientMessages = new HashMap < String, PrintWriter > () ;


        public ClientConnection ( Socket clientSocket, Integer clientID ) throws IOException
        {
            this.clientSocket = clientSocket;
            this.clientID = String.valueOf(clientID);
        }

        public void run ()
        {
            try
            {
                System.out.println("try block! run client connection");

                // create input and output streams
                reader  = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                writer = new PrintWriter( new OutputStreamWriter( clientSocket.getOutputStream() ), true );

                String message;

                // add new client connection to the list (use clientID when client not named)
                clientList.put(this.clientID, this);

                // add new client output stream to the list (use clientID when client not named)
                clientMessages.put(this.clientID, writer);

                while ( (message = reader.readLine()) != null )
                {
                    //check first word for a command; force lowercase to avoid ambiguity
                    String keyWord = message.split(" ")[0].toLowerCase();

                    // register new client
                    if (keyWord.equals("register"))
                    {
                        System.out.println("REGISTERING A CLIENT");
                        // get client name (second word of the message, immediately after REGISTER keyword)
                        this.name = keyWord.split(" ")[1];

                        // add the client name to the connection list and remove a record with client id
                        clientList.put(this.name, this);
                        clientList.remove(this.clientID);
                        // add the client to the message list and remove the record with client ID
                        clientMessages.put(this.name, writer);
                        clientMessages.remove(this.clientID);
                    }
                    // unregister existing client
                    else if (keyWord.equals("unregister"))
                    {
                        System.out.println("UN REGISTERING A CLIENT");
                        // remove the client from the connection list
                        clientList.remove(this.name);
                    }
                    // what the frick frack
                    // else if (keyWord.equals("asdasda"))
                    // {
                    //     System.out.println("CONFUSION");
                    // }
                    // just writing a normal message
                    else
                    {
                        System.out.println("last option else");
                        // iterate over all messages that have been written by all clients
                        for (PrintWriter writer: MessageServer.writers)
                        {
                            if (this.name == null)
                            {
                                writer.println("[ Client " + this.clientID + " ] says: " + message);
                            }
                            else
                            {
                                writer.println(this.name + " says: " + message);
                            }
                        }

                    }
                }
            }
            catch ( IOException e )
            {
                System.out.println("@@@@@ exception caught in ClientConnection.run() @@@@@");
                System.out.println("Server Exception: " + e.getMessage());
            }
        }
    }


}


// handler class for multiple client connections
