package AppKickstarter.myThreads;

import AppKickstarter.misc.*;
import AppKickstarter.AppKickstarter;
import AppKickstarter.timer.Timer;


//======================================================================
// ThreadA
public class Elevator extends AppThread {
    private final int sleepTime = 5;
    private int idleFloor = 0;

    private String passengerId;
    private int src, dest;

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
                    log.info(id + ": receiving order at " + appKickstarter.getSimulationTimeStr());
                    log.info(id + ": go to source floor...");

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

                    // Wait for to src time
                    try {
                        long time = GetArrivedTime(idleFloor, src);  // calculation
                        Thread.sleep(time * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // Arrive at src floor
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.Waiting, "Arrive at source floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case Waiting:
                    log.info(id + ": " + msg.getSender() + " is arrived at source floor!!!");

                    // Waiting

                    // Go to destination
                    this.getMBox().send(new Msg(id, mbox, Msg.Type.GoToDest, "Going to destination floor!  (mCnt: " + ++mCnt + ")"));

                    break;

                case GoToDest:
                    log.info(id + ": " + msg.getSender() + " is going to destination floor!!!");

                    // Moving

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
        return 5;
    }

} // ThreadA

