package AppKickstarter;

import AppKickstarter.misc.AppThread;
import AppKickstarter.myThreads.ThreadA;

import java.io.IOException;
import java.util.ArrayList;

public class ElevatorController {

    private ArrayList<Elevator> elevators = new ArrayList<Elevator>();
    private AppKickstarter appKickstarter;
    private char [] elevatorIDs = new char[]{'A', 'B', 'C', 'D', 'E', 'F'};

    public ElevatorController(AppKickstarter appKickstarter) throws IOException {
        this.appKickstarter = appKickstarter;
    }

    public void createElevator() throws IOException {
        for (int i = 0; i < elevatorIDs.length; i++) {
            elevators.add(new Elevator("" + elevatorIDs[i], appKickstarter));
        }
    }

    public void startElevator() throws IOException {
        for (int i = 0; i < elevatorIDs.length; i++) {
            new Thread(elevators.get(i)).start();
        }
        new SystemServer(appKickstarter);
    }

    public ArrayList<Elevator> getElevators() {
        return elevators;
    }


}
