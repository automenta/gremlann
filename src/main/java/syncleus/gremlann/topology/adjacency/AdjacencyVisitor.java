/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.topology.adjacency;

import com.tinkerpop.gremlin.structure.Vertex;

public interface AdjacencyVisitor<X,Y> {
    public Y adjacency(Vertex input, Vertex output, X adjacencyValue, Y accumulator);
}
    
