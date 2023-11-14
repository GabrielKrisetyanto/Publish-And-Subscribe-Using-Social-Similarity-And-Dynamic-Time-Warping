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
public class ContactHistory implements RoutingDecisionEngine, BantuReport {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    //constructor
    public ContactHistory(Settings s) {
    }

    public ContactHistory(ContactHistory proto) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();

    }

    //mulai menghitung waktu start bertemu nya this node dengan peer
    //begitu juga peer dengan this node
    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost my = con.getOtherNode(peer);
        ContactHistory otherN = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        otherN.startTimestamps.put(my, SimClock.getTime());
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    //menyimpan historynya ke list Duration ketika node selesai bertemu
    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time;

        if (startTimestamps.get(peer) == null) {
            time = 0;
        } else {
            time = startTimestamps.get(peer);
        }
        double etime = SimClock.getTime();

        // cari dan buat kontak historynya
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {

            history = new LinkedList<Duration>();
            connHistory.put(peer, history);

        } else {
            history = connHistory.get(peer);
        }

        //tambahkan koneksi ke list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }

        startTimestamps.remove(peer);
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
            return true;  //jika bener pesan ketemu tujuan maka kirimkan
        }

        //nodeTujuan atau dest
        DTNHost des = m.getTo();
        ContactHistory cd = getOtherDecisionEngine(otherHost);
        //hitung kontak durasi peer - dest, this - dest 
//        double peer = cd.getAvgCD(des);
//        double ku = this.getAvgCD(des);
        double peer = cd.getConHis(des);
        double ku = this.getConHis(des);
//        double peer = cd.getInter(des);
//        double ku = this.getInter(des);
        return ku < peer; //kirim jika peer lebih baik
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
        return new ContactHistory(this);
    }

    private ContactHistory getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (ContactHistory) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    //ambil value listnya
    private List<Duration> getList(DTNHost h) {
        List<Duration> listNya;
        if (connHistory.containsKey(h)) {
            listNya = connHistory.get(h);
        } else {
            listNya = new LinkedList<>();
        }
        return listNya;
    }

    //hitung rata2 contact duration
    private double getAvgCD(DTNHost h) {
        List<Duration> listNya = getList(h);
        double total = 0;
        for (Iterator<Duration> iterator = listNya.iterator(); iterator.hasNext();) {
            Duration d = iterator.next();
            total = total + (d.end - d.start);
        }

        return total / listNya.size();
    }

//kalo gabung
    private double getConHis(DTNHost host) {
        double total = 0;
        double jumlah = 0;
        List<Duration> coba = new LinkedList<>();
        if (connHistory.containsKey(host)) {
            coba = connHistory.get(host);
        } else {
            coba = new LinkedList<>();
        }
        for (Iterator<Duration> iterator = coba.iterator(); iterator.hasNext();) {
            Duration next = iterator.next();
            total = total + (next.end - next.start);
        }

        jumlah = coba.size();
        return total / jumlah;
    }

    @Override
    public double getRerata() {
        List<Duration> coba = new LinkedList<>();
        double aku = 0;
        for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet()) {
            DTNHost key = entry.getKey();
//            coba = entry.getValue();
            aku = aku + this.getConHis(key);
        }
        System.out.println("HASIL : " + aku / connHistory.size());
        return aku / connHistory.size();
    }

    private double getInter(DTNHost h) {
        double total = 0;
        double jumlah = 0;
        List<Duration> coba = new LinkedList<>();
        if (connHistory.containsKey(h)) {
            coba = connHistory.get(h);
        }
        jumlah = coba.size();
        if (jumlah == 1) {
            return 0;
        }
        Double end = new Double(0);
        for (Iterator<Duration> iterator = coba.iterator(); iterator.hasNext();) {
            Duration next = iterator.next();
            if (end == 0) {
                end = next.start;
            }
            total = total + (next.start - end);
            end = next.end;

        }

        return total / (jumlah - 1);
    }

    @Override
    public double getRerataInter() {
        List<Duration> coba = new LinkedList<>();
        double aku = 0;
        for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet()) {
            DTNHost key = entry.getKey();
//            coba = entry.getValue();
            aku = aku + this.getInter(key);
        }
        System.out.println("HASIL : " + aku / connHistory.size());
        return aku / connHistory.size();
    }
}
