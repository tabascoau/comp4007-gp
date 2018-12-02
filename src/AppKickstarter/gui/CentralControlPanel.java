package AppKickstarter.gui;

import AppKickstarter.AppKickstarter;

import javax.swing.*;

public class CentralControlPanel extends JFrame {
    private JLabel floorA;
    private JLabel floorB;
    private JLabel floorC;
    private JLabel floorD;
    private JLabel floorE;
    private JLabel floorF;
    private JLabel directionA;
    private JLabel directionB;
    private JLabel directionC;
    private JLabel directionD;
    private JLabel directionE;
    private JLabel directionF;
    private JButton startUpButton;
    private JButton shutDownButton;
    private JPanel panel;

    public CentralControlPanel() {
        add(panel);
        setTitle("Central Control Panel");
        setSize(600, 400);
        setVisible(true);
    }

    public void setFloorA(String id, int floor) {
        switch (id)
        {
            case "ElevatorA":
                this.floorA.setText("" + floor);
                break;
            case "ElevatorB":
                this.floorB.setText("" + floor);
                break;
            case "ElevatorC":
                this.floorC.setText("" + floor);
                break;
            case "ElevatorD":
                this.floorD.setText("" + floor);
                break;
            case "ElevatorE":
                this.floorE.setText("" + floor);
                break;
            case "ElevatorF":
                this.floorF.setText("" + floor);
                break;
        }
    }

    public void setDirectionA(String id, String direction) {
        switch (id)
        {
            case "ElevatorA":
                this.directionA.setText(direction);
                break;
            case "ElevatorB":
                this.directionB.setText(direction);
                break;
            case "ElevatorC":
                this.directionC.setText(direction);
                break;
            case "ElevatorD":
                this.directionD.setText(direction);
                break;
            case "ElevatorE":
                this.directionE.setText(direction);
                break;
            case "ElevatorF":
                this.directionF.setText(direction);
                break;
        }
    }
}
