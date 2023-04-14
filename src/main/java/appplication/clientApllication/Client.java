package appplication.clientApllication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    private Socket socket;
    private BufferedReader bufferedReader;
    public PrintWriter printWriter;
    public boolean done;

    @Override
    public void run() {
        try{
            socket = new Socket("localhost", 9000);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String inMessage;
            while ((inMessage = bufferedReader.readLine()) != null){
                System.out.println(inMessage);
            }

        } catch (IOException e){
            shutdown();
        }
    }
    public void shutdown(){
        done = true;
        try {
            bufferedReader.close();
            printWriter.close();
            if(!socket.isClosed()){
                socket.close();
            }
        } catch (IOException e){
            //TODO:
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader inRead = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = inRead.readLine();
                    if(message.equals("/quit")){
                        printWriter.println(message);
                        inRead.close();
                        shutdown();
                    } else {
                        printWriter.println(message);
                    }
                }
            } catch (IOException e){
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
