package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import java.util.*;

import ku.Helper;

public class UtilityandEfficiencyReportSociSimi extends Report implements MessageListener, ReportHelper {

    private static Map<String, int[]> kategori;
    private static final int WAKTU = 18000; // 5 jam
    private static Map<String, Quintet> utilandEfi;
    private static Map<String, ArrayList<Message>> tampung;
    private static Map<Message, ArrayList<Message>> tertarik;
    private static Map<String, Set<Integer>> nBawa;
    private static Map<Message, Set<Integer>> dapat;
    private List<Double> avgUtil;
    private List<Double> avgEfi;
    private String reportString;

    public UtilityandEfficiencyReportSociSimi() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        kategori = new HashMap<>();
        kategori.putAll(Helper.getKategori());
        utilandEfi = new HashMap<>();
        tertarik = new HashMap<>();
        tampung = new HashMap<>();
        nBawa = new HashMap<>();
        dapat = new HashMap<>();
        avgUtil = new ArrayList<>();
        avgEfi = new ArrayList<>();
    }

    @Override
    public void newMessage(Message m) {
        //yg baru
        if (m.getId().contains("C") && m.getCreationTime() + WAKTU <= 259200) {
            dapat.put(m, new HashSet());
            tertarik.put(m, new ArrayList<>());
            nBawa.put(m.getId(), new HashSet<>());
        } else if (m.getId().contains("S")) {
            String ev = tes(m.getId());
            if (tampung.containsKey(ev)) {
                ArrayList l2 = tampung.get(ev);
                if (!l2.contains(m)) {
                    l2.add(m);
                    tampung.put(ev, l2);
                }
            } else {
                ArrayList l = new ArrayList<>();
                l.add(m);
                tampung.put(ev, l);
            }
            for (Map.Entry<Message, ArrayList<Message>> entry : tertarik.entrySet()) {
                Message key = entry.getKey();
                ArrayList value = entry.getValue();
                String jenis = tes(key.getId());
                if (tampung.containsKey(jenis)) {
                    ArrayList list = tampung.get(jenis);
                    for (int i = 0; i < list.size(); i++) {
                        Message me = (Message) list.get(i);
                        double sBuat = me.getCreationTime();
                        double sTtl = sBuat + WAKTU;
                        double cBuat = key.getCreationTime();
                        double cTtl = key.getCreationTime() + WAKTU;
                        if ((cBuat <= sTtl) && (sBuat <= cTtl)) {
                            //cek apakah saya sudah termasuk org yg suka suatu konten tertentu
                            boolean coba = false;
                            for (int j = 0; j < value.size(); j++) {
                                Message before = (Message) value.get(j);
                                if (before.getFrom() == me.getFrom()) {
                                    coba = true;
                                }
                            }
                            if (!coba) {
                                value.add(me);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (m.getId().contains("C") && m.getFrom() != to) {//buat relay
            Set li = nBawa.get(m.getId());
            if (li != null) {
                if (!li.contains(to.getAddress())) {
                    li.add(to.getAddress());
                    nBawa.put(m.getId(), li);
                }
            }
            if (firstDelivery) {
                for (Map.Entry<Message, Set<Integer>> entry1 : dapat.entrySet()) {
                    Message key1 = entry1.getKey();
                    Set<Integer> value1 = entry1.getValue();
                    if (key1.getId().equals(m.getId())) {
                        value1.add(to.getAddress());
                        dapat.put(key1, value1);
                    }
                }
            }
        }
    }
    private String tes(String msgId) {
        String jenis = "";
        double id = Double.parseDouble(msgId.substring(1));
        for (Map.Entry<String, int[]> entry : kategori.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();
            int min = value[0];
            int max = value[1];
            if (id >= min && id <= max) {
                jenis = key;
            }
        }
        return jenis;
    }

    @Override
    public void done() {
        StringBuilder reportBuilder = new StringBuilder();
        int deliv, subs, relay;
        double rata, rataEf;
        ArrayList<Message> temp;
        reportBuilder.append("Content\tDapat\tTertarik\tTerinfeksi\tUtilitiy\tEfficiency\n");
        
        for (Map.Entry<Message, ArrayList<Message>> entry : tertarik.entrySet()) {
            Message key = entry.getKey();
            ArrayList<Message> value = entry.getValue();
            temp = value;
            subs = value.size();
            for (Map.Entry<Message, Set<Integer>> entry2 : dapat.entrySet()) {
                Message key2 = entry2.getKey();
                Set<Integer> value2 = entry2.getValue();
                if (key2.getId().equals(key.getId())) {
                    deliv = value2.size();
                    for (Message message : temp) {
                        DTNHost from = message.getFrom();
                        Set<Integer> temp2 = nBawa.get(key.getId());
                        if (temp2.contains(from.getAddress())) {
                            nBawa.get(key.getId()).remove(from.getAddress());
                        }
                    }
                    relay = nBawa.get(key.getId()).size();
                    rata = (double) deliv / (double) subs;
                    rataEf = (double) deliv / (double) relay;
                    if (Double.isInfinite(rataEf) || rataEf >= 1) {
                        rataEf = 1;
                    }
                    if (Double.isNaN(rataEf)) {
                        rataEf = 0;
                    }
                    if (Double.isNaN(rata)) {
                        rata = 0;
                    }
                    if (subs != 0) {
                        Quintet q = new Quintet(deliv, subs, relay, rata, rataEf);
                        utilandEfi.put(key.getId(), q);
                    }
                }
            }
        }

        for (Map.Entry<String, Quintet> entry : utilandEfi.entrySet()) {
            String key = entry.getKey();
            Quintet value = entry.getValue();
            reportBuilder.append(key).append("\t").append(value.toString()).append("\n");
            avgUtil.add(value.util);
            avgEfi.add(value.efi);
        }
        
        String avgUtility = getAverage(avgUtil);
        String avgEfficiency = getAverage(avgEfi);
        reportBuilder.append("\nAverage Utility: ").append(avgUtility).append("\n");
        reportBuilder.append("Average Efficiency: ").append(avgEfficiency).append("\n");
        
        reportString = reportBuilder.toString();
        
        super.done();
    }

    @Override
    public void haveReceivedMessages(int h, Message m) {
        for (Map.Entry<Message, Set<Integer>> entry1 : dapat.entrySet()) {
            Message key1 = entry1.getKey();
            Set<Integer> value1 = entry1.getValue();
            if (key1.getId().equalsIgnoreCase(m.getId())) {
                value1.add(h);
                dapat.put(key1, value1);
            }
        }

    }
    

    @Override
    public String getAverage(List<Double> list) {
        if (list.isEmpty()) {
            return "N/A";
        }

        double sum = 0;
        for (Double value : list) {
            sum += value;
        }

        double average = sum / list.size();
        return String.format("%.2f", average);
    }

    public String getReport() {
        return reportString;
    }

    private class Quintet {

        int delivered;
        int subs;
        int relayed;
        double util;
        double efi;

        public Quintet(int d, int s, int r, double u, double e) {
            delivered = d;
            subs = s;
            relayed = r;
            util = u;
            efi = e;
        }

        @Override
        public String toString() {
            return delivered + "\t" + subs + "\t" + relayed + "\t" + util + "\t" + efi;
        }

    }
}
