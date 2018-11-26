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
    CentralControlPanel c=CentralControlPanel.getInstance();

    private String passengerId;
    private int src, dest;

    //  / ___| | | |_ _| |  _ \ / \  |  _ \_   _|
    // | |  _| | | || |  | |_) / _ \ | |_) || |
    // | |_| | |_| || |  |  __/ ___ \|  _ < | |
    //  \____|\___/|___| |_| /_/   \_\_| \_\|_|




    //------------------------------------------------------------
    // ThreadA
    public Elevator(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
    } // ThreadA


    //------------------------------------------------------------
    // run
    public void run() {
        log.info(id + ": starting...");
        Timer.setSimulationTimer(id, mbox,sleepTime);
        int mCnt = 0;

        for (boolean quit = false; !quit;) {
            Msg msg = mbox.receive();

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
                    if (idleFloor == src)
                    {
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.Waiting, "Alreadly at source floor!  (mCnt: " + ++mCnt + ")"));
                    }
                    // Go to src
                    else
                    {
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.GoToSrc, "Going to source floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case GoToSrc:
                    log.info(id + ": " + msg.getSender() + " is going to source floor!!!");

                    // Debug data
                    System.out.println("GoToSrc: ");
                    System.out.println("current floor " + idleFloor);
                    //Tabasco added code
                    c.setaCurrentFloor(idleFloor);
                    //


                    System.out.println("Source floor " + src);

                    // Wait for to src time
                    try {
                        long sleepTime = GetArrivedTime(idleFloor, src);  // calculation
                        System.out.println("Travelling time: " + sleepTime);

                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // Arrive at src floor
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.Waiting, "Arrive at source floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case Waiting:
                    log.info(id + ": " + msg.getSender() + " is arrived at source floor!!!");

                    // Debug data
                    System.out.println("Waiting: ");
                    System.out.println("current floor " + src);
                    //Tabasco added code
                    c.setaCurrentFloor(src);
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
                    c.setaCurrentFloor(src);
                    //
                    System.out.println("Destination floor " + dest);

                    // Wait for to src time
                    try {
                        long sleepTime = GetArrivedTime(src, dest);  // calculation
                        System.out.println("Travelling time: " + sleepTime);

                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // Arrive at src floor
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.ArriveDest, "Arrive at source floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case ArriveDest:
                    // Set current floor to idle floor
                    idleFloor = dest;

                    // Debug data
                    System.out.println("ArriveDest: ");
                    System.out.println("current floor " + idleFloor);
                    //Tabasco added code
                    c.setaCurrentFloor(idleFloor);
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

    private long GetArrivedTime(int from, int to)
    {
        double sleepTime;

        // Up
        if (from < to)
        {
            sleepTime = (to - from == 1) ? accUp : accUp + (to - from - 2) * upOneFloor + decUp;
        }
        // Down
        else
        {
            sleepTime = (from - to == 1) ? accDown : accDown + (from - to - 2) * downOneFloor + decDown;
        }

        return (long) (sleepTime * 1000);
    }

} // ThreadA

