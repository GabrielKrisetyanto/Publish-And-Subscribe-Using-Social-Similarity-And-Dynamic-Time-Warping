/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.contentKu;

import core.*;
import java.util.*;
import ku.Helper;
import report.LatencyReport;
import report.ReportHelper;
import report.UtilandEfiReport;
import report.UtilityandEfficiencyReport;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
import routing.community.PeopleRankEngine;

/**
 *
 * @author Gabriel Krisetyanto
 */
public class ContentSubsDTWHaggle implements RoutingDecisionEngine {

    //menyimpan request pesan subscriber
    private Map<DTNHost, Message> subscriptionList;
    private Double[] subsRange;
    private static final String CATEGORYRANGE = "INTERVAL";
    private static final String DURATION = "duration";
    private static int waktu;
    private double waktuJadiSub;
    private Map<String, Message> tombstone;
    private ReportHelper rh;
    private ReportHelper rh2;
    private static final double WARMUP = 28800; //8jam warmup
    private static final double WARM = 21600; //6 jam
    private ReportHelper rh3;
    public double d = 0.85;
    public int threshold = 5;
    public double thisRank = 0;
    private Map<DTNHost, List<Tuple<Double, Integer>>> peopleRank;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    public ContentSubsDTWHaggle(Settings s) {
        waktu = s.getInt(DURATION);
    }

    public ContentSubsDTWHaggle(ContentSubsDTWHaggle csd) {

        subscriptionList = new HashMap<DTNHost, Message>();
        tombstone = new HashMap<>();
        rh = new UtilityandEfficiencyReport();
        rh2 = new LatencyReport();
        rh3 = new UtilandEfiReport();
        peopleRank = new HashMap<>();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        ContentSubsDTWHaggle pHost = getOtherDecisionEngine(peer); // Objek host yang sedang ditemui
        this.startTimestamps.put(peer, SimClock.getTime());
        pHost.startTimestamps.put(myHost, SimClock.getTime());

        if (SimClock.getTime() >= WARM && SimClock.getTime() <= WARMUP) {
            // Perhitungan peringkat dan jumlah peer di host saat ini
            double myRank = countRank(peopleRank); // Menghitung peringkat host
            int myPeer = countPeer(peopleRank); // Menghitung jumlah peer host

            // Pertukaran peopleRank
            if (this.peopleRank.keySet().contains(peer)) {
            for (Map.Entry<DTNHost, List<Tuple<Double, Integer>>> entry : pHost.peopleRank.entrySet()) {
                DTNHost key = entry.getKey();
                List<Tuple<Double, Integer>> value = entry.getValue();

                // Periksa apakah peer sudah menjadi tetangga di peopleRank
                if (peopleRank.containsKey(key)) {
                    // Jika sudah ada, catat node-node peringkat yang ditemui
                    List<Tuple<Double, Integer>> currentRankList = peopleRank.get(key);
                    for (Tuple<Double, Integer> newValue : value) {
                        boolean found = false;
                        for (Tuple<Double, Integer> existingValue : currentRankList) {
                            // Periksa apakah data sudah ada dalam daftar peringkat yang sudah ada
                            if (newValue.equals(existingValue)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // Jika data belum ada dalam daftar peringkat, tambahkan data baru
                            currentRankList.add(newValue);
                        }
                    }
                }
            }
        }

            // Memperbarui data peringkat dan jumlah peer di host
            List<Tuple<Double, Integer>> myRankList = new ArrayList<>();
            myRankList.add(new Tuple<>(myRank, myPeer));
            peopleRank.put(peer, myRankList);
            thisRank = myRank;
        }

        // Proses untuk pertukaran subscriptionList
        // Iterasi melalui entri subscriptionList dari decision engine lain untuk peer yang diberikan
        for (Map.Entry<DTNHost, Message> entry : getOtherDecisionEngine(peer).subscriptionList.entrySet()) {
            DTNHost key = entry.getKey(); // ambil kunci DTNHost
            Message value = entry.getValue(); // ambil nilai Message

            // Periksa apakah subscriptionList sudah mengandung kunci tersebut
            if (!subscriptionList.containsKey(key)) { // Jika belum
                subscriptionList.put(key, value);
            } else {
                // Jika sudah ada, periksa nilai untuk memperbarui pesan yang baru
                if (subscriptionList.get(key).getCreationTime() < value.getCreationTime()) {
                    subscriptionList.put(key, value);
                }
            }
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = startTimestamps.get(peer);
//		double time = cek(thisHost, peer);
        double etime = SimClock.getTime();
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        // add this connection to the list
        if (etime - time > 300) {
            history.add(new Duration(time, etime));
        }

        if (SimClock.getTime() >= WARM && SimClock.getTime() <= WARMUP) {
            ContentSubsDTWHaggle pHost = getOtherDecisionEngine(peer);
            if (!this.peopleRank.containsKey(peer)) { //kalo belum temenan
                if (this.countTime(peer) >= threshold) { //kalo udah kontak lebih dari threshold
                    // Hitung peringkat dan jumlah tetangga dari peer
                    double myRank = pHost.countRank(peopleRank);
                    int myPeerCount = pHost.countPeer(peopleRank);

                    // Tambahkan host sebagai tetangga dengan peringkat dan jumlah tetangga yang dihitung
                    Tuple<Double, Integer> f = new Tuple<>(myRank, myPeerCount);
                    List<Tuple<Double, Integer>> myRankList = pHost.peopleRank.get(peer);
                    if (myRankList != null) {
                        // Jika sudah ada daftar peringkat untuk host, tambahkan Tuple baru ke daftar tersebut
                        myRankList.add(f);
                    } else {
                        // Jika belum ada daftar peringkat untuk host, buat daftar baru dan tambahkan Tuple baru
                        myRankList = new ArrayList<>();
                        myRankList.add(f);
                        pHost.peopleRank.put(peer, myRankList);
                    }
                    thisRank = countRank(pHost.peopleRank);
                }
            }
        }
        startTimestamps.remove(peer);
    }

    @Override
    public boolean newMessage(Message m) {
        if (m.getId().contains("S")) {
            waktuJadiSub = m.getCreationTime();
            subsRange = (Double[]) m.getProperty(CATEGORYRANGE);
            if (!subscriptionList.containsKey(m.getFrom())) {
                subscriptionList.put(m.getFrom(), m);
            } else {
                // jika sudah ada cek velue nya  untuk perbaharui pesan yang baru
                if (subscriptionList.get(m.getFrom()).getCreationTime() < m.getCreationTime()) {
                    subscriptionList.put(m.getFrom(), m);
                }
            }
            cek(m.getFrom(), m);
        }
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        double id = Double.parseDouble(m.getId().substring(1));
        if (subsRange != null && id >= subsRange[0] && id <= subsRange[1]) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        tombstone.put(m.getId(), m);
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        ContentSubsDTWHaggle pHost = getOtherDecisionEngine(otherHost);
        if (m.getId().contains("S") || pHost.tombstone.containsKey(m.getId())) {
            return false;
        } else {
            double highestSimilarity = Double.MAX_VALUE;
            List<Double> rankListHost = new ArrayList<>();
            List<Double> rankListPeer = new ArrayList<>();

            double id = Double.parseDouble(m.getId().substring(1));
            for (Map.Entry<DTNHost, Message> entry : getOtherDecisionEngine(otherHost).subscriptionList.entrySet()) {
                DTNHost key = entry.getKey();
                Message value = entry.getValue();
                Double[] range = (Double[]) value.getProperty(CATEGORYRANGE);
                if (id >= range[0] && id <= range[1]) {
                    ContentSubsDTWHaggle dest = getOtherDecisionEngine(key);
                    List<Tuple<Double, Integer>> peerRankTuples = pHost.peopleRank.get(key);
                    if (peerRankTuples != null && !peerRankTuples.isEmpty()) {
                        List<Tuple<Double, Integer>> myRankTuples =  this.peopleRank.get(key);
                        if (myRankTuples != null && !myRankTuples.isEmpty()) {
                            for (Tuple<Double, Integer> rankTuple : peerRankTuples) {
                                double rankValue = rankTuple.getKey();
                                if (rankValue != 0) {
                                    rankListPeer.add(rankValue);
                                }
                            }
                            for (Tuple<Double, Integer> rankTuple : myRankTuples) {
                                double rankValue = rankTuple.getKey();
                                if (rankValue != 0) {
                                    rankListHost.add(rankValue);
                                }
                            }
                        } else {
                            return false; // Mengembalikan false jika rankListHost tidak memiliki nilai
                        }
                    } else {
                        return false; // Mengembalikan false jika rankListPeer tidak memiliki nilai
                    }
                }
            }
            // Memeriksa apakah rankListPeer dan rankListHost memiliki nilai yang valid
            if (rankListPeer.isEmpty() || rankListHost.isEmpty()) {
                return false;
            }

            // Menghitung kesamaan perilaku sosial berdasarkan peringkat
            double socialSimilarity = getDTW(rankListHost, rankListPeer);

            if (socialSimilarity < highestSimilarity) {
                return true;
            }

            return false;
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

    @Override
    public void update(DTNHost thisHost) {
        // Update subsRange
        if (SimClock.getTime() > (waktuJadiSub + waktu)) {
            if (subsRange != null) {
                subsRange[0] = 0.0;
                subsRange[1] = 0.0;
            }
        }

        // Proses update (buang) subsList yang telah kadaluarsa
        Iterator<DTNHost> it = subscriptionList.keySet().iterator();

        while (it.hasNext()) {
            DTNHost key = it.next();
            Message m = subscriptionList.get(key);
            double durasi = m.getCreationTime();
            if (durasi + waktu < SimClock.getIntTime()) {
                it.remove();
            }
        }
        // Proses update (buang) tombstone yang telah kadaluarsa
        Iterator<String> it2 = tombstone.keySet().iterator();

        while (it2.hasNext()) {
            String key = it2.next();
            Message m = tombstone.get(key);
            double durasi = m.getCreationTime();
            if (durasi + waktu < SimClock.getIntTime()) {
                it2.remove();
            }
        }
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new ContentSubsDTWHaggle(this);
    }

    private ContentSubsDTWHaggle getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (ContentSubsDTWHaggle) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    public void cek(DTNHost hos, Message m) {
        for (Map.Entry<String, Message> entry : tombstone.entrySet()) {
            String key = entry.getKey();
            Message msg = entry.getValue();
            double idM = Double.parseDouble(msg.getId().substring(1));
            double cBuat = msg.getCreationTime();
            double cTTL = cBuat + waktu;
            double sBuat = m.getCreationTime();
            double sTTL = sBuat + waktu;
            boolean a = idM >= subsRange[0] && idM <= subsRange[1];
            boolean b = (cBuat <= sTTL) && (sBuat <= cTTL);
            if (a && b) {
                rh.haveReceivedMessages(hos.getAddress(), msg);
                rh2.haveReceivedMessages(hos.getAddress(), msg);
                rh3.haveReceivedMessages(hos.getAddress(), msg);

            }
        }
    }

    public double countTime(DTNHost h) {
        if (this.connHistory.containsKey(h)) {
            double totalTime = 0.0;
            List<Duration> duration = new LinkedList<>(connHistory.get(h));
            Iterator<Duration> i = duration.iterator();

            Duration d = new Duration(0, 0);
            while (i.hasNext()) {
                d = i.next();
                double time = d.end - d.start;
                totalTime += time;
            }

            return totalTime;
        } else {
            return 0;
        }
    }

   protected double countRank(Map<DTNHost, List<Tuple<Double, Integer>>> peopleRank) {
    double totalRank = 0;  // Inisialisasi totalRank sebagai 0

    for (Map.Entry<DTNHost, List<Tuple<Double, Integer>>> entry : peopleRank.entrySet()) {
        // Mendapatkan daftar rankTuple
        List<Tuple<Double, Integer>> rankList = entry.getValue();
        for (Tuple<Double, Integer> rankTuple : rankList) {
            // Mendapatkan nilai skor (rank) dari rankTuple
            double rank = rankTuple.getKey();
            // Mendapatkan nilai jumlah (neighbor) dari rankTuple
            int neigh = rankTuple.getValue();
            // Memeriksa apakah neighbor tidak sama dengan 0
            if (neigh != 0) {
                // Menghitung nilai skor yang dinormalisasi
                double normalizedRank = rank / neigh;
                // Menambahkan nilai skor yang dinormalisasi ke totalRank
                totalRank += normalizedRank;
            }
        }
    }
    // Menghitung dan mengembalikan skor PeopleRank yang dihitung
    return (1 - d) + d * totalRank;
}

// Menghitung jumlah tetangga untuk peopleRank
    protected int countPeer(Map<DTNHost, List<Tuple<Double, Integer>>> peopleRank) {
        return peopleRank.size();
    }

    //perhitungan DTW
    public double getDTW(List<Double> rankListHost, List<Double> rankListPeer) {
        Double[] arrX1 = rankListHost.toArray(new Double[rankListHost.size()]);
        Double[] arrX2 = rankListPeer.toArray(new Double[rankListPeer.size()]);

        double table[][] = new double[arrX1.length][arrX2.length];
        table[0][0] = (double) Math.abs(arrX1[0] - arrX2[0]);

        for (int j = 1; j < arrX2.length; j++) {
            double cost = (double) Math.abs(arrX1[0] - arrX2[j]) + table[0][j - 1];
            table[0][j] = cost;
        }

        for (int i = 1; i < arrX1.length; i++) {
            double cost = (double) Math.abs(arrX1[i] - arrX2[0]) + table[i - 1][0];
            table[i][0] = cost;
        }

        for (int i = 1; i < arrX1.length; i++) {
            for (int j = 1; j < arrX2.length; j++) {
                double cost = (double) Math.abs(arrX1[i] - arrX2[j]);
                double min = findMin(table[i - 1][j], table[i][j - 1], table[i - 1][j - 1]);
                table[i][j] = cost + min;
            }
        }

        double d = table[arrX1.length - 1][arrX2.length - 1];
        return d;
    }

    private double findMin(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }
}
