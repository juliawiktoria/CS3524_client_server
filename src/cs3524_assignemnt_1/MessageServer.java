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

    public MessageServer ( int serverPort ) throws IOException, ClassNotFoundException
    {
        this.serverSocket = new ServerSocket ( serverPort );
        System.out.println("Server open! Listening for client connections!");
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

                // create a new ClientConnection to handle the client
                ClientConnection clientConn = new ClientConnection(client, id);
                // System.out.println(">>> Created new ClientConnection for the client " + clientIDname);

                // start new client thread
                clientConn.start();

                // increment id number for the next client
                id++;
            }
        }
        catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    public static void main( String[] args ) throws NumberFormatException, ClassNotFoundException, IOException, SocketException
    {   
        if (args.length < 1) {
            System.err.println( "Usage: java MessageServer <port>" ); 
            return;
        } 
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
        }
        // possibly catch an exception and display the error message
        catch ( IOException e)
        {
            System.out.println("io exception");
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
                // create input and output streams
                reader  = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
                writer = new PrintWriter( new OutputStreamWriter( this.clientSocket.getOutputStream() ), true );

                String message;

                // add new client connection to the list (use clientID when client not named)
                clientList.put(this.clientID, this);

                // add new client output stream to the list (use clientID when client not named)
                clientMessages.put(this.clientID, writer);
                MessageServer.writers.add(writer);

                while ( (message = reader.readLine()) != null )
                {
                    //check first word for a command; force lowercase to avoid ambiguity
                    String keyWord = message.split(" ")[0].toLowerCase();

                    // register new client
                    if (keyWord.equals("register"))
                    {
                        // get client name (second word of the message, immediately after REGISTER keyword)
                        this.name = message.split(" ")[1];

                        // add the client name to the connection list and remove a record with client id
                        clientList.put(this.name, this);
                        clientList.remove(this.clientID);
                        clientMessages.put(this.name, writer);
                        clientMessages.remove(this.clientID);
                    }
                    // unregister existing client
                    else if (keyWord.equals("unregister"))
                    {
                        // remove the client from the connection list
                        clientList.remove(this.name);
                        this.name = null;
                    }
                    else if (keyWord.equals("quit"))
                    {
                        // remove the client from the connection list
                        clientList.remove(this.name);
                        this.name = null;
                        this.clientSocket.close();
                    }
                    else
                    {
                        // iterate over all messages that have been written by all clients
                        for (PrintWriter out: MessageServer.writers)
                        {
                            if (this.name == null)
                            {
                                out.println("[ Client " + this.clientID + " ] says: " + message);
                            }
                            else
                            {
                                out.println("[ " + this.name + " ] says: " + message);
                            }
                        }

                        if (this.name == null)
                        {
                            System.out.println("[ Client " + this.clientID + " ] says: " + message);
                        }
                        else
                        {
                            System.out.println("[ " + this.name + " ] says: " + message);
                        }
                    }
                }
                clientList.remove(this.name);
                clientList.remove(this.clientID);
            }
            catch ( IOException e )
            {
                System.out.println("Server Exception: " + e.getMessage());
            }
        }
    }
}

