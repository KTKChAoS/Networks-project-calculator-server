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
            // return;
        } else {
            userName = args[0];
            serverAddress = args[1];
            portNumber = Integer.parseInt(args[2]);
        }
        startClient();
    }

    private void startClient() {
        try {
            socket = new Socket(serverAddress, portNumber);
            FromUser = new BufferedReader(new InputStreamReader(System.in));
            ToServer = new DataOutputStream(socket.getOutputStream());
            FromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ToServer.writeBytes(userName + " starting connection\n");
            ToServer.flush();

            System.out.println(FromServer.readLine());

            while (true) {
                System.out.print("Enter equation(type 'quit' to close connection): ");
                String input = FromUser.readLine();
                ToServer.writeBytes(input + "\n");

                String fromServer = FromServer.readLine();
                System.out.println("Result from server: " + fromServer);

                if (fromServer.equals("Ending connection...")) {
                    break;
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client(args);
    }
}