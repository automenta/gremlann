/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import static syncleus.gremlann.Graphs.real;
import static syncleus.gremlann.Graphs.set;
import static syncleus.gremlann.Neuron.signal;

/**
 * Synapse data
 */
public class Synapse {

    public static double weight(Edge e) {
        return real(e, "weight");
    }

    public static void weight(Edge e, double newWeight) {
        set(e, "weight", newWeight);
    }

    protected double weight;
    
    public static double inputSignal(final Edge synapse) {
        Vertex source = synapse.iterators().vertexIterator(Direction.IN).next();
        return signal(source);
    }
    
    /** returns activation */
    public static double propagate(final Edge synapse) {
        double s = inputSignal(synapse) * weight(synapse);
        signal(synapse, s);
        return s;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    
}
