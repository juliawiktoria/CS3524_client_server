package cs3524_assignemnt_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;

public class MessageClient
{
    public MessageClient ( String hostname, int port) throws UnknownHostException, IOException, ClassNotFoundException
    {
        // Connect to and set up input/output streams with the server
        Socket clientSocket = new Socket( hostname, port );

        BufferedReader in  = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
        PrintWriter    out = new PrintWriter( new OutputStreamWriter( clientSocket.getOutputStream() ), true );

        BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ));

        String message;

        // loop so the client can send multiple messages
        while(true)
        {
            while (in.ready())
            {
                System.out.print ( "ShoutClientSocket> " ) ;
                message = stdin.readLine();
                out.println( message );
            }

            while (stdin.ready())
            {
                message = stdin.readLine();
                out.println(message);
            } 
        }
    }
    
    static public void main ( String args[] ) throws UnknownHostException, ClassNotFoundException, IOException
    {
        // displays a message if the client was called incorrectly
        if (args.length < 2) {
            System.err.println( "Usage: java MessageClient <host> <port>" ); 
            return;
        } 
        // parse the hostname and the port number from the passed parameters
        String hostname = args[0]; 
        int port = Integer.parseInt( args[1] ); 

        // create a new MessageClient and pass parsed hostname and port as parameters
        new MessageClient ( hostname, port ) ; 
    } 
}