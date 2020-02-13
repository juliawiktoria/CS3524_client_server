package cs3524_assignemnt_1;

import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.HashMap;


// handler class for multiple client connections
public class ClientConnection extends Thread
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