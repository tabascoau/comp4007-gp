package AppKickstarter.misc;

import AppKickstarter.AppKickstarter;
import AppKickstarter.gui.CentralControlPanel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class GreetingServer extends Thread {
    private ServerSocket serverSocket;
    private AppKickstarter appKickstarter;
    CentralControlPanel centralControlPanel;

    public GreetingServer(int port, AppKickstarter appKickstarter) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
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
                appKickstarter.ActivateElevator(str);

                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
                        + "\nGoodbye!");

                in.close();
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