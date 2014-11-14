/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.google.common.base.Function;
import syncleus.gremlann.activation.ActivationFunction;
import syncleus.gremlann.activation.HyperbolicTangentActivationFunction;
import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.linear.ArrayRealVector;
import static syncleus.gremlann.Graphs.*;

/**
 * Brain which is structured as layers
 */
public class LayerBrain extends AbstractBrain {
    public final Vertex meta;
    public static final ActivationFunction defaultActivationFunction = new HyperbolicTangentActivationFunction();
    private final List<List<Vertex>> hidden;
    private final List<Vertex> inputs;
    private final List<Vertex> outputs;
    private final List<Vertex> layers;    
    
    public static final Comparator<Traverser<Vertex>> increasingLayer = new Comparator<Traverser<Vertex>>() {
        @Override
        public int compare(Traverser<Vertex> o1, Traverser<Vertex> o2) {
            return Integer.compare((int) o1.get().value("layerNum"), (int) o2.get().value("layerNum"));
        }
    };
    public static final Comparator<Traverser<Vertex>> decreasingLayer = new Comparator<Traverser<Vertex>>() {
        @Override
        public int compare(Traverser<Vertex> o1, Traverser<Vertex> o2) {
            return increasingLayer.compare(o2, o1);
        }
    };

    public LayerBrain(Vertex v, int inputs, int hidden, int outputs) {
        this(v, inputs, new int[]{hidden}, outputs);
    }

    public LayerBrain(Vertex v, int numInputs, int[] numHidden, int numOutputs) {
        this(v, newSignalArray("input", v.graph(), numInputs), numHidden, numOutputs);
    }

    public LayerBrain(Vertex v, List<Vertex> inputs, int[] numHidden, int numOutputs) {
        this.meta = v;
        this.layers = new ArrayList(2 + numHidden.length);
        this.inputs = addLayer("input", inputs);
        
        //TODO test if already created, if not avoid re-creating
        List<Vertex> previousLayer = this.inputs;
        
        this.hidden = new ArrayList(numHidden.length);
        int j = 0;
        
        for (int layerSize : numHidden) {
            String hiddenPrefix = "hidden." + j;
            List<Vertex> l = addLayer(hiddenPrefix, newNeuronArray(hiddenPrefix, v.graph(), layerSize));
            this.hidden.add(l);
            connectFully(previousLayer, l);            
        }
        
        this.outputs = addLayer("output", newNeuronArray("output", v.graph(), numOutputs));             
        for (Vertex o : this.outputs) 
            o.property("output", true);
        
        
        connectFully(this.hidden.get(this.hidden.size() - 1), this.outputs);
                
        //Add Bias to eveything except inputs:
        traverseSignaledNeuronsForward().sideEffect(neuron -> {
            addBias(neuron.get(), 1.0);
        }).iterate();
                
        //TODO add the created neurons as neighbors of meta
        //TODO test if already created, if not avoid re-creating
        //TODO allow implicit bias, reducing # of vertices/edges needed
    }

    public List<Vertex> addLayer(String id, List<Vertex> neurons) {
        int j = 0;
        Vertex l = meta.graph().addVertex("layer." + id);
        l.property("layerNum", layers.size());
        layers.add(l);
        meta.addEdge("layer", l);
        for (Vertex i : neurons) {
            l.addEdge("" + (j++), i);
        }
        return neurons;
    }


    public static double inputActivity(Vertex neuron) {
        double signalSum = 0;
        GraphTraversal<Vertex, Number> incomingSynapseOutputs = neuron.in("synapse").values("signal");
        while (incomingSynapseOutputs.hasNext()) {
            signalSum += incomingSynapseOutputs.next().doubleValue();
        }
        //neuron.property("activity", signalSum);
        set(neuron, "activity", signalSum);
        return signalSum;
    }

    public static double activate(Vertex neuron) {
        if (isTrue(neuron, "input")) {
            System.err.println("unnecessary activation: " + neuron);
            return real(neuron, "signal");
        }
        
        double activity = inputActivity(neuron);
        
        set(neuron, "activity", activity);
        
        //TODO select activation function from according to stored property in the neuron
        double signal = defaultActivationFunction.activate(activity);
        set(neuron, "signal", signal);
        
        return signal;        
    }
    

    public LayerBrain input(boolean forward, double... d) {
        assert (d.length == inputs.size());
        for (int i = 0; i < d.length; i++) {
            set(inputs.get(i), "signal", d[i]);
        }
        if (forward) forward();
        return this;
    }

    public ArrayRealVector signals(int layer) {
        List<Vertex> ll = getLayer(layer);
        ArrayRealVector r = new ArrayRealVector(ll.size());
        double[] d = r.getDataRef();
        int j = 0;
        for (Vertex o : ll) {
            d[j++] = ((Number)o.value("signal")).doubleValue();
        }
        return r;
    }
    
    public ArrayRealVector inputSignals() {
        return signals(0);
    }

    public ArrayRealVector outputSignals() {
        return signals(hidden.size() + 1);
    }

    /**
     * layer 0 = input, layer 1...h = hidden, layer h+1 = output
     */
    public List<Vertex> getLayer(final int l) {
        if (l == 0) {
            return inputs;
        }
        if (l >= hidden.size() + 1) {
            return outputs;
        }
        return hidden.get(l - 1);
    }

    public int geLayerSize(int l) {
        return getLayer(l).size();
    }

    public void forward() {
        traverseSignaledNeuronsForward().filter((v) -> !inputs.contains(v.get())).sideEffect(
                n -> activate(n.get())
        ).iterate();
    }

    public void connectFully(List<Vertex> source, List<Vertex> target) {
        for (int i = 0; i < source.size(); i++) {
            for (int j = 0; j < target.size(); j++) {
                newSynapse(source.get(i), target.get(j));
            }
        }
    }

    public static Edge newSynapse(Vertex source, Vertex target) {
        Edge s = source.addEdge("synapse", target);
        //TODO multiple synapse initial weight methods
        //    private static final Random RANDOM = new Random();
        final double RANGE = 2.0;
        final double OFFSET = -1.0;
        final double SCALE = 0.1;
        set(s, "weight", ((Math.random() * RANGE) + OFFSET) * SCALE);
        return s;
    }

    public static List<Vertex> newNeuronArray(String prefix, Graph g, int size) {
        List<Vertex> l = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            l.add(newNeuronVertex(prefix + "." + i, g));
        }
        return l;
    }

    public static List<Vertex> newSignalArray(String label, Graph g, int size) {
        List<Vertex> l = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            l.add(newSignalVertex(label + "." + i, g));
        }
        return l;
    }

    public static Vertex newSignalVertex(String label, Graph g) {
        return newSignalVertex(label, g, 0);
    }

    public static Vertex newNeuronVertex(String label, Graph g) {
        Vertex v = g.addVertex(label);
        v.property("activity", 0);
        v.property("signal", 0);
        return v;
    }

    public Vertex addBias(Vertex neuron, double value) {
        Vertex b = newSignalVertex(neuron.label() + ".bias", meta.graph(), value);
        set(b, "bias", true);
        newSynapse(b, neuron);
        return b;
    }
    
    public static Vertex newSignalVertex(String label, Graph g, double initialValue) {
        Vertex v = g.addVertex(label);
        v.property("signal", initialValue);
        v.property("input", true);
        return v;
    }


    
    
    public GraphTraversal<Vertex, Vertex> traverseNeurons() {
        return traverseNeuronsByIncreasingLayer();
    }

    public GraphTraversal<Vertex, Vertex> traverseLayer(int l) {
        return layers.get(l).out();
    }

    /** forward traverses all neurons which are themselves signaled by other neurons */
    public GraphTraversal<Vertex, Vertex> traverseSignaledNeuronsForward() {
        return traverseNeuronsByIncreasingLayer().
                filter(n -> (!n.get().value("input",false)));
    }
            
    public GraphTraversal<Vertex, Vertex> traverseLayersIncreasing() {
        return meta.out("layer").order(increasingLayer);
    }

    public GraphTraversal<Vertex, Vertex> traverseLayersDecreasing() {
        return meta.out("layer").order(decreasingLayer);
    }

    public GraphTraversal<Vertex, Vertex> traverseNeuronsByIncreasingLayer() {
        return traverseLayersIncreasing().out();
    }

    public GraphTraversal<Vertex, Vertex> traverseNeuronsByDecreasingLayer() {
        return traverseLayersDecreasing().out();
    }
    
    public GraphTraversal<Vertex, Vertex> traverseInputNeurons() {
        //TODO use cached input list with inject, this is for generalization but LayerBrain can accelerate this result
        return meta.graph().V().has("input", true);
    }        
    
    public GraphTraversal<Vertex, Vertex> traverseOutputNeurons() {
        //TODO use cached output list with inject, this is for generalization but LayerBrain can accelerate this result
        return meta.graph().V().has("output", true);
    }

    public void iterateNeuronsBackward(Iterable<Vertex> neurons, Function<Vertex,Boolean> f) {
        Set<Vertex> nextLayer = new HashSet();
        for (Vertex n : neurons) {
            if (f.apply(n)) {                        
                GraphTraversal<Vertex, Vertex> incoming = n.in("synapse");
                while (incoming.hasNext()) nextLayer.add(incoming.next());                
            }
        }
        if (!nextLayer.isEmpty())
            iterateNeuronsBackward(nextLayer, f);
        
        //return meta.graph().V().filter(v -> (v.get().out().hasNext()==false) ).in().tree();
        /*return meta.graph().V().union(
                traverseOutputNeurons(),
                traverseOutputNeurons().in("synapse")
        );*/
        //return meta.graph().inject(outputs.toArray(new Vertex[outputs.size()]));//.in("synapse");
                
                      
                
    }
    
    public void printAllNeurons() {
        traverseNeurons().sideEffect(n -> printVertex(n.get())).properties().sideEffect(prop -> System.out.println("  " + prop)).iterate();
    }

    public ActivationFunction getActivationFunction() {
        return defaultActivationFunction;
    }
    
}
