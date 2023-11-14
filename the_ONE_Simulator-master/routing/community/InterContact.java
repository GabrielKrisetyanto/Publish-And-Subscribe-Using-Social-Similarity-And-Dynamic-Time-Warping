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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author User
 */
public class InterContact implements RoutingDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    public InterContact(Settings s) {
    }

    public InterContact(InterContact ic) {
        startTimestamps = new HashMap<>();
        connHistory = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        InterContact otherN = this.getOtherDecisionEngine(peer);
        this.startTimestamps.put(peer, SimClock.getTime());
        otherN.startTimestamps.put(thisHost, SimClock.getTime());
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        double waktuTerakhir;
        if (startTimestamps.get(peer) == null) {
            waktuTerakhir = 0;
        } else {
            waktuTerakhir = startTimestamps.get(peer);
        }
        double waktuSekarang = SimClock.getTime();

        //buat list untuk nyimpen duration
        List<Duration> history;

        //cek apakah this node di tabel map nya punya history dengan peer yang ditemui
        if (connHistory.containsKey(peer)) {
            history = connHistory.get(peer); //ambil value historynya
        } else { //kalo ga ada buat list baru dan simpan key dan valuenya ke map
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        }

        //tambahkan waktu koneksi ke list history
        if (waktuSekarang - waktuTerakhir > 0) {
            history.add(new Duration(waktuTerakhir, waktuSekarang));
        }
//        connHistory.put(peer, history);
        this.startTimestamps.remove(peer);

    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost; // Unicast Routing
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true; //jika pesan ketemu peer yang bener tujuannya maka kirimkan
        }
        
        //node Dest
        DTNHost to = m.getTo();
        InterContact ic = getOtherDecisionEngine(otherHost);
        //hitung inter contactnya antara thisnode - to dan peer - to
        double ku = this.getAvgInter(to);
        double peer = ic.getAvgInter(to);
        if (ku > peer) {  //karena jika intercontact thisnode lebih besar artinya dia lebih jarang ketemu dest
            return true;  //jadi dititipkan ke peer yang lebih sering ketemu
        }else{
            return false;
        }
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
        return new InterContact(this);
    }

    private InterContact getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (InterContact) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    //hitung rata2 interContact dengan nodeDest
    private double getAvgInter(DTNHost ho) {
        List<Duration> isi = new LinkedList<>();
        double total = 0;
        //cek dulu apakah tabel mapnya ada history dengan dest
        if (connHistory.containsKey(ho)) {
            isi = connHistory.get(ho);
        } else {
            isi = new LinkedList<>();
        }
        for (Iterator<Duration> iterator = isi.iterator(); iterator.hasNext();) {
            Duration du = iterator.next();
            total = total + (du.end - du.start);
        }
        
        //coba pake while
//        Iterator<Duration> it = isi.iterator();
//        while(it.hasNext()){
//            Duration du = it.next();
//            total = total + (du.end - du.start);
//        }
        return total / isi.size();
    }
}
