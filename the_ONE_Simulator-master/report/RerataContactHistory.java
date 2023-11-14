/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.BantuReport;
import routing.community.Duration;

/**
 *
 * @author User
 */
public class RerataContactHistory extends Report {

    private Map<DTNHost, Double> coba;

    public RerataContactHistory() {
        coba = new HashMap<DTNHost, Double>();
    }

    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        for (DTNHost node : nodes) {
            MessageRouter r = node.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof BantuReport)) {
                continue;
            }
            BantuReport br = (BantuReport) de;

            double hasil = br.getRerata();
            write(node + "\t" + hasil);
//                coba.put(node, hasil);

        }
//        for (Map.Entry<DTNHost, Double> entry : coba.entrySet()) {
//            DTNHost key = entry.getKey();
//            Double value = entry.getValue();
////                print = print + key + " " + value + "\n";             
//            write(key + "\t" + value);
//
//        }
        super.done();
    }
}
