
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.contentKu;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import report.LatencyReport;
import report.ReportHelper;
import report.UtilityandEfficiencyReport;
import report.UtilandEfiReport;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Gabriel Krisetyanto
 */
public class ContentSubs implements RoutingDecisionEngine {

    private Double[] subsRange;
    private static final String CATEGORYRANGE = "INTERVAL";
    public static final String DURATION = "duration";
    private static int waktu;
    private List<Double> subsR;
    private double waktuJadiSub;
    private Set<Message> tom;
    private Map<String, Message> tom2;
    private ReportHelper rh2 = new UtilandEfiReport();
    private ReportHelper rh;
    private ReportHelper rh3 = new LatencyReport();

    public ContentSubs(Settings s) {
        waktu = s.getInt(DURATION);
    }

    public ContentSubs(ContentSubs cs) {
        tom = new HashSet<>();
        tom2 = new HashMap<>();
        rh = new UtilityandEfficiencyReport();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        if (m.getId().contains("S")) {
            waktuJadiSub = m.getCreationTime();
            subsRange = (Double[]) m.getProperty(CATEGORYRANGE);
            cek(m.getFrom(), m);
        }
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        if (subsRange != null) {
            double start = subsRange[0];
            double end = subsRange[1];
            double id = Double.parseDouble(m.getId().substring(1));
            if (id >= start && id <= end) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        tom.add(m);
        tom2.put(m.getId(), m);
        return true;
//        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        //pesan request subs tidak dikirim
        ContentSubs cs = getOtherDecisionEngine(otherHost);
//        if (m.getId().contains("S") || otherHost.getRouter().hasMessage(m.getId())) {
        if (m.getId().contains("S") || cs.tom2.containsKey(m.getId())) {
            return false;
        } else { // dikirim lewat subsList
            return true;
        }
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    private ContentSubs getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (ContentSubs) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new ContentSubs(this);
    }

    @Override
    public void update(DTNHost thisHost) {
        if (SimClock.getTime() > (waktuJadiSub + waktu)) {
            if (subsRange != null) {
                subsRange[0] = 0.0;
                subsRange[1] = 0.0;
            }
        }
    }

    public void cek(DTNHost hos, Message m) {
        double min = Double.parseDouble(m.getId().substring(1));
        double max = min + 19;
        for (Map.Entry<String, Message> entry : tom2.entrySet()) {
            String key = entry.getKey();
            Message msg = entry.getValue();
            double idM = Double.parseDouble(msg.getId().substring(1));
            if (idM >= min && idM <= max) {
                double cBuat = msg.getCreationTime();
                double cTTL = cBuat + waktu;
                double sBuat = m.getCreationTime();
                double sTTL = sBuat + waktu;
                if ((cBuat <= sTTL) && (sBuat <= cTTL)) {
//                    hos.getRouter().setDelivered(msg);
//                    System.out.println("hos + " + hos + " suka " + m.getId() + " psn nya msg " + msg);
                    rh.haveReceivedMessages(hos.getAddress(), msg);
                    rh2.haveReceivedMessages(hos.getAddress(), msg);
                    rh3.haveReceivedMessages(hos.getAddress(), msg);
                }
            }

        }
    }
}
