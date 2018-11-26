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

    private final int sleepTime = 5;
    private int idleFloor = 0;
    // Speed variable
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

    private String passengerId;
    private int src, dest;

    //new added code
    String elevArrmsg = "";

    //  / ___| | | |_ _| |  _ \ / \  |  _ \_   _|
    // | |  _| | | || |  | |_) / _ \ | |_) || |
    // | |_| | |_| || |  |  __/ ___ \|  _ < | |
    //  \____|\___/|___| |_| /_/   \_\_| \_\|_|
    private char direction;

    //------------------------------------------------------------
    // ThreadA
    public Elevator(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
    } // ThreadA


    //------------------------------------------------------------
    // run
    public void run() {
        log.info(id + ": starting...");
        Timer.setSimulationTimer(id, mbox, sleepTime);
        int mCnt = 0;

        for (boolean quit = false; !quit; ) {
            Msg msg = mbox.receive();
            System.out.println("FUCKING MSG MBOX: " + msg.getSender());
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

                    // Debug data
                    System.out.println("Order: ");
                    System.out.println("Passenger id: " + passengerId);
                    System.out.println("Source floor " + src);
                    System.out.println("Destination floor: " + dest);

                    // Already at src
                    if (idleFloor == src) {
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.Waiting, "Already at source floor!  (mCnt: " + ++mCnt + ")"));
                    }
                    // Go to src
                    else {
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.GoToSrc, "Going to source floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case GoToSrc:
                    if (msg.getSender().equals("ElevatorA")) {
                        elevArrmsg = "Elev_Arr" + " A " + idleFloor + " " + direction + " ";
                    } else {
                        elevArrmsg = "Elev_Arr" + " B " + idleFloor + " " + direction + " ";
                    }
                    for (int current = idleFloor + 1; current <= src; current++) {
                        elevArrmsg += " " + current;
                    }
                    System.out.println("Elevator_Arr message: " + elevArrmsg);


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
                    System.out.println("Waiting: ");
                    System.out.println("current floor " + src);
                    //Tabasco added code
                    if (msg.getSender().equals("ElevatorA")) {
                        centralControlPanel.setaDirection('S');
                        centralControlPanel.setaCurrentFloor(src, "wait");
                    } else {
                        centralControlPanel.setbDirection('S');
                        centralControlPanel.setbCurrentFloor(src, "wait");
                    }
//                    centralControlPanel.setaCurrentFloor(src);
                    //

                    // Wait for to src time
                    try {
                        System.out.println("Waiting time: " + doorOpenToClose);
                        Thread.sleep(doorOpenToClose);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // Go to destination
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.GoToDest, "Going to destination floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case GoToDest:
                    log.info(id + ": " + msg.getSender() + " is going to destination floor!!!");

                    // Debug data
                    System.out.println("GoToDest: ");
                    System.out.println("current floor " + src);
                    //Tabasco added code
                    if (msg.getSender().equals("ElevatorA")) {
                        centralControlPanel.setaCurrentFloor(src);
                    } else {
                        centralControlPanel.setbCurrentFloor(src);
                    }

                    //
                    System.out.println("Destination floor " + dest);

                    sleep(src, dest, msg.getSender());
                    this.getMBox().send(new Msg(id, mbox, Msg.Type.ArriveDest, "Arrive at source floor!  (mCnt: " + ++mCnt + ")"));
                    break;

                case ArriveDest:
                    // Set current floor to idle floor
                    idleFloor = dest;

                    // Debug data
                    System.out.println("ArriveDest: ");
                    System.out.println("current floor " + idleFloor);
                    //Tabasco added code
                    if (msg.getSender().equals("ElevatorA")) {
                        centralControlPanel.setaDirection('S');
                    } else {
                        centralControlPanel.setbDirection('S');
                    }
//                    centralControlPanel.setaCurrentFloor(idleFloor);
                    //
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
            centralControlPanel.setaDirection('U');
            centralControlPanel.setbDirection('U');
            sleepTime = (to - from == 1) ? accUp : accUp + (to - from - 2) * upOneFloor + decUp;

        }
        // Down
        else {
            centralControlPanel.setaDirection('D');
            centralControlPanel.setbDirection('D');
            sleepTime = (from - to == 1) ? accDown : accDown + (from - to - 2) * downOneFloor + decDown;
        }

        return (long) (sleepTime * 1000);
    }

    private void sleep(int from, int to, String whichElevator) {
        try {
            int current = from;
            boolean up = from < to; // Set direction
            if(whichElevator.equals("ElevatorA")) {
                centralControlPanel.setaDirection(up ? 'U' : 'D');
            }else {
                centralControlPanel.setbDirection(up ? 'U' : 'D');
            }
            // One floor
            if (Math.abs(from - to) == 1) {
                long sleepTime = (long) ((up ? accUp : accDown) * 1000);

                Thread.sleep(sleepTime);

                current += up ? 1 : -1;
                if (whichElevator.equals("ElevatorA")) {
                    centralControlPanel.setaCurrentFloor(current);
                } else {
                    centralControlPanel.setbCurrentFloor(current);
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
                    if (whichElevator.equals("ElevatorA")) {
                        centralControlPanel.setaCurrentFloor(current);
                    } else {
                        centralControlPanel.setbCurrentFloor(current);
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

