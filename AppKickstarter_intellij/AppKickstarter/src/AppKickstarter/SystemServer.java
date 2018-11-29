package AppKickstarter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SystemServer {
    private AppKickstarter appKickstarter;

    public SystemServer(AppKickstarter appKickstarter) throws IOException {
        this.appKickstarter = appKickstarter;
        ServerSocket sSocket = new ServerSocket(Integer.parseInt(appKickstarter.getProperty("Server.Port")));
        Socket cSocket = sSocket.accept();
        appKickstarter.getLog().info("Client Connected :)");
        serve(cSocket);
    }

    void serve(Socket cSocket) throws IOException {
        new ElevatorRequestHandler(cSocket, appKickstarter);
    }
}
