package AppKickstarter.myThreads;

import AppKickstarter.misc.*;
import AppKickstarter.AppKickstarter;


//======================================================================
// ThreadB
public class ThreadB extends AppThread {
    //------------------------------------------------------------
    // ThreadB
    public ThreadB(String id, AppKickstarter appKickstarter) {
	super(id, appKickstarter);
    } // ThreadB


    //------------------------------------------------------------
    // run
    public void run() {
	log.info(id + ": starting...");

	for (boolean quit = false; !quit;) {
	    Msg msg = mbox.receive();

	    log.info(id + ": message received: [" + msg + "].");

	    switch (msg.getType()) {
		case Hello:
		    log.info(id + ": " + msg.getSender() + " is saying Hello to me!!!");

		    // send reply to msg sender
		    MBox mbox = msg.getSenderMBox();
		    mbox.send(new Msg(id, mbox, Msg.Type.HiHi, "HiHi, this is Thread B!"));
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
} // ThreadB
