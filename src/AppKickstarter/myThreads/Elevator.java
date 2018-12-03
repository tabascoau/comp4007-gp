package AppKickstarter.myThreads;

import AppKickstarter.AppKickstarter;
import AppKickstarter.gui.CentralControlPanel;
import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.GreetingServer;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import AppKickstarter.timer.Timer;

import java.util.ArrayList;


//======================================================================
// ThreadA
public class Elevator extends AppThread {
    enum Direction {
        Stop,
        Up,
        Down
    }

    public char GetDirectionChar() {
        switch (direction)
        {
            case Stop: return 'S';
            case Up: return 'U';
            case Down: return 'D';
            default: return 'S';
        }
    }

    public char GetIdChar()
    {
        String id = getID();
        return id.charAt(id.length()-1);
    }

    private final int sleepTime = 5;

    private int current = 0;
    private Direction status = Direction.Stop;
    private Direction direction = Direction.Stop;

    private int minFloor;
    private int maxFloor;
    private Floor[] floors;

    private float upOneFloor;
    private float downOneFloor;
    private float accUp;
    private float accDown;
    private float decUp;
    private float decDown;
    private float doorOpen;
    private float doorClose;
    private float doorWait;

    private CentralControlPanel centralControlPanel;

    public Floor[] GetFloorsClone()
    {
        Floor[] floors = new Floor[this.floors.length];
        for (int index = 0; index < this.floors.length; index++)
        {
            floors[index] = new Floor(this.floors[index]);
        }
        return floors;
    }

    //------------------------------------------------------------
    // ThreadA
    public Elevator(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);

        // Floors
        minFloor = Integer.parseInt(appKickstarter.getProperty("Bldg.MinFloorNumber"));
        maxFloor = Integer.parseInt(appKickstarter.getProperty("Bldg.MaxFloorNumber"));
        floors = new Floor[maxFloor + 1];
        for (int floor = 0; floor <= maxFloor; floor++) {
            floors[floor] = new Floor(floor);
        }

        // Variables
        upOneFloor = Float.parseFloat(appKickstarter.getProperty("Elev.Time.UpOneFloor"));
        downOneFloor = Float.parseFloat(appKickstarter.getProperty("Elev.Time.DownOneFloor"));
        accUp = Float.parseFloat(appKickstarter.getProperty("Elev.Time.AccUp"));
        accDown = Float.parseFloat(appKickstarter.getProperty("Elev.Time.AccDown"));
        decUp = Float.parseFloat(appKickstarter.getProperty("Elev.Time.DecUp"));
        decDown = Float.parseFloat(appKickstarter.getProperty("Elev.Time.DecDown"));
        doorOpen = Float.parseFloat(appKickstarter.getProperty("Elev.Time.DoorOpen"));
        doorClose = Float.parseFloat(appKickstarter.getProperty("Elev.Time.DoorClose"));
        doorWait = Float.parseFloat(appKickstarter.getProperty("Elev.Time.DoorWait"));

        // Update central control panel
        centralControlPanel = appKickstarter.GetCentralControlPanel();
        centralControlPanel.setFloorA(id, current);
        centralControlPanel.setDirectionA(id, GetDirectionString(status));
    } // ThreadA

    private String GetDirectionString(Direction status) {
        switch (status) {
            case Stop:
                return "Stop";
            case Up:
                return "Up";
            case Down:
                return "Down";
            default:
                return "null";
        }
//        return "null";
    }

    //------------------------------------------------------------
    // run
    public void run() {
        log.info(id + ": starting...");
        Timer.setSimulationTimer(id, mbox, sleepTime);
        int mCnt = 0;

        for (boolean quit = false; !quit; ) {
            Msg msg = mbox.receive();

//            log.info(id + ": message received: [" + msg + "].");
//            System.out.println(id + ": " + msg.getType());

            switch (msg.getType()) {
                case Waiting:
                    try {
                        // Not initial
                        if (msg.getDetails().split(" ")[0].equals("Waiting")) {
                            Thread.sleep((long) ((doorOpen + doorWait + doorClose) * 1000));
                        }

                        boolean idle = false;
                        boolean hasRequest = false;

                        while (!hasRequest) {
                            Direction requestDirection = GetRequestDirection(floors, current, direction);
                            if (requestDirection != Direction.Stop) {
                                hasRequest = true;
                                // Update status
                                status = requestDirection;
                                direction = status;
                                // Idle
                                if (idle)
                                {
                                    if (direction == Direction.Up && floors[current].up)
                                    {
                                        // Send Elev_Arr
                                        SendElevDep();

                                        Thread.sleep((long) ((doorOpen + doorWait + doorClose) * 1000));
                                        floors[current].up = false;

                                        // Send Elev_Dep
                                        SendElevArr();
                                    }
                                    else if (direction == Direction.Down && floors[current].down)
                                    {
                                        // Send Elev_Arr
                                        SendElevDep();

                                        Thread.sleep((long) ((doorOpen + doorWait + doorClose) * 1000));
                                        floors[current].up = false;

                                        // Send Elev_Dep
                                        SendElevArr();
                                    }
                                }
                                // To moving state
                                centralControlPanel.setDirectionA(id, GetDirectionString(status));
                                mbox.send(new Msg(id, mbox, Msg.Type.Moving, "Moving"));
                            } else {
                                // wait
                                idle = true;
                                Thread.sleep(1000);
//                                System.out.println(id + " Waiting");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                    break;

                case Moving:
//                    log.info(id + ": " + msg.getSender() + " is saying HiHi to me!!!");
                    boolean firstRun = true;

                    boolean up = status == Direction.Up;

                    long dccelerationSpeed = up ? (long) (decUp * 1000) : (long) (decDown * 1000);
                    int floorChange = up ? 1 : -1;
                    long accelerationSpeed = up ? (long) (accUp * 1000) : (long) (accDown * 1000);
                    long constantSpeed = up ? (long) (upOneFloor * 1000) : (long) (downOneFloor * 1000);

                    // Send Elev_Arr
                    SendElevDep();

                    while (true) {
                        boolean nextFloorStop = up ? floors[current + floorChange].up : floors[current + floorChange].down;
                        if (nextFloorStop) {
                            try {
                                // Decelerate
                                Thread.sleep(dccelerationSpeed);
                                // Update floor
                                if (up)
                                    floors[current + floorChange].up = false;
                                else
                                    floors[current + floorChange].down = false;
                                current += floorChange;
                                centralControlPanel.setFloorA(id, current);
                                // Update status
                                Direction requestDirection = GetRequestDirection(floors, current, direction);
                                direction = requestDirection;
                                status = Direction.Stop;
                                // To waiting state
                                centralControlPanel.setDirectionA(id, GetDirectionString(status));
                                mbox.send(new Msg(id, mbox, Msg.Type.Waiting, "Waiting"));

                                // Send Elev_Dep
                                SendElevArr();

                                break;
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        } else {
                            try {
                                if (firstRun) {
                                    // acceleration speed
                                    Thread.sleep(accelerationSpeed);
                                    firstRun = false;
                                } else {
                                    // Constant speed
                                    Thread.sleep(constantSpeed);
                                }
                                // Update floor
                                current += floorChange;
                                centralControlPanel.setFloorA(id, current);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
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

    private boolean HasUpperRequest(Floor[] floors, int current) {
        // Up to upper
        for (int upperFloor = current + 1; upperFloor <= maxFloor; upperFloor++) {
            if (floors[upperFloor].up) {
                return true;
            }
        }
        return false;
    }

    private boolean HasLowerRequest(Floor[] floors, int current) {
        // Down to lower
        for (int lowerFloor = current - 1; lowerFloor >= minFloor; lowerFloor--) {
            if (floors[lowerFloor].down) {
                return true;
            }
        }
        return false;
    }

    private int UppestRequest(Floor[] floors, int current) {
        int uppest = current;
        // Up to upper
        for (int upperFloor = current + 1; upperFloor <= maxFloor; upperFloor++) {
            if (floors[upperFloor].up) {
                uppest = upperFloor;
            }
        }
        return uppest;
    }

    private int LowestRequest(Floor[] floors, int current) {
        int lowest = current;
        // Down to lower
        for (int lowerFloor = current - 1; lowerFloor >= minFloor; lowerFloor--) {
            if (floors[lowerFloor].down) {
                lowest = lowerFloor;
            }
        }
        return lowest;
    }

    private Direction GetRequestDirection(Floor[] floors, int current, Direction direction) {
        if (direction == Direction.Up || direction == Direction.Stop) {
            if (HasUpperRequest(floors, current)) {
                return Direction.Up;
            } else if (HasLowerRequest(floors, current)) {
                return Direction.Down;
            }
        } else if (direction == Direction.Down) {
            if (HasLowerRequest(floors, current)) {
                return Direction.Down;
            } else if (HasUpperRequest(floors, current)) {
                return Direction.Up;
            }
        }

        return Direction.Stop;
    }

    // Default
    public void AddRequest(String str)
    {
        // Send data
        SendSvcReply(str);

        AddRequest(floors, current, direction, str);
    }

    // Simulation direct call
    public void AddRequest(Floor[] floors, int current, Direction direction, String str) {
        // Get message
        String message[] = str.split(" ");

        // Get message
        String passengerId = message[1];
        int src = Integer.parseInt(message[2]);
        int dest = Integer.parseInt(message[3]);

        // Set data
        boolean srcLargerThanCurrent = (src - current) > 0;
        boolean destLargerThanSrc = (dest - src) > 0;

        if (destLargerThanSrc)
        {
            floors[dest].up = true;
        }
        else
        {
            floors[dest].down = true;
        }

        // User status
        if (direction == Direction.Stop) {
            if ((src - current) > 0) {
                floors[src].up = true;
            } else if ((src - current) < 0) {
                floors[src].down = true;
            } else {
                if (destLargerThanSrc)
                {
                    floors[src].up = true;
                }
                else
                {
                    floors[src].down = true;
                }
            }
        } else if (direction == Direction.Up) {
            if (srcLargerThanCurrent) {
                if (destLargerThanSrc)
                {
                    floors[src].up = true;
                }
                else
                {
                    if (src > UppestRequest(floors, current))
                    {
                        floors[src].up = true;
                    }
                    else
                    {
                        floors[src].down = true;
                    }
                }
            } else {
                floors[src].down = true;
            }
        } else {
            if (srcLargerThanCurrent) {
                floors[src].up = true;
            } else {
                if (destLargerThanSrc)
                {
                    if (src < LowestRequest(floors, current))
                    {
                        floors[src].down = true;
                    }
                    else
                    {
                        floors[src].up = true;
                    }
                    floors[src].up = true;
                }
                else
                {
                    floors[src].down = true;
                }
            }
        }
    }

    public long GetSimulationTime(String str)
    {
        long time = 0;

        Floor[] floors = GetFloorsClone();
        int current = this.current;
        Direction status = this.status;
        Direction direction = this.direction;

        boolean idle = true;
        boolean finish = false;

        AddRequest(floors, current, direction, str);

        while (!finish) {
            if (status == Direction.Stop)
            {
                Direction requestDirection = GetRequestDirection(floors, current, direction);
                if (requestDirection != Direction.Stop) {
                    // Not initial
                    if (!idle) {
                        time += (long) ((doorOpen + doorWait + doorClose) * 1000);
                    }
                    // Update status
                    status = requestDirection;
                    direction = status;

                    // Idle
                    if (idle)
                    {
                        if (direction == Direction.Up && floors[current].up)
                        {
                            time += (long) ((doorOpen + doorWait + doorClose) * 1000);
                            floors[current].up = false;
                        }
                        else if (direction == Direction.Down && floors[current].down)
                        {
                            time += (long) ((doorOpen + doorWait + doorClose) * 1000);
                            floors[current].up = false;
                        }
                    }
                } else {
                    // wait
                    finish = true;
                }
            }
            else
            {
                idle = false;

                boolean firstRun = true;

                boolean up = status == Direction.Up;

                long dccelerationSpeed = up ? (long) (decUp * 1000) : (long) (decDown * 1000);
                int floorChange = up ? 1 : -1;
                long accelerationSpeed = up ? (long) (accUp * 1000) : (long) (accDown * 1000);
                long constantSpeed = up ? (long) (upOneFloor * 1000) : (long) (downOneFloor * 1000);

                while (status != Direction.Stop) {
                    boolean nextFloorStop = up ? floors[current + floorChange].up : floors[current + floorChange].down;
                    if (nextFloorStop) {
                        // Decelerate
                        time += dccelerationSpeed;
                        // Update floor
                        if (up)
                            floors[current + floorChange].up = false;
                        else
                            floors[current + floorChange].down = false;
                        current += floorChange;
                        // Update status
                        Direction requestDirection = GetRequestDirection(floors, current, direction);
                        direction = requestDirection;
                        status = Direction.Stop;
                    } else {
                        if (firstRun) {
                            // acceleration speed
                            time += accelerationSpeed;
                            firstRun = false;
                        } else {
                            // Constant speed
                            time += constantSpeed;
                        }
                        // Update floor
                        current += floorChange;
                    }
                }
            }
        }

        return time;
    }

    // To passenger Stream
    private void SendSvcReply(String str)
    {
        String message[] = str.split(" ");

        // Get message
        String passengerId = message[1];
        int src = Integer.parseInt(message[2]);
        int dest = Integer.parseInt(message[3]);

        String elevatorId = getID();

        String reply = "Svc_Reply " + passengerId + " " + src + " " + dest + " " + GetIdChar();
        log.info("Svc_Reply: " + reply);

        // Send
        GreetingServer.SendToServer(reply);
    }

    private ArrayList<Integer> GetUpperSchedule()
    {
        ArrayList<Integer> schedule = new ArrayList<Integer>();
        // Up to upper
        for (int upperFloor = current; upperFloor <= maxFloor; upperFloor++) {
            if (floors[upperFloor].up) {
                schedule.add(upperFloor);
            }
        }
        return schedule;
    }

    private ArrayList<Integer> GetLowerSchedule() {
        ArrayList<Integer> schedule = new ArrayList<Integer>();
        // Down to lower
        for (int lowerFloor = current; lowerFloor >= minFloor; lowerFloor--) {
            if (floors[lowerFloor].down) {
                schedule.add(lowerFloor);
            }
        }
        return schedule;
    }

    private void SendElevDep()
    {
        String elevatorId = getID();

        // Get schedule
        ArrayList<Integer> schedule = null;

        if (direction == Direction.Up)
        {
            schedule = GetUpperSchedule();
        }
        else if (direction == Direction.Down)
        {
            schedule = GetLowerSchedule();
        }

        String reply = "Elev_Dep " + GetIdChar() + " " + current + " " + GetDirectionChar();
        for (Integer floor: schedule)
        {
            reply += " " + floor;
        }
        log.info("Elev_Dep: " + reply);

        // Send
        GreetingServer.SendToServer(reply);
    }

    private void SendElevArr()
    {
        String elevatorId = getID();

        // Get schedule
        ArrayList<Integer> schedule = null;

        if (direction == Direction.Up)
        {
            schedule = GetUpperSchedule();
        }
        else if (direction == Direction.Down)
        {
            schedule = GetLowerSchedule();
        }

        String reply = "Elev_Arr " + GetIdChar() + " " + current + " " + GetDirectionChar();
        // May no request
        if (direction != Direction.Stop)
        {
            for (Integer floor : schedule)
            {
                reply += " " + floor;
            }
        }
        else
        {
            reply += " -3.14";
        }
        log.info("Elev_Arr: " + reply);

        // Send
        GreetingServer.SendToServer(reply);
    }

} // ThreadA

