package AppKickstarter;

import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.Msg;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ElevatorRequestHandler {


    public ElevatorRequestHandler(Socket cSocket) throws IOException {
        DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
        DataInputStream in = new DataInputStream(cSocket.getInputStream());
        while(cSocket.isConnected()){
            try {
                byte [] buffer = new byte[1024];
                in.read(buffer);
                String reqMsg = new String(buffer);

                String [] splitedReqMsg = reqMsg.split(" ");
for(String m: splitedReqMsg) {
    System.out.println(m);
}


                switch (splitedReqMsg[0]) {
                    case "Svc_Req":
                        String passenger = splitedReqMsg[1];
                        ElevatorRequest elevatorRequest = new ElevatorRequest(passenger.substring(passenger.indexOf("-") + 1, passenger.length()), Integer.parseInt(splitedReqMsg[2]),  Integer.parseInt(splitedReqMsg[3]));
                        getClosestElevator(elevatorRequest).addRequest(elevatorRequest);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Elevator getClosestElevator(ElevatorRequest elevatorRequest) {
        ArrayList<Elevator> elevators = AppKickstarter.getElevatorController().getElevators();

        Elevator closestElevator = null;

        for(Elevator elevator: elevators) {
            if (closestElevator == null) {
                closestElevator = elevator;
            } else {
                int oldElevatorFloorDifference = Math.abs(elevatorRequest.getSrcFloor() - closestElevator.getCurrentFloor());
                int newElevatorFloorDifference = Math.abs(elevatorRequest.getSrcFloor() - elevator.getCurrentFloor());

                if (newElevatorFloorDifference < oldElevatorFloorDifference) {
                    closestElevator = elevator;
                }
            }
        }

        return closestElevator;
    }
}


