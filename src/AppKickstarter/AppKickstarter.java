package AppKickstarter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.Hashtable;

import AppKickstarter.myThreads.Elevator;
import AppKickstarter.timer.Timer;
import AppKickstarter.misc.*;
import AppKickstarter.myThreads.ThreadA;
import AppKickstarter.myThreads.ThreadB;


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
    private Elevator elevator1;
//    private ThreadA threadA1, threadA2;
//    private ThreadB threadB;


    //------------------------------------------------------------
    // main
    public static void main(String [] args) {
	AppKickstarter appKickstarter = new AppKickstarter("AppKickstarter", "etc/MyApp.cfg");
	appKickstarter.startApp();
	try {
	    Thread.sleep(1800 * 1000);
	} catch (Exception e) {}
	appKickstarter.stopApp();
    } // main


    //------------------------------------------------------------
    // AppKickstarter
    private AppKickstarter(String id) {
	this(id, "etc/MyApp.cfg");
    } // AppKickstarter


    //------------------------------------------------------------
    // AppKickstarter
    private AppKickstarter(String id, String cfgFName) {
	this(id, cfgFName, false);
    } // AppKickstarter


    //------------------------------------------------------------
    // AppKickstarter
    private AppKickstarter(String id, String cfgFName, boolean append) {
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
	    System.out.println("Failed to open config file ("+cfgFName+").");
	    System.exit(-1);
	} catch (IOException e) {
	    System.out.println("Error reading config file ("+cfgFName+").");
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
    private void startApp() {
	// start our application
	log.info("");
	log.info("");
	log.info("============================================================");
	log.info(id + ": Application Starting...");

	int port=54321;
	try{
		Thread t=new GreetingServer(port, this);
		t.start();
	}catch (IOException e){
		e.printStackTrace();
	}


	// create threads
	timer = new Timer("timer", this);
	elevator1 = new Elevator("Elevator1", this);
//	threadA1 = new ThreadA("ThreadA1", this);
//	threadA2 = new ThreadA("ThreadA2", this);
//	threadB  = new ThreadB("ThreadB",  this);

	// start threads
	new Thread(timer).start();
	new Thread(elevator1).start();
//	new Thread(threadA1).start();
//	new Thread(threadA2).start();
//	new Thread(threadB).start();
    } // startApp


    //------------------------------------------------------------
    // stopApp
    private void stopApp() {
	log.info("");
	log.info("");
	log.info("============================================================");
	log.info(id + ": Application Stopping...");
	elevator1.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
//	threadA1.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
//	threadA2.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
//	threadB.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
	timer.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
    } // stopApp


    //------------------------------------------------------------
    // regThread
    public void regThread(AppThread appThread) {
	log.fine(id + ": registering " + appThread.getID());
	synchronized (appThreads) { appThreads.put(appThread.getID(), appThread); }
    } // regThread


    //------------------------------------------------------------
    // unregThread
    public void unregThread(AppThread appThread) {
	log.fine(id + ": unregistering " + appThread.getID());
	synchronized (appThreads) { appThreads.remove(appThread.getID()); }
    } // unregThread


    //------------------------------------------------------------
    // getThread
    public AppThread getThread(String id) {
	synchronized (appThreads) { return appThreads.get(id); }
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
	int m = (int) (t/60) % 60;
	int h = (int) (t/3600) % 60;

	return String.format("%02d:%02d:%02d", h, m, s);
    } // getSimulationTimeStr

	public void ActivateElevator(String str)
	{
		System.out.println("Retrieve request: " + str);

		// Enter current floor checking
		if (GoToCurrentFloor(str))
		{
			System.out.println("Enter same floor");
			return;
		}

		// Find shortest path

		// Assign to elevator
		elevator1.getMBox().send(new Msg("Timer", null, Msg.Type.TimesUp, str));
	}

	private boolean GoToCurrentFloor(String str)
	{
		String[] datas = str.split(" ");
		int	src = Integer.parseInt(datas[2]);
		int dest = Integer.parseInt(datas[3]);
		return src == dest;
	}

} // AppKickstarter
