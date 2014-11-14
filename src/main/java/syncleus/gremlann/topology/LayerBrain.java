/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.topology;

import com.google.common.base.Function;
import syncleus.gremlann.activation.ActivationFunction;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.linear.ArrayRealVector;
import static syncleus.gremlann.Graphs.*;

/**
 * Brain which is structured as layers
 */
abstract public class LayerBrain extends BipartiteBrain {
    
    protected final List<List<Vertex>> hidden;
    protected final List<Vertex> layers;    
    


    public LayerBrain(Vertex v, int inputs, int hidden, int outputs) {
        this(v, inputs, new int[]{hidden}, outputs);
    }

    public LayerBrain(Vertex v, int numInputs, int[] numHidden, int numOutputs) {
        this(v, newSignalArray("input", v.graph(), numInputs), numHidden, numOutputs);
    }

    public LayerBrain(Vertex v, List<Vertex> inputs, int[] numHidden, int numOutputs) {
        super(v, inputs, newNeuronArray("output", v.graph(), numOutputs));
        
        this.layers = new ArrayList(2 + numHidden.length);
        
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
        
        this.outputs = addLayer("output", this.outputs);             
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
        meta.addEdge("layer", l);
        for (Vertex i : neurons) {
            l.addEdge("" + (j++), i);
        }
        layers.add(l);
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

    public double activate(Vertex neuron) {
        if (isTrue(neuron, "input")) {
            System.err.println("unnecessary activation: " + neuron);
            return real(neuron, "signal");
        }
        
        double activity = inputActivity(neuron);
        
        set(neuron, "activity", activity);
        
        //TODO select activation function from according to stored property in the neuron
        double signal = getActivationFunction().activate(activity);
        set(neuron, "signal", signal);
        
        //System.out.println("activate: " + neuron.label() + " " + activity + " " + signal);
        
        return signal;        
    }
    
    public ArrayRealVector signals(int layer) {        
        return signals(getLayer(layer));
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

    
    public LayerBrain input(boolean forward, double... d) {
        super.input(d);
        if (forward) forward();
        return this;
    }
    
    
    
    public void forward() {
        traverseSignaledNeuronsForward().sideEffect(
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



    public Vertex addBias(Vertex neuron, double value) {
        Vertex b = newSignalVertex(neuron.label() + ".bias", meta.graph(), value);
        set(b, "bias", true);
        newSynapse(b, neuron);
        return b;
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
    

    abstract public ActivationFunction getActivationFunction();


    
}
