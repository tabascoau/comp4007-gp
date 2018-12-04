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
        switch (direction) {
            case Stop:
                return 'S';
            case Up:
                return 'U';
            case Down:
                return 'D';
            default:
                return 'S';
        }
    }

    public char GetIdChar() {
        String id = getID();
        return id.charAt(id.length() - 1);
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

    private float simulationTime;

    private CentralControlPanel centralControlPanel;

    public Floor[] GetFloorsClone() {
        Floor[] floors = new Floor[this.floors.length];
        for (int index = 0; index < this.floors.length; index++) {
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
        simulationTime = Float.parseFloat(appKickstarter.getProperty("Timer.SimulationSpeed"));

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
                            Thread.sleep((long) ((doorOpen + doorWait + doorClose) * simulationTime));
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
                                if (idle) {
                                    if (direction == Direction.Up && floors[current].up) {
                                        // Send Elev_Arr
                                        SendElevArr();

                                        Thread.sleep((long) ((doorOpen + doorWait + doorClose) * simulationTime));
                                        floors[current].up = false;
                                    } else if (direction == Direction.Down && floors[current].down) {
                                        // Send Elev_Arr
                                        SendElevArr();

                                        Thread.sleep((long) ((doorOpen + doorWait + doorClose) * simulationTime));
                                        floors[current].up = false;
                                    }
                                }
                                // To moving state
                                centralControlPanel.setDirectionA(id, GetDirectionString(status));
                                mbox.send(new Msg(id, mbox, Msg.Type.Moving, "Moving"));
                            } else {
                                // wait
                                idle = true;
                                Thread.sleep((long) simulationTime);
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

                    long dccelerationSpeed = up ? (long) (decUp * simulationTime) : (long) (decDown * simulationTime);
                    int floorChange = up ? 1 : -1;
                    long accelerationSpeed = up ? (long) (accUp * simulationTime) : (long) (accDown * simulationTime);
                    long constantSpeed = up ? (long) (upOneFloor * simulationTime) : (long) (downOneFloor * simulationTime);

                    // Send Elev_Arr
                    SendElevDep();

                    while (true) {
                        int next = current + floorChange;

                        boolean hasSignal = up ? floors[next].up : floors[next].down;
                        // Sure arrive src/dest
                        boolean nextFloorStop = up ?
                                floors[next].up &&
                                        (floors[next].upUserDirection == Direction.Up ||
                                                (floors[next].upUserDirection == null && floors[next].srcArrived)) :
                                floors[next].down &&
                                        (floors[next].downUserDirection == Direction.Down ||
                                                (floors[next].downUserDirection == null && floors[next].srcArrived));
                        // Three case: no signal, has signal and same Direction, has signal but different Direction
                        // has signal and same Direction = stop
                        if (nextFloorStop) {
                            System.out.println("Same");
                            // decelerate and stop
                            try {
                                // Decelerate
                                Thread.sleep(dccelerationSpeed);
                                // Update floor
                                if (up) {
                                    // Go to src
                                    if (floors[next].up && floors[next].upUserDirection != null)
                                    {
                                        floors[next].up = false;
                                        floors[next].upUserDirection = null;
                                        floors[floors[next].dest].srcArrived = true;
                                        System.out.println("To src");
                                    }
                                    // Go to dest
                                    else
                                    {
                                        floors[next].up = false;
                                        floors[next].srcArrived = false;
                                        System.out.println("To dest");
                                    }
                                }
                                else {
                                    // Go to src
                                    if (floors[next].down && floors[next].downUserDirection != null)
                                    {
                                        floors[next].down = false;
                                        floors[next].downUserDirection = null;
                                        floors[floors[next].dest].srcArrived = true;
                                        System.out.println("To src");
                                    }
                                    // Go to dest
                                    else
                                    {
                                        floors[next].down = false;
                                        floors[next].srcArrived = false;
                                        System.out.println("To dest");
                                    }
                                }
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
                        }
                        // no signal + has signal but different Direction(Ignore+change)
                        else {
                            int floorIndex = up ? UppestRequest(floors, current) : LowestRequest(floors, current);

                            if (hasSignal)
                            {
                                // Is uppest request -> decelerate and stop
                                if (floorIndex == next)
                                {
                                    // decelerate and stop
                                    try {
                                        // Decelerate
                                        Thread.sleep(dccelerationSpeed);
                                        // Update floor
                                        if (up) {
                                            // Go to src
                                            if (floors[next].up && floors[next].upUserDirection != null)
                                            {
                                                floors[next].up = false;
                                                floors[next].upUserDirection = null;
                                                floors[floors[next].dest].srcArrived = true;
                                                System.out.println("To src");
                                            }
                                            // Go to dest
                                            else
                                            {
                                                floors[next].up = false;
                                                floors[next].srcArrived = false;
                                                System.out.println("To dest");
                                            }
                                        }
                                        else {
                                            // Go to src
                                            if (floors[next].down && floors[next].downUserDirection != null)
                                            {
                                                floors[next].down = false;
                                                floors[next].downUserDirection = null;
                                                floors[floors[next].dest].srcArrived = true;
                                                System.out.println("To src");
                                            }
                                            // Go to dest
                                            else
                                            {
                                                floors[next].down = false;
                                                floors[next].srcArrived = false;
                                                System.out.println("To dest");
                                            }
                                        }
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
                                }
                                // Not uppest request -> change direction and pass
                                else
                                {
                                    // Pass
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

                                        // Change direction
                                        floors[current].InvertDirection();
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                            }
                            else {
                                // Pass
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
            if (floors[upperFloor].up &&
                    (floors[upperFloor].upUserDirection == Direction.Up ||
                            (floors[upperFloor].upUserDirection == null && floors[upperFloor].srcArrived))) {
                return true;
            }
        }
        return false;
    }

    private boolean HasLowerRequest(Floor[] floors, int current) {
        // Down to lower
        for (int lowerFloor = current - 1; lowerFloor >= minFloor; lowerFloor--) {
            if (floors[lowerFloor].down &&
                    (floors[lowerFloor].downUserDirection == Direction.Down ||
                            (floors[lowerFloor].downUserDirection == null && floors[lowerFloor].srcArrived))) {
                return true;
            }
        }
        return false;
    }

    private boolean HasUpperReverseRequest(Floor[] floors, int current) {
        // Up to upper
        for (int upperFloor = current + 1; upperFloor <= maxFloor; upperFloor++) {
            if (floors[upperFloor].up && floors[upperFloor].upUserDirection == Direction.Down) {
                return true;
            }
        }
        return false;
    }

    private boolean HasLowerReverseRequest(Floor[] floors, int current) {
        // Down to lower
        for (int lowerFloor = current - 1; lowerFloor >= minFloor; lowerFloor--) {
            if (floors[lowerFloor].down && floors[lowerFloor].downUserDirection == Direction.Up) {
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
            } else if (HasUpperReverseRequest(floors, current)) {
                return Direction.Up;
            } else if (HasLowerReverseRequest(floors, current)) {
                return Direction.Down;
            }
        } else if (direction == Direction.Down) {
            if (HasLowerRequest(floors, current)) {
                return Direction.Down;
            } else if (HasUpperRequest(floors, current)) {
                return Direction.Up;
            } else if (HasLowerReverseRequest(floors, current)) {
                return Direction.Down;
            } else if (HasUpperReverseRequest(floors, current)) {
                return Direction.Up;
            }
        }

        return Direction.Stop;
    }

    // Default
    public void AddRequest(String str) {
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
        boolean destLargetThanCurrent = (dest - current) > 0;

        Direction srcDirection;

        if ((dest - src) > 0)
        {
            srcDirection = Direction.Up;
        }
        else if ((dest - src) < 0)
        {
            srcDirection = Direction.Down;
        }
        else
        {
            srcDirection = null;
        }

        // Direction
        // Stop
        if (direction == Direction.Stop) {
            // current < src
            if ((src - current) > 0) {
                // current < src < dest
                if (destLargerThanSrc)
                {
                    floors[src].up = true;
                    floors[dest].up = true;
                    floors[src].upUserDirection = srcDirection;
                    floors[src].dest = dest;
                }
                // current < src and dest < src
                else
                {
                    // current < dest < src
                    if (destLargetThanCurrent)
                    {
                        floors[src].up = true;
                        floors[dest].down = true;
                        floors[src].downUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                    // dest < current < src
                    else
                    {
                        floors[src].up = true;
                        floors[dest].down = true;
                        floors[src].downUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                }
            }
            // src < current
            else if ((src - current) < 0) {
                // src < current and src < dest
                if (destLargerThanSrc)
                {
                    // src < current < dest
                    if (destLargetThanCurrent)
                    {
                        floors[src].down = true;
                        floors[dest].up = true;
                        floors[src].upUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                    // src < dest < current
                    else
                    {
                        floors[src].down = true;
                        floors[dest].up = true;
                        floors[src].upUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                }
                // dest < src < current
                else
                {
                    floors[src].down = true;
                    floors[dest].down = true;
                    floors[src].downUserDirection = srcDirection;
                    floors[src].dest = dest;
                }
            }
            // Same floor with current
            else {
                if (destLargerThanSrc) {
                    floors[src].up = true;
                    floors[dest].up = true;
                    floors[src].upUserDirection = srcDirection;
                    floors[src].dest = dest;
                } else {
                    floors[src].down = true;
                    floors[dest].down = true;
                    floors[src].downUserDirection = srcDirection;
                    floors[src].dest = dest;
                }
            }
        }
        // Up, Down
        else {
            // up, current < src
            if (srcLargerThanCurrent) {
                // up, current < src < dest
                if (destLargerThanSrc) {
                    floors[src].up = true;
                    floors[dest].up = true;
                    floors[src].upUserDirection = srcDirection;
                    floors[src].dest = dest;
                }
                // up, current < src and dest < src
                else {
                    // up, current < dest < src
                    if (destLargetThanCurrent) {
                        floors[src].up = true;
                        floors[dest].down = true;
                        floors[src].downUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                    // up, dest < current < src
                    else {
                        floors[src].up = true;
                        floors[dest].down = true;
                        floors[src].downUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                }
            }
            // up, src < current
            else {
                // up, src < current and src < dest
                if (destLargerThanSrc) {
                    // up, src < current < dest
                    if (destLargetThanCurrent) {
                        floors[src].down = true;
                        floors[dest].up = true;
                        floors[src].upUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                    // up, src < dest < current
                    else {
                        floors[src].down = true;
                        floors[dest].up = true;
                        floors[src].upUserDirection = srcDirection;
                        floors[src].dest = dest;
                    }
                }
                // up, dest < src < current
                else {
                    floors[src].down = true;
                    floors[dest].down = true;
                    floors[src].downUserDirection = srcDirection;
                    floors[src].dest = dest;
                }
            }
        }
    }

    public long GetSimulationTime(String str) {
        long time = 0;

        Floor[] floors = GetFloorsClone();
        int current = this.current;
        Direction status = this.status;
        Direction direction = this.direction;

        boolean idle = true;
        boolean finish = false;

        AddRequest(floors, current, direction, str);

        while (!finish) {
            if (status == Direction.Stop) {
                Direction requestDirection = GetRequestDirection(floors, current, direction);
                if (requestDirection != Direction.Stop) {
                    // Not initial
                    if (!idle) {
                        time += (long) ((doorOpen + doorWait + doorClose) * simulationTime);
                    }
                    // Update status
                    status = requestDirection;
                    direction = status;

                    // Idle
                    if (idle) {
                        if (direction == Direction.Up && floors[current].up) {
                            time += (long) ((doorOpen + doorWait + doorClose) * simulationTime);
                            floors[current].up = false;
                        } else if (direction == Direction.Down && floors[current].down) {
                            time += (long) ((doorOpen + doorWait + doorClose) * simulationTime);
                            floors[current].up = false;
                        }
                    }
                } else {
                    // wait
                    finish = true;
                }
            } else {
                idle = false;

                boolean firstRun = true;

                boolean up = status == Direction.Up;

                long dccelerationSpeed = up ? (long) (decUp * simulationTime) : (long) (decDown * simulationTime);
                int floorChange = up ? 1 : -1;
                long accelerationSpeed = up ? (long) (accUp * simulationTime) : (long) (accDown * simulationTime);
                long constantSpeed = up ? (long) (upOneFloor * simulationTime) : (long) (downOneFloor * simulationTime);

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
    private void SendSvcReply(String str) {
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

    private ArrayList<Integer> GetUpperSchedule() {
        ArrayList<Integer> schedule = new ArrayList<Integer>();
        // Up to upper
        for (int upperFloor = current + 1; upperFloor <= maxFloor; upperFloor++) {
            if (floors[upperFloor].up) {
                schedule.add(upperFloor);
            }
        }
        return schedule;
    }

    private ArrayList<Integer> GetLowerSchedule() {
        ArrayList<Integer> schedule = new ArrayList<Integer>();
        // Down to lower
        for (int lowerFloor = current - 1; lowerFloor >= minFloor; lowerFloor--) {
            if (floors[lowerFloor].down) {
                schedule.add(lowerFloor);
            }
        }
        return schedule;
    }

    private void SendElevDep() {
        String elevatorId = getID();

        // Get schedule
        ArrayList<Integer> schedule = null;

        if (direction == Direction.Up) {
            schedule = GetUpperSchedule();
        } else if (direction == Direction.Down) {
            schedule = GetLowerSchedule();
        }

        String reply = "Elev_Dep " + GetIdChar() + " " + current + " " + GetDirectionChar();
        for (Integer floor : schedule) {
            reply += " " + floor;
        }
        log.info("Elev_Dep: " + reply);

        // Send
        GreetingServer.SendToServer(reply);
    }

    private void SendElevArr() {
        String elevatorId = getID();

        // Get schedule
        ArrayList<Integer> schedule = null;

        if (direction == Direction.Up) {
            schedule = GetUpperSchedule();
        } else if (direction == Direction.Down) {
            schedule = GetLowerSchedule();
        }

        String reply = "Elev_Arr " + GetIdChar() + " " + current + " " + GetDirectionChar();
        // May no request
        if (direction != Direction.Stop) {
            for (Integer floor : schedule) {
                reply += " " + floor;
            }
        } else {
            reply += " -2147483648";
        }
        log.info("Elev_Arr: " + reply);

        // Send
        GreetingServer.SendToServer(reply);
    }

} // ThreadA

