package AppKickstarter.gui;

import AppKickstarter.AppKickstarter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CentralControlPanel extends JFrame{
    public static CentralControlPanel instance;

    public static CentralControlPanel getInstance() {
        return CentralControlPanel.instance == null ? CentralControlPanel.instance = new CentralControlPanel() : instance;
    }
    final int defaultFloor=0;
    final char defaultDir='S';
    private AppKickstarter appKickstarter = new AppKickstarter("AppKickstarter", "etc/MyApp.cfg");
    private JButton startElevatorButton;
    private JButton stopElevatorButton;
    private JPanel rootPanel;




    public JLabel aCurrent, bCurrent, cCurrent, dCurrent, eCurrent, fCurrent;
    public JLabel aDirection, bDirection, cDirection, dDirection, eDirection, fDirection;



    private boolean start = false;


    public CentralControlPanel() {
        CentralControlPanel.instance = this;

        //This uses the designer form
        add(rootPanel);
        setTitle("Central Control Panel");
        setSize(800, 600);
        aCurrent.setText(String.valueOf(defaultFloor));
        bCurrent.setText(String.valueOf(defaultFloor));
        cCurrent.setText(String.valueOf(defaultFloor));
        dCurrent.setText(String.valueOf(defaultFloor));
        eCurrent.setText(String.valueOf(defaultFloor));
        fCurrent.setText(String.valueOf(defaultFloor));

        aDirection.setText(String.valueOf(defaultDir));
        bDirection.setText(String.valueOf(defaultDir));
        cDirection.setText(String.valueOf(defaultDir));
        dDirection.setText(String.valueOf(defaultDir));
        eDirection.setText(String.valueOf(defaultDir));
        fDirection.setText(String.valueOf(defaultDir));
        setVisible(true);


        startElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (start == true) {
                    JOptionPane.showMessageDialog(rootPanel, "Elevator already started! ");
                } else {
                    appKickstarter.startApp();
                    start = true;
                }
            }
        });



        stopElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (start == true) {
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


    }

    public void setaCurrentFloor(int aCurrentFloor){
        aCurrent.setText(String.valueOf(aCurrentFloor));
        this.repaint();
    }

    public void setaCurrentFloor(int aCurrentFloor, String status){
        aCurrent.setText(String.valueOf(aCurrentFloor)+" "+status);
        this.repaint();
    }

    public void setaDirection(char direction){
        aDirection.setText(String.valueOf(direction));
    }

    public void setbCurrentFloor(int bCurrentFloor){
        bCurrent.setText(String.valueOf(bCurrentFloor));
        this.repaint();
    }

    public void setbCurrentFloor(int bCurrentFloor, String status){
        bCurrent.setText(String.valueOf(bCurrentFloor)+" "+status);
        this.repaint();
    }

    public void setbDirection(char direction){
        bDirection.setText(String.valueOf(direction));
    }

    public void setcCurrentFloor(int cCurrentFloor){
        cCurrent.setText(String.valueOf(cCurrentFloor));
        this.repaint();
    }

    public void setcCurrentFloor(int cCurrentFloor, String status){
        cCurrent.setText(String.valueOf(cCurrentFloor)+" "+status);
        this.repaint();
    }


    public void setcDirection(char direction){
        cDirection.setText(String.valueOf(direction));
    }

    public void setdCurrentFloor(int dCurrentFloor){
        dCurrent.setText(String.valueOf(dCurrentFloor));
        this.repaint();
    }

    public void setdCurrentFloor(int dCurrentFloor, String status){
        dCurrent.setText(String.valueOf(dCurrentFloor)+" "+status);
        this.repaint();
    }

    public void setdDirection(char direction){
        dDirection.setText(String.valueOf(direction));
    }

    public void seteCurrentFloor(int eCurrentFloor){
        eCurrent.setText(String.valueOf(eCurrentFloor));
        this.repaint();
    }

    public void seteCurrentFloor(int eCurrentFloor, String status){
        eCurrent.setText(String.valueOf(eCurrentFloor)+" "+status);
        this.repaint();
    }

    public void seteDirection(char direction){
        eDirection.setText(String.valueOf(direction));
    }

    public void setfCurrentFloor(int fCurrentFloor){
        fCurrent.setText(String.valueOf(fCurrentFloor));
        this.repaint();
    }

    public void setfCurrentFloor(int fCurrentFloor, String status){
        fCurrent.setText(String.valueOf(fCurrentFloor)+" "+status);
        this.repaint();
    }

    public void setfDirection(char direction){
        fDirection.setText(String.valueOf(direction));
    }




}
