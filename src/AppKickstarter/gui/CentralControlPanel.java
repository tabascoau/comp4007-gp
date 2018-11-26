package AppKickstarter.gui;

import AppKickstarter.AppKickstarter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CentralControlPanel extends JFrame{
    private AppKickstarter appKickstarter = new AppKickstarter("AppKickstarter", "etc/MyApp.cfg");
    private JButton startElevatorButton;
    private JButton stopElevatorButton;
    private JPanel rootPanel;
    private boolean start=false;

    public CentralControlPanel(){
        //This uses the designer form
        add(rootPanel);
        setTitle("Central Control Panel");
        setSize(400,500);
        setVisible(true);


        startElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(start==true){
                    JOptionPane.showMessageDialog(rootPanel, "Don't troll the app please. Elevator already started! ");
                }
                else {
                    appKickstarter.startApp();
                    start = true;
                }
            }
        });

        stopElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int input=JOptionPane.showConfirmDialog(rootPanel, "Are you sure to stop? ");
                if(input==0){
                    appKickstarter.stopApp();
                    System.exit(0);
                }

            }
        });
    }

}
