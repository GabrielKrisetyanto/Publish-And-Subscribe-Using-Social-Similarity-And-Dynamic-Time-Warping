/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ku;

/**
 *
 * @author User
 */
public class Pair {
    private String event;
    private double durasi;

    public Pair(String event, double durasi) {
        this.event = event;
        this.durasi = durasi;
    }

    public String getEvent() {
        return event;
    }

    public double getDurasi() {
        return durasi;
    }
    
}
