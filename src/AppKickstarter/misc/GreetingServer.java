package AppKickstarter.misc;

import AppKickstarter.AppKickstarter;
import AppKickstarter.gui.CentralControlPanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class GreetingServer extends Thread {
    public static ServerSocket serverSocket;
    private AppKickstarter appKickstarter;
    public static Socket clientSocket;

    public GreetingServer(int port, AppKickstarter appKickstarter) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
        this.appKickstarter = appKickstarter;
    }

//    @Override
    public void run() {
        try {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            clientSocket = serverSocket.accept();
            System.out.println("Just connected to " + clientSocket.getLocalSocketAddress());

            while (true) {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String str = in.readLine();

                // Send data
                str.trim();
                appKickstarter.ReceiveRequest(str);
            }
        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

