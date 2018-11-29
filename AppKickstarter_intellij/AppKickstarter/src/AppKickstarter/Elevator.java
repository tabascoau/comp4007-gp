package AppKickstarter;

import AppKickstarter.misc.AppThread;
import AppKickstarter.timer.Timer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

class Elevator extends AppThread {

    private final int sleepTime = 5;
    private final String id;
    private volatile int currentFloor;
    private final int maxFloor;
    private char state, direction;
    private boolean quit = false;
    private boolean addingRequest = false;

    private String cfgFName;
    private Properties cfgConfig;

    private ArrayList<ElevatorRequest> requestList = new ArrayList<ElevatorRequest>();
    private volatile List<Integer> floorQueue = Collections.synchronizedList(new ArrayList<Integer>());

    public Elevator(String id, AppKickstarter appKickstarter) throws IOException {
        super(id, appKickstarter);
        this.currentFloor = 0;
        this.maxFloor = 50;
        this.state = 'S';
        this.direction = 'U';
        this.id = id;
        this.cfgFName = "etc/SmartElevator.cfg";

        //Read Config File
        cfgConfig = new Properties();
        FileInputStream in = new FileInputStream(cfgFName);
        cfgConfig.load(in);
        in.close();
    }

    @Override
    public void run() {
        log.info("Elevator " + id + ": starting...");
        Timer.setSimulationTimer(id, mbox, sleepTime);

        while (quit == false) {
            if (floorQueue.size() > 0 && state == 'S' && !addingRequest) {
//                System.out.println(floorQueue);
                int destFloor = floorQueue.get(0);
                log.info("Elevator " + id + ": is going to " + (destFloor) + "/F");
                if (destFloor > currentFloor) {
                    state = 'M';
                    direction = 'U';
                } else if (destFloor < currentFloor) {
                    state = 'M';
                    direction = 'D';
                }

                floorQueue.remove(0);

                try {
                    move(destFloor);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void move(int destFloor) throws InterruptedException {
        int floorDifference = Math.abs(currentFloor - destFloor);

        for (int floor = 1; floor <= floorDifference; floor++) {
            if (direction == 'U') {
                currentFloor++;
                log.info("Elevator " + id + ": is moving up to " + (currentFloor) + "/F");
                if (floor == 1) {
                    sleep(getProperty("Elev.Time.AccUp"));
                }
                if (floor == floorDifference) {
                    sleep(getProperty("Elev.Time.DecUp"));
                    arrive();
                } else {
                    sleep(getProperty("Elev.Time.UpOneFloor"));
                }

            } else if (direction == 'D') {
                currentFloor--;
                log.info("Elevator " + id + ": is moving down to " + (currentFloor - 1) + "/F");
                if (floor == 1) {
                    sleep(getProperty("Elev.Time.AccDown"));
                }
                if (floor == floorDifference) {
                    sleep(getProperty("Elev.Time.DecDown"));
                    arrive();
                } else {
                    sleep(getProperty("Elev.Time.DownOneFloor"));
                }
            }
        }
    }

    private void arrive() throws InterruptedException {
        log.info("Elevator " + id + ": has arrived to " + currentFloor + "/F");
        log.info("Elevator " + id + ": opening the door ");
        sleep(getProperty("Elev.Time.DoorOpen"));
        log.info("Elevator " + id + ": door opened ");
        sleep(getProperty("Elev.Time.DoorWait"));
        log.info("Elevator " + id + ": closing the door ");
        sleep(getProperty("Elev.Time.DoorClose"));
        log.info("Elevator " + id + ": door closed ");

        state = 'S';
    }

    public void sleep(double sleepTime) throws InterruptedException {
        Thread.sleep((long) (sleepTime * 1000));
    }

    private void sortFloorQueue() {
        if (direction == 'D') {
            Collections.sort(floorQueue, Collections.reverseOrder());
        } else {
            Collections.sort(floorQueue);
        }
        addingRequest = false;
    }

    public void addRequest(ElevatorRequest erh) {
        addingRequest = true;
        requestList.add(erh);

        if (!floorQueue.contains(erh.getDestFloor())) {
            floorQueue.add(erh.getDestFloor());
            log.info("Elevator " + id + ": Assigned " + erh.getDestFloor() + "/F");
        } else {
            log.info("Elevator " + id + ": Dest Floor " + erh.getDestFloor() + "/F assign failed (REASON: IS EXIST)");
        }

        if (!floorQueue.contains(erh.getSrcFloor())) {

            floorQueue.add(erh.getSrcFloor());
            log.info("Elevator " + id + ": Assigned " + erh.getSrcFloor() + "/F");
        } else {
            log.info("Elevator " + id + ": Src Floor " + erh.getSrcFloor() + "/F assign failed - (REASON: IS EXIST)");
        }
        sortFloorQueue();
    }

    public Double getProperty(String property) {
        String s = cfgConfig.getProperty(property);

        if (s == null) {
            log.severe(id + ": getProperty(" + property + ") failed.  Check the config file (" + cfgFName + ")!");
        }
        return Double.parseDouble(s);
    } // getProperty

    public char getDirection() {
        return direction;
    }

    public char getState() {
        return state;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }
}