/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

/**
 *
 * @author User
 */
public class Quintet {

    public int deliv;
    public int suka;
    public int infected;
    public double util;
    public double efi;


    public Quintet(int d, int s, int i, double u, double e) {
        deliv = d;
        suka = s;
        infected = i;
        util = u;
        efi = e;
    }

    @Override
    public String toString() {
        return deliv + "\t" + suka + "\t" + infected + "\t" + util + "\t" + efi + "\n";
    }

}
