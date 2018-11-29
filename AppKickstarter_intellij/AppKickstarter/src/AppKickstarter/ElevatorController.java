package AppKickstarter;

import AppKickstarter.misc.AppThread;
import AppKickstarter.myThreads.ThreadA;

import java.io.IOException;
import java.util.ArrayList;

public class ElevatorController {

    private ArrayList<Elevator> elevators = new ArrayList<Elevator>();
    private AppKickstarter appKickstarter;

    public ElevatorController(AppKickstarter appKickstarter) throws IOException {
        this.appKickstarter = appKickstarter;
        char [] elevatorIDs = new char[]{'A', 'B', 'C', 'D', 'E', 'F'};

        for (int i = 0; i < elevatorIDs.length; i++) {
            createElevator(elevatorIDs[i], appKickstarter);
            startElevator(elevators.get(i));
        }

        new SystemServer(appKickstarter);
    }

    private void createElevator(char elevatorID, AppKickstarter appKickstarter) throws IOException {
        elevators.add(new Elevator("" + elevatorID, appKickstarter));
    }

    private void startElevator(Elevator elevator) {
        new Thread(elevator).start();
        appKickstarter.regThread(elevator);
    }

    public ArrayList<Elevator> getElevators() {
        return elevators;
    }


}
