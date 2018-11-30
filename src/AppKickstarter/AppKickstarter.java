package AppKickstarter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import AppKickstarter.gui.CentralControlPanel;
import AppKickstarter.myThreads.Elevator;
import AppKickstarter.timer.Timer;
import AppKickstarter.misc.*;

import javax.swing.*;


//======================================================================
// AppKickstarter
public class AppKickstarter {
    private String cfgFName = null;
    private Properties cfgProps = null;
    private Hashtable<String, AppThread> appThreads = null;
    private String id = null;
    private Logger log = null;
    private ConsoleHandler logConHd = null;
    private FileHandler logFileHd = null;
    private Timer timer = null;
    private Elevator elevatorA, elevatorB, elevatorC, elevatorD, elevatorE, elevatorF;//, elevatorF;
    CentralControlPanel centralControlPanel;
    public static Queue<String> requestQueue = new LinkedList<String>();
    private boolean[] elevatorBusy = new boolean[6];

    //------------------------------------------------------------
    // main
    public static void main(String[] args) {
//  / ___| | | |_ _| |  _ \ / \  |  _ \_   _|
// | |  _| | | || |  | |_) / _ \ | |_) || |
// | |_| | |_| || |  |  __/ ___ \|  _ < | |
//  \____|\___/|___| |_| /_/   \_\_| \_\|_|
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CentralControlPanel();
            }
        });
    } // main


    //------------------------------------------------------------
    // AppKickstarter
    public AppKickstarter(String id) {
        this(id, "etc/MyApp.cfg");
    } // AppKickstarter

    //------------------------------------------------------------
    // AppKickstarter
    public AppKickstarter(String id, String cfgFName) {
        this(id, cfgFName, false);
    } // AppKickstarter

    //------------------------------------------------------------
    // AppKickstarter
    public AppKickstarter(String id, String cfgFName, boolean append) {

        this.id = id;
        this.cfgFName = cfgFName;
        logConHd = null;
        logFileHd = null;
        id = getClass().getName();

        // set my thread name
        Thread.currentThread().setName(this.id);


        // read system config from property file
        try {
            cfgProps = new Properties();
            FileInputStream in = new FileInputStream(cfgFName);
            cfgProps.load(in);
            in.close();
            logConHd = new ConsoleHandler();
            logConHd.setFormatter(new LogFormatter());
            logFileHd = new FileHandler("etc/" + id + ".log", append);
            logFileHd.setFormatter(new LogFormatter());
        } catch (FileNotFoundException e) {
            System.out.println("Failed to open config file (" + cfgFName + ").");
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("Error reading config file (" + cfgFName + ").");
            System.exit(-1);
        }

        // get and configure logger
        log = Logger.getLogger(id);
        log.addHandler(logConHd);
        log.addHandler(logFileHd);
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINER);
        logConHd.setLevel(Level.INFO);
        logFileHd.setLevel(Level.INFO);
        appThreads = new Hashtable<String, AppThread>();
    } // AppKickstarter


    //------------------------------------------------------------
    // startApp
    public void startApp() {
        centralControlPanel = CentralControlPanel.getInstance();
        // start our application
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Starting...");

        int port = 54321;
        Thread t = null;
        try {
            t = new GreetingServer(port, this);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // create threads
        timer = new Timer("timer", this);
        elevatorA = new Elevator("ElevatorA", this);
        elevatorB = new Elevator("ElevatorB", this);
        elevatorC = new Elevator("ElevatorC", this);
        elevatorD = new Elevator("ElevatorD", this);
        elevatorE = new Elevator("ElevatorE", this);
        //elevator F is for Joe use
        // elevatorF = new Elevator("ElevatorF", this);
        // start threads
        new Thread(timer).start();

        new Thread(elevatorA).start();
        new Thread(elevatorB).start();
        new Thread(elevatorC).start();
        new Thread(elevatorD).start();
        new Thread(elevatorE).start();
        //elevator F is for Joe use
        //new Thread(elevatorF).start();

        //system.add(
        centralControlPanel.setElevatorArray(new Elevator[]{elevatorA, elevatorB, elevatorC, elevatorD, elevatorE, elevatorF});

    } // startApp


    //------------------------------------------------------------
    // stopApp
    public void stopApp() {
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Stopping...");
        elevatorA.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
//	threadA1.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
//	threadA2.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
//	threadB.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        timer.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
    } // stopApp


    //------------------------------------------------------------
    // regThread
    public void regThread(AppThread appThread) {
        log.fine(id + ": registering " + appThread.getID());
        synchronized (appThreads) {
            appThreads.put(appThread.getID(), appThread);
        }
    } // regThread


    //------------------------------------------------------------
    // unregThread
    public void unregThread(AppThread appThread) {
        log.fine(id + ": unregistering " + appThread.getID());
        synchronized (appThreads) {
            appThreads.remove(appThread.getID());
        }
    } // unregThread


    //------------------------------------------------------------
    // getThread
    public AppThread getThread(String id) {
        synchronized (appThreads) {
            return appThreads.get(id);
        }
    } // getThread


    //------------------------------------------------------------
    // getLogger
    public Logger getLogger() {
        return log;
    } // getLogger


    //------------------------------------------------------------
    // getLogConHd
    public ConsoleHandler getLogConHd() {
        return logConHd;
    }
    // getLogConHd


    //------------------------------------------------------------
    // getLogFileHd
    public FileHandler getLogFileHd() {
        return logFileHd;
    } // getLogFileHd


    //------------------------------------------------------------
    // getProperty
    public String getProperty(String property) {
        String s = cfgProps.getProperty(property);

        if (s == null) {
            log.severe(id + ": getProperty(" + property + ") failed.  Check the config file (" + cfgFName + ")!");
        }
        return s;
    } // getProperty


    //------------------------------------------------------------
    // getSimulationTime (in seconds)
    public long getSimulationTime() {
        return timer.getSimulationTime();
    } // getSimulationTime


    //------------------------------------------------------------
    // getSimulationTimeStr
    public String getSimulationTimeStr() {
        long t = timer.getSimulationTime();
        int s = (int) t % 60;
        int m = (int) (t / 60) % 60;
        int h = (int) (t / 3600) % 60;

        return String.format("%02d:%02d:%02d", h, m, s);
    } // getSimulationTimeString
}

//public class queueHandler extends Thread{
//    @Override
//    public void run(){
//        while ()
//    }
//}

//    public void ActivateElevator(String str) {
//        System.out.println("Retrieve request: " + str);
//
//
//        // Enter current floor checking
//        if (GoToCurrentFloor(str)) {
//            System.out.println("Enter same floor");
//            return;
//        }
//
//        // Find shortest path
//        String[] data = str.split(" ");
//        int src = Integer.parseInt(data[2]);
//        int dest = Integer.parseInt(data[3]);
//
//        requestQueue.add(str);
//
//
//        if (centralControlPanel.getAFreeElevator()) {
//            elevatorA.getMBox().send(new Msg("Timer", elevatorA.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getBFreeElevator()) {
//            elevatorB.getMBox().send(new Msg("Timer", elevatorB.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getCFreeElevator()) {
//            elevatorC.getMBox().send(new Msg("Timer", elevatorC.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getDFreeElevator()) {
//            elevatorD.getMBox().send(new Msg("Timer", elevatorD.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getEFreeElevator()) {
//            elevatorE.getMBox().send(new Msg("Timer", elevatorE.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        }else{
//            handler.start();
//        }

//        if(!requestQueue.isEmpty()){
//
//        }


//    private boolean GoToCurrentFloor(String str) {
//        String[] datas = str.split(" ");
//        int src = Integer.parseInt(datas[2]);
//        int dest = Integer.parseInt(datas[3]);
//        return src == dest;
//    }
//
//class QueueHandlerThread extends Thread {
//    @Override
//    public void run() {
//        if (centralControlPanel.getAFreeElevator()) {
//            elevatorA.getMBox().send(new Msg("Timer", elevatorA.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getBFreeElevator()) {
//            elevatorB.getMBox().send(new Msg("Timer", elevatorB.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getCFreeElevator()) {
//            elevatorC.getMBox().send(new Msg("Timer", elevatorC.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getDFreeElevator()) {
//            elevatorD.getMBox().send(new Msg("Timer", elevatorD.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        } else if (centralControlPanel.getEFreeElevator()) {
//            elevatorE.getMBox().send(new Msg("Timer", elevatorE.getMBox(), Msg.Type.TimesUp, requestQueue.peek()));
//            requestQueue.poll();
//        }
//    }
//}
//
//} // AppKicksta