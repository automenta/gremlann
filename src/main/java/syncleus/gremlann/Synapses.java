/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import static syncleus.gremlann.AbstractBrain.signal;
import static syncleus.gremlann.AbstractBrain.weight;

/**
 *
 * @author me
 */
public class Synapses {

    public static double inputSignal(Edge synapse) {
        Vertex source = synapse.inV().next();
        return signal(source);
    }
    
    /** returns activation */
    public static double propagate(Edge synapse) {
        double s = inputSignal(synapse) * weight(synapse);
        signal(synapse, s);
        return s;
    }
}
