package AppKickstarter;

import AppKickstarter.misc.AppThread;
import AppKickstarter.timer.Timer;

import java.io.FileInputStream;
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

    private ArrayList<ElevatorRequest> requestList = new ArrayList<ElevatorRequest>();
    private ArrayList<Integer> floorQueue = new ArrayList<Integer>();

    public Elevator(String id, AppKickstarter appKickstarter) throws IOException {
        super(id, appKickstarter);
        this.currentFloor = 0;
        this.maxFloor = 50;
        this.state = 'S';
        this.id = id;

//        Read Config File
        configFile = new Properties();
        FileInputStream in = new FileInputStream("etc/SmartElevator.cfg");
        configFile.load(in);
        in.close();

    }

    @Override
    public void run() {
        log.info("Elevator " + id + ": starting...");
        Timer.setSimulationTimer(id, mbox,sleepTime);

        while (quit == false) {
            if (!requestList.isEmpty()) {
                int destFloor = floorQueue.get(0);
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
            if (state == 'U') {
                log.info("Elevator " + id + ": is moving up to " + (currentFloor + 1) + "/F");
                if (floor == 1) {
                    sleep(Long.parseLong(configFile.getProperty("Elev.Time.AccUp")));
                } else if (floor == floorDifference){
                    sleep(Long.parseLong(configFile.getProperty("Elev.Time.DecUp")));
                } else {
                    sleep(Long.parseLong(configFile.getProperty("Elev.Time.UpOneFloor")));
                }
                currentFloor ++;
            } else if (state == 'D') {
                log.info("Elevator " + id + ": is moving down to " + (currentFloor - 1) + "/F");
                if (floor == 1) {
                    sleep(Long.parseLong(configFile.getProperty("Elev.Time.AccDown")));
                } else if (floor == floorDifference){
                    sleep(Long.parseLong(configFile.getProperty("Elev.Time.DecDown")));
                } else {
                    sleep(Long.parseLong(configFile.getProperty("Elev.Time.DownOneFloor")));
                }
                currentFloor --;
            }
            log.info("Elevator " + id + ": Opening the door ");
            sleep(Long.parseLong("Elev.Time.DoorOpen"));
            log.info("Elevator " + id + ": Door opened ");
            sleep(Long.parseLong("Elev.Time.DoorWait"));
            log.info("Elevator " + id + ": Closing the door ");
            sleep(Long.parseLong("Elev.Time.DoorClose"));
            log.info("Elevator " + id + ": has arrived to " + currentFloor + "/F");
        }

    }

    public void sleep(float sleepTime) throws InterruptedException {
        Thread.sleep((long)sleepTime);
    }

    private void sortFloorQueue() {
        if (state == 'U') {
            Collections.sort(floorQueue, Collections.reverseOrder());
        } else if (state == 'D') {
            Collections.sort(floorQueue);
        }
    }

    public void addRequest(ElevatorRequest erh) {
        requestList.add(erh);

        if (!floorQueue.contains(erh.getDestFloor()))
            floorQueue.add(erh.getDestFloor());
        if (!floorQueue.contains(erh.getSrcFloor()))
            floorQueue.add(erh.getSrcFloor());

        sortFloorQueue();

        log.info("Elevator " + id + ": Assigned " + id + "/F");
    }

    public int getCurrentFloor() {
        return currentFloor;
    }
}