package AppKickstarter.misc;

import AppKickstarter.AppKickstarter;
import AppKickstarter.gui.CentralControlPanel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

//public ClientHandler{
//    Socket s;
//    public ClientHandler(Socket s) {
//        this.s = s;
//    }
//}

public class GreetingServer extends Thread {
    private ServerSocket serverSocket;
    private AppKickstarter appKickstarter;
    private ArrayList<String> queue=new ArrayList<>();
    CentralControlPanel c;

//    private Queue<String> requestQueue = new LinkedList<String>();


    public GreetingServer(int port, AppKickstarter appKickstarter) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
        c=CentralControlPanel.getInstance();
        this.appKickstarter = appKickstarter;



    }

    public void run() {
        while(true) {
            try {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();

                System.out.println("Just connected to " + server.getLocalSocketAddress());


                DataInputStream in = new DataInputStream(server.getInputStream());
                byte[] bs = new byte[1024];

                in.read(bs);
                String str = new String(bs);
                str = str.trim();

                // Send to appkickstarter
//                requestQueue.add(str);
//                System.out.println("=THE QUEUE"+requestQueue);
//                while (requestQueue.size() > 0) {
//                    if (c.getAFreeElevator()||c.getBFreeElevator()||c.getCFreeElevator()||c.getDFreeElevator()||c.getEFreeElevator())
//                    {
//                        appKickstarter.ActivateElevator(requestQueue.peek());
//                        requestQueue.poll();
//                    }
//                    else {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (Exception e) {
//                            e.getStackTrace();
//                        }
//                    }
//                }
//                appKickstarter.ActivateElevator(str);
                appKickstarter.AddRequestQueue(str);


                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
                        + "\nGoodbye!");
                out.flush();

                in.close();
                out.close();
                server.close();

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

    }
}