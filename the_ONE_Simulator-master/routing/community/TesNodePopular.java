/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author User
 */
public class TesNodePopular implements RoutingDecisionEngine, CommunityDetectionEngine, CentralityKu {

    /** Community Detection Algorithm to employ -setting id {@value} */
    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    /** Centrality Computation Algorithm to employ -setting id {@value} */
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
        
    private int interval = 3600;
//    protected int interval= 21600; // 6 hours
    private int lastRecord = 0;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, Double> startTimestamps;
    	
    protected CommunityDetection community;
    protected CentralityUas centrality;

    public TesNodePopular(Settings s) {
        if(s.contains(COMMUNITY_ALG_SETTING))
			this.community = (CommunityDetection) 
				s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
		else
			this.community = new SimpleCommunityDetection(s);
		
		if(s.contains(CENTRALITY_ALG_SETTING))
			this.centrality = (CentralityUas) 
				s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
//		else
//			this.centrality = new SWindowCentrality(s);
    }

    public TesNodePopular(TesNodePopular tnp) {
        this.community = tnp.community.replicate();
//        this.centrality = (CentralityUas) tnp.centrality.replicate();
	this.centrality = tnp.centrality.replicate();
        connHistory = new HashMap<>();
        startTimestamps = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time;
        if (startTimestamps.get(peer) == null) {
            time = 0;
        } else {
            time = startTimestamps.get(peer);
        }

        int etime = SimClock.getIntTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }
        lastRecord = SimClock.getIntTime();
        startTimestamps.remove(peer);

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        TesNodePopular de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());
        
        this.community.newConnection(myHost, peer, de.community);

    }

    @Override
    public boolean newMessage(Message m) {
        return false;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return false;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new TesNodePopular(this);
    }

    private TesNodePopular getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (TesNodePopular) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    @Override
    public int[] sentral() {
        return this.centrality.getArrayGlobalCentrality(connHistory);
    }

  
}
