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
    private final int maxPassenger=10;
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
    String elevDepmsg = "";
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

                    // Debug data
                    System.out.println("Order: ");
                    System.out.println("Passenger id: " + passengerId);
                    System.out.println("Source floor " + src);
                    System.out.println("Destination floor: " + dest);

                    // Already at src
                    if (idleFloor == src) {
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.Waiting, "Already at source floor!  (mCnt: " + ++mCnt + ")"));
                        if (msg.getSender().equals("ElevatorA")) {
                            centralControlPanel.setAPassenger(1);
                            centralControlPanel.aNoPeople=false;

                        } else if (msg.getSender().equals("ElevatorB")) {
                            centralControlPanel.setBPassenger(1);
                            centralControlPanel.bNoPeople=false;
                            centralControlPanel.addTotalPassenger(1);
                        } else if (msg.getSender().equals("ElevatorC")) {
                            centralControlPanel.setCPassenger(1);
                            centralControlPanel.cNoPeople=false;
                        } else if (msg.getSender().equals("ElevatorD")) {
                            centralControlPanel.setDPassenger(1);
                            centralControlPanel.dNoPeople=false;
                        } else if (msg.getSender().equals("ElevatorE")) {
                            centralControlPanel.setEPassenger(1);
                            centralControlPanel.eNoPeople=false;
                        }

                    }
                    // Go to src
                    else {
                        if (msg.getSender().equals("ElevatorA")) {
                            centralControlPanel.aNoPeople=false;
                        } else if (msg.getSender().equals("ElevatorB")) {
                            centralControlPanel.bNoPeople=false;
                        } else if (msg.getSender().equals("ElevatorC")) {
                            centralControlPanel.cNoPeople=false;
                        } else if (msg.getSender().equals("ElevatorD")) {
                            centralControlPanel.dNoPeople=false;
                        } else if (msg.getSender().equals("ElevatorE")) {
                            centralControlPanel.eNoPeople=false;
                        }
                        this.getMBox().send(new Msg(id, mbox, Msg.Type.GoToSrc, "Going to source floor!  (mCnt: " + ++mCnt + ")"));
                    }

                    break;

                case GoToSrc:
                    if (idleFloor > src) {
                        direction = 'D';
                    } else if (idleFloor < src) {
                        direction = 'U';
                    } else {
                        direction = 'S';
                    }
                    if (msg.getSender().equals("ElevatorA")) {
                        centralControlPanel.aNoPeople=false;
                        System.out.println("*********************************************** : "+centralControlPanel.getAFreeElevator());
                        elevArrmsg = "Elev_Arr" + " A " + idleFloor + " " + direction + " ";
                        centralControlPanel.setAPassenger(1);
                        centralControlPanel.addTotalPassenger(1);
                    } else if (msg.getSender().equals("ElevatorB")) {
                        centralControlPanel.bNoPeople=false;
                        centralControlPanel.setBPassenger(1);
                        elevArrmsg = "Elev_Arr" + " B " + idleFloor + " " + direction + " ";
                        centralControlPanel.addTotalPassenger(1);
                    } else if (msg.getSender().equals("ElevatorC")) {
                        centralControlPanel.cNoPeople=false;
                        centralControlPanel.setCPassenger(1);
                        elevArrmsg = "Elev_Arr" + " C " + idleFloor + " " + direction + " ";
                        centralControlPanel.addTotalPassenger(1);
                    } else if (msg.getSender().equals("ElevatorD")) {
                        centralControlPanel.dNoPeople=false;
                        centralControlPanel.setDPassenger(1);
                        elevArrmsg = "Elev_Arr" + " D " + idleFloor + " " + direction + " ";
                        centralControlPanel.addTotalPassenger(1);
                    } else if (msg.getSender().equals("ElevatorE")) {
                        centralControlPanel.eNoPeople=false;
                        centralControlPanel.setEPassenger(1);
                        elevArrmsg = "Elev_Arr" + " E " + idleFloor + " " + direction + " ";
                        centralControlPanel.addTotalPassenger(1);
                    }
                    if (idleFloor < src) {
                        for (int current = idleFloor + 1; current <= src; current++) {
                            elevArrmsg += current + " ";
                        }
                    }

                    if (idleFloor > src) {
                        for (int current = idleFloor - 1; current >= src; current--) {
                            elevArrmsg += current + " ";
                        }
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
                        centralControlPanel.aNoPeople=false;
                        centralControlPanel.setaDirection('S');
                        centralControlPanel.setaCurrentFloor(src, "wait");
                    } else if (msg.getSender().equals("ElevatorB")) {
                        centralControlPanel.bNoPeople=false;
                        centralControlPanel.setbDirection('S');
                        centralControlPanel.setbCurrentFloor(src, "wait");
                    } else if (msg.getSender().equals("ElevatorC")) {
                        centralControlPanel.cNoPeople=false;
                        centralControlPanel.setcDirection('S');
                        centralControlPanel.setcCurrentFloor(src, "wait");
                    } else if (msg.getSender().equals("ElevatorD")) {
                        centralControlPanel.dNoPeople=false;
                        centralControlPanel.setdDirection('S');
                        centralControlPanel.setdCurrentFloor(src, "wait");
                    } else if (msg.getSender().equals("ElevatorE")) {
                        centralControlPanel.eNoPeople=false;
                        centralControlPanel.seteDirection('S');
                        centralControlPanel.seteCurrentFloor(src, "wait");
                    }

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

                    if (src > dest) {
                        direction = 'D';
                    } else if (src < dest) {
                        direction = 'U';
                    } else {
                        direction = 'S';
                    }

                    if (msg.getSender().equals("ElevatorA")) {
                        elevDepmsg = "Elev_Dep" + " A " + src + " " + direction + " ";
                    } else if (msg.getSender().equals("ElevatorB")) {
                        elevDepmsg = "Elev_Dep" + " B " + src + " " + direction + " ";
                    } else if (msg.getSender().equals("ElevatorC")) {
                        elevDepmsg = "Elev_Dep" + " C " + src + " " + direction + " ";
                    } else if (msg.getSender().equals("ElevatorD")) {
                        elevDepmsg = "Elev_Dep" + " D " + src + " " + direction + " ";
                    } else if (msg.getSender().equals("ElevatorE")) {
                        elevDepmsg = "Elev_Dep" + " E " + src + " " + direction + " ";
                    }
                    if (src < dest) {
                        for (int current = src + 1; current <= dest; current++) {
                            elevDepmsg += current + " ";
                        }
                    }

                    if (src > dest) {
                        for (int current = src - 1; current >= dest; current--) {
                            elevDepmsg += current + " ";
                        }
                    }
                    System.out.println("Elevator_Dep message: " + elevDepmsg);

                    // Debug data
                    System.out.println("GoToDest: ");
                    System.out.println("current floor " + src);
                    if (msg.getSender().equals("ElevatorA")) {
                        centralControlPanel.setaCurrentFloor(src, "wait");
                    } else if (msg.getSender().equals("ElevatorB")) {
                        centralControlPanel.setbCurrentFloor(src, "wait");
                    } else if (msg.getSender().equals("ElevatorC")) {
                        centralControlPanel.setcCurrentFloor(src, "wait");
                    } else if (msg.getSender().equals("ElevatorD")) {
                        centralControlPanel.setdCurrentFloor(src, "wait");

                    } else if (msg.getSender().equals("ElevatorE")) {
                        centralControlPanel.seteCurrentFloor(src, "wait");
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

                    if (msg.getSender().equals("ElevatorA")) {
                        centralControlPanel.aNoPeople=true;
                        centralControlPanel.setAPassenger(-1);
                        centralControlPanel.setaDirection('S');
                    } else if (msg.getSender().equals("ElevatorB")) {
                        centralControlPanel.bNoPeople=true;
                        centralControlPanel.setBPassenger(-1);
                        centralControlPanel.setbDirection('S');
                    } else if (msg.getSender().equals("ElevatorC")) {
                        centralControlPanel.cNoPeople=true;
                        centralControlPanel.setCPassenger(-1);
                        centralControlPanel.setcDirection('S');
                    } else if (msg.getSender().equals("ElevatorD")) {
                        centralControlPanel.dNoPeople=true;
                        centralControlPanel.setDPassenger(-1);
                        centralControlPanel.setdDirection('S');

                    } else if (msg.getSender().equals("ElevatorE")) {
                        centralControlPanel.eNoPeople=true;
                        centralControlPanel.setEPassenger(-1);
                        centralControlPanel.seteDirection('S');
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
            centralControlPanel.setcDirection('U');
            centralControlPanel.setdDirection('U');
            centralControlPanel.seteDirection('U');
            sleepTime = (to - from == 1) ? accUp : accUp + (to - from - 2) * upOneFloor + decUp;

        }
        // Down
        else {
            centralControlPanel.setaDirection('D');
            centralControlPanel.setbDirection('D');
            centralControlPanel.setcDirection('D');
            centralControlPanel.setdDirection('D');
            centralControlPanel.seteDirection('D');
            sleepTime = (from - to == 1) ? accDown : accDown + (from - to - 2) * downOneFloor + decDown;
        }

        return (long) (sleepTime * 1000);
    }

    private void sleep(int from, int to, String whichElevator) {
        try {
            int current = from;
            boolean up = from < to; // Set direction

            if (whichElevator.equals("ElevatorA")) {
                centralControlPanel.setaDirection(up ? 'U' : 'D');
            } else if (whichElevator.equals("ElevatorB")) {
                centralControlPanel.setbDirection(up ? 'U' : 'D');
            } else if (whichElevator.equals("ElevatorC")) {
                centralControlPanel.setcDirection(up ? 'U' : 'D');
            } else if (whichElevator.equals("ElevatorD")) {
                centralControlPanel.setdDirection(up ? 'U' : 'D');
            } else if (whichElevator.equals("ElevatorE")) {
                centralControlPanel.seteDirection(up ? 'U' : 'D');
            }
            // One floor
            if (Math.abs(from - to) == 1) {
                long sleepTime = (long) ((up ? accUp : accDown) * 1000);

                Thread.sleep(sleepTime);

                current += up ? 1 : -1;
                if (whichElevator.equals("ElevatorA")) {
                    centralControlPanel.setaCurrentFloor(current);
                } else if (whichElevator.equals("ElevatorB")) {
                    centralControlPanel.setbCurrentFloor(current);
                } else if (whichElevator.equals("ElevatorC")) {
                    centralControlPanel.setcCurrentFloor(current);
                } else if (whichElevator.equals("ElevatorD")) {
                    centralControlPanel.setdCurrentFloor(current);
                } else if (whichElevator.equals("ElevatorE")) {
                    centralControlPanel.seteCurrentFloor(current);
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
                    } else if (whichElevator.equals("ElevatorB")) {
                        centralControlPanel.setbCurrentFloor(current);
                    } else if (whichElevator.equals("ElevatorC")) {
                        centralControlPanel.setcCurrentFloor(current);
                    } else if (whichElevator.equals("ElevatorD")) {
                        centralControlPanel.setdCurrentFloor(current);
                    } else if (whichElevator.equals("ElevatorE")) {
                        centralControlPanel.seteCurrentFloor(current);
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

