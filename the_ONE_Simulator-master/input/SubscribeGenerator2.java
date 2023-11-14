/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import core.Settings;
import static input.MessageGene.tempPub;
import java.util.Iterator;
import java.util.Random;
import ku.Pair;

/**
 *
 * @author User
 */
public class SubscribeGenerator2 extends MessageGene {

    private final int waktu = 18000; //5jam

    public SubscribeGenerator2(Settings s) {
        super(s);
    }

    @Override
    protected int drawHostAddress(int[] hostRange) {
        Random r = new Random();
        return r.nextInt((hostRange[1] - hostRange[0]) + 1) + hostRange[0];
    }
    protected void setSubs(int from) {
        String evt = drawRandomEvent();
        if (tempPub.containsKey(from)) { //jika dia publisher suatu konten
            /*selama event yg di draw sama dgn yang dia publish maka acak terus*/
            while (evt.equalsIgnoreCase(tempPub.get(from).getEvent())) {
                evt = drawRandomEvent();
            }     
            double durasi = this.nextEventsTime + waktu;
            Pair p = new Pair(evt, durasi);
            tempSubs.put(from, p);
        } else { //jika bukan publisher
            double durasi = this.nextEventsTime + waktu;
            Pair p = new Pair(evt, durasi);
            tempSubs.put(from, p);

        }
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
        int responseSize = 0;
        /* zero stands for one way messages */
        int msgSize;
        int interval;
        int from;
        int to;
        String id;
        /* Get two *different* nodes randomly from the host ranges */
        from = drawHostAddress(this.hostRange);
        while (tempSubs.containsKey(from)) {
            from = drawHostAddress(hostRange);
        }
        to = drawToAddress(hostRange, from);

        setSubs(from);
        id = getSID(from);
  
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
