/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import core.Settings;
import static input.MessageGene1.tempSubs;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import ku.Helper;
import ku.Pair;

/**
 *
 * @author User
 */
public class KontenGene1 extends MessageGene1 {

//    private static int ttl = 86400;
//    private static int waktu = 82800;  //1hr //23*3600
//    private int waktu = 21600; //6jam
    private int waktu = 18000; //5jam
//     private int waktu = 1209600; //2 weeks
    private static String contenCategory;
    private int co;
    private String id;
    List<Integer> keys;
    List<String> donePublish;
    int randomKey;
    protected String event[] = {"event1", "event2", "event3", "event4", "event5"};
    protected static List<String> sisa;
    private static Map<String, int[]> kategori;
    
    public KontenGene1(Settings s) {
        super(s);
        this.donePublish = new ArrayList();
        kategori = new HashMap<>();
        kategori.putAll(Helper.getKategori());
        sisa = new ArrayList();
    }

    //untuk random event
    private String randomEvent(List<String> li) {
        Random r = new Random();
        return li.get(r.nextInt(li.size()));
    }

    //untuk menyimpan ke temp pub
    protected boolean setPub(int from) {
        boolean test = false;
        String E = drawRandomEvent();
        //jika interest sama atau dia masih menjadi publisher konten tertentu maka keluar
        if ((tempSubs.containsKey(from) && E.equalsIgnoreCase(tempSubs.get(from).getEvent()))
                || tempPub.containsKey(from)) {
            test = false; //ganti node lain
        } else {
            double durasi = this.nextEventsTime + waktu;
            Pair p = new Pair(E, durasi);
            if (!donePublish.contains(E)) {
                donePublish.add(E);
            }
            if (!tempPub.containsKey(from)) {
                tempPub.put(from, p);
            }
            test = true;
        }

        return test;
    }

    protected String getC(int from) {
        if (tempPub.containsKey(from)) {
            contenCategory = tempPub.get(from).getEvent();
        }
        return idPrefix + getRandomNumberForID(contenCategory);
    }

    @Override
    public ExternalEvent nextEvent() {
        Iterator<Integer> it = tempPub.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            Pair p = tempPub.get(key);
            double durasi = p.getDurasi();
            if (this.nextEventsTime > durasi) {
                it.remove();
            }
        }

        int responseSize = 0;
        /* zero stands for one way messages */
        int msgSize;
        int interval;
        int from;
        int to;

        if (co == 0) {
            setEventCategory();
         
        }
        boolean bole = false;
        do {
            from = drawHostAddress(super.hostRange);
            bole = setPub(from);
//            System.out.println(from + " " + bole);
        } while (bole == false);

        to = drawToAddress(hostRange, from);
//        to = 0;
        id = getC(from);
        msgSize = drawMessageSize();
        interval = drawNextEventTimeDiff();

        MessageCreateEvent mce = new MessageCreateEvent(from, to, id,
                msgSize, responseSize, this.nextEventsTime);
        co++;

//        System.out.println("sebelum");
//        for (Map.Entry<Integer, Pair> entry : tempPub.entrySet()) {
//            Integer key = entry.getKey();
//            Pair value = entry.getValue();
//            System.out.println(key + "\t" + value.durasi);
//        }
//
//        System.out.println("sesudah");
//        for (Map.Entry<Integer, Pair> entry : tempPub.entrySet()) {
//            Integer key = entry.getKey();
//            Pair value = entry.getValue();
//            System.out.println(key + "\t" + value.durasi);
//        }
        this.nextEventsTime += interval;

        if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
            /* next event would be later than the end time */
            this.nextEventsTime = Double.MAX_VALUE;
        }
        return mce;
    }
}
