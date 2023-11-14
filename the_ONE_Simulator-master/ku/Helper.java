/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ku;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author User
 */
public class Helper {

    private static Map<String, int[]> kategori = new HashMap<>();
     private static Map<String, int[]> kategori2 = new HashMap<>();
    private static final String EVENT[] = {"event1", "event2", "event3", "event4", "event5"};
    private static final String EVENT2[] = {"event1", "event2"};
    private static final Map<Integer, double[]> AREA;
    private static Map<Integer, int[]> host = new HashMap<>();
    static {
        int total = 100 - 1;
        int subTotal = total / 5;
        int current_start = 1;
        int e[];
        LinkedList li = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            e = new int[]{current_start, current_start + subTotal};
            kategori.put(EVENT[i], e);
            current_start += subTotal + 1;
        }

        int t = 100-1;
        int st = total / 2;
        int cur = 1;
        int elain[];
        for (int i = 0; i < 2; i++) {
            elain = new int[]{cur, cur+ st};     
            kategori2.put(EVENT2[i], elain);
            cur += st + 1;
        }
        AREA = new HashMap<>();
        for (int i = 1; i <= 9; i++) {
            double p = 1200 / 3;
            double[] a = gridCoordinates(i, p);
            AREA.put(i, a);
        }
        
        //host
        int[] h1 = new int[]{0,29}; //a1 dn2 dn 3
        int[] h2 = new int[]{30,59}; //a4 dn 5 dn 6
        int[] h3 = new int[]{60,79}; //a5 dn 8 
        int[] h4 = new int[]{80,89}; //9
        host.put(1, h1);
        host.put(2, h2);
        host.put(3, h3);
        host.put(4, h4);
        
    }

    public static double[] gridCoordinates(int cell, double p) {
        int col = (cell - 1) % 3;
        int row = (cell - 1) / 3;
        double y1 = 0 + row * p;
        double y2 = y1 + p;
        double x1 = 0 + col * p;
        double x2 = x1 + p;
        return new double[]{x1, x2, y1, y2};
    }

    public static Map getKategori() {
        return kategori;
    }
    public static Map getKategori2() {
        return kategori2;
    }
    public static Map getArea(){
        return AREA;
    }
    public static Map getHostRange(){
        return host;
    }
}
