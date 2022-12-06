import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
public class Server {
    int portNumber; 
    ServerSocket serverSocket;
    List<ConnectionInfo>connections = new ArrayList<ConnectionInfo>();
    int aliveConnections = 0;

    Server(String portnumber){
        portNumber = Integer.parseInt(portnumber);
        startServer();    
    }

    // creates log file and starts the server on the specified port number
    private void startServer(){
        try {
            File file = new File("logfile.txt");
            //Instantiating the PrintStream class
            PrintStream stream = new PrintStream(file);
            System.setOut(stream);
        } catch (Exception e) {            
            e.printStackTrace();
        }
        try{
            serverSocket = new ServerSocket(portNumber);
            // listens on the port and creates a new connection every time a new client requests for a connection
            while(true){
                connections.add(new ConnectionInfo(serverSocket.accept()));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length == 0){
            // if the use doesn not specify a port number, it is defaulted to 5000
            new Server("5000");
        }
        new Server(args[0]);
    }

    private class ConnectionInfo extends Thread{
        Socket socket;
        BufferedReader FromClient;
        DataOutputStream ToClient;
        String clientUserName; 
        LocalDateTime startTime;
        LocalDateTime endTime;
        List<String>equationList = new ArrayList<String>();
        String hostName;

        ConnectionInfo(Socket socket){
            this.socket = socket;
            hostName = socket.getInetAddress().getHostName();
            System.out.println("New connection started with " + hostName);
            this.start();
        }

        @Override
        public void run() {
            try{
                FromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ToClient = new DataOutputStream(socket.getOutputStream());

                while(true){
                    String inputFromClient = FromClient.readLine();
                    if(inputFromClient == null) continue;
                    if(clientUserName == null){
                        System.out.println("Message from client " + hostName + ": " + inputFromClient);
                    }else{
                        System.out.println("Message from client " + clientUserName + " (" + hostName + "): " + inputFromClient);
                    }

                    if(inputFromClient.contains("starting connection")){
                        clientUserName = inputFromClient.split(" ")[0];
                        startTime = LocalDateTime.now();
                        ToClient.writeBytes("Connection established\n");
                        aliveConnections++;
                    }else if(inputFromClient.equals("quit")){
                        endTime = LocalDateTime.now();
                        ToClient.writeBytes("Ending connection...\n");
                        printInfo();
                        aliveConnections--;
                    }else{
                        Double result = Double.parseDouble(calculator(inputFromClient));
                        System.out.println("Sending " + clientUserName + " answer: " + result);
                        if(result.equals(Double.NaN)){
                            ToClient.writeBytes("Expression format could not be resolved. Refer to documentation\n");
                        }else{
                            ToClient.writeBytes(result +"\n");
                        }     
                        equationList.add(inputFromClient);
                    }
                    ToClient.flush();
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }

        private String calculator(String expression){
            Object expResult1 = Double.NaN;
            try {
                ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("Nashorn");
                expResult1 = scriptEngine.eval(expression);
                System.out.println(expResult1.toString());
            } catch (Exception e) {
                System.out.println("c" + expResult1.toString());

            }
            return expResult1.toString();
        }

        private void printInfo(){
            System.out.println("\n\tClient disconnected. Information:\n");
            System.out.println("Client: " + hostName);
            System.out.println("User name: " + clientUserName);
            System.out.println("Connection started: " + startTime);
            System.out.println("Connection ended: " + endTime);
            System.out.println("Connection duration (seconds): " + Duration.between(startTime, endTime).toMillis() / 1000);
            System.out.println("Expressions evaluated:");
            for(String eq : equationList){
                System.out.println(eq);
            }
            System.out.println();
        }
    }
}

