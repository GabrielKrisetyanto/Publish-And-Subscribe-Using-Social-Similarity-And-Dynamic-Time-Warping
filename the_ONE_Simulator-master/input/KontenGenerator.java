/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import core.Settings;
import static input.MessageGene.tempSubs;
import java.util.Iterator;
import java.util.Random;
import ku.Pair;

/**
 *
 * @author User
 */
public class KontenGenerator extends MessageGene {

    private final int waktu = 18000; //5jam

    public KontenGenerator(Settings s) {
        super(s);
    }

    @Override
    protected int drawHostAddress(int[] hostRange) {
        Random r = new Random();
        return r.nextInt((hostRange[1] - hostRange[0]) + 1) + hostRange[0];
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
//            if (!tempPub.containsKey(from)) {
                tempPub.put(from, p);
//            }
            test = true;
        }

        return test;
    }

    //untuk draw random id Kategori interval
    protected String drawRandomNumberForID(String e) {
        int[] interval = kategori.get(e);
//        if (kategori.containsKey(e)) {
//            interval = kategori.get(e);
            min = interval[0];
            max = interval[1];
//        }
        double r = new Random().nextDouble();
        double result = min + (r * (max - min));
        return String.valueOf(result);
    }

    protected String getContentID(int from) {
        String contentCategory = "";
        if (tempPub.containsKey(from)) {
            contentCategory = tempPub.get(from).getEvent();
        }
        return idPrefix + drawRandomNumberForID(contentCategory);
    }

    @Override
    public ExternalEvent nextEvent() {
        //update(buang) temporaryPub yang kadaluarsa
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
        String id;
        boolean bole = false;
        do {
            from = drawHostAddress(hostRange);
            bole = setPub(from);
        } while (bole == false);

        to = drawToAddress(hostRange, from);
        id = getContentID(from);
        msgSize = drawMessageSize();
        interval = drawNextEventTimeDiff();

        MessageCreateEvent mce = new MessageCreateEvent(from, to, id,
                msgSize, responseSize, this.nextEventsTime);

        this.nextEventsTime += interval;

        if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
            /* next event would be later than the end time */
            this.nextEventsTime = Double.MAX_VALUE;
        }
        return mce;
    }
}
