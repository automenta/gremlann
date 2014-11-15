/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.topology.adjacency;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import java.io.Serializable;

/**
 * Abstract adjacency structure which can morph between implementations as needed:
 * 
 *      -(slow) lowest common denominator Edge graph representation
 *      -(fast) primitive numeric matrices (dense & sparse)
 *      -(fast) formula-determined value: X x = f(input, output)
 * 
 */
public interface Adjacency<X> {
    
    public Iterable<Vertex> getInputs();
    public Iterable<Vertex> getOutputs();
    
    public X get(Vertex source, Vertex target);
    public void set(Vertex source, Vertex target, X x);
    
    public EdgeAdjacency toEdges(Graph g);
    
    public Serializable toCompact();
    
    /** allows indexing by the integer order of the input and output vertices */
    public interface IntegerIndexedAdjacency<X> {
        
        public X get(int source, int target);
        public void set(int source, int target, X x);
        
    }
}
