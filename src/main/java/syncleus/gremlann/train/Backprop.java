/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.train;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AtomicDouble;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import static syncleus.gremlann.Graphs.isTrue;
import static syncleus.gremlann.Graphs.real;
import static syncleus.gremlann.Graphs.set;
import syncleus.gremlann.LayerBrain;
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
    
    
    public static double weight(Edge e) {  return real(e, "weight");    }        
    public static double deltaTrain(Vertex v) {   return real(v, "deltaTrain", 0);    }
    public double learningRate(Vertex v) {  return real(v, "learningRate", learningRate);    }
    public static double signal(Vertex v) {  return real(v, "signal");    }
    public static double activity(Vertex v) {  return real(v, "activity",0);    }
    
    public void backpropagate(final Vertex neuron) {

        final AtomicDouble newDeltaTrain = new AtomicDouble(0);
        
        System.out.println("bp " + neuron.label());
        
        neuron.inE("synapse").sideEffect(et -> {
           Edge synapse = et.get();
           
           Vertex source = synapse.outV().next();
                      
           double sourceDelta = deltaTrain(source);
           
           double oldWeight = weight(synapse);
           
           double newWeight = oldWeight + (sourceDelta * learningRate(source) * signal(source));
           
           System.out.println("   " + source.label() + " <-- " + neuron.label() + " " + sourceDelta + " " + learningRate(source) + " " + signal(source) + " "+ " newWeight " + newWeight + " " + oldWeight);
           set(synapse, "weight", newWeight);           
           
           newDeltaTrain.addAndGet( newWeight * sourceDelta );
           
        }).iterate();

        double ndt = newDeltaTrain.get() * getActivationFunction().activateDerivative(activity(neuron));

        set(neuron, "deltaTrain", ndt);
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
            brain.iterateNeuronsBackward(brain.traverseOutputNeurons().toSet(), new Function<Vertex,Boolean>() {
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
