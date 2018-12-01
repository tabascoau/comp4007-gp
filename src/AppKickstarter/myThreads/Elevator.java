package AppKickstarter.myThreads;

import AppKickstarter.gui.CentralControlPanel;
import AppKickstarter.misc.*;
import AppKickstarter.AppKickstarter;
import AppKickstarter.timer.Timer;
import sun.rmi.runtime.Log;

import javax.swing.*;
import java.util.logging.Logger;


//======================================================================
// ThreadA
public class Elevator extends AppThread {
    private static int totalElevatorCount = 0;
    private int _id;

    private final int sleepTime = 5;
    private final int maxPassenger = 10;
    private int passengers = 0;

    public boolean IsEmpty() {
        return passengers == 0;
    }

    private int idleFloor = 0;
    private double upOneFloor = 0.6f;
    private double downOneFloor = 0.5f;
    private double accUp = 1.2f;
    private double accDown = 1f;
    private double decUp = 1.2f;
    private double decDown = 1f;
    private double doorOpen = 1f;
    private double doorClose = 1.5f;
    private double doorWait = 5f;
    private long doorOpenToClose = (long) ((doorOpen + doorClose + doorWait) * 1000);
    CentralControlPanel centralControlPanel = CentralControlPanel.getInstance();
    private String[] Elevator = {"A", "B", "C", "D", "E", "F"};
    private String passengerId;
    private int totalNumberOfElevator = centralControlPanel.totalNumberOfElevator;
    private int src, dest;

    //new added code
    String elevArrmsg = "";
    String elevDepmsg = "";
    //  / ___| | | |_ _| |  _ \ / \  |  _ \_   _|
    // | |  _| | | || |  | |_) / _ \ | |_) || |
    // | |_| | |_| || |  |  __/ ___ \|  _ < | |
    //  \____|\___/|___| |_| /_/   \_\_| \_\|_|
    private String direction;

    //------------------------------------------------------------
    // ThreadA
    public Elevator(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
        _id = totalElevatorCount++;
    } // ThreadA


    //------------------------------------------------------------
    // run
    public void run() {
        log.info(id + ": starting...");
        Timer.setSimulationTimer(id, mbox, sleepTime);
        int mCnt = 0;

        for (boolean quit = false; !quit; ) {
            Msg msg = mbox.receive();
            System.out.println("MSG MBOX: " + msg.getSender());
            log.info(id + ": message received: [" + msg + "].");

            switch (msg.getType()) {
                case TimesUp:
                    // Store data
                    String[] datas = msg.getDetails().split(" ");
                    passengerId = datas[1];
                    src = Integer.parseInt(datas[2]);
                    dest = Integer.parseInt(datas[3]);

                    // Action
                    log.info(id + ": -----------------------------------------------------------");
                    log.info(id + ": receiving order at " + appKickstarter.getSimulationTimeStr());
                    log.info(id + ": go to source floor...");
                    log.info("THIS_ID: " + this.id);
                    // Debug data
                    System.out.println("Order: ");
                    System.out.println("Passenger id: " + passengerId);
                    System.out.println("Source floor " + src);
                    System.out.println("Destination floor: " + dest);

                    // Already at src
                    if (idleFloor == src) {
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.Waiting, "Already at source floor!  (mCnt: " + ++mCnt + ")"));

                        //Svc_Req Passenger-0001 0 10
                        for (int i = 0; i < totalNumberOfElevator; i++) {
                            if (msg.getSender().equals(Elevator[i])) {
                                centralControlPanel.setCurrentPassenger(i, 1);
                                centralControlPanel.liftAvailable[i] = false;
                                centralControlPanel.addTotalPassenger(1);
                                centralControlPanel.setCurrentFloor(i, idleFloor);
                                centralControlPanel.setCurrentDirection(i, "S (Wait)");
                                break;
                            }
                        }
                    }
                    // Go to src
                    else {
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.GoToSrc, "Going to source floor!  (mCnt: " + ++mCnt + ")"));
                    }
                    break;

                case GoToSrc:
                    if (idleFloor > src) {
                        direction = "D";
                    } else if (idleFloor < src) {
                        direction = "U";
                    } else {
                        direction = "S";
                    }
                    String tmp = (src > dest) ? "D" : "U";
                    for (int i = 0; i < totalNumberOfElevator; i++) {
                        if (msg.getSender().equals(Elevator[i])) {
                            centralControlPanel.liftAvailable[i] = false;
                            centralControlPanel.setCurrentFloor(i, idleFloor);
                            centralControlPanel.setCurrentDirection(i, direction);
                            centralControlPanel.setCurrentPassenger(i, 1);
                            elevDepmsg = "Elev_Dep " + msg.getSender() + " " + idleFloor + " " + direction + " " + src + " " + dest;
                            elevArrmsg = "Elev_Arr " + msg.getSender() + " " + src + " " + tmp + " " + dest;
                            break;
                        }
                    }
//                    if (idleFloor < src) {
//                        for (int current = idleFloor + 1; current <= src; current++) {
//                            elevArrmsg += current + " ";
//                        }
//                    }
//                    if (idleFloor > src) {
//                        for (int current = idleFloor - 1; current >= src; current--) {
//                            elevArrmsg += current + " ";
//                        }
//                    }
                    System.out.println("Elevator_Dep message: " + elevDepmsg);
                    System.out.println("Elevator_Arr message: " + elevArrmsg);
                    GreetingServer.sendMsgToClient(elevDepmsg);
                    GreetingServer.sendMsgToClient(elevArrmsg);


                    //==========================================================================
                    log.info(id + ": " + msg.getSender() + " is going to source floor!!!");

                    // Debug data
                    System.out.println("GoToSrc: ");
                    System.out.println("current floor " + idleFloor);


                    System.out.println("Source floor " + src);

                    sleep(idleFloor, src, msg.getSender());
                    this.getMBox().send(new Msg(id, mbox, Msg.Type.Waiting, "Arrive at source floor!  (mCnt: " + ++mCnt + ")"));
                    break;

                case Waiting:
                    log.info(id + ": " + msg.getSender() + " is arrived at source floor!!!");
                    // Debug data
                    if(src<dest){
                        direction="U";
                    }else{
                        direction="D";
                    }
                    System.out.println("Waiting: ");
                    System.out.println("current floor " + src);
                    //Tabasco added code
                    for (int i = 0; i < totalNumberOfElevator; i++) {
                        if (msg.getSender().equals(Elevator[i])) {
                            centralControlPanel.liftAvailable[i] = false;
                            centralControlPanel.setCurrentDirection(i, "S (Wait)");
                            centralControlPanel.setCurrentFloor(i, src);
                            centralControlPanel.setCurrentPassenger(i, 1);
                            centralControlPanel.addTotalPassenger(1);
                            break;
                        }
                    }
                    // Wait for to src time
                    try {
                        System.out.println("Waiting time: " + doorOpenToClose);
                        Thread.sleep(doorOpenToClose);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // Go to destination
                        elevDepmsg="Elev_Dep "+msg.getSender()+" "+src+" "+direction+" "+dest;
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.GoToDest, "Going to destination floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case GoToDest:
                    log.info(id + ": " + msg.getSender() + " is going to destination floor!!!");
                    if (src > dest) {
                        direction = "D";
                    } else if (src < dest) {
                        direction = "U";
                    } else {
                        direction = "S";
                    }

                    for (int i = 0; i < totalNumberOfElevator; i++) {
                        if (msg.getSender().equals(Elevator[i])) {
                            elevDepmsg = "Elev_Dep " + msg.getSender() + " " + src + " " + direction + " " + dest;
                            break;
                        }
                    }
//                    if (src < dest) {
//                        for (int current = src + 1; current <= dest; current++) {
//                            elevDepmsg += current + " ";
//                        }
//                    }
//
//                    if (src > dest) {
//                        for (int current = src - 1; current >= dest; current--) {
//                            elevDepmsg += current + " ";
//                        }
//                    }
                    System.out.println("Elevator_Dep message: " + elevDepmsg);
                    GreetingServer.sendMsgToClient(elevDepmsg);

                    // Debug data
                    System.out.println("GoToDest: ");
                    System.out.println("current floor " + src);
                    for (int i = 0; i < totalNumberOfElevator; i++) {
                        if (msg.getSender().equals(Elevator[i])) {
                            centralControlPanel.setCurrentFloor(i, src);
                            break;
                        }
                    }
                    //
                    System.out.println("Destination floor " + dest);

                    sleep(src, dest, msg.getSender());
                    this.getMBox().send(new Msg(id, mbox, Msg.Type.ArriveDest, "Arrive at source floor!  (mCnt: " + ++mCnt + ")"));
                    break;

                case ArriveDest:
                    idleFloor = dest;
                    System.out.println("ArriveDest: ");
                    System.out.println("current floor " + idleFloor);

//                    for (int i = 0; i < totalNumberOfElevator; i++) {
//                        if (msg.getSender().equals(Elevator[i])) {
//                            elevDepmsg = "Elev_Dep " + msg.getSender() + " " + src + " " + direction + " " + dest;
//                            break;
//                        }
//                    }

                    for (int i = 0; i < totalNumberOfElevator; i++) {
                        if (msg.getSender().equals(Elevator[i])) {
                            centralControlPanel.liftAvailable[i] = true;
                            centralControlPanel.setCurrentPassenger(i, -1);
                            centralControlPanel.setCurrentDirection(i, "S Arrived");
                            elevArrmsg = "Elev_Arr " + msg.getSender() + " " + dest + " S " + Integer.MIN_VALUE;
                            break;
                        }
                    }

                    GreetingServer.sendMsgToClient(elevArrmsg);
                    log.info(id + ": -----------------------------------------------------------");

                    break;

                case Terminate:
                    quit = true;
                    break;

                default:
                    log.severe(id + ": unknown message type!!");
                    break;
            }
        }

        // declaring our departure
        appKickstarter.unregThread(this);
        log.info(id + ": terminating...");
    } // run

    private long GetArrivedTime(int from, int to) {
        double sleepTime;
        // Up

        if (from < to) {
            for (int i = 0; i < totalNumberOfElevator; i++) {
                centralControlPanel.setCurrentDirection(i, "U");
                break;
            }
            sleepTime = (to - from == 1) ? accUp : accUp + (to - from - 2) * upOneFloor + decUp;

        }
        // Down
        else {
            for (int i = 0; i < totalNumberOfElevator; i++) {
                centralControlPanel.setCurrentDirection(i, "D");
                break;
            }
            sleepTime = (from - to == 1) ? accDown : accDown + (from - to - 2) * downOneFloor + decDown;
        }

        return (long) (sleepTime * 1000);
    }

    private void sleep(int from, int to, String whichElevator) {
        try {
            int current = from;
            boolean up = from < to; // Set direction

            for (int i = 0; i < totalNumberOfElevator; i++) {
                if (whichElevator.equals(Elevator[i])) {
                    centralControlPanel.setCurrentDirection(i, up ? "U" : "D");
                }
            }
            // One floor
            if (Math.abs(from - to) == 1) {
                long sleepTime = (long) ((up ? accUp : accDown) * 1000);

                Thread.sleep(sleepTime);

                current += up ? 1 : -1;
                for (int i = 0; i < totalNumberOfElevator; i++) {
                    if (whichElevator.equals(Elevator[i])) {
                        centralControlPanel.setCurrentFloor(i, current);
                        break;
                    }
                }
                System.out.println("Reach " + current + " floor");
                System.out.println("Use " + sleepTime + " millisecond");
            }
            // More than one floor
            else {
                while (current != to) {
                    long sleepTime = 0;

                    // Acceleration
                    if (current == from) {
                        sleepTime = (long) ((up ? accUp : accDown) * 1000);
                    }
                    // Deceleration
                    else if (Math.abs(current - to) == 1) {
                        sleepTime = (long) ((up ? decUp : decDown) * 1000);
                    }
                    // Constant speed
                    else {
                        sleepTime = (long) ((up ? upOneFloor : downOneFloor) * 1000);
                    }

                    Thread.sleep(sleepTime);

                    current += up ? 1 : -1;
                    for (int i = 0; i < totalNumberOfElevator; i++) {
                        if (whichElevator.equals(Elevator[i])) {
                            centralControlPanel.setCurrentFloor(i, current);
                            break;
                        }
                    }
                    System.out.println("Reach " + current + " floor");
                    System.out.println("Use " + sleepTime + " millisecond");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
} // ThreadA

