package AppKickstarter;

import AppKickstarter.misc.AppThread;
import AppKickstarter.timer.Timer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

class Elevator extends AppThread{

    private final int sleepTime = 5;
    private final String id;
    private int currentFloor;
    private final int maxFloor;
    private char state;
    private boolean quit = false;

    private Properties configFile;

    private ArrayList<ElevatorRequestHandler> requestList = new ArrayList<ElevatorRequestHandler>();
    private ArrayList<Integer> floorQueue = new ArrayList<Integer>();

    public Elevator(String id, AppKickstarter appKickstarter) throws IOException {
        super(id, appKickstarter);
        this.currentFloor = 0;
        this.maxFloor = 50;
        this.state = 'S';
        this.id = id;

//        Read Config File
        configFile = new Properties();
        configFile.load(this.getClass().getClassLoader().getResourceAsStream("etc/SmartElevator.cfg"));
    }

    @Override
    public void run() {
        log.info("Elevator " + id + ": starting...");
        Timer.setSimulationTimer(id, mbox,sleepTime);

        while (quit == false) {
            if (!requestList.isEmpty()) {
                int destFloor = floorQueue.get(0);

            }
        }
    }

    private void move(int destFloor) {
        int floorDifference = Math.abs(currentFloor - destFloor);

        for (int floor = 1; floor <= floorDifference; floor++) {

        }
    }

    private void sortFloorQueue() {
        if (state == 'U') {
            Collections.sort(floorQueue, Collections.reverseOrder());
        } else if (state == 'D') {
            Collections.sort(floorQueue);
        }
    }

    private void addRequest(ElevatorRequestHandler erh) {
        requestList.add(erh);

        if (!floorQueue.contains(erh.getDestFloor()))
            floorQueue.add(erh.getDestFloor());
        if (!floorQueue.contains(erh.getSrcFloor()))
            floorQueue.add(erh.getSrcFloor());

        sortFloorQueue();

        log.info("Elevator " + id + ": Assigned " + id + "/F");
    }
}