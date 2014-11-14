/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import syncleus.gremlann.topology.LayerBrain;
import com.google.common.base.Function;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static syncleus.gremlann.Graphs.isTrue;
import static syncleus.gremlann.Graphs.printVertex;
import syncleus.gremlann.activation.ActivationFunction;

/**
 *
 * @author me
 */
public class LayerBrainTest {

    @Test public void testLayerBrain1() {
        TinkerGraph g = TinkerGraph.open();
                
        LayerBrain b = new LayerBrain(g.addVertex(), 3,2,1) {
            @Override public ActivationFunction getActivationFunction() {
                throw new UnsupportedOperationException("Not supported yet.");
            }            
        };

        assertEquals("output=0 on new unpropagated network", b.outputSignals().getL1Norm(), 0.0, 0.01);
        
        b.input(false, 1,1,1);
        
        assertTrue("propagating some values will make output vector non-zero", b.outputSignals().getL1Norm() != 0.0);        

        Vertex n = null;        
        GraphTraversal<Vertex, Vertex> f = b.traverseSignaledNeuronsForward();
        while (f.hasNext()) {
            n = f.next();
            assertTrue(!isTrue(n, "input"));
        }
        assertTrue("outputs should be last in this traversal", isTrue(n, "output"));
        
        b.iterateNeuronsBackward( b.traverseOutputNeurons().toSet(), new Function<Vertex,Boolean>() {
            @Override public Boolean apply(Vertex f) {
                printVertex(f);
                return true;
            }            
        });
        

        
    }
}
