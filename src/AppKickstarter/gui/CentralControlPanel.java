package AppKickstarter.gui;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.GreetingServer;
import AppKickstarter.misc.Msg;
import AppKickstarter.myThreads.Elevator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class CentralControlPanel extends JFrame {
    public static CentralControlPanel instance;

    public static CentralControlPanel getInstance() {
        return CentralControlPanel.instance == null ? CentralControlPanel.instance = new CentralControlPanel() : instance;
    }

    final int defaultFloor = 0;
    final char defaultDirection = 'S';
    public final int totalNumberOfElevator = 6;
    final int screenWidth = 800;
    final int screenHeight = 600;
    int totalProcessedPassenger = 0;
    private boolean[] isLiftInitial = new boolean[totalNumberOfElevator];

    private AppKickstarter appKickstarter = new AppKickstarter("AppKickstarter", "etc/MyApp.cfg");
    private JButton startElevatorButton, stopElevatorButton;
    private JPanel rootPanel;
    public JLabel aDirection, bDirection, cDirection, dDirection, eDirection, fDirection;
    public JLabel aCurrent, bCurrent, cCurrent, dCurrent, eCurrent, fCurrent;
    private JLabel processedPassenger;

    public int[] liftTotalPassenger = new int[totalNumberOfElevator];
    public int[] currentFloor = new int[totalNumberOfElevator];
    public String[] currentDirection = new String[totalNumberOfElevator];

    public JLabel[] currentDirectionJLabel;
    public JLabel[] currentFloorJLabel;
    public JLabel[] currentNoOfPassengerJLabel;
    public static CentralControlPanel panel;

    private boolean start = false;

    private static Elevator[] elevatorArray = {};

    public void setElevatorArray(Elevator[] elevatorArray) {
        this.elevatorArray = elevatorArray;
    }

    public static Queue<String> requestQueue = new LinkedList<>();

    public CentralControlPanel() {
        //GUI
        currentDirectionJLabel = new JLabel[]{aDirection, bDirection, cDirection, dDirection, eDirection, fDirection};
        currentFloorJLabel = new JLabel[]{aCurrent, bCurrent, cCurrent, dCurrent, eCurrent, fCurrent};
        panel=CentralControlPanel.instance = this;


        //Initial GUI JLabel text
        for (int i = 0; i < totalNumberOfElevator; i++) {
            currentFloor[i] = 0;
            currentDirection[i] = "S";
            currentFloorJLabel[i].setText(String.valueOf(defaultFloor));
            currentDirectionJLabel[i].setText(String.valueOf(defaultDirection));
            isLiftInitial[i] = true;
        }

        processedPassenger.setText(String.valueOf(totalProcessedPassenger));
        add(rootPanel);
        setTitle("Central Control Panel");
        setSize(screenWidth, screenHeight);
        setVisible(true);

        //Check whether the elevator started when user click start button
        startElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (start == true) {    //When elevator already started, show alert.
                    JOptionPane.showMessageDialog(rootPanel, "Elevator already started! ");
                } else {
                    appKickstarter.startApp();  //else start app
                    start = true;
                }
            }
        });

        //Stop the elevator when user click stop button
        stopElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (start == true) {    //prompt the user whether stop the elevator
                    int input = JOptionPane.showConfirmDialog(rootPanel, "Are you sure to stop? ");
                    if (input == 0) {
                        appKickstarter.stopApp();
                        System.exit(0);
                    }
                } else {
                    JOptionPane.showMessageDialog(rootPanel, "Elevator have not start yet!");
                }
            }
        });
        //END GUI

        Thread queueThread = new QueueHandler();
        queueThread.start();
    }

    public void setCurrentFloor(int index, int currentFloor) {   //set the elevator current floor
        this.currentFloor[index] = currentFloor;
        this.currentFloorJLabel[index].setText(String.valueOf(currentFloor));
        this.repaint();
    }

    public void setCurrentDirection(int index, String direction) { //set the elevator current direction
        this.currentDirection[index] = direction;
        this.currentDirectionJLabel[index].setText(String.valueOf(direction));
        this.repaint();
    }

    public void addTotalPassenger(int number) { //how many passenger processed
        totalProcessedPassenger += number;
        processedPassenger.setText(String.valueOf(totalProcessedPassenger));
        this.repaint();
    }

    //handle the request in queue
    public static void handlerQueue() {
        //handle the msg queue
        System.out.println("T:" + requestQueue.size());

        // Find shortest path
        String[] data = requestQueue.peek().split(" ");
        int src = Integer.parseInt(data[2]);
        int dest = Integer.parseInt(data[3]);
        String direction;
        String passengerID = data[1];
        if (src < dest) {
            direction = "U";
        } else {
            direction = "D";
        }
        System.out.println("Src: " + src);
        System.out.println("currentFloor[i]: " + panel.currentFloor[0]);

        //default 6 lift available, 6 elevators are ready
        for (int i = 0; i < panel.totalNumberOfElevator; i++) {
            if (panel.getInstance().isLiftInitial[i]) {
                liftOrderAssignment(i, passengerID, src, dest);
                break;
            }
        }

        //The queue have something now, we need algorithm
        if (requestQueue.size() > 0) {
            for (int i = 0; i < panel.getInstance().totalNumberOfElevator; i++) {
                if (panel.getInstance().currentDirection[i] == direction) {
                    elevatorArray[i].getMBox().send(new Msg(elevatorArray[i].getID(), elevatorArray[i].getMBox(), Msg.Type.GoToSrc, requestQueue.peek()));
                    requestQueue.poll();
                    break;
                }
            }
        }

        System.out.println("REQUEST QUEUE: " + requestQueue.size());
    }

    public static void liftOrderAssignment(int index, String passengerID, int src, int dest) {
        elevatorArray[index].getMBox().send(new Msg(elevatorArray[index].getID(), elevatorArray[index].getMBox(), Msg.Type.ReceiveOrder, requestQueue.peek()));
        elevatorArray[index].addToElevatorQueue(index, requestQueue.peek());
        requestQueue.poll();
        panel.getInstance().isLiftInitial[index] = false;
        String msg = "Svc_Reply " + passengerID + " " + src + " " + dest + " " + elevatorArray[index].getID();
        System.out.println(msg);
        GreetingServer.sendMsgToClient(msg);
        //When the queue is handled queue size is 0
    }

    public static void liftOrderInsertion(int index, String passengerID, int src, int dest) {

    }


    //Thread to handle queue
    class QueueHandler extends Thread {
        @Override
        public void run() {
            while (true) {
                System.out.print("");
                if (!requestQueue.isEmpty()) {
                    panel.handlerQueue();
                }
            }
        }
    }
}

//if current floor=source, assign to it.
//            if (CentralControlPanel.getInstance().currentFloor[i] == src) {
//                elevatorArray[i].getMBox().send(new Msg(elevatorArray[i].getID(), elevatorArray[i].getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//                elevatorArray[i].addToElevatorQueue(i, requestQueue.peek());
//                String msg = "Svc_Reply " + passengerID + " " + src + " " + dest + " " + elevatorArray[i].getID();
//                System.out.println(msg);
//                GreetingServer.sendMsgToClient(msg);
//                requestQueue.poll();
//                break;
//            }else{
//                System.out.println("COME TO ELSE STATEMENT NOW!");
//                floorDifference[i]=Math.abs(CentralControlPanel.getInstance().currentFloor[i]-src);
//                int tmp=findSmallest(floorDifference);
//                elevatorArray[tmp].getMBox().send(new Msg(elevatorArray[tmp].getID(), elevatorArray[tmp].getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//                elevatorArray[tmp].addToElevatorQueue(i, requestQueue.peek());
//                String msg = "Svc_Reply " + passengerID + " " + src + " " + dest + " " + elevatorArray[tmp].getID();
//                System.out.println(msg);
//                GreetingServer.sendMsgToClient(msg);
//                requestQueue.poll();
//                break;
//            }


//else assign nearest elevator to it.
//            else if(number<Math.abs(CentralControlPanel.getInstance().currentFloor[i]-src)){
//
//            }
//            elevatorArray[i].getMBox().send(new Msg(elevatorArray[i].getID(), elevatorArray[i].getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            elevatorArray[i].addToElevatorQueue(i, requestQueue.peek());
//            String msg = "Svc_Reply " + passengerID + " " + src + " " + dest + " " + elevatorArray[i].getID();
//            System.out.println(msg);
//            GreetingServer.sendMsgToClient(msg);
//            requestQueue.poll();
//When the queue is handled queue size is 0


