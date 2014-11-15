/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.model.som;

import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import syncleus.gremlann.Graphs;
import static syncleus.gremlann.Graphs.set;
import static syncleus.gremlann.Graphs.the;
import syncleus.gremlann.Neuron;
import static syncleus.gremlann.Neuron.signal;
import syncleus.gremlann.activation.ActivationFunction;
import syncleus.gremlann.activation.ActivationFunctions;
import syncleus.gremlann.activation.SqrtActivationFunction;
import syncleus.gremlann.topology.BipartiteBrain;
import syncleus.gremlann.topology.adjacency.AdjacencyVisitor;
import syncleus.gremlann.topology.adjacency.TableAdjacency;

/**
 *
 */
public class SOM extends BipartiteBrain {
    
    protected Class<? extends ActivationFunction> activationFunctionClass = SqrtActivationFunction.class;
    protected SomModel model;
    protected RealVector upperBounds, lowerBounds;
    protected int iterationsTrained;
    TableAdjacency<Double> weight;
    
    public SOM(Vertex v, SomModel model, int dimension, int inputCount) {        
        super(v, newSignalArray("input", v.graph(), inputCount), new ArrayList());
        
        
        weight = new TableAdjacency<Double>();                
        
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
        
        setPosition(output, position);
                
        // connect all inputs to the new neuron
        for (final Vertex input : this.getInputs()) {            
            weight.set(input, output, getRandomInitialSynapseWeight());
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
    public boolean train(final Vertex output, final RealVector bestMatchPoint, final double neighborhoodRadius) {
        
        final double currentDistance = getPosition(output).getDistance(bestMatchPoint);        
        final double learningRate = getModel().learningRateFunction(this);
        
        if (currentDistance < neighborhoodRadius) {
            
            final double neighborhoodAdjustment = getModel().neighborhoodFunction(this, currentDistance);            
            double totalChange = weight.forEachInput(output, new AdjacencyVisitor<Double,Double>() {

                @Override
                public Double adjacency(Vertex source, Vertex output, Double currentWeight, Double totalChange) {
                    double sourceSignal = signal(source);
                    
                    double weightDelta = (learningRate * neighborhoodAdjustment * sourceSignal - currentWeight);
                    double newWeight = currentWeight + weightDelta;
                   
                    weight.setDirect(source, output, newWeight);
                    
                    return totalChange + weightDelta;
                }
                
            }, 0d);
                        
            return true;
        }
        return false;
    }
    
    
    /**
     * Propagates all the inputs to determine to calculate the output.
     *
     * @since 2.0
     */    
    public double propagateOutput(Vertex output) {
        // calculate the current input activity        
        
        double activity = weight.forEachInput(output, new AdjacencyVisitor<Double, Double>() {
            @Override
            public Double adjacency(Vertex input, Vertex output, Double weight, Double accumulatedActivity) {
                double synapseActivity = signal(input) * weight;
                return accumulatedActivity + Math.pow(synapseActivity - weight, 2.0);
            }
        }, 0d);
        
        
        Neuron.activity(output, activity);
        
        // calculate the activity function and reset the result as the output        
        double s = getActivationFunction().activate(activity);        
        Neuron.signal(output, s);      
        
        return s;        
    }    

    public Class<? extends ActivationFunction> getActivationFunctionClass() {
        return activationFunctionClass;
    }
    
        
    public ActivationFunction getActivationFunction() {        
        return ActivationFunctions.get(getActivationFunctionClass());
    }

    


    
    public RealVector getPosition(Vertex output) {        
        double[] p = the(output, SomOutput.class).getDataRef();
        if (p.length!=getDimension()) {
            p = new double[ getDimension() ];
        }
        return new ArrayRealVector(p);
    }
    
    public void setPosition(Vertex output, RealVector position) {
        set(output, SomOutput.class, new SomOutput(position));
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
        
        //if (weight instanceof TableAdjacency) {            
            return ((TableAdjacency)weight).getTable();
        //}
        
//        int numInputs = getInputCount();
//        
//        //TODO check dimensions on this:
//        Table<Vertex,Vertex,Double> t = HashBasedTable.create(numInputs, numInputs);
//        
//        final HashMap<RealVector, double[]> weightVectors = new HashMap();
//        for (final Vertex currentNeuron : getOutputs()) {
//            
//            final double[] weightVector = new double[numInputs];            
//            final RealVector currentPoint = getPosition(currentNeuron);
//            
//            // iterate through the weight vectors of the current neuron
//            GraphTraversal<Vertex, Edge> edges = currentNeuron.inE();
//            while (edges.hasNext()) {
//                final Edge s = edges.next();
//                t.put(currentNeuron, s.outV().next(), Synapse.weight(s));                
//            }
//            
//            // add the current weight vector to the map
//            weightVectors.put(currentPoint, weightVector);
//        }
//        
//        //TODO make immutable
//        return t;
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
