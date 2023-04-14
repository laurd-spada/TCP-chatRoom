package appplication.serverApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private ArrayList<ConnectionHandler> connectionsList;
    private ServerSocket serverSocket;
    private boolean done;
    private ExecutorService executorService;
    public Server(){
        connectionsList = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(9000);
            executorService = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = serverSocket.accept();

                ConnectionHandler handler = new ConnectionHandler(client);
                connectionsList.add(handler);
                executorService.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }
    public void broadCast(String message){
        for(ConnectionHandler ch : connectionsList){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }
    public void shutdown(){
        try {
            done = true;
            executorService.shutdown();
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            for(ConnectionHandler  ch : connectionsList){
                ch.shutdown();
            }
        } catch (IOException e){
            //TODO:
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader bufferedReader;
        private PrintWriter printWriter;
        private String nickname;
        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try{
                printWriter = new PrintWriter(client.getOutputStream(),true);
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                printWriter.println("Please enter a nick name: ");
                nickname = bufferedReader.readLine();

                System.out.println(nickname + " Connected!");
                broadCast(nickname + " joined the chat");

                String message;
                while ((message = bufferedReader.readLine()) != null){
                    if(message.startsWith("/nick ")){
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2){
                            broadCast(nickname + "renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + "renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            printWriter.println("Successfully changed nickName to " + nickname);
                        } else {
                            printWriter.println("No nickname provided");
                        }
                    } else if (message.startsWith("/quit")){
                        broadCast(nickname + " left");
                        shutdown();
                    } else{
                        broadCast(nickname + " : " + message);
                    }
                }

            }catch (IOException e){
                shutdown();
            }
        }
        public void sendMessage(String message){
            printWriter.println(message);
        }
        public void shutdown(){
            try {
                bufferedReader.close();
                printWriter.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e){
                //iignore first
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
