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

    private AppKickstarter appKickstarter;
    private DataInputStream in;
    private DataOutputStream out;

    public ElevatorRequestHandler(Socket cSocket, AppKickstarter appKickstarter) throws IOException {
        out = new DataOutputStream(cSocket.getOutputStream());
        in = new DataInputStream(cSocket.getInputStream());
        this.appKickstarter = appKickstarter;

        while (cSocket.isConnected()) {
            try {
                byte[] buffer = new byte[1024];
                in.read(buffer);
                String reqMsg = new String(buffer).trim();
                String[] splitedReqMsg = reqMsg.split(" ");

                switch (splitedReqMsg[0]) {
                    case "Svc_Req":
                        String passenger = splitedReqMsg[1];
                        ElevatorRequest elevatorRequest = new ElevatorRequest(passenger.substring(passenger.indexOf("-") + 1, passenger.length()), Integer.parseInt(splitedReqMsg[2]), Integer.parseInt(splitedReqMsg[3]));
                        getClosestElevator(elevatorRequest).addRequest(elevatorRequest);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Elevator getClosestElevator(ElevatorRequest elevatorRequest) {
        ArrayList<Elevator> elevators = appKickstarter.getElevatorController().getElevators();

        Elevator closestElevator = null;

        for (Elevator elevator : elevators) {
            boolean isExceed = false;
            boolean isSameDirection = elevator.getDirection() == elevatorRequest.getDirection();

            if (isSameDirection) {
                if ((elevator.getDirection() == 'U' && elevator.getCurrentFloor() > elevatorRequest.getSrcFloor())
                        || (elevator.getDirection() == 'D' && elevator.getCurrentFloor() < elevatorRequest.getSrcFloor()) ) {
                    isExceed = true;
                }
            }
//            System.out.println( elevator.getCurrentFloor() + " " + elevatorRequest.getSrcFloor());
            if (!isExceed) {
                if (closestElevator == null) {
                    closestElevator = elevator;
                } else {
                    int oldElevatorFloorDifference = Math.abs(elevatorRequest.getSrcFloor() - closestElevator.getCurrentFloor());
                    int newElevatorFloorDifference = Math.abs(elevatorRequest.getSrcFloor() - elevator.getCurrentFloor());

                    boolean isCloser = newElevatorFloorDifference < oldElevatorFloorDifference;

                    if (isCloser) {
                        closestElevator = elevator;
                    }

//                if ((elevator.getDirection() == elevatorRequest.getDirection())&& elevator.getState() == 'S') {
//                    if (newElevatorFloorDifference < oldElevatorFloorDifference) {
//                        closestElevator = elevator;
//                    }
//                }

                }
            }
        }

        return closestElevator;
    }
}


