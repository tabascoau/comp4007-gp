package AppKickstarter.misc;

import AppKickstarter.AppKickstarter;
import AppKickstarter.gui.CentralControlPanel;
import AppKickstarter.myThreads.Elevator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class GreetingServer extends Thread {
    public static ServerSocket serverSocket;
    private AppKickstarter appKickstarter;
    public static Socket clientSocket;
    CentralControlPanel c;

//    private Queue<String> requestQueue = new LinkedList<String>();


    public GreetingServer(int port, AppKickstarter appKickstarter) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
        this.appKickstarter = appKickstarter;
    }

    @Override
    public void run() {
        try {
            System.out.println("Waiting for client on port " +
                    serverSocket.getLocalPort() + "...");
            clientSocket = serverSocket.accept();
            System.out.println("Just connected to " + clientSocket.getLocalSocketAddress());

            while (true) {
                BufferedReader in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String str=in.readLine();

                synchronized (CentralControlPanel.requestQueue) {
                    CentralControlPanel.requestQueue.add(str);
                }
            }


//                DataOutputStream out = new DataOutputStream(server.getOutputStream());
//                out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
//                        + "\nGoodbye!");
//                out.flush();
//                in.close();
//                out.close();
//                server.close();
//            }

        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMsgToClient(String str) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
            out.println(str);


        }catch (IOException e){
            e.printStackTrace();
            System.exit(444444);
        }



    }


}

