/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import core.Settings;
import core.SimClock;
import static input.MessageGene.tempPub;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ku.Helper;
import ku.Pair;

/**
 *
 * @author User
 */
public class SubscribeGenerator1 extends MessageGene {

    public static Map<Integer, String> tempInterest = new HashMap<Integer, String>();
//    private static String interestCategory;
    private String id;
    private int co = 0;
    private int waktu = 18000; //5jam
    List<Integer> keys;
    List<String> listEvent = new LinkedList<>();
//    protected String eventKu[] = {"event1", "event2", "event3", "event4", "event5"};
    protected String eventKu[] = {"event1", "event2"};
    public static Map<Integer, int[]> hostR = new HashMap<>();

    public SubscribeGenerator1(Settings s) {
        super(s);
        hostR.putAll(Helper.getHostRange());
    }

    protected String drawRandomEvent() {
        int select = rng.nextInt(eventKu.length);
        return eventKu[select];
    }

    @Override
    protected int drawHostAddress(int[] hostRange) {
        Random r = new Random();
        return r.nextInt((hostRange[1] - hostRange[0]) + 1) + hostRange[0];
    }

    protected boolean setSubs(int from) {
        String evt = drawRandomEvent();
        String evtKu = "";
        boolean tes = true;
        if (tempPub.containsKey(from)) { //jika dia publisher suatu konten
//            System.out.println("ADA DIA DI PUB");
            /*selama event yg di draw sama dgn yang dia publish maka acak terus*/
//            while (evt.equalsIgnoreCase(tempPub.get(from).getEvent())) {
//                evt = drawRandomEvent();
////                System.out.println("TES LOOP ");
//            }
            for (Map.Entry<Integer, int[]> entry : hostR.entrySet()) {
                Integer key = entry.getKey();
                int[] value = entry.getValue();
                if (from >= value[0] && from <= value[1]) {
                    if (key == 1 || key == 2) {
                        evtKu = eventKu[0];
                        if (evtKu.equalsIgnoreCase(tempPub.get(from).getEvent())) {
                            tes = false;
                        }
                    } else {
                        evtKu = eventKu[1];
                        if (evtKu.equalsIgnoreCase(tempPub.get(from).getEvent())) {
                            tes = false;
                        }
                    }

                }
            }
            double durasi = this.nextEventsTime + waktu;
            Pair p = new Pair(evtKu, durasi);
            tempSubs.put(from, p);
        } else { //jika bukan publisher
            for (Map.Entry<Integer, int[]> entry : hostR.entrySet()) {
                Integer key = entry.getKey();
                int[] value = entry.getValue();
                if (from >= value[0] && from <= value[1]) {
                    if (key == 1 || key == 2) {
                        evtKu = eventKu[0];
                    } else {
                        evtKu = eventKu[1];
                    }

                }
            }
            double durasi = this.nextEventsTime + waktu;
            Pair p = new Pair(evtKu, durasi);
            tempSubs.put(from, p);

//            System.out.println("pembatas");
//            
//            for (Map.Entry<Integer, Pair> entry : tempSubs.entrySet()) {
//                Integer key = entry.getKey();
//                Pair value = entry.getValue();
//                System.out.println(key + "\t" + value.getDurasi());
//            }
        }
        return tes;
    }

    private String getSID(int from) {
        String interestCategory = "";
        int[] interval;
        if (tempSubs.containsKey(from)) {
            interestCategory = tempSubs.get(from).getEvent();
        }

        interval = kategori.get(interestCategory);
        min = interval[0];
        return idPrefix + min;
    }

    @Override
    public ExternalEvent nextEvent() {
        Iterator<Integer> it = tempSubs.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            Pair a = tempSubs.get(key);
            double durasi = a.getDurasi();
            if (this.nextEventsTime > durasi) {
                it.remove();
            }
        }
//        if (this.nextEventsTime > ttl) {
//            listEvent.clear();
//            ttl+=43200;
//        }
        int responseSize = 0;
        /* zero stands for one way messages */
        int msgSize;
        int interval;
        int from;
        int to;
//        if (co == 0) {
//            setEventCategory();
//        }
        /* Get two *different* nodes randomly from the host ranges */
//        from = drawHostAddress(this.hostRange);
////        System.out.println("waktu saat ini :" + SimClock.getIntTime());
//        while (tempSubs.containsKey(from)) {
//            from = drawHostAddress(hostRange);
////            System.out.println(from);
////            System.out.println("mslh 2");
//        }
        boolean bole = false;
        do {
            from = drawHostAddress(hostRange);
            bole = setSubs(from);
//            System.out.println(from + " " + bole);
        } while (bole == false);
        to = drawToAddress(hostRange, from);

        setSubs(from);
        id = getSID(from);

        for (Map.Entry<String, int[]> entry : kategori.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();
            System.out.println(key + "\t" + value[0] + "," + value[1]);
        }
        msgSize = drawMessageSize();
        interval = drawNextEventTimeDiff();

        MessageCreateEvent mce = new MessageCreateEvent(from, to, id,
                msgSize, responseSize, this.nextEventsTime);
        co++;

        this.nextEventsTime += interval;

        if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
            /* next event would be later than the end time */
            this.nextEventsTime = Double.MAX_VALUE;
        }
        return mce;
    }

}
