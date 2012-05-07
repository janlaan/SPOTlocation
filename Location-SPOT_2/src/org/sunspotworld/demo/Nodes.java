/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunspotworld.demo;

/**
 * A dataset with all other SPOT nodes encountered by this node.
 * 
 * @author jan
 */
class Nodes {
    public static SPOTData[] data = new SPOTData[10];
    public static int nodes = 0;
    
    /**
     * Update the (location) data of the given node with new values. Create a 
     * new entry if the node is encountered the first time.
     * @param ID The IEEE address of the encountered node
     * @param position The position given by the node. (3-dimensional)
     * @param timestamp The timestamp at which the node took its measurements.
     * @param rssi The signal strength of the node.
     */
    public static void updateNodeData(String ID, double[] position, long timestamp, int rssi) {
        boolean updated = false;
        for(int i = 0; i < nodes; i++) {
            if(data[i].getID().equals(ID)) {
                data[i].setPos(position);
                data[i].setTime(timestamp);
                data[i].setRssi(rssi);
                updated = true;
                break;
            }
        }
        if(!updated) {
            data[nodes] = new SPOTData(ID, position, timestamp, rssi);
            nodes++;
        }
    }
    
    /**
     * Retreive the data of a single node
     * @param ID The IEEE address of the node
     * @return A SPOTData object with node information.
     */
    public static SPOTData getNodeData(String ID) {
        for(int i = 0; i < nodes; i++) {
            if(data[i].getID().equals(ID)) {
                return data[i];
            }
        }
        return null;
    }
    
    public static SPOTData[] getAllNodes() {
        SPOTData[] ret = new SPOTData[nodes];
        System.arraycopy(data, 0, ret, 0, nodes);
        return ret;
    }
}
