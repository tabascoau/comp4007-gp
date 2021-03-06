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
    private Elevator elevatorA, elevatorB, elevatorC, elevatorD, elevatorE, elevatorF;
    private Elevator[] elevatorArray;
    private String[] elevatorIDArray;
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
        elevatorArray = new Elevator[]{elevatorA, elevatorB, elevatorC, elevatorD, elevatorE, elevatorF};
        elevatorIDArray=new String[]{"A","B","C","D","E","F"};
        centralControlPanel = CentralControlPanel.getInstance();
        // start our application
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Starting...");

        int port = 54321;   //Port is hardcoded
        Thread connectionThread = null;
        try {
            connectionThread = new GreetingServer(port, this);
            connectionThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // create threads
        timer = new Timer("timer", this);
        new Thread(timer).start();

        //Initialize elevator array thread
        for(int i=0;i<elevatorArray.length;i++){
            elevatorArray[i]=new Elevator(elevatorIDArray[i],this);
            new Thread(elevatorArray[i]).start();
        }
        centralControlPanel.setElevatorArray(elevatorArray);

    } // startApp


    //------------------------------------------------------------
    // stopApp
    public void stopApp() {
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Stopping...");
        elevatorA.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
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

