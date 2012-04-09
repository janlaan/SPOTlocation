/*
 * SensorSampler.java
 *
 * Copyright (c) 2008-2010 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Determines a SPOT location based on accelerometer values and location data from other SPOTs.
 * Other SPOTs should be running this same program.
 * @author: Jan Laan
 */
public class SensorSampler extends MIDlet {

    private static final int HOST_PORT = 67;
    private static final int SAMPLE_PERIOD = 1000;  // in milliseconds
    
    /**
     * Start the main SPOT application. This starts transmitting the perceived
     * node location, and starts a thread where the location data of other nodes
     * is received.
     * @throws MIDletStateChangeException 
     */
    protected void startApp() throws MIDletStateChangeException {
        
        /* Initalize some variables */
        RadiogramConnection rCon = null;
        Datagram dg = null;
        
        ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
        IAccelerometer3D accel = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
        
        double[] position = {0.0, 0.0, 0.0};
        double[] speed = {0.0, 0.0, 0.0};
        double[] acceleration = {0.0, 0.0, 0.0}; 
        double[] new_speed = new double[3];
        
        long timestamp = System.currentTimeMillis();
        long new_timestamp;
        
	// Listen for downloads/commands over USB connection
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            // Open up a broadcast connection to the host port
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dg = rCon.newDatagram(450);  // only sending 12 bytes of data
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
        
        //Start receiving incoming location data in a separate thread.
        SPOTListener s = new SPOTListener();
        s.start();
        
        /* Continuously read the accelerometer values and calculate position
         * based upon them.*/
        while (true) {
            try {
                acceleration = accel.getAccelValues();
                new_timestamp = System.currentTimeMillis();
                double seconds_passed = ((new_timestamp - timestamp) / 1000.0);
                timestamp = new_timestamp;
                
                new_speed[0] += acceleration[0] * 9.81* seconds_passed;
                position[0] += new_speed[0] * seconds_passed;

                new_speed[1] += acceleration[1] * 9.81 * seconds_passed;
                position[1] += new_speed[1] * seconds_passed;

                new_speed[2] += (acceleration[2] - 1.0) * 9.81 * seconds_passed;
                position[2] += new_speed[2] * seconds_passed;
                speed[0] = new_speed[0];
                speed[1] = new_speed[1];
                speed[2] = new_speed[2];

                /* Broadcast this node's new location */
                dg.reset();
                dg.writeLong(timestamp);
                
                dg.writeDouble(seconds_passed);
                dg.writeDouble(acceleration[0]);
                dg.writeDouble(acceleration[1]);
                dg.writeDouble(acceleration[2]);
                dg.writeDouble(position[0]);
                dg.writeDouble(position[1]);
                dg.writeDouble(position[2]);
                rCon.send(dg);
                
                /* Blink to show activity */
                led.setRGB(255, 255, 255);
                led.setOn();
                Utils.sleep(50);
                led.setOff();
                
            } catch(IOException ex) {
                //There's not much you can do when the sensor fails, just try again.
                System.err.println("Caught " + ex + " while collecting/sending sensor sample.");
            }
            
            //Sleep for a full secods
            Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - timestamp));
        }
    }
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}

/**
 * Listens for incoming location transmissions from other SPOT nodes.
 * 
 * @author jan
 */
class SPOTListener extends Thread {
    
    private RadiogramConnection rCon;
    private Radiogram dg = null;
   
    /**
     * Construct a new listener
     */
    public SPOTListener() {
        rCon = null;
        dg = null;
    }
    
    /**
     * Run this listener.
     */
    public void run() {
        
        /* Open up a listen connection on the host port */
        try {
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + 67);
            dg = (Radiogram) rCon.newDatagram(rCon.getMaximumLength());
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
        }
        
        ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED4");
        led.setColor(LEDColor.RED);
        
        /* Continuously receive incoming location transmissions. */
        while(true) {
            try {
                led.setOff();
                // Read sensor sample received over the radio
                rCon.receive(dg);
                int rssi = dg.getRssi();
                String addr = dg.getAddress();  // read sender's Id
                long time = dg.readLong();      // read time of the reading
                double sp = dg.readDouble();   // Time passed since last measurement of this node
                
                //Read accel. value/ position
                double acX = dg.readDouble();
                double acY = dg.readDouble();
                double acZ = dg.readDouble();
                double posX = dg.readDouble();
                double posY = dg.readDouble();
                double posZ = dg.readDouble();
                double[] pos = {posX, posY, posZ};
                
                // Update new data globally.
                Nodes.updateNodeData(addr, pos, time, rssi);
                led.setOn();
                Utils.sleep(10); //TODO: This sleep can potentially hinder the receiving of messages, it's only used for the LED

                //DEBUG
                //System.out.println(fmt.format(new Date(time)) + "  from: " + addr + " secs = " + sp + " accel =  (" + acX + ", " + acY + ", " + acZ + "),  value = (" + posX + ", " + posY + ", " + posZ + ")");
            } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
            }
        }
    }
}
