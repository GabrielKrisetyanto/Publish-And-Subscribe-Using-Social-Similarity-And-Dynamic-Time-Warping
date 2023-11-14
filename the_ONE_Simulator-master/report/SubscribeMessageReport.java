/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 *
 * @author User
 */
public class SubscribeMessageReport extends Report implements MessageListener {

    public static String HEADER = "# time  ID  fromHost  toHost";

    /**
     * Constructor.
     */
    public SubscribeMessageReport() {
        init();
    }
    
    @Override
    public void init() {
        super.init();
        write(HEADER);
    }
    
    public void newMessage(Message m) {
        if (isWarmup()) {
            return;
        }
        
        int ttl = m.getTtl();
//		write(format(getSimTime()) + " " + m.getId() + " " + 
//				m.getSize() + " " + m.getFrom() + " " + m.getTo() + " " +
//				(ttl != Integer.MAX_VALUE ? ttl : "n/a") +  
//				(m.isResponse() ? " Y " : " N "));
        if (m.getId().contains("S")) {
            write(format(getSimTime()) + "\t" + m.getId() + "\t"
                + m.getFrom() + "\t" + m.getTo() + "\t");
        }
        
        
    }

    // nothing to implement for the rest
    public void messageTransferred(Message m, DTNHost f, DTNHost t, boolean b) {}
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}
    
    @Override
    public void done() {
        super.done();
    }
}
