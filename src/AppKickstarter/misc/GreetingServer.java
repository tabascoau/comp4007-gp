package AppKickstarter.misc;

import AppKickstarter.AppKickstarter;
import AppKickstarter.gui.CentralControlPanel;
import AppKickstarter.myThreads.Elevator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class GreetingServer extends Thread {
    private ServerSocket serverSocket;
    private AppKickstarter appKickstarter;

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
            while (true) {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getLocalSocketAddress());

                DataInputStream in = new DataInputStream(server.getInputStream());
                byte[] bs = new byte[1024];

                //str is the request from client side
                in.read(bs);
                String str = new String(bs);
                str = str.trim();

                synchronized (CentralControlPanel.requestQueue) {
                    CentralControlPanel.requestQueue.add(str);
                }

                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
                        + "\nGoodbye!");
                out.flush();
                in.close();
                out.close();
                server.close();
            }

        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

