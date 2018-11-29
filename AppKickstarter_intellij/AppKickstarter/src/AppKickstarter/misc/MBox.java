package AppKickstarter.misc;

import java.util.logging.Logger;
import java.util.ArrayList;


//======================================================================
// MBox
public class MBox {
    private String id;
    private Logger log;
    private ArrayList<Msg> mqueue = new ArrayList<Msg>();


    //------------------------------------------------------------
    // MBox
    public MBox(String id, Logger log) {
	this.id = id;
	this.log = log;
    } // MBox


    //------------------------------------------------------------
    // send
    public final synchronized void send(Msg msg) {
	mqueue.add(msg);
	log.finest(id + ": send \"" + msg + "\"");
	notify();
    } // send


    //------------------------------------------------------------
    // receive
    public final synchronized Msg receive() {
	while (mqueue.isEmpty()) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
	Msg msg = mqueue.remove(0);
	log.finest(id + ": receiving \"" + msg + "\"");
	return msg;
    } // receive


    //------------------------------------------------------------
    // getMsgCnt
    public int getMsgCnt() {
	return mqueue.size();
    } // getMsgCnt
} // MBox

