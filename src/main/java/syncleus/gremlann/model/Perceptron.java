/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.model;

import com.tinkerpop.gremlin.structure.Vertex;
import java.util.List;
import syncleus.gremlann.activation.ActivationFunction;
import syncleus.gremlann.activation.HyperbolicTangentActivationFunction;
import syncleus.gremlann.topology.LayerBrain;

/**
 * Multilayer feedforward network
 */
public class Perceptron extends LayerBrain {
    
    public static final ActivationFunction defaultActivationFunction = new HyperbolicTangentActivationFunction();
    
    public Perceptron(Vertex v, List<Vertex> inputs, int[] numHidden, int numOutputs) {
        super(v, inputs, numHidden, numOutputs);
    }

    public Perceptron(Vertex v, int inputs, int hidden, int outputs) {
        super(v, inputs, hidden, outputs);
    }
    
    
    @Override
    public ActivationFunction getActivationFunction() {
        return defaultActivationFunction;
    }    
}
