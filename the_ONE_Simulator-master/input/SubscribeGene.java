/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import core.Settings;
import core.SimClock;
import static input.MessageGene1.tempPub;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ku.Pair;

/**
 *
 * @author User
 */
public class SubscribeGene extends MessageGene1 {

    public static Map<Integer, String> tempInterest = new HashMap<Integer, String>();
    private static String interestCategory;
    private String id;
    private int co = 0;
//    private int ttl = 86400;
    private int ttl = 43200;
//    private int waktu = 82800; //misal setiap 1 hr interest berubah
//    private int waktu = 21600; //6jam
    private int waktu = 18000; //5jam
//       private int waktu = 1209600; //2 weeks
    List<Integer> keys;
    List<String> listEvent = new LinkedList<>();
//    protected ReportHelper rh = new Util3();

    public SubscribeGene(Settings s) {
        super(s);
    }

    protected void setSubs(int from) {
        String evt = drawRandomEvent();
        if (tempPub.containsKey(from)) {
//            System.out.println("ADA DIA DI PUB");
            /*selama event yg di draw sama dgn yang dia publish atau 
            salah satu dri listEvent terpenuhi maka acak terus*/
            while (evt.equalsIgnoreCase(tempPub.get(from).getEvent()) || (listEvent.contains(evt) && listEvent.size() < 5)) {
                evt = drawRandomEvent();
//                System.out.println("TES LOOP ");
            }
            if (!listEvent.contains(evt)) {
                listEvent.add(evt);
            }

            double durasi = this.nextEventsTime + waktu;
            Pair p = new Pair(evt, durasi);
            tempSubs.put(from, p);
        } else {
            while (listEvent.size() < 5 && listEvent.contains(evt)) {
                evt = drawRandomEvent();
//                System.out.println("TES LOOP2 ");
            }
            if (!listEvent.contains(evt)) {
                listEvent.add(evt);
            }

            double durasi = this.nextEventsTime + waktu;
            Pair p = new Pair(evt, durasi);
            tempSubs.put(from, p);
            
//            System.out.println("pembatas");
//            
//            for (Map.Entry<Integer, Pair> entry : tempSubs.entrySet()) {
//                Integer key = entry.getKey();
//                Pair value = entry.getValue();
//                System.out.println(key + "\t" + value.getDurasi());
//            }
        }
    }

    private String getSID(int from) {
        int[] interval;
        if (tempSubs.containsKey(from)) {
            interestCategory = tempSubs.get(from).getEvent();
        }
        interval = eventCategory.get(interestCategory);
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
        if (co == 0) {
            setEventCategory();
        }
        /* Get two *different* nodes randomly from the host ranges */
        from = drawHostAddress(this.hostRange);
//        System.out.println("waktu saat ini :" + SimClock.getIntTime());
        while (tempSubs.containsKey(from)) {
            from = drawHostAddress(hostRange);
//            System.out.println(from);
//            System.out.println("mslh 2");
        }
        to = drawToAddress(hostRange, from);
//        to = 0;
        setSubs(from);
        id = getSID(from);
        /*
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        for (DTNHost node : nodes) {
            if (node.getAddress() == from) {
                MessageRouter r = node.getRouter();
                if (!(r instanceof DecisionEngineRouter)) {
                    continue;
                }
                RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
                if (!(de instanceof ContentSubs3)) {
                    continue;
                }
                double min = Double.parseDouble(id.substring(1));
                double max = min + 19;
                Message[] messages = node.getMessageCollection().toArray(new Message[0]);
                for (int i = 0; i < messages.length; i++) {
                    double idM = Double.parseDouble(messages[i].getId().substring(1));
                    boolean te = idM >= min && idM <=max;
                    System.out.println("isi boolean te"+te);
                    if (te) {
                        double cBuat = messages[i].getCreationTime();
                        double cTTL = cBuat + waktu;
                        double sBuat = this.nextEventsTime;
                        double sTTL = sBuat + waktu;
                        if ((cBuat <= sTTL) && (sBuat <= cTTL)) {
                            boolean test = true;
//                            for (ReportHelper r1 : this.rh) {
                            rh.hasMessagesInBuffer(node.getAddress(), messages[i].getId(), test);
//                            }
//                            System.out.println("hash code rh : " +rh.hashCode());
                        }
                    }
                }
            }
        }*/

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
