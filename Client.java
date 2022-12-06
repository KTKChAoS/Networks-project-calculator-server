import java.io.*;
import java.net.*;

public class Client {

    String userName;
    String serverAddress;
    int portNumber;
    Socket socket;

    BufferedReader FromUser;
    DataOutputStream ToServer;
    BufferedReader FromServer;

    Client(String[] args) {
        if (args.length < 3) {
            // if the arguments are not as expected, defaults to specified values.
            System.out.println(
                    "Three arguments required (username, server address, and port number). Refer to documentation. Defaulting to 'anonymous', 'localhost', '5000'");
            userName = "anonymous";
            serverAddress = "localhost";
            portNumber = 5000;
        } else {
            userName = args[0];
            serverAddress = args[1];
            portNumber = Integer.parseInt(args[2]);
        }
        startClient();
    }

    private void startClient() {
        try {
            // create new socket, and new I/O streams for connecting to the server and communicating
            socket = new Socket(serverAddress, portNumber);
            FromUser = new BufferedReader(new InputStreamReader(System.in));
            ToServer = new DataOutputStream(socket.getOutputStream());
            FromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // done to pass the username to the server
            ToServer.writeBytes(userName + " starting connection\n");
            ToServer.flush();

            System.out.println(FromServer.readLine());

            // wait for user to type an expression and then send it to the server
            while (true) {
                System.out.print("Enter expression (type 'quit' to close connection): ");
                String input = FromUser.readLine();
                ToServer.writeBytes(input + "\n");

                // read the output from the server and display it
                String fromServer = FromServer.readLine();
                System.out.println("Result from server: " + fromServer);

                // if the server is ending connection, break out of the while loop
                if (fromServer.equals("Ending connection...")) {
                    break;
                }
            }
            // close the socket at the end
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client(args);
    }
}