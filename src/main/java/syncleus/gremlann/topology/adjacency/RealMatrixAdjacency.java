/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.topology.adjacency;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import syncleus.gremlann.topology.adjacency.Adjacency.IntegerIndexedAdjacency;

/**
 *
 * @author me
 */
public class RealMatrixAdjacency implements Adjacency<Double>, IntegerIndexedAdjacency<Double>, Serializable {
    
    public final List<Vertex> input;
    public final List<Vertex> output;
    public final HashMap<Vertex, Integer> inputIndex;
    public final HashMap<Vertex, Integer> outputIndex;
    public final RealMatrix matrix;

    public RealMatrixAdjacency(List<Vertex> input, List<Vertex> output) {
        this(input, output, new Array2DRowRealMatrix(input.size(), output.size()));
    }

    
    public RealMatrixAdjacency(List<Vertex> input, List<Vertex> output, RealMatrix matrix) {
        this.input = input;
        this.output = output;
        this.matrix = matrix;
        
        inputIndex = new HashMap(input.size());
        int j = 0;
        for (Vertex i : input)
            inputIndex.put(i, j++);
        
        if (input == output) {
            outputIndex = inputIndex;
        }
        else {
            outputIndex = new HashMap(output.size());
            int k = 0;
            for (Vertex i : output)
                outputIndex.put(i, k++);
        }
    }

    @Override
    public List<Vertex> getInputs() {
        return input;
    }

    @Override
    public List<Vertex> getOutputs() {
        return output;
    }

    @Override
    public Double get(int source, int target) {
        return matrix.getEntry(source, target);
    }
    
    
    @Override
    public Double get(final Vertex source, final Vertex target) {
        return get(inputIndex.get(source), outputIndex.get(target));
    }

    @Override
    public void set(int source, int target, Double x) {
        matrix.setEntry(source, target, x);
    }
    
    @Override
    public void set(Vertex source, Vertex target, Double x) {
        set(inputIndex.get(source), outputIndex.get(target), x);
    }

    @Override
    public EdgeAdjacency toEdges(Graph g) {
        return null;
    }

    @Override
    public Serializable toCompact() {
        return this;
    }
    
}
