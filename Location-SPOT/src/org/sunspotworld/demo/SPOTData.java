/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunspotworld.demo;

/**
 * Holds various data for one SPOT node.
 * @author jan
 */
class SPOTData {
    public String ID;
    public double[] position;
    public long timestamp;
    public static int rssi;
    
    /**
     * Construct an empty node with just the address
     * @param ID The IEEE address of the node.
     */
    public SPOTData(String ID) {
        this.ID = ID;
        this.position = null;
        this.timestamp = 0l;
        this.rssi = 0;
    }
    
    /**
     * Construct a new node.
     * @param ID The IEEE address of the node
     * @param position The position transmitted by the node.
     * @param timestamp The time at which the position was measured.
     * @param rssi The signal strength at the time of <b>receiving<b> the data.
     */
    public SPOTData(String ID, double[] position, long timestamp, int rssi) {
        this.ID = ID;
        this.position = position;
        this.timestamp = timestamp;
        this.rssi = rssi;
    }
    
    public String getID() {
        return this.ID;
    }
    
    public double[] getPos() {
        return this.position;
    }
    
    public void setPos(double[] newpos) {
        this.position = newpos;
    }
    
    public long getTime() {
        return this.timestamp;
    }
    
    public void setTime(long newtime) {
        this.timestamp = newtime;
    }
    
    public int getRssi() {
        return this.rssi;
    }
    
    public void setRssi(int new_rssi) {
        this.rssi = new_rssi;
    }
}
