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
import ku.DTWDistance;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author User
 */
public class DistancetoOtherHost extends Report {

    private Map<DTNHost, Integer> hasil;

    public DistancetoOtherHost() {
        hasil = new HashMap<>();
    }

    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        for (DTNHost node : nodes) {
            MessageRouter r = node.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof DTWDistance)) {
                continue;
            }
            DTWDistance br = (DTWDistance) de;

            write(node.toString());
            hasil = br.getDistance();
            for (Map.Entry<DTNHost, Integer> entry : hasil.entrySet()) {
                DTNHost key = entry.getKey();
                Integer value = entry.getValue();
                write(key + "\t" + value);
            }
//            write(node + "\t" + hasil);
//                coba.put(node, hasil);
        }
        super.done();
    }

}
