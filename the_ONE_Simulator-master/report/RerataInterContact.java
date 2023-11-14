/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.List;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.BantuReport;

/**
 *
 * @author User
 */
public class RerataInterContact extends Report {
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

            double hasil = br.getRerataInter();
            write(node + "\t" + hasil);
//                coba.put(node, hasil);

        }
        super.done();
     }
     
}
