/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.train;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AtomicDouble;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.Set;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import static syncleus.gremlann.AbstractBrain.activity;
import static syncleus.gremlann.AbstractBrain.signal;
import static syncleus.gremlann.AbstractBrain.weight;
import static syncleus.gremlann.Graphs.isTrue;
import static syncleus.gremlann.Graphs.real;
import static syncleus.gremlann.Graphs.set;
import syncleus.gremlann.topology.LayerBrain;
import syncleus.gremlann.activation.ActivationFunction;

/**
 *
 * @author me
 */
public class Backprop {

    private static final double DEFAULT_LEARNING_RATE = 0.0175;
    
    private final LayerBrain brain;
    private final double learningRate;

    //TODO generalize to non-LayerBrain brains
    public Backprop(LayerBrain b) {
        this.brain = b;
        this.learningRate = DEFAULT_LEARNING_RATE;
        
    }
    
    public ActivationFunction getActivationFunction() { return brain.getActivationFunction(); }
    
    public double learningRate(Vertex v) {  return real(v, "learningRate", learningRate);    }
    public static double deltaTrain(Vertex v) {   return real(v, "deltaTrain",0);    }
    
    public void backpropagate(final Vertex neuron) {
        
        //1. calculate deltaTrain based on all the destination synapses
        if (!isTrue(neuron,"output")) {
        
            final AtomicDouble newDeltaTrain = new AtomicDouble(0);
            
            neuron.outE("synapse").sideEffect(et -> {
               Edge synapse = et.get();           
               Vertex target = synapse.outV().next();

               newDeltaTrain.addAndGet( weight(synapse) * deltaTrain(target) );           
            }).iterate();

            double ndt = newDeltaTrain.get() * getActivationFunction().activateDerivative(activity(neuron));
            set(neuron, "deltaTrain", ndt);

            System.out.println(" delta=" + ndt + " " + newDeltaTrain.get());
        }
        
        System.out.println("bp " + neuron.label() + " " + neuron.inE("synapse").toSet().size() + "|" +neuron.outE("synapse").toSet().size() + " d=" + real(neuron,"deltaTrain"));        

        //2. Back-propagates the training data to all the incoming synapses
        neuron.inE("synapse").sideEffect(et -> {
            Edge synapse = et.get();
            Vertex source = synapse.outV().next();
            
            double curWeight = weight(synapse);
            double sourceDelta = deltaTrain(neuron); 
            
            double newWeight = curWeight + (sourceDelta * learningRate(source) * signal(neuron));
            set(synapse, "weight", newWeight);            
            System.out.println("  " + synapse + " " + curWeight + " -> " + newWeight + " "+ sourceDelta + " " + deltaTrain(neuron));
        }).iterate();
        
        
        
        
    }

    /**
     * 
     * @param input
     * @param expectedOutput
     * @param train
     * @return error rate
     */
    public double associate(RealVector input, RealVector expectedOutput, boolean train) {
        
        brain.input(true, array(input));
        
        ArrayRealVector o = brain.outputSignals();
        double distance = o.getDistance(expectedOutput);
        
        if (train) {
            Set<Vertex> outputs = brain.traverseOutputNeurons().toSet();
            
            int j = 0;
            for (Vertex outputNeuron : outputs) {
                double expected = expectedOutput.getEntry(j);
                double outputSignal = signal(outputNeuron);
                double dt = (expected - outputSignal) * getActivationFunction().activateDerivative(activity(outputNeuron));
                        
                set(outputNeuron,"deltaTrain",dt);                
                j++;
            }
            
            brain.iterateNeuronsBackward(outputs, new Function<Vertex,Boolean>() {
                @Override
                public Boolean apply(Vertex f) {
                    //if (isTrue(f,"input")) return false;
                    backpropagate(f);
                    return true;
                }
            });
        
        }
        
        return distance;
    }
    
    public double associate(double[] input, double[] expectedOutput, boolean train) {
        return associate(new ArrayRealVector(input), new ArrayRealVector(expectedOutput), train);
    }
    
    public double train(double[] input, double[] expectedOutput) {
        return associate(input, expectedOutput, true);
    }
    
    /** convenience method which extracts the direct array from a RealVector if possible, avoiding an array copy when a copy isnt necessary */
    public double[] array(RealVector v) {
        if (v instanceof ArrayRealVector)
            return ((ArrayRealVector)v).getDataRef();
        return v.toArray();
    }
}
