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
import java.util.ArrayList;
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

        // create hash map for group messages
        public static Map < String, ArrayList < String > > groupMessages = new HashMap < String, ArrayList < String > > ();

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
                String[] separateWords;

                // create an array list for usernames
                ArrayList < String > usernames = new ArrayList < String > ();

                // add new client connection to the list (use clientID when client not named)
                clientList.put(this.clientID, this);

                // add new client output stream to the list (use clientID when client not named)
                clientMessages.put(this.clientID, writer);
                MessageServer.writers.add(writer);

                // while ( (message = reader.readLine()) != null )
                while ( ! this.clientSocket.isClosed() ) // use the command from the assignment notes??? works the same what the frick frack
                {
                    message = reader.readLine();
                    // array of words in the message
                    separateWords = message.split(" ");

                    //check first word for a command; force lowercase to avoid ambiguity
                    String keyWord = separateWords[0].toLowerCase();
                    
                    // LONG IF-THEN-ELSE INCOMING!
                    // needs to check for keywords

                    // register new client
                    if (keyWord.equals("register"))
                    {
                        // get client name (second word of the message, immediately after REGISTER keyword)
                        this.name = separateWords[1];

                        // add the client name to the connection list and remove a record with client id
                        clientList.put(this.name, this);
                        clientList.remove(this.clientID);
                        clientMessages.put(this.name, writer);
                        clientMessages.remove(this.clientID);
                        // print in the server
                        if (this.name == null)
                        {
                            System.out.println("[ Client " + this.clientID + " ] says: " + message);
                        }
                        else
                        {
                            System.out.println("[ " + this.name + " ] says: " + message);
                        }
                    }
                    // unregister existing client
                    else if (keyWord.equals("unregister"))
                    {
                        // remove the client from the connection list
                        clientList.remove(this.name);
                        this.name = null;
                        // print in the server
                        if (this.name == null)
                        {
                            System.out.println("[ Client " + this.clientID + " ] says: " + message);
                        }
                        else
                        {
                            System.out.println("[ " + this.name + " ] says: " + message);
                        }
                    }
                    else if (keyWord.equals("create"))
                    {
                        // check if the message is long enough to contain all needed data
                        if (separateWords.length < 2)
                        {
                            System.out.println("Name of the group not specified!");
                        }
                        // chceck if client has a name and add it to the usernames
                        if (this.name == null)
                        {
                            usernames.add(this.clientID);
                        }
                        else
                        {
                            usernames.add(this.name);
                        }
                        // add client to the group with a specified name
                        groupMessages.put(separateWords[1], usernames);
                        // print in the server
                        if (this.name == null)
                        {
                            System.out.println("[ Client " + this.clientID + " ] says: " + message);
                        }
                        else
                        {
                            System.out.println("[ " + this.name + " ] says: " + message);
                        }
                        writer.println("Group " + separateWords[1] + " has been successfully created!");
                    }
                    else if (keyWord.equals("join"))
                    {
                        // check if the message is long enough to contain all needed data
                        System.out.println("join");
                        if (separateWords.length < 2)
                        {
                            System.out.println("Name of the group not specified!");
                        }
                        
                        // check if requested group exists
                        if (groupMessages.containsKey(separateWords[1]))
                        {
                            if (this.name != null)
                            {
                                groupMessages.get(separateWords[1]).add(this.name);
                            }
                            else
                            {
                                groupMessages.get(separateWords[1]).add(this.clientID);
                            }
                            // print in the server
                            if (this.name == null)
                            {
                                System.out.println("[ Client " + this.clientID + " ] says: " + message);
                            }
                            else
                            {
                                System.out.println("[ " + this.name + " ] says: " + message);
                            }
                            writer.println("Group " + separateWords[1] + " joined successfully!");
                        }
                        // if requested group does not exist create it and join
                        else
                        {
                            if (this.name == null)
                            {
                                usernames.add(this.clientID);
                            }
                            else
                            {
                                usernames.add(this.name);
                            }
                            // add client to the group with a specified name
                            groupMessages.put(separateWords[1], usernames);
                            // print in the server
                            if (this.name == null)
                            {
                                System.out.println("[ Client " + this.clientID + " ] says: " + message);
                            }
                            else
                            {
                                System.out.println("[ " + this.name + " ] says: " + message);
                            }
                            writer.println("Group " + separateWords[1] + " has been successfully created and joined!");
                        }
                    }
                    // remove a group
                    else if (keyWord.equals("leave"))
                    {
                        // check if the message is long enough to contain all needed data
                        if (separateWords.length < 2)
                        {
                            System.out.println("Name of the group not specified!");
                        }

                        // check if requested group exists
                        if (groupMessages.containsKey(separateWords[1]))
                        {
                            if (this.name != null)
                            {
                                groupMessages.get(separateWords[1]).remove(this.name);
                            }
                            else
                            {
                                groupMessages.get(separateWords[1]).remove(this.clientID);
                            }
                            //print in the server
                            if (this.name == null)
                            {
                                System.out.println("[ Client " + this.clientID + " ] says: " + message);
                            }
                            else
                            {
                                System.out.println("[ " + this.name + " ] says: " + message);
                            }
                            writer.println("Group " + separateWords[1] + " left successfully!");
                        }
                        // if the requested group does not exist display an error message
                        else
                        {
                            writer.println("The group you are trying to leave does not exist!");
                        }
                    }
                    // user wants to send a message to an existing group
                    else if (keyWord.equals("send") && groupMessages.containsKey(separateWords[1]))
                    {
                        ArrayList < String > helperArray = groupMessages.get(separateWords[1]);
                        String nameOfGroup = separateWords[1];

                        for ( String user: helperArray )
                        {
                            PrintWriter localWriter = clientMessages.get(user);
                            localWriter.println("[ " + this.name + " ] says: " + message.substring(keyWord.length() + nameOfGroup.length() + 2));
                            localWriter.flush();
                        }
                        //print in the server
                        if (this.name == null)
                        {
                            System.out.println("[ Client " + this.clientID + " ] says: " + message);
                        }
                        else
                        {
                            System.out.println("[ " + this.name + " ] says: " + message);
                        }
                        
                    }
                    else if (keyWord.equals("send") && (!groupMessages.containsKey(separateWords[1])))
                    {
                        System.out.println("send to a specific client");
                        if (clientMessages.containsKey(separateWords[1]))
                        {
                            String clientName = (separateWords[1]);
                            System.out.println(separateWords[1] + separateWords[0]);
                            PrintWriter localWriter;
                            localWriter = clientMessages.get(clientName);
                            localWriter.println("[ "+ name + " ] sent: " + message.substring(keyWord.length() + clientName.length() + 2));
                            localWriter.flush();
                            //print in the server
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
                    else if (keyWord.equals("quit"))
                    {
                        // remove the client from the connection list
                        clientList.remove(this.name);
                        this.name = null;
                        this.clientSocket.close();
                    }
                    // if a group or a client name are specified incorrectly, the message is sent to all clients
                    else
                    {
                    // iterate over clients and send that message
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
