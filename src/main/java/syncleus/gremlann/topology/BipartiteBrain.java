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
import syncleus.gremlann.AbstractBrain;
import static syncleus.gremlann.Graphs.set;

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

    public BipartiteBrain input(double... d) {
        assert (d.length == inputs.size());
        for (int i = 0; i < d.length; i++) {
            set(inputs.get(i), "signal", d[i]);
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
