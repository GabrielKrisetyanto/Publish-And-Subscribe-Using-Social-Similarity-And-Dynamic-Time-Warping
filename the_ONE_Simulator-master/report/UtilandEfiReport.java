/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ku.Helper;

/**
 *
 * @author User
 */
public class UtilandEfiReport extends Report implements MessageListener, ReportHelper {

    private static Map<String, int[]> kategori;
//    private static final String EVENT[] = {"event1", "event2", "event3", "event4", "event5"};
    public static final String CONTENT = "tertarik";
//    private static int waktu = 86400; //1hari
//    private static int waktu = 21600; //6jam
    private static final int waktu = 18000; //5jam
//    private static final int waktu = 1209600; //2 weeks
    private double lastRecord = Double.MIN_VALUE;
    private static Map<DTNHost, Double> tempSubs;
    private static Map<Message, Set<Integer>> dapat;
    private static Map<String, Quintet> utilnEfi;
//       private static Map<String, ArrayList<Double>> utilnEfi;
    private static final int WARMUP = 18000;
    private static Map<String, ArrayList<Message>> tampung;
    private static Map<Message, ArrayList<Message>> tertarik;
    private static Map<String, Set<Integer>> nBawa;
    private List<Double> avgUtil = new ArrayList<Double>();
    private List<Double> avgEfi = new ArrayList<Double>();
    private static final int WAKTU = 18000; //5jam

    public UtilandEfiReport() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        dapat = new HashMap<>();
        kategori = new HashMap<>();
        kategori.putAll(Helper.getKategori());
        tempSubs = new HashMap<>();
        utilnEfi = new HashMap<>();
        tampung = new HashMap<>();
        tertarik = new HashMap<>();
        nBawa = new HashMap<>();
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
                        double sTtl = sBuat + waktu;
                        double cBuat = key.getCreationTime();
                        double cTtl = key.getCreationTime() + waktu;
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
//                            if (!value.contains(me)) {
//                                value.add(me);
//                            }
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

        if (dropped && m.getId().contains("C")) {
            int deliv = 0;
            int subs = 0;
            int relay;
            double rata, rataEf;
            //jumlah dpt
            for (Map.Entry<Message, Set<Integer>> entry : dapat.entrySet()) {
                Message key = entry.getKey();
                Set value = entry.getValue();
                if (key.getId().equals(m.getId())) {
                    deliv = value.size();
                }
            }
            //jumlah subs
            ArrayList<Message> temp = new ArrayList<>();
            for (Map.Entry<Message, ArrayList<Message>> entry : tertarik.entrySet()) {
                Message key = entry.getKey();
                ArrayList value = entry.getValue();
                if (key.getId().equals(m.getId())) {
                    subs = value.size();
                    temp = value;
                }
            }
            //jumlah relay
            for (Message message : temp) {
                DTNHost from = message.getFrom();
                Set<Integer> temp2 = nBawa.get(m.getId());
                for (int i = 0; i < temp2.size(); i++) {
                    if (temp2.contains(from.getAddress())) {
                        nBawa.get(m.getId()).remove(from.getAddress());
                    }
                }
            }
            relay = nBawa.get(m.getId()).size();
            rata = (double) deliv / (double) subs;
            rataEf = (double) deliv / (double) relay;

            if (Double.isInfinite(rataEf) || rataEf >= 1) {
                rataEf = 1;}
            if (Double.isNaN(rataEf)) {
                rataEf = 0;}
            if (Double.isNaN(rata)) {
                rata = 0; }
            if (subs != 0) {
                Quintet q = new Quintet(deliv, subs, relay, rata, rataEf);
                utilnEfi.put(m.getId(), q);
            }
        }
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (m.getId().contains("C") && m.getFrom() != to) {//buat relay
            Set li = nBawa.get(m.getId());
//            if (!li.contains(to.getAddress())) {
              if (li != null) {
                if (!li.contains(to.getAddress())) {
                    li.add(to.getAddress());
                    nBawa.put(m.getId(), li);
                }
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
    public void done() {
        String coba = "Content\tDapat\tTertarik\tTerinfeksi\tUtilitiy\tEficiency\n";

        for (Map.Entry<String, Quintet> entry : utilnEfi.entrySet()) {
            String key = entry.getKey();
            Quintet value = entry.getValue();
            coba += key + "\t" + value.toString();
            avgUtil.add(value.util);
            avgEfi.add(value.efi);
        }

//        System.out.println("yg bawa");
//        for (Map.Entry<String, Set<Integer>> entry : nBawa.entrySet()) {
//            String key = entry.getKey();
//            Set value = entry.getValue();
//            System.out.println(key + "\t" + Arrays.toString(value.toArray()));
//        }
//        for (Map.Entry<String, ArrayList<Double>> entry : utilnEfi.entrySet()) {
//            String key = entry.getKey();
//            ArrayList value = entry.getValue();
//            coba += key + "\t" + value.get(0) + "\t" + value.get(1) + "\t" + value.get(2) + "\t" + value.get(3)
//                    + "\t" + value.get(4) + "\n";
//        }
//  
//        System.out.println("isi dapat");
//        for (Map.Entry<Message, Set<Integer>> entry : dapat.entrySet()) {
//            Message key1 = entry.getKey();
//            Set<Integer> value1 = entry.getValue();
//            System.out.println(key1 + "\t" + value1);
//        }
//        System.out.println("yg suka");
//        ArrayList<Message> su = new ArrayList<>();
//        for (Map.Entry<Message, ArrayList<Message>> entry : tertarik.entrySet()) {
//            ArrayList<Integer> suA = new ArrayList<>();
//            Message key = entry.getKey();
//            ArrayList value = entry.getValue();
//            su = value;
//            for (Message msg : su) {
//                suA.add(msg.getFrom().getAddress());
//            }
//            System.out.println(key + "\t" + Arrays.toString(suA.toArray()));
//        }
        /*
        for (Map.Entry<Message, LinkedList<String>> entry : tarik.entrySet()) {
            Message key = entry.getKey();
            LinkedList value = entry.getValue();
            String jenis = tes(key.getId());
            if (tertarik.containsKey(jenis)) {
                LinkedList list = tertarik.get(jenis);
                for (int i = 0; i < list.size(); i++) {
                    String isi = (String) list.get(i);
                    String[] sp = isi.split(",");
                    double sBuat = Double.parseDouble(sp[1]);
                    double sTtl = sBuat + waktu;
                    double cBuat = key.getCreationTime();
                    double cTtl = key.getCreationTime() + waktu;
                    if ((cBuat <= sTtl) && (sBuat <= cTtl)) {
                        if (!value.contains(isi)) {
                            value.add(isi);
                        }
                    } else {
                        continue;
                    }
                }
            }
        }

        for (Map.Entry<Message, LinkedList<String>> entry : tarik.entrySet()) {
            Message key = entry.getKey();
            LinkedList value = entry.getValue();
            String jen = tes(key.getId());
            if (dapat.containsKey(key)) {
                int deliv = dapat.get(key).size();
                double utili = 0.0;
                if (deliv == 0 || value.isEmpty()) {
                    utili = 0.0;
                } else {
                    utili = (double) deliv / (double) value.size();
                }
                avgUtil.add(utili);
                coba += key + "\t" + deliv + "\t" + value.size() + "\t" + utili + "\n";
            }
        }*/
        String avgUtility = getAverage(avgUtil);
        String avgEficiency = getAverage(avgEfi);
        write(coba);
        write(avgUtility);
        write(avgEficiency);
        super.done();
    }

}
