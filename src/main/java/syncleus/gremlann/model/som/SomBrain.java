/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.model.som;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import syncleus.gremlann.Graphs;
import static syncleus.gremlann.Graphs.doubles;
import static syncleus.gremlann.Graphs.set;
import syncleus.gremlann.Synapses;
import syncleus.gremlann.activation.ActivationFunction;
import syncleus.gremlann.activation.ActivationFunctions;
import syncleus.gremlann.activation.SqrtActivationFunction;
import syncleus.gremlann.topology.BipartiteBrain;

/**
 *
 */
public class SomBrain extends BipartiteBrain {
    
    protected Class<? extends ActivationFunction> activationFunctionClass = SqrtActivationFunction.class;
    protected SomModel model;
    protected RealVector upperBounds, lowerBounds;
    protected int iterationsTrained;
    
    public SomBrain(Vertex v, SomModel model, int dimension, int inputCount) {        
        super(v, newSignalArray("input", v.graph(), inputCount), new ArrayList());
        
        setUpperBounds( new ArrayRealVector(dimension) );
        setLowerBounds( new ArrayRealVector(dimension) );
        setIterationsTrained(0);
        setModel(model);
    }


    
    private void updateBounds(final RealVector position) {
        //make sure we have the proper dimentionality
        if (position.getDimension() != getDimension())
            throw new IllegalArgumentException("Dimentionality mismatch");

        for (int dimensionIndex = 0; dimensionIndex < position.getDimension(); dimensionIndex++) {
            final double v = position.getEntry(dimensionIndex);
            
            if (this.getUpperBounds().getEntry(dimensionIndex) < v)
                this.getUpperBounds().setEntry(dimensionIndex, v);
            if (this.getLowerBounds().getEntry(dimensionIndex) > v)
                this.getLowerBounds().setEntry(dimensionIndex, v);
        }
    }

    public int getDimension() {
        if (getUpperBounds() == null)
            return 0;
        return getUpperBounds().getDimension();
    }
    
    
    @Override
    protected final void ensureCorrectOutputDimensions(int d) {
        if (d!=getDimension())
            throw new RuntimeException("Invalid dimension");
    }

    public Vertex addOutput(final double... d) {    
        ensureCorrectOutputDimensions(d.length);
        return addOutput(new ArrayRealVector(d));
    }
 
    
    /**
     * Creates a new point in the output lattice at the given position. This will
     * automatically have all inputs connected to it.
     *
     * @param position The position of the new output in the lattice.
     * @since 2.0
     */    
    public Vertex addOutput(final RealVector position) {        
        ensureCorrectOutputDimensions(position.getDimension());

        // increase the upper bounds if needed
        this.updateBounds(position);
        
        // create and add the new output neuron
        
        Vertex output = newNeuronVertex("output", meta.graph());
        outputs.add(output);
        
        set(output, "position", doubles(position));
        
        // connect all inputs to the new neuron
        for (final Vertex input : this.getInputs()) {            
            Edge s = input.addEdge("synapse", output);
            set(s, "weight", getRandomInitialSynapseWeight() );
        }
        
        return output;
    }
    

    /**
     * Gets the positions of all the outputs in the output lattice.
     *
     * @return the positions of all the outputs in the output lattice.
     * @since 2.0
     */
    public final Set<RealVector> getPositions(boolean cloned) {
        final Set<RealVector> positions = new HashSet<RealVector>();
        for (final Vertex output: getOutputs()) {
            RealVector p = new ArrayRealVector( (double[])output.value("position") );
            if (cloned) {
                p = new ArrayRealVector(p);
            }
            positions.add(p);
        }
        return Collections.unmodifiableSet(positions);
    }
    

    public SomModel getModel() {
        return model;
    }

    public void setModel(SomModel model) {
        this.model = model;
    }

    public RealVector getUpperBounds() {
        return upperBounds;
    }

    public RealVector getLowerBounds() {
        return lowerBounds;
    }

    public void setUpperBounds(RealVector upperBounds) {
        this.upperBounds = upperBounds;
    }

    public void setLowerBounds(RealVector lowerBounds) {
        this.lowerBounds = lowerBounds;
    }
    
    public void setIterationsTrained(int iterationsTrained) {
        this.iterationsTrained = iterationsTrained;
    }

    public int getIterationsTrained() {
        return iterationsTrained;
    }

            
    public final Set<RealVector> getPositions() {
        return getPositions(true);
    }
    
//    /**
//     * Gets the current output at the specified position in the output lattice if
// the position does not have a AbstractSomNeuron associated with it then it throws
// an exception.
//     *
//     * @param position position in the output lattice of the output you wish to
//     *                 retrieve.
//     * @return The value of the specified AbstractSomNeuron, or null if there is
// no AbstractSomNeuron associated with the given position.
//     * @throws IllegalArgumentException if position does not exist.
//     * @since 2.0
//     */
//    @Override
//    public final double getOutput(final RealVector position) {
//        final ON outputNeuron = this.outputs.get(position);
//        if (outputNeuron == null)
//            throw new IllegalArgumentException("position does not exist");
//
//        outputNeuron.tick();
//        return outputNeuron.getOutput();
//    }

    /**
     * Obtains the BMU (Best Matching Unit) for the current input reset.
     * This will also train against the current input.
     *
     * @return the BMU for the current input reset.
     */
    public final RealVector getBestMatchingUnit() {
        return this.getBestMatchingUnit(true);
    }

    public final RealVector getBestMatchingUnit(final boolean train) {
        Vertex bestMatchingUnit = null;
        double bestMatch = Double.POSITIVE_INFINITY;
        
        for (final Vertex neuron : getOutputs()) {            
            final double outputSignal = propagateOutput(neuron);                        
            
            if ((bestMatchingUnit == null) || (outputSignal < bestMatch)) {
                bestMatchingUnit = neuron;
                bestMatch = outputSignal;                
            }
        }
        
        RealVector bestMatchingUnitVector = getPosition(bestMatchingUnit);
        
        if (train)
            this.train(bestMatchingUnitVector);

        return bestMatchingUnitVector;
    }

    
    /**
     * Trains the neuron to be closer to the input vector according to the
     * specified parameters.
     *
     * @since 2.0
     */
    public boolean train(final Vertex neuron, final RealVector bestMatchPoint, final double neighborhoodRadius) {
        
        final double currentDistance = getPosition(neuron).getDistance(bestMatchPoint);        
        final double learningRate = getModel().learningRateFunction(this);
        
        if (currentDistance < neighborhoodRadius) {
            
            final double neighborhoodAdjustment = getModel().neighborhoodFunction(this, currentDistance);            
            GraphTraversal<Vertex, Edge> edges = neuron.inE("synapse");
            while (edges.hasNext()) {
                Edge synapse = edges.next();
                Vertex sourceInput = synapse.inV().next();
                
                //source.setWeight(source.getWeight() + (learningRate * neighborhoodAdjustment * (source.getInput() - source.getWeight())));

               double w = weight(synapse);
               weight(synapse, w + (learningRate * neighborhoodAdjustment * (signal(sourceInput) - w)));
            }
            
            return true;
        }
        return false;
    }
    
    
    /**
     * Propagates all the inputs to determine to calculate the output.
     *
     * @since 2.0
     */    
    public double propagateOutput(Vertex v) {
        // calculate the current input activity        
        
        double activity = 0;
        
        GraphTraversal<Vertex, Edge> sources = v.inE("synapse");
        while (sources.hasNext()) {
            Edge source = sources.next();
            double a = Synapses.propagate(source);
            //activity += Math.pow(source.getSignal() - source.getWeight(), 2.0);
            activity += Math.pow(a - weight(source), 2.0);
        }

        activity(v, activity);
        
        // calculate the activity function and reset the result as the output        
        double s = getActivationFunction().activate(activity);        
        signal(v, s);        
        return s;        
    }    

    public Class<? extends ActivationFunction> getActivationFunctionClass() {
        return activationFunctionClass;
    }
    
        
    public ActivationFunction getActivationFunction() {        
        return ActivationFunctions.get(getActivationFunctionClass());
    }

    
    /**
     * Applies an input vector to the input nodes, according to their natural ordering
     * @param v 
     */
    public void setInput(final RealVector v) {         setInput(v.toArray());     }
    
    public void setInput(final ArrayRealVector v) {        setInput(v.getDataRef());    }

    
    @Deprecated public void setInput(final double... d) {
        input(d);
    }
    
    public RealVector getPosition(Vertex output) {
        return new ArrayRealVector((double[])output.value("position"));
    }
    

    private void train(final RealVector bestMatchingUnit) {
        final double neighborhoodRadius = this.neighborhoodRadiusFunction();
        
        for (final Vertex neuron : getOutputs()) {
            train(neuron, bestMatchingUnit, neighborhoodRadius);
        }
        
        setIterationsTrained(getIterationsTrained()+1);
    }
    
    
    public final int getOutputCount() {
        return Iterables.size(getOutputs());
    }


    /**
     * Obtains the weight vectors of the outputs.
     *
     * @return the weight vectors of each output in the output lattice
     * @since 2.0
     */
    public final Table<Vertex,Vertex,Double> getOutputWeights() {
        // iterate through the output lattice
        
        int numInputs = getInputCount();
        
        //TODO check dimensions on this:
        Table<Vertex,Vertex,Double> t = HashBasedTable.create(numInputs, numInputs);
        
        final HashMap<RealVector, double[]> weightVectors = new HashMap();
        for (final Vertex currentNeuron : getOutputs()) {
            
            final double[] weightVector = new double[numInputs];            
            final RealVector currentPoint = getPosition(currentNeuron);
            
            // iterate through the weight vectors of the current neuron
            GraphTraversal<Vertex, Edge> edges = currentNeuron.inE();
            while (edges.hasNext()) {
                final Edge s = edges.next();
                t.put(currentNeuron, s.outV().next(), weight(s));                
            }
            
            // add the current weight vector to the map
            weightVectors.put(currentPoint, weightVector);
        }
        
        //TODO make immutable
        return t;
    }
    /**
     * Determine the current radius of the neighborhood which will be centered
     * around the Best Matching Unit (BMU).
     *
     * @return the current radius of the neighborhood.
     * @since 2.0
     */
    public double neighborhoodRadiusFunction() {
        return getModel().neighborhoodRadiusFunction(this);
    }

    /**
     * Determines the current learning rate for the network.
     *
     * @return the current learning rate for the network.
     * @since 2.0
     */
    public double learningRateFunction() {
        return getModel().learningRateFunction(this);
    }

    private double getRandomInitialSynapseWeight() {
        return ((Graphs.random.nextDouble() * 2.0) - 1.0) / 10000.0;
    }


    
}
