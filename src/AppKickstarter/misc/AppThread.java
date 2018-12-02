package AppKickstarter.misc;

import AppKickstarter.AppKickstarter;
import java.util.logging.Logger;


//======================================================================
// AppThread
public abstract class AppThread implements Runnable {
    protected String id;
    protected AppKickstarter appKickstarter;
    protected MBox mbox = null;
    protected Logger log = null;

    //------------------------------------------------------------
    // AppThread
    public AppThread(String id, AppKickstarter appKickstarter) {
	this.id = id;
	this.appKickstarter = appKickstarter;
	log = appKickstarter.getLogger();
	mbox = new MBox(id, log);
	appKickstarter.regThread(this);
	log.fine(id + ": created!");
    } // AppThread


    //------------------------------------------------------------
    // getters
    public MBox getMBox() { return mbox; }
    public String getID() { return id; }
} // AppThread
