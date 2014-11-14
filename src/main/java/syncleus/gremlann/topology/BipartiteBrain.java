/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.topology;

import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math3.linear.ArrayRealVector;
import syncleus.gremlann.AbstractBrain;
import static syncleus.gremlann.AbstractBrain.signals;

/**
 * Bipartite brain with only input and output layers
 */
abstract public class BipartiteBrain extends AbstractBrain {

    public final Vertex meta;
    
    protected List<Vertex> inputs;
    protected List<Vertex> outputs;
    
    public BipartiteBrain(Vertex v, List<Vertex> inputs, List<Vertex> outputs) {
        super();
        this.meta = v;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    protected void ensureCorrectInputDimensions(int d) {
        if (d != getInputCount())
            throw new IllegalArgumentException("Dimentionality mismatch");
    }
    
    protected void ensureCorrectOutputDimensions(int d) {
        if (d != getOutputCount())
            throw new IllegalArgumentException("Dimentionality mismatch");
    }
    
    
    
    public ArrayRealVector inputSignals() {
        return signals(inputs);
    }

    public ArrayRealVector outputSignals() {
        return signals(outputs);
    }

    
    public BipartiteBrain input(double... d) {        
        ensureCorrectInputDimensions(d.length);
        
        for (int i = 0; i < d.length; i++) {
            signal(inputs.get(i), d[i]);
        }
        return this;
    }

    public List<Vertex> getInputs() {
        return inputs;
    }

    public List<Vertex> getOutputs() {
        return outputs;
    }
    
    public double inputSignal(int i) {
        return signal(getInputs().get(i));
    }
    public void inputSignal(int i, double newValue) {
        signal(getInputs().get(i), newValue);
    }
    public double outputSignal(int i) {
        return signal(getOutputs().get(i));
    }
    public void outputSignal(int i, double newValue) {
        signal(getOutputs().get(i), newValue);
    }

    public int getInputCount() { return getInputs().size(); }
    public int getOutputCount() { return getOutputs().size(); }
    
    @Override
    public GraphTraversal<Vertex, Vertex> traverseNeurons() {
        return null;
    }
    
    
    public static final Comparator<Traverser<Vertex>> increasingLayer = new Comparator<Traverser<Vertex>>() {
        @Override public int compare(Traverser<Vertex> o1, Traverser<Vertex> o2) {
            return Integer.compare((int) o1.get().value("layerNum"), (int) o2.get().value("layerNum"));
        }
    };
    
    public static final Comparator<Traverser<Vertex>> decreasingLayer = new Comparator<Traverser<Vertex>>() {
        @Override public int compare(Traverser<Vertex> o1, Traverser<Vertex> o2) {
            return increasingLayer.compare(o2, o1);
        }
    };
    
}
