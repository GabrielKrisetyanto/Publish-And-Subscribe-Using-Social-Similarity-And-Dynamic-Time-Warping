/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author User
 */
public class LatencyReport extends Report implements MessageListener, UpdateListener, ReportHelper {

    private List<Double> latencies = new ArrayList<Double>();
    private int lastRecord = 0;
    private int interval = 7200;
//    private int interval = 10800;
    private Map<Integer, String> nrofLatency = new HashMap<Integer, String>();

    @Override
    public void newMessage(Message m) {}
    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}
    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (firstDelivery) {
            double delay = getSimTime() - m.getCreationTime();
            this.latencies.add(delay);
        }
    }

    @Override
    public void done() {
        String statsText = "Contact\tLatencies\n";
        for (Map.Entry<Integer, String> entry : nrofLatency.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            statsText += key + "\t" + value + "\n";
        }
        write(statsText);
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        //mencari rata-rata
        if (SimClock.getIntTime() - lastRecord >= interval) {
            lastRecord = SimClock.getIntTime();
            String latenciesValue = getAverage(latencies);
            nrofLatency.put(lastRecord, latenciesValue);

        }

    }

    @Override
    public void haveReceivedMessages(int h, Message m) {
//        System.out.println("dipanggil");
        double delay = SimClock.getTime() - m.getCreationTime();
        this.latencies.add(delay);

    }
}
