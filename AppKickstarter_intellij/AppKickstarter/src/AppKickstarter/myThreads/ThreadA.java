package AppKickstarter.myThreads;

import AppKickstarter.misc.*;
import AppKickstarter.AppKickstarter;
import AppKickstarter.timer.Timer;


//======================================================================
// ThreadA
public class ThreadA extends AppThread {
    private final int sleepTime = 5;

    //------------------------------------------------------------
    // ThreadA
    public ThreadA(String id, AppKickstarter appKickstarter) {
	super(id, appKickstarter);
    } // ThreadA


    //------------------------------------------------------------
    // run
    public void run() {
	log.info(id + ": starting...");
	Timer.setSimulationTimer(id, mbox,sleepTime);
	int mCnt = 0;

	for (boolean quit = false; !quit;) {
	    Msg msg = mbox.receive();

	    log.info(id + ": message received: [" + msg + "].");

	    switch (msg.getType()) {
		case TimesUp:
		    log.info(id + ": receiving timesup at " + appKickstarter.getSimulationTimeStr());
		    log.info(id + ": say hello to Thread B...");

		    // time to say hello to Thread B
		    AppThread thdB = appKickstarter.getThread("ThreadB");
		    MBox thdBMBox = thdB.getMBox();
		    thdBMBox.send(new Msg(id, mbox, Msg.Type.Hello, "Hello, this is Thread A!  (mCnt: " + ++mCnt + ")"));

		    // sleep again
		    Timer.setSimulationTimer(id, mbox, sleepTime);
		    break;

		case HiHi:
		    log.info(id + ": " + msg.getSender() + " is saying HiHi to me!!!");
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
} // ThreadA

