/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.structure.Element;
import java.io.Serializable;
import static syncleus.gremlann.Graphs.the;

/**
 * Neuron data
 */
public class Neuron implements Serializable {
 
    /** internalized activation amount */
    protected double activation;
    
    /** externalalized signal */
    protected double signal;
    

    
    /**
     * @return the activation
     */
    public double getActivity() {
        return activation;
    }

    /**
     * @param activation the activation to set
     */
    public Neuron setActivity(double activation) {
        this.activation = activation;
        return this;
    }

    /**
     * @return the signal
     */
    public double getSignal() {
        return signal;
    }

    /**
     * @param signal the signal to set
     */
    public Neuron setSignal(double signal) {
        this.signal = signal;
        return this;
    }
    
    public static double signal(Element v) {  
        return the(v, Neuron.class).getSignal();
    }
    public static void signal(Element v, double newValue) {  
        the(v, Neuron.class).setSignal(newValue);
    }
    public static double activity(Element v) {  
        return the(v, Neuron.class).getActivity();
    }
    public static void activity(Element v, double newValue) {  
        the(v, Neuron.class).setActivity(newValue);
    }
    
}
