package AppKickstarter.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CentralControlPanel extends JFrame{
    private JButton startElevatorButton;
    private JButton stopElevatorButton;
    private JPanel rootPanel;

    public CentralControlPanel(){
        //This uses the designer form
        add(rootPanel);
        setTitle("Central Control Panel");
        setSize(400,500);

        stopElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(rootPanel, "Are you sure to stop? ");
            }
        });
        startElevatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

}
