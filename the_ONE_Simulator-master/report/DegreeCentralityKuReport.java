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
import routing.community.CentralityKu;


/**
 *
 * @author User
 */
public class DegreeCentralityKuReport extends Report {

    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        for (DTNHost node : nodes) {
            MessageRouter r = node.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof CentralityKu)) {
                continue;
            }
            CentralityKu ck = (CentralityKu) de;
            int[] a = ck.sentral();
            String cetak = "";
            for (int i = 0; i < a.length; i++) {
                cetak += a[i] + ", ";
            }
            
            write(node + "\t" + cetak);
//                coba.put(node, hasil);

        }
        super.done();
    }
}
