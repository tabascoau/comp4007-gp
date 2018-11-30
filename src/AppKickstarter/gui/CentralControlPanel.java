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
    final int maxNumberOfPassenger = 10;
    final int defaultPassenger = 0;
    int totalProcessedPassenger = 0;

    private AppKickstarter appKickstarter = new AppKickstarter("AppKickstarter", "etc/MyApp.cfg");
    private JButton startElevatorButton, stopElevatorButton;
    private JPanel rootPanel;
    public JLabel aDirection, bDirection, cDirection, dDirection, eDirection, fDirection;
    public JLabel aPassenger, bPassenger, cPassenger, dPassenger, ePassenger, fPassenger;
    public JLabel aCurrent, bCurrent, cCurrent, dCurrent, eCurrent, fCurrent;
    private JLabel processedPassenger;

    public boolean[] liftAvailable = new boolean[6];
    public int[] liftTotalPassenger = new int[6];
    public int[] currentFloor = new int[6];
    public String[] currentDirection = new String[6];

    public JLabel[] currentDirectionJLabel;
    public JLabel[] currentFloorJLabel;
    public JLabel[] currentNoOfPassengerJLabel;

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
        currentNoOfPassengerJLabel = new JLabel[]{aPassenger, bPassenger, cPassenger, dPassenger, ePassenger, fPassenger};
        CentralControlPanel.instance = this;

        //Initial GUI JLabel text
        for (int i = 0; i < 6; i++) {
            liftAvailable[i] = true;
            liftTotalPassenger[i] = 0;
            currentFloor[i] = 0;
            currentDirection[i] = "S";
            currentFloorJLabel[i].setText(String.valueOf(defaultFloor));
            currentNoOfPassengerJLabel[i].setText(String.valueOf(defaultPassenger));
            currentDirectionJLabel[i].setText(String.valueOf(defaultDirection));
        }

        processedPassenger.setText(String.valueOf(totalProcessedPassenger));
        add(rootPanel);
        setTitle("Central Control Panel");
        setSize(800, 600);
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

    public void setCurrentPassenger(int index, int number) { //set how many passenger in the elevator
        liftTotalPassenger[index] += number;
        currentNoOfPassengerJLabel[index].setText(String.valueOf(number));
        this.repaint();
    }

    public boolean getAvailableElevator(int index) {
        return liftAvailable[index];
    }   //get available elevator

    public void addTotalPassenger(int number) { //how many passenger processed
        totalProcessedPassenger += number;
        processedPassenger.setText(String.valueOf(totalProcessedPassenger));
        this.repaint();
    }

    public boolean isElevatorAvailable(int index) {
        return elevatorArray[index].IsEmpty();  //return which elevator is empty
    }

    //handle the request in queue
    public static void handlerQueue(){
        //handle the msg queue
        System.out.println("T:" + requestQueue.size());

        synchronized (requestQueue) {
            // Find shortest path
            String[] data = requestQueue.peek().split(" ");

//            int src = Integer.parseInt(data[2]);
//            int dest = Integer.parseInt(data[3]);

            for (int i = 0; i < 6; i++) {

                if (CentralControlPanel.getInstance().liftAvailable[i]) {
                    elevatorArray[i].getMBox().send(new Msg("Timer", elevatorArray[i].getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
                    requestQueue.poll();
                    CentralControlPanel.getInstance().liftAvailable[i] = false;
                    //When the queue is handled queue size is 0
                    break;
                } else {

                }
            }



        }
    }

    //Thread to handle queue
    class QueueHandler extends Thread {
        @Override
        public void run() {
            while (true) {
                System.out.print("");
                if (requestQueue.size()==1) {
                    CentralControlPanel.handlerQueue();
                }
            }
        }
    }
}